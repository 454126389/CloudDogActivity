#ifndef __DISPLAY_H__
#define __DISPLAY_H__

#ifdef __DISPLAY_C__
#define __DISPLAY_DEC__
#else
#define __DISPLAY_DEC__ extern
#endif

#define POINT_TYPE_PICTURE_START		0x00
#define POINT_TYPE_PICTURE_END			0x1E
#define POINT_TYPE_SAFE_START			0x20
#define POINT_TYPE_SAFE_END				0xFF

#define POINT_TYPE_NULL					0x00
#define POINT_TYPE_CAMERA				0x01			//�����
#define POINT_TYPE_VIDEO				0x02			//¼Ӱ��
#define POINT_TYPE_REGION				0x03			//������ٵ�
#define POINT_TYPE_RADAR				0x04			//�״ﲹ����
//resver (0x05~0x1E)

#define POINT_TYPE_SELFPOINT			0x1F			//�Խ���

#define POINT_TYPE_HOSPITAL				0x20			//ҽԺ
#define POINT_TYPE_SCHOOL				0x21			//ѧУ
#define POINT_TYPE_ENTER_PORT			0x22			//������·��
#define POINT_TYPE_EXIT_PORT			0x23			//����������
#define POINT_TYPE_GAS_STATION			0x24			//����վ
#define POINT_TYPE_TOLL_STATION			0x25			//�շ�վ
#define POINT_TYPE_REST_STATION			0x26			//��Ϣվ
#define POINT_TYPE_BUS_STATION			0x27			//����վ
#define POINT_TYPE_TUNNEL				0x28			//���
#define POINT_TYPE_CAR_PARK				0x29			//ͣ����


struct display_node
{
	uint8 type;
	uint8 index;
	int32 lat;
	int32 lon;
	uint32 speed;
	uint32 distance;
	struct list_head node;
};

static inline void RemoveDisplayByNode(struct display_node **pNode)
{
	list_del(&((*pNode)->node));
	free(*pNode);
	LOGD("remove speed(%d)", (*pNode)->speed);
	*pNode = NULL;
}

static inline void RefreshDisplayByNode(ALARM_POINT_NODE *pPoint, struct display_node *pNode)
{
	pNode->distance = pPoint->cur2ep_dist;
}

__DISPLAY_DEC__ void DisplayInit(void);
__DISPLAY_DEC__ void DisplayDeInit(void);
__DISPLAY_DEC__ struct display_node *NotifyDisplayByPoint(ALARM_POINT_NODE *pPoint);
__DISPLAY_DEC__ uint8 RefreshDisplayByPoint(ALARM_POINT_NODE *pPoint);
__DISPLAY_DEC__ uint8 RemoveDisplayByPoint(ALARM_POINT_NODE *pPoint);
__DISPLAY_DEC__ void DisplayProcess(void);

#endif
