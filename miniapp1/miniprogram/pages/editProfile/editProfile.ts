import { wxUser } from '../../../typings'
import { chooseAndUploadImage } from '../../utils/upload';

// 获取应用实例
const app = getApp<{ globalData: { qnurl: string, url: string, currentUser: wxUser } }>();

Page({
  data: {
    userData: {} as wxUser,
    expWidth: '0%', // 预设值
    genderOptions: ['未知', '男', '女'],
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad() {
    this.loadUserData();
  },

  /**
   * 加载用户数据
   */
  loadUserData() {
    const currentUser = app.globalData.currentUser;
    const width = (currentUser.exp / (currentUser.grade * 10)) * 100;
    this.setData({
      userData: { ...currentUser },
      expWidth: `${width}%`
    });
  },
  
  // 图片上传方法
  didPressChooesImage: function () {
    chooseAndUploadImage('user_avatars/', app)
      .then(res => {
        this.setData({
          'userData.avatar': res.url
        });
      })
      .catch(err => {
        wx.showToast({
          title: '上传失败',
          icon: 'none'
        });
        console.error('上传错误:', err);
      });
  },

  /**
   * 输入框变更事件
   */
  onInputChange(e: WechatMiniprogram.Input) {
    const field = e.currentTarget.dataset.field as keyof wxUser; // 获取字段名
    const value = e.detail.value.trim(); // 去除首尾空格

    // 正则校验：允许中文、英文、数字和标点符号，至少2个字符，最多15个字符
    const isValid = /^[a-zA-Z0-9\u4e00-\u9fa5\p{P}]{2,15}$/u.test(value);

    if (!isValid) {
      wx.showToast({
        title: '至少2字，最多15字,不包含非法字符哦',
        icon: 'none'
      });
      return;
    }

    // 如果校验通过，更新数据
    this.setData({
      [`userData.${String(field)}`]: value
    });
  },

  /**
   * 性别选择
   */
  onGenderChange(e: WechatMiniprogram.PickerChange) {
    const value = Number(e.detail.value);
    this.setData({
      'userData.gender': value
    });
  },

  /**
   * 日期选择
   */
  onDateChange(e: WechatMiniprogram.PickerChange) {
    this.setData({
      'userData.birthday': e.detail.value
    });
  },
  
  /**
   * 处理手机号输入
   * @param e 小程序输入事件
   */
  handlePhoneInput(e: WechatMiniprogram.Input) {
    let value = e.detail.value;

    // 过滤非数字字符
    value = value.replace(/\D/g, '');

    // 更新手机号数据
    this.setData({
      'userData.phone': value
    });
  },
  
  /**
   * 保存用户信息
   */
  onSave() {
    const { name, avatar, phone } = this.data.userData;
    if (!name || !avatar) {
      wx.showToast({
        title: '昵称和头像为必填项',
        icon: 'none'
      });
      return;
    }
    if(phone.length != 11){
      wx.showToast({
        title: '电话号码应该为11位',
        icon: 'none'
      });
      return;
    }

    // 保存到全局数据
    app.globalData.currentUser = { ...this.data.userData };
    // 保存到后端
    wx.request({
      url: app.globalData.url + '/user/wx/update',
      method: 'POST', // 使用 POST 方法
      data: {
        id: app.globalData.currentUser.id,
        userAccount: app.globalData.currentUser.name,
        avatarUrl: app.globalData.currentUser.avatar,
        gender: app.globalData.currentUser.gender,
        birthday: app.globalData.currentUser.birthday,
        description: app.globalData.currentUser.description,
        label: app.globalData.currentUser.label,
        phone: app.globalData.currentUser.phone,
        token: app.globalData.currentUser.token
      },
      header: {
        'Content-Type': 'application/json', // 设置请求头，通常为 JSON 格式
      },
      success: (res) => {
        if (res.statusCode === 200) {
          wx.showToast({
            title: '保存成功',
            icon: 'success'
          });

        } else {
          wx.showToast({
            title: '保存失败，请重试',
            icon: 'error'
          });
          console.error('服务器返回错误:', res);
        }
      },
      fail: (err) => {
        wx.showToast({
          title: '网络错误，请稍后再试',
          icon: 'error'
        });
        console.error('请求失败:', err);
      }
    });

    wx.navigateBack();
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