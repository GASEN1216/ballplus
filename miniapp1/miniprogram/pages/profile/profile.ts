// pages/profile/profile.ts
Page({
  data: {
    nickname: "",
    hobby: "",
    skillLevel: "",
    availability: "",
  },

  onLoad() {
    this.loadProfileData();
  },

  onShow() {
    this.loadProfileData(); // 每次显示页面时都加载数据
  },

  loadProfileData() {
    const nickname = wx.getStorageSync<string>('nickname') || "";
    const hobby = wx.getStorageSync<string>('hobby') || "";
    const skillLevel = wx.getStorageSync<string>('skillLevel') || "";
    const availability = wx.getStorageSync<string>('availability') || "";
    this.setData({ nickname, hobby, skillLevel, availability });
  },

  editProfile() {
    wx.navigateTo({
      url: `/pages/editProfile/editProfile?nickname=${this.data.nickname}&hobby=${this.data.hobby}&skillLevel=${this.data.skillLevel}&availability=${this.data.availability}`,
    });
  }
});
