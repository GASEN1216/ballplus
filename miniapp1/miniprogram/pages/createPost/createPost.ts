import { chooseAndUploadImage } from '../../utils/upload';

const app = getApp<any>();

Page({
  data: {
    title: '',         // 帖子标题
    content: '',       // 帖子内容
    imageUrl: '',      // 上传的图片URL
    canPublish: false, // 是否可以发布
  },

  onLoad() {
    // 页面加载时的初始化逻辑
  },

  // 监听标题输入
  onTitleInput(e: any) {
    const title = e.detail.value;
    this.setData({
      title,
      canPublish: this.checkCanPublish(title, this.data.content)
    });
  },

  // 监听内容输入
  onContentInput(e: any) {
    const content = e.detail.value;
    this.setData({
      content,
      canPublish: this.checkCanPublish(this.data.title, content)
    });
  },

  // 检查是否可以发布
  checkCanPublish(title: string, content: string) {
    return title.trim() !== '' && content.trim() !== '';
  },

  // 选择图片
  chooseImage() {
    chooseAndUploadImage('post/', app)
      .then(res => {
        this.setData({
          imageUrl: res.url
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

  // 预览图片
  previewImage() {
    wx.previewImage({
      current: this.data.imageUrl,
      urls: [this.data.imageUrl]
    });
  },

  // 删除图片
  deleteImage() {
    this.setData({
      imageUrl: ''
    });
  },

  // 发布帖子
  onPublish() {
    if (!this.data.canPublish) {
      wx.showToast({
        title: '请完善标题和内容',
        icon: 'none'
      });
      return;
    }

    const { title, content, imageUrl } = this.data;
    
    // 构造请求数据
    const payload = {
      addId: app.globalData.currentUser.id,       // 用户ID
      appName: app.globalData.currentUser.name,    // 用户名称
      avatar: app.globalData.currentUser.avatar,   // 用户头像
      grade: app.globalData.currentUser.grade,     // 用户等级
      title: title,
      content: content,
      picture: imageUrl
    };

    wx.showLoading({
      title: '发布中...',
      mask: true
    });

    wx.request({
      url: `${app.globalData.url}/user/wx/addPost`,
      method: 'POST',
      data: payload,
      header: {
        'Content-Type': 'application/json',
        'X-Token': app.globalData.currentUser.token
      },
      success: (res: any) => {
        wx.hideLoading();
        if (res.data.code === 0) {
          wx.showToast({
            title: '发布成功',
            icon: 'success'
          });
          // 延迟返回，让用户看到成功提示
          setTimeout(() => {
            wx.navigateBack({
              success: () => {
                // 通知前一页刷新
                const pages = getCurrentPages();
                if (pages.length >= 2) {
                  const prePage = pages[pages.length - 2];
                  if (prePage && prePage.onLoad) {
                    prePage.onLoad();
                  }
                }
              }
            });
          }, 1500);
        } else {
          wx.showToast({
            title: res.data.message || '发布失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        });
      }
    });
  }
}); 