// pages/activity_detail/activity_detail.ts
Page({
  data: {
    activity: {} as Activity,
    isApplying: false,
    isJoined: false
  },
  onLoad: function (options: { activity: string }) {
    const activity = JSON.parse(decodeURIComponent(options.activity));
    this.setData({
      activity: activity
    });
  },
  applyForActivity: function () {
    this.setData({
      isApplying: true
    });
    // 这里可以添加实际的申请逻辑，比如发送请求到服务器
    setTimeout(() => {
      this.setData({
        isApplying: false,
        isJoined: true
      });
      wx.showToast({ title: '申请成功', icon: 'success' });
    }, 2000); // 模拟2秒后申请成功
  }
});