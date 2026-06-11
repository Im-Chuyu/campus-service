<template>
  <div class="chat-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>私聊</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button :icon="ArrowLeft" @click="$router.push(`/profile/${peerId}`)">返回主页</el-button>
        <el-button :icon="ArrowLeft" @click="$router.back()">返回上一页</el-button>
      </div>
    </header>

    <main class="chat-main" v-loading="loading">
      <section class="chat-shell">
        <div class="chat-head">
          <div class="peer-info">
            <UserAvatar
              clickable
              :size="62"
              :src="peer.avatar"
              :text="peerText"
              :role-id="peer.roleId"
              @click="goPeerProfile"
            />
            <div>
              <h2>{{ peerName }}</h2>
              <p>{{ peerStatusText }}</p>
            </div>
          </div>

          <div class="peer-actions">
            <el-button
              :icon="peer.blockedByMe ? CircleCloseFilled : CircleCheckFilled"
              :type="peer.blockedByMe ? 'danger' : 'warning'"
              @click="toggleBlock"
            >
              {{ peer.blockedByMe ? '取消屏蔽' : '屏蔽用户' }}
            </el-button>
          </div>
        </div>

        <el-alert
          v-if="!peer.canChat"
          class="chat-alert"
          :title="alertText"
          type="warning"
          show-icon
          :closable="false"
        />

        <el-scrollbar ref="scrollbarRef" class="message-list">
          <div v-if="messages.length === 0" class="empty-state">
            <el-empty description="暂无消息" />
          </div>

          <template v-for="item in messageItems" :key="item.key">
            <div v-if="item.type === 'time'" class="time-divider">
              <span>{{ item.label }}</span>
            </div>

            <div
              v-else
              class="message-row"
              :class="{ mine: Number(item.message.senderId) === meId }"
            >
              <UserAvatar
                clickable
                :size="47"
                :src="getMessageAvatar(item.message)"
                :text="getMessageText(item.message)"
                :role-id="getMessageRoleId(item.message)"
                @click="goMessageSenderProfile(item.message)"
              />
              <div class="bubble">
                <p>{{ item.message.content }}</p>
              </div>
            </div>
          </template>
        </el-scrollbar>

        <div class="composer">
          <el-input
            v-model="draft"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            :disabled="!peer.canChat"
            placeholder="请输入消息..."
          />
          <div class="composer-actions">
            <span class="hint">{{ hintText }}</span>
            <el-button type="primary" :disabled="!peer.canChat" :loading="sending" @click="sendMessage">
              发送
            </el-button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import {
  getPrivateChatPeerApi,
  getPrivateChatHistoryApi,
  syncPrivateChatApi,
  sendPrivateChatApi,
  markPrivateChatReadApi,
  blockUserApi,
  unblockUserApi
} from '../api/chat'
import UserAvatar from '../components/UserAvatar.vue'
import { formatBeijingDateTime, parseBeijingTime } from '../utils/time'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const sending = ref(false)
const draft = ref('')
const messages = ref([])
const peer = ref({})
const scrollbarRef = ref()
let refreshTimer = null
let markingRead = false
let emptySyncCount = 0

const ACTIVE_POLL_INTERVAL = 5000
const NORMAL_POLL_INTERVAL = 15000
const IDLE_POLL_INTERVAL = 30000
const NORMAL_AFTER_EMPTY_SYNCS = 2
const IDLE_AFTER_EMPTY_SYNCS = 8
const TIME_DIVIDER_INTERVAL = 10 * 60 * 1000

const meId = computed(() => Number(userStore.userInfo?.id || 0))
const peerId = computed(() => Number(route.params.userId || 0))

const meText = computed(() => (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').slice(0, 1))
const peerText = computed(() => (peer.value.nickname || peer.value.username || 'U').slice(0, 1))
const peerName = computed(() => peer.value.nickname || peer.value.username || '用户')
const getMessageAvatar = message => Number(message.senderId) === meId.value ? userStore.userInfo?.avatar : peer.value.avatar
const getMessageText = message => Number(message.senderId) === meId.value ? meText.value : peerText.value
const getMessageRoleId = message => Number(message.senderId) === meId.value ? userStore.userInfo?.roleId : (message.senderRoleId || peer.value.roleId)
const storageKey = computed(() => {
  if (!meId.value || !peerId.value) return ''
  const ids = [meId.value, peerId.value].sort((a, b) => a - b)
  return `campus-private-chat:${ids[0]}:${ids[1]}`
})

const messageItems = computed(() => {
  const items = []
  let lastTime = null
  for (const message of messages.value) {
    const time = parseMessageTime(message.createTime)
    if (time && (lastTime == null || time - lastTime >= TIME_DIVIDER_INTERVAL)) {
      items.push({
        type: 'time',
        key: `time-${message.id || message.localId || time}`,
        label: formatTime(message.createTime)
      })
      lastTime = time
    }
    items.push({
      type: 'message',
      key: `message-${message.id || message.localId}`,
      message
    })
  }
  return items
})

const peerStatusText = computed(() => {
  if (!peer.value.id) return ''
  if (peer.value.blockedMe) return '对方已屏蔽你'
  if (peer.value.blockedByMe) return '你已屏蔽对方'
  if (peer.value.privateChatEnabled === 0) return '对方已关闭私聊'
  if (peer.value.canChat) return '可以聊天'
  return '当前不可聊天'
})

const goPeerProfile = () => {
  if (!peerId.value) return
  router.push(`/profile/${peerId.value}`)
}

const goMessageSenderProfile = message => {
  const senderId = Number(message?.senderId || 0)
  if (!senderId) return
  router.push(`/profile/${senderId}`)
}

const alertText = computed(() => {
  if (peer.value.blockedMe) return '对方已屏蔽你，无法继续发送消息。'
  if (peer.value.blockedByMe) return '你已屏蔽对方，如需继续聊天请先取消屏蔽。'
  if (peer.value.privateChatEnabled === 0) return '对方已关闭私聊。'
  return '当前无法私聊。'
})

const hintText = computed(() => {
  if (peer.value.blockedMe) return '对方已屏蔽你'
  if (peer.value.blockedByMe) return '你已屏蔽对方'
  if (peer.value.privateChatEnabled === 0) return '私聊已关闭'
  return ''
})

const formatTime = value => {
  return formatBeijingDateTime(value)
}

const parseMessageTime = value => {
  if (!value) return 0
  return parseBeijingTime(value)?.getTime() || 0
}

const sortMessages = list => {
  return [...list].sort((a, b) => {
    const timeDiff = parseMessageTime(a.createTime) - parseMessageTime(b.createTime)
    if (timeDiff !== 0) return timeDiff
    return Number(a.id || 0) - Number(b.id || 0)
  })
}

const readCachedMessages = () => {
  if (!storageKey.value) return []
  try {
    const raw = localStorage.getItem(storageKey.value)
    const list = JSON.parse(raw || '[]')
    return Array.isArray(list) ? list : []
  } catch {
    return []
  }
}

const saveCachedMessages = list => {
  if (!storageKey.value) return
  localStorage.setItem(storageKey.value, JSON.stringify(sortMessages(list)))
}

const mergeMessages = incoming => {
  const map = new Map()
  const serverMessages = []
  const localMessages = []
  const add = message => {
    if (!message?.content) return
    if (message.id != null) {
      serverMessages.push(message)
    } else {
      localMessages.push(message)
    }
  }

  readCachedMessages().forEach(add)
  messages.value.forEach(add)
  ;(incoming || []).forEach(add)

  for (const message of serverMessages) {
    map.set(`server-${message.id}`, message)
  }

  for (const message of localMessages) {
    const localTime = parseMessageTime(message.createTime)
    const duplicated = serverMessages.some(serverMessage => {
      const serverTime = parseMessageTime(serverMessage.createTime)
      return Number(serverMessage.senderId) === Number(message.senderId)
        && Number(serverMessage.receiverId) === Number(message.receiverId)
        && serverMessage.content === message.content
        && Math.abs(serverTime - localTime) < 2 * 60 * 1000
    })
    if (!duplicated) {
      map.set(`local-${message.localId || `${message.senderId}-${message.receiverId}-${message.createTime}-${message.content}`}`, message)
    }
  }

  const merged = sortMessages([...map.values()])
  messages.value = merged
  saveCachedMessages(merged)
}

const markLocalConversationRead = () => {
  let changed = false
  messages.value = messages.value.map(message => {
    if (Number(message.senderId) === peerId.value && Number(message.receiverId) === meId.value && message.isRead !== 1) {
      changed = true
      return { ...message, isRead: 1 }
    }
    return message
  })
  if (changed) saveCachedMessages(messages.value)
}

const maxServerMessageId = () => {
  return messages.value.reduce((max, message) => {
    const id = Number(message.id || 0)
    return id > max ? id : max
  }, 0)
}

const scrollToBottom = async () => {
  await nextTick()
  const wrap = scrollbarRef.value?.wrapRef
  if (wrap) {
    wrap.scrollTop = wrap.scrollHeight
  }
}

const loadPeer = async () => {
  const res = await getPrivateChatPeerApi(peerId.value)
  peer.value = res.data || {}
}

const loadMessages = async () => {
  const cachedMessages = readCachedMessages()
  if (cachedMessages.length) {
    messages.value = sortMessages(cachedMessages)
    await scrollToBottom()
  }

  const res = await getPrivateChatHistoryApi(peerId.value)
  mergeMessages(res.data || [])
  await markConversationRead()
  await scrollToBottom()
}

const syncMessages = async () => {
  try {
    const res = await syncPrivateChatApi(peerId.value, maxServerMessageId())
    const incoming = res.data || []
    if (incoming.length) {
      mergeMessages(incoming)
      await markConversationRead()
      await scrollToBottom()
    }
    return incoming.length
  } catch (error) {
    if (!refreshTimer) {
      throw error
    }
    return 0
  }
}

const markConversationRead = async () => {
  if (!peerId.value || markingRead) return
  markLocalConversationRead()
  markingRead = true
  try {
    await markPrivateChatReadApi(peerId.value)
  } finally {
    markingRead = false
  }
}

const loadPage = async () => {
  if (!peerId.value) return
  loading.value = true
  try {
    await loadPeer()
    await loadMessages()
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  if (!draft.value.trim() || !peer.value.canChat) return
  const content = draft.value.trim()
  const localMessage = {
    localId: `local-${Date.now()}`,
    senderId: meId.value,
    receiverId: peerId.value,
    content,
    isRead: 0,
    createTime: new Date().toISOString(),
    senderNickname: userStore.userInfo?.nickname,
    senderAvatar: userStore.userInfo?.avatar
  }

  mergeMessages([localMessage])
  draft.value = ''
  await scrollToBottom()

  sending.value = true
  try {
    await sendPrivateChatApi({
      receiverId: peerId.value,
      content
    })
    emptySyncCount = 0
    await syncMessages()
    await markConversationRead()
  } finally {
    sending.value = false
  }
}

const toggleBlock = async () => {
  if (!peerId.value) return
  if (peer.value.blockedByMe) {
    await unblockUserApi(peerId.value)
    ElMessage.success('已取消屏蔽')
  } else {
    await blockUserApi(peerId.value)
    ElMessage.success('已屏蔽该用户')
  }
  await loadPeer()
}

const currentPollInterval = () => {
  if (emptySyncCount >= IDLE_AFTER_EMPTY_SYNCS) return IDLE_POLL_INTERVAL
  if (emptySyncCount >= NORMAL_AFTER_EMPTY_SYNCS) return NORMAL_POLL_INTERVAL
  return ACTIVE_POLL_INTERVAL
}

const schedulePolling = () => {
  if (document.hidden) return
  refreshTimer = window.setTimeout(async () => {
    refreshTimer = null
    if (!document.hidden && !loading.value && peerId.value) {
      const incomingCount = await syncMessages()
      emptySyncCount = incomingCount > 0 ? 0 : emptySyncCount + 1
    }
    schedulePolling()
  }, currentPollInterval())
}

const startPolling = () => {
  stopPolling()
  if (document.hidden) return
  schedulePolling()
}

const stopPolling = () => {
  if (refreshTimer) {
    window.clearTimeout(refreshTimer)
    refreshTimer = null
  }
}

const handleVisibilityChange = () => {
  if (document.hidden) {
    stopPolling()
    return
  }
  if (peerId.value) {
    emptySyncCount = 0
    syncMessages()
    markConversationRead()
  }
  startPolling()
}

watch(
  () => route.params.userId,
  async () => {
    stopPolling()
    emptySyncCount = 0
    await loadPage()
    startPolling()
  }
)

onMounted(async () => {
  await loadPage()
  document.addEventListener('visibilitychange', handleVisibilityChange)
  startPolling()
})

onBeforeUnmount(() => {
  stopPolling()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  markConversationRead()
})
</script>

<style scoped>
.chat-page {
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
}

.chat-main {
  max-width: 980px;
  margin: 0 auto;
  padding: 28px 20px 40px;
}

.chat-shell {
  padding: 20px;
  border-radius: 20px;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.chat-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}

.peer-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.peer-info h2 {
  margin: 0;
  font-size: 20px;
}

.peer-info p {
  margin: 6px 0 0;
  color: var(--muted);
}

.chat-alert {
  margin-top: 16px;
}

.message-list {
  height: 520px;
  margin-top: 18px;
  padding-right: 8px;
}

.empty-state {
  padding: 120px 0;
}

.time-divider {
  display: flex;
  justify-content: center;
  margin: 18px 0;
}

.time-divider span {
  padding: 5px 12px;
  border-radius: 999px;
  background: var(--surface-soft);
  color: var(--muted);
  font-size: 12px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.message-row.mine {
  flex-direction: row-reverse;
}

.bubble {
  max-width: min(72%, 560px);
  padding: 12px 14px;
  border-radius: 16px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.message-row.mine .bubble {
  background: var(--primary);
  color: #fff;
}

.bubble p {
  margin: 0;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.bubble span {
  display: block;
  margin-top: 8px;
  color: inherit;
  opacity: 0.68;
  font-size: 12px;
}

.composer {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--border);
}

.composer-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.hint {
  color: var(--muted);
  font-size: 13px;
}

@media (max-width: 768px) {
  .chat-head,
  .composer-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .bubble {
    max-width: 86%;
  }
}
</style>

