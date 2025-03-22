export const app = getApp<IAppOption>();

Page({
    data: {
        apiUrl: `${app.globalData.url}/user/wx/getUserInfoByUserId`,
        addFriendUrl: `${app.globalData.url}/user/wx/addFriendsRequest`,
        checkFriendUrl: `${app.globalData.url}/user/wx/checkFriendship`,
        userData: {},
        genderText: "",
        isSelf: false,
        isFriend: false,
        userId: 0
    },

    onLoad(options) {

        const userId = options.userId; // 从页面参数获取 userId
        if (userId) {
            // 判断是否是自己的资料页面
            const currentUser = app.globalData.currentUser as any;
            const currentUserId = currentUser?.id || 0;
            const isSelf = Number(userId) === Number(currentUserId);

            this.setData({
                userId: Number(userId),
                isSelf
            });

            this.loadUserData(Number(userId));
            // 检查用户是否登录
            if (!app.globalData.currentUser) {
                wx.redirectTo({
                    url: '../index/index'
                });
                return;
            }

            // 如果不是自己，检查是否已经是好友
            if (!isSelf) {
                this.checkFriendship(Number(userId));
            }
        } else {
            wx.showToast({
                title: "用户 ID 不存在",
                icon: "none",
            });
        }
    },

    // 页面显示时重新检查用户身份
    onShow() {
        // 检查用户是否登录
        if (!app.globalData.currentUser) {
            wx.redirectTo({
                url: '../index/index'
            });
            return;
        }

        const userId = this.data.userId;
        if (userId) {
            // 获取当前登录用户ID (可能已经在其他页面更新)
            const currentUser = app.globalData.currentUser as any;
            const currentUserId = currentUser?.id || 0;

            // 重新判断是否是自己的资料页面
            const isSelf = Number(userId) === Number(currentUserId);

            // 如果状态有变化，更新页面状态
            if (isSelf !== this.data.isSelf) {
                this.setData({ isSelf });

                // 如果不是自己，检查是否已经是好友
                if (!isSelf) {
                    this.checkFriendship(userId);
                }
            }
        }
    },

    // 检查与用户的好友关系
    checkFriendship(targetUserId: number) {
        const currentUser = app.globalData.currentUser as any;

        wx.request({
            url: this.data.checkFriendUrl,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': app.globalData.currentUser.token,
            },
            data: {
                userId: Number(currentUser?.id) || 0,
                friendId: targetUserId
            },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    // 设置好友状态
                    this.setData({
                        isFriend: res.data.data
                    });
                }
            },
            fail: () => {
                console.error('检查好友关系失败');
            }
        });
    },

    loadUserData(userId: Number) {
        wx.request({
            url: `${this.data.apiUrl}`, // 替换成你的后端接口
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            data: {
                userId: userId,
            },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    const user = res.data.data;

                    this.setData({
                        userData: user,
                        genderText: this.getGenderText(user.gender),
                    });
                } else {
                    wx.showToast({
                        title: "获取用户信息失败",
                        icon: "none",
                    });
                    console.error("获取用户信息失败:", res);
                }
            },
            fail: (err) => {
                wx.showToast({
                    title: "网络错误，请稍后重试",
                    icon: "none",
                });
                console.error("请求失败:", err);
            },
        });
    },

    getGenderText(gender: number) {
        return gender === 1 ? "男" : gender === 2 ? "女" : "未知";
    },

    // 复制球号到剪贴板
    copyBallNumber() {
        const ballNumber = (this.data.userData as any).ballNumber;

        if (!ballNumber) {
            wx.showToast({
                title: '球号为空',
                icon: 'none'
            });
            return;
        }

        wx.setClipboardData({
            data: ballNumber,
            success: () => {
                wx.showToast({
                    title: '球号已复制',
                    icon: 'success'
                });
            },
            fail: (err) => {
                wx.showToast({
                    title: '复制失败',
                    icon: 'none'
                });
                console.error('Clipboard Error:', err);
            }
        });
    },

    // 添加好友功能
    addFriend() {
        // 检查用户是否登录
        if (!app.globalData.currentUser) {
            wx.redirectTo({
                url: '../index/index'
            });
            wx.showLoading({
                title: '请先登录...',
                mask: true
            });
            return;
        }

        wx.showLoading({
            title: '发送请求中...',
            mask: true
        });

        const currentUser = app.globalData.currentUser as any;
        const userData = this.data.userData as any;

        wx.request({
            url: this.data.addFriendUrl,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': app.globalData.currentUser.token
            },
            data: {
                userId: currentUser?.id || 0,
                ballNumber: userData.ballNumber  // 使用球号添加好友
            },
            success: (res: any) => {
                wx.hideLoading();
                if (res.statusCode === 200 && res.data.code === 0) {
                    wx.showToast({
                        title: '好友请求已发送',
                        icon: 'success'
                    });
                } else {
                    // 处理各种错误情况
                    let errorMsg = '添加好友失败';
                    if (res.data && res.data.message) {
                        errorMsg = res.data.message;
                    }
                    wx.showToast({
                        title: errorMsg,
                        icon: 'none'
                    });
                }
            },
            fail: () => {
                wx.hideLoading();
                wx.showToast({
                    title: '网络错误，请重试',
                    icon: 'none'
                });
            }
        });
    },

    // 发送消息功能
    sendMessage() {
        // 检查用户是否登录
        if (!app.globalData.currentUser) {
            wx.redirectTo({
                url: '../index/index'
            });
            return;
        }

        const userData = this.data.userData as any;

        // 构建完全符合privateChat.js期望的friend对象
        const friend = {
            id: this.data.userId,
            name: userData.userAccount || '球友',
            avatar: userData.avatarUrl || ''
        };

        // 跳转到私聊页面，使用正确的参数格式
        wx.navigateTo({
            url: `/friends/pages/chat/privateChat/privateChat?to=` + JSON.stringify(friend),
            success: () => {
                console.log('跳转到聊天页面成功');
            },
            fail: (err) => {
                console.error('跳转到聊天页面失败', err);
                // 尝试备用路径
                wx.navigateTo({
                    url: `../../friends/pages/chat/privateChat/privateChat?to=` + JSON.stringify(friend),
                    success: () => {
                        console.log('使用备用路径跳转成功');
                    },
                    fail: (secondErr) => {
                        console.error('备用路径也跳转失败', secondErr);
                        wx.showToast({
                            title: '跳转失败，请重试',
                            icon: 'none'
                        });
                    }
                });
            }
        });
    },

    // 分享功能
    onShareAppMessage() {
        const userData = this.data.userData as any;
        const userName = userData.userAccount || '帅气球友';
        // 根据是否为自己的资料页提供不同的分享内容
        if (this.data.isSelf) {
            // 分享自己的资料
            return {
                title: `Hi，我是「${userName}」，一起来约球吧！`,
                path: `/pages/profile/profile?userId=${this.data.userId}`,
                imageUrl: userData.avatarUrl,
                success: function () {
                    wx.showToast({
                        title: '分享成功',
                        icon: 'success',
                        duration: 2000
                    });
                },
                fail: function () {
                    wx.showToast({
                        title: '分享失败',
                        icon: 'none',
                        duration: 2000
                    });
                }
            };
        } else {
            // 分享他人的资料
            // 如果是好友，使用更加个性化的标题
            const title = this.data.isFriend
                ? `我的球友「${userName}」有超棒的球技！！`
                : `这位球友「${userName}」打球超厉害！！`;

            return {
                title: title,
                path: `/pages/profile/profile?userId=${this.data.userId}`,
                imageUrl: userData.avatarUrl,
                success: function () {
                    wx.showToast({
                        title: '分享成功',
                        icon: 'success',
                        duration: 2000
                    });
                },
                fail: function () {
                    wx.showToast({
                        title: '分享失败',
                        icon: 'none',
                        duration: 2000
                    });
                }
            };
        }
    }
});
