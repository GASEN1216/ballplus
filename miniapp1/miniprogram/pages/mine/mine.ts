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
    toEventDetailsPath: "../../activities",
    pointsMPath: '../../pointsMall',
    userData: {} as wxUser,
    expWidth: '0%', // 预设值
    // 后端接口相关
    apiUrl: `${app.globalData.url}/user/wx/getNearestEvent`,
    // 活动数据
    activities: [] as any[], // 所有活动数据
    
    // 显示控制
    showActivityList: false // 控制活动列表的折叠/展开
  },
  
  // 折叠/展开活动列表
  toggleActivityList() {
    this.setData({
      showActivityList: !this.data.showActivityList
    });
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
      this.fetchActivities();
    } else {
      app.loginReadyCallback = () => {
        this.setData({
          isLoggedin: app.globalData.isLoggedin,
          nickname: app.globalData.currentUser.name,
          profilePic: app.globalData.currentUser.avatar,
          userData: { ...currentUser },
          expWidth: `${width}%`
        });
        this.fetchActivities();
      };
    }
  },
  
  // 获取活动数据
  fetchActivities() {
    const { apiUrl } = this.data;

    wx.request({
      url: `${apiUrl}`,
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser.token,
      },
      data: {
        userId: app.globalData.currentUser.id,
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {

          let newActivities = res.data.data;

          if (!newActivities || newActivities.length === 0) {
            return; // 停止后续处理
          }

          // 格式化日期和时间
          newActivities = newActivities.map((activity: any) => {
            const eventLatitude = activity.latitude; // 假设活动地点的纬度字段为 latitude
            const eventLongitude = activity.longitude; // 假设活动地点的经度字段为 longitude

            // 计算活动与用户之间的距离
            const distance = app.cD(eventLatitude, eventLongitude);

            const eventDate = new Date(activity.eventDate);
            const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
            const weekDay = weekDays[eventDate.getDay()]; // 获取周几

            // 去掉 eventTime 的秒数
            const eventTime = activity.eventTime.split(':').slice(0, 2).join(':');

            // 去掉 eventTimee 的秒数
            const eventTimee = activity.eventTimee.split(':').slice(0, 2).join(':');

            // 计算活动与当前时间的差异
            const currentTime = new Date();
            // 将 eventDate 和 eventTime 合并，得到完整的活动时间
            // 拼接成一个完整的日期字符串
            const eventTime2 = activity.eventTime.split(':'); // ['18', '30', '00'] 
            const eventDateTimeString = `${activity.eventDate}T${eventTime2[0]}:${eventTime2[1]}:00`; //'2025-02-18T18:30:00'
            // 创建完整的活动日期时间对象
            const eventDate2 = new Date(eventDateTimeString);

            const timeDiff = eventDate2.getTime() - currentTime.getTime(); // 获取时间差（毫秒）
            let remainingTime = '';

            if (timeDiff < 86400000) { // 如果差值小于1天（24小时），显示小时数
              const remainingHours = Math.floor(timeDiff / (1000 * 60 * 60)); // 小时数 
              remainingTime = `离本次活动开始还剩 ${remainingHours} 小时`;
            } else { // 否则显示天数
              const remainingDays = Math.floor(timeDiff / (1000 * 60 * 60 * 24)); // 天数
              remainingTime = `离本次活动开始还剩 ${remainingDays} 天`;
            }

            return {
              ...activity,
              weekDay, // 添加周几信息
              eventTime, // 格式化时间
              eventTimee,
              distance, // 添加距离信息
              remainingTime, // 添加剩余时间信息
            };
          });

          this.setData({
            activities: [...newActivities] // 新数据,
          });

        } else {
          wx.showToast({
            title: '获取活动数据失败',
            icon: 'none',
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
        });
      },
    });
  },

  // 点击按钮调用登录函数
  onLoginButtonClick() {
    app.login();
  },

  // 查看活动详情
  viewActivityDetail(e: any) {
    const activityId = e.currentTarget.dataset.id; // 获取活动ID
    wx.navigateTo({
      url: `${this.data.toEventDetailsPath}/pages/activityDetail/activityDetail?id=${activityId}`, // 跳转到活动详情页
    });
  },

  // 点击"个人信息"跳转到填写信息页面
  goToEditInfo() {
    wx.navigateTo({
      url: '/pages/editProfile/editProfile', // 跳转到编辑信息页面
    });
  },

  // 查看个人资料
  goToInfo() {
    wx.navigateTo({
      url: `/pages/profile/profile?userId=${this.data.userData.id}`,
    });
  },

  // 跳转到设置页面
  navigateToSettings() {
    wx.navigateTo({
      url: '/pages/settings/settings'
    });
  },

  // 跳转到活动页面（带标签）
  goToActivities(e: any) {
    const tab = e.currentTarget.dataset.tab; // 获取按钮对应的标签
    wx.navigateTo({
      url: `${this.data.actPath}/pages/activities/activities?tab=${tab}`, // 跳转并传递标签参数
    });
  },

  // 跳转到地图
  toGdmap() {
    wx.navigateTo({
      url: `${this.data.gdmapPath}/pages/amap/amap`
    });
  },

  // 跳转到好友列表
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

  // 创建活动
  createActivity() {
    if (!this.checkLogin()) return;
    
    wx.navigateTo({
      url: `${this.data.actPath}/pages/createActivity/createActivity`
    });
  },
  
  // 跳转到球坛
  goToForum() {
    wx.switchTab({
      url: '/pages/forum/forum'
    });
  },
  
  // 创建帖子
  createPost() {
    if (!this.checkLogin()) return;
    
    wx.navigateTo({
      url: '/pages/createPost/createPost'
    });
  },
  
  // 查看我的帖子
  goToMyPosts() {
    if (!this.checkLogin()) return;

    wx.navigateTo({
      url: '/pages/myPosts/myPosts'
    });
  },
  
  // 查看我的评论
  goToMyComments() {
    if (!this.checkLogin()) return;
    
    wx.navigateTo({
      url: '/pages/myComments/myComments'
    });
  },
  
  // 添加好友
  goToAddFriend() {
    if (!this.checkLogin()) return;
    
    wx.showModal({
      title: '添加好友',
      editable: true,
      placeholderText: '请输入好友球号',
      success: (res) => {
        if (res.confirm && res.content) {
          const ballNumber = res.content;

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
    if (!this.checkLogin()) return;
    
    wx.navigateTo({
      url: '/pages/friendRequests/friendRequests?type=0',
    });
  },
  
  // 查看我发送的好友申请
  viewSentRequests() {
    if (!this.checkLogin()) return;
    
    wx.navigateTo({
      url: '/pages/friendRequests/friendRequests?type=1',
    });
  },

  // 添加登录检查函数
  checkLogin() {
    if (!app.globalData.isLoggedin) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return false;
    }
    return true;
  },

  onShow() {
    // 强制刷新数据
    this.setData({
      activities: [],    // 清空原有数据，避免数据不一致
    });
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
      this.fetchActivities();
      // 刷新用户数据
      this.fetchUserData();
    }
  },

  // 获取用户数据
  fetchUserData() {
    const currentUser = app.globalData.currentUser;
    const width = (currentUser.exp / (currentUser.grade * 10)) * 100;
    this.setData({
      userData: { ...currentUser },
      expWidth: `${width}%`
    });
  },
});