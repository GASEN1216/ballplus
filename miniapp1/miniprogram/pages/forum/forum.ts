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
    },

    onLoad() {
        // 强制刷新数据
        this.setData({
            posts: [],    // 清空原有数据，避免数据不一致
            filteredPosts: []
        });
        this.fetchPosts();
    },

    // 下拉刷新
    onPullDownRefresh() {
        this.onLoad();  // 重新执行 onLoad
        wx.stopPullDownRefresh();  // 停止下拉刷新动画
    },

    // 从后端获取帖子数据，并转换成前端需要的格式
    fetchPosts() {
        wx.request({
            url: `${app.globalData.url}/user/wx/getPostList`,
            method: 'GET',
            success: (res: WechatMiniprogram.RequestSuccessCallbackResult<ApiResponse>) => {
                if (res.data.code === 0 && res.data.data) {
                    const posts: Post[] = res.data.data.map((item: any) => ({
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
                        updateTimeStamp: new Date(item.updateTime).getTime() // 转换为时间戳
                    }));

                    // 按更新时间排序，最新的排在前面
                    posts.sort((a, b) => {
                        return (b.updateTimeStamp || 0) - (a.updateTimeStamp || 0);
                    });

                    this.setData({
                        posts,
                        filteredPosts: posts
                    });
                } else {
                    wx.showToast({
                        title: res.data.message || '加载帖子失败',
                        icon: 'error'
                    });
                }
            },
            fail: (err) => {
                wx.showToast({
                    title: '加载帖子失败',
                    icon: 'error'
                });
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
            url: `/pages/postDetail/postDetail?id=${postId}`
        });
    },

    // 输入搜索关键词
    onSearchInput(e: any) {
        const keyword = e.detail.value;
        this.setData({
            searchKeyword: keyword
        });
        this.filterPosts(keyword);
    },

    // 搜索框确认事件
    onSearchConfirm() {
        this.filterPosts(this.data.searchKeyword);
    },

    // 筛选帖子，根据标题、作者或内容关键词匹配
    filterPosts(keyword: string) {
        if (!keyword) {
            this.setData({
                filteredPosts: this.data.posts
            });
            return;
        }
        const filtered = this.data.posts.filter((post: Post) =>
            post.title.includes(keyword) ||
            post.name.includes(keyword) ||
            post.content.includes(keyword)
        );
        this.setData({
            filteredPosts: filtered
        });
    },

    // 修改加号按钮点击事件
    onAddClick() {
        if (!this.checkLogin()) return;

        // 跳转到独立的发帖页面
        wx.navigateTo({
            url: '/pages/createPost/createPost'
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
        if (!this.checkLogin()) return;

        const userId = e.currentTarget.dataset.userid;
        wx.navigateTo({
            url: `../profile/profile?userId=${userId}`,
        });
    },
});
