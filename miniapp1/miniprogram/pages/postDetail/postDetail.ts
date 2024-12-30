interface SubComment {
  id: number;
  parentId: number;
  name: string;
  content: string;
}

interface Comment {
  id: number;
  avatar: string;
  name: string;
  content: string;
  subComments: SubComment[];
  visibleSubComments: SubComment[];
  likes: number; // 新增字段用于记录点赞数
}

Page({
  data: {
    post: {},
    comments: [] as Comment[],
    visibleComments: [] as Comment[],
    showReplyPopup: false,
    replyTo: '',
    replyContent: '',
    pageSize: 10, // 每次加载的评论数量
    currentPage: 1, // 当前加载的页数
    isLoading: false, // 是否正在加载中，防止重复加载
    selectedCommentId: -1 // 新增字段用于记录选中的评论ID
  },

  onLoad(query: any) {
    const postId = query.id;
    const post = {
      id: postId,
      avatar: 'https://picsum.photos/150',
      name: 'Jack',
      time: '2023-12-07 08:50',
      content: '这个社区真棒，我希望能认识更多志同道合的朋友！',
      image: '',
      likes: 999,
      comments: 999,
      title: '帖子标题 10'
    };

    const comments = this.generateComments(30);

    // 预处理子评论，设置 visibleSubComments 字段
    comments.forEach(comment => {
      comment.visibleSubComments = comment.subComments.length > 2
        ? comment.subComments.slice(0, 2)
        : comment.subComments;
    });

    this.setData({
      post,
      comments,
      visibleComments: comments.slice(0, this.data.pageSize),
    });
  },

  // 生成评论数据
  generateComments(count: number): Comment[] {
    return Array.from({ length: count }, (_, i) => ({
      id: i + 1,
      avatar: `https://picsum.photos/50/50?random=${i + 1}`,
      name: `用户 ${i + 1}`,
      content: `主评论内容 ${i + 1}`,
      subComments: this.generateSubComments(i + 1),
      visibleSubComments: [] as SubComment[], // 新增字段用于存放前两条子评论
      likes: 0 // 初始化点赞数为0
    }));
  },

  // 生成子评论数据
  generateSubComments(parentId: number): SubComment[] {
    // 生成一个 0 到 3 之间的随机整数
    const randomLength = Math.floor(Math.random() * 4); // 0, 1, 2, 或 3

    return Array.from({ length: randomLength }, (_, j) => ({
      id: j + 1,
      parentId: parentId,
      name: `子评论者 ${j + 1}`,
      content: `子评论内容 ${j + 1}`
    }));
  },

  // 滑动到底部自动加载更多评论
  onReachBottom() {
    if (this.data.isLoading) {
      return;
    }

    this.setData({ isLoading: true });

    const { currentPage, pageSize, comments } = this.data;
    const nextPage = currentPage + 1;
    const start = (nextPage - 1) * pageSize;
    const end = start + pageSize;
    const moreComments = comments.slice(start, end);

    if (moreComments.length > 0) {
      this.setData({
        visibleComments: this.data.visibleComments.concat(moreComments),
        currentPage: nextPage
      });
    } else {
      console.log('No more comments to load');
    }

    // 模拟网络请求延迟，防止频繁触发
    setTimeout(() => {
      this.setData({ isLoading: false });
    }, 1000);
  },

  goToCommentDetail(e: any) {
    const commentId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/commentDetail/commentDetail?id=${commentId}`
    });
  },

  openReplyPopup(e: any) {
    const commentId = e.currentTarget.dataset.id;
    const replyTo = e.currentTarget.dataset.name || '帖子';
    this.setData({
      showReplyPopup: true,
      replyTo,
      selectedCommentId: commentId // 记录选中的评论ID
    });
  },
  closeReplyPopup() {
    this.setData({
      showReplyPopup: false,
      replyTo: '',
      selectedCommentId: -1 // 清除选中的评论ID
    });
  },
  onReplyInput(e: any) {
    this.setData({
      replyContent: e.detail.value
    });
  },

  sendReply() {
    const newComment: Comment = {
      id: Date.now(),
      avatar: 'https://picsum.photos/50/50?random=3',
      name: '当前用户',
      content: this.data.replyContent,
      subComments: [],
      visibleSubComments: [],
      likes: 0
    };

    let updatedComments = [...this.data.comments];
    if (this.data.selectedCommentId !== null) {
      // 如果有选中的评论，则添加到该评论的子评论中
      const parentIndex = updatedComments.findIndex(comment => comment.id === this.data.selectedCommentId);
      if (parentIndex !== -1) {
        const newSubComment: SubComment = {
          id: Date.now(),
          parentId: this.data.selectedCommentId,
          name: '当前用户',
          content: this.data.replyContent,
        };
        updatedComments[parentIndex].subComments.unshift(newSubComment as SubComment);
        updatedComments[parentIndex].visibleSubComments.unshift(newSubComment as SubComment);
      }
    } else {
      // 否则添加到主评论中
      updatedComments.unshift(newComment as Comment);
    }

    this.setData({
      comments: updatedComments,
      visibleComments: updatedComments.slice(0, this.data.pageSize),
      showReplyPopup: false,
      replyContent: ''
    });
  },
  likeComment(e: any) {
    const commentId = e.currentTarget.dataset.id;
    const updatedComments = this.data.comments.map(comment => {
      if (comment.id === commentId) {
        return {
          ...comment,
          likes: comment.likes + 1
        };
      }
      return comment;
    });

    this.setData({
      comments: updatedComments,
      visibleComments: updatedComments.slice(0, this.data.pageSize*this.data.currentPage)
    });
  },
  sharePost() {
    // 这里可以添加分享逻辑
    console.log('分享帖子');
  }
});
