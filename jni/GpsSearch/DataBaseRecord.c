#include <stdio.h>
#include <pthread.h>

#include "common.h"
#include "List.h"

#define __DATABASERECORD_C__

#include "DataBaseRecord.h"
#include "DataBase.h"

int32 RecordSimpleParse(uint8 *pBuf, DataBase_Record_Format *pRecord, int32 nAddrInFile)
{
    if(pRecord == NULL || pBuf == NULL)
    {
        return RECORD_ERR_PARAMETER;
    }

    pRecord->ep_lat = LATITUDE_FROM_BUF(pBuf);
    pRecord->ep_lon = LONGITUDE_FROM_BUF(pBuf);

    if( pRecord->ep_lat > 900000 )
    {
        //make the latitude become Negative
        pRecord->ep_lat |= (0xFF << 24);
    }

    if( pRecord->ep_lon > 1800000 )
    {
        //make the longitude become Negative
        pRecord->ep_lon |= (0xFF << 24);
    }

    if( pRecord->sp_ep_lat & 0xF000 )
    {
        //if the sp_ep_lat is negative
        pRecord->sp_lat = pRecord->ep_lat - (0xFFFF - pRecord->sp_ep_lat);
    }
    else
    {
        pRecord->sp_lat = pRecord->ep_lat + pRecord->sp_ep_lat;
    }

    if( pRecord->sp_ep_lon & 0xF000 )
    {
        pRecord->sp_lon = pRecord->sp_lon - (0xFFFF - pRecord->sp_ep_lon);
    }
    else
    {
        pRecord->sp_lon = pRecord->sp_lon + pRecord->sp_ep_lon;
    }

    //check the range
    if( (pRecord->ep_lat > 900000 || pRecord->ep_lat < -90000)
        || (pRecord->ep_lon > 1800000 || pRecord->ep_lon < -1800000)
        || (pRecord->sp_lat > 900000 || pRecord->sp_lat < -90000)
        || (pRecord->sp_lon > 1800000 || pRecord->sp_lon < -1800000) )
    {
        return RECORD_ERR_EXCEED_RANGE;
    }
    
    return RECORD_SUCCESS;
}
