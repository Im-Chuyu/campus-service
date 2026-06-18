import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
    meta: {
      public: true
    }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/Register.vue'),
    meta: {
      public: true
    }
  },
  {
    path: '/forgot-password',
    name: 'forgotPassword',
    component: () => import('../views/ForgotPassword.vue'),
    meta: {
      public: true
    }
  },
  {
    path: '/home',
    name: 'home',
    component: () => import('../views/Home.vue'),
    meta: {
      public: true
    }
  },
  {
  path: '/publish',
  name: 'publishContent',
  component: () => import('../views/PublishContent.vue')
},
{
  path: '/my-content',
  name: 'myContent',
  component: () => import('../views/MyContent.vue')
},
{
  path: '/my-favorite',
  name: 'myFavorite',
  component: () => import('../views/MyFavorite.vue')
},
{
  path: '/message',
  name: 'messageCenter',
  component: () => import('../views/MessageCenter.vue')
},
{
  path: '/friends',
  name: 'friends',
  component: () => import('../views/FriendList.vue')
},
{
  path: '/trade',
  name: 'tradeMarket',
  component: () => import('../views/TradeMarket.vue'),
  meta: {
    public: true
  }
},
{
  path: '/notice',
  name: 'noticeList',
  component: () => import('../views/NoticeList.vue')
},
{
  path: '/notice/:id',
  name: 'noticeDetail',
  component: () => import('../views/NoticeDetail.vue')
},
  {
    path: '/content/edit/:id',
    name: 'editContent',
    component: () => import('../views/EditContent.vue')
  },
  {
    path: '/content/:id',
    name: 'contentDetail',
    component: () => import('../views/ContentDetail.vue'),
    meta: {
      public: true
    }
  },
  {
    path: '/profile/edit',
    name: 'editProfile',
    component: () => import('../views/EditProfile.vue')
  },
  {
    path: '/chat/:userId',
    name: 'privateChat',
    component: () => import('../views/PrivateChat.vue')
  },
  {
    path: '/profile/:id?',
    name: 'profile',
    component: () => import('../views/Profile.vue')
  },
{
  path: '/admin',
  name: 'adminDashboard',
  component: () => import('../views/AdminDashboard.vue'),
  meta: {
    admin: true
  }
}
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.public) {
    return next()
  }

  if (!userStore.token) {
    return next('/login')
  }

  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfo()
    } catch {
      userStore.logout()
      return next('/login')
    }
  }

  if (to.meta.admin && !userStore.isAdmin) {
    return next('/home')
  }

  next()
})

export default router
