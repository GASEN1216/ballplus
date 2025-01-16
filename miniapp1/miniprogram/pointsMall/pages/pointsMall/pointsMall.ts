import { IAppOption } from '../../../../typings'
const app = getApp<IAppOption>();
Page({
  data: {
    score: 0, // 用户当前赛点（初始值）
    products: [
      { id: 1, name: 'dog1', price: 88, image: 'http://sunsetchat.top/user_avatars/gif/dog.gif', type: 'avatar' },
      { id: 2, name: 'dog2', price: 88, image: 'http://sunsetchat.top/user_avatars/gif/dog2.gif', type: 'avatar' },
      { id: 3, name: '加载中', price: 88, image: 'http://sunsetchat.top/user_avatars/gif/loading.gif', type: 'avatar' },
      { id: 4, name: 'duck', price: 88, image: 'http://sunsetchat.top/user_avatars/gif/duck.gif', type: 'avatar' },
      // { id: 5, name: '头像边框-金色', price: 150, image: 'https://example.com/gold-border.png', type: 'border' },
      // { id: 6, name: '个人资料背景-蓝色', price: 200, image: 'https://example.com/blue-background.png', type: 'background' },
      // { id: 7, name: '特殊道具-钻石', price: 300, image: 'https://example.com/diamond.png', type: 'item' },
      // { id: 8, name: '限量称号-无敌王者', price: 500, image: 'https://example.com/king-title.png', type: 'title' }
    ],
    ownedItems: [], // 用户拥有的物品 ID
    avatarInUse: '' // 当前穿戴的头像
  },

  onLoad() {
    this.loadUserPoints();
  },

  // 加载用户当前赛点
  loadUserPoints() {
    this.setData({
      score: app.globalData.currentUser.score || this.data.score,
      avatarInUse: app.globalData.currentUser.avatar || ''
    });
    // 发起请求获取用户拥有的物品数据
    wx.request({
      url: `${app.globalData.url}/user/wx/getItems`,
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser.token
      },
      data: {
        userId: app.globalData.currentUser.id
      },
      success: (res) => { 
        if (res.statusCode === 200 && typeof res.data === 'object' && 'code' in res.data && res.data.code === 0) {
          this.setData({
            ownedItems: res.data.data || [] // 假设返回的是 { items: [1, 2, 3] }
          });
        } else {
          console.error('获取用户物品失败', res);
        }
      },
      fail: (err) => {
        console.error('请求失败', err);
      }
    });
  },

    // 判断是否拥有某个物品
    isOwned(itemId:any) {
      // 遍历 ownedItems 数组，判断是否存在该 itemId
      for (let id of this.data.ownedItems) {
        if (id === itemId) {
          return true; // 如果找到对应的 ID，返回 true
        }
      }
      return false; // 未找到，返回 false
    },

  // 点击商品卡片，弹出确认框
  onProductTap(event: WechatMiniprogram.TouchEvent) {
    const itemId = event.currentTarget.dataset.id;
    const selectedProduct = this.data.products.find(product => product.id === itemId);

    if (!selectedProduct) {
      wx.showToast({
        title: '商品不存在',
        icon: 'error'
      });
      return;
    }

    if (this.data.ownedItems.includes(itemId)) {
      // 用户已拥有该物品
      if (this.data.avatarInUse === selectedProduct.image) {
        wx.showToast({
          title: '已穿戴',
          icon: 'none'
        });
      } else {
        wx.showModal({
          title: '是否穿戴',
          content: `您确定要穿戴【${selectedProduct.name}】吗？`,
          success: (res) => {
            if (res.confirm) {
              this.useItem(selectedProduct);
            }
          }
        });
      }
    } else {
      // 用户未拥有，显示购买确认框
      wx.showModal({
        title: '确认兑换',
        content: `您确定要兑换【${selectedProduct.name}】吗？`,
        success: (res) => {
          if (res.confirm) {
            this.redeemItem(selectedProduct);
          }
        }
      });
    }
  },
  // 使用物品
  useItem(selectedProduct: { id: number; name: string; price: number; image: string; type: string }) {
    app.globalData.currentUser.avatar = selectedProduct.image;
    this.setData({
      avatarInUse: selectedProduct.image
    });

    wx.showToast({
      title: '已穿戴成功',
      icon: 'success'
    });

    // 同步更新到后端
    wx.request({
      url: `${app.globalData.url}/user/wx/useItem`,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'X-Token': app.globalData.currentUser.token
      },
      data: {
        userId: app.globalData.currentUser.id,
        url: app.globalData.currentUser.avatar
      },
      success: (res) => {
        if (res.statusCode !== 200 || res.data.code !== 0) {
          console.error('同步穿戴信息失败', res);
        }
      },
      fail: (err) => {
        console.error('请求失败', err);
      }
    });
  },
  // 执行兑换逻辑
  redeemItem(selectedProduct: { id: number; name: string; price: number; image: string; type: string }) {
    if (this.data.score < selectedProduct.price) {
      wx.showToast({
        title: '赛点不足，无法兑换',
        icon: 'none'
      });
      return;
    }

    // 实现兑换成功后的逻辑
    // 更新用户头像
    app.globalData.currentUser.avatar = selectedProduct.image;
    // 同步更新用户信息（1.头像url，2.物品信息）
    // 发起 POST 请求同步到后端
    wx.request({
      url: `${app.globalData.url}/user/wx/addItem`,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'X-Token': app.globalData.currentUser.token // 添加 token 到请求头
      },
      data: {
        userId: app.globalData.currentUser.id,       // 用户 ID
        itemId: selectedProduct.id, // 物品id
        url: selectedProduct.image // url
      },
      success: (res) => {
        if (res.statusCode === 200 && typeof res.data === 'object' && 'code' in res.data && res.data.code === 0) {
          console.log('添加物品成功', res.data);
          // 模拟扣除赛点并兑换
          app.globalData.currentUser.score = app.globalData.currentUser.score - selectedProduct.price;
          this.setData({
            score: app.globalData.currentUser.score
          });
          // 弹出提示
          wx.showToast({
            title: `成功兑换：${selectedProduct.name}`,
            icon: 'success'
          });
          // 购买成功要更新一下信息
          this.loadUserPoints();
        } else {
          console.error('添加物品失败', res);
          wx.showToast({
            title: '购买物品失败',
            icon: 'error'
          });
        }
      },
      fail: (err) => {
        console.error('请求失败', err);
        wx.showToast({
          title: '网络错误，请稍后再试',
          icon: 'error'
        });
      }
    })
  }
});
