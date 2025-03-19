import * as qiniuUploader from './qiniuUploader';

// 初始化七牛云相关配置
export function initQiniu(app: any) {
  const options: qiniuUploader.QiniuOptions = {
    region: 'SCN',
    uptokenURL: app.globalData.url+'/user/wx/uptoken',
    uptokenFunc: function () { return '' },
    domain: 'https://portal.qiniu.com/kodo/bucket/resource-v2?bucketName=ballplus',
    shouldUseQiniuFileName: false
  };
  qiniuUploader.init(options);
}

/**
 * 选择并上传图片到七牛云
 * @param prefix 文件夹前缀
 * @param app 小程序实例
 */
export function chooseAndUploadImage(prefix: string, app: any): Promise<{ uploadRes: any, fileName: string,  url: string }> {
  return new Promise((resolve, reject) => {
    // 初始化七牛云配置
    initQiniu(app);
    
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        if (!res.tempFiles || res.tempFiles.length === 0) {
          reject(new Error('未选择图片'));
          return;
        }

        const filePath = res.tempFiles[0].tempFilePath;
        const fileName = prefix + filePath.split('/').pop();

        wx.showLoading({
          title: '上传中...',
          mask: true
        });

        // 构造上传参数
        const uploadOptions: qiniuUploader.QiniuUploadOptions = {
          filePath: filePath,
          success: (uploadRes: any) => {
            wx.hideLoading();
            resolve({
              uploadRes,
              fileName,
              url: app.globalData.qnurl + fileName
            });
          },
          fail: (error: any) => {
            wx.hideLoading();
            reject(error);
          },
          options: {
            region: 'SCN',
            uptokenURL: app.globalData.url + '/user/wx/uptoken',
            domain: 'https://portal.qiniu.com/kodo/bucket/resource-v2?bucketName=ballplus',
            shouldUseQiniuFileName: false,
            key: fileName,
          }
        };

        // 开始上传
        qiniuUploader.upload(uploadOptions);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
} 