export const app = getApp<IAppOption>();

interface Post {
    id: number;
    userId: number;
    avatar: string;
    name: string;
    grade: number;
    content: string;
    image: string;
    likes: number;
    comments: number;
    title: string;
    updateTime: string;
    updateTimeStamp?: number; // 添加时间戳字段用于排序
}

interface ApiResponse {
    code: number;
    message: string;
    data: any;
}

// 数据类型
interface IData {
    showSearchBar: boolean;
    showUpArrow: boolean;
    addButtonRotate: number;
    showPostModal: boolean;
    searchKeyword: string;
    posts: Post[];
    filteredPosts: Post[];
    newPost: {
        title: string;
        content: string;
        image: string;
    };
    isLoading: boolean;
    pageSize: number;
    currentPage: number;
    hasMore: boolean;
    totalPosts: number;
    totalPages: number;
    lastScrollTop: number;
    timer: number | null;
    recycleList: Post[];
    nextCursor: string | null; // 添加游标字段
}

Page({
    data: {
        showSearchBar: true,
        showUpArrow: false,
        addButtonRotate: 0,
        showPostModal: false,
        searchKeyword: '', // 搜索关键词
        posts: [] as Post[],         // 从后端获取的帖子列表
        filteredPosts: [] as Post[], // 筛选后的帖子列表
        newPost: {
            title: '',
            content: '',
            image: ''
        },
        timer: null as number | null,
        lastScrollTop: 0,

        // --- 新增分页状态 ---
        currentPage: 1, // 当前页码
        pageSize: 10,  // 每页数量
        totalPosts: 0, // 总帖子数
        totalPages: 0, // 总页数
        isLoading: false, // 是否正在加载
        hasMore: true,   // 是否还有更多数据
        // --- 结束新增 ---

        // --- 移除 recycleViewCtx ---
        recycleList: [] as Post[],
        // recycleViewCtx: null as any,
        // --- 结束移除 ---

        nextCursor: null // 添加游标字段
    },

    onLoad() {
        this.resetAndFetch();
    },

    // 下拉刷新
    onPullDownRefresh() {
        this.resetAndFetch();
    },

    // 从后端获取帖子数据，并转换成前端需要的格式
    fetchPosts(pageNum: number = 1, append: boolean = false, keyword: string | null = null, cursor: string | null = null) {
        // 游标分页不再需要pageNum参数，但为了兼容保留此参数
        const app = getApp<IAppOption>();

        // 如果是第一页加载（下拉刷新或初始加载），可以取消之前的请求（如果支持）
        // 标记开始加载
        this.setData({ isLoading: true });

        // --- Prepare request data with optional keyword ---
        const requestData: { pageSize: number, cursor?: string, asc?: boolean, keyword?: string } = {
            pageSize: this.data.pageSize
        };

        // 如果有游标且是加载更多操作，则使用游标
        if (cursor && append) {
            requestData.cursor = cursor;
        }

        // 默认降序排列
        requestData.asc = false;

        if (keyword && keyword.trim() !== '') { // Ensure keyword is not null or just whitespace
            requestData.keyword = keyword;
        }
        // --- End request data preparation ---

        wx.request({
            url: `${app.globalData.url}/user/wx/getPostListWithCursor`, // 使用游标分页API
            method: 'GET',
            data: requestData, // Use the prepared request data
            success: (res: WechatMiniprogram.RequestSuccessCallbackResult<ApiResponse>) => {
                if (res.data.code === 0 && res.data.data && res.data.data.records) {
                    const pageData = res.data.data; // 游标分页响应
                    const newPosts: Post[] = pageData.records.map((item: any) => ({
                        id: item.postId,
                        userId: item.appId,
                        avatar: item.avatar,
                        name: item.appName,
                        grade: item.grade,
                        content: item.content,
                        image: item.picture,
                        likes: item.likes,
                        comments: item.comments,
                        title: item.title,
                        updateTime: item.updateTime,
                        updateTimeStamp: new Date(item.updateTime).getTime()
                    }));

                    // 后端已排序，前端不需要再次排序
                    // newPosts.sort((a, b) => (b.updateTimeStamp || 0) - (a.updateTimeStamp || 0));

                    const currentPosts = append ? this.data.posts.concat(newPosts) : newPosts;

                    // --- 修改：统一计算 targetList 并使用 setData 更新 recycleList --- 
                    const targetRecycleList = append ? this.data.recycleList.concat(newPosts) : newPosts;

                    this.setData({
                        posts: currentPosts, // posts 仍然维护完整列表
                        nextCursor: pageData.nextCursor, // 存储下一页的游标
                        hasMore: pageData.hasMore, // 直接使用后端返回的hasMore
                        recycleList: targetRecycleList // 更新 recycleList
                    });
                    // --- 结束修改 ---

                    // filterPosts 内部会处理 recycleList 的 setData
                    // this.filterPosts(this.data.searchKeyword); // fetch时不主动触发filter，让用户行为触发
                } else {
                    wx.showToast({
                        title: res.data.message || '加载帖子失败',
                        icon: 'none' // 改为 none，避免一直显示错误图标
                    });
                    // 如果是加载更多失败，恢复hasMore状态可能需要考虑，或者保持false
                    if (append) {
                        this.setData({ hasMore: true }); // 假设还可以重试
                    } else {
                        this.setData({ hasMore: false }); // 第一页加载失败，认为没有数据
                    }
                }
            },
            fail: (err) => {
                wx.showToast({
                    title: '网络请求失败',
                    icon: 'error'
                });
                this.setData({ hasMore: false }); // 网络失败也认为没有更多了
                if (append) {
                    this.setData({ hasMore: true }); // 假设还可以重试
                }
            },
            complete: () => {
                this.setData({ isLoading: false }); // 请求完成（无论成功失败）
                wx.stopPullDownRefresh(); // 如果是下拉刷新触发的，停止动画
            }
        });
    },

    // 添加登录检查函数
    checkLogin() {
        if (!app.globalData.isLoggedin) {
            wx.showToast({
                title: '请先登录',
                icon: 'none'
            });
            wx.switchTab({
                url: '/pages/mine/mine'
            });
            return false;
        }
        return true;
    },

    // 修改跳转到帖子详情的方法
    goToPostDetail(e: any) {
        if (!this.checkLogin()) return;

        const postId = e.currentTarget.dataset.id;
        wx.navigateTo({
            url: `/forumPackage/pages/postDetail/postDetail?id=${postId}`
        });
    },

    // 输入搜索关键词
    onSearchInput(e: any) {
        const keyword = e.detail.value;
        this.setData({
            searchKeyword: keyword
        });
        // Remove immediate client-side filtering
        // this.filterPosts(keyword);
    },

    // 搜索框确认事件
    onSearchConfirm() {
        // Trigger fetch with the current keyword
        this.resetAndFetch(); // Pass the keyword implicitly via this.data.searchKeyword
    },

    // 修改加号按钮点击事件
    onAddClick() {
        if (!this.checkLogin()) return;

        // 跳转到独立的发帖页面
        wx.navigateTo({
            url: '/forumPackage/pages/createPost/createPost'
        });
    },

    // 点击空白区域关闭发帖弹窗 - 此方法保留以防后续需要
    onCancelPost() {
        this.setData({
            addButtonRotate: (this.data.addButtonRotate + 45) % 360,
            showPostModal: false,
            newPost: { title: '', content: '', image: '' }
        });
    },

    // 阻止点击弹窗内容区域关闭弹窗
    preventClose() {
        // 空函数，用于阻止事件冒泡
    },

    // 页面滑动事件：控制顶部搜索栏和向上返回按钮的显示
    onPageScroll(e: any) {
        // 因为页面设置了 disableScroll: true, 此事件理论上不会触发
        // recycle-view 内部滚动由其自身处理
        // 原有的 showSearchBar 和 showUpArrow 逻辑需要调整或移除
        // 可以考虑监听 recycle-view 的滚动事件，但 recycle-view 本身不直接提供 scrollTop
        // 暂时注释掉相关逻辑
        /*
        clearTimeout(this.data.timer as number);
        const currentScrollTop = e.scrollTop || e.detail.scrollTop;
        if (currentScrollTop > this.data.lastScrollTop) {
            this.setData({
                showSearchBar: false,
                showUpArrow: true
            });
            const timer = setTimeout(() => {
                this.setData({ showUpArrow: false });
            }, 3000);
            this.setData({ timer });
        } else {
            this.setData({
                showSearchBar: true,
                showUpArrow: false
            });
        }
        this.setData({ lastScrollTop: currentScrollTop });
        */
    },

    // 滚动到顶部
    onScrollToTop() {
        wx.pageScrollTo({
            scrollTop: 0,
            duration: 300
        });
    },

    // 修改预览图片的方法
    previewImage(e: any) {
        if (!this.checkLogin()) return;

        const imageUrl = e.currentTarget.dataset.url;
        wx.previewImage({
            current: imageUrl,
            urls: [imageUrl]
        });
    },

    // 预览上传的图片
    previewUploadImage(e: any) {
        const imageUrl = this.data.newPost.image;
        wx.previewImage({
            current: imageUrl, // 当前显示图片链接
            urls: [imageUrl]   // 预览的图片列表
        });
    },

    // 标题输入：额外进行截断处理，确保最多20个字符
    onTitleInput(e: any) {
        let value = e.detail.value;
        if (value.length > 20) {
            value = value.substring(0, 20);
        }
        this.setData({
            'newPost.title': value
        });
    },

    // 输入事件：更新新帖子的内容
    onContentInput(e: any) {
        this.setData({
            'newPost.content': e.detail.value
        });
    },

    // 修改提交帖子的方法
    onSubmitPost() {
        if (!this.checkLogin()) return;

        const { title, content, image } = this.data.newPost;
        if (!title || !content) {
            wx.showToast({
                title: '请填写完整信息',
                icon: 'error'
            });
            return;
        }
        // 构造请求数据，与后端 AddPost 对象字段保持一致
        const payload = {
            addId: app.globalData.currentUser.id,         // 用户ID
            appName: app.globalData.currentUser.name,       // 用户名称
            avatar: app.globalData.currentUser.avatar,      // 用户头像
            grade: app.globalData.currentUser.grade,        // 用户等级
            title: title,
            content: content,
            picture: image
        };
        wx.request({
            url: `${app.globalData.url}/user/wx/addPost`, // 替换成你的后端地址
            method: 'POST',
            data: payload,
            header: {
                'Content-Type': 'application/json',
                'X-Token': app.globalData.currentUser.token
            },
            success: (res: any) => {
                if (res.data.code === 0) {
                    wx.showToast({
                        title: '发帖成功！',
                        icon: 'success'
                    });
                    // 提交成功后，清空输入，并刷新帖子列表
                    this.setData({
                        newPost: { title: '', content: '', image: '' },
                        showPostModal: false
                    });
                    this.fetchPosts();
                } else {
                    wx.showToast({
                        title: res.data.message || '发帖失败！',
                        icon: 'error'
                    });
                }
            },
            fail: (err: any) => {
                wx.showToast({
                    title: '发帖失败！',
                    icon: 'error'
                });
            }
        });
    },

    // 修改跳转到用户信息的方法
    goToInfo(e: any) {
        const userId = e.currentTarget.dataset.userid; // 获取传递的id
        wx.navigateTo({
            url: `/profilePackage/pages/profile/profile?userId=${userId}`,
        });
    },

    // 分享给朋友
    onShareAppMessage() {
        return {
            title: '来球坛畅所欲言，分享你的热爱！',
            path: '/pages/forum/forum',
            imageUrl: this.data.posts.length > 0 && this.data.posts[0].image ? this.data.posts[0].image : '',
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
        return {
            title: '来球坛畅所欲言，分享你的热爱！',
            query: '',
            imageUrl: this.data.posts.length > 0 && this.data.posts[0].image ? this.data.posts[0].image : ''
        };
    },

    // 封装重置和加载第一页的逻辑
    resetAndFetch() {
        this.setData({
            posts: [],
            // filteredPosts: [], // Removed
            currentPage: 1,
            hasMore: true,
            isLoading: false,
            // searchKeyword: '', // Keep current keyword
            recycleList: [],
        });
        // Pass current searchKeyword from data to fetchPosts
        this.fetchPosts(1, false, this.data.searchKeyword);
    },

    // 页面上拉触底事件
    onReachBottom() {
        // 如果还有更多数据且不在加载中
        if (this.data.hasMore && !this.data.isLoading) {
            this.loadMorePosts();
        }
    },

    // 加载更多帖子
    loadMorePosts() {
        if (this.data.hasMore && !this.data.isLoading) {
            // 使用nextCursor加载下一页
            this.fetchPosts(1, true, this.data.searchKeyword, this.data.nextCursor);
        }
    },

    // recycle-view 滚动到底部时触发
    bindscrolltolower() {
        // 如果还有更多数据
        if (this.data.hasMore && !this.data.isLoading) {
            this.loadMorePosts();
        }
    }
});
