Page({
  data: {
    points: 0, // 用户当前赛点
    products: [
      { id: 1, name: '动态头像1', price: 100, image: 'https://avatars.githubusercontent.com/u/101633833?s=400&u=209fc93d51298b2b8aa2c61cf93c5adc493520cc&v=4' },
      { id: 2, name: '动态头像2', price: 200, image: 'https://avatars.githubusercontent.com/u/101633833?s=400&u=209fc93d51298b2b8aa2c61cf93c5adc493520cc&v=4' },
      { id: 3, name: '头像边框-金色', price: 300, image: 'https://avatars.githubusercontent.com/u/101633833?s=400&u=209fc93d51298b2b8aa2c61cf93c5adc493520cc&v=4' },
      { id: 4, name: '个人资料背景-蓝色', price: 400, image: 'https://avatars.githubusercontent.com/u/101633833?s=400&u=209fc93d51298b2b8aa2c61cf93c5adc493520cc&v=4' },
    ]
  },

  onLoad() {
    this.loadUserPoints();
  },

  // 加载用户当前赛点
  loadUserPoints() {
    const app = getApp<IAppOption>();
    this.setData({
      points: app.globalData.userPoints || 0
    });
  },

  // 兑换商品
  redeemItem(event: any) {
    const itemId = event.currentTarget.dataset.id;
    const selectedProduct = this.data.products.find(product => product.id === itemId);

    if (!selectedProduct) {
      wx.showToast({
        title: '商品不存在',
        icon: 'error'
      });
      return;
    }

    if (this.data.points < selectedProduct.price) {
      wx.showToast({
        title: '赛点不足，无法兑换',
        icon: 'none'
      });
      return;
    }

    // 模拟扣除赛点和兑换
    const app = getApp<IAppOption>();
    app.globalData.userPoints -= selectedProduct.price;
    this.setData({
      points: app.globalData.userPoints
    });

    wx.showToast({
      title: `成功兑换：${selectedProduct.name}`,
      icon: 'success'
    });
  }
});
