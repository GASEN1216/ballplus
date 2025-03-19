// 避免变量重复声明问题
const myPostsApp = getApp<any>();

Page({
    data: {
        posts: [] as any[],
        currentPage: 1,
        pageSize: 10,
        loading: false,
        loadAll: false,
        userId: ''
    },

    onLoad() {
        this.setData({
            userId: myPostsApp.globalData.currentUser.id
        });
        this.fetchMyPosts();
    },

    // 刷新页面
    onPullDownRefresh() {
        this.setData({
            posts: [],
            currentPage: 1,
            loadAll: false
        });
        this.fetchMyPosts(() => {
            wx.stopPullDownRefresh();
        });
    },

    // 加载更多帖子
    loadMorePosts() {
        if (!this.data.loadAll && !this.data.loading) {
            this.setData({
                currentPage: this.data.currentPage + 1
            });
            this.fetchMyPosts();
        }
    },

    // 获取我的帖子列表
    fetchMyPosts(callback?: Function) {
        const { userId, currentPage, pageSize } = this.data;

        if (!userId) {
            wx.showToast({
                title: '用户未登录',
                icon: 'none'
            });
            return;
        }

        this.setData({ loading: true });

        wx.request({
            url: `${myPostsApp.globalData.url}/user/wx/getMyPosts`,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': myPostsApp.globalData.currentUser.token
            },
            data: {
                userId: userId,
                pageNum: currentPage,
                pageSize: pageSize
            },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    const newPosts = res.data.data.posts || [];

                    // 处理时间格式和其他数据
                    const processedPosts = newPosts.map((post: any) => ({
                        ...post,
                        // 将 postId 转换为前端使用的 id
                        id: post.postId,
                        //  
                        createTime: this.formatTime(post.createTime)
                    }));

                    if (currentPage === 1) {
                        this.setData({
                            posts: processedPosts,
                            loadAll: processedPosts.length < pageSize
                        });
                    } else {
                        this.setData({
                            posts: [...this.data.posts, ...processedPosts],
                            loadAll: processedPosts.length < pageSize
                        });
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

    // 进入帖子详情
    goToPostDetail(e: any) {
        const postId = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `/pages/postDetail/postDetail?id=${postId}`
        });
    },

    // 去创建帖子
    goToCreatePost() {
        wx.navigateTo({
            url: '/pages/createPost/createPost'
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