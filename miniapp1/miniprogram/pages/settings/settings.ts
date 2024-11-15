Page({
  goToAccountSettings() {
    wx.navigateTo({ url: '/pages/settings/account' });
  },
  goToPrivacySettings() {
    wx.navigateTo({ url: '/pages/settings/privacy' });
  },
  goToNotifications() {
    wx.navigateTo({ url: '/pages/settings/notifications' });
  }
});
