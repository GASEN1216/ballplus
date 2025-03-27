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
        likedComments: [] as string[],
        likedSubComments: [] as string[],
        targetSubCommentId: -1,
    },

    onLoad(query: any) {
        const { subCommentId } = query;
        
        // 先设置目标子评论ID
        this.setData({
            targetSubCommentId: subCommentId
        });
        
        // 加载点赞记录
        this.loadLikedRecords();
        
        // 获取事件通道
        const eventChannel = this.getOpenerEventChannel();
        
        // 监听数据传递事件
        eventChannel.on('acceptDataFromOpenerPage', (data: any) => {
            if (data && data.comment) {
                // 设置主评论数据
                this.setMainComment(data.comment);
                
                // 获取子评论列表
                this.fetchSubComments().then(() => {
                    if (subCommentId) {
                        // 延迟执行滚动，确保数据已经加载完成
                        setTimeout(() => {
                            this.scrollToSubComment(subCommentId);
                        }, 100);
                    }
                }).catch(error => {
                    console.error('Error fetching subComments:', error);
                });
            } else {
                console.error('Invalid data received:', data);
            }
        });
    },

    onShow() {
        this.loadLikedRecords();
        this.updateLikeStatus();
    },

    loadLikedRecords() {
        const likedComments = wx.getStorageSync('likedComments') || [];
        const likedSubComments = wx.getStorageSync('likedSubComments') || [];
        this.setData({
            likedComments: likedComments,
            likedSubComments: likedSubComments
        });
    },

    saveLikedRecords() {
        wx.setStorageSync('likedComments', this.data.likedComments);
        wx.setStorageSync('likedSubComments', this.data.likedSubComments);
    },

    updateLikeStatus() {
        if (this.data.mainComment.commentId) {
            this.setData({
                'mainComment.isLiked': this.data.likedComments.includes(this.data.mainComment.commentId.toString())
            });
        }

        if (this.data.subComments.length > 0) {
            const updatedSubComments = this.data.subComments.map(subComment => ({
                ...subComment,
                isLiked: this.data.likedSubComments.includes(subComment.subCommentId.toString())
            }));

            this.setData({
                subComments: updatedSubComments
            });
        }
    },

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
                        const processedSubComments = subComments.map((subComment: any) => ({
                            ...subComment,
                            createTime: this.formatTime(subComment.createTime),
                            isLiked: this.data.likedSubComments.includes(subComment.subCommentId.toString())
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

    setMainComment(comment: any) {
        wx.setNavigationBarTitle({
            title: `${comment.appName}的评论`
        });

        const isLiked = this.data.likedComments.includes(comment.commentId.toString());

        this.setData({
            mainComment: {
                ...comment,
                isLiked: isLiked
            }
        });
    },

    likeMainComment() {
        const commentId = this.data.mainComment.commentId;

        if (this.data.likedComments.includes(commentId.toString())) {
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
                commentId: commentId
            },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    this.setData({
                        'mainComment.isLiked': true,
                        'mainComment.likes': (this.data.mainComment.likes || 0) + 1
                    });

                    const newLikedComments = [...this.data.likedComments, commentId.toString()];
                    this.setData({
                        likedComments: newLikedComments
                    });

                    this.saveLikedRecords();
                }
            }
        });
    },

    likeSubComment(e: any) {
        const subCommentId = e.currentTarget.dataset.id;

        if (!subCommentId) {
            console.error('Invalid subCommentId:', subCommentId);
            return;
        }

        if (this.data.likedSubComments.includes(subCommentId.toString())) {
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
                        if (subComment.subCommentId.toString() === subCommentId.toString()) {
                            return {
                                ...subComment,
                                likes: (subComment.likes || 0) + 1,
                                isLiked: true
                            };
                        }
                        return subComment;
                    });

                    const newLikedSubComments = [...this.data.likedSubComments, subCommentId.toString()];

                    this.setData({
                        subComments: updatedSubComments,
                        likedSubComments: newLikedSubComments
                    });

                    this.saveLikedRecords();
                }
            }
        });
    },

    handleTapCommentInput() {
        this.setData({
            showReplyPopup: true,
            autoFocus: true,
            replyTo: '',
            replyContent: '',
            replyToSubCommentId: -1
        });
    },

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
    },

    scrollToSubComment(subCommentId: string | number) {

        
        // 延迟执行滚动，确保DOM已经更新
        setTimeout(() => {
            const query = wx.createSelectorQuery();
            query.select(`#sub-comment-${subCommentId}`).boundingClientRect();
            query.selectViewport().scrollOffset();
            query.exec((res) => {
                if (res && res[0] && res[1]) {
                    const scrollTop = res[1].scrollTop + res[0].top - 100;
                    wx.pageScrollTo({
                        scrollTop: scrollTop,
                        duration: 300
                    });

                    this.highlightSubComment(subCommentId);
                } else {
                    console.log('Element not found or query failed');
                }
            });
        }, 100); 
    },

    highlightSubComment(subCommentId: string | number) {
        const subComments = this.data.subComments.map(subComment => {
            if (subComment.subCommentId.toString() === subCommentId.toString()) {
                return {
                    ...subComment,
                    isHighlighted: true
                };
            }
            return subComment;
        });
        
        this.setData({
            subComments: subComments
        });
        
        // 2秒后取消高亮
        setTimeout(() => {
            const resetSubComments = this.data.subComments.map(subComment => {
                if (subComment.subCommentId.toString() === subCommentId.toString()) {
                    return {
                        ...subComment,
                        isHighlighted: false
                    };
                }
                return subComment;
            });
            
            this.setData({
                subComments: resetSubComments
            });
        }, 2000);
    }
});
