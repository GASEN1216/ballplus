Page({
  data: {
    activity: {} as {
      id: number;
      name: string;
      time: string;
      location: string;
      participants: string;
      cover: string;
    }
  },

  onLoad(options: { id: string }) {
    const activityId = parseInt(options.id);
    this.loadActivityDetails(activityId);
  },

  loadActivityDetails(id: number) {
    const fakeActivities = [
      { id: 1, name: '篮球友谊赛', time: '2024-01-10 18:00', location: 'XX体育馆', participants: '8/10', cover: 'https://picsum.photos/200/150?random=1' },
      { id: 2, name: '瑜伽课程', time: '2024-01-12 10:00', location: 'YY瑜伽馆', participants: '5/10', cover: 'https://picsum.photos/200/150?random=2' }
    ];
    
    const activity = fakeActivities.find(item => item.id === id);
    if (activity) {
      this.setData({ activity });
    } else {
      wx.showToast({
        title: '活动不存在',
        icon: 'none'
      });
    }
  },

  joinActivity() {
    wx.showToast({
      title: '报名成功！',
      icon: 'success'
    });
  }
});
