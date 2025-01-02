interface Resource {
  id: number;
  title: string;
  type: 'video' | 'text' | 'pdf';
  cover: string;
  url: string;
  description: string;
  likes: number;
  favorites: number;
}

Page({
  data: {
    resources: [] as Resource[],
    filteredResources: [] as Resource[]
  },

  onLoad() {
    this.loadFakeResources();
  },

  // 加载假数据
  loadFakeResources(): void {
    const fakeResources: Resource[] = [
      {
        id: 1,
        title: '🏀 篮球投篮技巧',
        type: 'video',
        cover: 'https://picsum.photos/1200/400?random=1',
        url: 'http://sunsetchat.top/study/basketball.mp4',
        description: '提高你的投篮命中率，观看完整教程。',
        likes: 120,
        favorites: 45
      },
      {
        id: 2,
        title: '🧘‍♀️ 瑜伽核心训练',
        type: 'video',
        cover: 'https://picsum.photos/1200/400?random=2',
        url: 'http://sunsetchat.top/study/yoga.mp4',
        description: '改善你的核心力量，适合初学者。',
        likes: 98,
        favorites: 60
      },
      {
        id: 3,
        title: '⚽ 足球基础训练',
        type: 'text',
        cover: 'https://picsum.photos/1200/400?random=3',
        url: 'https://dongqiudi.com/articles/4821323',
        description: '掌握足球基本技能，成为场上明星。',
        likes: 75,
        favorites: 30
      },
      {
        id: 4,
        title: '📄 健身计划表',
        type: 'pdf',
        cover: 'https://picsum.photos/1200/400?random=4',
        url: 'http://sunsetchat.top/study/fitness.pdf',
        description: '科学的健身计划，助你事半功倍。',
        likes: 85,
        favorites: 40
      }
    ];

    this.setData({ resources: fakeResources, filteredResources: fakeResources });
  },

  // 筛选资源
  filterResources(event: WechatMiniprogram.BaseEvent): void {
    const type = event.currentTarget.dataset.type as string;

    if (type === 'all') {
      this.setData({ filteredResources: this.data.resources });
    } else {
      const filtered = this.data.resources.filter((item) => item.type === type);
      this.setData({ filteredResources: filtered });
    }
  },

  // 跳转到详情页
  goToDetail(event: WechatMiniprogram.BaseEvent): void {
    const id = event.currentTarget.dataset.id;
    wx.navigateTo({
      url: `../resourcesDetail/resourcesDetail?id=${id}`
    });
  }
});
