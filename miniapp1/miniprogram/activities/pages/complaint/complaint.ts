import { IAppOption } from '../../../typings'

const app = getApp<IAppOption>();

Page({
    data: {
        eventId: null, // 活动ID
        userId: null, // 当前用户ID
        participants: [], // 活动参与者列表
        content: '', // 投诉内容
        commonReasons: ['迟到', '爽约', '不遵守规则', '言行不当', '态度恶劣', '其他问题'], // 常见投诉原因
        canSubmit: false, // 是否可以提交
    },

    onLoad(options) {
        const { eventId } = options;

        if (!eventId) {
            wx.showToast({
                title: '参数错误',
                icon: 'error',
            });
            setTimeout(() => {
                wx.navigateBack();
            }, 1500);
            return;
        }

        this.setData({
            eventId: Number(eventId),
            userId: Number(app.globalData.currentUser.id),
        });

        // 获取活动参与者
        this.fetchParticipants();
    },

    // 获取活动参与者
    fetchParticipants() {
        const { eventId, userId } = this.data;

        wx.request({
            url: `${app.globalData.url}/user/wx/complaint/participants`,
            method: 'GET',
            header: {
                'X-Token': app.globalData.currentUser.token,
            },
            data: {
                userId,
                eventId,
            },
            success: (res) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    const participants = res.data.data.map((item: any) => ({
                        ...item,
                        selected: false, // 初始化为未选中
                    }));

                    this.setData({
                        participants,
                    });
                } else {
                    wx.showToast({
                        title: '获取参与者失败',
                        icon: 'none',
                    });
                }
            },
            fail: () => {
                wx.showToast({
                    title: '网络错误',
                    icon: 'none',
                });
            },
        });
    },

    // 切换选中状态
    toggleSelect(e: any) {
        const index = e.currentTarget.dataset.index;
        const participants = [...this.data.participants];
        participants[index].selected = !participants[index].selected;

        this.setData({
            participants,
        });

        this.checkCanSubmit();
    },

    // 检查是否可以提交
    checkCanSubmit() {
        const { participants, content } = this.data;
        const hasSelectedParticipant = participants.some((item: any) => item.selected);
        const hasContent = content.trim().length >= 5;

        this.setData({
            canSubmit: hasSelectedParticipant && hasContent,
        });
    },

    // 选择常见投诉原因
    selectCommonReason(e: any) {
        const reason = e.currentTarget.dataset.reason;
        this.setData({
            content: reason,
        });

        this.checkCanSubmit();
    },

    // 输入投诉内容
    onContentInput(e: any) {
        this.setData({
            content: e.detail.value,
        });

        this.checkCanSubmit();
    },

    // 提交投诉
    submitComplaint() {
        const { eventId, userId, participants, content } = this.data;

        // 获取选中的被投诉人ID
        const selectedIds = participants
            .filter((item: any) => item.selected)
            .map((item: any) => item.userId)
            .join(',');

        if (!selectedIds) {
            wx.showToast({
                title: '请选择被投诉人',
                icon: 'none',
            });
            return;
        }

        if (content.trim().length < 5) {
            wx.showToast({
                title: '请输入详细投诉内容',
                icon: 'none',
            });
            return;
        }

        wx.showLoading({
            title: '提交中...',
        });

        wx.request({
            url: `${app.globalData.url}/user/wx/complaint/submit`,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': app.globalData.currentUser.token,
            },
            data: {
                userId,
                eventId,
                complainedIds: selectedIds,
                content,
            },
            success: (res) => {
                wx.hideLoading();

                if (res.statusCode === 200 && res.data.code === 0) {
                    wx.showToast({
                        title: '投诉提交成功',
                        icon: 'success',
                    });

                    setTimeout(() => {
                        wx.navigateBack();
                    }, 1500);
                } else {
                    wx.showModal({
                        title: '提交失败',
                        content: res.data.message || '请稍后重试',
                        showCancel: false,
                    });
                }
            },
            fail: () => {
                wx.hideLoading();
                wx.showToast({
                    title: '网络错误',
                    icon: 'none',
                });
            },
        });
    },
}); 