import { createApp } from 'vue'
import App from './App.vue'
import store from './store'
// 引入 Element Plus
import ElementPlus from 'element-plus'
// 引入 Element Plus 样式
import 'element-plus/dist/index.css'
import * as ElIcons from '@element-plus/icons-vue';
//引入路由器
import router from './router';
// 添加 Vuetify 样式
import vuetify from './plugins/vuetify.ts'
import 'vuetify/styles'
// 确保图标能正常显示
import '@mdi/font/css/materialdesignicons.css' 
// 引入移动端适配样式
import './styles/mobile.css'  

const app = createApp(App)

Object.keys(ElIcons).forEach((iconName) => {
    app.component(iconName, ElIcons[iconName as keyof typeof ElIcons]);
});
// 页面刷新时恢复 token
const token = localStorage.getItem('token');
const user = localStorage.getItem('user');
const avatar = localStorage.getItem('avatar');
const id = localStorage.getItem('id');
const roles = JSON.parse(localStorage.getItem('roles') || '[]');

if (token) store.commit('setToken', token);
if (user) store.commit('setUser', user);
if (avatar) store.commit('setAvatar', avatar);
if (id) store.commit('setId', id);
if (roles) store.commit('setRoles', roles);

// 初始化称号数据
const initHonoraryTitle = () => {
  const savedTitle = localStorage.getItem('honoraryTitle')
  if (savedTitle) {
    try {
      const titleData = JSON.parse(savedTitle)
      store.commit('setHonoraryTitle', titleData)
    } catch (error) {
      console.error('Failed to parse honorary title data:', error)
      localStorage.removeItem('honoraryTitle')
    }
  }
}

// 初始化称号数据
initHonoraryTitle()

app.use(router)
app.use(store)
app.use(ElementPlus)
app.use(vuetify)
app.mount('#app')