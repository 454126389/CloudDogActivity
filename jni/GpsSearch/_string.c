#include <string.h>
#include "common.h"

#define __STRING_C__
#include "_string.h"

uint32 _strlen(const uint8 *src)
{
	uint8 *p;

	p = (uint8 *)src;
	while(*(p++) != 0);

	return p - src - 1;
}

uint8 *_strstr(const uint8 *src, const uint8 *sub)
{
	uint32 n = 0;

	if(*sub)
	{
		while(*src)
		{
			for(n = 0; *(src + n) && *(src + n) == *(sub + n); )
			{
				n = n + 1;
				if(*(sub + n) == 0)
				{
					return (uint8 *)src;
				}
			}
			src++;
		}
	}
	else
	{
		return (uint8 *)src;
	}

	return NULL;
}

uint32 _strcpy(uint8 *des, const uint8 *src)
{
	const uint8 *p = src;

	while(*p)
	{
		*(des++) = *(p++);
	}

	*des = 0;
	return p - src;
}

uint8 *_strdup(const uint8 *src)
{
	uint32 len = _strlen(src);
	uint8 *p;

	p = (uint8 *)malloc(len + 1);
	_strcpy(p, src);

	return p;
}

uint8 *_strcat(const uint8 *src, const uint8 *s1)
{
	uint32 len = _strlen(src) + _strlen(s1);
	uint8 *p;

	p = (uint8 *)malloc(len + 1);
	memcpy(p, src, _strlen(src));
	_strcpy(p + _strlen(src), s1);

	return p;
}
