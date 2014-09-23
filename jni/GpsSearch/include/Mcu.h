#ifndef __MCU_H__
#define __MCU_H__

#ifdef __MCU_C__
#define __MCU_DEC__
#else
#define __MCU_DEC__ extern
#endif

typedef enum
{
	PROTOCAL_MCU,
	PROTOCAL_RADAR,
	PROTOCAL_RF,
	PROTOCAL_BATTERY,
	MAX_SUPPORT_PROTOCAL
}PROTOCAL_TYPE;

typedef struct _PROTOCAL
{
	void (*init)(struct _PROTOCAL *protocal);
	void (*process)(struct _PROTOCAL *protocal, uint8 *buf, uint32 size);
	void (*deinit)(struct _PROTOCAL *protocal);
	int32 (*send)(PROTOCAL_TYPE prot_code, uint8 *buf, uint32 size);
	void *extra;
}MCU_PROTOCAL_INFO;

__MCU_DEC__ MCU_PROTOCAL_INFO *mcu_protocal[MAX_SUPPORT_PROTOCAL];

int8 unRegisterProtocal(PROTOCAL_TYPE code);
__MCU_DEC__ uint8 mcu_init(void);
__MCU_DEC__ void mcu_deinit(void);

#endif
