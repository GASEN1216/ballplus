// 引入 amap-wx.js 库
const amapFile = require('../../../libs/amap-wx.js');
export const app = getApp<IAppOption>();
// 定义 Marker 接口
interface Marker {
  id: number;
  latitude: number;
  longitude: number;
  iconPath: string;
  width?: number;
  height?: number;
  name?: string;
  address?: string;
}

// 定义 Activity 接口
interface Activity {
  time: string;
  host: string;
  currentParticipants: number;
  maxParticipants: number;
  content: string;
}

// 定义 markersData 的类型
let markersData: Marker[] = [];

let activities: Activity[] = [
  { time: '周一 10:00', host: '张三', currentParticipants: 5, maxParticipants: 10, content: '羽毛球比赛' },
  { time: '周二 14:00', host: '李四', currentParticipants: 8, maxParticipants: 15, content: '乒乓球友谊赛' },
  { time: '周三 18:00', host: '王五', currentParticipants: 3, maxParticipants: 8, content: '篮球训练营' }
];

Page({
  data: {
    markers: [] as Marker[],
    latitude: '',
    longitude: '',
    textData: {},
    actPath: '../../../activities',
    searchQuery: '', // 搜索内容
    activities: activities, // 约球活动信息
    amapKey: '' // 存储后端返回的高德地图 key
  },
  onLoad: function () {
    if (this.data.amapKey) {
      // 如果 amapKey 已经有值，直接初始化
      const myAmapFun = new amapFile.AMapWX({ key: this.data.amapKey });
      this.loadMarkers(myAmapFun, "球馆"); // 加载默认地点
    } else { 
      // 如果 amapKey 为空，从后端获取 key
      this.getMapKey().then((key) => {
        const myAmapFun = new amapFile.AMapWX({ key });
        this.setData({ amapKey: key }); // 保存 key 到 data
        this.loadMarkers(myAmapFun, "球馆"); // 加载默认地点
      });
    }
  },
  
  /**
   * 从后端获取高德地图 key
   */
  getMapKey: function (): Promise<string> {
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${app.globalData.url}/user/wx/getKey`, // 替换为后端接口地址
        method: 'GET',
        header: {
          'X-Token': app.globalData.currentUser.token,
        },
        success: (res) => {
          if (res.statusCode === 200 && res.data.code === 0) {
            resolve(res.data.data); // 返回后端的 key
          } else {
            wx.showToast({
              title: '获取地图Key失败',
              icon: 'none'
            });
            reject(new Error('Failed to get map key'));
          }
        },
        fail: (err) => {
          wx.showToast({
            title: '网络错误',
            icon: 'none'
          });
          reject(err);
        }
      });
    });
  },


  // 输入内容更新
  onSearchInput: function (e: { detail: { value: string } }) {
    this.setData({
      searchQuery: e.detail.value
    });
  },
// 搜索地点
onSearch: function () {
  const query = this.data.searchQuery.trim();
  if (query) {
    if (query.includes("球")) {
      const myAmapFun = new amapFile.AMapWX({ key: this.data.amapKey });
      this.loadMarkers(myAmapFun, query);
    } else {
      wx.showToast({ title: '请确保搜索内容中包含“球”', icon: 'none' });
    }
  } else {
    const myAmapFun = new amapFile.AMapWX({ key: this.data.amapKey });
    this.loadMarkers(myAmapFun, "球");
  }
},
  // 加载地图标记
  loadMarkers: function (myAmapFun: any, keywords: string) {
    myAmapFun.getPoiAround({
      iconPathSelected: '../../../icon/marker_checked.png',
      iconPath: '../../../icon/marker.png',
      querykeywords: keywords,
      success: (data: { markers: Marker[] }) => {
        markersData = data.markers;
        this.setData({
          markers: markersData,
          latitude: markersData[0]?.latitude.toString() || '',
          longitude: markersData[0]?.longitude.toString() || ''
        });
        this.showMarkerInfo(markersData, 0);
      },
      fail: (info: { errMsg: string }) => {
        wx.showModal({ title: info.errMsg });
      }
    });
  },
  makertap: function (e: { markerId: number }) {
    const id = e.markerId;
    this.showMarkerInfo(markersData, id);
    this.changeMarkerColor(markersData, id);
  },
  showMarkerInfo: function (data: Marker[], i: number) {
    this.setData({
      latitude: data[i]?.latitude.toString(),
      longitude: data[i]?.longitude.toString(),
      textData: {
        name: data[i]?.name || '',
        desc: data[i]?.address || ''
      }
    });
  },
  changeMarkerColor: function (data: Marker[], i: number) {
    const markers = data.map((marker, j) => ({
      ...marker,
      iconPath: j === i ? "../../../icon/marker_checked.png" : "../../../icon/marker.png"
    }));
    this.setData({ markers });
  },
  goMap: function () {
    const latitude = Number(this.data.latitude);
    const longitude = Number(this.data.longitude);
    wx.openLocation({
      latitude,
      longitude,
      scale: 18
    });
  },
  showActivityDetail: function (e: WechatMiniprogram.BaseEvent<{ index: number }>) {
    const index = e.currentTarget.dataset.index;
    const activity = this.data.activities[index];
    wx.navigateTo({
      url: `${this.data.actPath}/pages/activity_detail/activity_detail?activity=${encodeURIComponent(JSON.stringify(activity))}`
    });
  },
  createActivity: function() {
    const { name, desc } = this.data.textData; // 获取当前的 textData
    wx.navigateTo({
      url: `${this.data.actPath}/pages/createActivity/createActivity?location=${encodeURIComponent(name)}&locationDetail=${encodeURIComponent(desc)}`
    });
  }
  
});