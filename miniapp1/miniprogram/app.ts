// app.ts
App<IAppOption>({
  globalData: {
    userInfo: undefined,  
    token: "",
  },

  onLaunch() {
    console.log("小程序启动");
  },
});
