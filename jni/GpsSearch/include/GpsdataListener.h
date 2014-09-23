#ifndef __GPSDATLISTENER_H__
#define __GPSDATLISTENER_H__

#ifdef __GPSDATLISTENER_C__
#define __GPSDATLISTENER_DEC__
#else
#define __GPSDATLISTENER_DEC__ extern
#endif

typedef struct gps_listener
{
	uint8 (*listener)(void *arg);
	struct list_head node;
}GPS_LISTENER;

__GPSDATLISTENER_DEC__ struct list_head gps_listener_list;

static __inline void RegisterGpsdataListener(struct gps_listener *listener)
{
	list_add(&listener->node, &gps_listener_list);
}

static __inline void UnRegisterGpsdataListener(struct gps_listener *listener)
{
	list_del(&listener->node);
}

#endif
