/// <reference path="./types/index.d.ts" />

interface EventData {
  appId: number; // 发起人id
  name: string; // 活动名称
  eventData: string; // 开始时间（日期）
  eventTime: string; // 开始时间（当天时间）
  eventTimee: string; // 结束时间（当天时间）
  location: string; // 地点
  locationDetail: string; // 详细地点
  totalParticipants: number; // 活动总人数
  participants: number; // 参加人数
  phoneNumber: number; // 联系方式
  type: number; // 类型（0娱乐，1训练，2对打，3比赛）
  remarks?: string; // 备注（可选）
  labels?: string; // 标签（可选）
  limits: number; // 限制（0无，1男，2女）
  visibility: boolean; // 可见性状态
  level: number; // 水平（0小白，1初学者，2业余，3中级，4高级，5专业）
  feeMode: number; // 费用模式（0未启用）
  fee: number; // 活动费用
  penalty: boolean; // 爽约惩罚
  isTemplate: boolean; // 是否模板
  state: number; // 状态(0报名中，1已结束，2已取消）
}


interface wxUser {
  id: string;              // 用户ID
  ballNumber: string;      // 唯一球号
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
  phone: string;          // 电话号码
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