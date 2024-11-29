// pages/friends/friends.ts
Page({
  data: {
    friendName: "",
    friends: [] as string[]
  },

  inputFriend(e: WechatMiniprogram.Input) {
    this.setData({ friendName: e.detail.value });
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
