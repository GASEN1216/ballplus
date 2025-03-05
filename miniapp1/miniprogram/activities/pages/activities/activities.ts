export const app = getApp<IAppOption>();

Page({
  data: {
    activeTab: 'all', // 当前小标题
    // 后端接口相关
    apiUrl: `${app.globalData.url}/user/wx/getEventByPer`,
    // 活动数据
    activities: [] as any[], // 所有活动数据
    filteredActivities: [] as any[], // 筛选后的活动数据
  },

  onLoad(options: any) {
    const { tab } = options; // 获取传递的参数

    this.setData({
      activeTab: tab || 'all',
    });

    this.fetchActivities();
  },

  switchTab(e: any) {
    const tab = e.currentTarget.dataset.tab;
    const tabNumber = Number(tab);  // 将 tab 转换为数字
    this.setData({
      activeTab: tab,
      filteredActivities: this.data.activities.filter(activity => tab === 'all' || activity.state === tabNumber),
    });
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
    
                const eventDate = new Date(activity.eventDate);
                const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
                const weekDay = weekDays[eventDate.getDay()]; // 获取周几
    
                // 去掉 eventTime 的秒数
                const eventTime = activity.eventTime.split(':').slice(0, 2).join(':');
    
                // 去掉 eventTimee 的秒数
                const eventTimee = activity.eventTimee.split(':').slice(0, 2).join(':');
    
                return {
                  ...activity,
                  weekDay, // 添加周几信息
                  eventTime, // 格式化时间
                  eventTimee,
                };
              });
    
              this.setData({
                activities: [...newActivities],  // 更新当前页数据
              });
              // 在更新 activities 后，重新计算 filteredActivities
              this.updateFilteredActivities();
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
      // 重新计算 filteredActivities
updateFilteredActivities() {
  const { activeTab, activities } = this.data;
  this.setData({
    filteredActivities: activities.filter(activity => activeTab === 'all' || activity.state === Number(activeTab)),
  });
  console.log(this.data.filteredActivities);
},

  // 查看活动详情
  viewActivityDetail(e: any) {
    const activityId = e.currentTarget.dataset.id; // 获取活动ID
    wx.navigateTo({
      url: `../activityDetail/activityDetail?id=${activityId}`, // 跳转到活动详情页
    });
  },

});
