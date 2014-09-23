#ifndef __STRING_H__
#define __STRING_H__

#ifdef __STRING_C__
#define __STRING_DEC__
#else
#define __STRING_DEC__ extern
#endif

__STRING_DEC__ uint32 _strlen(const uint8 *src);
__STRING_DEC__ uint8 *_strstr(const uint8 *src, const uint8 *sub);
__STRING_DEC__ uint32 _strcpy(uint8 *des, const uint8 *src);
__STRING_DEC__ uint8 *_strdup(const uint8 *src);
__STRING_DEC__ uint8 *_strcat(const uint8 *src, const uint8 *s1);
#endif
