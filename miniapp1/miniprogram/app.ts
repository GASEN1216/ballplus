// app.ts
App<IAppOption>({
  globalData: {
    url: 'http://192.168.1.10:8080',
    isLoggedin: false,
    nickname: '',
    profilePic: '',
    token: '',
  },

  // 获取完整URL的方法
  getUrl(path: string): string {
    return `${this.globalData.url}${path}`;
  },

  login(){
    // 先调用 wx.login 获取 code
    wx.login({
      success: (res) => {
        if (res.code) {
          // 将 code 发送到服务器
          wx.request({
            url: this.getUrl('/user/wx/login'), // 使用 getUrl 获取完整URL
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

                this.globalData.isLoggedin = true,
                  this.globalData.nickname = userData.userAccount || '用户',
                  this.globalData.profilePic = userData.avatarUrl || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0';
                // 保存 token 到全局变量
                this.globalData.token = userData.password;
                console.log(this.globalData.token);

                // Notify pages after login
                if (this.loginReadyCallback) {
                  this.loginReadyCallback();
                }

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

  onLaunch() {
    this.login();
  },
});
