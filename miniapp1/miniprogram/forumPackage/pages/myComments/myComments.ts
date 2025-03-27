// 避免变量重复声明问题
export const myCommentsApp = getApp<any>();

Page({
    data: {
        activeTab: 'post', // 'post' 或 'comment'
        postComments: [] as any[], // 帖子评论
        commentReplies: [] as any[], // 评论回复
        currentPage: 1,
        pageSize: 10,
        loading: false,
        loadAll: false,
        userId: -1,
        likedComments: [] as string[], // 记录已点赞的评论
        likedSubComments: [] as string[] // 记录已点赞的子评论
    },

    onLoad() {
        this.setData({
            userId: Number(myCommentsApp.globalData.currentUser.id)
        });
        // 从本地存储加载点赞记录
        this.loadLikedRecords();
        this.fetchMyComments();
    },

    onShow() {
        // 每次页面显示时重新加载点赞记录
        this.loadLikedRecords();
        this.updateLikeStatus();
    },

    // 加载点赞记录
    loadLikedRecords() {
        const likedComments = wx.getStorageSync('likedComments') || [];
        const likedSubComments = wx.getStorageSync('likedSubComments') || [];
        this.setData({
            likedComments: likedComments,
            likedSubComments: likedSubComments
        });
    },

    // 保存点赞记录 - 全局共享存储
    saveLikedRecords() {
        wx.setStorageSync('likedComments', this.data.likedComments);
        wx.setStorageSync('likedSubComments', this.data.likedSubComments);
    },

    // 更新数据中的点赞状态
    updateLikeStatus() {
        if (!this.data.postComments.length && !this.data.commentReplies.length) {
            return;
        }

        // 更新帖子评论的点赞状态
        const updatedPostComments = this.data.postComments.map(comment => {
            return {
                ...comment,
                isLiked: this.data.likedComments.includes(comment.commentId)
            };
        });

        // 更新评论回复的点赞状态
        const updatedCommentReplies = this.data.commentReplies.map(reply => {
            return {
                ...reply,
                isLiked: this.data.likedSubComments.includes(reply.subCommentId)
            };
        });

        this.setData({
            postComments: updatedPostComments,
            commentReplies: updatedCommentReplies
        });
    },

    // 切换选项卡
    switchTab(e: any) {
        const tabType = e.currentTarget.dataset.type;
        if (this.data.activeTab !== tabType) {
            this.setData({
                activeTab: tabType,
                currentPage: 1,
                loadAll: false
            });

            // 如果当前选项卡没有数据，则加载
            if ((tabType === 'post' && this.data.postComments.length === 0) ||
                (tabType === 'comment' && this.data.commentReplies.length === 0)) {
                this.fetchMyComments();
            }
        }
    },

    // 刷新页面
    onPullDownRefresh() {
        this.setData({
            currentPage: 1,
            loadAll: false
        });

        if (this.data.activeTab === 'post') {
            this.setData({ postComments: [] });
        } else {
            this.setData({ commentReplies: [] });
        }

        this.fetchMyComments(() => {
            wx.stopPullDownRefresh();
        });
    },

    // 加载更多评论
    loadMoreComments() {
        if (!this.data.loadAll && !this.data.loading) {
            this.setData({
                currentPage: this.data.currentPage + 1
            });
            this.fetchMyComments();
        }
    },

    // 获取我的评论列表
    fetchMyComments(callback?: Function) {
        const { userId, currentPage, pageSize, activeTab } = this.data;

        if (!userId) {
            wx.showToast({
                title: '用户未登录',
                icon: 'none'
            });
            return;
        }

        this.setData({ loading: true });

        // API 路径根据选项卡类型不同
        const apiUrl = activeTab === 'post'
            ? `${myCommentsApp.globalData.url}/user/wx/getMyPostComments`
            : `${myCommentsApp.globalData.url}/user/wx/getMyCommentReplies`;

        wx.request({
            url: apiUrl,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': myCommentsApp.globalData.currentUser.token
            },
            data: {
                userId: userId,
                pageNum: currentPage,
                pageSize: pageSize
            },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    // 根据不同的Tab获取不同的数据字段
                    const newComments = activeTab === 'post'
                        ? res.data.data.comments || []
                        : res.data.data.replies || [];

                    // 处理时间格式和其他数据
                    const processedComments = newComments.map((comment: any) => {
                        const processed = {
                            ...comment,
                            createTime: this.formatTime(comment.createTime),
                            // 添加点赞状态
                            isLiked: activeTab === 'post' 
                                ? this.data.likedComments.includes(comment.commentId)
                                : this.data.likedSubComments.includes(comment.subCommentId)
                        };

                        // 如果是评论回复，添加 commentId 字段用于导航
                        if (activeTab === 'comment' && comment.subCommentId) {
                            processed.commentId = comment.commentId || 0;
                        }

                        return processed;
                    });

                    if (activeTab === 'post') {
                        if (currentPage === 1) {
                            this.setData({
                                postComments: processedComments,
                                loadAll: processedComments.length < pageSize
                            });
                        } else {
                            this.setData({
                                postComments: [...this.data.postComments, ...processedComments],
                                loadAll: processedComments.length < pageSize
                            });
                        }
                    } else {
                        if (currentPage === 1) {
                            this.setData({
                                commentReplies: processedComments,
                                loadAll: processedComments.length < pageSize
                            });
                        } else {
                            this.setData({
                                commentReplies: [...this.data.commentReplies, ...processedComments],
                                loadAll: processedComments.length < pageSize
                            });
                        }
                    }
                } else {
                    wx.showToast({
                        title: res.data.message || '获取失败',
                        icon: 'none'
                    });
                }
            },
            fail: (err) => {
                console.error('网络请求失败:', err);
                wx.showToast({
                    title: '网络错误，请重试',
                    icon: 'none'
                });
            },
            complete: () => {
                this.setData({ loading: false });
                if (callback) callback();
            }
        });
    },

    // 跳转到帖子评论
    goToPostComment(e: any) {
        const postId = e.currentTarget.dataset.postId;
        const commentId = e.currentTarget.dataset.commentId;

        // 跳转到帖子详情页并定位到评论位置
        wx.navigateTo({
            url: `/pages/postDetail/postDetail?id=${postId}&commentId=${commentId}`
        });
    },

    // 跳转到帖子详情
    goToPostDetail(e: any) {
        const postId = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `/pages/postDetail/postDetail?id=${postId}`
        });
    },

    // 跳转到评论详情
    goToCommentDetail(e: any) {
        const commentId = e.currentTarget.dataset.commentId;
        const subCommentId = e.currentTarget.dataset.subCommentId;

        // 根据 commentId 获取评论详情，然后跳转
        wx.request({
            url: `${myCommentsApp.globalData.url}/user/wx/getCommentDetail`,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': myCommentsApp.globalData.currentUser.token,
            },
            data: { commentId: commentId },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    const comment = res.data.data;
                    
                    // 格式化评论数据中的时间
                    const formattedComment = {
                        ...comment,
                        createTime: this.formatTime(comment.createTime),
                        subComments: comment.subComments ? comment.subComments.map(subComment => ({
                            ...subComment,
                            createTime: this.formatTime(subComment.createTime)
                        })) : []
                    };

                    // 跳转到评论详情页
                    wx.navigateTo({
                        url: `/pages/commentDetail/commentDetail?subCommentId=${subCommentId}`,
                        success: function (res) {
                            // 将格式化后的评论数据传递给评论详情页
                            res.eventChannel.emit('acceptDataFromOpenerPage', { comment: formattedComment });
                        }
                    });
                } else {
                    wx.showToast({
                        title: res.data.message || '获取评论详情失败',
                        icon: 'none'
                    });
                }
            },
            fail: (err) => {
                console.error('网络请求失败:', err);
                wx.showToast({
                    title: '网络错误，请重试',
                    icon: 'none'
                });
            }
        });
    },

    // 查看原评论
    viewOriginalComment(e: any) {
        const commentId = e.currentTarget.dataset.commentId;
        
        // 直接查看评论详情
        this.goToCommentDetail(e);
    },

    // 点赞评论
    likeComment(e: any) {
        const commentId = e.currentTarget.dataset.id;
        
        // 检查是否已经点赞
        if (this.data.likedComments.includes(commentId)) {
            wx.showToast({
                title: '已经点赞过了',
                icon: 'none'
            });
            return;
        }

        wx.request({
            url: `${myCommentsApp.globalData.url}/user/wx/likeComment`,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': myCommentsApp.globalData.currentUser.token
            },
            data: {
                commentId: commentId
            },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    // 更新评论点赞数
                    const updatedComments = this.data.postComments.map(comment => {
                        if (comment.commentId === commentId) {
                            return {
                                ...comment,
                                likes: (comment.likes || 0) + 1,
                                isLiked: true
                            };
                        }
                        return comment;
                    });
                    
                    // 更新点赞记录
                    const newLikedComments = [...this.data.likedComments, commentId];
                    
                    this.setData({
                        postComments: updatedComments,
                        likedComments: newLikedComments
                    });
                    
                    // 保存到本地存储
                    this.saveLikedRecords();
                    
                    wx.showToast({
                        title: '点赞成功',
                        icon: 'success'
                    });
                }
            }
        });
    },

    // 点赞子评论
    likeSubComment(e: any) {
        const subCommentId = e.currentTarget.dataset.id;
        
        // 检查是否已经点赞
        if (this.data.likedSubComments.includes(subCommentId)) {
            wx.showToast({
                title: '已经点赞过了',
                icon: 'none'
            });
            return;
        }

        wx.request({
            url: `${myCommentsApp.globalData.url}/user/wx/likeSubComment`,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': myCommentsApp.globalData.currentUser.token
            },
            data: {
                subCommentId: subCommentId
            },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    // 更新子评论点赞数
                    const updatedReplies = this.data.commentReplies.map(reply => {
                        if (reply.subCommentId === subCommentId) {
                            return {
                                ...reply,
                                likes: (reply.likes || 0) + 1,
                                isLiked: true
                            };
                        }
                        return reply;
                    });
                    
                    // 更新点赞记录
                    const newLikedSubComments = [...this.data.likedSubComments, subCommentId];
                    
                    this.setData({
                        commentReplies: updatedReplies,
                        likedSubComments: newLikedSubComments
                    });
                    
                    // 保存到本地存储
                    this.saveLikedRecords();
                    
                    wx.showToast({
                        title: '点赞成功',
                        icon: 'success'
                    });
                }
            }
        });
    },

    // 格式化时间
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