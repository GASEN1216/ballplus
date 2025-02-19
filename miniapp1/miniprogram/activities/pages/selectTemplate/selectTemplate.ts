export const app = getApp<IAppOption>();

Page({
  data: {
    // 后端接口相关
    apiUrl: `${app.globalData.url}/user/wx/getTemplateByPer`,
    deleteTemplateUrl: `${app.globalData.url}/user/wx/deleteTemplateByPer`,
    templates: [],
  },

  onLoad() {
    const { apiUrl } = this.data;
    wx.request({
      url: `${apiUrl}`,
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser.token,
      },
      data: {
        userId: app.globalData.currentUser.id,
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {

          if (!res.data.data || res.data.data.length === 0) return;
          
          // 格式化时间，去掉秒数，只保留小时和分钟
          const formattedTemplates = res.data.data.map(template => {
            const formatTime = (time) => {
              return time.split(':').slice(0, 2).join(':');  // 只保留小时和分钟
            };

            // 格式化时间字段
            template.eventTime = formatTime(template.eventTime);
            template.eventTimee = formatTime(template.eventTimee);

            // 加上用户信息
            template.appId = app.globalData.currentUser.id;
            template.avatar = app.globalData.currentUser.avatar;

            return template;
          });
          this.setData({
            templates: formattedTemplates
          })
        } else {
          wx.showToast({
            title: '获取活动模板失败',
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

    })
  },

  // 长按模板显示删除弹窗
  onLongTapTemplate(e: any) {
    const templateId = e.currentTarget.dataset.template.id;
    wx.showModal({
      title: '删除模板',
      content: '确定要删除这个模板吗？',
      success: (res) => {
        if (res.confirm) {
          this.deleteTemplate(templateId);
        }
      }
    });
  },

    // 删除模板
    deleteTemplate(templateId: string) {
      const { deleteTemplateUrl } = this.data;
      const userId = app.globalData.currentUser.id;
  
      wx.request({
        url: deleteTemplateUrl,
        method: 'POST',
        header: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'X-Token': app.globalData.currentUser.token,
        },
        data: {
          userId: userId,
          templateId: templateId,
        },
        success: (res) => {
          if (res.statusCode === 200 && res.data.code === 0) {
            wx.showToast({
              title: '模板已删除',
              icon: 'success',
            });
            // 重新请求模板数据
            this.onLoad();
          } else {
            wx.showToast({
              title: '删除失败',
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

  // 选择模板
  selectTemplate(e: any) {
    const templateData = e.currentTarget.dataset.template;  // 获取选中的模板数据
    const pages = getCurrentPages();  // 获取当前页面栈
    const prevPage = pages[pages.length - 2];  // 获取上一页（即活动表单页）
    
    const location = prevPage.data.event.location;
    const locationDetail = prevPage.data.event.locationDetail;
    const latitude = prevPage.data.event.latitude;
    const longitude = prevPage.data.event.longitude;
    if(prevPage.data.fromMap) {
      templateData.location = location;
      templateData.locationDetail = locationDetail;
      templateData.latitude = latitude;
      templateData.longitude = longitude;
    }

    // 将模板数据传递给表单页
    prevPage.setData({
      event: { ...prevPage.data.event, ...templateData }
    });

    // 返回到表单页面
    wx.navigateBack();
  }
});
