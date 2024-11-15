/// <reference path="./types/index.d.ts" />

interface IAppOption {
  globalData: {
    url: string,
    token: "",
    nickname: "",
    profilePic: '',
    isLoggedin: boolean;
  }
  loginReadyCallback?: () => void;
  getUrl: (path: string) => string;
  login: () => void;
}