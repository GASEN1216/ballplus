import { IAppOption } from '../../../typings'

// pages/mine/mine.ts
const app = getApp<IAppOption>();

Page({
  data: {
    // 后端接口相关
    apiUrl: `${app.globalData.url}/user/wx/getEvent`,
    currentPage: 1, // 当前页码
    pageSize: 5, // 每页大小
    hasMoreData: true, // 是否还有更多数据

    // 活动数据
    activities: [] as any[], // 所有活动数据
    filteredActivities: [] as any[], // 筛选后的活动数据

    // 轮播图数据
    carouselImages: [
      { url: 'https://picsum.photos/1200/400?random=1', link: '/pages/mine/mine' },
      { url: 'https://picsum.photos/1200/400?random=2', link: '/pages/forum/forum' },
      { url: 'https://picsum.photos/1200/400?random=3', link: '/pages/settings/settings' }
    ],

    // 路径配置
    createActivityPath: '../../activities',
    matchActivityPath: '../../match',
    studyPath: '../../study',

    // 搜索和过滤
    searchQuery: '',
  },

  // 页面加载时获取活动数据
  onLoad() {
    app.loginReadyCallback = () => {
      this.fetchActivities(); // 获取第一页活动数据
    }
  },

  // 页面每次显示时触发
  onShow() {
    this.removeCurrentPageActivities(); // 移除当前页数据
    this.fetchActivities(false); // 重新请求当前页数据
  },

  // 移除当前页数据
  removeCurrentPageActivities() {
    const { activities, currentPage, pageSize } = this.data;

    // 计算当前页数据的开始和结束索引
    const startIdx = (currentPage - 1) * pageSize;
    const endIdx = currentPage * pageSize;

    // 创建新数组，仅保留非当前页的数据
    const updatedActivities = activities.filter((_, index) => index < startIdx || index >= endIdx);

    this.setData({
      activities: updatedActivities,
      filteredActivities: updatedActivities,
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
      method: 'GET',
      header: {
        'X-Token': app.globalData.currentUser.token,
      },
      data: {
        page: currentPage,
        size: pageSize,
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          let newActivities = res.data.data.records;

          if (!newActivities || newActivities.length === 0) {
            wx.showToast({
              title: '暂时没有活动发布哦~',
              icon: 'none',
            });
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

            return {
              ...activity,
              weekDay, // 添加周几信息
              eventTime, // 格式化时间
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
            filteredActivities: isLoadMore
              ? [...this.data.activities, ...newActivities]
              : [
                  ...this.data.activities.slice(0, (currentPage - 1) * pageSize),
                  ...newActivities,
                  ...this.data.activities.slice(currentPage * pageSize),
                ],
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

  // 监听用户滚动到底部
  onReachBottom() {
    this.fetchActivities(); // 请求下一页数据
  },

  // 发起活动
  createActivity() {
    wx.navigateTo({
      url: `${this.data.createActivityPath}/pages/createActivity/createActivity`,
    });
  },

  // 查看活动详情
  viewActivityDetail(e: any) {
    const activityId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `${this.data.matchActivityPath}/pages/matchActivity/matchActivity?id=${activityId}`,
    });
  },

  // 搜索活动
  onSearchInput(e: any) {
    this.setData({ searchQuery: e.detail.value });
  },

  onSearch() {
    const { activities, searchQuery } = this.data;
    const filtered = activities.filter((item) =>
      item.name.includes(searchQuery) || item.location.includes(searchQuery)
    );
    this.setData({ filteredActivities: filtered });
  },
});
