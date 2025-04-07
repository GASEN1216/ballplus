// 避免变量重复声明问题
export const myPostsApp = getApp<any>();

Page({
    data: {
        posts: [] as any[],
        currentPage: 1,
        pageSize: 10,
        loading: false,
        loadAll: false,
        userId: '',
        nextCursor: ''
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
            loading: true,
            loadAll: false,
            nextCursor: null // 重置游标
        });
        this.fetchMyPosts(() => {
            wx.stopPullDownRefresh();
        });
    },

    // 加载更多帖子
    loadMorePosts() {
        if (!this.data.loadAll && !this.data.loading) {
            this.setData({
                loading: true
            });
            // 使用nextCursor加载更多
            this.fetchMyPosts();
        }
    },

    // 获取我的帖子列表
    fetchMyPosts(callback?: Function) {
        const { userId, pageSize, nextCursor } = this.data;
        const isFirstLoad = !nextCursor;

        if (!userId) {
            wx.showToast({
                title: '用户未登录',
                icon: 'none'
            });
            return;
        }

        // 构建请求参数
        const requestData: {
            userId: number;
            pageSize: number;
            cursor?: string;
            asc?: boolean;
        } = {
            userId: Number(userId),
            pageSize: pageSize,
            asc: false // 默认降序
        };

        // 如果有游标且不是首次加载，添加游标参数
        if (nextCursor && !isFirstLoad) {
            requestData.cursor = nextCursor;
        }

        wx.request({
            url: `${myPostsApp.globalData.url}/user/wx/getMyPostsWithCursor`,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': myPostsApp.globalData.currentUser.token
            },
            data: requestData,
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    const responseData = res.data.data;
                    const newPosts = responseData.records || [];
                    const hasMore = responseData.hasMore;
                    const newCursor = responseData.nextCursor;

                    // 处理时间格式和其他数据
                    const processedPosts = newPosts.map((post: any) => ({
                        ...post,
                        id: post.postId,
                        createTime: this.formatTime(post.createTime)
                    }));

                    if (isFirstLoad) {
                        this.setData({
                            posts: processedPosts,
                            loadAll: !hasMore,
                            nextCursor: newCursor
                        });
                    } else {
                        this.setData({
                            posts: [...this.data.posts, ...processedPosts],
                            loadAll: !hasMore,
                            nextCursor: newCursor
                        });
                    }

                    // 如果有回调函数，传入新数据
                    if (callback) callback(processedPosts);
                } else {
                    wx.showToast({
                        title: res.data.message || '获取失败',
                        icon: 'none'
                    });
                    // 如果有回调函数，传入空数组表示获取失败
                    if (callback) callback([]);
                }
            },
            fail: (err) => {
                console.error('网络请求失败:', err);
                wx.showToast({
                    title: '网络错误，请重试',
                    icon: 'none'
                });
                // 如果有回调函数，传入空数组表示获取失败
                if (callback) callback([]);
            },
            complete: () => {
                this.setData({ loading: false });
            }
        });
    },

    // 进入帖子详情
    goToPostDetail(e: any) {
        const postId = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `../postDetail/postDetail?id=${postId}`
        });
    },

    // 去创建帖子
    goToCreatePost() {
        wx.navigateTo({
            url: '../createPost/createPost'
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