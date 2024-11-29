// pages/index/index.ts
const app = getApp<IAppOption>();

Page({
  data: {
    isLoggedin: false,
    nickname: '',
    profilePic: '',
    tokenStatus: '',
    actPath: '../../activities',
    gdmapPath: '../../gdmap' 
  },
  onLoad() {
    if (app.globalData.isLoggedin) {
      this.setData({
        isLoggedin: app.globalData.isLoggedin,
        nickname: app.globalData.nickname,
        profilePic: app.globalData.profilePic,
      });
    } else {
      app.loginReadyCallback = () => {
        this.setData({
          isLoggedin: app.globalData.isLoggedin,
          nickname: app.globalData.nickname,
          profilePic: app.globalData.profilePic,
        });
      };
    }
  },

  // 点击按钮调用登录函数
  onLoginButtonClick() {
    app.login();
  },

  // 点击“个人信息”跳转到填写信息页面
  goToEditInfo() {
    wx.navigateTo({
      url: '/pages/editProfile/editProfile', // 跳转到编辑信息页面
    });
  },

  navigateToSettings() {
    wx.navigateTo({
      url: '/pages/settings/settings'
    });
  },

    goToActivities(e: any) {
      const tab = e.currentTarget.dataset.tab; // 获取按钮对应的标签
      wx.navigateTo({
        url: `${this.data.actPath}/pages/activities/activities?tab=${tab}`, // 跳转并传递标签参数
      });
    },

    toGdmap() {
      wx.navigateTo({
        url: `${this.data.gdmapPath}/pages/amap/amap`
      });
    },

  // 验证token方法
  onTokenButtonClick() {
    const token = app.globalData.token; // 获取全局token
    wx.request({
      url: app.getUrl('/user/test/token'), // 确保URL正确
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      data: {
        token: token
      },
      success: (response) => {
        if (response.data && typeof response.data === 'object' && 'code' in response.data) {
          if (response.data.code === 0) {
            this.setData({
              tokenStatus: 'Token有效',
            });
          } else {
            this.setData({
              tokenStatus: 'Token无效或已过期',
            });
          }
        }
      },
      fail: () => {
        this.setData({
          tokenStatus: '验证失败',
        });
      }
    });
  },
});
