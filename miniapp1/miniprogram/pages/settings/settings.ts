Page({
  // 点击“个人信息”跳转到填写信息页面
  goToEditInfo() {
    wx.navigateTo({
      url: '/pages/editProfile/editProfile', // 跳转到编辑信息页面
    });
  },
});
