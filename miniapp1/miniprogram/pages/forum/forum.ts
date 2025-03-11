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
    lastScrollTop: 0
  },

  onLoad() {
    this.fetchPosts();
  },

   // 页面每次显示时触发
   onShow() {
    // 强制刷新数据
    this.setData({
      posts: [],    // 清空原有数据，避免数据不一致
      filteredPosts: []
    });

    this.fetchPosts();
  
  },

  // 从后端获取帖子数据，并转换成前端需要的格式
  fetchPosts() {
    wx.request({
      url: `${app.globalData.url}/user/wx/getPostList`, // 替换成你的后端地址
      method: 'GET',
      success: (res) => {
        console.log(res);
        
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
            title: item.title
          }));
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

  goToPostDetail(e: any) {
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

  // 加号按钮点击事件：打开发帖弹窗
  onAddClick() {
    this.setData({
      addButtonRotate: (this.data.addButtonRotate + 45) % 360,
      showPostModal: true
    });
  },

  // 点击空白区域关闭发帖弹窗
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

  // 输入事件：更新新帖子的标题
  onTitleInput(e: any) {
    this.setData({
      'newPost.title': e.detail.value
    });
  },

  // 输入事件：更新新帖子的内容
  onContentInput(e: any) {
    this.setData({
      'newPost.content': e.detail.value
    });
  },

  // 上传图片事件：选择图片后填充到 newPost.image
  onUploadImage() {
    wx.chooseMedia({
      count: 1,
      success: (res) => {
        this.setData({
          'newPost.image': res.tempFiles[0].tempFilePath
        });
      }
    });
  },

  // 提交帖子事件：这里仅作页面更新提示，实际提交逻辑建议调用后端接口
  onSubmitPost() {
    const { title, content, image } = this.data.newPost;
    if (!title || !content) {
      wx.showToast({
        title: '请填写完整信息',
        icon: 'error'
      });
      return;
    }
    // 提交成功后，将新帖子添加到列表中（实际业务中建议调用后端接口提交并返回结果）
    const newPost: Post = {
      id: Date.now(), // 临时生成ID，实际应由后端返回
      avatar: app.globalData.currentUser.avatar,
      name: app.globalData.currentUser.name,
      grade: app.globalData.currentUser.grade || 0,
      content,
      image,
      likes: 0,
      comments: 0,
      title
    };
    this.setData({
      addButtonRotate: (this.data.addButtonRotate + 45) % 360,
      posts: [newPost, ...this.data.posts],
      filteredPosts: [newPost, ...this.data.filteredPosts],
      showPostModal: false,
      newPost: { title: '', content: '', image: '' }
    });
    wx.showToast({
      title: '提交成功，待审核',
      icon: 'success'
    });
  }
});
