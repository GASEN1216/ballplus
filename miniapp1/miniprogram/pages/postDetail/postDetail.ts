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
  likes: number;
}

Page({
  data: {
    post: {} as any,
    comments: [] as Comment[],
    visibleComments: [] as Comment[],
    showReplyPopup: false,
    replyTo: '',
    replyContent: '',
    // 用于输入状态下控制焦点
    autoFocus: false,
    // 分页相关
    pageSize: 10,
    currentPage: 1,
    isLoading: false,
    selectedCommentId: -1, // -1 表示新增主评论，否则为回复某条评论
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
      title: '帖子标题 10',
    };

    const comments = this.generateComments(30);

    // 预处理子评论，只显示前两条
    comments.forEach(comment => {
      comment.visibleSubComments = comment.subComments.length > 2
        ? comment.subComments.slice(0, 2)
        : comment.subComments;
    });

    this.setData({
      post,
      comments,
      visibleComments: comments.slice(0, this.data.pageSize)
    });
  },

  // 生成主评论数据
  generateComments(count: number): Comment[] {
    return Array.from({ length: count }, (_, i) => ({
      id: i + 1,
      avatar: `https://picsum.photos/50/50?random=${i + 1}`,
      name: `用户 ${i + 1}`,
      content: `主评论内容 ${i + 1}`,
      subComments: this.generateSubComments(i + 1),
      visibleSubComments: [] as SubComment[],
      likes: 0
    }));
  },

  // 生成子评论数据（随机0~3条）
  generateSubComments(parentId: number): SubComment[] {
    const randomLength = Math.floor(Math.random() * 4);
    return Array.from({ length: randomLength }, (_, j) => ({
      id: j + 1,
      parentId,
      name: `子评论者 ${j + 1}`,
      content: `子评论内容 ${j + 1}`
    }));
  },

  // 自动加载更多评论
  onReachBottom() {
    if (this.data.isLoading) return;
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
      console.log('没有更多评论了');
    }

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

  // 点击“点我发评论”进入回复状态
  handleTapCommentInput() {
    this.setData({
      showReplyPopup: true,
      autoFocus: true,
      replyTo: '帖子',  // 如果是回复主帖，可固定提示“帖子”，如回复评论则在 openReplyPopup 中传入对应名字
      replyContent: '',
      selectedCommentId: -1  // 新增主评论
    });

    // 延时确保输入框自动聚焦
    setTimeout(() => {
      wx.createSelectorQuery()
        .select('#replyInput')
        .fields({ properties: ['focus'] }, res => {
          if (!res.focus) {
            this.setData({ autoFocus: true });
          }
        })
        .exec();
    }, 200);
  },

  // 普通状态下点击某评论的回复按钮
  openReplyPopup(e: any) {
    const replyTo = e.currentTarget.dataset.name || '帖子';
    const commentId = e.currentTarget.dataset.id;
    this.setData({
      showReplyPopup: true,
      replyTo,
      autoFocus: true,
      replyContent: '',
      selectedCommentId: commentId
    });

    setTimeout(() => {
      wx.createSelectorQuery()
        .select('#replyInput')
        .fields({ properties: ['focus'] }, res => {
          if (!res.focus) {
            this.setData({ autoFocus: true });
          }
        })
        .exec();
    }, 200);
  },

  // 关闭回复状态
  closeReplyPopup() {
    this.setData({
      showReplyPopup: false,
      replyTo: '',
      replyContent: '',
      autoFocus: false,
      selectedCommentId: -1
    });
  },

  preventClose() {
    // 阻止点击事件冒泡
  },

  // 输入评论
  onReplyInput(e: any) {
    this.setData({ replyContent: e.detail.value });
  },

  onReplyBlur() {
    // 可选择失焦时做处理，此处直接不操作
    this.setData({ showReplyPopup: false });
  },

  onReplyFocus() {
    // 保证输入框自动聚焦
    this.setData({ autoFocus: true });
  },

  cancelClosePopup(){

  },

  // 发送评论或回复
  sendReply() {
    const replyContent = this.data.replyContent.trim();
    if (!replyContent) {
      wx.showToast({ title: '请输入回复内容', icon: 'none' });
      return;
    }

    let updatedComments = [...this.data.comments];

    if (this.data.selectedCommentId > 0) {
      // 添加子评论到对应主评论
      const parentIndex = updatedComments.findIndex(comment => comment.id === this.data.selectedCommentId);
      if (parentIndex !== -1) {
        const newSubComment: SubComment = {
          id: Date.now(),
          parentId: this.data.selectedCommentId,
          name: '当前用户',
          content: replyContent
        };
        updatedComments[parentIndex].subComments.unshift(newSubComment);
        updatedComments[parentIndex].visibleSubComments =
          updatedComments[parentIndex].subComments.length > 2
            ? updatedComments[parentIndex].subComments.slice(0, 2)
            : updatedComments[parentIndex].subComments;
      }
    } else {
      // 添加新的主评论
      const newComment: Comment = {
        id: Date.now(),
        avatar: 'https://picsum.photos/50/50?random=3',
        name: '当前用户',
        content: replyContent,
        subComments: [],
        visibleSubComments: [],
        likes: 0
      };
      updatedComments.unshift(newComment);
    }

    // 根据当前分页重新计算显示的评论
    const totalVisible = this.data.pageSize * this.data.currentPage;
    this.setData({
      comments: updatedComments,
      visibleComments: updatedComments.slice(0, totalVisible),
      showReplyPopup: false,
      replyContent: '',
      selectedCommentId: -1,
      autoFocus: false
    });
  },

  // 点赞评论
  likeComment(e: any) {
    const commentId = e.currentTarget.dataset.id;
    const updatedComments = this.data.comments.map(comment => {
      if (comment.id === commentId) {
        return { ...comment, likes: comment.likes + 1 };
      }
      return comment;
    });
    this.setData({
      comments: updatedComments,
      visibleComments: updatedComments.slice(0, this.data.pageSize * this.data.currentPage)
    });
  },

  sharePost() {
    console.log('分享帖子');
  },
});
