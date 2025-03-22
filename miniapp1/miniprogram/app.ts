// app.ts
import GoEasy from './libs/goeasy-2.6.6.esm.min';
import { IAppOption } from '../typings';

App<IAppOption>({
  globalData: {
    qnurl: 'http://sunsetchat.top/',// 加个‘/’方便直接加图片名
    url: 'http://192.168.1.10:8080',
    isLoggedin: false,
    latitude: 23.108649,
    longitude: 113.324646,
    currentUser: {
      id: '',
      ballNumber: '',
      name: '',
      avatar: '',
      token: '',
      gender: 0,
      exp: 0,
      grade: 0,
      state: 0,
      unblockingTime: '',
      birthday: '',
      credit: 0,
      score: 0,
      description: '',
      label: '',
      phone: ''
    },
    myEvents: [] // 添加 myEvents 数组来保存当前用户的活动 ID
  },

  // 获取完整URL的方法
  getUrl(path: string): string {
    return `${this.globalData.url}${path}`;
  },

  initializeGoEasy() {
    wx.goEasy = GoEasy.getInstance({
      host: 'hangzhou.goeasy.io', // 应用所在的区域地址
      appkey: 'BC-d9ccbb5323994296b2255364532d3c2a', // common key
      modules: ['im']
    });
    wx.GoEasy = GoEasy;

    console.log('GoEasy initialized');
  },

  login() {
    wx.getLocation({
      type: 'wgs84', // 坐标类型
      success: (res) => {
        const { latitude, longitude } = res;
        // 直接将位置数据保存到 globalData
        this.globalData.latitude = latitude;
        this.globalData.longitude = longitude;

        // Notify pages after login
        if (this.jwReadyCallback) {
          this.jwReadyCallback();
        }
      },
      fail: (err) => {
        // 区分位置获取失败的原因
        if (err.errMsg.includes('auth deny') || err.errMsg.includes('auth denied')) {
          wx.showToast({
            title: '获取位置失败：请授权位置权限',
            icon: 'none',
          });
        } else {
          wx.showToast({
            title: '获取位置失败：请检查网络或定位服务',
            icon: 'none',
          });
        }
        console.error('获取位置失败:', err);
      }
    });
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

                console.log(userData);

                this.globalData.isLoggedin = true,
                  this.globalData.currentUser = Object.assign(this.globalData.currentUser || {}, {
                    id: String(userData.id),
                    ballNumber: userData.ballNumber,
                    name: userData.userAccount || '用户',
                    avatar: userData.avatarUrl || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0',
                    token: userData.token,
                    gender: userData.gender || 0,
                    exp: userData.exp || 0,
                    grade: userData.grade || 1,
                    state: userData.state || 0,
                    unblockingTime: userData.unblockingTime || '',
                    birthday: userData.birthday || '',
                    credit: userData.credit || 100,
                    score: userData.score || 0,
                    description: userData.description || '这个人很冷酷，什么都没留下...',
                    label: userData.label || '',
                    phone: userData.phone || ''
                  });

                // 调用封装的 GoEasy 初始化函数
                this.initializeGoEasy();

                // 请求获取用户的所有活动 ID
                this.fetchMyEvents();

                // Notify pages after login
                if (this.loginReadyCallback) {
                  this.loginReadyCallback();
                }
              } else {
                console.error('Unexpected response:', response.data);
                wx.showToast({
                  title: '登录失败：服务器返回数据异常',
                  icon: 'error',
                });
              }
            },
            fail: (error) => {
              // 区分请求失败的原因
              if (error.errMsg.includes('timeout')) {
                wx.showToast({
                  title: '登录失败：请求超时，请检查网络',
                  icon: 'none',
                });
              } else {
                wx.showToast({
                  title: '登录失败：服务器错误',
                  icon: 'none',
                });
              }
              console.error('Failed to send login request:', error);
            },
          });
        } else {
          console.error('Login failed:', res.errMsg);
          wx.showToast({
            title: '登录失败：获取 code 失败',
            icon: 'none',
          });
        }
      },
      fail: (loginError) => {
        // 区分 wx.login 失败的原因
        if (loginError.errMsg.includes('network error')) {
          wx.showToast({
            title: '登录失败：网络错误，请检查网络连接',
            icon: 'none',
          });
        } else {
          wx.showToast({
            title: '登录失败：未知错误',
            icon: 'none',
          });
        }
        console.error('Failed to execute wx.login:', loginError);
      }
    });

  },

  // 获取用户的所有活动 ID
  fetchMyEvents() {
    const userId = this.globalData.currentUser.id;

    wx.request({
      url: this.getUrl('/user/wx/getAllMyEvents'), // 后端接口URL
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': this.globalData.currentUser.token,
      },
      data: {
        userId: userId,
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          // 将获取到的活动 ID 数组保存到 myEvents
          this.globalData.myEvents = res.data.data || [];
        } else {
          wx.showToast({
            title: '获取活动列表失败',
            icon: 'none',
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
        });
      }
    });
  },

  onLaunch() {
    this.login();
  },

  calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number) {
    const EARTH_RADIUS = 6371e3; // 地球半径，单位为米
    const toRadians = (deg: number) => (deg * Math.PI) / 180;

    const dLat = toRadians(lat2 - lat1);
    const dLon = toRadians(lon2 - lon1);
    const lat1Rad = toRadians(lat1);
    const lat2Rad = toRadians(lat2);

    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1Rad) * Math.cos(lat2Rad) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    const distance = EARTH_RADIUS * c; // 距离，单位为米

    // 根据距离自动选择单位
    return distance >= 1000
      ? `${(distance / 1000).toFixed(1)}km`
      : `${Math.round(distance)}m`;
  },
  cD(lat: number, lon: number) {
    return this.calculateDistance(lat, lon, this.globalData.latitude, this.globalData.longitude)
  }
});
