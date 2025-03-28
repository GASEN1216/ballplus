// 获取应用实例
const getAppInstance = () => getApp();
const app = getAppInstance();

// 历史记录项接口
interface HistoryItem {
  id: number;
  changeAmount: number;
  type: string;
  description: string;
  createdTime: string;
}

Page({
  data: {
    historyList: [] as HistoryItem[],
    isLoading: true
  },
  
  onLoad() {
    this.loadScoreHistory();
  },
  
  // 加载赛点历史
  loadScoreHistory() {
    this.setData({ isLoading: true });
    
    wx.request({
      url: `${app.globalData.url}/user/wx/getScoreHistory`,
      method: 'GET',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser.token
      },
      data: {
        userId: app.globalData.currentUser.id
      },
      success: (res: any) => { 
        if (res.statusCode === 200 && res.data.code === 0) {
          // 格式化时间
          const formattedList = res.data.data.map((item: any) => {
            return {
              id: item.id,
              changeAmount: item.changeAmount,
              type: item.type,
              description: item.description,
              createdTime: this.formatTime(item.createdTime)
            };
          });
          
          this.setData({
            historyList: formattedList,
            isLoading: false
          });
        } else {
          console.error('获取赛点历史失败', res);
          wx.showToast({
            title: '获取历史记录失败',
            icon: 'none'
          });
          this.setData({ isLoading: false });
        }
      },
      fail: (err) => {
        console.error('请求失败', err);
        wx.showToast({
          title: '网络错误，请稍后再试',
          icon: 'none'
        });
        this.setData({ isLoading: false });
      }
    });
  },
  
  // 格式化时间
  formatTime(timeStr: string) {
    if (!timeStr) return '';
    
    try {
      const date = new Date(timeStr);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hour = date.getHours().toString().padStart(2, '0');
      const minute = date.getMinutes().toString().padStart(2, '0');
      
      return `${year}-${month}-${day} ${hour}:${minute}`;
    } catch (e) {
      console.error('日期格式化错误:', e);
      return timeStr;
    }
  }
}); 