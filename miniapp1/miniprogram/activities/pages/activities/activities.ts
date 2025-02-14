export const app = getApp<IAppOption>();

Page({
  data: {
    activeTab: 'all', // 当前小标题
    // 后端接口相关
    apiUrl: `${app.globalData.url}/user/wx/getEventByPer`,
    currentPage: 1, // 当前页码
    pageSize: 5, // 每页大小
    hasMoreData: true, // 是否还有更多数据
        
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
      fetchActivities(isLoadMore = true) {
        if (!this.data.hasMoreData && isLoadMore) {
          wx.showToast({
            title: '没有更多活动了',
            icon: 'none',
          });
          return;
        }
    
        const { currentPage, pageSize, apiUrl } = this.data;
    
        wx.request({
          url: `${apiUrl}`,
          method: 'POST',
          header: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Token': app.globalData.currentUser.token,
          },
          data: {
            page: currentPage,
            size: pageSize,  
            userId: app.globalData.currentUser.id,
          },
          success: (res) => {
    
            if (res.statusCode === 200 && res.data.code === 0) {
              let newActivities = res.data.data;

              if (!newActivities || newActivities.length === 0) {
                this.setData({
                  hasMoreData: false, // 标记无更多数据
                });
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
    
              if (newActivities.length < pageSize) {
                this.setData({ hasMoreData: false }); // 如果返回的数据小于页面大小，说明没有更多数据了
              }
    
              this.setData({
                activities: isLoadMore
                  ? [...this.data.activities, ...newActivities] // 追加新数据
                  : [
                    ...this.data.activities.slice(0, (currentPage - 1) * pageSize), // 保留之前的数据
                    ...newActivities, // 更新当前页数据
                    ...this.data.activities.slice(currentPage * pageSize), // 保留之后的数据
                  ],
                currentPage: this.data.hasMoreData ? this.data.currentPage + 1 : this.data.currentPage, // 增加当前页码
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
},
        // 监听用户滚动到底部
    onReachBottom() {
      this.fetchActivities(true); // 请求下一页数据
    },
});
