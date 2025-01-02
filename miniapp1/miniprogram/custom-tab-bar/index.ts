Component({
  data: {
    "selected": 0,
    "color": "#666666",
    "selectedColor": "#ff0000",
    "backgroundColor": "#ffffff",
    "borderStyle": "black",
    "list": [
      {
        "pagePath": "../index/index",
        "text": "首页",
        "iconPath": "../icon/home-icon.png", 
        "selectedIconPath": "../icon/home-icon-selected.png" 
      },
      {
        "pagePath": "../profile/profile",
        "text": "个人资料",
        "iconPath": "../icon/profile-icon.png", 
        "selectedIconPath": "../icon/profile-icon-selected.png" 
      },
      {
        "pagePath": "../friends/friends",
        "text": "好友管理",
        "iconPath": "../icon/mine-icon.png", 
        "selectedIconPath": "../icon/friends-icon-selected.png" 
      }
    ]
  },
  attached() {
  },
  methods: {
    switchTab(e:any) {
      const data = e.currentTarget.dataset; // 获取点击项的数据
      const url = data.path; // 目标页面路径
      const index = data.index; // 当前点击的索引

      // 如果点击的 tab 当前已选中，则不执行任何操作
      if (this.data.selected === index) return;

      // 页面跳转
      wx.switchTab({
        url, // 目标页面路径
        success: () => {
          // 更新当前选中的 tab
          // this.setData({
          //   selected: index,
          // });
        },
        fail: (err) => {
          console.error("页面跳转失败", err); // 捕获跳转失败的错误
        },
      });
    }
  }
})