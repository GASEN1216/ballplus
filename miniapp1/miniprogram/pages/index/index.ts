Page({
  data: {
    studyPath: '../../study',
    matchActivityPath: '../../match',
    // 轮播图图片数据
    carouselImages: [
      { url: 'https://picsum.photos/1200/400?random=1', link: '/pages/mine/mine' },
      { url: 'https://picsum.photos/1200/400?random=2', link: '/pages/forum/forum' },
      { url: 'https://picsum.photos/1200/400?random=3', link: '/pages/settings/settings' }
    ],
    // 活动数据
    activities: [
      { 
        id: 1,
        name: '篮球友谊赛',
        time: '2024-01-10 18:00',
        location: 'XX体育馆',
        participants: '8/10',
        skillLevel: '中级',
        type: '篮球'
      },
      { 
        id: 2,
        name: '瑜伽课程',
        time: '2024-01-12 10:00',
        location: 'YY瑜伽馆',
        participants: '5/10',
        skillLevel: '初级',
        type: '瑜伽'
      }
    ],
    userPreferences: {
      skillLevel: '初级',
      preferredType: '瑜伽'
    },
    filteredActivities: [] as any[],
    searchQuery: ''
  },

  onLoad() {
    this.setData({
      filteredActivities: this.data.activities
    });
  },

  // 轮播图点击事件
  onCarouselItemClick(e:any) {
    const link = e.currentTarget.dataset.link;
    if (link) {
      wx.navigateTo({
        url: link
      });
    }
  },

    goToStudy() {
      wx.navigateTo({
        url: `${this.data.studyPath}/pages/resources/resources`
      });
    },

  // 匹配活动
  matchActivity() {
    const { activities, userPreferences } = this.data;

    // 简单的匹配逻辑
    const matchedActivity = activities.find(activity => 
      activity.skillLevel === userPreferences.skillLevel &&
      activity.type === userPreferences.preferredType
    );

    if (matchedActivity) {
      wx.navigateTo({
        url: `${this.data.matchActivityPath}/pages/matchActivity/matchActivity?id=${matchedActivity.id}`
      });
    } else {
      wx.showToast({
        title: '未找到合适的活动',
        icon: 'none'
      });
    }
  },
    // 搜索活动
    onSearchInput(e: any) {
      this.setData({ searchQuery: e.detail.value });
    },
  
    onSearch() {
      const { activities, searchQuery } = this.data;
      const filtered = activities.filter(item => 
        item.name.includes(searchQuery) || item.type.includes(searchQuery)
      );
      this.setData({ filteredActivities: filtered });
    },
  
    // 标签筛选
    filterByTag(e: any) {
      const tag = e.currentTarget.dataset.tag;
      const filtered = this.data.activities.filter(activity => activity.type === tag);
      this.setData({ filteredActivities: filtered });
    },
  
    // 排序活动
    sortActivities(e: any) {
      const sortType = e.currentTarget.dataset.sort;
      let sortedActivities = [...this.data.filteredActivities];
  
      if (sortType === 'distanceAsc') {
        sortedActivities.sort((a, b) => a.distance - b.distance);
      } else if (sortType === 'distanceDesc') {
        sortedActivities.sort((a, b) => b.distance - a.distance);
      }
  
      this.setData({ filteredActivities: sortedActivities });
    },
  
    // 查看活动详情
    viewActivityDetail(e: any) {
      const activityId = e.currentTarget.dataset.id;
      wx.navigateTo({
        url: `${this.data.matchActivityPath}/pages/matchActivityPath/matchActivityPath?id=${activityId}`
      });
    }
});
