export const app = getApp<IAppOption>();

Page({
  data: {
    apiUrl: `${app.globalData.url}/user/wx/getPostDetail`,
    addCommentUrl: `${app.globalData.url}/user/wx/addComment`,
    post: {} as any,
    comments: [],
    visibleComments: [],
    showReplyPopup: false,
    replyTo: '',
    replyContent: '',
    // 用于输入状态下控制焦点
    autoFocus: false,
    // 分页相关
    pageSize: 100,
    currentPage: 1,
    isLoading: false,
    selectedCommentId: -1, // -1 表示新增主评论，否则为回复某条评论
  },

  onLoad(options) {
    const { id } = options; 
    wx.request({
      url: `${this.data.apiUrl}`,
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Token': app.globalData.currentUser.token,
      },
      data: { postId: id },
      success: res => {
        if (res.statusCode === 200 && res.data.code === 0) {
          const postDetail = res.data.data;
          // 假设后端返回的 postDetail 中包含 commentsList 字段，里面包含评论和对应的子评论数据
          this.setData({
            post: {
              id: postDetail.id,  // 或使用其它唯一标识
              appId: postDetail.appId,
              avatar: postDetail.avatar,
              name: postDetail.appName,
              time: this.formatTime(postDetail.createTime),
              content: postDetail.content,
              image: postDetail.picture,
              likes: postDetail.likes,
              comments: postDetail.comments,
              title: postDetail.title,
            },
            comments: postDetail.commentsList,
            visibleComments: postDetail.commentsList.slice(0, this.data.pageSize)
          });
        } else {
          wx.showToast({ title: res.data.message, icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '请求失败', icon: 'none' });
      }
    });
  },

    // 用于格式化时间，具体实现可根据实际需求调整
    formatTime(timeStr) {
      const date = new Date(timeStr);
      return date.toLocaleString();
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
      const parentIndex = updatedComments.findIndex(comment => comment.commentId === this.data.selectedCommentId);
      if (parentIndex !== -1) {
        const newSubComment = {
          userId: app.globalData.currentUser.id,
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
      let user = app.globalData.currentUser;
      // 添加新的主评论
      const newComment = {
        userId: user.id,
        avatar: user.avatar,
        appName: user.name,
        grade: user.grade,
        postId: this.data.post.id,
        content: replyContent,
      };
      updatedComments.unshift(newComment);

      wx.request({
        url: `${this.data.addCommentUrl}`, // 替换成你的后端接口
        method: 'POST',
        header: {
          'Content-Type': 'application/json',
          'X-Token': app.globalData.currentUser.token,
        },
        data: newComment,
        success: (res) => {
          if (res.statusCode === 200 && res.data.code === 0) {
            wx.showToast({
              title: "发送成功",
              icon: "none",
            });
          } else {
            wx.showToast({
              title: "发送失败",
              icon: "none",
            });
          }
        },
        fail: (err) => {
          wx.showToast({
            title: "网络错误，请稍后重试",
            icon: "none",
          });
          console.error("请求失败:", err);
        },
      });
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
    const commentId = e.currentTarget.dataset.commentid;
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
