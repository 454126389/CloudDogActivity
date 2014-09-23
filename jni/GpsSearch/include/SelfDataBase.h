#ifndef __SELFDATABASE_H__
#define __SELFDATABASE_H__

#ifdef __SELFDATABASE_C__
#define __SELFDATABASE_DEC__
#else
#define __SELFDATABASE_DEC__ extern
#endif

#define LAST_SEARCH_NOT_FOUND                       (-1)
#define LAST_SEARCH_RECURSIVE_OK                    (0)
#define LAST_SEARCH_RANGE_OK                        (1)
#define LAST_SEARCH_DETAIL_OK                       (2)

#define LAT_MIN                                     (0)
#define LAT_MAX                                     (1)
#define LON_MIN                                     (2)
#define LON_MAX                                     (3)

#ifndef __SELFDATABASE_C__
__SELFDATABASE_DEC__ DataBaseType *pSelfDataBase;
#endif

#endif
