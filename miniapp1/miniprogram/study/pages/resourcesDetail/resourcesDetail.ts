interface Resource {
    id: number;
    title: string;
    description: string;
    coverImage: string;
    type: string;
    content: string;
    views: number;
    likes: number;
    isFavorite?: boolean;
}

interface IAppOption {
    globalData: {
        qnurl: string;
        url: string;
        isLoggedin: boolean;
        currentUser: wxUser;
    }
}

interface wxUser {
    id: string;
    token: string;
}

const app = getApp<IAppOption>();

Page({
  data: {
    resource: {} as Resource,
    recommendations: [] as Resource[]
  },

  onLoad(options: any) {
    if (options.id) {
      this.loadResourceDetail(parseInt(options.id));
    }
  },

  // 加载资源详情
  loadResourceDetail(id: number) {
    wx.request({
      url: `${app.globalData.url}/user/wx/resources/${id}`,
      method: 'GET',
      header: {
        'X-Token': app.globalData.currentUser?.token
      },
      success: (res: any) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          const resource = res.data.data;

          // 如果用户已登录，检查是否已收藏
          if (app.globalData.isLoggedin && app.globalData.currentUser?.id) {
            this.getFavorites((favorites: number[]) => {
              resource.isFavorite = favorites.includes(resource.id);
              this.setData({ resource });
              // 加载推荐内容
              this.loadRecommendations(resource.type);
            });
          } else {
            this.setData({ resource });
            // 加载推荐内容
            this.loadRecommendations(resource.type);
          }
        } else {
          wx.showToast({
            title: res.data.message || '资源不存在',
            icon: 'none'
          });
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        }
      },
      fail: () => {
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        });
      }
    });
  },

  // 加载推荐内容
  loadRecommendations(type: string) {
    wx.request({
      url: `${app.globalData.url}/user/wx/resources/`,
      method: 'GET',
      data: {
        type,
        page: 1,
        size: 5
      },
      header: {
        'X-Token': app.globalData.currentUser?.token
      },
      success: (res: any) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          const recommendations = res.data.data.records.filter(
            (item: Resource) => item.id !== this.data.resource.id
          ).slice(0, 4);
          this.setData({ recommendations });
        }
      }
    });
  },

  // 获取收藏列表
  getFavorites(callback: (favorites: number[]) => void) {
    if (!app.globalData.isLoggedin || !app.globalData.currentUser?.id) {
      callback([]);
      return;
    }

    wx.request({
      url: `${app.globalData.url}/user/wx/resources/favorites`,
      method: 'GET',
      data: {
        userId: app.globalData.currentUser.id,
        page: 1,
        size: 100
      },
      header: {
        'X-Token': app.globalData.currentUser?.token
      },
      success: (res: any) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          callback(res.data.data.records.map((item: Resource) => item.id));
        } else {
          callback([]);
        }
      },
      fail: () => {
        callback([]);
      }
    });
  },

  // 播放视频
  playVideo() {
    if (this.data.resource.type === 'video' && this.data.resource.link) {
      this.setData({
        'resource.isPlaying': true
      });
    }
  },

  // 切换收藏状态
  toggleFavorite() {
    if (!app.globalData.isLoggedin || !app.globalData.currentUser?.id) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }

    const resource = this.data.resource;
    
    wx.request({
      url: `${app.globalData.url}/user/wx/resources/${resource.id}/favorite`,
      method: resource.isFavorite ? 'DELETE' : 'POST',
      data: {
        userId: app.globalData.currentUser.id
      },
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser?.token
      },
      success: (res: any) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          this.setData({
            'resource.isFavorite': !resource.isFavorite
          });
        } else {
          wx.showToast({
            title: res.data.message || '操作失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '操作失败',
          icon: 'none'
        });
      }
    });
  },

  openWebPage(): void {
    wx.navigateTo({
      url: `../webview/webview?url=${encodeURIComponent(this.data.resource.link)}`
    });
  },  

  // 点赞功能
  likeResource() {
    const resource = this.data.resource;
    this.setData({ 
      'resource.likes': resource.likes + 1 
    });
    wx.showToast({ 
      title: '点赞成功', 
      icon: 'success' 
    });
  },

  // 收藏功能
  favoriteResource(): void {
    this.setData({ 'resource.isFavorite': true });
    wx.showToast({ title: '收藏成功', icon: 'success' });
  },

  // 下载 PDF
  handleDownloadTip(): void {
    const downloadUrl = this.data.resource.link;
  
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
  },

  // 跳转到推荐资源
  navigateToDetail(e: any) {
    const id = e.currentTarget.dataset.id;
    wx.redirectTo({
      url: `./resourcesDetail?id=${id}`
    });
  }
});