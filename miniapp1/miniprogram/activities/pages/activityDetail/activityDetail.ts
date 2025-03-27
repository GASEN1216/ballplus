import { IAppOption } from '../../../typings'

const app = getApp<IAppOption>();

Page({
    data: {
        activity: null, // 活动数据
        userId: Number(app.globalData.currentUser.id), // 当前用户ID
        isCreator: false, // 是否为活动创建者
        isJoined: false, // 是否已参与
        feeModeText: '',
        typeText: '',
        limitsText: '',
        levelText: '',
        isPastEvent: false, // 是否是过去的活动
        isStateZero: true,  // 活动是否 state === 0
        ifNotification: true,
        showCancelModal: false,
        cancelReason: '',
        canComplaint: false, // 是否可以投诉
        complaints: [], // 投诉记录列表
        commonReasons: ['人数不足', '场地变更', '天气原因', '临时有事', '个人原因', '时间冲突'], // 常用取消原因
        eventId: null // 保存活动ID，方便后续刷新使用
    },
    // 请求订阅消息授权
    requestSubscribeMessage() {
        const { activity, userId } = this.data;
        const templateIds = [
            '0y74iVIHCLCJJeS-zFL1Q90cFJNjNqQv8TzjMw-cuIQ', // 报名成功通知
            'LVVo1OQ_oe6-hFSJ1yZtsB8odWdA4B8Qg5OdwBVVYWc', // 活动开始提醒
            'HDfFvsUk1yKMyiqouzGoigEoIQ9aGzJ7dned5iowXoU'  // 活动取消通知
        ];

        // 先检查用户是否开启了通知
        const notificationsEnabled = wx.getStorageSync('notificationsEnabled');
        if (notificationsEnabled === false) {
            console.log('用户已关闭通知设置，不请求订阅消息');
            return;
        }

        wx.requestSubscribeMessage({
            tmplIds: templateIds,
            success(res) {
                // 发送报名成功消息
                if (res[templateIds[0]] === 'accept') {
                    wx.request({
                        url: `${app.globalData.url}/user/wx/sendJoinEventNotification`,
                        method: 'POST',
                        header: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                            'X-Token': app.globalData.currentUser.token,
                        },
                        data: {
                            userId,
                            eventId: activity.id
                        },
                        success: (res) => {
                            if (res.statusCode === 200 && res.data.code === 0) {
                                console.log('参加活动通知发送成功！');
                            } else {
                                console.error('参加活动通知发送失败！');
                            }
                        },
                        fail: () => {
                            console.error('网络问题，参加活动通知发送失败！');
                        },
                    });
                }
            },
            fail(err) {
                console.error('订阅消息授权失败', err);
            }
        });
    },

    onLoad(options) {
        const { id } = options; // 获取活动ID
        this.setData({
            eventId: Number(id) // 保存活动ID，方便后续刷新使用
        });
        this.fetchActivityDetail(Number(id));
        if (app.globalData.isLoggedin) {
            this.setData({
                ifNotification: false
            });
        }

        // 检查是否可以投诉
        this.checkCanComplaint(Number(id));

        // 获取投诉列表
        this.fetchComplaints(Number(id));
    },

    // 页面显示时刷新数据
    onShow() {
        const { eventId } = this.data;
        if (eventId) {
            // 检查是否可以投诉
            this.checkCanComplaint(eventId);
            // 获取投诉列表
            this.fetchComplaints(eventId);
        }
    },

    // 下拉刷新
    onPullDownRefresh() {
        const { eventId } = this.data;
        if (eventId) {
            // 刷新活动详情
            this.fetchActivityDetail(eventId);
            // 检查是否可以投诉
            this.checkCanComplaint(eventId);
            // 获取投诉列表
            this.fetchComplaints(eventId);
        }
        // 停止下拉刷新动画
        wx.stopPullDownRefresh();
    },

    // 获取活动详情
    fetchActivityDetail(id: Number) {
        wx.request({
            url: `${app.globalData.url}/user/wx/getEventDetailById`, // 后端接口URL
            method: 'GET',
            header: {
                'X-Token': app.globalData.currentUser.token,
            },
            data: { eventId: id },
            success: (res) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    const activity = res.data.data;
                    // 判断当前用户是否是活动创建者
                    const isCreator = activity.appId === this.data.userId;
                    // 判断当前用户是否已参与活动
                    const isJoined = app.globalData.myEvents.includes(id);

                    // 处理消费模式
                    let feeModeText = '';
                    switch (activity.feeMode) {
                        case 0: feeModeText = ' 免费 🎟️'; break;
                        case 1: feeModeText = ' 大哥全包 💸'; break;
                        case 2: feeModeText = ' AA制 💰'; break;
                    }
                    // 处理类型
                    const typeMapping = ['🎮 娱乐', '🏋️ 训练', '🥊 对打', '🏆 比赛'];
                    const typeText = typeMapping[activity.type] || '未知';

                    // 处理限制
                    const limitsMapping = ['🚻 无限制', '👨 男士专场', '👩 女士专场'];
                    const limitsText = limitsMapping[activity.limits] || '未知';

                    // 处理水平
                    const levelMapping = ['🌱 小白', '🎓 初学者', '🎭 业余', '🏅 中级', '🥇 高级', '🏆 专业'];
                    const levelText = levelMapping[activity.level] || '未知';

                    // 获取今天的日期 (YYYY-MM-DD)
                    const today = new Date().toISOString().split('T')[0];

                    // 检查活动日期是否早于今天
                    const isPastEvent = activity.eventDate < today;

                    // 去掉 eventTime 的秒数
                    activity.eventTime = activity.eventTime.split(':').slice(0, 2).join(':');

                    // 去掉 eventTimee 的秒数
                    activity.eventTimee = activity.eventTimee.split(':').slice(0, 2).join(':');

                    // 检查活动状态是否是 0
                    const isStateZero = activity.state === 0;

                    this.setData({
                        activity,
                        isCreator,
                        isJoined,
                        feeModeText,
                        typeText,
                        limitsText,
                        levelText,
                        isPastEvent, // 存储过去的活动状态
                        isStateZero,
                    });
                } else {
                    wx.showToast({
                        title: '获取活动详情失败',
                        icon: 'none',
                    });
                }
            },
            fail: () => {
                wx.showToast({
                    title: '网络错误，请稍后重试',
                    icon: 'none',
                });
            },
        });
    },

    // 检查是否可以投诉
    checkCanComplaint(eventId: number) {
        wx.request({
            url: `${app.globalData.url}/user/wx/complaint/check`,
            method: 'GET',
            header: {
                'X-Token': app.globalData.currentUser.token,
            },
            data: {
                userId: this.data.userId,
                eventId: eventId,
            },
            success: (res) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    this.setData({
                        canComplaint: res.data.data
                    });
                }
            }
        });
    },

    // 跳转到投诉页面
    goToComplaint() {
        wx.navigateTo({
            url: `/activities/pages/complaint/complaint?eventId=${this.data.activity.id}`,
        });
    },

    // 我要取消（仅限创建者）
    cancelActivity() {
        if (!this.data.isCreator) return;

        // 显示自定义弹窗，并默认填入第一个常用理由
        this.setData({
            showCancelModal: true,
            cancelReason: this.data.commonReasons[0] // 默认选择第一个理由
        });
    },

    // 关闭取消活动弹窗
    closeCancelModal() {
        this.setData({
            showCancelModal: false
        });
    },

    // 输入框获得焦点时清空内容
    onCancelReasonFocus() {
        this.setData({
            cancelReason: ''
        });
    },

    // 选择常用取消原因
    selectCommonReason(e: any) {
        const index = e.currentTarget.dataset.index;
        const reason = this.data.commonReasons[index];
        this.setData({
            cancelReason: reason
        });
    },

    // 输入取消原因
    onCancelReasonInput(e: any) {
        this.setData({
            cancelReason: e.detail.value
        });
    },

    // 确认取消活动
    confirmCancelActivity() {
        const cancelReason = this.data.cancelReason.trim();

        if (!cancelReason) {
            wx.showToast({
                title: '请输入取消原因',
                icon: 'none'
            });
            return;
        }

        if (cancelReason.length > 20) {
            wx.showToast({
                title: '取消原因不能超过20个字',
                icon: 'none'
            });
            return;
        }

        // 有了取消原因，再次确认
        wx.showModal({
            title: '确认取消',
            content: '确定要取消这个活动吗？',
            success: (confirmRes) => {
                if (confirmRes.confirm) {
                    this.deleteActivity(cancelReason); // 传入取消原因
                    this.closeCancelModal(); // 关闭弹窗
                    wx.navigateBack();
                }
            }
        });
    },

    // 删除活动
    deleteActivity(cancelReason: string) {
        const { activity, userId } = this.data;

        wx.request({
            url: `${app.globalData.url}/user/wx/cancelEvent`,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': app.globalData.currentUser.token,
            },
            data: {
                eventId: activity.id,
                userId,
                cancelReason: cancelReason // 传递取消原因到后端
            },
            success: (res) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    wx.showToast({
                        title: '活动已取消',
                        icon: 'success',
                    });
                    // 查找活动 ID 的索引
                    const index = app.globalData.myEvents.indexOf(this.data.activity.id);

                    if (index !== -1) {
                        // 移除该活动 ID
                        app.globalData.myEvents.splice(index, 1);
                    } else {
                        console.log('未找到指定的活动 ID，取消活动失败');
                    }

                    wx.navigateBack(); // 返回上一页
                } else {
                    wx.showToast({
                        title: '取消活动失败',
                        icon: 'none',
                    });
                }
            },
            fail: () => {
                wx.showToast({
                    title: '网络错误，请稍后重试',
                    icon: 'none',
                });
            },
        });
    },

    // 我要退出（如果已参与活动）
    exitActivity() {
        if (!this.data.isJoined) return;

        wx.showModal({
            title: '退出活动',
            content: '确定要退出这个活动吗？',
            success: (res) => {
                if (res.confirm) {
                    this.removeParticipant(); // 退出活动
                }
            }
        });
    },

    // 退出活动
    removeParticipant() {
        const { activity, userId } = this.data;

        wx.request({
            url: `${app.globalData.url}/user/wx/quitEvent`,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': app.globalData.currentUser.token,
            },
            data: {
                eventId: activity.id,
                userId,
            },
            success: (res) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    wx.showToast({
                        title: '已退出活动',
                        icon: 'success',
                    });

                    // 查找活动 ID 的索引
                    const index = app.globalData.myEvents.indexOf(this.data.activity.id);

                    if (index !== -1) {
                        // 移除该活动 ID
                        app.globalData.myEvents.splice(index, 1);
                    } else {
                        console.log('未找到指定的活动 ID，退出活动失败');
                    }

                    wx.navigateBack(); // 返回上一页
                } else {
                    wx.showToast({
                        title: '退出失败',
                        icon: 'none',
                    });
                }
            },
            fail: () => {
                wx.showToast({
                    title: '网络错误，请稍后重试',
                    icon: 'none',
                });
            },
        });
    },

    // 我要参加（如果未参加过）
    joinActivity() {
        if (this.data.isJoined) return;

        const activity = this.data.activity;

        if (activity.totalParticipants <= activity.participants) {
            wx.showToast({
                title: '人数已满！',
                icon: 'none',
            });
            return;
        }

        // 校验性别
        if (activity.limits !== 0) { // 0 表示无限制
            if (activity.limits === 1 && app.globalData.currentUser.gender !== 2) {
                // 活动为 "男士专场"，但当前用户不是男生（2）
                wx.showToast({
                    title: '该活动仅限男士参加',
                    icon: 'none',
                });
                return;
            }
            if (activity.limits === 2 && app.globalData.currentUser.gender !== 1) {
                // 活动为 "女士专场"，但当前用户不是女生（1）
                wx.showToast({
                    title: '该活动仅限女士参加',
                    icon: 'none',
                });
                return;
            }
        }

        wx.showModal({
            title: '参加活动',
            content: '确定要参加这个活动吗？',
            success: (res) => {
                if (res.confirm) {
                    this.addParticipant(); // 参加活动

                    // 检查是否开启通知，仅在开启时请求订阅消息授权
                    const notificationsEnabled = wx.getStorageSync('notificationsEnabled');
                    if (notificationsEnabled !== false) {
                        this.requestSubscribeMessage();
                    }
                }
            }
        });
    },

    // 参加活动
    addParticipant() {
        const { activity, userId } = this.data;

        wx.request({
            url: `${app.globalData.url}/user/wx/joinEvent`,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': app.globalData.currentUser.token,
            },
            data: {
                eventId: activity.id,
                userId,
            },
            success: (res) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    wx.showToast({
                        title: '已参加活动',
                        icon: 'success',
                    });
                    // 参加了，将app.globalData.myEvents添加该活动activity.id
                    app.globalData.myEvents.push(activity.id);
                    wx.navigateBack(); // 返回上一页
                } else {
                    wx.showToast({
                        title: '参加失败',
                        icon: 'none',
                    });
                }
            },
            fail: () => {
                wx.showToast({
                    title: '网络错误，请稍后重试',
                    icon: 'none',
                });
            },
        });
    },

    goToInfo(e: any) {
        const userId = e.currentTarget.dataset.userid; // 获取传递的id
        wx.navigateTo({
            url: `/profilePackage/pages/profile/profile?userId=${userId}`,
        });
    },

    // 分享给朋友
    onShareAppMessage() {
        const activity = this.data.activity;
        if (!activity) return {
            title: '精彩活动',
            path: '/pages/index/index',
            imageUrl: ''
        };

        return {
            title: `邀请你参加『${activity.name}』`,
            path: `/activities/pages/activityDetail/activityDetail?id=${activity.id}`,
            imageUrl: activity.avatar || '',
            success: function () {
                wx.showToast({
                    title: '分享成功',
                    icon: 'success',
                    duration: 2000
                });
            }
        };
    },

    // 分享到朋友圈
    onShareTimeline() {
        const activity = this.data.activity;
        if (!activity) return {
            title: '精彩活动',
            query: '',
            imageUrl: ''
        };

        return {
            title: `邀请你参加『${activity.name}』- ${activity.eventDate} ${activity.eventTime}`,
            query: `id=${activity.id}`,
            imageUrl: activity.avatar || ''
        };
    },

    // 获取投诉列表
    fetchComplaints(eventId: number) {
        wx.request({
            url: `${app.globalData.url}/user/wx/complaint/list`,
            method: 'GET',
            header: {
                'X-Token': app.globalData.currentUser.token,
            },
            data: {
                eventId: eventId
            },
            success: (res) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    // 处理时间格式，只保留日期部分
                    const complaints = res.data.data.map((item: any) => {
                        if (item.createTime) {
                            item.createTime = item.createTime.split(' ')[0];
                        }
                        return item;
                    });

                    this.setData({
                        complaints: complaints
                    });
                } else {
                    console.error('获取投诉列表失败:', res);
                    // 设置为空数组以显示暂无记录
                    this.setData({
                        complaints: []
                    });
                }
            },
            fail: (err) => {
                console.error('请求投诉列表接口失败:', err);
                // 设置为空数组以显示暂无记录
                this.setData({
                    complaints: []
                });
            },
            complete: () => {
                // 隐藏加载提示
                wx.hideLoading();
            }
        });
    }
});
