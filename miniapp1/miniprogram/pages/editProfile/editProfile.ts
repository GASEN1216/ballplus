import { wxUser } from '../../../typings'
import * as qiniuUploader from '../../utils/qiniuUploader';
// 获取应用实例
const app = getApp<{ globalData: { qnurl: string, url: string, currentUser: wxUser } }>();

// 初始化七牛云相关配置
function initQiniu() {
  const options: qiniuUploader.QiniuOptions = {
    // bucket所在区域，这里是华北区。ECN, SCN, NCN, NA, ASG，分别对应七牛云的：华东，华南，华北，北美，新加坡 5 个区域
    region: 'SCN',

    /**
     * 受限于七牛云安全机制，上传文件之前必须获取 uploadToken，由于生成 uploadToken 需要隐私数据，本 SDK 提供三种获取方法：
     * 1. upToken 直接赋值（本例使用）
     * 2. 从指定 URL 处获取
     * 3. 执行函数 upTokenFunc 获取
     * (后端获取 token 见 https://developer.qiniu.com/kodo/1289/nodejs)
     *
     *  注意：此处的 region、domain 与生成 uploadToken 时提交的 bucket 对应，否则上传图片时可能出现 401 expired token
     */
    uptoken: '',
    uptokenURL: 'http://10.45.4.53:8080/user/test/uptoken',
    uptokenFunc: function () { return '' },

    // 后端生成 uploadToken 时使用的 bucket 所分配的域名。 https://portal.qiniu.com/kodo/bucket/overview?bucketName=cregskin-static-pictures
    domain: 'https://portal.qiniu.com/kodo/bucket/resource-v2?bucketName=ballplus',

    // qiniuShouldUseQiniuFileName 如果是 true，则文件的 key 由 qiniu 服务器分配（全局去重）。如果是 false，则文件的 key 使用微信自动生成的 filename。出于初代sdk用户升级后兼容问题的考虑，默认是 false。
    // 微信自动生成的 filename较长，导致fileURL较长。推荐使用{qiniuShouldUseQiniuFileName: true} + "通过fileURL下载文件时，自定义下载名" 的组合方式。
    // 自定义上传key 需要两个条件：1. 此处shouldUseQiniuFileName值为false。 2. 通过修改qiniuUploader.upload方法传入的options参数，可以进行自定义key。（请不要直接在sdk中修改options参数，修改方法请见demo的index.js）
    // 通过fileURL下载文件时，自定义下载名，请参考：七牛云“对象存储 > 产品手册 > 下载资源 > 下载设置 > 自定义资源下载名”（https://developer.qiniu.com/kodo/manual/1659/download-setting）。本sdk在README.md的"常见问题"板块中，有"通过fileURL下载文件时，自定义下载名"使用样例。
    shouldUseQiniuFileName: false
  };
  // 将七牛云相关配置初始化进本sdk
  qiniuUploader.init(options);
}


Page({
  data: {
    // 图片上传（从相册）返回对象。上传完成后，此属性被赋值
    imageObject: {},
    // 图片上传（从相册）进度对象。开始上传后，此属性被赋值
    imageProgress: {},
    userData: {} as wxUser,
    expWidth: '0%', // 预设值
    genderOptions: ['未知', '男', '女'],
    // 此属性在qiniuUploader.upload()中被赋值，用于中断上传
    cancelTask: function () { }
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
  // 图片上传（从相册）方法
  didPressChooesImage: function () {
    this._didPressChooesImage();
  },
  // 图片上传（从相册）方法
  _didPressChooesImage() {
    // 初始化七牛云配置
    initQiniu();
    // 置空messageFileObject，否则在第二次上传过程中，wxml界面会存留上次上传的信息
    this.setData({
      'imageObject': {},
      'imageProgress': {}
    });
    const that = this;
    // 微信 API 选择图片（从相册）
    wx.chooseMedia({ // https://developers.weixin.qq.com/miniprogram/dev/api/media/video/wx.chooseMedia.html
      // 最多可以选择的图片张数。目前本sdk只支持单图上传，若选择多图，只会上传第一张图
      count: 1,
      mediaType: ['image'],
      success: function (res) {
        // 拿取图片名，并加入七牛云文件夹前缀
        let filePath = res.tempFiles[0].tempFilePath;
        let fileName = 'user_avatars/' + filePath.split('/').pop();

        // 向七牛云上传
        const uploadOptions: qiniuUploader.QiniuUploadOptions = {
          filePath: res.tempFiles[0].tempFilePath,
          success: (res) => {
            that.setData({
              'imageObject': res,
              'userData.avatar': app.globalData.qnurl + fileName
            });
          },
          fail: (error) => {
            console.error('error: ' + JSON.stringify(error));
          },
          options:
          {
            region: 'SCN', // 华北区
            uptokenURL: 'http://10.45.4.53:8080/user/test/uptoken',
            domain: 'https://portal.qiniu.com/kodo/bucket/resource-v2?bucketName=ballplus',
            shouldUseQiniuFileName: false,
            key: fileName,
          },
          progress: (progress) => {
            that.setData({
              'imageProgress': progress
            });
            // console.log('上传进度', progress.progress);
            // console.log('已经上传的数据长度', progress.totalBytesSent);
            // console.log('预期需要上传的数据总长度', progress.totalBytesExpectedToSend);
          },

          cancelTask: that.data.cancelTask
        }
        //console.log(uploadOptions);
        qiniuUploader.upload(uploadOptions);
      }
    })
  },

  /**
   * 输入框变更事件
   */
  onInputChange(e: WechatMiniprogram.Input) {
    const field = e.currentTarget.dataset.field as keyof wxUser; // 获取字段名
    const value = e.detail.value.trim(); // 去除首尾空格

    // 正则校验：只允许中文、英文、数字，至少2个字符
    const isValid = /^[a-zA-Z0-9\u4e00-\u9fa5]{2,15}$/.test(value);

    if (!isValid) {
      wx.showToast({
        title: '请输入至少2个字符，且不能包含非法字符',
        icon: 'none'
      });
      return;
    }

    // 如果校验通过，更新数据
    this.setData({
      [`userData.${field}`]: value
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
   * 保存用户信息
   */
  onSave() {
    const { name, avatar } = this.data.userData;
    if (!name || !avatar) {
      wx.showToast({
        title: '昵称和头像为必填项',
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
  }
});
