interface myActivity {
  id: number;
  name: string;
  time: string;
  status: string;
}
Page({
  data: {
    activeTab: 'all', // 当前小标题
    activities: [] as myActivity[], // 所有活动数据
    filteredActivities: [] as myActivity[], // 筛选后的活动数据
  },

  onLoad(options: any) {
    const { tab } = options; // 获取传递的参数
    const activities = [
      { id: 1, name: '活动 1', time: '2024-01-01', status: 'all' },
      { id: 2, name: '活动 2', time: '2024-01-02', status: 'pendingDeparture' },
      { id: 3, name: '活动 3', time: '2024-01-03', status: 'pendingReview' },
      { id: 4, name: '活动 4', time: '2024-01-04', status: 'cancelled' },
    ];

    this.setData({
      activeTab: tab || 'all',
      activities,
      filteredActivities: activities.filter(activity => tab === 'all' || activity.status === tab),
    });
  },

  switchTab(e: any) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({
      activeTab: tab,
      filteredActivities: this.data.activities.filter(activity => tab === 'all' || activity.status === tab),
    });
  },
});
