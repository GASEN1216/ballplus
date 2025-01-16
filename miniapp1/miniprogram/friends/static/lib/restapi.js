const app = getApp();
class RestApi {
  users = [];
  //群数据示例
  groups = [{
      id: 'group-a42b-47b2-bb1e-15e0f5f9a19a',
      name: '小程序交流群',
      avatar: '/friends/static/images/wx.png',
      userList: [
        '08c0a6ec-a42b-47b2-bb1e-15e0f5f9a19a',
        '3bb179af-bcc5-4fe0-9dac-c05688484649',
        'fdee46b0-4b01-4590-bdba-6586d7617f95',
        '33c3693b-dbb0-4bc9-99c6-fa77b9eb763f',
        '9'
      ],
    },
    {
      id: 'group-4b01-4590-bdba-6586d7617f95',
      name: 'UniApp交流群',
      avatar: '/friends/static/images/uniapp.png',
      userList: [
        '08c0a6ec-a42b-47b2-bb1e-15e0f5f9a19a',
        'fdee46b0-4b01-4590-bdba-6586d7617f95',
        '33c3693b-dbb0-4bc9-99c6-fa77b9eb763f', '9'
      ],
    },
    {
      id: 'group-dbb0-4bc9-99c6-fa77b9eb763f',
      name: 'GoEasy交流群',
      avatar: '/friends/static/images/goeasy.jpeg',
      userList: ['08c0a6ec-a42b-47b2-bb1e-15e0f5f9a19a', '3bb179af-bcc5-4fe0-9dac-c05688484649', '9'],
    },
  ];
  // 订单
  orders = [{
      id: '252364104325',
      url: '/friends/static/images/goods1-1.jpg',
      name: '青桔柠檬气泡美式',
      price: '￥23',
      count: 1
    },
    {
      id: '251662058022',
      url: '/friends/static/images/goods1-2.jpg',
      name: '咸柠七',
      price: '￥8',
      count: 2
    },
    {
      id: '250676186141',
      url: '/friends/static/images/goods1-3.jpg',
      name: '黑糖波波鲜奶茶',
      price: '￥12',
      count: 1
    }
  ];

  findUsers() {
    return this.users;
  };

  findFriends(user, callback) {
    // 检查是否有有效的用户ID
    if (!user || !user.id) {
      console.error('Invalid user object');
      return [];
    }

    // 初始化好友列表
    this.users = [];

    // 使用wx.request获取数据
    wx.request({
      url: `${app.globalData.url}` + '/user/wx/getFriends',
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser.token
      },
      data: {
        userId: app.globalData.currentUser.id
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.data) {
          // 将好友数据填充到 users 中
          this.users = res.data.data.map(friend => ({
            id: friend.userId,
            name: friend.userName,
            avatar: friend.avatar
          }));
          if (callback) {
            callback(this.users); // 调用回调并传递数据
          }
        } else {
          console.error('Failed to fetch friends:', res);
        }
      },
      fail: (err) => {
        console.error('Request failed:', err);
      }
    });
    return this.users;
  }

  findGroups(user) {
    return this.groups.filter((v) => v.userList.find((id) => id === user.id));
  }

  findUser(username, password) {
    return this.users.find((user) => user.name === username && user.password === password);
  }

  getOrderList() {
    return this.orders;
  }

  findGroupById(groupId) {
    return this.groups.find((group) => group.id === groupId);
  }

  findUserById(userId) {
    return this.users.find((user) => user.id === userId);
  }

  findGroupMembers(groupId) {
    let members = [];
    let group = this.groups.find(v => v.id === groupId);
    // 使用 Set 优化查找性能
    const groupUserIds = new Set(group.userList);

    // 遍历用户，找到属于该群组的成员
    this.users.forEach(user => {
      if (groupUserIds.has(user.id)) {
        members.push(user);
      }
    });
    return members;
  }

  findGroupMemberAvatars(groupId) {
    let avatars = [];
    let group = this.groups.find((v) => v.id === groupId);
    this.users.map((user) => {
      group.userList.forEach((userId) => {
        if (user.id === userId) {
          avatars.push(user.avatar);
        }
      });
    });
    return avatars;
  }
}

export default new RestApi();