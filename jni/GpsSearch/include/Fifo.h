#ifndef __FIFO_H__
#define __FIFO_H__

#ifdef __FIFO_C__
#define __FIFO_DEC__
#else
#define __FIFO_DEC__ extern
#endif

#define FIFO_SUCCESS						(0)

#define FIFO_ERR_INVALID_PARMETER			(-1)
#define FIFO_ERR_NOT_ENOUGH_MEMORY			(-2)
#define FIFO_ERR_LOCK						(-3)
#define FIFO_ERR_UNLOCK						(-4)
#define FIFO_ERR_READ_EMPTY					(-5)

typedef uint8 (*lock_type)(void);

typedef struct fifo_info
{
	uint8 *buf;						//buffer to read/write
	uint32 size;					//total buffer size
	uint32 in;						//count is writed to buffer
	uint32 out;						//count is read from buffer
	lock_type lock;					//lock function
	lock_type unlock;				//unlock function
}FIFO_TYPE;

static int8 inline fifo_get(struct fifo_info *fifo, uint8 *c)
{
	if(fifo->out >= fifo->in)
	{
		return FIFO_ERR_READ_EMPTY;
	}

	*c = fifo->buf[fifo->out % fifo->size];
	fifo->out++;

	return FIFO_SUCCESS;
}

static int8 inline fifo_getpos(struct fifo_info *fifo, uint8 *c, uint32 pos)
{
	if(fifo->out >= fifo->in || ((fifo->out + pos) > fifo->in))
	{
		return FIFO_ERR_READ_EMPTY;
	}

	*c = fifo->buf[(fifo->out + pos) % fifo->size];

	return FIFO_SUCCESS;
}

static int8 inline fifo_put(struct fifo_info *fifo, uint8 c)
{
	fifo->buf[fifo->in % fifo->size] = c;
	fifo->in ++;

	return FIFO_SUCCESS;
}

static int8 inline fifo_inc_out(struct fifo_info *fifo, uint32 offset)
{
	fifo->out += offset;
	return FIFO_SUCCESS;
}

__FIFO_DEC__ struct fifo_info *fifo_init(uint8 *buf, uint32 size, lock_type lock, lock_type unlock);
__FIFO_DEC__ int8 fifo_write(struct fifo_info *fifo, uint8 *w_buf, uint32 size);
__FIFO_DEC__ uint8 fifo_read(struct fifo_info *fifo, uint8 *r_buf, uint32 size);
__FIFO_DEC__ uint32 fifo_len(struct fifo_info *fifo);
__FIFO_DEC__ uint8 fifo_reset(struct fifo_info *fifo);
__FIFO_DEC__ uint8 fifo_free(struct fifo_info *fifo);
#endif
