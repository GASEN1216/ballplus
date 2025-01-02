Page({
  data: {
    url: ''
  },

  onLoad(options: { url: string }) {
    if (options.url) {
      this.setData({ url: decodeURIComponent(options.url) });
    } else {
      wx.showToast({
        title: '无效的链接',
        icon: 'error'
      });
    }
  }
});
