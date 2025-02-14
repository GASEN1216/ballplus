export const app = getApp<IAppOption>();

Page({
  data: {
    // 后端接口相关
    apiUrl: `${app.globalData.url}/user/wx/getTemplateByPer`,
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

  // 选择模板
  selectTemplate(e: any) {
    const templateData = e.currentTarget.dataset.template;  // 获取选中的模板数据
    const pages = getCurrentPages();  // 获取当前页面栈
    const prevPage = pages[pages.length - 2];  // 获取上一页（即活动表单页）

    // 将模板数据传递给表单页
    prevPage.setData({
      event: { ...prevPage.data.event, ...templateData }
    });

    // 返回到表单页面
    wx.navigateBack();
  }
});
