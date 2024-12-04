interface Comment {
  id: number;
  avatar: string;
  name: string;
  content: string;
  subComments: SubComment[];
}

interface SubComment {
  id: number;
  parentId: number;
  name: string;
  content: string;
}

Page({
  data: {
    post: {},
    comments: [] as Comment[],
    visibleComments: [] as Comment[],
    showMore: false,
    showReplyPopup: false,
    replyTo: '',
    replyContent: ''
  },

  onLoad(query: any) {
    const postId = query.id; // 模拟帖子数据
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

    const comments = Array.from({ length: 10 }, (_, i) => ({
      id: i + 1,
      avatar: `https://picsum.photos/50/50?random=${i + 1}`,
      name: `用户 ${i + 1}`,
      content: `主评论内容 ${i + 1}`,
      subComments: Array.from({ length: 3 }, (_, j) => ({
        id: j + 1,
        parentId: i + 1,
        name: `子评论者 ${j + 1}`,
        content: `子评论内容 ${j + 1}`
      }))
    }));

    this.setData({
      post,
      comments,
      visibleComments: comments.slice(0, 2),
      showMore: comments.length > 2
    });
  },

  viewAllComments() {
    this.setData({
      visibleComments: this.data.comments,
      showMore: false
    });
  },

  goToCommentDetail(e: any) {
    const commentId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/commentDetail/commentDetail?id=${commentId}`
    });
  },

  openReplyPopup(e: any) {
    const replyTo = e.currentTarget.dataset.name || '帖子';
    this.setData({
      showReplyPopup: true,
      replyTo
    });
  },

  onReplyInput(e: any) {
    this.setData({
      replyContent: e.detail.value
    });
  },

  sendReply() {
    const newComment = {
      id: Date.now(),
      avatar: 'https://picsum.photos/50/50?random=3',
      name: '当前用户',
      content: this.data.replyContent,
      subComments: []
    };

    const updatedComments = [newComment, ...this.data.comments];

    this.setData({
      comments: updatedComments,
      visibleComments: updatedComments.slice(0, 2),
      showReplyPopup: false,
      replyContent: ''
    });
  }
});
