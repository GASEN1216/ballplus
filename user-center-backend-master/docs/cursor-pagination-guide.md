# 游标分页API使用指南

## 简介

本文档介绍了后端API中新增的游标分页功能的使用方法。游标分页相比传统的基于页码的分页具有以下优势：

1. **性能更高**：直接基于数据库索引进行查询，避免了分页计算的开销
2. **数据一致性**：解决了传统分页中因数据插入/删除而导致的数据重复或丢失问题
3. **实时数据友好**：更适合处理不断更新的数据集
4. **移动端友好**：适合"加载更多"的交互模式

## API 说明

### 1. 获取帖子列表（游标分页）

**接口路径**：`/user/wx/getPostListWithCursor`

**请求方法**：GET

**请求参数**：

| 参数名    | 类型    | 必填 | 默认值  | 说明                                         |
|-----------|---------|------|---------|----------------------------------------------|
| cursor    | String  | 否   | null    | 游标值，首次请求不传，后续请求使用返回的nextCursor |
| pageSize  | Integer | 否   | 10      | 每页数据条数                                 |
| asc       | Boolean | 否   | false   | 是否升序，默认为false（降序）                |
| keyword   | String  | 否   | null    | 搜索关键词                                   |

**响应示例**：

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "postId": 123,
        "appId": 456,
        "appName": "张三",
        "avatar": "https://example.com/avatar.jpg",
        "grade": 2,
        "title": "帖子标题",
        "content": "帖子内容...",
        "picture": "https://example.com/image.jpg",
        "likes": 10,
        "comments": 5,
        "createTime": "2023-04-01T12:30:45",
        "updateTime": "2023-04-01T15:20:30",
        "updateContentTime": "2023-04-01T15:20:30"
      },
      // 更多帖子...
    ],
    "nextCursor": "2023-04-01T14:15:20",
    "hasMore": true
  },
  "message": "success"
}
```

### 2. 获取用户发布的帖子列表（游标分页）

**接口路径**：`/user/wx/getMyPostsWithCursor`

**请求方法**：POST

**请求参数**：

| 参数名    | 类型    | 必填 | 默认值  | 说明                                         |
|-----------|---------|------|---------|----------------------------------------------|
| userId    | Long    | 是   | -       | 用户ID                                       |
| cursor    | String  | 否   | null    | 游标值，首次请求不传，后续请求使用返回的nextCursor |
| pageSize  | Integer | 否   | 10      | 每页数据条数                                 |
| asc       | Boolean | 否   | false   | 是否升序，默认为false（降序）                |

**响应示例**：与帖子列表接口响应格式相同

### 3. 获取用户评论收到的回复列表（游标分页）

**接口路径**：`/user/wx/getMyCommentRepliesWithCursor`

**请求方法**：POST

**请求参数**：

| 参数名    | 类型    | 必填 | 默认值  | 说明                                         |
|-----------|---------|------|---------|----------------------------------------------|
| userId    | Long    | 是   | -       | 用户ID                                       |
| cursor    | String  | 否   | null    | 游标值，首次请求不传，后续请求使用返回的nextCursor |
| pageSize  | Integer | 否   | 10      | 每页数据条数                                 |
| asc       | Boolean | 否   | false   | 是否升序，默认为false（降序）                |

**响应示例**：

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "subCommentId": 123,
        "commentId": 456,
        "appId": 789,
        "appName": "李四",
        "avatar": "https://example.com/avatar2.jpg",
        "content": "评论内容...",
        "likes": 5,
        "createTime": "2023-04-01T13:45:20",
        "originalCommentContent": "原评论内容...",
        "parentCommentAppId": 456
      },
      // 更多回复...
    ],
    "nextCursor": "2023-04-01T13:40:15",
    "hasMore": true
  },
  "message": "success"
}
```

### 4. 资源列表查询（游标分页）

**接口路径**：`/resource/listWithCursor`

**请求方法**：POST

**请求参数**：

| 参数名         | 类型    | 必填 | 默认值  | 说明                                         |
|----------------|---------|------|---------|----------------------------------------------|
| cursorRequest  | Object  | 是   | -       | 游标分页请求对象                             |
| ├─ cursor      | String  | 否   | null    | 游标值，首次请求不传，后续请求使用返回的nextCursor |
| ├─ pageSize    | Integer | 否   | 10      | 每页数据条数                                 |
| ├─ asc         | Boolean | 否   | false   | 是否升序，默认为false（降序）                |
| request        | Object  | 是   | -       | 资源查询请求对象                             |
| ├─ type        | Integer | 否   | null    | 资源类型                                     |
| ├─ keyword     | String  | 否   | null    | 搜索关键词                                   |
| ├─ categoryId  | Long    | 否   | null    | 分类ID                                       |

### 5. 用户收藏列表（游标分页）

**接口路径**：`/resource/listFavoritesWithCursor`

**请求方法**：POST

**请求参数**：

| 参数名      | 类型    | 必填 | 默认值  | 说明                                         |
|-------------|---------|------|---------|----------------------------------------------|
| userId      | Long    | 是   | -       | 用户ID                                       |
| cursor      | String  | 否   | null    | 游标值，首次请求不传，后续请求使用返回的nextCursor |
| pageSize    | Integer | 否   | 10      | 每页数据条数                                 |
| asc         | Boolean | 否   | false   | 是否升序，默认为false（降序）                |

### 6. 用户参与活动列表（游标分页）

**接口路径**：`/event/getUserEventsWithCursor`

**请求方法**：POST

**请求参数**：

| 参数名      | 类型    | 必填 | 默认值  | 说明                                         |
|-------------|---------|------|---------|----------------------------------------------|
| userId      | Long    | 是   | -       | 用户ID                                       |
| cursor      | String  | 否   | null    | 游标值，首次请求不传，后续请求使用返回的nextCursor |
| pageSize    | Integer | 否   | 10      | 每页数据条数                                 |
| asc         | Boolean | 否   | false   | 是否升序，默认为false（降序）                |

## 前端使用示例

### 微信小程序示例

```javascript
// 首次加载数据
loadData() {
  this.setData({
    loading: true,
    posts: []
  });
  
  wx.request({
    url: 'https://api.example.com/user/wx/getPostListWithCursor',
    method: 'GET',
    data: {
      pageSize: 10
    },
    success: (res) => {
      if (res.data.code === 0) {
        this.setData({
          posts: res.data.data.records,
          nextCursor: res.data.data.nextCursor,
          hasMore: res.data.data.hasMore,
          loading: false
        });
      }
    }
  });
}

// 加载更多数据
loadMore() {
  if (!this.data.hasMore || this.data.loading) {
    return;
  }
  
  this.setData({ loading: true });
  
  wx.request({
    url: 'https://api.example.com/user/wx/getPostListWithCursor',
    method: 'GET',
    data: {
      cursor: this.data.nextCursor,
      pageSize: 10
    },
    success: (res) => {
      if (res.data.code === 0) {
        this.setData({
          posts: [...this.data.posts, ...res.data.data.records],
          nextCursor: res.data.data.nextCursor,
          hasMore: res.data.data.hasMore,
          loading: false
        });
      }
    }
  });
}
```

### WXML模板示例

```html
<view class="container">
  <block wx:for="{{posts}}" wx:key="postId">
    <post-item post="{{item}}" />
  </block>
  
  <view wx:if="{{loading}}" class="loading">加载中...</view>
  
  <view wx:if="{{!loading && hasMore}}" bindtap="loadMore" class="load-more">
    加载更多
  </view>
  
  <view wx:if="{{!loading && !hasMore && posts.length > 0}}" class="no-more">
    没有更多数据了
  </view>
</view>
```

## 通用前端处理逻辑

无论是加载帖子、评论、资源还是其他数据，游标分页的处理模式都是相似的：

1. **首次加载**：不传游标参数，从服务器获取第一页数据
2. **记录游标**：保存响应中的`nextCursor`值和`hasMore`状态
3. **加载更多**：传递之前保存的`nextCursor`，将新数据追加到现有数据列表
4. **判断结束**：根据`hasMore`状态决定是否显示"加载更多"按钮
5. **错误处理**：处理网络错误、服务器错误等异常情况

## 注意事项

1. 游标值是基于排序字段的值（例如创建时间或更新时间），格式通常为ISO日期时间字符串
2. 首次请求不需要传递cursor参数
3. 返回的nextCursor应在下次请求时作为cursor参数传递
4. hasMore字段表示是否还有更多数据，可用于前端判断是否显示"加载更多"按钮
5. 游标分页API与传统分页API共存，但传统分页API已被标记为@Deprecated，建议使用游标分页API
6. 所有游标分页响应均使用标准的`CursorPageResponse`格式，包含`records`、`nextCursor`和`hasMore`字段 