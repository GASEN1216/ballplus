import { IAppOption } from '../../../../typings'

const app = getApp<IAppOption>();

Page({
    data: {
        creditInfo: {
            credit: 0,
            level: 0,
            levelName: ''
        },
        creditHistory: []
    },

    onLoad() {
        this.loadCreditInfo();
        this.loadCreditHistory();
    },

    // 加载信誉分详情
    loadCreditInfo() {
        wx.showLoading({
            title: '加载中...',
            mask: true
        });

        wx.request({
            url: `${app.globalData.url}/user/wx/getCreditInfo`,
            method: 'GET',
            header: {
                'X-Token': app.globalData.currentUser.token,
            },
            data: {
                userId: app.globalData.currentUser.id
            },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    const creditData = res.data.data;

                    // 计算级别名称
                    let levelName = '信用较差';
                    if (creditData.credit >= 90) {
                        levelName = '优秀球友';
                    } else if (creditData.credit >= 80) {
                        levelName = '信用良好';
                    } else if (creditData.credit >= 70) {
                        levelName = '正常信用';
                    } else if (creditData.credit >= 60) {
                        levelName = '信用一般';
                    }

                    // 计算级别数字
                    let level = 1;
                    if (creditData.credit >= 90) {
                        level = 5;
                    } else if (creditData.credit >= 80) {
                        level = 4;
                    } else if (creditData.credit >= 70) {
                        level = 3;
                    } else if (creditData.credit >= 60) {
                        level = 2;
                    }

                    this.setData({
                        creditInfo: {
                            credit: creditData.credit,
                            level: level,
                            levelName: levelName
                        }
                    });
                } else {
                    wx.showToast({
                        title: '获取信誉信息失败',
                        icon: 'none'
                    });
                }
            },
            fail: () => {
                wx.showToast({
                    title: '网络错误，请重试',
                    icon: 'none'
                });
            },
            complete: () => {
                wx.hideLoading();
            }
        });
    },

    // 加载信誉分变动历史
    loadCreditHistory() {
        wx.request({
            url: `${app.globalData.url}/user/wx/getCreditHistory`,
            method: 'GET',
            header: {
                'X-Token': app.globalData.currentUser.token,
            },
            data: {
                userId: app.globalData.currentUser.id
            },
            success: (res: any) => {
                if (res.statusCode === 200 && res.data.code === 0) {
                    // 处理日期格式，只保留日期部分
                    const formattedHistory = res.data.data.map((item: any) => {
                        if (item.createTime) {
                            item.createTime = item.createTime.split(' ')[0];
                        }
                        return item;
                    });

                    this.setData({
                        creditHistory: formattedHistory
                    });
                } else {
                    console.error('获取信誉分历史记录失败:', res);
                    // 设置为空数组以显示暂无记录
                    this.setData({
                        creditHistory: []
                    });
                }
            },
            fail: (err) => {
                console.error('请求信誉分历史记录接口失败:', err);
                // 设置为空数组以显示暂无记录
                this.setData({
                    creditHistory: []
                });
            }
        });
    },

    // 下拉刷新
    onPullDownRefresh() {
        this.loadCreditInfo();
        this.loadCreditHistory();
        // 停止下拉刷新动画
        wx.stopPullDownRefresh();
    }
}); 