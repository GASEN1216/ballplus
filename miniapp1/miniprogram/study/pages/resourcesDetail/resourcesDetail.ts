Page({
  data: {
    resource: {} as Resource
  },

  onLoad(options: { id: string }) {
    const { id } = options;
    this.loadResourceDetails(parseInt(id));
  },

  // 加载资源详情
  loadResourceDetails(id: number): void {
    const fakeResources: Resource[] = [
      {
        id: 1,
        title: '🏀 篮球投篮技巧',
        type: 'video',
        cover: 'https://picsum.photos/1200/400?random=1',
        url: 'http://sunsetchat.top/study/basketball.mp4',
        description: '提高你的投篮命中率，观看完整教程。',
        likes: 120,
        favorites: 45
      },
      {
        id: 2,
        title: '🧘‍♀️ 瑜伽核心训练',
        type: 'video',
        cover: 'https://picsum.photos/1200/400?random=2',
        url: 'http://sunsetchat.top/study/yoga.mp4',
        description: '改善你的核心力量，适合初学者。',
        likes: 98,
        favorites: 60
      },
      {
        id: 3,
        title: '⚽ 足球基础训练',
        type: 'text',
        cover: 'https://picsum.photos/1200/400?random=3',
        url: 'https://dongqiudi.com/articles/4821323',
        description: '掌握足球基本技能，成为场上明星。',
        likes: 75,
        favorites: 30
      },
      {
        id: 4,
        title: '📄 健身计划表',
        type: 'pdf',
        cover: 'https://picsum.photos/1200/400?random=4',
        url: 'http://sunsetchat.top/study/fitness.pdf',
        description: '科学的健身计划，助你事半功倍。',
        likes: 85,
        favorites: 40
      }
    ];

    const resource = fakeResources.find((item) => item.id === id);
    if (resource) {
      this.setData({ resource });
    } else {
      console.error('未找到资源');
    }
  },

  openWebPage(): void {
    wx.navigateTo({
      url: `../webview/webview?url=${encodeURIComponent(this.data.resource.url)}`
    });
  },  

  // 点赞功能
  likeResource(): void {
    this.setData({ 'resource.likes': this.data.resource.likes + 1 });
    wx.showToast({ title: '点赞成功', icon: 'success' });
  },

  // 收藏功能
  favoriteResource(): void {
    this.setData({ 'resource.favorites': this.data.resource.favorites + 1 });
    wx.showToast({ title: '收藏成功', icon: 'success' });
  },

  // 下载 PDF
  handleDownloadTip(): void {
    const downloadUrl = this.data.resource.url;
  
    // 提示用户在浏览器中打开链接
    wx.showModal({
      title: '使用浏览器下载',
      content: '请复制链接并在手机或电脑浏览器中打开进行下载。',
      confirmText: '复制链接',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          wx.setClipboardData({
            data: downloadUrl,
            success: () => {
              wx.showToast({
                title: '链接已复制',
                icon: 'success'
              });
            },
            fail: (err) => {
              console.error('复制失败', err);
              wx.showToast({
                title: '复制失败',
                icon: 'error'
              });
            }
          });
        }
      }
    });
  }
  
});