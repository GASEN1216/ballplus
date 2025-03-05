import { IAppOption } from '../../../typings'

// pages/mine/mine.ts
const app = getApp<IAppOption>();

Page({
  data: {
    event: {
      appId: null, // 发起人id直接赋值
      avatar: '', // 发起人头像
      name: '', // 活动名称
      eventDate: '', // 开始日期
      eventTime: '18:30', // 开始时间
      eventTimee: '20:30', // 结束时间
      location: '', // 地点
      locationDetail: '', // 详细地点
      latitude: 0.0,   // 纬度
      longitude: 0.0,   // 经度
      totalParticipants: 2, // 活动总人数
      phoneNumber: '', // 联系方式
      type: 0, // 类型
      remarks: '', // 备注
      labels: '', // 标签
      limits: 0, // 限制
      visibility: true, // 可见性状态
      level: 0, // 水平
      feeMode: 0, // 费用模式
      fee: 0.0, // 活动费用
      penalty: true, // 爽约惩罚
      isTemplate: false, // 是否模板
    },
    dateRange: [], // 日期选择范围
    participantRange: [], // 活动总人数范围
    showMore: false, // 是否显示更多设置
    showFeeInput: false, // 控制活动费用输入框显示
    editProPath: '../../../',
    fromMap: false
  },

  onLoad(options) {
    // 从 URL 中接收传递的参数
    const location = decodeURIComponent(options.location || ''); // 地点名称
    const locationDetail = decodeURIComponent(options.locationDetail || ''); // 详细地址
    const latitude = decodeURIComponent(options.latitude || ''); // 纬度
    const longitude = decodeURIComponent(options.longitude || ''); // 经度

    // 初始化日期范围（今天到未来30天）
    const today = new Date();
    const todayDate = today.toISOString().slice(0, 10); // 格式为 YYYY-MM-DD

    // 获取今天是星期几
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    const todayWeek = weekDays[today.getDay()]; // 获取对应的星期名称
    // 默认活动名称
    const defaultName = `${todayWeek}晚`;

    const dateRange = Array.from({ length: 30 }, (_, i) => {
      const date = new Date(today.getTime() + i * 24 * 60 * 60 * 1000);
      return date.toISOString().slice(0, 10);
    });

    // 初始化人数范围（2~100）
    const participantRange = Array.from({ length: 99 }, (_, i) => i + 2);

    // 检查 options 是否包含 location 或 locationDetail
    const fromMap = !!(options.location || options.locationDetail);

    this.setData({
      'event.name': defaultName, // 默认设置为 "今天的星期 + 晚"
      'event.appId': app.globalData.currentUser.id,
      'event.avatar': app.globalData.currentUser.avatar,
      'event.eventDate': todayDate, // 默认设置为今天的日期
      'event.location': location, // 解码参数
      'event.locationDetail': locationDetail,
      'event.latitude': latitude,
      'event.longitude': longitude,
      fromMap, // 根据 options 判断是否从地图传入
      dateRange,
      participantRange
    });
  },
  onShow() {
    const pages = getCurrentPages();
    const prevPage = pages[pages.length - 2]; // 获取上一页
    if (prevPage && prevPage.data.event) {
      // 如果返回的数据有 event 字段（即模板数据），则更新表单内容
      this.setData({
        event: { ...this.data.event, ...prevPage.data.event }
      });
    }
  },
  /**
   * 选择地点
   */
  handleLocationSelect() {
    wx.chooseLocation({
      success: (res) => {
        // 更新地点和详细地点
        this.setData({
          'event.location': res.name || '', // 地点名称
          'event.locationDetail': res.address || '', // 详细地址
          'event.latitude': res.latitude || null, // 纬度
          'event.longitude': res.longitude || null, // 经度
        });
      },
      fail: (err) => {
        console.error('选择地点失败:', err);
        wx.showToast({
          title: '未选择地点',
          icon: 'none'
        });
      }
    });
  },


  /**
   * 输入地点
   */
  onLocationInput(e: WechatMiniprogram.Input) {
    this.setData({
      'event.location': e.detail.value
    });
  },

  /**
   * 输入详细地点
   */
  onLocationDetailInput(e: WechatMiniprogram.Input) {
    this.setData({
      'event.locationDetail': e.detail.value
    });
  },

  // 获取手机号
  handleGetPhoneNumber() {
    const phone = app.globalData.currentUser.phone;

    if (!phone) {
      // 未填写手机号资料
      wx.showModal({
        title: '提示',
        content: '请完善手机号资料',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({
              url: '/pages/editProfile/editProfile', // 跳转到编辑信息页面
            });
          }
        },
      });
    } else {
      // 已有手机号
      this.setData({
        'event.phoneNumber': phone, // 赋值给表单中的联系方式
      });

      wx.showToast({
        title: '已获取手机号',
        icon: 'success',
      });
    }
  },

  // 更新表单数据
  handleInputChange(e: WechatMiniprogram.BaseEvent) {
    const { field } = e.currentTarget.dataset;
    const value = e.detail.value;

    // 限制活动名称字数
    if (field === 'name' && value.length > 15) {
      wx.showToast({ title: '活动名称最多15个字', icon: 'none' });
      return;
    }

    // 限制备注字数
    if (field === 'remarks' && value.length > 800) {
      wx.showToast({ title: '备注最多800字', icon: 'none' });
      return;
    }

    if (field === 'totalParticipants') {
      // 选择人数时，根据索引获取实际的值
      const selectedParticipants = this.data.participantRange[value]; // 获取实际选中的人数
      this.setData({
        [`event.${field}`]: selectedParticipants, // 更新活动的总人数
      });
    } else if (field === 'visibility') {
      this.setData({
        [`event.${field}`]: value === 0 ? false : true,
      });
    } else {
      this.setData({
        [`event.${field}`]: value,
      });
    }
  },

  // 更新费用模式
  handleFeeModeChange(e: WechatMiniprogram.BaseEvent) {
    const feeMode = parseInt(e.detail.value, 10);
    this.setData({
      'event.feeMode': feeMode,
      showFeeInput: feeMode === 2, // 如果费用模式是AA制，显示费用输入框
    });
  },

  // 显示/隐藏更多设置
  toggleMoreSettings() {
    this.setData({
      showMore: !this.data.showMore,
    });
  },
  // 跳转到选择模板页面
  useTemplate() {
    wx.navigateTo({
      url: '../selectTemp/selectTemp', // 跳转到选择模板页面
    });
  },

  // 提交表单
  handleSubmit() {
    const { event } = this.data;

    // 必填项校验
    const requiredFields = [
      { field: 'name', label: '活动名称' },
      { field: 'eventDate', label: '开始日期' },
      { field: 'eventTime', label: '开始时间' },
      { field: 'eventTimee', label: '结束时间' },
      { field: 'location', label: '地点' },
      { field: 'locationDetail', label: '详细地点' },
      { field: 'totalParticipants', label: '活动总人数' },
      { field: 'phoneNumber', label: '联系方式' },
    ];

    for (const { field, label } of requiredFields) {
      if (!event[field]) {
        wx.showToast({ title: `请填写${label}`, icon: 'none' });
        return;
      }
    }

    // 验证时间逻辑：结束时间不能小于开始时间
    const startTimeParts = event.eventTime.split(':').map(Number); // 转换为 [小时, 分钟]
    const endTimeParts = event.eventTimee.split(':').map(Number); // 转换为 [小时, 分钟]
    const startMinutes = startTimeParts[0] * 60 + startTimeParts[1]; // 开始时间的总分钟数
    const endMinutes = endTimeParts[0] * 60 + endTimeParts[1]; // 结束时间的总分钟数

    if (endMinutes <= startMinutes) {
      wx.showToast({ title: '结束时间必须大于开始时间', icon: 'none' });
      return;
    }

    // 提交表单数据
    wx.request({
      url: `${app.globalData.url}/user/wx/createEvent`,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'X-Token': app.globalData.currentUser.token
      },
      data: event,
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          wx.showToast({ title: '活动创建成功', icon: 'success', duration: 500 });
          this.requestSubscribeMessage(Number(res.data.data));
          wx.navigateBack();
        } else {
          wx.showToast({ title: res.data.message || '活动创建失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '请求失败，请检查网络', icon: 'none' });
      },
    });
  },
  // 请求订阅消息授权
  requestSubscribeMessage(eventId:Number) {
    const templateIds = ['0y74iVIHCLCJJeS-zFL1Q90cFJNjNqQv8TzjMw-cuIQ', 'LVVo1OQ_oe6-hFSJ1yZtsB8odWdA4B8Qg5OdwBVVYWc'];
    wx.requestSubscribeMessage({
      tmplIds: templateIds,
      success(res) {
        // 发送报名成功消息
        if (res[templateIds[0]] === 'accept') {
          wx.request({
            url: `${app.globalData.url}/user/wx/sendJoinEventNotification`,
            method: 'POST',
            header: {
              'Content-Type': 'application/x-www-form-urlencoded',
              'X-Token': app.globalData.currentUser.token,
            },
            data: {
              userId: Number(app.globalData.currentUser.id),
              eventId: eventId
            },
            success: (res) => {
              if (res.statusCode === 200 && res.data.code === 0) {
                console.error('参加活动通知发送成功！');
              } else {
                console.error('参加活动通知发送失败！');
              }
            },
            fail: () => {
              console.error('网络问题，参加活动通知发送失败！');
            },
          });
        }
      },
      fail(err) {
        console.error('订阅消息授权失败', err);
      }
    });
  },
});
