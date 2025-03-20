interface Resource {
  id: number;
  title: string;
  description: string;
  coverImage: string;
  type: string;
  views: number;
  likes: number;
  link: string;
  category: string;
  isFavorite?: boolean;
}

interface PageData {
  records: Resource[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

interface wxUser {
  id: string;
  token: string;
}

interface IAppOption {
  globalData: {
    qnurl: string;
    url: string;
    isLoggedin: boolean;
    currentUser: wxUser;
  }
}

export const app = getApp<IAppOption>();

Page({
  data: {
    categories: [
      { name: '全部', type: 'all' },
      { name: '我的收藏', type: 'favorites' },
      { name: '基础教程', type: 'basic' },
      { name: '进阶技巧', type: 'advanced' },
      { name: '比赛策略', type: 'strategy' },
      { name: '装备指南', type: 'equipment' },
      { name: '健身训练', type: 'fitness' }
    ],
    currentCategory: 0,
    searchQuery: '',
    resources: [] as Resource[],
    filteredResources: [] as Resource[],
    page: 1,
    size: 10,
    hasMore: true,
    total: 0,
    pages: 0
  },

  onLoad() {
    this.loadResources();
  },

  // 加载资源列表
  loadResources() {
    const { currentCategory, searchQuery, categories, page, size } = this.data;
    
    // 如果是"我的收藏"分类
    if (categories[currentCategory].type === 'favorites') {
      if (!app.globalData.isLoggedin) {
        wx.showToast({
          title: '请先登录',
          icon: 'none'
        });
        this.setData({
          resources: [],
          filteredResources: [],
          hasMore: false,
          total: 0,
          pages: 0
        });
        return;
      }

      // 直接请求收藏列表
      wx.request({
        url: `${app.globalData.url}/user/wx/resources/favorites`,
        method: 'GET',
        data: {
          userId: app.globalData.currentUser.id,
          page,
          size
        },
        header: {
          'X-Token': app.globalData.currentUser?.token
        },
        success: (res: any) => {
          if (res.statusCode === 200 && res.data.code === 0) {
            const pageData = res.data.data as PageData;
            const resourcesWithFavorite = pageData.records.map(resource => ({
              ...resource,
              isFavorite: true
            }));

            this.setData({
              resources: page === 1 ? resourcesWithFavorite : [...this.data.resources, ...resourcesWithFavorite],
              filteredResources: page === 1 ? resourcesWithFavorite : [...this.data.resources, ...resourcesWithFavorite],
              hasMore: pageData.current < pageData.pages,
              total: pageData.total,
              pages: pageData.pages
            });
          } else {
            wx.showToast({
              title: res.data.message || '加载失败',
              icon: 'none'
            });
          }
        },
        fail: () => {
          wx.showToast({
            title: '加载失败',
            icon: 'none'
          });
        }
      });
      return;
    }

    // 其他分类的原有逻辑
    const category = currentCategory === 0 ? '' : categories[currentCategory].type;
    wx.request({
      url: `${app.globalData.url}/user/wx/resources/`,
      method: 'GET',
      data: {
        category,
        keyword: searchQuery,
        page,
        size
      },
      header: {
        'X-Token': app.globalData.currentUser?.token
      },
      success: (res: any) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          const pageData = res.data.data as PageData;
          const hasMore = pageData.current < pageData.pages;
          
          if (app.globalData.isLoggedin && app.globalData.currentUser?.id) {
            this.getFavorites((favorites: number[]) => {
              const resourcesWithFavorite = pageData.records.map((resource: Resource) => ({
                ...resource,
                isFavorite: favorites.includes(resource.id)
              }));

              const sortedResources = resourcesWithFavorite.sort((a, b) => {
                if (a.isFavorite === b.isFavorite) {
                  return 0;
                }
                return a.isFavorite ? -1 : 1;
              });

              this.setData({
                resources: page === 1 ? sortedResources : [...this.data.resources, ...sortedResources],
                filteredResources: page === 1 ? sortedResources : [...this.data.resources, ...sortedResources],
                hasMore,
                total: pageData.total,
                pages: pageData.pages
              });
            });
          } else {
            this.setData({
              resources: page === 1 ? pageData.records : [...this.data.resources, ...pageData.records],
              filteredResources: page === 1 ? pageData.records : [...this.data.resources, ...pageData.records],
              hasMore,
              total: pageData.total,
              pages: pageData.pages
            });
          }
        } else {
          wx.showToast({
            title: res.data.message || '加载失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        });
      }
    });
  },

  // 获取收藏列表
  getFavorites(callback: (favorites: number[]) => void) {
    if (!app.globalData.isLoggedin || !app.globalData.currentUser?.id) {
      callback([]);
      return;
    }

    wx.request({
      url: `${app.globalData.url}/user/wx/resources/favorites`,
      method: 'GET',
      data: {
        userId: app.globalData.currentUser.id,
        page: 1,
        size: 100
      },
      header: {
        'X-Token': app.globalData.currentUser?.token
      },
      success: (res: any) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          callback(res.data.data.records.map((item: Resource) => item.id));
        } else {
          callback([]);
        }
      },
      fail: () => {
        callback([]);
      }
    });
  },

  // 切换收藏状态
  toggleFavorite(e: any) {
    if (!app.globalData.isLoggedin || !app.globalData.currentUser?.id) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }

    const id = e.currentTarget.dataset.id;
    const resource = this.data.resources.find(r => r.id === id);
    
    if (!resource) return;

    wx.request({
      url: `${app.globalData.url}/user/wx/resources/${id}/favorite`,
      method: resource.isFavorite ? 'DELETE' : 'POST',
      data: {
        userId: app.globalData.currentUser.id
      },
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser?.token
      },
      success: (res: any) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          const resources = this.data.resources.map(r => {
            if (r.id === id) {
              return { ...r, isFavorite: !r.isFavorite };
            }
            return r;
          });

          // 重新排序，确保收藏的资源始终在前面
          const sortedResources = resources.sort((a, b) => {
            if (a.isFavorite === b.isFavorite) {
              return 0;
            }
            return a.isFavorite ? -1 : 1;
          });

          this.setData({ 
            resources: sortedResources,
            filteredResources: sortedResources
          });
        } else {
          wx.showToast({
            title: res.data.message || '操作失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '操作失败',
          icon: 'none'
        });
      }
    });
  },

  // 搜索输入处理
  onSearchInput(e: any) {
    this.setData({
      searchQuery: e.detail.value,
      page: 1
    }, () => {
      this.loadResources();
    });
  },

  // 切换分类
  switchCategory(e: any) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      currentCategory: index,
      page: 1
    }, () => {
      this.loadResources();
    });
  },

  // 触底加载更多
  onReachBottom() {
    if (this.data.hasMore) {
      this.setData({
        page: this.data.page + 1
      }, () => {
        this.loadResources();
      });
    }
  },

  // 跳转到详情页
  navigateToDetail(e: any) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `../resourcesDetail/resourcesDetail?id=${id}`
    });
  },
});
