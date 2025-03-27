export const app = getApp<IAppOption>();

Page({
    data: {
        apiUrl: `${app.globalData.url}/user/wx/getPostDetail`,
        addCommentUrl: `${app.globalData.url}/user/wx/addComment`,
        likePostUrl: `${app.globalData.url}/user/wx/likePost`,
        likeCommentUrl: `${app.globalData.url}/user/wx/likeComment`,
        addSubCommentUrl: `${app.globalData.url}/user/wx/addSubComment`,
        post: {} as any,
        comments: [],
        visibleComments: [],
        showReplyPopup: false,
        replyTo: '',
        replyContent: '',
        autoFocus: false,
        pageSize: 100,
        currentPage: 1,
        isLoading: false,
        islike: false,
        selectedCommentId: -1,
        likedComments: [] as string[],
        sortType: 'hot',
        postId: '',
        targetCommentId: '',
    },

    onLoad(options) {
        const { id, commentId } = options;
        this.setData({
            postId: id,
            targetCommentId: commentId
        });
        // 加载点赞记录
        this.loadLikedRecords();
        this.fetchPostDetail().then(() => {
            // 获取数据后，如果有 commentId 参数，滚动到对应评论位置
            if (commentId) {
                this.scrollToComment(commentId);
            }
        });
    },

    onShow() {
        // 每次页面显示时重新加载点赞记录
        this.loadLikedRecords();
        this.updateLikeStatus();
    },

    // 加载点赞记录
    loadLikedRecords() {
        const likedComments = wx.getStorageSync('likedComments') || [];
        this.setData({
            likedComments: likedComments
        });
    },

    // 保存点赞记录
    saveLikedRecords() {
        wx.setStorageSync('likedComments', this.data.likedComments);
    },

    // 更新点赞状态
    updateLikeStatus() {
        // 更新帖子点赞状态
        if (this.data.post.id) {
            const isLiked = wx.getStorageSync(`likedPost_${this.data.post.id}`);
            this.setData({ islike: isLiked });
        }

        // 更新评论点赞状态
        if (this.data.comments.length) {
            const updatedComments = this.data.comments.map(comment => ({
                ...comment,
                isLiked: this.data.likedComments.includes(comment.commentId.toString())
            }));

            this.setData({
                comments: updatedComments,
                visibleComments: updatedComments.slice(0, this.data.pageSize * this.data.currentPage)
            });
        }
    },

    // 添加下拉刷新处理函数
    async onPullDownRefresh() {
        try {
            this.fetchPostDetail();
            wx.stopPullDownRefresh();
        } catch (error) {
            wx.stopPullDownRefresh();
            wx.showToast({
                title: '刷新失败，请重试',
                icon: 'none'
            });
        }
    },

    // 抽取获取帖子详情的方法
    fetchPostDetail() {
        return new Promise((resolve, reject) => {
            wx.request({
                url: `${this.data.apiUrl}`,
                method: 'POST',
                header: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Token': app.globalData.currentUser.token,
                },
                data: { postId: this.data.postId },
                success: res => {
                    if (res.statusCode === 200 && res.data.code === 0) {
                        const postDetail = res.data.data;
                        const processedComments = postDetail.commentsList.map(comment => {
                            return {
                                ...comment,
                                createTimeStamp: new Date(comment.createTime).getTime(),
                                createTime: this.formatTime(comment.createTime),
                                subComments: comment.subComments ? comment.subComments.map(subComment => ({
                                    ...subComment,
                                    createTime: this.formatTime(subComment.createTime)
                                })).sort((a, b) => (b.likes || 0) - (a.likes || 0)) : [],
                                visibleSubComments: comment.subComments ?
                                    comment.subComments
                                        .sort((a, b) => (b.likes || 0) - (a.likes || 0))
                                        .slice(0, 2)
                                        .map(subComment => ({
                                            ...subComment,
                                            createTime: this.formatTime(subComment.createTime)
                                        })) : []
                            };
                        });

                        // 根据当前排序方式排序
                        const sortedComments = this.sortComments(processedComments);

                        this.setData({
                            post: {
                                id: postDetail.id,
                                appId: postDetail.appId,
                                avatar: postDetail.avatar,
                                name: postDetail.appName,
                                grade: postDetail.grade,
                                content: postDetail.content,
                                image: postDetail.picture,
                                likes: postDetail.likes,
                                comments: postDetail.comments,
                                title: postDetail.title,
                                time: this.formatTime(postDetail.createTime),
                                updateTime: this.formatTime(postDetail.updateContentTime),
                            },
                            comments: sortedComments,
                            visibleComments: sortedComments.slice(0, this.data.pageSize)
                        });

                        // 检查帖子点赞状态
                        this.setData({
                            islike: wx.getStorageSync(`likedPost_${postDetail.id}`) || false
                        });

                        // 立即更新评论点赞状态
                        this.updateLikeStatus();

                        resolve(true);
                    } else {
                        wx.showToast({ title: res.data.message, icon: 'none' });
                        reject(new Error(res.data.message));
                    }
                },
                fail: (error) => {
                    wx.showToast({ title: '请求失败', icon: 'none' });
                    reject(error);
                }
            });
        });
    },

    // 添加评论排序方法
    sortComments(comments) {
        return [...comments].sort((a, b) => {
            if (this.data.sortType === 'hot') {
                const likeDiff = (b.likes || 0) - (a.likes || 0);
                return likeDiff !== 0 ? likeDiff : b.createTimeStamp - a.createTimeStamp;
            } else {
                return b.createTimeStamp - a.createTimeStamp;
            }
        });
    },

    formatTime(timeStr) {
        const inputDate = new Date(timeStr);
        const now = new Date();
        const diffMs = now - inputDate; // 毫秒差值
        const oneSecond = 1000;
        const oneMinute = 60 * oneSecond;
        const oneHour = 60 * oneMinute;
        const oneDay = 24 * oneHour;

        if (diffMs < oneSecond) {
            return "刚刚";
        } else if (diffMs < oneMinute) {
            // 如果一分钟内，显示"xx秒前"
            const seconds = Math.floor(diffMs / oneSecond);
            return seconds + "秒前";
        } else if (diffMs < oneHour) {
            // 如果一小时内，显示"xx分钟前"
            const minutes = Math.floor(diffMs / oneMinute);
            return minutes + "分钟前";
        } else if (diffMs < oneDay) {
            // 如果是一日内，显示"xx小时前"
            const hours = Math.floor(diffMs / (60 * 60 * 1000));
            return hours + "小时前";
        } else if (inputDate.getFullYear() === now.getFullYear()) {
            // 如果不是一天内，但是今年，显示"03-12"格式（月份和日期）
            const month = (inputDate.getMonth() + 1).toString().padStart(2, '0');
            const day = inputDate.getDate().toString().padStart(2, '0');
            return `${month}-${day}`;
        } else {
            // 如果不是今年，显示"2024-03-12"格式（年月日）
            const year = inputDate.getFullYear();
            const month = (inputDate.getMonth() + 1).toString().padStart(2, '0');
            const day = inputDate.getDate().toString().padStart(2, '0');
            return `${year}-${month}-${day}`;
        }
    },

    goToInfo(e: any) {
        const userId = e.currentTarget.dataset.userid; // 获取传递的id
        wx.navigateTo({
            url: `../profile/profile?userId=${userId}`,
        });
    },

    goToCommentDetail(e: any) {
        const commentId = e.currentTarget.dataset.id;
        const comment = e.currentTarget.dataset.comment;
        wx.navigateTo({
            url: `/pages/commentDetail/commentDetail?id=${commentId}`,
            success: function (res) {
                // 将评论数据传递给评论详情页
                res.eventChannel.emit('acceptDataFromOpenerPage', { comment: comment })
            }
        });
    },

    // 点击"点我发评论"进入回复状态
    handleTapCommentInput() {
        this.setData({
            showReplyPopup: true,
            autoFocus: true,
            replyTo: '帖子',  // 如果是回复主帖，可固定提示"帖子"，如回复评论则在 openReplyPopup 中传入对应名字
            replyContent: '',
            selectedCommentId: -1  // 新增主评论
        });

        // 延时确保输入框自动聚焦
        setTimeout(() => {
            wx.createSelectorQuery()
                .select('#replyInput')
                .fields({ properties: ['focus'] }, res => {
                    if (!res.focus) {
                        this.setData({ autoFocus: true });
                    }
                })
                .exec();
        }, 200);
    },

    // 普通状态下点击某评论的回复按钮
    openReplyPopup(e: any) {
        const replyTo = e.currentTarget.dataset.name || '帖子';
        const commentId = e.currentTarget.dataset.id;
        this.setData({
            showReplyPopup: true,
            replyTo,
            autoFocus: true,
            replyContent: '',
            selectedCommentId: commentId
        });

        setTimeout(() => {
            wx.createSelectorQuery()
                .select('#replyInput')
                .fields({ properties: ['focus'] }, res => {
                    if (!res.focus) {
                        this.setData({ autoFocus: true });
                    }
                })
                .exec();
        }, 200);
    },

    // 关闭回复状态
    closeReplyPopup() {
        this.setData({
            showReplyPopup: false,
            replyTo: '',
            replyContent: '',
            autoFocus: false,
            selectedCommentId: -1
        });
    },

    preventClose() {
        // 阻止点击事件冒泡
    },

    // 输入评论
    onReplyInput(e: any) {
        this.setData({ replyContent: e.detail.value });
    },

    onReplyBlur() {
        // 可选择失焦时做处理，此处直接不操作
        this.setData({ showReplyPopup: false });
    },

    onReplyFocus() {
        // 保证输入框自动聚焦
        this.setData({ autoFocus: true });
    },

    cancelClosePopup() {

    },

    // 发送评论或回复
    sendReply() {
        const replyContent = this.data.replyContent.trim();
        if (!replyContent) {
            wx.showToast({ title: '请输入回复内容', icon: 'none' });
            return;
        }

        let user = app.globalData.currentUser;

        if (this.data.selectedCommentId > 0) {
            // 添加子评论
            const subCommentData = {
                userId: user.id,
                commentId: this.data.selectedCommentId,
                content: replyContent,
                appName: user.name,
                avatar: user.avatar,
                grade: user.grade
            };

            wx.request({
                url: `${this.data.addSubCommentUrl}`,
                method: 'POST',
                header: {
                    'Content-Type': 'application/json',
                    'X-Token': app.globalData.currentUser.token,
                },
                data: subCommentData,
                success: (res: any) => {
                    if (res.statusCode === 200 && res.data.code === 0) {
                        // 更新评论列表中的子评论
                        const updatedComments = this.data.comments.map(comment => {
                            if (comment.commentId === this.data.selectedCommentId) {
                                const newSubComment = {
                                    ...subCommentData,
                                    createTime: '刚刚'
                                };

                                // 确保 subComments 数组存在
                                const subComments = comment.subComments || [];
                                const newSubComments = [newSubComment, ...subComments];

                                return {
                                    ...comment,
                                    subComments: newSubComments,
                                    visibleSubComments: newSubComments.slice(0, 2)
                                };
                            }
                            return comment;
                        });

                        this.setData({
                            comments: updatedComments,
                            visibleComments: updatedComments.slice(0, this.data.pageSize * this.data.currentPage),
                            showReplyPopup: false,
                            replyContent: '',
                            selectedCommentId: -1,
                            autoFocus: false
                        });

                        wx.showToast({
                            title: '回复成功',
                            icon: 'success'
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
        } else {
            // 添加主评论的逻辑保持不变
            const newComment = {
                commentId: -1,
                userId: user.id,
                avatar: user.avatar,
                appName: user.name,
                grade: user.grade,
                postId: this.data.post.id,
                content: replyContent,
                createTime: "刚刚",
                createTimeStamp: new Date().getTime(), // 添加时间戳
                subComments: [],
                visibleSubComments: []
            };

            wx.request({
                url: `${this.data.addCommentUrl}`,
                method: 'POST',
                header: {
                    'Content-Type': 'application/json',
                    'X-Token': app.globalData.currentUser.token,
                },
                data: newComment,
                success: (res) => {
                    if (res.statusCode === 200 && res.data.code === 0) {
                        newComment.commentId = res.data.data.commentId;
                        const updatedComments = [newComment, ...this.data.comments];
                        this.setData({
                            'post.comments': this.data.post.comments + 1,
                            comments: updatedComments,
                            visibleComments: updatedComments.slice(0, this.data.pageSize * this.data.currentPage),
                            showReplyPopup: false,
                            replyContent: '',
                            selectedCommentId: -1,
                            autoFocus: false
                        });

                        wx.showToast({
                            title: res.data.message || "发送成功",
                            icon: "success"
                        });
                    } else {
                        wx.showToast({
                            title: res.data.message || "发送失败",
                            icon: "none"
                        });
                    }
                },
                fail: () => {
                    wx.showToast({
                        title: "网络错误，请稍后重试",
                        icon: "none"
                    });
                }
            });
        }
    },

    preventBubble() {
        // 阻止事件冒泡
    },

    // 点赞评论
    likeComment(e: any) {
        const commentId = e.currentTarget.dataset.id;

        // 检查是否已经点赞
        if (this.data.likedComments.includes(commentId.toString())) {
            wx.showToast({
                title: '已经点赞过了',
                icon: 'none'
            });
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
                    // 更新评论列表中的点赞数和状态
                    const updatedComments = this.data.comments.map(comment => {
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
                    const newLikedComments = [...this.data.likedComments, commentId.toString()];

                    this.setData({
                        comments: updatedComments,
                        visibleComments: updatedComments.slice(0, this.data.pageSize * this.data.currentPage),
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

    // 处理帖子点赞
    handleLikePost() {
        if (this.data.islike) {
            wx.showToast({
                title: '已经点赞过了',
                icon: 'none'
            });
            return;
        }

        wx.request({
            url: this.data.likePostUrl,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': app.globalData.currentUser.token,
            },
            data: {
                postId: this.data.post.id
            },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    // 更新点赞状态
                    this.setData({
                        islike: true,
                        'post.likes': this.data.post.likes + 1
                    });

                    // 保存到本地存储
                    wx.setStorageSync(`likedPost_${this.data.post.id}`, true);

                    wx.showToast({
                        title: '点赞成功',
                        icon: 'success'
                    });
                } else {
                    wx.showToast({
                        title: res.data.message || '点赞失败',
                        icon: 'none'
                    });
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

    // 分享给朋友
    onShareAppMessage() {
        const post = this.data.post;

        return {
            title: post.title || '来看看这个有趣的帖子',
            path: `/pages/postDetail/postDetail?id=${this.data.postId}`,
            imageUrl: post.image || '', // 如果帖子有图片则使用，否则使用默认图片
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
    },

    previewImage(e: any) {
        const imageUrl = e.currentTarget.dataset.url;
        wx.previewImage({
            current: imageUrl, // 当前显示图片的链接
            urls: [imageUrl]   // 需要预览的图片链接列表
        });
    },

    // 切换排序方式
    toggleSortType() {
        const newSortType = this.data.sortType === 'hot' ? 'new' : 'hot';
        const sortedComments = [...this.data.comments].sort((a, b) => {
            if (newSortType === 'hot') {
                // 按点赞数排序，点赞数相同时按时间倒序
                const likeDiff = (b.likes || 0) - (a.likes || 0);
                return likeDiff !== 0 ? likeDiff : b.createTimeStamp - a.createTimeStamp;
            } else {
                // 按时间倒序排序
                return b.createTimeStamp - a.createTimeStamp;
            }
        });

        this.setData({
            sortType: newSortType,
            comments: sortedComments,
            visibleComments: sortedComments.slice(0, this.data.pageSize * this.data.currentPage)
        });
    },

    // 滚动到指定的评论
    scrollToComment(commentId) {
        // 给异步渲染留出时间
        setTimeout(() => {
            // 获取评论元素的位置信息
            const query = wx.createSelectorQuery();
            query.select(`#comment-${commentId}`).boundingClientRect();
            query.selectViewport().scrollOffset();
            query.exec((res) => {
                if (res && res[0]) {
                    // 滚动到评论位置
                    wx.pageScrollTo({
                        scrollTop: res[1].scrollTop + res[0].top - 100, // 偏移量，让评论显示在稍微靠上的位置
                        duration: 300
                    });

                    // 添加高亮动画
                    this.highlightComment(commentId);
                }
            });
        }, 100);
    },

    // 高亮显示评论
    highlightComment(commentId) {
        // 设置高亮状态
        const comments = this.data.comments.map(comment => {
            if (comment.commentId.toString() === commentId.toString()) {
                return {
                    ...comment,
                    isHighlighted: true
                };
            }
            return comment;
        });

        this.setData({
            comments: comments,
            visibleComments: comments.slice(0, this.data.pageSize * this.data.currentPage)
        });

        // 2秒后取消高亮
        setTimeout(() => {
            const resetComments = this.data.comments.map(comment => {
                if (comment.commentId.toString() === commentId.toString()) {
                    return {
                        ...comment,
                        isHighlighted: false
                    };
                }
                return comment;
            });

            this.setData({
                comments: resetComments,
                visibleComments: resetComments.slice(0, this.data.pageSize * this.data.currentPage)
            });
        }, 2000);
    },
});
