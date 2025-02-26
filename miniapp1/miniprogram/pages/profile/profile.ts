export const app = getApp<IAppOption>();

Page({
  data: {
    apiUrl: `${app.globalData.url}/user/wx/getUserInfoByUserId`,
    userData: {},
    genderText: "",
  },

  onLoad(options) {
    const userId = options.userId; // 从页面参数获取 userId
    if (userId) {
      this.loadUserData(Number(userId));
    } else {
      wx.showToast({
        title: "用户 ID 不存在",
        icon: "none",
      });
    }
  },

  loadUserData(userId: Number) {
    wx.request({
      url: `${this.data.apiUrl}`, // 替换成你的后端接口
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser.token,
      },
      data: {
        userId: userId,
      },
      success: (res) => {
        console.log(res);
        
        if (res.statusCode === 200 && res.data.code === 0) {
          const user = res.data.data;

          this.setData({
            userData: user,
            genderText: this.getGenderText(user.gender),
          });
        } else {
          wx.showToast({
            title: "获取用户信息失败",
            icon: "none",
          });
          console.error("获取用户信息失败:", res);
        }
      },
      fail: (err) => {
        wx.showToast({
          title: "网络错误，请稍后重试",
          icon: "none",
        });
        console.error("请求失败:", err);
      },
    });
  },

  getGenderText(gender: number) {
    return gender === 1 ? "男" : gender === 2 ? "女" : "未知";
  },

   // 复制球号到剪贴板
   copyBallNumber() {
    const ballNumber = this.data.userData.ballNumber;

    if (!ballNumber) {
      wx.showToast({
        title: '球号为空',
        icon: 'none'
      });
      return;
    }

    wx.setClipboardData({
      data: ballNumber,
      success: () => {
        wx.showToast({
          title: '球号已复制',
          icon: 'success'
        });
      },
      fail: (err) => {
        wx.showToast({
          title: '复制失败',
          icon: 'none'
        });
        console.error('Clipboard Error:', err);
      }
    });
  },
});
