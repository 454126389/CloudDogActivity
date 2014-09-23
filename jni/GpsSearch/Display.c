#include <stdio.h>
#include <fcntl.h>
#include <pthread.h>
#include "config.h"
#include "common.h"
#include "List.h"
#include "GpsData.h"
#include "DataBaseRecord.h"
#include "GpsdataListener.h"
#include "DataBaseRecord.h"
#include "PointType.h"
#include "PointSearch.h"
#include "AlarmProcess.h"

#define __DISPLAY_C__
#include "Display.h"

struct list_head display_list;

void DisplayInit(void)
{
	INIT_LIST_HEAD(&display_list);
}

void DisplayDeInit(void)
{
	struct list_head *pos;
	struct display_node *pnode;

	for(pos = display_list.next; pos != &display_list; )
	{
		pnode = list_entry(pos, struct display_node, node);
		pos = pos->next;
		RemoveDisplayByNode(&pnode);
	}
}

static uint8 getType(ALARM_POINT_NODE *pPoint)
{
	uint8 index = pPoint->DBRecord->voice_index;
	uint8 mode = pPoint->DBRecord->driver_mode;

	if(mode == DRIVER_MODE_PICTURE || mode == DRIVER_MODE_SINGLE_POINT_CURSOR_PICTURE || mode == DRIVER_MODE_SINGLE_POINT_NOCURSOR_PICTURE)
	{
		if(index >= 0x10 && index <= 0x12)
		{
			return POINT_TYPE_VIDEO;
		}
		else if(index >= 0xD8 && index <= 0xDF)
		{
			return POINT_TYPE_REGION;
		}
		else if((index >= 0xB3 && index < 0xB8)
				|| (index >= 0xBB && index < 0xC0))
		{
			return POINT_TYPE_RADAR;
		}

		return POINT_TYPE_CAMERA;
	}
	else if(mode == DRIVER_MODE_SAFE || mode == DRIVER_MODE_SINGLE_POINT_CURSOR_SAFE || mode == DRIVER_MODE_SINGLE_POINT_NOCURSOR_SAFE)
	{
		if(index == 0x05 || index == 0x85)
		{
			return POINT_TYPE_SCHOOL;
		}
		else if(index == 0x32)
		{
			return POINT_TYPE_GAS_STATION;
		}
		else if(index == 0x8F)
		{
			return POINT_TYPE_HOSPITAL;
		}
		else if(index == 0x20 || index == 0x25)
		{
			return POINT_TYPE_TUNNEL;
		}
		else if(index == 0x33 || index == 0x34)
		{
			return POINT_TYPE_TOLL_STATION;
		}
		else if(index == 0x35 || index == 0x36)
		{
			return POINT_TYPE_REST_STATION;
		}
		else if(index == 0x11 || index == 0x14)
		{
			return POINT_TYPE_ENTER_PORT;
		}
		else if(index == 0x10 || index == 0x48)
		{
			return POINT_TYPE_EXIT_PORT;
		}
		else if(index == 0x39)
		{
			return POINT_TYPE_CAR_PARK;
		}
		else if(index == 0x40 || index == 0x41)
		{
			return POINT_TYPE_CAR_PARK;
		}

		return POINT_TYPE_NULL;
	}
	else if(mode == DRIVER_MODE_SELF_POINT)
	{
		return POINT_TYPE_SELFPOINT;
	}

	return POINT_TYPE_NULL;
}

struct display_node *NotifyDisplayByPoint(ALARM_POINT_NODE *pPoint)
{
	struct display_node *node;
	struct display_node *pNode;
	uint8 bFound = FALSE;
	DataBase_Record_Format *pRecord = pPoint->DBRecord;

	node = malloc(sizeof(struct display_node));

	if(node == NULL)
	{
		return NULL;
	}

	if(pPoint != NULL)
	{
		node->lat = pRecord->ep_lat;
		node->lon = pRecord->ep_lon;
		node->type = getType(pPoint);
		node->distance = pPoint->cur2ep_dist;
		node->speed = pRecord->speed_limit;
		node->index = pRecord->voice_index;
	}

	list_for_each_entry(pNode, &display_list, node)
	{
		if(pNode->distance > node->distance)
		{
			bFound = TRUE;
			break;
		}
	}

	if(!bFound)
	{
		list_add_tail(&node->node, &display_list);
	}
	else
	{
		__list_add(&node->node, pNode->node.prev, &(pNode->node));
	}

	LOGD("add (%d,%d) point to show, speed %d, distance %d", pRecord->ep_lat, pRecord->ep_lon, node->speed, node->distance);
	return node;
}

uint8 RefreshDisplayByPoint(ALARM_POINT_NODE *pPoint)
{
	struct display_node *pNode = NULL;
	uint8 bFound = FALSE;
	list_for_each_entry(pNode, &display_list, node)
	{
		if(pNode->lat == pPoint->DBRecord->ep_lat && pNode->lon == pPoint->DBRecord->ep_lon)
		{
			bFound = TRUE;
			break;
		}
	}

	if(!bFound)
	{
		if((pNode = NotifyDisplayByPoint(pPoint)) == NULL)
		{
			return FALSE;
		}
	}
	else
	{
		RefreshDisplayByNode(pPoint, pNode);
		//LOGD("refresh (%d, %d) display", pPoint->DBRecord->ep_lat, pPoint->DBRecord->ep_lon);
	}

	return TRUE;
}


uint8 RemoveDisplayByPoint(ALARM_POINT_NODE *pPoint)
{
	struct display_node *pNode;
	uint8 bFound = FALSE;

	list_for_each_entry(pNode, &display_list, node)
	{
		if(pNode->lat == pPoint->DBRecord->ep_lat && pNode->lon == pPoint->DBRecord->ep_lon)
		{
			bFound = TRUE;
			break;
		}
	}

	if(bFound)
	{
		RemoveDisplayByNode(&pNode);
	}

	return 0;
}

static uint8 last_cnt = 0;

void DisplayProcess(void)
{
	struct display_node *pNode;
	uint8 buf[64];
	uint8 i = 0;

	list_for_each_entry(pNode, &display_list, node)
	{
		buf[5 * i + 1] = pNode->type;
		buf[5 * i + 2] = pNode->speed;
		buf[5 * i + 3] = pNode->distance >> 8;
		buf[5 * i + 4] = pNode->distance;
		buf[5 * i + 5] = pNode->index;
		i++;
		//LOGD("show %d speed(%d) distance(%d)", i, pNode->speed, pNode->distance);
	}

	buf[0] = i;

	if(last_cnt != 0 && i == 0)
	{
		NotifyUI(NOTIFY_UI_CODE_ALARM_DISTANCE, buf, 1);
	}
	else if(i > 0)
	{
		NotifyUI(NOTIFY_UI_CODE_ALARM_DISTANCE, buf, 5 * i + 1);
	}

	last_cnt = i;
}
