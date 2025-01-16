import { IAppOption } from '../../../typings'

// pages/mine/mine.ts
const app = getApp<IAppOption>();

Page({
  data: {
    sentRequests: [] as Array<{ ballNumber: string; status: string }>, // 已发送的好友申请
    receivedRequests: [] as Array<{ ballNumber: string; status: string }>, // 收到的好友申请
  },

  // 点击“个人信息”跳转到填写信息页面
  goToEditInfo() {
    wx.navigateTo({
      url: '/pages/editProfile/editProfile',
    });
  },

  // 点击“添加好友”功能
  goToAddFriend() {
    wx.showModal({
      title: '添加好友',
      editable: true,
      placeholderText: '请输入好友球号',
      success: (res) => {
        if (res.confirm && res.content) {
          const ballNumber: string = res.content;

          // 校验输入数据是否为数字
          if (!/^\d+$/.test(ballNumber)) {
            wx.showToast({
              title: '球号只能包含数字',
              icon: 'none',
            });
            return;
          }

          // 调用后端接口发送好友申请
          wx.request({
            url: `${app.globalData.url}/user/wx/addFriendsRequest`,
            method: 'POST',
            header: {
              'Content-Type': 'application/x-www-form-urlencoded',
              'X-Token': app.globalData.currentUser.token,
            },
            data: {
              userId: app.globalData.currentUser.id, // 当前用户ID
              ballNumber, // 要添加的好友球号
            },
            success: (response) => {
              if (response.statusCode === 200 && response.data.success) {
                wx.showToast({
                  title: '好友申请已发送',
                  icon: 'success',
                });
              } else {
                console.log(response);
                
                wx.showToast({
                  title: response.data.data || '好友申请发送失败',
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
        }
      },
    });
  },


  // 查看好友申请
  viewFriendRequests() {
    wx.navigateTo({
      url: '/pages/friendRequests/friendRequests',
    });
  },
});
