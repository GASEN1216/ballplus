/// <reference path="./types/index.d.ts" />

interface IAppOption {
  globalData: {
    url: string,
    token: "",
    nickname: "",
    profilePic: '',
    isLoggedin: boolean;
    currentUser: {
      id: string,
      name: string,
      avatar: string
    }
  }
  loginReadyCallback?: () => void;
  getUrl: (path: string) => string;
  login: () => void;
  initializeGoEasy: () => void;
}