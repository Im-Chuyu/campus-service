<template>
  <div class="friend-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>好友列表</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button :icon="ArrowLeft" @click="$router.push('/home')">返回首页</el-button>
        <el-button :icon="ArrowLeft" @click="$router.back()">返回上一页</el-button>
        <el-button :icon="Refresh" @click="loadFriends">刷新</el-button>
        <el-button type="primary" plain @click="openCategoryDialog()">添加分类</el-button>
      </div>
    </header>

    <main class="friend-main">
      <section class="hero-card">
        <div>
          <p class="eyebrow">Friends</p>
          <h2>好友列表</h2>
          <p>查看常联系的同学，点击头像可以私聊或进入主页。</p>
        </div>
        <strong>{{ friends.length }}</strong>
      </section>

      <section class="search-panel">
        <el-input
          v-model="searchUsername"
          size="large"
          placeholder="输入用户名添加好友"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" :loading="searching" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>

        <article
          v-if="searchResult"
          class="friend-card search-result"
          :class="{ 'has-bg': hasFriendBackground(searchResult) }"
        >
          <div class="friend-card-bg" :style="friendBackgroundStyle(searchResult)"></div>
          <UserAvatar
            class="friend-avatar"
            clickable
            :size="81"
            :src="searchResult.avatar"
            :text="getFriendName(searchResult)"
            :role-id="searchResult.roleId"
            @click="goProfile(searchResult.friendId)"
          />
          <div class="friend-body">
            <div class="friend-head">
              <div>
                <h3>{{ getFriendName(searchResult) }}</h3>
                <span>{{ searchResult.username }}</span>
              </div>
              <div class="friend-actions">
                <el-button plain @click="goProfile(searchResult.friendId)">主页</el-button>
                <el-button type="success" plain @click="goChat(searchResult.friendId)">私聊</el-button>
                <el-button
                  type="primary"
                  plain
                  :disabled="searchResult.isFriend === 1"
                  :loading="addingId === searchResult.friendId"
                  @click="handleAdd(searchResult)"
                >
                  {{ searchResult.isFriend === 1 ? '已是好友' : '添加好友' }}
                </el-button>
              </div>
            </div>
            <p>{{ searchResult.signature || '这个用户还没有设置个性签名。' }}</p>
          </div>
        </article>
      </section>

      <section class="category-panel" v-loading="loading">
        <div class="category-card-head">
          <div>
            <h3>好友分类</h3>
            <span>点击分类展开好友，空分类不会显示空状态提示。</span>
          </div>
          <strong>{{ categories.length }} / 20</strong>
        </div>

        <section v-for="category in categoriesWithFriends" :key="category.id" class="category-block" :class="{ active: activeCategoryId === category.id }">
          <button type="button" class="category-head" @click="toggleCategory(category.id)">
            <div>
              <h3>{{ category.name }}</h3>
              <span>{{ category.friends.length }} 个好友</span>
            </div>
            <div class="category-actions" @click.stop>
              <el-button size="small" plain @click="openCategoryDialog(category)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleDeleteCategory(category)">删除</el-button>
            </div>
          </button>

          <div v-if="activeCategoryId === category.id && category.friends.length > 0" class="category-friends">
            <article
              v-for="item in category.friends"
              :key="item.friendId"
              class="friend-card"
              :class="{ 'has-bg': hasFriendBackground(item) }"
            >
              <div class="friend-card-bg" :style="friendBackgroundStyle(item)"></div>
              <UserAvatar
                class="friend-avatar"
                clickable
                :size="81"
                :src="item.avatar"
                :text="getFriendName(item)"
                :role-id="item.roleId"
                @click="goProfile(item.friendId)"
              />

              <div class="friend-body">
                <div class="friend-head">
                  <div>
                    <h3>{{ getFriendName(item) }}</h3>
                    <span>{{ item.username }} · {{ formatTime(item.createTime) }}</span>
                  </div>
                </div>
                <p>{{ item.signature || '这个用户还没有设置个性签名。' }}</p>
              </div>
              <div class="friend-actions category-friend-actions">
                <el-select
                  v-model="item.categoryId"
                  size="small"
                  class="category-select"
                  @change="value => handleAssignCategory(item, value)"
                >
                  <el-option v-for="option in categories" :key="option.id" :label="option.name" :value="option.id" />
                </el-select>
                <el-button type="success" plain @click="goChat(item.friendId)">私聊</el-button>
                <el-button type="danger" plain :loading="removingId === item.friendId" @click="handleRemove(item)">
                  移除
                </el-button>
              </div>
            </article>
          </div>
        </section>
      </section>
    </main>

    <el-dialog v-model="categoryDialogVisible" :title="categoryForm.id ? '编辑分类' : '添加分类'" width="360px">
      <el-input v-model="categoryForm.name" maxlength="20" show-word-limit placeholder="请输入分类名称" />
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySaving" @click="handleSaveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Refresh, Search } from '@element-plus/icons-vue'
import {
  addFriendApi,
  assignFriendCategoryApi,
  deleteFriendCategoryApi,
  deleteFriendApi,
  getFriendCategoryListApi,
  getFriendListApi,
  saveFriendCategoryApi,
  searchFriendByUsernameApi
} from '../api/friend'
import { getProfileExtra } from '../utils/profileAppearance'
import UserAvatar from '../components/UserAvatar.vue'
import { formatBeijingDate } from '../utils/time'

const router = useRouter()

const loading = ref(false)
const searching = ref(false)
const addingId = ref(null)
const removingId = ref(null)
const categorySaving = ref(false)
const categoryDialogVisible = ref(false)
const friends = ref([])
const categories = ref([])
const activeCategoryId = ref(null)
const searchUsername = ref('')
const searchResult = ref(null)
const categoryForm = reactive({ id: null, name: '' })

const getFriendName = item => item.nickname || item.username || `用户${item.friendId}`
const categoriesWithFriends = computed(() => categories.value.map(category => ({
  ...category,
  friends: friends.value.filter(friend => friend.categoryId === category.id)
})))
const getFriendBackground = item => item.profileBackground || getProfileExtra(item.friendId).background
const hasFriendBackground = item => Boolean(getFriendBackground(item))
const friendBackgroundStyle = item => {
  const background = getFriendBackground(item)
  if (background) return { '--friend-bg-image': `url(${background})` }
  return {}
}
const toggleCategory = id => {
  activeCategoryId.value = activeCategoryId.value === id ? null : id
}

const formatTime = value => {
  return formatBeijingDate(value)
}

const loadFriends = async () => {
  loading.value = true
  try {
    const [categoryRes, friendRes] = await Promise.all([
      getFriendCategoryListApi(),
      getFriendListApi()
    ])
    categories.value = categoryRes.data || []
    friends.value = friendRes.data || []
    if (!activeCategoryId.value || !categories.value.some(item => item.id === activeCategoryId.value)) {
      activeCategoryId.value = categories.value[0]?.id || null
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  const username = searchUsername.value.trim()
  if (!username) {
    ElMessage.warning('请输入用户名')
    return
  }
  searching.value = true
  try {
    const res = await searchFriendByUsernameApi(username)
    searchResult.value = res.data || null
  } finally {
    searching.value = false
  }
}

const handleAdd = async item => {
  addingId.value = item.friendId
  try {
    await addFriendApi(item.friendId)
    item.isFriend = 1
    ElMessage.success('已添加好友')
    await loadFriends()
  } finally {
    addingId.value = null
  }
}

const openCategoryDialog = category => {
  categoryForm.id = category?.id || null
  categoryForm.name = category?.name || ''
  categoryDialogVisible.value = true
}

const handleSaveCategory = async () => {
  const name = categoryForm.name.trim()
  if (!name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  categorySaving.value = true
  try {
    await saveFriendCategoryApi({ id: categoryForm.id, name })
    categoryDialogVisible.value = false
    ElMessage.success('保存成功')
    await loadFriends()
  } finally {
    categorySaving.value = false
  }
}

const handleDeleteCategory = async category => {
  await ElMessageBox.confirm(`确定删除分类「${category.name}」吗？分类下好友会移动到其他分类。`, '删除分类', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await deleteFriendCategoryApi(category.id)
  ElMessage.success('已删除分类')
  await loadFriends()
}

const handleAssignCategory = async (item, categoryId) => {
  await assignFriendCategoryApi({ friendId: item.friendId, categoryId })
  ElMessage.success('已移动分类')
  await loadFriends()
}

const handleRemove = async item => {
  await ElMessageBox.confirm(`确定移除好友「${getFriendName(item)}」吗？`, '移除好友', {
    type: 'warning',
    confirmButtonText: '移除',
    cancelButtonText: '取消'
  })
  removingId.value = item.friendId
  try {
    await deleteFriendApi(item.friendId)
    friends.value = friends.value.filter(friend => friend.friendId !== item.friendId)
    if (searchResult.value?.friendId === item.friendId) searchResult.value.isFriend = 0
    ElMessage.success('已移除好友')
  } finally {
    removingId.value = null
  }
}

const goChat = friendId => {
  router.push(`/chat/${friendId}`)
}

const goProfile = friendId => {
  router.push(`/profile/${friendId}`)
}

onMounted(loadFriends)
</script>

<style scoped>
.friend-page {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  height: 72px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  padding: 0 32px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  backdrop-filter: blur(18px);
}

.brand,
.top-actions,
.hero-card,
.friend-card,
.friend-head {
  display: flex;
  align-items: center;
}

.brand {
  gap: 12px;
  cursor: pointer;
}

.brand-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: var(--primary);
  color: #fff;
  font-weight: 800;
  font-size: 20px;
}

.brand h1 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 18px;
}

.brand p {
  margin: 3px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.top-actions {
  gap: 10px;
}

.friend-main {
  width: min(1000px, calc(100% - 44px));
  margin: 0 auto;
  padding: 28px 0 44px;
}

.hero-card,
.search-panel,
.friend-panel {
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.hero-card {
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
  background: var(--hero-gradient);
  color: #fff;
}

.eyebrow {
  margin: 0 0 8px;
  opacity: 0.82;
  font-size: 12px;
  text-transform: uppercase;
}

.hero-card h2 {
  margin: 0;
  font-size: 30px;
}

.hero-card p {
  margin: 10px 0 0;
  opacity: 0.9;
}

.hero-card strong {
  font-size: 42px;
}

.search-panel,
.friend-panel,
.category-panel {
  display: grid;
  gap: 10px;
  margin-top: 18px;
  padding: 20px;
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.category-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 4px 4px 10px;
}

.category-card-head h3 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 20px;
}

.category-card-head span {
  display: block;
  margin-top: 6px;
  color: var(--muted);
  font-size: 13px;
}

.category-card-head strong {
  min-width: 72px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: var(--theme-tint);
  color: var(--primary-deep);
}

.category-block {
  display: grid;
  overflow: hidden;
  border-radius: 14px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.category-block.active {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--glow-primary);
}

.category-head {
  width: 100%;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  border: 0;
  background: transparent;
  cursor: pointer;
  text-align: left;
}

.category-head h3 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 18px;
}

.category-head span {
  display: block;
  margin-top: 5px;
  color: var(--muted);
  font-size: 13px;
}

.category-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.category-friends {
  display: grid;
  gap: 12px;
  justify-items: center;
  padding: 14px;
  border-top: 1px solid var(--border);
  background: var(--surface);
}

.friend-card {
  position: relative;
  overflow: hidden;
  align-items: flex-start;
  gap: 16px;
  width: min(100%, 720px);
  min-height: 164px;
  aspect-ratio: 3.36 / 1;
  padding: 18px 18px 54px;
  border-radius: 16px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.category-block .friend-card {
  background: var(--surface);
}

.friend-card-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  background-image: var(--friend-bg-image);
  background-color: var(--primary);
  background-size: cover;
  background-position: center;
}

.friend-card:not(.has-bg) .friend-card-bg {
  display: none;
}

.friend-card > :not(.friend-card-bg),
.friend-head > div:first-child,
.friend-body > p {
  position: relative;
  z-index: 1;
}

.search-result {
  justify-self: center;
  background: var(--surface-soft);
}

.friend-avatar {
  cursor: pointer;
  border-radius: 50%;
}

.friend-avatar :deep(.el-avatar) {
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.1);
}

.friend-body {
  min-width: 0;
  flex: 1;
  padding-right: 172px;
  position: static;
  z-index: auto;
}

.friend-head {
  justify-content: space-between;
  gap: 14px;
}

.friend-actions {
  position: absolute;
  right: 16px;
  bottom: 14px;
  z-index: 2;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
}

.friend-actions :deep(.el-button) {
  height: 28px;
  padding: 0 10px;
  font-size: 12px;
}

.category-select {
  width: 116px;
}

.category-block .friend-actions {
  right: 14px;
  bottom: 12px;
  max-width: calc(100% - 28px);
}

.category-block .friend-actions :deep(.el-input__wrapper) {
  min-height: 28px;
}

.category-block .friend-actions :deep(.el-button) {
  min-width: 48px;
}

.friend-head h3 {
  margin: 0;
  color: var(--text);
  font-size: 18px;
}

.friend-head span {
  display: block;
  margin-top: 5px;
  color: var(--muted);
  font-size: 13px;
}

.friend-body p {
  margin: 12px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.friend-card.has-bg .friend-head h3 {
  color: #fff;
  text-shadow: 0 1px 8px rgba(15, 23, 42, 0.28);
}

.friend-card.has-bg .friend-head span {
  color: rgba(255, 255, 255, 0.82);
  text-shadow: 0 1px 8px rgba(15, 23, 42, 0.2);
}

.friend-card.has-bg .friend-body p {
  color: rgba(255, 255, 255, 0.88);
  text-shadow: 0 1px 8px rgba(15, 23, 42, 0.2);
}

@media (max-width: 720px) {
  .topbar,
  .friend-head {
    height: auto;
    align-items: flex-start;
    flex-direction: column;
  }

  .friend-card {
    flex-direction: column;
    width: 100%;
    min-height: 190px;
    aspect-ratio: auto;
  }

  .friend-body {
    width: 100%;
    padding-right: 0;
  }

  .friend-actions {
    position: relative;
    right: auto;
    bottom: auto;
    justify-content: flex-start;
    margin-top: 12px;
  }

  .category-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
