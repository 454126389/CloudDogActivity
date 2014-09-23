#include <stdio.h>
#include <errno.h>
#include <termios.h>
#include <fcntl.h>
#include <string.h>
#include <sys/epoll.h>
#include <pthread.h>
#include <sys/socket.h>
#include <netinet/in.h>

#include "config.h"
#include "common.h"
#include "List.h"
#include "Fifo.h"

#define __MCU_C__
#include "Mcu.h"


#define THREAD_QUIT_CMD					2

#define STEP_WAIT_HEADER				0
#define STEP_READ_TYPE					1
#define STEP_READ_DATA					2
#define STEP_WAIT_TAIL					3

typedef struct
{
	int fd;					        //radar device node
	pthread_t thread_id;			//the thread id
	int control[2];
	struct fifo_info *fifo;
	uint8 proc_code;
	uint8 proc_step;
}MCU_INFO;

static MCU_INFO mci;
static char *tty_path = "/dev/ttyS1";
static char *header = "$*$";
static char *tail = "#*#";

#define FIFO_SIZE						64
static uint8 fifo_buffer[FIFO_SIZE];

static int epoll_register( int  epoll_fd, int  fd )
{
    struct epoll_event  ev;
    int                 ret, flags;

    /* important: make the fd non-blocking */
    flags = fcntl(fd, F_GETFL);
    fcntl(fd, F_SETFL, flags | O_NONBLOCK);

    ev.events  = EPOLLIN;
    ev.data.fd = fd;
    do {
        ret = epoll_ctl( epoll_fd, EPOLL_CTL_ADD, fd, &ev );
    } while (ret < 0 && errno == EINTR);
    return ret;
}

static inline uint8 c2u4(uint8 c)
{
	uint8 ret = 0;

	if(c >= '0' && c <= '9')
	{
		ret = c - '0';
	}
	else if(c >= 'a' && c <= 'f')
	{
		ret = c - 'a' + 10;
	}
	else if(c >= 'A' && c <= 'F')
	{
		ret = c - 'A' + 10;
	}
	else
	{
		ret = c;
	}

	return ret;
}

static uint32 parseStrToArray(uint8 *str, uint32 size)
{
	uint32 len = size;
	uint32 i = 0;
	uint8 c = 0;

	if(c2u4(str[0]) == str[0])
	{
		//not Hex string
		return size;
	}

	len = size / 2;
	for(i = 0; i < len; i++)
	{
		c = c2u4(str[2 * i]);
		c = (c << 4) | c2u4(str[2 * i + 1]);
		str[i] = c;
	}

	if(size % 2 != 0)
	{
		str[i] = c2u4(str[size - 1]);
		i++;
	}

	return i;
}

static uint32 mcu_process(uint8 *buf, uint32 size)
{
	int8 ret = 0;
	uint8 c, bFound = FALSE;
	uint32 len, i;
	uint8 *tmp, *ps, *pe, *p;
	uint8 code = 0;
	uint8 bNeedFree = FALSE;

	len = fifo_len(mci.fifo);
	if(len > 0)
	{
		tmp = (uint8 *)malloc(len + size + 1);
		fifo_read(mci.fifo, tmp, len);
		memcpy(tmp + len, buf, size);
		len += size;
		bNeedFree = TRUE;
	}
	else
	{
		tmp = buf;
		len = size;
	}

	tmp[len] = 0;
	LOGD("mcu process %s, len %d", tmp, len);

	ps = tmp;
	do
	{
		if(strlen(ps) <= 0)
		{
			//LOGD("len is smaller");
			break;
		}

		p = strstr(ps, header);

		if(p != NULL)
		{
			len -= p - ps;
			ps = p;

			//LOGD("found header");
			pe = strstr(ps, tail);
			if(pe == NULL)
			{
				fifo_write(mci.fifo, ps, len);
				break;
			}
			else
			{
				//ok found
				p = strstr(ps, "@");
				if(p != NULL && p < pe)
				{
					for(i = strlen(header); (ps + i) < p; i++)
					{
						code = code * 10 + *(ps + i) - '0';
					}

					if(code >= MAX_SUPPORT_PROTOCAL || mcu_protocal[code] == NULL)
					{
						LOGD("not support protocal %d", code);
					}
					else
					{
						//fifo_write(mcu_protocal[code]->fifo, ps + strlen(header), pe - ps - strlen(header));
						i = parseStrToArray(p + 1, pe - p - 1);
						mcu_protocal[code]->process(mcu_protocal[code], p + 1, i);
					}
				}

				len = len - (pe + strlen(tail) - ps);
				ps = pe + strlen(tail);
				//LOGD("remain %d, %s", len, ps);
			}
		}
		else
		{
			//LOGD("not found header");
			if(len > 0 && len < 3)
			{
				fifo_write(mci.fifo, ps, len);
			}
			break;
		}
	}while(1);

	if(bNeedFree)
	{
		free(tmp);
	}

	return 0;
}

static int32 mcu_send(PROTOCAL_TYPE prot_code, uint8 *buf, uint32 size)
{
	uint32 ret = 0;
	ret = write(mci.fd, buf, size);
	return ret;
}

static void *mcu_thread(void *arg)
{
	int epoll_fd = epoll_create(2);
	int started = 0;
	int mcu_fd = mci.fd;
	int control_fd = mci.control[1];
	int ret = -1;

	ret = epoll_register( epoll_fd, control_fd );
	//LOGD("epoll_register : control_fd ret=%d", ret);
	ret = epoll_register( epoll_fd, mcu_fd );
	//LOGD("epoll_register : fd ret=%d", ret);

	LOGD("MCU thread running");

	while(1)
	{
		struct epoll_event   events[2];
		int                  ne, nevents;

		nevents = epoll_wait( epoll_fd, events, 2, -1 );
		if (nevents < 0)
		{
			if (errno != EINTR)
			{
				LOGD("%s epoll_wait() unexpected error: %s", __FUNCTION__, strerror(errno));
			}
			continue;
		}

		for (ne = 0; ne < nevents; ne++)
		{
			if ((events[ne].events & (EPOLLERR|EPOLLHUP)) != 0)
			{
				LOGD("%s EPOLLERR or EPOLLHUP after epoll_wait() !?", __FUNCTION__);
				goto Exit;
			}

			if ((events[ne].events & EPOLLIN) != 0)
			{
				int fd = events[ne].data.fd;

				if (fd == control_fd)
				{
					char  cmd = 255;
					int   ret;

					do
					{
						ret = read( fd, &cmd, 1 );
					} while (ret < 0 && errno == EINTR);

					LOGD("mcu control fd event, %d", cmd);
					if (cmd == THREAD_QUIT_CMD)
					{
						goto Exit;
					}
				}
				else if (fd == mcu_fd)
				{
					char  buff[33];

					for (;;)
					{
						int  nn, ret;

						ret = read (fd, buff, 32);
						if (ret < 0)
						{
							if (errno == EINTR)
								continue;
							if (errno != EWOULDBLOCK)
								LOGD("error while reading from mcu daemon socket: %s:", strerror(errno));
							break;
						}
						else
						{
							if(ret > 0)
							{
								mcu_process (buff, ret);
							}
							//LOGD("receive data from mcu %s", buff);
						}
					}
				}
			}
		}
	}


Exit:
	LOGD("MCU thread exit");
	return NULL;
}


int8 registerProtocal(PROTOCAL_TYPE code, MCU_PROTOCAL_INFO *protocal)
{
	if(code >= MAX_SUPPORT_PROTOCAL || protocal == NULL)
	{
		return -1;
	}

	mcu_protocal[code] = protocal;

	if(protocal->init != NULL)
	{
		protocal->init(protocal);
	}
	protocal->send = mcu_send;

	return 0;
}

int8 unRegisterProtocal(PROTOCAL_TYPE code)
{
	if(code >= MAX_SUPPORT_PROTOCAL)
	{
		return -1;
	}

	if(mcu_protocal[code] != NULL && mcu_protocal[code]->deinit != NULL)
	{
		mcu_protocal[code]->deinit(mcu_protocal[code]);
	}
	mcu_protocal[code]->send = NULL;

	mcu_protocal[code] = NULL;

	return 0;
}

uint8 mcu_init(void)
{
	struct termios cfg;
	int err;

	memset(mcu_protocal, 0, sizeof(mcu_protocal));

	mci.control[0] = -1;
	mci.control[1] = -1;
	mci.fd = open(tty_path, O_RDWR);

	if(mci.fd == -1)
	{
		LOGD("MCU open %s fail", tty_path);
		return -1;
	}

	mci.fifo = fifo_init(fifo_buffer, FIFO_SIZE, NULL, NULL);
	if(mci.fifo == NULL)
	{
		LOGD("fifo init error");
		goto Fail;
	}

	if (tcgetattr(mci.fd, &cfg))
	{
		LOGD("MCU tcgetattr() failed");
		close(mci.fd);
		goto Fail;
	}

	cfmakeraw(&cfg);
	cfsetispeed(&cfg, B9600);
	cfsetospeed(&cfg, B9600);

	if (tcsetattr(mci.fd, TCSANOW, &cfg))
	{
		LOGD("MCU tcsetattr() failed");
		close(mci.fd);
		goto Fail;
	}

	if ( socketpair( AF_LOCAL, SOCK_STREAM, 0, mci.control ) < 0 )
	{
		LOGD("could not create thread control socket pair: %s", strerror(errno));
		goto Fail;
	}

	err = pthread_create(&mci.thread_id, NULL, mcu_thread, NULL);
	if(err != 0)
	{
		LOGD("cann't create thread: %s ", strerror(err));
		goto Fail;
	}

	return 0;

Fail:
	mcu_deinit();
	return 0;
}

void mcu_deinit(void)
{
	char cmd = THREAD_QUIT_CMD;
	void *dummy;

	write(mci.control[0], &cmd, 1);
	if(mci.fd >= 0)
	{
		close(mci.fd);
		mci.fd = -1;
	}
	pthread_join(mci.thread_id, &dummy);

	close(mci.control[0]);
	mci.control[0] = -1;
	close(mci.control[1]);
	mci.control[1] = -1;



	fifo_free(mci.fifo);

	LOGD("mcu deinit finish");
}
