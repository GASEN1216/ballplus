export const app = getApp<any>();

Page({
  data: {
    activeTab: 'received', // 当前激活的标签
    receivedRequests: [] as Array<{ id: number; avatar: string; name: string; state: number }>, // 收到的好友申请
    sentRequests: [] as Array<{ id: number; avatar: string; name: string; state: number }>, // 已发送的好友申请
  },

  onLoad(options) {
    // 获取URL参数type（0:收到的申请，1:发送的申请）
    const type = options && options.type ? parseInt(options.type) : 0;
    
    // 设置激活的标签
    this.setData({
      activeTab: type === 0 ? 'received' : 'sent'
    });
    
    // 加载对应类型的好友申请
    this.loadRequests(type);
  },

  // 切换标签
  switchTab(e: WechatMiniprogram.BaseEvent) {
    const tab: string = e.currentTarget.dataset.tab;
    this.setData({ activeTab: tab });

    // 根据标签加载对应的好友申请
    const type = tab === 'received' ? 0 : 1;
    this.loadRequests(type);
  },

  // 加载好友申请
  loadRequests(type: number) {
    wx.request({
      url: `${app.globalData.url}/user/wx/getFriendsRequest`,
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser.token,
      },
      data: {
        userId: app.globalData.currentUser.id,
        type, // 0: 收到的申请, 1: 发送的申请
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          if (type === 0) {
            this.setData({ receivedRequests: res.data.data });
          } else {
            this.setData({ sentRequests: res.data.data });
          }
        } else {
          wx.showToast({
            title: '加载失败，请重试',
            icon: 'none',
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '请求失败，请检查网络',
          icon: 'none',
        });
      },
    });
  },

  // 同意好友申请
  acceptRequest(e: WechatMiniprogram.BaseEvent) {
    const id: number = e.currentTarget.dataset.id;

    // 调用同意好友申请接口
    wx.request({
      url: `${app.globalData.url}/user/wx/agreeFriendsRequest`,
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser.token,
      },
      data: {
        userId: app.globalData.currentUser.id, // 当前用户 ID
        friendId: id, // 申请好友 ID
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          wx.showToast({
            title: '已同意',
            icon: 'success',
          });

          // 更新本地数据
          const index = this.data.receivedRequests.findIndex((request) => request.id === id);
          if (index !== -1) {
            const updatedRequests = [...this.data.receivedRequests];
            updatedRequests[index].state = 1; // 更新状态为已同意
            this.setData({ receivedRequests: updatedRequests });
          }
        } else {
          wx.showToast({
            title: '操作失败',
            icon: 'none',
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '请求失败，请检查网络',
          icon: 'none',
        });
      },
    });
  },
});
