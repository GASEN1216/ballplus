// pages/index/index.ts
const app = getApp<IAppOption>();

Page({
  data: {
    isLoggedin: false,
    nickname: '',
    profilePic: '',
    tokenStatus: '', // 显示token验证结果
  },

  // 登录方法
  onLoginButtonClick() {
    // 先调用 wx.login 获取 code
    wx.login({
      success: (res) => {
        if (res.code) {
          // 将 code 发送到服务器
          wx.request({
            url: 'http://ytrumg.natappfree.cc/user/wx/login', // 确保URL正确
            method: 'POST',
            header: {
              'Content-Type': 'application/x-www-form-urlencoded'
            },
            data: {
              code: res.code // 只发送 code
            },
            success: (response) => {
              // 检查 response.data 是否是对象，并包含 userName
              if (typeof response.data === 'object' && response.data !== null && 'data' in response.data && 'userAccount' in response.data.data) {
                const userData = response.data.data; // 假设 user 数据直接在 response.data 中
                this.setData({
                  isLoggedin: true,
                  nickname: userData.userAccount || '用户',
                  profilePic: userData.avatarUrl || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
                });

                // 保存 token 到全局变量
                app.globalData.token = userData.password;
                console.log(app.globalData.token);

                wx.showToast({
                  title: '登录成功',
                  icon: 'success',
                });
              } else {
                console.error('Unexpected response:', response.data);
                wx.showToast({
                  title: '数据错误',
                  icon: 'error',
                });
              }
            },
            fail: (error) => {
              wx.showToast({
                title: '登录失败',
                icon: 'error',
              });
              console.error('Failed to send login request:', error);
            },
          });
        } else {
          console.error('Login failed:', res.errMsg);
        }
      },
      fail: (loginError) => {
        wx.showToast({
          title: '登录失败',
          icon: 'error',
        });
        console.error('Failed to execute wx.login:', loginError);
      }
    });
  },
  // 验证token方法
  onTokenButtonClick() {
    const token = app.globalData.token; // 获取全局token
    wx.request({
      url: 'http://ytrumg.natappfree.cc/user/test/token', // 确保URL正确
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      data: {
        token: token
      },
      success: (response) => {
        if (response.data && typeof response.data === 'object' && 'code' in response.data) {
          if (response.data.code === 0) {
            this.setData({
              tokenStatus: 'Token有效',
            });
          } else {
            this.setData({
              tokenStatus: 'Token无效或已过期',
            });
          }
        }
      },
      fail: () => {
        this.setData({
          tokenStatus: '验证失败',
        });
      }
    });
  },
});
