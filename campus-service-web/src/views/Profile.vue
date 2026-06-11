<template>
  <div class="profile-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>个人主页</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button :icon="ArrowLeft" @click="$router.push('/home')">返回首页</el-button>
        <el-button :icon="ArrowLeft" @click="$router.back()">返回上一页</el-button>
        <el-button v-if="isSelf" type="primary" :icon="Edit" @click="$router.push('/profile/edit')">
          修改资料
        </el-button>
      </div>
    </header>

    <main class="profile-main" v-loading="loading">
      <section class="profile-shell" :class="{ 'self-layout': isSelf }">
        <aside v-if="isSelf" class="friend-sidebar">
          <div class="panel-head">
            <h3>好友</h3>
            <el-button size="small" plain @click="$router.push('/friends')">管理</el-button>
          </div>
          <div class="profile-friend-groups" v-loading="friendListLoading">
            <el-empty v-if="!friendListLoading && friendCategories.length === 0" description="暂无好友分类" />
            <div v-else class="profile-category-list">
              <section
                v-for="group in groupedFriends"
                :key="group.id"
                class="profile-category-section"
                :class="{ active: activeFriendCategoryId === group.id }"
              >
                <button type="button" class="profile-category-row" @click="toggleFriendCategory(group.id)">
                  <span>{{ group.name }}</span>
                  <strong>{{ group.friends.length }}</strong>
                </button>
                <div v-if="activeFriendCategoryId === group.id && group.friends.length > 0" class="profile-friend-list">
                  <article
                    v-for="friend in group.friends"
                    :key="friend.friendId"
                    class="profile-friend-card"
                    :class="{ 'has-bg': hasFriendBackground(friend) }"
                  >
                    <div class="friend-mini-bg" :style="friendBackgroundStyle(friend)"></div>
                    <UserAvatar
                      clickable
                      :size="55"
                      :src="friend.avatar"
                      :text="getFriendName(friend)"
                      :role-id="friend.roleId"
                      @click="goFriendProfile(friend.friendId)"
                    />
                    <div class="profile-friend-meta" @click="goFriendProfile(friend.friendId)">
                      <strong>{{ getFriendName(friend) }}</strong>
                      <span>{{ friend.signature || '这个用户还没有设置个性签名。' }}</span>
                    </div>
                    <el-button size="small" type="primary" plain @click="goChat(friend.friendId)">私聊</el-button>
                  </article>
                </div>
              </section>
            </div>
          </div>
        </aside>

        <div class="profile-right">
      <section class="hero-card" :style="heroStyle">
        <div class="hero-content">
              <UserAvatar
                class="hero-avatar"
                :size="114"
                :src="displayAvatar"
                :text="avatarText"
                :role-id="displayUser.roleId"
                :compact="false"
              />

              <div class="hero-copy">
                <div class="profile-title-row">
                  <h2>{{ displayName }}</h2>
                </div>
                <p class="signature-line">{{ signatureText }}</p>
              </div>
            </div>
            <div v-if="!isSelf" class="hero-actions">
              <el-button
                :type="isFriend ? 'success' : 'default'"
                :loading="friendLoading"
                :disabled="!displayUser.id || displayUser.blockedMe === 1"
                @click="toggleFriend"
              >
                {{ isFriend ? '已是好友' : '加好友' }}
              </el-button>
              <el-button type="primary" :icon="ChatDotRound" :disabled="!displayUser.id" @click="goChat()">
                私聊
              </el-button>
            </div>
            <div class="hero-badges">
              <el-tag type="success" effect="dark">发布 {{ contentCount }}</el-tag>
              <el-tag type="info" effect="dark">点赞 {{ totalLikes }}</el-tag>
              <el-tag type="warning" effect="dark">收藏 {{ totalFavorites }}</el-tag>
              <el-tag :type="displayUser.privateChatEnabled === 1 ? 'success' : 'info'" effect="dark">
                {{ displayUser.privateChatEnabled === 1 ? '私聊开启' : '私聊关闭' }}
              </el-tag>
            </div>
          </section>

          <div class="profile-grid">
            <section class="panel">
              <div class="panel-head">
                <h3>公开资料</h3>
              </div>

              <div class="info-grid">
                <div class="info-item">
                  <span>用户名</span>
                  <strong>{{ displayUser.username || '-' }}</strong>
                </div>
                <div class="info-item">
                  <span>昵称</span>
                  <strong>{{ displayUser.nickname || '-' }}</strong>
                </div>
                <div class="info-item">
                  <span>性别</span>
                  <strong>{{ genderText }}</strong>
                </div>
                <div class="info-item">
                  <span>手机号</span>
                  <strong>{{ resolveField(displayUser.phone, displayUser.phoneVisible) }}</strong>
                  <small v-if="isSelf">状态：{{ displayUser.phoneVisible === 1 ? '公开' : '隐藏' }}</small>
                </div>
                <div class="info-item">
                  <span>邮箱</span>
                  <strong>{{ resolveField(displayUser.email, displayUser.emailVisible) }}</strong>
                  <small v-if="isSelf">状态：{{ displayUser.emailVisible === 1 ? '公开' : '隐藏' }}</small>
                </div>
                <div class="info-item">
                  <span>微信号</span>
                  <strong>{{ resolveField(displayUser.wechat, displayUser.wechatVisible) }}</strong>
                  <small v-if="isSelf">状态：{{ displayUser.wechatVisible === 1 ? '公开' : '隐藏' }}</small>
                </div>
                <div class="info-item">
                  <span>QQ号</span>
                  <strong>{{ resolveField(displayUser.qq, displayUser.qqVisible) }}</strong>
                  <small v-if="isSelf">状态：{{ displayUser.qqVisible === 1 ? '公开' : '隐藏' }}</small>
                </div>
              </div>
            </section>

            <section class="panel">
              <div class="panel-head">
                <h3>账号状态</h3>
              </div>

              <div class="status-list">
                <div>
                  <span>用户角色</span>
                  <strong>{{ roleText }}</strong>
                </div>
                <div>
                  <span>注册时间</span>
                  <strong>{{ formatDate(displayUser.createTime) }}</strong>
                </div>
                <div>
                  <span>最近更新</span>
                  <strong>{{ formatDate(displayUser.updateTime) }}</strong>
                </div>
                <div>
                  <span>主页背景</span>
                  <strong>{{ hasCustomBackground ? '已自定义' : '默认背景' }}</strong>
                </div>
              </div>

              <div v-if="!isSelf" class="peer-actions">
                <el-button
                  :icon="displayUser.blockedByMe ? CircleCloseFilled : CircleCheckFilled"
                  :type="displayUser.blockedByMe ? 'danger' : 'warning'"
                  @click="toggleBlock"
                >
                  {{ displayUser.blockedByMe ? '取消屏蔽' : '屏蔽用户' }}
                </el-button>
                <el-tag v-if="displayUser.blockedMe" type="danger" effect="dark">对方已屏蔽你</el-tag>
                <el-tag v-else-if="displayUser.privateChatEnabled === 0" type="info" effect="dark">对方已关闭私聊</el-tag>
                <el-tag v-else-if="displayUser.canChat === 0" type="info" effect="dark">当前不可私聊</el-tag>
              </div>
            </section>
          </div>

          <section class="panel profile-content-panel" :class="{ 'self-content-panel': isSelf }">
            <div class="panel-head">
              <div>
                <h3>{{ isSelf ? '我的发布' : 'TA 的发布' }}</h3>
              </div>
              <div v-if="canViewPrivateContents" class="content-tabs">
                <button
                  type="button"
                  :class="{ active: activeContentScope === 'public' }"
                  @click="activeContentScope = 'public'"
                >
                  公开
                  <strong>{{ publicContents.length }}</strong>
                </button>
                <button
                  type="button"
                  :class="{ active: activeContentScope === 'private' }"
                  @click="activeContentScope = 'private'"
                >
                  私密
                  <strong>{{ privateContents.length }}</strong>
                </button>
              </div>
            </div>

            <div v-if="contentCategoryFilters.length > 1" class="content-category-tabs">
              <button
                v-for="item in contentCategoryFilters"
                :key="item.id"
                type="button"
                :class="{ active: activeContentCategoryId === item.id }"
                @click="activeContentCategoryId = item.id"
              >
                <span>{{ item.name }}</span>
                <strong>{{ item.count }}</strong>
              </button>
            </div>

            <div class="content-list" v-loading="contentLoading">
              <el-empty v-if="!contentLoading && visibleContents.length === 0" description="暂无内容" />

              <article
                v-for="item in visibleContents"
                :key="item.id"
                class="content-item"
                @click="goContent(item.id)"
              >
                <div class="content-item-head">
                  <div class="content-tags">
                    <el-tag size="small" :type="item.status === 1 ? 'success' : 'info'">
                      {{ item.status === 1 ? '已发布' : '待审核' }}
                    </el-tag>
                    <el-tag size="small" type="primary" effect="plain">{{ getContentCategoryName(item) }}</el-tag>
                    <el-tag v-if="item.isPrivate === 1" size="small" type="info">私密</el-tag>
                  </div>
                  <span>{{ formatDate(item.createTime) }}</span>
                </div>
                <h4>{{ item.title }}</h4>
                <p>{{ item.content }}</p>
              </article>
            </div>
          </section>
          </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft,
  ChatDotRound,
  CircleCheckFilled,
  CircleCloseFilled,
  Edit
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getPrivateChatPeerApi, blockUserApi, unblockUserApi } from '../api/chat'
import { getContentListApi, getMyContentListApi } from '../api/content'
import { getCategoryListApi } from '../api/category'
import { addFriendApi, deleteFriendApi, getFriendCategoryListApi, getFriendListApi, getFriendStatusApi } from '../api/friend'
import { getDisplayedAvatar, getProfileExtra, getDisplayedBackground } from '../utils/profileAppearance'
import UserAvatar from '../components/UserAvatar.vue'
import { formatBeijingDate } from '../utils/time'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const friendLoading = ref(false)
const friendListLoading = ref(false)
const contentLoading = ref(false)
const displayUser = ref({})
const displayExtra = ref({})
const userContents = ref([])
const categories = ref([])
const friendCategories = ref([])
const friends = ref([])
const activeContentScope = ref('public')
const activeContentCategoryId = ref(0)
const activeFriendCategoryId = ref(null)
const isFriend = ref(false)

const currentUserId = computed(() => Number(userStore.userInfo?.id || 0))
const routeUserId = computed(() => (route.params.id ? Number(route.params.id) : currentUserId.value))
const isSelf = computed(() => !route.params.id || Number(route.params.id) === currentUserId.value)

const displayName = computed(() => displayUser.value.nickname || displayUser.value.username || '同学')
const avatarText = computed(() => displayName.value.slice(0, 1))
const displayAvatar = computed(() => getDisplayedAvatar(displayUser.value, displayExtra.value))
const displayedBackground = computed(() => getDisplayedBackground(displayUser.value, displayExtra.value))
const hasCustomBackground = computed(() => Boolean(displayedBackground.value))
const signatureText = computed(() => {
  if (displayUser.value.signature) return displayUser.value.signature
  return isSelf.value ? '还没有设置个性签名' : 'TA 还没有留下个性签名'
})

const publicContents = computed(() => userContents.value.filter(item => item.isPrivate !== 1))
const privateContents = computed(() => userContents.value.filter(item => item.isPrivate === 1))
const canViewPrivateContents = computed(() => isSelf.value || userStore.isAdmin)
const scopedContents = computed(() => {
  if (activeContentScope.value === 'private' && canViewPrivateContents.value) {
    return privateContents.value
  }
  return publicContents.value
})
const visibleContents = computed(() => {
  if (!activeContentCategoryId.value) {
    return scopedContents.value
  }
  if (activeContentCategoryId.value === -1) {
    return scopedContents.value.filter(item => !item.categoryId)
  }
  return scopedContents.value.filter(item => Number(item.categoryId) === Number(activeContentCategoryId.value))
})
const contentCount = computed(() => publicContents.value.length)
const totalLikes = computed(() => publicContents.value.reduce((sum, item) => sum + (item.likeCount || 0), 0))
const totalFavorites = computed(() => publicContents.value.reduce((sum, item) => sum + (item.favoriteCount || 0), 0))
const groupedFriends = computed(() => friendCategories.value
  .map(category => ({
    ...category,
    friends: friends.value.filter(friend => friend.categoryId === category.id)
  }))
)
const categoryMap = computed(() => {
  const map = {}
  categories.value.forEach(item => {
    map[item.id] = item.name
  })
  return map
})
const contentCategoryFilters = computed(() => {
  const counts = new Map()
  scopedContents.value.forEach(item => {
    const id = Number(item.categoryId || 0)
    counts.set(id, (counts.get(id) || 0) + 1)
  })
  const filters = [{ id: 0, name: '全部', count: scopedContents.value.length }]
  categories.value.forEach(category => {
    const count = counts.get(Number(category.id)) || 0
    if (count > 0) {
      filters.push({ id: Number(category.id), name: category.name, count })
    }
  })
  if (counts.has(0)) {
    filters.push({ id: -1, name: '未分类', count: counts.get(0) })
  }
  return filters
})

const genderText = computed(() => {
  const map = { 1: '男', 2: '女', 0: '保密' }
  return map[displayUser.value.gender] || '-'
})

const roleText = computed(() => (displayUser.value.roleId === 1 ? '管理员' : '普通用户'))

const heroStyle = computed(() => {
  if (displayedBackground.value) {
    return { backgroundImage: `url(${displayedBackground.value})` }
  }
  return {}
})

const resolveField = (value, visible) => {
  if (visible === 1) return value || '-'
  return isSelf.value ? (value || '-') : '用户已隐藏'
}

const formatDate = value => {
  return formatBeijingDate(value)
}

const getFriendName = item => item.nickname || item.username || `用户${item.friendId}`
const getFriendBackground = item => item.profileBackground || getProfileExtra(item.friendId).background
const hasFriendBackground = item => Boolean(getFriendBackground(item))
const friendBackgroundStyle = item => {
  const background = getFriendBackground(item)
  if (background) return { '--friend-bg-image': `url(${background})` }
  return {}
}

const toggleFriendCategory = id => {
  activeFriendCategoryId.value = activeFriendCategoryId.value === id ? null : id
}

const getContentCategoryName = item => item.categoryName || categoryMap.value[item.categoryId] || '未分类'

const loadProfile = async () => {
  loading.value = true
  try {
    if (isSelf.value) {
      const me = await userStore.getUserInfo()
      displayUser.value = me || {}
      displayExtra.value = getProfileExtra(me.id)
      return
    }

    const res = await getPrivateChatPeerApi(routeUserId.value)
    displayUser.value = res.data || {}
    displayExtra.value = getProfileExtra(routeUserId.value)
    await loadFriendStatus()
  } finally {
    loading.value = false
  }
}

const loadFriendStatus = async () => {
  if (isSelf.value || !routeUserId.value) {
    isFriend.value = false
    return
  }
  const res = await getFriendStatusApi(routeUserId.value)
  isFriend.value = Number(res.data || 0) === 1
}

const loadContents = async () => {
  contentLoading.value = true
  try {
    if (isSelf.value) {
      const res = await getMyContentListApi()
      userContents.value = res.data || []
      return
    }

    const res = await getContentListApi({ userId: routeUserId.value, status: 1 })
    userContents.value = res.data || []
  } finally {
    contentLoading.value = false
  }
}

const loadCategories = async () => {
  const res = await getCategoryListApi()
  categories.value = res.data || []
}

const loadFriends = async () => {
  if (!isSelf.value) return
  friendListLoading.value = true
  try {
    const [categoryRes, friendRes] = await Promise.all([
      getFriendCategoryListApi(),
      getFriendListApi()
    ])
    friendCategories.value = categoryRes.data || []
    friends.value = friendRes.data || []
    if (!activeFriendCategoryId.value || !friendCategories.value.some(item => item.id === activeFriendCategoryId.value)) {
      activeFriendCategoryId.value = friendCategories.value[0]?.id || null
    }
  } finally {
    friendListLoading.value = false
  }
}

const loadPage = async () => {
  await loadProfile()
  await Promise.all([loadCategories(), loadContents(), loadFriends()])
}

const goContent = id => {
  router.push(`/content/${id}`)
}

const goChat = friendId => {
  const targetId = friendId || displayUser.value.id
  if (!targetId) return
  router.push(`/chat/${targetId}`)
}

const goFriendProfile = friendId => {
  router.push(`/profile/${friendId}`)
}

const toggleFriend = async () => {
  if (!displayUser.value.id || displayUser.value.blockedMe === 1) return
  friendLoading.value = true
  try {
    if (isFriend.value) {
      await deleteFriendApi(displayUser.value.id)
      isFriend.value = false
      ElMessage.success('已取消好友')
    } else {
      await addFriendApi(displayUser.value.id)
      isFriend.value = true
      ElMessage.success('已添加好友')
    }
  } finally {
    friendLoading.value = false
  }
}

const toggleBlock = async () => {
  if (!displayUser.value.id) return
  if (displayUser.value.blockedByMe) {
    await unblockUserApi(displayUser.value.id)
    ElMessage.success('已取消屏蔽')
  } else {
    await blockUserApi(displayUser.value.id)
    ElMessage.success('已屏蔽该用户')
  }
  await loadProfile()
}

watch(
  () => route.params.id,
  () => {
    activeContentScope.value = 'public'
    activeContentCategoryId.value = 0
    loadPage()
  }
)

watch([activeContentScope, contentCategoryFilters], () => {
  if (!contentCategoryFilters.value.some(item => item.id === activeContentCategoryId.value)) {
    activeContentCategoryId.value = 0
  }
})

onMounted(loadPage)
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: transparent;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 0 32px;
  background: var(--topbar-bg);
  border-bottom: 1px solid var(--border);
  backdrop-filter: blur(16px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
}

.brand-mark {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: var(--primary);
  color: #fff;
  font-weight: 800;
}

.brand h1 {
  margin: 0;
  font-size: 18px;
}

.brand p {
  margin: 4px 0 0;
  color: var(--muted);
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.profile-main {
  max-width: 1280px;
  margin: 0 auto;
  padding: 26px 24px 40px;
}

.hero-card {
  position: relative;
  width: 100%;
  aspect-ratio: 3.36 / 1;
  border-radius: 20px;
  overflow: hidden;
  background:
    var(--hero-gradient),
    var(--primary);
  background-size: cover;
  background-position: center;
  box-shadow: var(--shadow);
}

.hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 22px;
  padding: 34px 38px;
  color: #fff;
}

.hero-avatar {
  filter: drop-shadow(0 16px 28px rgba(0, 0, 0, 0.16));
}

.profile-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-copy h2 {
  margin: 0;
  font-size: 30px;
}

.hero-copy p {
  margin: 10px 0 0;
  max-width: 760px;
  line-height: 1.7;
}

.signature-line {
  font-size: 15px;
  opacity: 0.96;
  white-space: pre-wrap;
  word-break: break-word;
}

.hero-badges {
  position: absolute;
  left: 38px;
  bottom: 26px;
  z-index: 2;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  max-width: calc(100% - 76px);
}

.hero-actions {
  position: absolute;
  right: 26px;
  bottom: 18px;
  z-index: 2;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.hero-actions :deep(.el-button) {
  height: 26px;
  padding: 0 10px;
  font-size: 12px;
}

.profile-shell {
  width: 75%;
  margin-top: 18px;
  margin-left: auto;
  margin-right: auto;
}

.profile-shell.self-layout {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(270px, 320px) minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.profile-right {
  min-width: 0;
  display: grid;
  gap: 18px;
}

.profile-content-panel {
  width: 100%;
}

.friend-sidebar {
  padding: 20px;
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.05);
}

.profile-friend-groups {
  display: grid;
  gap: 14px;
  margin-top: 16px;
}

.profile-category-list {
  display: grid;
  gap: 8px;
}

.profile-category-section {
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--surface-soft);
}

.profile-category-section.active {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--glow-primary);
}

.profile-category-row {
  min-height: 38px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 0 12px;
  border: 0;
  background: transparent;
  color: var(--text);
  cursor: pointer;
  text-align: left;
}

.profile-category-section.active .profile-category-row {
  background: var(--theme-tint);
}

.profile-category-row span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-category-row strong {
  min-width: 24px;
  height: 22px;
  display: grid;
  place-items: center;
  padding: 0 7px;
  border-radius: 999px;
  background: var(--surface);
  color: var(--muted);
  font-size: 12px;
}

.profile-friend-list {
  display: grid;
  gap: 10px;
  justify-items: center;
  padding: 10px 12px 12px;
  border-top: 1px solid var(--border);
}

.profile-friend-card {
  position: relative;
  box-sizing: border-box;
  overflow: hidden;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 10px 12px;
  align-items: center;
  width: min(100%, 260px);
  min-height: 82px;
  aspect-ratio: 3.36 / 1;
  padding: 12px;
  border-radius: 14px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.friend-mini-bg {
  position: absolute;
  inset: 0;
  background-image: var(--friend-bg-image);
  background-color: var(--primary);
  background-size: cover;
  background-position: center;
}

.profile-friend-card:not(.has-bg) .friend-mini-bg {
  display: none;
}

.profile-friend-card > :not(.friend-mini-bg) {
  position: relative;
  z-index: 1;
}

.profile-friend-card .el-button {
  position: absolute;
  right: 10px;
  bottom: 8px;
  z-index: 1;
  height: 24px;
  padding: 0 10px;
  font-size: 12px;
}

.profile-friend-meta {
  min-width: 0;
  padding-right: 56px;
  cursor: pointer;
}

.profile-friend-meta strong,
.profile-friend-meta span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-friend-meta strong {
  color: var(--text);
  font-size: 14px;
}

.profile-friend-meta span {
  margin-top: 4px;
  color: var(--muted);
  font-size: 12px;
}

.profile-friend-card.has-bg .profile-friend-meta strong {
  color: #fff;
  text-shadow: 0 1px 8px rgba(15, 23, 42, 0.26);
}

.profile-friend-card.has-bg .profile-friend-meta span {
  color: rgba(255, 255, 255, 0.86);
  text-shadow: 0 1px 8px rgba(15, 23, 42, 0.22);
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.85fr);
  gap: 18px;
  align-items: stretch;
}

.panel {
  padding: 20px;
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.05);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.panel-head h3 {
  margin: 0;
  font-size: 18px;
}

.content-tabs {
  display: inline-flex;
  gap: 8px;
  padding: 4px;
  border-radius: 999px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.content-tabs button {
  height: 34px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 13px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
}

.content-tabs button.active {
  background: var(--primary);
  color: #fff;
}

.content-tabs strong {
  min-width: 20px;
  height: 20px;
  display: grid;
  place-items: center;
  padding: 0 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.22);
  font-size: 12px;
}

.content-category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.content-category-tabs button {
  height: 34px;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  max-width: 180px;
  padding: 0 12px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--surface-soft);
  color: var(--text);
  cursor: pointer;
}

.content-category-tabs button.active {
  border-color: var(--primary);
  background: var(--theme-tint);
  color: var(--primary);
}

.content-category-tabs span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content-category-tabs strong {
  min-width: 20px;
  height: 20px;
  display: grid;
  place-items: center;
  padding: 0 6px;
  border-radius: 999px;
  background: var(--surface);
  color: var(--muted);
  font-size: 12px;
}

.content-category-tabs button.active strong {
  background: var(--primary);
  color: #fff;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.info-item {
  padding: 14px;
  border-radius: 14px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.info-item span,
.status-list span {
  display: block;
  color: var(--muted);
  font-size: 12px;
}

.info-item strong,
.status-list strong {
  display: block;
  margin-top: 6px;
  color: var(--text);
  font-size: 15px;
}

.info-item small {
  display: block;
  margin-top: 6px;
  color: var(--muted);
}

.status-list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.status-list > div {
  padding: 14px;
  border-radius: 14px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.peer-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-top: 18px;
}

.content-list {
  display: grid;
  gap: 14px;
  margin-top: 16px;
}

.content-item {
  padding: 16px 18px;
  border-radius: 14px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
  cursor: pointer;
}

.content-item-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 10px;
  color: var(--muted);
  font-size: 12px;
}

.content-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.content-item h4 {
  margin: 0;
  font-size: 16px;
}

.content-item p {
  margin: 10px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

@media (max-width: 1024px) {
  .hero-card {
    width: 100%;
  }

  .profile-shell {
    width: 100%;
  }

  .profile-shell.self-layout {
    grid-template-columns: 1fr;
  }

  .profile-grid,
  .info-grid {
    grid-template-columns: 1fr;
  }

  .panel-head {
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .hero-content {
    align-items: flex-start;
    padding: 26px 24px 116px;
  }

  .hero-badges {
    left: 24px;
    right: 24px;
    bottom: 20px;
    max-width: none;
  }

  .hero-actions {
    left: 24px;
    right: 24px;
    bottom: 64px;
    justify-content: flex-start;
  }

  .profile-friend-card {
    grid-template-columns: auto minmax(0, 1fr);
  }
}
</style>

