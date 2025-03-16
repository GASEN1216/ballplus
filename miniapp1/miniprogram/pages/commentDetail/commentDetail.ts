export const app = getApp<IAppOption>();

interface SubComment {
    id: number;
    content: string;
    appName: string;
    avatar: string;
    grade: number;
    createTime: string;
    likes: number;
    isLiked: boolean;
    subCommentId: number;
}

Page({
    data: {
        likeSubCommentUrl: `${app.globalData.url}/user/wx/likeSubComment`,
        likeCommentUrl: `${app.globalData.url}/user/wx/likeComment`,
        addSubCommentUrl: `${app.globalData.url}/user/wx/addSubComment`,
        getSubCommentListUrl: `${app.globalData.url}/user/wx/getSubCommentList`,
        mainComment: {} as any,
        subComments: [] as any[],
        showReplyPopup: false,
        replyTo: '',
        replyContent: '',
        autoFocus: false,
        replyToSubCommentId: -1,
        likedSubComments: [] as number[],
    },

    onLoad(_query: any) {
        const eventChannel = this.getOpenerEventChannel();
        eventChannel.on('acceptDataFromOpenerPage', (data) => {
            this.setMainComment(data.comment);
            this.fetchSubComments();
        });
    },

    // 添加下拉刷新处理函数
    async onPullDownRefresh() {
        try {
            await this.fetchSubComments();
            wx.stopPullDownRefresh();
        } catch (error) {
            wx.stopPullDownRefresh();
            wx.showToast({
                title: '刷新失败，请重试',
                icon: 'none'
            });
        }
    },

    // 获取子评论列表
    fetchSubComments() {
        return new Promise((resolve, reject) => {
            if (!this.data.mainComment.commentId) {
                reject(new Error('评论ID不存在'));
                return;
            }

            wx.request({
                url: this.data.getSubCommentListUrl,
                method: 'POST',
                header: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Token': app.globalData.currentUser.token,
                },
                data: {
                    commentId: this.data.mainComment.commentId
                },
                success: (res: any) => {
                    if (res.statusCode === 200 && res.data.code === 0) {
                        const subComments = res.data.data || [];
                        // 处理子评论数据
                        const processedSubComments = subComments.map((subComment: any) => ({
                            ...subComment,
                            createTime: this.formatTime(subComment.createTime)
                        }));

                        this.setData({
                            subComments: processedSubComments
                        });
                        resolve(true);
                    } else {
                        reject(new Error(res.data.message || '获取子评论失败'));
                    }
                },
                fail: (error) => {
                    reject(error);
                }
            });
        });
    },

    // 设置主评论数据
    setMainComment(comment: any) {
        // 设置页面标题
        wx.setNavigationBarTitle({
            title: `${comment.appName}的评论`
        });

        this.setData({
            mainComment: comment
        });
    },

    // 点赞主评论
    likeMainComment() {
        if (this.data.mainComment.isLiked) {
            return;
        }

        wx.request({
            url: this.data.likeCommentUrl,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': app.globalData.currentUser.token
            },
            data: {
                commentId: this.data.mainComment.commentId
            },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    this.setData({
                        'mainComment.isLiked': true,
                        'mainComment.likes': (this.data.mainComment.likes || 0) + 1
                    });
                }
            }
        });
    },

    // 点赞子评论
    likeSubComment(e: any) {
        const subCommentId = parseInt(e.currentTarget.dataset.id);

        // 确保 subCommentId 存在且有效
        if (!subCommentId || isNaN(subCommentId)) {
            console.error('Invalid subCommentId:', subCommentId);
            return;
        }

        // 检查是否已经点赞过
        if (this.data.likedSubComments.includes(subCommentId)) {
            return;
        }

        wx.request({
            url: this.data.likeSubCommentUrl,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': app.globalData.currentUser.token
            },
            data: {
                subCommentId: subCommentId
            },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    const updatedSubComments = this.data.subComments.map(subComment => {
                        if (subComment.subCommentId === subCommentId) {
                            return {
                                ...subComment,
                                likes: (subComment.likes || 0) + 1,
                                isLiked: true
                            };
                        }
                        return subComment;
                    });

                    this.setData({
                        subComments: updatedSubComments,
                        likedSubComments: [...this.data.likedSubComments, subCommentId]
                    });
                }
            }
        });
    },

    // 点击"点我发评论"进入回复状态
    handleTapCommentInput() {
        this.setData({
            showReplyPopup: true,
            autoFocus: true,
            replyTo: '',
            replyContent: '',
            replyToSubCommentId: -1
        });
    },

    // 回复子评论
    replyToSubComment(e: any) {
        const replyTo = e.currentTarget.dataset.name;
        const subCommentId = e.currentTarget.dataset.id;
        this.setData({
            showReplyPopup: true,
            replyTo,
            autoFocus: true,
            replyContent: '',
            replyToSubCommentId: subCommentId
        });
    },

    // 关闭回复状态
    closeReplyPopup() {
        this.setData({
            showReplyPopup: false,
            replyTo: '',
            replyContent: '',
            autoFocus: false,
            replyToSubCommentId: -1
        });
    },

    preventBubble() {
        // 阻止事件冒泡
    },

    // 输入评论
    onReplyInput(e: any) {
        this.setData({ replyContent: e.detail.value });
    },

    onReplyBlur() {
        this.setData({ showReplyPopup: false });
    },

    onReplyFocus() {
        this.setData({ autoFocus: true });
    },

    cancelClosePopup() {
        // 阻止冒泡
    },

    // 发送回复
    sendReply() {
        const replyContent = this.data.replyContent.trim();
        if (!replyContent) {
            wx.showToast({ title: '请输入回复内容', icon: 'none' });
            return;
        }

        const user = app.globalData.currentUser;
        const subCommentData = {
            userId: user.id,
            commentId: this.data.mainComment.commentId,
            content: this.data.replyTo ? `回复 ${this.data.replyTo}：${replyContent}` : replyContent,
            appName: user.name,
            avatar: user.avatar,
            grade: user.grade
        };

        wx.request({
            url: this.data.addSubCommentUrl,
            method: 'POST',
            header: {
                'Content-Type': 'application/json',
                'X-Token': app.globalData.currentUser.token,
            },
            data: subCommentData,
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    // 发送成功后重新获取子评论列表
                    this.fetchSubComments().then(() => {
                        this.setData({
                            showReplyPopup: false,
                            replyContent: '',
                            replyToSubCommentId: -1,
                            autoFocus: false
                        });

                        wx.showToast({
                            title: '回复成功',
                            icon: 'success'
                        });
                    });
                } else {
                    wx.showToast({
                        title: res.data.message || '回复失败',
                        icon: 'none'
                    });
                }
            },
            fail: () => {
                wx.showToast({
                    title: '网络错误，请稍后重试',
                    icon: 'none'
                });
            }
        });
    },

    formatTime(timeStr: string) {
        const inputDate = new Date(timeStr);
        const now = new Date();
        const diffMs = now.getTime() - inputDate.getTime();
        const oneSecond = 1000;
        const oneMinute = 60 * oneSecond;
        const oneHour = 60 * oneMinute;
        const oneDay = 24 * oneHour;

        if (diffMs < oneMinute) {
            const seconds = Math.floor(diffMs / oneSecond);
            return seconds + "秒前";
        } else if (diffMs < oneHour) {
            const minutes = Math.floor(diffMs / oneMinute);
            return minutes + "分钟前";
        } else if (diffMs < oneDay) {
            const hours = Math.floor(diffMs / oneHour);
            return hours + "小时前";
        } else if (inputDate.getFullYear() === now.getFullYear()) {
            const month = (inputDate.getMonth() + 1).toString().padStart(2, '0');
            const day = inputDate.getDate().toString().padStart(2, '0');
            return `${month}-${day}`;
        } else {
            const year = inputDate.getFullYear();
            const month = (inputDate.getMonth() + 1).toString().padStart(2, '0');
            const day = inputDate.getDate().toString().padStart(2, '0');
            return `${year}-${month}-${day}`;
        }
    }
});
