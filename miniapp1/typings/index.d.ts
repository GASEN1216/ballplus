/// <reference path="./types/index.d.ts" />

import { VideoTextureLoader } from "XrFrame/loader";

interface wxUser {
  id: string;              // 用户ID
  name: string;            // 昵称
  avatar: string;          // 用户头像
  token: string;           // token
  gender: number;          // 性别 0 保密， 1女生， 2男生
  exp: number;             // 经验
  grade: number;           // 等级
  state: number;           // 状态
  unblockingTime: string;  // 解锁时间
  birthday: string;        // 生日
  credit: number;          // 信誉分
  score: number;           // 积分
  description: string;     // 个性签名
  label: string;         // 标签（多个标签）
}

interface IAppOption {
  globalData: {
    qnurl: string
    url: string
    isLoggedin: boolean
    currentUser: wxUser
  }
  loginReadyCallback?: () => void;
  getUrl: (path: string) => string;
  login: () => void;
  initializeGoEasy: () => void;
}