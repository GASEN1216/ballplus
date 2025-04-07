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

// 定义 markersData 的类型
let markersData: Marker[] = [];

Page({
    data: {
        markers: [] as Marker[],
        latitude: '',
        longitude: '',
        textData: {},
        actPath: '../../../activities',
        searchQuery: '', // 搜索内容

        amapKey: '', // 存储后端返回的高德地图 key
        // 后端接口相关
        apiUrl: `${app.globalData.url}/user/wx/getEventByMap`,
        toEventDetailsPath: "../../../activities",

        // 活动数据
        activities: [] as any[], // 所有活动数据
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

    // 页面每次显示时触发
    onShow() {
        if (app.globalData.isLoggedin) {
            // 强制刷新数据
            this.setData({
                activities: [],    // 清空原有数据，避免数据不一致
            });

            this.fetchActivities(); // 重新拉取数据
        }
    },

    // 获取活动数据
    fetchActivities() {

        const { apiUrl } = this.data;

        wx.request({
            url: `${apiUrl}`,
            method: 'POST',
            header: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Token': app.globalData.currentUser.token,
            },
            data: {
                location: this.data.textData.name,
            },
            success: (res) => {

                if (res.statusCode === 200 && res.data.code === 0) {
                    let newActivities = res.data.data;

                    if (!newActivities || newActivities.length === 0) {
                        return; // 停止后续处理
                    }

                    // 只显示公开活动（visibility=1），过滤掉私人活动（visibilitye=0）
                    newActivities = newActivities.filter((activity: any) => activity.visibility === true);

                    if (newActivities.length === 0) {
                        this.setData({
                            activities: [],
                            markers: [this.data.centerMarker]
                        });
                        return; // 如果过滤后没有活动，停止后续处理
                    }

                    // 格式化日期和时间
                    newActivities = newActivities.map((activity: any) => {

                        const eventDate = new Date(activity.eventDate);
                        const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
                        const weekDay = weekDays[eventDate.getDay()]; // 获取周几

                        // 去掉 eventTime 的秒数
                        const eventTime = activity.eventTime.split(':').slice(0, 2).join(':');

                        // 去掉 eventTimee 的秒数
                        const eventTimee = activity.eventTimee.split(':').slice(0, 2).join(':');

                        return {
                            ...activity,
                            weekDay, // 添加周几信息
                            eventTime, // 格式化时间
                            eventTimee,
                        };
                    });

                    this.setData({
                        activities: [
                            ...newActivities, // 更新当前页数据
                        ],
                    });

                    const today = new Date();
                    today.setHours(0, 0, 0, 0); // 确保时间从当天00:00:00开始

                    let filtered = this.data.activities.filter(activity => {
                        const activityDate = new Date(activity.eventDate);
                        activityDate.setHours(0, 0, 0, 0); // 统一时间为00:00:00，防止时间影响比较
                        return activityDate.getTime() >= today.getTime(); // 只保留今天及以后的活动
                    });

                    this.setData({ activities: filtered });

                } else {
                    wx.showToast({
                        title: '获取活动数据失败',
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

    // 查看活动详情
    viewActivityDetail(e: any) {
        const activityId = e.currentTarget.dataset.id; // 获取活动ID
        wx.navigateTo({
            url: `${this.data.toEventDetailsPath}/pages/activityDetail/activityDetail?id=${activityId}`, // 跳转到活动详情页
        });
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
                wx.showToast({ title: '请确保搜索内容中包含"球"', icon: 'none' });
            }
        } else {
            const myAmapFun = new amapFile.AMapWX({ key: this.data.amapKey });
            this.loadMarkers(myAmapFun, "球");
        }
    },
    // 加载地图标记
    loadMarkers: function (myAmapFun: any, keywords: string) {
        myAmapFun.getPoiAround({
            iconPathSelected: 'http://ballplus.asia/icon/marker_checked.png',
            iconPath: 'http://ballplus.asia/icon/marker.png',
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
        this.setData({
            hasMoreData: true,
            activities: []
        });

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

        // 向后端拿取活动数据
        this.fetchActivities();
    },
    changeMarkerColor: function (data: Marker[], i: number) {
        const markers = data.map((marker, j) => ({
            ...marker,
            iconPath: j === i ? "http://ballplus.asia/icon/marker_checked.png" : "http://ballplus.asia/icon/marker.png"
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
    createActivity: function () {
        const { name, desc } = this.data.textData; // 获取当前的 textData
        const { latitude, longitude } = this.data; // 从 this.data 中获取 latitude 和 longitude
        wx.navigateTo({
            url: `${this.data.actPath}/pages/createActivity/createActivity?location=${encodeURIComponent(name)}&locationDetail=${encodeURIComponent(desc)}&latitude=${encodeURIComponent(latitude)}&longitude=${encodeURIComponent(longitude)}`
        });
    }


});