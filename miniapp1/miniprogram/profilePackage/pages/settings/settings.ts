import { IAppOption } from '../../../typings'

// pages/mine/mine.ts
const app = getApp<IAppOption>();

Page({
    data: {
        isLoggedin: false,
        sentRequests: [] as Array<{ ballNumber: string; status: string }>, // 已发送的好友申请
        receivedRequests: [] as Array<{ ballNumber: string; status: string }>, // 收到的好友申请

        // 通知设置
        notificationsEnabled: true,

        // 功能显示设置
        showActivityCountdown: true, // 控制活动倒计时显示
        showActivityManagement: true, // 控制活动管理显示
        showFriendManagement: true, // 控制好友管理显示
        showForumManagement: true, // 控制球坛管理显示
    },

    onLoad() {
        // 获取登录状态
        this.setData({
            isLoggedin: app.globalData.isLoggedin
        });

        // 获取存储的通知设置
        const notificationsEnabled = wx.getStorageSync('notificationsEnabled');

        // 获取功能显示设置
        const showActivityCountdown = wx.getStorageSync('showActivityCountdown');
        const showActivityManagement = wx.getStorageSync('showActivityManagement');
        const showFriendManagement = wx.getStorageSync('showFriendManagement');
        const showForumManagement = wx.getStorageSync('showForumManagement');

        this.setData({
            notificationsEnabled: notificationsEnabled !== false, // 默认开启
            showActivityCountdown: showActivityCountdown !== false, // 默认开启
            showActivityManagement: showActivityManagement !== false, // 默认开启
            showFriendManagement: showFriendManagement !== false, // 默认开启
            showForumManagement: showForumManagement !== false // 默认开启
        });
    },

    // 点击"个人信息"跳转到填写信息页面
    goToEditInfo() {
        wx.navigateTo({
            url: '../editProfile/editProfile',
        });
    },

    // 点击"添加好友"功能
    goToAddFriend() {
        wx.showModal({
            title: '添加好友',
            editable: true,
            placeholderText: '请输入好友球号',
            success: (res) => {
                if (res.confirm && res.content) {
                    const ballNumber: string = res.content;

                    // 校验输入数据是否为数字
                    if (!/^\d+$/.test(ballNumber)) {
                        wx.showToast({
                            title: '球号只能包含数字',
                            icon: 'none',
                        });
                        return;
                    }

                    // 调用后端接口发送好友申请
                    wx.request({
                        url: `${app.globalData.url}/user/wx/addFriendsRequest`,
                        method: 'POST',
                        header: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                            'X-Token': app.globalData.currentUser.token,
                        },
                        data: {
                            userId: app.globalData.currentUser.id, // 当前用户ID
                            ballNumber, // 要添加的好友球号
                        },
                        success: (response) => {
                            if (response.statusCode === 200 && response.data.success) {
                                wx.showToast({
                                    title: '好友申请已发送',
                                    icon: 'success',
                                });
                            } else {
                                console.log(response);

                                wx.showToast({
                                    title: response.data.data || '好友申请发送失败',
                                    icon: 'none',
                                });
                            }
                        },
                        fail: () => {
                            wx.showToast({
                                title: '请求失败，请检查网络',
                                icon: 'none',
                            });
                        },
                    });
                }
            },
        });
    },

    // 查看好友申请
    viewFriendRequests() {
        wx.navigateTo({
            url: '../friendRequests/friendRequests',
        });
    },

    // 切换通知设置
    toggleNotifications(e) {
        const enabled = e.detail.value;
        this.setData({ notificationsEnabled: enabled });
        wx.setStorageSync('notificationsEnabled', enabled);

        // 显示提示
        wx.showToast({
            title: enabled ? '通知已开启' : '通知已关闭',
            icon: 'success',
            duration: 1500
        });
    },

    // 切换活动倒计时显示
    toggleActivityCountdown(e) {
        const enabled = e.detail.value;
        this.setData({ showActivityCountdown: enabled });
        wx.setStorageSync('showActivityCountdown', enabled);

        // 显示提示
        wx.showToast({
            title: enabled ? '活动倒计时已显示' : '活动倒计时已隐藏',
            icon: 'success',
            duration: 1500
        });
    },

    // 切换活动管理显示
    toggleActivityManagement(e) {
        const enabled = e.detail.value;
        this.setData({ showActivityManagement: enabled });
        wx.setStorageSync('showActivityManagement', enabled);

        wx.showToast({
            title: enabled ? '活动管理已显示' : '活动管理已隐藏',
            icon: 'success',
            duration: 1500
        });
    },

    // 切换好友管理显示
    toggleFriendManagement(e) {
        const enabled = e.detail.value;
        this.setData({ showFriendManagement: enabled });
        wx.setStorageSync('showFriendManagement', enabled);

        wx.showToast({
            title: enabled ? '好友管理已显示' : '好友管理已隐藏',
            icon: 'success',
            duration: 1500
        });
    },

    // 切换球坛管理显示
    toggleForumManagement(e) {
        const enabled = e.detail.value;
        this.setData({ showForumManagement: enabled });
        wx.setStorageSync('showForumManagement', enabled);

        wx.showToast({
            title: enabled ? '球坛管理已显示' : '球坛管理已隐藏',
            icon: 'success',
            duration: 1500
        });
    },

    // 显示关于我们
    showAboutUs() {
        wx.showModal({
            title: '关于球Plus',
            content: '球Plus专为球友设计的社交平台，帮助球类运动爱好者找到伙伴，组织参与球赛活动',
            showCancel: false
        });
    },

    // 退出登录
    handleLogout() {
        wx.showModal({
            title: '确认退出',
            content: '确定要退出登录吗？',
            success: (res) => {
                if (res.confirm) {
                    // 清除登录状态
                    app.globalData.isLoggedin = false;
                    app.globalData.currentUser = {} as any;

                    // 删除本地存储的登录信息
                    wx.removeStorageSync('userInfo');
                    wx.removeStorageSync('token');

                    // 显示退出成功提示
                    wx.showToast({
                        title: '已退出登录',
                        icon: 'success'
                    });

                    // 跳转到首页
                    wx.switchTab({
                        url: '/pages/index/index'
                    });
                }
            }
        });
    }
});
