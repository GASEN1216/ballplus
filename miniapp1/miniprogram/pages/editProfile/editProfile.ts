// editInfo.ts
Page({
  data: {
    nickname: '',
    gender: '',
    birthday: '',
    city: '',
    sports: [],
    bio: '',
    profilePic: '', // 用于存储头像URL
  },

  // 输入框值变化处理，指定参数类型
  handleInputChange(e: any) {
    const field = e.target.dataset.field;
    this.setData({
      [field]: e.detail.value,
    });
  },

  // 处理checkbox选项变化
  handleSportsChange(e: any) {
    // 获取选中的运动项目
    const selectedSports = e.detail.value;  // 获取选中的checkbox值

    // 更新sports数组
    this.setData({
      sports: selectedSports,
    });
  },

  // 选择头像
  chooseAvatar() {
    wx.chooseImage({
      success: (res) => {
        this.setData({
          profilePic: res.tempFilePaths[0],
        });
      },
    });
  },

  // 保存信息
  saveInfo() {
    // 这里可以做表单验证，检查用户是否输入了必填项
    if (!this.data.nickname || !this.data.city) {
      wx.showToast({
        title: '昵称和城市是必填项',
        icon: 'none',
      });
      return;
    }

    // 模拟保存过程
    wx.showToast({
      title: '保存成功',
      icon: 'success',
    });

    // 可以在此保存数据到全局或数据库
    const app = getApp();
    app.globalData.nickname = this.data.nickname;
    app.globalData.profilePic = this.data.profilePic;
    app.globalData.gender = this.data.gender;
    app.globalData.city = this.data.city;
    app.globalData.sports = this.data.sports;
    app.globalData.bio = this.data.bio;
    
    // 返回到个人主页
    wx.navigateBack();
  },
});
