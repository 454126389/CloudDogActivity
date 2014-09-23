#ifndef __TTSCOMMONPCM_H_
#define __TTSCOMMONPCM_H_

#ifdef __TTSCOMMONPCM_C__
#define __TTSCOMMONPCM_DEC__
#else
#define __TTSCOMMONPCM_DEC__ extern
#endif

typedef struct _TTS_COMMOM_PCM
{
    const unsigned char     *pStr;
    const unsigned char     *pPcmBuf;
    unsigned int            dwStrSize;
    unsigned int            dwPCMSize;
}TTS_COMMOM_PCM;

#define COMMON_PCM_SIZE						9
__TTSCOMMONPCM_DEC__ const TTS_COMMOM_PCM common_pcm[COMMON_PCM_SIZE];

#endif
