Page({
  data: {
    time: '',
    host: '',
    currentParticipants: '',
    maxParticipants: '',
    content: ''
  },

  onLoad: function(options) {
    if (options && options.time) {
      this.setData({
        time: options.time,
        host: options.host,
        currentParticipants: options.currentParticipants,
        maxParticipants: options.maxParticipants,
        content: options.content
      });
    }
  },

  formSubmit: function(e: { detail: { value: string } }) {
    const formData = e.detail.value;
    console.log('表单数据:', formData);
    // 这里可以处理表单提交，例如保存到服务器
    wx.showToast({
      title: '活动创建成功',
      icon: 'success',
      duration: 2000
    });

    // 跳转回上一页或首页
    wx.navigateBack();
  }
});