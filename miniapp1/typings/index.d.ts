/// <reference path="./types/index.d.ts" />

interface IAppOption {
  globalData: {
    userInfo?: WechatMiniprogram.UserInfo,
    token: ""
  }
  userInfoReadyCallback?: WechatMiniprogram.GetUserInfoSuccessCallback,
}