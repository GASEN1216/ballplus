// pages/friends/friends.ts
Page({
  data: {
    friendName: "",
    friends: [] as string[]
  },

  inputFriend(e: WechatMiniprogram.Input) {
    this.setData({ friendName: e.detail.value });
  },
  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 2  //这个数字是当前页面在tabBar中list数组的索引
      })
    }
  },
  addFriend() {
    const newFriend = this.data.friendName.trim();
    if (newFriend) {
      const friends = [...this.data.friends, newFriend];
      this.setData({ friends, friendName: "" });
      wx.setStorageSync('friends', friends);
      wx.showToast({
        title: '好友已添加',
        icon: 'success'
      });
    }
  },

  onLoad() {
    const friends = wx.getStorageSync<string[]>('friends') || [];
    this.setData({ friends });
  }
});
