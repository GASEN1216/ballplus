import { IAppOption, wxUser } from '../../../typings'

// pages/mine/mine.ts
const app = getApp<IAppOption>();

Page({
  data: {
    isLoggedin: false,
    tokenStatus: '',
    actPath: '../../activities',
    gdmapPath: '../../gdmap',
    friPath: '../../friends',
    pointsMPath: '../../pointsMall',
    userData: {} as wxUser,
    expWidth: '0%', // 预设值
    availableDates: [] as string[], // 存储用户标记的可用日期
  },
  onLoad() {
    const currentUser = app.globalData.currentUser;
    const width = (currentUser.exp / (currentUser.grade * 10)) * 100;
    if (app.globalData.isLoggedin) {
      this.setData({
        isLoggedin: app.globalData.isLoggedin,
        nickname: app.globalData.currentUser.name,
        profilePic: app.globalData.currentUser.avatar,
        userData: { ...currentUser },
        expWidth: `${width}%`
      });
    } else {
      app.loginReadyCallback = () => {
        this.setData({
          isLoggedin: app.globalData.isLoggedin,
          nickname: app.globalData.currentUser.name,
          profilePic: app.globalData.currentUser.avatar,
          userData: { ...currentUser },
          expWidth: `${width}%`
        });
      };
    }
  },

  // 发送通知提醒
sendNotification(message: string) {
  wx.showToast({
    title: message,
    icon: 'none',
    duration: 3000,
  });
},

// 模拟即将到来的活动提醒
checkUpcomingActivities() {
  const upcomingActivities = ['2025-01-08', '2024-07-05']; // 示例日期
  const today = new Date().toISOString().split('T')[0];

  if (upcomingActivities.includes(today)) {
    this.sendNotification('您今天有活动安排，请准时参加！');
  }
},


  // 选择日期并标记可用性
  onDateChange(e: any) {
    const selectedDate = e.detail.value;
    let updatedDates = this.data.availableDates;

    if (updatedDates.includes(selectedDate)) {
      updatedDates = updatedDates.filter(date => date !== selectedDate);
    } else {
      updatedDates.push(selectedDate);
    }

    this.setData({
      availableDates: updatedDates,
    });

    wx.showToast({
      title: '日期已更新',
      icon: 'success',
    });
  },

  // 显示用户标记的可用时间
  showAvailability() {
    wx.showModal({
      title: '您的可用时间',
      content: this.data.availableDates.length > 0 ? this.data.availableDates.join(', ') : '尚未标记任何日期',
      showCancel: false,
    });
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

    toFriends() {
      wx.navigateTo({
        url: `${this.data.friPath}/pages/conversations/conversations`
      });
    },

    // 跳转到积分商城
toPointsMall() {
  wx.navigateTo({
    url: `${this.data.pointsMPath}/pages/pointsMall/pointsMall`
  });
},


    onShow() {
      if (typeof this.getTabBar === 'function' && this.getTabBar()) {
        this.getTabBar().setData({
          selected: 0  //这个数字是当前页面在tabBar中list数组的索引
        })
      }
      if (app.globalData.isLoggedin) {
        this.setData({
          nickname: app.globalData.currentUser.name,
          profilePic: app.globalData.currentUser.avatar,
        });
      }
      this.checkUpcomingActivities(); // 检查即将到来的活动
    },
  // 验证token方法
  onTokenButtonClick() {
    const token = app.globalData.currentUser.token; // 获取全局token
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