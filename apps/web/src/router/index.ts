import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router'; // 使用 type 导入
import store from '../store';
import { ElMessage } from 'element-plus';

const routes: RouteRecordRaw[] = [  //使用RouteRecordRaw 类型
  {
    path: '/',
    redirect: '/bmgf/home'
  },
  {
    path: '/bmgf/login',
    name: 'bmgfLogin',
    component: () => import('@/components/Login_Register.vue')
  },
  {
    path: '/bmgf/news/:id',
    name: 'bmgfNewsDetail',
    component: () => import('@/components/news/NewsDetail.vue')
  },
  {
    path: '/bmgf/admin',
    name: 'bmgfAdminLayout',
    component: () => import('@/components/backMange/Root/root.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'bmgfAdminDefault',
        redirect: 'home'
      },
      {
        path: 'home',
        name: 'bmgfAdminHome',
        component: () => import('@/components/backMange/home.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'comment',
        name: 'comment',
        component: () => import('@/components/backMange/DateMange/CommentMange.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'data',
        name: 'data',
        component: () => import('@/components/backMange/UnderMange/DataMange.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'user',
        name: 'user',
        component: () => import('@/components/backMange/UserMange/UserMange.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'video',
        name: 'video',
        component: () => import('@/components/backMange/DateMange/VideoMange.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'bug',
        name: 'bug',
        component: () => import('@/components/backMange/SystemMange/BugMange.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'category',
        name: 'category',
        component: () => import('@/components/backMange/SystemMange/Category.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'ques',
        name: 'ques',
        component: () => import('@/components/backMange/SystemMange/Quesstion.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'reword',
        name: 'reword',
        component: () => import('@/components/backMange/UserMange/Reword.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'announcement',
        name: 'announcement',
        component: () => import('@/components/backMange/DateMange/AnnouncementMange.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'news',
        name: 'news',
        component: () => import('@/components/backMange/DateMange/NewsMange.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'trend',
        name: 'trend',
        component: () => import('@/components/backMange/UnderMange/trend.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'settings',
        name: 'settings',
        component: () => import('@/components/backMange/settings.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'help',
        name: 'help',
        component: () => import('@/components/backMange/help.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'document',
        name: 'document',
        component: () => import('@/components/backMange/document.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/components/backMange/profile.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'feedback',
        name: 'feedback',
        component: () => import('@/components/backMange/SystemMange/FeedbackMange.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
      }
    ]
  },
  {
    path: '/bmgf',
    name: 'bmgfRoot',
    component: () => import('@/components/home/Root/Root.vue'),
    children: [
      { 
        path: 'home', 
        name: 'bmgfHome', 
        component: () => import('@/components/home/HomePage.vue'),
        meta: { requiresUser: true, excludeAdmin: true }
      },
      { path: 'about', name: 'bmgfAbout', component: () => import('@/components/home/aboutUs.vue') },
      { path: 'bug-hole', name: 'bmgfBugHole', component: () => import('@/components/home/BugHole.vue') },
      { path: 'app', name: 'bmgfApp', component: () => import('@/components/app_download/Dow.vue') },
      { path: 'community', name: 'bmgfCommunity', component: () => import('@/components/home/ConunityTalk.vue') },
      { path: 'private', name: 'bmgfPrivate', component: () => import('@/components/home/Pricate.vue'), meta: { requiresAuth: true, requiresUser: true } },
      { path: 'attack', name: 'bmgfAttack', component: () => import('@/components/home/Atack.vue'), meta: { requiresAuth: true, requiresUser: true } },
      
      // 新闻相关路由
      { 
        path: 'news', 
        name: 'bmgfNewsList', 
        component: () => import('@/components/news/NewsDetail.vue')
      },
      
      // 实战工具路由重组
      {
        path: 'tools',
        name: 'bmgfTools',
        component: () => import('@/components/home/Pricate.vue'),
        meta: { requiresAuth: true, requiresUser: true }
      },
      {
        path: 'tools/cms',
        name: 'bmgfToolsCms',
        component: () => import('@/components/home/Pricate_until/CmsTool.vue'),
        meta: { requiresAuth: true, requiresUser: true }
      },
      {
        path: 'tools/base64',
        name: 'bmgfToolsBase64',
        component: () => import('@/components/home/Pricate_until/Base64.vue'),
        meta: { requiresAuth: true, requiresUser: true }
      },
      {
        path: 'tools/ip-finder',
        name: 'bmgfToolsIpFinder',
        component: () => import('@/components/home/Pricate_until/FindIp.vue'),
        meta: { requiresAuth: true, requiresUser: true }
      },
      {
        path: 'tools/hash',
        name: 'bmgfToolsHash',
        component: () => import('@/components/home/Pricate_until/CreHash.vue'),
        meta: { requiresAuth: true, requiresUser: true }
      },
      {
        path: 'tools/password-check',
        name: 'bmgfToolsPasswordCheck',
        component: () => import('@/components/home/Pricate_until/CheckPwd.vue'),
        meta: { requiresAuth: true, requiresUser: true }
      },
      {
        path: 'tools/subdomain',
        name: 'bmgfToolsSubdomain',
        component: () => import('@/components/home/Pricate_until/Subdomain.vue'),
        meta: { requiresAuth: true, requiresUser: true }
      },
      {
        path: 'tools/port-scan',
        name: 'bmgfToolsPortScan',
        component: () => import('@/components/home/Pricate_until/PortScan.vue'),
        meta: { requiresAuth: true, requiresUser: true }
      },
      {
        path: 'tools/url-encode',
        name: 'bmgfToolsUrlEncode',
        component: () => import('@/components/home/Pricate_until/UrlEncode.vue'),
        meta: { requiresAuth: true, requiresUser: true }
      },
      {
        path: 'tools/caesar',
        name: 'bmgfToolsCaesar',
        component: () => import('@/components/home/Pricate_until/CaesarCipher.vue'),
        meta: { requiresAuth: true, requiresUser: true }
      },
      {
        path: 'tools/unicode',
        name: 'bmgfToolsUnicode',
        component: () => import('@/components/home/Pricate_until/Unicode.vue'),
        meta: { requiresAuth: true, requiresUser: true }
      },
      { path: 'questions', name: 'bmgfQuestions', component: () => import('@/components/home/Question.vue'), meta: { requiresAuth: true, requiresUser: true } },
      { path: 'my-sorts', name: 'bmgfMySorts', component: () => import('@/components/home/SortMine.vue'), meta: { requiresAuth: true, requiresUser: true } },
      { path: 'rewards', name: 'bmgfRewards', component: () => import('@/components/home/Reward.vue'), meta: { requiresAuth: true, requiresUser: true } },
      { 
        path: 'game', 
        name: 'bmgfGame', 
        redirect: '/bmgf/game/normal'
      },
      { 
        path: 'game/normal', 
        name: 'bmgfGameNormal', 
        component: () => import('@/components/gameView/NormalGameView.vue'), 
        meta: { requiresAuth: true, requiresUser: true } 
      },
      { 
        path: 'game/vip', 
        name: 'bmgfGameVip', 
        component: () => import('@/components/gameView/VipGame.vue'), 
        meta: { requiresAuth: true, requiresUser: true, requiresVip: true } 
      },
      { path: 'chat', name: 'bmgfChat', component: () => import('@/components/ai_answer/aianswer.vue'), meta: { requiresAuth: true, requiresUser: true } },
      { path: 'discussion', name: 'bmgfDiscussion', component: () => import('@/components/home/PublicDiscussion.vue') },
      { path: 'game/sql', name: 'bmgfGameSql', component: () => import('@/components/gameView/hole_coms/SQLInjection.vue'), meta: { requiresAuth: true, requiresUser: true } },
      { path: 'game/xss', name: 'bmgfGameXss', component: () => import('@/components/gameView/hole_coms/XSSInjection.vue')},
      { path: 'game/xss-admin', name: 'bmgfGameXssAdmin', component: () => import('@/components/gameView/components/Adminview.vue'), meta: { requiresAuth: true, requiresUser: true } },
      { 
        path: 'mine-mes', 
        name: 'bmgfMineMes', 
        component: () => import('@/components/home/MineMes.vue'),
        meta: { requiresAuth: true, requiresUser: true }
      },
      {
        path: 'help',
        name: 'bmgfHelp',
        component: () => import('@/components/home/Help/Help.vue')
      },
      {
        path: 'feedback',
        name: 'bmgfFeedback',
        component: () => import('@/components/home/Feedback/Feedback.vue')
      }
    ],
  },
  {
    path: '/bmgf/verify-email',
    name: 'bmgfVerifyEmail',
    component: () => import('@/components/emailverify/VerifyEmail.vue'),
    meta: {
      requiresAuth: true
    }
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 设置属性始终滚动到顶部
    return { top: 0 }
  }
});

//路由守卫
router.beforeEach((to, from, next) => {
  const token = store.state.token || localStorage.getItem('token');
  const userRoles = store.state.roles || [];
  const isAdmin = userRoles.includes('ROLE_ADMIN');
  const isVip = userRoles.includes('ROLE_VIP');
  
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token || token === 'null' || token === '') {
      ElMessage.warning('该功能需要登录后才能使用');
      if (from.name === 'bmgfLogin') {
        next(false);
      } else {
        next('/bmgf/login');
      }
    } else {
      // 检查管理员权限
      if (to.matched.some(record => record.meta.requiresAdmin)) {
        if (!isAdmin) {
          ElMessage.error('您没有权限访问管理员页面');
          next('/bmgf/game/normal');
          return;
        }
      }
      
      // 检查普通用户权限
      if (to.matched.some(record => record.meta.requiresUser)) {
        if (isAdmin) {
          ElMessage.error('管理员不能访问普通用户页面');
          next('/bmgf/admin/home');
          return;
        }
      }

      //检查VIP用户权限
      if(to.matched.some(record => record.meta.requiresVip)){
        if(!isVip){
          ElMessage.error('您没有权限访问VIP页面');
          next('/bmgf/home');
          return;
        }
      }
      
      next();
    }
  } else if (to.matched.some(record => record.meta.excludeAdmin)) {
    // 检查是否排除管理员访问
    if (isAdmin) {
      ElMessage.info('管理员不能访问普通用户页面');
      next('/bmgf/admin/home');
      return;
    }
    next();
  } else {
    next();
  }
});

export default router;