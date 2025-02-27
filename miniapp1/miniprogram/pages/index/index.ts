import { IAppOption } from '../../../typings'

// pages/mine/mine.ts
const app = getApp<IAppOption>();

Page({
  data: {
    // 后端接口相关
    apiUrl: `${app.globalData.url}/user/wx/getEvent`,
    matchApiUrl: `${app.globalData.url}/user/wx/matchEvent`,
    toEventDetailsPath: "../../activities",
    showAnimation: false, // 是否显示匹配动画

    // 活动数据
    activities: [] as any[], // 所有活动数据
    filteredActivities: [] as any[], // 筛选后的活动数据

    // 轮播图数据
    carouselImages: [
      { url: 'https://picsum.photos/1200/400?random=1', link: '../../study/pages/resources/resources' },
      { url: 'https://picsum.photos/1200/400?random=2', link: '../../study/pages/resources/resources' },
      { url: 'https://picsum.photos/1200/400?random=3', link: '../../study/pages/resources/resources' }
    ],

    // 路径配置
    activityPath: '../../activities',
    matchActivityPath: '../../match',
    studyPath: '../../study',
    gdmapPath: '../../gdmap',

    // 搜索和过滤
    searchQuery: '',

    dateRange: [], // 日期范围
    selectedDateIndex: 0, // 选中的日期索引
    sortOptions: ['距离排序', '日期排序', '人数排序', '综合排序'], // 排序选项
    selectedSort: 0, // 当前选中的排序方式
    order: 'asc', // 默认正序
  },

  // 页面加载时获取活动数据
  onLoad() {
    this.initDateRange();
    this.fetchActivities(); 
    app.loginReadyCallback = () => {
      this.fetchActivities(); 
    },
      app.jwReadyCallback = () => {
        this.fetchActivities(); 
      }
  },

  onUnload() {
    // 强制刷新数据
    this.setData({
      activities: [],    // 清空原有数据，避免数据不一致
      filteredActivities: []
    });
  },

  // 页面每次显示时触发
  onShow() {
    if (app.globalData.isLoggedin) {
      this.fetchActivities(); // 重新拉取数据
    }
  },

  // 轮播图点击事件
  onCarouselItemClick(e: any) {
    const link = e.currentTarget.dataset.link; // 获取 data-link 的值
    if (!link) {
      wx.showToast({
        title: '链接无效',
        icon: 'none'
      });
      return;
    }
    wx.navigateTo({ url: link });
  },

  // 查看活动详情
  viewActivityDetail(e: any) {
    const activityId = e.currentTarget.dataset.id; // 获取活动ID
    wx.navigateTo({
      url: `${this.data.toEventDetailsPath}/pages/activityDetail/activityDetail?id=${activityId}`, // 跳转到活动详情页
    });
  },


  // 初始化日期范围
  initDateRange() {
    const today = new Date();
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];

    const dateRange = Array.from({ length: 5 }, (_, i) => {
      const date = new Date(today.getTime() + i * 24 * 60 * 60 * 1000);

      // 获取 YYYY-MM-DD 格式的日期
      const ymd = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;

      return {
        date: date.getDate(), // 日期
        week: weekDays[date.getDay()], // 周几
        ymd: ymd//补充获取如2025-01-17的数据
      };
    });

    this.setData({ dateRange });
  },

  // 日期选择
  handleDateSelect(e) {
    const { index } = e.currentTarget.dataset;
    this.setData({ selectedDateIndex: index });
    // 可在此调用方法按日期筛选活动
    this.fetchActivities(); // 重新请求当前页数据
  },

  // 切换排序菜单
  toggleSortMenu() {
    wx.showActionSheet({
      itemList: this.data.sortOptions,
      success: (res) => {
        this.setData({ selectedSort: res.tapIndex });
        this.filterActivities(); // 按新排序方式重新过滤
      },
    });
  },

  // 切换地图模式
  toggleMapMode() {
    wx.navigateTo({
      url: `${this.data.gdmapPath}/pages/amap/amap`
    });
  },

  // 获取活动数据
  fetchActivities() {

    const { apiUrl } = this.data;

    wx.request({
      url: `${apiUrl}`,
      method: 'GET',
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

            return {
              ...activity,
              weekDay, // 添加周几信息
              eventTime, // 格式化时间
              eventTimee,
              distance, // 添加距离信息
            };
          });

          this.setData({
            activities: [
                ...newActivities, // 更新当前页数据
              ],
          });
          this.filterActivities(); // 按条件过滤数据
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

  // 筛选活动数据
  filterActivities() {
    const { activities, selectedSort, order, dateRange, selectedDateIndex } = this.data;
    let ymd = dateRange[selectedDateIndex].ymd;
    const getDistanceValue = (distance: string): number => {
      if (distance.endsWith('km')) {
        return parseFloat(distance.replace('km', '')) * 1000; // 转为米
      } else if (distance.endsWith('m')) {
        return parseFloat(distance.replace('m', '')); // 保持米
      }
      return Infinity; // 无效值放在最后
    };

    // 若activity.eventDate和ymd不一样则过滤掉
    let filtered = activities.filter(activity => activity.eventDate === ymd);

    // 排序逻辑
    filtered = filtered.sort((a, b) => {
      let comparison = 0;
      switch (selectedSort) {
        case 0:
          comparison = getDistanceValue(a.distance) - getDistanceValue(b.distance);
          break;
        case 1:
          comparison = new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime();
          break;
        case 2:
          comparison = a.totalParticipants - b.totalParticipants;
          break;
        case 3:
          comparison = getDistanceValue(a.distance) - getDistanceValue(b.distance) || new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime();
          break;
      }
      return order === 'asc' ? comparison : -comparison;
    });

    this.setData({ filteredActivities: filtered });
  },

  // 发起活动
  createActivity() {
    wx.navigateTo({
      url: `${this.data.activityPath}/pages/createActivity/createActivity`,
    });
  },

  // 日期筛选
  handleDateFilterChange(e) {
    const selectedDate = this.data.dateRange[e.detail.value];
    this.setData({ selectedDate });
  },

  // 切换排序顺序
  toggleOrder() {
    const newOrder = this.data.order === 'asc' ? 'desc' : 'asc';
    this.setData({ order: newOrder });
    this.filterActivities(); // 按新顺序重新过滤
  },

    // 匹配活动
    matchActivity() {
      this.setData({ showAnimation: true }); // 开始播放动画
  
      setTimeout(() => {
        wx.request({
          url: this.data.matchApiUrl,
          method: 'POST',
          header: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Token': app.globalData.currentUser.token,
          },
          data: {
            userId: app.globalData.currentUser.id,
            latitude: app.globalData.latitude,
            longitude: app.globalData.longitude
          },
          success: (res) => {
            if (res.statusCode === 200 && res.data.code === 0) {
              const matchedEventId = res.data.data;
              if (!isNaN(matchedEventId)) {
                // 关闭动画
                this.setData({ showAnimation: false });
                
                // 跳转到活动详情页
                wx.navigateTo({
                  url: `${this.data.activityPath}/pages/activityDetail/activityDetail?id=${matchedEventId}`,
                });
              } else {
                wx.showToast({
                  title: res.data.data,
                  icon: 'none',
                });
                this.setData({ showAnimation: false }); // 关闭动画
              }
            } else {
              wx.showToast({
                title: '匹配失败，请稍后再试',
                icon: 'none',
              });
              this.setData({ showAnimation: false }); // 关闭动画
            }
          },
          fail: () => {
            wx.showToast({
              title: '网络错误，请稍后再试',
              icon: 'none',
            });
            this.setData({ showAnimation: false }); // 关闭动画
          },
        });
      }, 1000); // 动画时间
    },

});
