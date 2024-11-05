// pages/editProfile/editProfile.ts
Page({
  data: {
    nickname: "",
    hobby: "",
    skillLevel: "",
    availability: ""
  },

  onLoad(options) {
    // 获取传递的参数
    this.setData({
      nickname: options.nickname || "",
      hobby: options.hobby || "",
      skillLevel: options.skillLevel || "",
      availability: options.availability || ""
    });
  },

  inputNickname(e: WechatMiniprogram.Input) {
    this.setData({ nickname: e.detail.value });
  },

  inputHobby(e: WechatMiniprogram.Input) {
    this.setData({ hobby: e.detail.value });
  },

  inputSkillLevel(e: WechatMiniprogram.Input) {
    this.setData({ skillLevel: e.detail.value });
  },

  inputAvailability(e: WechatMiniprogram.Input) {
    this.setData({ availability: e.detail.value });
  },

  saveProfile() {
    wx.setStorageSync('nickname', this.data.nickname);
    wx.setStorageSync('hobby', this.data.hobby);
    wx.setStorageSync('skillLevel', this.data.skillLevel);
    wx.setStorageSync('availability', this.data.availability);
    wx.showToast({
      title: '资料已保存',
      icon: 'success'
    });
    
    wx.navigateBack();
  }
});
