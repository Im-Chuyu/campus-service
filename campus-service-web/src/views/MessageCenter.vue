<template>
  <div class="message-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>消息中心</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button :icon="ArrowLeft" @click="$router.push('/home')">返回首页</el-button>
        <el-button :icon="ArrowLeft" @click="$router.back()">返回上一页</el-button>
        <el-button :icon="Refresh" @click="loadMessages">刷新</el-button>
        <el-button
          type="primary"
          plain
          :disabled="totalUnreadCount === 0"
          :loading="markAllLoading"
          @click="handleMarkAllRead"
        >
          一键已读
        </el-button>
      </div>
    </header>

    <main class="message-main">
      <section class="hero-card">
        <div>
          <p class="eyebrow">Message Center</p>
          <h2>消息中心</h2>
          <p>查看评论、回复、点赞、系统通知，以及管理员的审核消息。</p>
        </div>
        <div class="unread-box">
          <strong>{{ totalUnreadCount }}</strong>
          <span>未读消息</span>
        </div>
      </section>

      <section v-if="isAdmin" class="audit-panel">
        <div class="panel-head">
          <div>
            <h3>待审核内容</h3>
            <p>{{ auditList.length }} 条内容等待处理</p>
          </div>
          <el-button :icon="Refresh" @click="loadAuditList">刷新审核</el-button>
        </div>

        <div class="audit-list" v-loading="auditLoading">
          <el-empty v-if="!auditLoading && auditList.length === 0" description="暂无待审核内容" />

          <article v-for="item in auditList" :key="item.id" class="audit-card">
            <div class="audit-main">
              <div class="audit-head">
                <el-tag :type="getCategoryTag(item.categoryId)">{{ getCategoryName(item.categoryId) }}</el-tag>
                <span>#{{ item.id }}</span>
              </div>
              <h3>{{ item.title }}</h3>
              <p>发布者：{{ item.userId }} · {{ formatTime(item.createTime) }}</p>
            </div>

            <div class="audit-actions">
              <el-button @click="goContent(item.id)">查看内容</el-button>
              <el-button type="success" :loading="auditActionId === item.id" @click="handleApprove(item)">通过</el-button>
              <el-button type="danger" plain :loading="auditActionId === item.id" @click="openReject(item)">拒绝</el-button>
              <el-button type="danger" plain :loading="deleteAuditId === item.id" @click="handleDeleteContent(item)">删除</el-button>
            </div>
          </article>
        </div>
      </section>

      <section class="filter-row">
        <button
          v-for="item in visibleFilters"
          :key="item.value"
          class="filter-pill"
          :class="{ active: currentFilter === item.value }"
          @click="currentFilter = item.value"
        >
          {{ item.label }}
          <span v-if="getFilterUnreadCount(item.value) > 0" class="filter-badge">
            {{ getFilterUnreadCount(item.value) }}
          </span>
        </button>
      </section>

      <section v-if="currentFilter === 'PRIVATE_CHAT'" class="message-panel" v-loading="loading">
        <el-empty v-if="!loading && chatSessions.length === 0" description="暂无私聊" />

        <article
          v-for="item in chatSessions"
          :key="item.peerId"
          class="message-card chat-session-card"
          :class="{ unread: Number(item.unreadCount || 0) > 0 }"
          @click="goPrivateChat(item)"
        >
          <UserAvatar
            :size="60"
            :src="item.avatar"
            :text="item.nickname || item.username || 'U'"
            :role-id="item.roleId"
          />

          <div class="message-body">
            <div class="message-head">
              <div>
                <h3>{{ item.nickname || item.username || `用户${item.peerId}` }}</h3>
                <span>私聊 · {{ formatTime(item.lastTime) }}</span>
              </div>
              <el-tag v-if="Number(item.unreadCount || 0) > 0" type="warning">
                {{ item.unreadCount }} 条未读
              </el-tag>
            </div>

            <p>{{ item.lastContent }}</p>
          </div>
        </article>
      </section>

      <section v-else class="message-panel" v-loading="loading">
        <el-empty v-if="!loading && filteredMessages.length === 0" description="暂无消息" />

        <article
          v-for="item in filteredMessages"
          :key="item.id"
          class="message-card"
          :class="{ unread: item.isRead === 0 }"
          @click="handleViewMessage(item)"
        >
          <div class="message-icon" :class="`icon-${item.type}`">
            <el-icon><component :is="getTypeIcon(item.type)" /></el-icon>
          </div>

          <div class="message-body">
            <div class="message-head">
              <div>
                <h3>{{ item.title }}</h3>
                <span>{{ getTypeText(item.type) }} · {{ formatTime(item.createTime) }}</span>
              </div>
              <el-tag :type="item.isRead === 0 ? 'warning' : 'info'">
                {{ item.isRead === 0 ? '未读' : '已读' }}
              </el-tag>
            </div>

            <p>{{ item.content }}</p>

            <div class="actions">
              <el-button v-if="item.relatedId" plain @click.stop="goRelated(item)">查看内容</el-button>
              <el-button type="danger" plain :loading="deletingId === item.id" @click.stop="handleDelete(item)">
                删除
              </el-button>
            </div>
          </div>
        </article>
      </section>

      <el-dialog v-model="rejectDialogVisible" title="拒绝审核" width="460px">
        <el-input v-model="rejectReason" type="textarea" :rows="4" maxlength="255" show-word-limit placeholder="拒绝原因可选，留空即可" />
        <template #footer>
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" :loading="auditActionId === currentAudit?.id" @click="handleReject">确认拒绝</el-button>
        </template>
      </el-dialog>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Bell, ChatDotRound, Message, Refresh, Tickets } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getWaitAuditListApi, handleAuditApi } from '../api/audit'
import { deleteContentApi } from '../api/content'
import {
  deleteMessageApi,
  getMyMessageListApi,
  getUnreadMessageCountApi,
  markAllMessageReadApi,
  markMessageReadApi
} from '../api/message'
import { getCategoryListApi } from '../api/category'
import { getPrivateChatSessionsApi, markAllPrivateChatReadApi } from '../api/chat'
import UserAvatar from '../components/UserAvatar.vue'
import { formatBeijingDateTime } from '../utils/time'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const markAllLoading = ref(false)
const deletingId = ref(null)
const unreadCount = ref(0)
const messages = ref([])
const chatSessions = ref([])
const currentFilter = ref('all')

const auditLoading = ref(false)
const auditActionId = ref(null)
const deleteAuditId = ref(null)
const auditList = ref([])
const currentAudit = ref(null)
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const categories = ref([])

const isAdmin = computed(() => userStore.isAdmin)

const filters = [
  { label: '全部', value: 'all' },
  { label: '评论', value: 'COMMENT' },
  { label: '回复', value: 'REPLY' },
  { label: '点赞', value: 'LIKE' },
  { label: '私聊', value: 'PRIVATE_CHAT' },
  { label: '审核', value: 'AUDIT' },
  { label: '公告', value: 'NOTICE' },
  { label: '系统', value: 'SYSTEM' }
]

const visibleFilters = computed(() => filters.filter(item => {
  if (item.value === 'FRIEND') return false
  return item.value !== 'AUDIT' || isAdmin.value
}))

const filteredMessages = computed(() => {
  if (currentFilter.value === 'PRIVATE_CHAT') return []
  if (currentFilter.value === 'all') return messages.value
  return messages.value.filter(item => item.type === currentFilter.value)
})

const privateChatUnreadCount = computed(() => {
  return chatSessions.value.reduce((total, item) => total + Number(item.unreadCount || 0), 0)
})
const totalUnreadCount = computed(() => unreadCount.value + privateChatUnreadCount.value)

const getFilterUnreadCount = value => {
  if (value === 'all') return totalUnreadCount.value
  if (value === 'PRIVATE_CHAT') return privateChatUnreadCount.value
  return messages.value.filter(item => item.type === value && item.isRead === 0).length
}

const formatTime = value => {
  return formatBeijingDateTime(value)
}

const getTypeText = type => {
  const map = {
    COMMENT: '评论提醒',
    REPLY: '回复提醒',
    LIKE: '点赞提醒',
    PRIVATE_CHAT: '私聊消息',
    AUDIT: '审核通知',
    NOTICE: '公告通知',
    SYSTEM: '系统通知'
  }
  return map[type] || '消息'
}

const getTypeIcon = type => {
  const map = {
    COMMENT: ChatDotRound,
    REPLY: ChatDotRound,
    LIKE: ChatDotRound,
    PRIVATE_CHAT: Message,
    AUDIT: Tickets,
    NOTICE: Bell,
    SYSTEM: Tickets
  }
  return map[type] || Tickets
}

const getCategoryName = categoryId => {
  return categories.value.find(item => item.id === categoryId)?.name || '未分类'
}

const getCategoryTag = categoryId => {
  const name = getCategoryName(categoryId)
  const map = {
    '失物招领': 'success',
    '二手交易': 'warning',
    '活动通知': 'primary',
    '校园论坛': 'info'
  }
  return map[name] || ''
}

const loadCategories = async () => {
  const res = await getCategoryListApi()
  categories.value = res.data || []
}

const loadUnreadCount = async () => {
  const res = await getUnreadMessageCountApi()
  unreadCount.value = res.data || 0
}

const loadMessages = async () => {
  loading.value = true
  try {
    const [messageRes, sessionRes] = await Promise.all([
      getMyMessageListApi(),
      getPrivateChatSessionsApi()
    ])
    messages.value = messageRes.data || []
    chatSessions.value = sessionRes.data || []
    await loadUnreadCount()
  } finally {
    loading.value = false
  }
}

const loadAuditList = async () => {
  if (!isAdmin.value) return
  auditLoading.value = true
  try {
    const res = await getWaitAuditListApi()
    auditList.value = res.data || []
  } finally {
    auditLoading.value = false
  }
}

const goContent = id => {
  router.push(`/content/${id}`)
}

const handleApprove = async row => {
  auditActionId.value = row.id
  try {
    await handleAuditApi({ contentId: row.id, auditResult: 1, auditReason: '' })
    ElMessage.success('已通过审核')
    await Promise.all([loadAuditList(), loadMessages()])
  } finally {
    auditActionId.value = null
  }
}

const openReject = row => {
  currentAudit.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const handleReject = async () => {
  const reason = rejectReason.value.trim()
  if (!reason) {
    ElMessage.warning('请填写拒绝原因')
    return
  }

  auditActionId.value = currentAudit.value.id
  try {
    await handleAuditApi({
      contentId: currentAudit.value.id,
      auditResult: 2,
      auditReason: reason
    })
    ElMessage.success('已拒绝该内容')
    rejectDialogVisible.value = false
    await Promise.all([loadAuditList(), loadMessages()])
  } finally {
    auditActionId.value = null
  }
}

const handleDeleteContent = async row => {
  await ElMessageBox.confirm(`确定删除帖子「${row.title}」吗？删除后不可恢复。`, '删除帖子', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  })

  deleteAuditId.value = row.id
  try {
    await deleteContentApi(row.id)
    ElMessage.success('帖子已删除')
    await Promise.all([loadAuditList(), loadMessages()])
  } finally {
    deleteAuditId.value = null
  }
}

const handleMarkAllRead = async () => {
  if (totalUnreadCount.value === 0) return
  markAllLoading.value = true
  try {
    await Promise.all([markAllMessageReadApi(), markAllPrivateChatReadApi()])
    messages.value = messages.value.map(item => ({ ...item, isRead: 1 }))
    chatSessions.value = chatSessions.value.map(item => ({ ...item, unreadCount: 0 }))
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } finally {
    markAllLoading.value = false
  }
}

const markMessageReadIfNeeded = async item => {
  if (!item || item.isRead !== 0) return
  await markMessageReadApi(item.id)
  item.isRead = 1
  unreadCount.value = Math.max(unreadCount.value - 1, 0)
}

const handleViewMessage = async item => {
  try {
    await markMessageReadIfNeeded(item)
  } catch {
    return
  }
}

const goRelated = async item => {
  try {
    await markMessageReadIfNeeded(item)
  } finally {
    if (item.type === 'PRIVATE_CHAT' && item.relatedId) {
      router.push(`/chat/${item.relatedId}`)
      return
    }
    if (item.relatedId) {
      router.push(`/content/${item.relatedId}`)
    }
  }
}

const handleDelete = async item => {
  await ElMessageBox.confirm('确定删除这条消息吗？', '删除消息', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })

  deletingId.value = item.id
  try {
    await markMessageReadIfNeeded(item)
    await deleteMessageApi(item.id)
    messages.value = messages.value.filter(message => message.id !== item.id)
    ElMessage.success('消息已删除')
  } finally {
    deletingId.value = null
  }
}

const goPrivateChat = async session => {
  if (Number(session.unreadCount || 0) > 0) {
    try {
      await markAllPrivateChatReadApi()
      unreadCount.value = Math.max(unreadCount.value, 0)
      chatSessions.value = chatSessions.value.map(item =>
        item.peerId === session.peerId ? { ...item, unreadCount: 0 } : item
      )
    } catch {
      // 进入私聊页后也会同步已读状态，这里失败不阻止跳转。
    }
  }
  router.push(`/chat/${session.peerId}`)
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadMessages(), loadAuditList()])
})
</script>

<style scoped>
.message-page {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 0 32px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  backdrop-filter: blur(18px);
}

.brand,
.top-actions,
.hero-card,
.message-card,
.message-head,
.actions {
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
  border-radius: 14px;
  display: grid;
  place-items: center;
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

.message-main {
  width: min(1180px, calc(100% - 44px));
  margin: 0 auto;
  padding: 28px 0 44px;
}

.hero-card {
  justify-content: space-between;
  gap: 20px;
  padding: 30px;
  border-radius: var(--radius);
  background: var(--hero-gradient), var(--primary);
  color: #fff;
  box-shadow: var(--shadow);
}

.eyebrow {
  margin: 0 0 10px;
  font-size: 13px;
  opacity: 0.82;
  font-weight: 700;
  text-transform: uppercase;
}

.hero-card h2 {
  margin: 0;
  font-size: 32px;
}

.hero-card p {
  margin: 10px 0 0;
  opacity: 0.86;
}

.unread-box {
  min-width: 120px;
  padding: 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
  text-align: center;
}

.unread-box strong {
  display: block;
  font-size: 30px;
}

.unread-box span {
  display: block;
  margin-top: 4px;
  font-size: 13px;
  opacity: 0.82;
}

.filter-row {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 20px 2px 2px;
}

.filter-pill {
  height: 40px;
  flex: 0 0 auto;
  position: relative;
  padding: 0 16px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--surface);
  color: var(--muted);
  cursor: pointer;
}

.filter-badge {
  min-width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-left: 6px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 12px;
  line-height: 1;
}

.filter-pill.active,
.filter-pill:hover {
  background: var(--theme-tint);
  color: var(--primary-deep);
  border-color: var(--primary);
}

.audit-panel,
.message-panel,
.message-card {
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.audit-panel {
  margin-top: 20px;
  padding: 20px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.panel-head h3 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 18px;
}

.panel-head p {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.audit-list {
  display: grid;
  gap: 12px;
}

.audit-card {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border-radius: 14px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.audit-main {
  min-width: 0;
}

.audit-head {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--muted);
  font-size: 12px;
}

.audit-main h3 {
  margin: 10px 0 6px;
  color: var(--primary-deep);
  font-size: 16px;
}

.audit-main p {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.6;
}

.audit-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-self: center;
  justify-content: flex-end;
}

.message-panel {
  min-height: 320px;
  display: grid;
  gap: 16px;
  margin-top: 20px;
  padding: 20px;
}

.message-card {
  align-items: flex-start;
  gap: 16px;
  padding: 22px;
}

.message-card.unread {
  border-color: rgba(245, 158, 11, 0.35);
}

.chat-session-card {
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease;
}

.chat-session-card:hover {
  transform: translateY(-1px);
  border-color: var(--primary);
}

.message-icon {
  width: 42px;
  height: 42px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: var(--theme-tint-soft);
  color: var(--primary);
}

.message-icon.icon-REPLY {
  background: rgba(59, 130, 246, 0.12);
  color: #2563eb;
}

.message-icon.icon-LIKE {
  background: rgba(236, 72, 153, 0.12);
  color: #db2777;
}

.message-icon.icon-PRIVATE_CHAT {
  background: var(--theme-tint-soft);
  color: var(--primary);
}

.message-icon.icon-NOTICE,
.message-icon.icon-SYSTEM,
.message-icon.icon-AUDIT {
  background: rgba(245, 158, 11, 0.12);
  color: #d97706;
}

.message-body {
  min-width: 0;
  flex: 1;
}

.message-head {
  justify-content: space-between;
  gap: 16px;
}

.message-head h3 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 18px;
}

.message-head span {
  display: block;
  margin-top: 5px;
  color: var(--muted);
  font-size: 13px;
}

.message-body p {
  margin: 14px 0 0;
  color: var(--text);
  line-height: 1.8;
  white-space: pre-wrap;
}

.actions {
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
  flex-wrap: wrap;
}

@media (max-width: 760px) {
  .topbar,
  .hero-card {
    height: auto;
    align-items: flex-start;
    flex-direction: column;
    padding: 16px 22px;
  }

  .message-main {
    width: min(100% - 28px, 1180px);
    padding-top: 18px;
  }

  .audit-card,
  .message-card,
  .message-head {
    flex-direction: column;
  }

  .actions,
  .audit-actions,
  .actions .el-button,
  .audit-actions .el-button {
    width: 100%;
  }
}
</style>

