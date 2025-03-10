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
    minPopupHeight: 300,    // 最小高度 300rpx
    popupHeight: 300,       // 当前弹窗高度，初始为最小高度
    startY: 0,              // 拖拽开始时的触摸 Y 坐标
    initialPopupHeight: 300,// 拖拽开始时的弹窗高度
    dragging: false,
    pageSize: 10,           // 每次加载的评论数量
    currentPage: 1,         // 当前加载的页数
    isLoading: false,       // 防止重复加载
    selectedCommentId: -1   // 记录选中的评论ID，-1 表示未选中
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

    // 预处理子评论：设置 visibleSubComments 字段（只显示前 2 条子评论）
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

  // 生成指定数量的主评论数据
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

  // 生成指定主评论下的子评论数据（随机生成 0 ~ 3 条）
  generateSubComments(parentId: number): SubComment[] {
    const randomLength = Math.floor(Math.random() * 4); // 可能值 0, 1, 2, 或 3
    return Array.from({ length: randomLength }, (_, j) => ({
      id: j + 1,
      parentId,
      name: `子评论者 ${j + 1}`,
      content: `子评论内容 ${j + 1}`
    }));
  },

  // 滑动到底部自动加载更多评论
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

    // 模拟网络请求延迟
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

  // 打开回复弹窗时重置高度
  openReplyPopup(e: any) {
    const replyTo = e.currentTarget.dataset.name || '帖子';
    this.setData({
      showReplyPopup: true,
      replyTo,
      popupHeight: this.data.minPopupHeight
    });
  },

  // 关闭回复弹窗
  closeReplyPopup() {
    this.setData({
      showReplyPopup: false,
      replyTo: '',
      popupHeight: this.data.minPopupHeight
    });
  },

  preventClose() {
    // 阻止点击事件冒泡，不做任何操作
  },

    // 开始拖拽：记录初始触摸位置和弹窗高度
    onTouchStart(e: any) {
      const touch = e.touches[0];
      this.setData({
        startY: touch.clientY,
        initialPopupHeight: this.data.popupHeight,
        dragging: true
      });
    },

  // 拖拽过程中，只允许向上拖动，扩展回复弹窗高度
  onTouchMove(e: any) {
    if (!this.data.dragging) return;
    const touch = e.touches[0];
    // 如果用户向上拖动（startY > currentY），delta 为正
    const delta = this.data.startY - touch.clientY;
    if (delta > 0) {
      this.setData({
        popupHeight: this.data.initialPopupHeight + delta
      });
    }
  },

  // 拖拽结束，停止拖拽状态
  onTouchEnd() {
    this.setData({ dragging: false });
    const closeThreshold = 500; // 50rpx 拖拽距离就足够关闭弹窗
    if (this.data.popupHeight - this.data.minPopupHeight >= closeThreshold) {
      this.closeReplyPopup();
    }
  },

  onReplyInput(e: any) {
    this.setData({ replyContent: e.detail.value });
  },

  // 发送回复：如果 selectedCommentId > 0，则为子评论回复，否则为主评论
  sendReply() {
    const replyContent = this.data.replyContent.trim();
    if (!replyContent) {
      wx.showToast({ title: '请输入回复内容', icon: 'none' });
      return;
    }

    let updatedComments = [...this.data.comments];

    if (this.data.selectedCommentId > 0) {
      // 添加子评论到指定的主评论中
      const parentIndex = updatedComments.findIndex(
        comment => comment.id === this.data.selectedCommentId
      );
      if (parentIndex !== -1) {
        const newSubComment: SubComment = {
          id: Date.now(),
          parentId: this.data.selectedCommentId,
          name: '当前用户',
          content: replyContent
        };
        // 插入到子评论数组最前面
        updatedComments[parentIndex].subComments.unshift(newSubComment);
        // 更新 visibleSubComments（只显示前两条）
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

      this.closeReplyPopup();
    }

    // 根据当前加载页数更新 visibleComments 数组
    const totalVisible = this.data.pageSize * this.data.currentPage;
    this.setData({
      comments: updatedComments,
      visibleComments: updatedComments.slice(0, totalVisible),
      showReplyPopup: false,
      replyContent: '',
      selectedCommentId: -1
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
    // 添加分享逻辑
    console.log('分享帖子');
  }
});
