//请求路径地址
// Docker 一键部署时可通过环境变量覆盖（Vite 构建参数），不填则回退到线上默认值
const ToUrl={
    // dev环境
    url: import.meta.env.VITE_API_URL || 'https://wacyg.fun',
    // 本地环境
    // url:'http://localhost:8080',
    //ai路径
    aiUrl: import.meta.env.VITE_AI_URL || 'https://wacyg.fun:9000/api/chat',
    // 获取实例
    staUrl: import.meta.env.VITE_AI_STA_URL || 'https://wacyg.fun:9000/api/chat/instance',
    // 获取实例状态
    stadUrl: import.meta.env.VITE_AI_STAD_URL || 'https://wacyg.fun:9000/api/chat/instance'
}
export default ToUrl;
