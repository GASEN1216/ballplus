export const app = getApp<IAppOption>();
interface Post {
  id: number;
  avatar: string;
  name: string;
  time: string;
  content: string;
  image: string;
  likes: number;
  comments: number;
  title: string
}
Page({
  data: {
    showSearchBar: true,
    showUpArrow: false,
    addButtonRotate: 0,
    showPostModal: false,
    searchKeyword: '', // 搜索关键词
    posts: [
      {
        id: 1,
        avatar: 'https://picsum.photos/150',
        name: 'Alice',
        grade: 5,
        time: '2023-12-01 12:00',
        content: '这是一个示例帖子内容。',
        image: 'https://picsum.photos/300/200?random=1',
        likes: 10,
        comments: 5,
        title: '帖子标题 1'
      },
      {
        id: 2,
        avatar: 'https://picsum.photos/150',
        name: 'Bob',
        grade: 15,
        time: '2023-12-02 08:30',
        content: '另一个示例帖子内容。',
        image: 'http://sunsetchat.top/test/dicnisnfiwna.png',
        likes: 10,
        comments: 5,
        title: '帖子标题 2'
      },
      {
        id: 3,
        avatar: 'https://picsum.photos/150',
        name: 'Charlie',
        grade: 25,
        time: '2023-12-03 14:50',
        content: '这是一条带有图片的帖子，图片显示了一些美丽的风景。',
        image: 'https://picsum.photos/300/200?random=2',
        likes: 10,
        comments: 5,
        title: '帖子标题 3'
      },
      {
        id: 4,
        avatar: 'https://picsum.photos/150',
        name: 'Diana',
        grade: 35,
        time: '2023-12-03 18:30',
        content: '刚刚完成了一天的工作，真是充实的一天！',
        image: 'http://sunsetchat.top/test/dicnisnfiwna.png',
        likes: 10,
        comments: 5,
        title: '帖子标题 4'
      },
      {
        id: 5,
        avatar: 'https://picsum.photos/150',
        name: 'Eve',
        grade: 45,
        time: '2023-12-04 09:00',
        content: '大家好，今天我有一个有趣的问题想跟大家讨论：你们最喜欢的编程语言是什么？',
        image: 'https://picsum.photos/300/200?random=3',
        likes: 10,
        comments: 5,
        title: '帖子标题 5'
      },
      {
        id: 6,
        avatar: 'https://picsum.photos/150',
        name: 'Frank',
        grade: 55,
        time: '2023-12-04 11:15',
        content: '这是我家小猫的照片，可爱吗？',
        image: 'https://picsum.photos/300/200?random=4',
        likes: 10,
        comments: 5,
        title: '帖子标题 6'
      },
      {
        id: 7,
        avatar: 'https://picsum.photos/150',
        name: 'Grace',
        grade: 65,
        time: '2023-12-05 16:45',
        content: '今天的天气真好，蓝天白云，忍不住拍了一张照片。',
        image: 'https://picsum.photos/300/200?random=5',
        likes: 10,
        comments: 5,
        title: '帖子标题 7'
      },
      {
        id: 8,
        avatar: 'https://picsum.photos/150',
        name: 'Henry',
        grade: 75,
        time: '2023-12-06 10:20',
        content: '有人喜欢看科幻电影吗？我最近看了一部非常棒的电影。',
        image: 'http://sunsetchat.top/test/dicnisnfiwna.png',
        likes: 10,
        comments: 5,
        title: '帖子标题 8'
      },
      {
        id: 9,
        avatar: 'https://picsum.photos/150',
        name: 'Isabella',
        grade: 85,
        time: '2023-12-06 14:30',
        content: '今天学习了一个新的编程框架，感觉挺有趣的，欢迎大家交流。',
        image: 'https://picsum.photos/300/200?random=6',
        likes: 10,
        comments: 5,
        title: '帖子标题 9'
      },
      {
        id: 10,
        avatar: 'https://picsum.photos/150',
        name: 'Jack',
        grade: 95,
        time: '2023-12-07 08:50',
        content: '这个社区真棒，我希望能认识更多志同道合的朋友！',
        image: '',
        likes: 999,
        comments: 999,
        title: '帖子标题 10'
      }
    ] as Post[],
    filteredPosts: [] as Post[], // 筛选后的帖子列表
    newPost: {
      title: '',
      content: '',
      image: ''
    },
    timer: null as number | null,
    lastScrollTop: 0
  },

  // 页面加载时初始化帖子列表
  onLoad() {
    this.setData({
      filteredPosts: this.data.posts
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

    // 动态筛选帖子
    this.filterPosts(keyword);
  },

  // 搜索框确认事件
  onSearchConfirm() {
    this.filterPosts(this.data.searchKeyword);
  },

  // 筛选帖子
  filterPosts(keyword: string) {
    if (!keyword) {
      // 搜索框为空时，显示所有帖子
      this.setData({
        filteredPosts: this.data.posts
      });
      return;
    }

    // 根据关键词筛选帖子
    const filtered = this.data.posts.filter(post =>
      post.title.includes(keyword) || // 按标题搜索
      post.name.includes(keyword) || // 按名字搜索
      post.content.includes(keyword) // 按内容搜索
    );

    this.setData({
      filteredPosts: filtered
    });
  },

  // 加号按钮点击事件
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
      newPost: { title: '', content: '', image: '' } // 清空输入内容
    });
  },

  // 防止点击弹窗内容区域关闭弹窗
  preventClose() {
    // 空函数，用于阻止事件冒泡
  },

  // 滑动页面事件
  onPageScroll(e: any) {
    clearTimeout(this.data.timer as number);

    const currentScrollTop = e.scrollTop || e.detail.scrollTop; // 当前滚动位置

    // 判断滑动方向
    if (currentScrollTop > this.data.lastScrollTop) {
      // 下滑：隐藏搜索栏，显示向上箭头
      this.setData({
        showSearchBar: false,
        showUpArrow: true
      });

      // 自动隐藏向上箭头
      const timer = setTimeout(() => {
        this.setData({ showUpArrow: false });
      }, 3000);
      this.setData({ timer });
    } else {
      // 上滑：显示搜索栏，隐藏向上箭头
      this.setData({
        showSearchBar: true,
        showUpArrow: false
      });
    }

    // 更新 lastScrollTop 值
    this.setData({ lastScrollTop: currentScrollTop });
  },

  // 滚动到顶部
  onScrollToTop() {
    wx.pageScrollTo({
      scrollTop: 0,
      duration: 300
    });
  },

  // 输入事件
  onTitleInput(e: any) {
    this.setData({
      'newPost.title': e.detail.value
    });
  },
  onContentInput(e: any) {
    this.setData({
      'newPost.content': e.detail.value
    });
  },

  // 上传图片
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

  // 提交帖子
  onSubmitPost() {
    const { title, content, image } = this.data.newPost;

    if (!title || !content) {
      wx.showToast({
        title: '请填写完整信息',
        icon: 'error'
      });
      return;
    }

    // 添加新帖子
    const newPost = {
      id: Date.now(),
      avatar: app.globalData.currentUser.avatar,
      name: app.globalData.currentUser.name,
      time: new Date().toLocaleString(),
      content,
      image,
      likes: 0,
      comments: 0,
      title
    };

    this.setData({
      addButtonRotate: (this.data.addButtonRotate + 45) % 360,
      posts: [newPost, ...this.data.posts],
      showPostModal: false,
      newPost: { title: '', content: '', image: '' }
    });

    wx.showToast({
      title: '提交成功，待审核',
      icon: 'success'
    });
  }
});
