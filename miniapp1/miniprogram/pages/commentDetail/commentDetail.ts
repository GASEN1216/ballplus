Page({
  data: {
    mainComment: {} as Comment
  },

  onLoad(query: any) {
    const commentId = query.id; // 从后端查询评论
    // 模拟假数据
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
    const mainComment = comments.find((c) => c.id === parseInt(commentId));

    if (mainComment) {
      this.setData({
        mainComment
      });
    } else {
      wx.showToast({
        title: '评论未找到',
        icon: 'error'
      });
    }
  }
});
