#include <string.h>
#include <pthread.h>

#include "common.h"

#define __FIFO_C__
#include "Fifo.h"

struct fifo_info *fifo_init(uint8 *buf, uint32 size, lock_type lock, lock_type unlock)
{
	struct fifo_info *fifo = NULL;

	if(buf == NULL || ((!(uint32)lock) ^ (!(uint32)unlock)))
	{
		return NULL;
	}

	fifo = (struct fifo_info *)malloc(sizeof(struct fifo_info));
	if(fifo == NULL)
	{
		return NULL;
	}

	fifo->buf = buf;
	fifo->size = size;
	fifo->in = 0;
	fifo->out = 0;
	fifo->lock = lock;
	fifo->unlock = unlock;

	return fifo;
}

int8 fifo_write(struct fifo_info *fifo, uint8 *w_buf, uint32 size)
{
	uint8 bLock = FALSE;
	uint32 i;

	if(fifo->lock != NULL)
	{
		if(fifo->lock())
		{
			return FIFO_ERR_LOCK;
		}

		bLock = TRUE;
	}

	for(i = 0; i < size; i++)
	{
		*(fifo->buf + (fifo->in % fifo->size)) = *(w_buf + i);
		fifo->in++;
	}

	if(bLock)
	{
		if(fifo->unlock())
		{
			return FIFO_ERR_UNLOCK;
		}
	}

	return i;
}

uint8 fifo_read(struct fifo_info *fifo, uint8 *r_buf, uint32 size)
{
	uint8 bLock = FALSE;
	uint32 i;

	if(fifo->lock != NULL)
	{
		if(fifo->lock())
		{
			return FIFO_ERR_LOCK;
		}

		bLock = TRUE;
	}

	if(fifo->out + size > fifo->in)
	{
		size = fifo->in - fifo->out;
	}

	for(i = 0; i < size; i++)
	{
		r_buf[i] = fifo->buf[fifo->out % fifo->size];
		fifo->out++;
	}

	if(bLock)
	{
		if(fifo->unlock())
		{
			return FIFO_ERR_UNLOCK;
		}
	}

	return i;
}

uint32 fifo_len(struct fifo_info *fifo)
{
	return (fifo->in - fifo->out);
}

uint8 fifo_reset(struct fifo_info *fifo)
{
	fifo->in = 0;
	fifo->out = 0;

	return FIFO_SUCCESS;
}

uint8 fifo_free(struct fifo_info *fifo)
{
	if(fifo != NULL)
	{
		free(fifo);
	}
	return FIFO_SUCCESS;
}
