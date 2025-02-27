import { IAppOption } from '../../../typings'

const app = getApp<IAppOption>();

Page({
  data: {
    activity: null, // 活动数据
    userId: Number(app.globalData.currentUser.id), // 当前用户ID
    isCreator: false, // 是否为活动创建者
    isJoined: false, // 是否已参与
    feeModeText: '',
    typeText: '',
    limitsText: '',
    levelText: '',
    isPastEvent: false, // 是否是过去的活动
    isStateTwo: false,  // 活动是否 state === 2
  },

  onLoad(options) {
    const { id } = options; // 获取活动ID
    this.fetchActivityDetail(Number(id));
  },

  // 获取活动详情
  fetchActivityDetail(id: Number) {
    wx.request({
      url: `${app.globalData.url}/user/wx/getEventDetailById`, // 后端接口URL
      method: 'GET',
      header: {
        'X-Token': app.globalData.currentUser.token,
      },
      data: { eventId: id },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          const activity = res.data.data;
          // 判断当前用户是否是活动创建者
          const isCreator = activity.appId === this.data.userId;
          // 判断当前用户是否已参与活动
          const isJoined = app.globalData.myEvents.includes(id);

          // 处理消费模式
          let feeModeText = '';
          switch (activity.feeMode) {
            case 0: feeModeText = ' 免费 🎟️'; break;
            case 1: feeModeText = ' 大哥全包 💸'; break;
            case 2: feeModeText = ' AA制 💰'; break;
          }
          // 处理类型
          const typeMapping = ['🎮 娱乐', '🏋️ 训练', '🥊 对打', '🏆 比赛'];
          const typeText = typeMapping[activity.type] || '未知';

          // 处理限制
          const limitsMapping = ['🚻 无限制', '👨 男士专场', '👩 女士专场'];
          const limitsText = limitsMapping[activity.limits] || '未知';

          // 处理水平
          const levelMapping = ['🌱 小白', '🎓 初学者', '🎭 业余', '🏅 中级', '🥇 高级', '🏆 专业'];
          const levelText = levelMapping[activity.level] || '未知';

          // 获取今天的日期 (YYYY-MM-DD)
          const today = new Date().toISOString().split('T')[0];

          // 检查活动日期是否早于今天
          const isPastEvent = activity.eventDate < today;

          // 检查活动状态是否是 2
          const isStateTwo = activity.state === 2;

          this.setData({
            activity,
            isCreator,
            isJoined,
            feeModeText,
            typeText,
            limitsText,
            levelText,
            isPastEvent, // 存储过去的活动状态
            isStateTwo,
          });
        } else {
          wx.showToast({
            title: '获取活动详情失败',
            icon: 'none',
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
        });
      },
    });
  },

  // 我要取消（仅限创建者）
  cancelActivity() {
    if (!this.data.isCreator) return;

    wx.showModal({
      title: '取消活动',
      content: '确定要取消这个活动吗？',
      success: (res) => {
        if (res.confirm) {
          this.deleteActivity(); // 删除活动
        }
      }
    });
  },

  // 删除活动
  deleteActivity() {
    const { activity, userId } = this.data;

    wx.request({
      url: `${app.globalData.url}/user/wx/cancelEvent`,
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser.token,
      },
      data: {
        eventId: activity.id,
        userId,
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          wx.showToast({
            title: '活动已取消',
            icon: 'success',
          });

          wx.navigateBack(); // 返回上一页
        } else {
          wx.showToast({
            title: '取消活动失败',
            icon: 'none',
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
        });
      },
    });
  },

  // 我要退出（如果已参与活动）
  exitActivity() {
    if (!this.data.isJoined) return;

    wx.showModal({
      title: '退出活动',
      content: '确定要退出这个活动吗？',
      success: (res) => {
        if (res.confirm) {
          this.removeParticipant(); // 退出活动
        }
      }
    });
  },

  // 退出活动
  removeParticipant() {
    const { activity, userId } = this.data;

    wx.request({
      url: `${app.globalData.url}/user/wx/quitEvent`,
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser.token,
      },
      data: {
        eventId: activity.id,
        userId,
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          wx.showToast({
            title: '已退出活动',
            icon: 'success',
          });
          wx.navigateBack(); // 返回上一页
        } else {
          wx.showToast({
            title: '退出失败',
            icon: 'none',
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
        });
      },
    });
  },

  // 我要参加（如果未参加过）
  joinActivity() {
    if (this.data.isJoined) return;

    // 校验性别
    if (this.data.activity.limits !== 0) { // 0 表示无限制
      if (this.data.activity.limits === 1 && app.globalData.currentUser.gender !== 2) {
        // 活动为 "男士专场"，但当前用户不是男生（2）
        wx.showToast({
          title: '该活动仅限男士参加',
          icon: 'none',
        });
        return;
      }
      if (this.data.activity.limits === 2 && app.globalData.currentUser.gender !== 1) {
        // 活动为 "女士专场"，但当前用户不是女生（1）
        wx.showToast({
          title: '该活动仅限女士参加',
          icon: 'none',
        });
        return;
      }
    }

    wx.showModal({
      title: '参加活动',
      content: '确定要参加这个活动吗？',
      success: (res) => {
        if (res.confirm) {
          this.addParticipant(); // 参加活动
        }
      }
    });
  },

  // 参加活动
  addParticipant() {
    const { activity, userId } = this.data;

    wx.request({
      url: `${app.globalData.url}/user/wx/joinEvent`,
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser.token,
      },
      data: {
        eventId: activity.id,
        userId,
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          wx.showToast({
            title: '已参加活动',
            icon: 'success',
          });
          wx.navigateBack(); // 返回上一页
        } else {
          wx.showToast({
            title: '参加失败',
            icon: 'none',
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none',
        });
      },
    });
  },

  goToInfo(e: any) {
    const userId = e.currentTarget.dataset.userid; // 获取传递的id
    wx.navigateTo({
      url: `../../../pages/profile/profile?userId=${userId}`,
    });
  },
});
