import { IAppOption } from '../../../typings'

// pages/mine/mine.ts
const app = getApp<IAppOption>();

Page({
  data: {
    isLoggedin: false,
    sentRequests: [] as Array<{ ballNumber: string; status: string }>, // 已发送的好友申请
    receivedRequests: [] as Array<{ ballNumber: string; status: string }>, // 收到的好友申请
    
    // 主题设置
    themeOptions: ['浅色模式', '深色模式', '跟随系统'],
    themeIndex: 0,
    
    // 字体大小设置
    fontSizeOptions: ['小', '标准', '大', '特大'],
    fontSizeIndex: 1,
    
    // 通知设置
    notificationsEnabled: true,
  },

  onLoad() {
    // 获取登录状态
    this.setData({
      isLoggedin: app.globalData.isLoggedin
    });
    
    // 获取存储的主题设置
    const themeIndex = wx.getStorageSync('themeIndex') || 0;
    // 获取存储的字体大小设置
    const fontSizeIndex = wx.getStorageSync('fontSizeIndex') || 1;
    // 获取存储的通知设置
    const notificationsEnabled = wx.getStorageSync('notificationsEnabled');
    
    this.setData({
      themeIndex,
      fontSizeIndex,
      notificationsEnabled: notificationsEnabled !== false // 默认开启
    });
  },

  // 点击"个人信息"跳转到填写信息页面
  goToEditInfo() {
    wx.navigateTo({
      url: '/pages/editProfile/editProfile',
    });
  },

  // 点击"添加好友"功能
  goToAddFriend() {
    wx.showModal({
      title: '添加好友',
      editable: true,
      placeholderText: '请输入好友球号',
      success: (res) => {
        if (res.confirm && res.content) {
          const ballNumber: string = res.content;

          // 校验输入数据是否为数字
          if (!/^\d+$/.test(ballNumber)) {
            wx.showToast({
              title: '球号只能包含数字',
              icon: 'none',
            });
            return;
          }

          // 调用后端接口发送好友申请
          wx.request({
            url: `${app.globalData.url}/user/wx/addFriendsRequest`,
            method: 'POST',
            header: {
              'Content-Type': 'application/x-www-form-urlencoded',
              'X-Token': app.globalData.currentUser.token,
            },
            data: {
              userId: app.globalData.currentUser.id, // 当前用户ID
              ballNumber, // 要添加的好友球号
            },
            success: (response) => {
              if (response.statusCode === 200 && response.data.success) {
                wx.showToast({
                  title: '好友申请已发送',
                  icon: 'success',
                });
              } else {
                console.log(response);
                
                wx.showToast({
                  title: response.data.data || '好友申请发送失败',
                  icon: 'none',
                });
              }
            },
            fail: () => {
              wx.showToast({
                title: '请求失败，请检查网络',
                icon: 'none',
              });
            },
          });
        }
      },
    });
  },

  // 查看好友申请
  viewFriendRequests() {
    wx.navigateTo({
      url: '/pages/friendRequests/friendRequests',
    });
  },
  
  // 更改主题
  onThemeChange(e) {
    const themeIndex = e.detail.value;
    this.setData({ themeIndex });
    wx.setStorageSync('themeIndex', themeIndex);
    
    // 应用主题
    this.applyTheme(themeIndex);
  },
  
  // 应用主题
  applyTheme(themeIndex) {
    let theme = 'light';
    
    if (themeIndex == 1) {
      theme = 'dark';
    } else if (themeIndex == 2) {
      // 跟随系统
      const systemInfo = wx.getSystemInfoSync();
      theme = systemInfo.theme || 'light';
    }
    
    // 设置导航栏颜色
    if (theme === 'dark') {
      wx.setNavigationBarColor({
        frontColor: '#ffffff',
        backgroundColor: '#333333'
      });
    } else {
      wx.setNavigationBarColor({
        frontColor: '#000000',
        backgroundColor: '#ffffff'
      });
    }
    
    // 实际上小程序目前不支持全局主题切换，这里只是一个示例
    // 在实际应用中，可能需要更复杂的主题管理机制
  },
  
  // 更改字体大小
  onFontSizeChange(e) {
    const fontSizeIndex = e.detail.value;
    this.setData({ fontSizeIndex });
    wx.setStorageSync('fontSizeIndex', fontSizeIndex);
    
    // 在实际应用中，可能需要设置全局样式或特定页面的字体大小
  },
  
  // 切换通知设置
  toggleNotifications(e) {
    const notificationsEnabled = e.detail.value;
    this.setData({ notificationsEnabled });
    wx.setStorageSync('notificationsEnabled', notificationsEnabled);
    
    if (notificationsEnabled) {
      wx.showToast({
        title: '已开启通知',
        icon: 'success'
      });
    } else {
      wx.showToast({
        title: '已关闭通知',
        icon: 'none'
      });
    }
  },
  
  // 显示关于我们
  showAboutUs() {
    wx.showModal({
      title: '关于球Plus',
      content: '球Plus是一款专为球友设计的社交平台，旨在帮助热爱篮球、足球等球类运动的爱好者找到志同道合的伙伴，组织和参与各类球赛活动。\n\n版本: v1.0.0\n开发团队: 球Plus技术团队',
      showCancel: false
    });
  },
  
  // 退出登录
  handleLogout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 清除登录状态
          app.globalData.isLoggedin = false;
          app.globalData.currentUser = {} as any;
          
          // 删除本地存储的登录信息
          wx.removeStorageSync('userInfo');
          wx.removeStorageSync('token');
          
          // 显示退出成功提示
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          });
          
          // 跳转到首页
          wx.switchTab({
            url: '/pages/index/index'
          });
        }
      }
    });
  }
});
