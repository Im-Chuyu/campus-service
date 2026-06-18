<template>
  <div class="detail-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>内容详情</p>
        </div>
      </div>

      <div class="top-actions">
        <el-button :icon="ArrowLeft" @click="$router.push('/home')">返回首页</el-button>
        <el-button :icon="ArrowLeft" @click="$router.back()">返回上一页</el-button>
        <el-button
          v-if="currentUserId && detail && Number(detail.userId) === Number(currentUserId)"
          :icon="EditPen"
          @click="goEditContent"
          >
          修改帖子
        </el-button>
        <el-button
          v-if="detail && (userStore.isAdmin || Number(detail.userId) === Number(currentUserId))"
          type="danger"
          plain
          :icon="Delete"
          @click="handleDeleteContent"
        >
          删除帖子
        </el-button>
        <el-button type="primary" :icon="EditPen" @click="goAuthRequired('/publish')">
          发布内容
        </el-button>
      </div>
    </header>

    <main class="detail-main" v-loading="loading">
      <el-empty v-if="!loading && !detail" description="内容不存在" />

      <template v-else-if="detail">
        <div class="detail-layout">
          <section class="article">
            <div class="article-head">
              <div class="tag-row">
                <el-tag :type="tagType">{{ categoryName }}</el-tag>
                <el-tag v-if="detail.isTop === 1" type="warning">置顶</el-tag>
                <el-tag v-if="detail.isPrivate === 1" type="info">私密</el-tag>
                <span>{{ formatTime(detail.createTime) }}</span>
              </div>

              <div class="author-chip" @click="goUserProfile(detail.userId)">
                <UserAvatar
                  :size="44"
                  :src="contentAuthor?.avatar"
                  :text="contentAuthor?.nickname || contentAuthor?.username || 'U'"
                  :role-id="contentAuthor?.roleId"
                />
                <div>
                  <strong>{{ contentAuthor?.nickname || contentAuthor?.username || '未知用户' }}</strong>
                </div>
              </div>

              <h2>{{ detail.title }}</h2>

              <div v-if="isTradeContent" class="trade-summary">
                <div>
                  <span>交易价格</span>
                  <strong>￥{{ formatPrice(detail.price) }}</strong>
                </div>
                <div>
                  <span>物品分类</span>
                  <strong>{{ detail.tradeType || '未填写' }}</strong>
                </div>
                <div>
                  <span>新旧程度</span>
                  <strong>{{ detail.tradeCondition || '未填写' }}</strong>
                </div>
                <div>
                  <span>交易状态</span>
                  <strong>{{ tradeStatusText }}</strong>
                </div>
              </div>

              <div class="meta-row">
                <span><el-icon><View /></el-icon>{{ detail.viewCount || 0 }} 浏览</span>
                <span><el-icon><Pointer /></el-icon>{{ detail.likeCount || 0 }} 点赞</span>
                <span><el-icon><Star /></el-icon>{{ detail.favoriteCount || 0 }} 收藏</span>
                <span><el-icon><ChatDotRound /></el-icon>{{ detail.commentCount || 0 }} 评论</span>
              </div>

              <div v-if="canViewAuditReason && detail.auditReason" class="audit-reason">
                <span>审核原因</span>
                <strong>{{ detail.auditReason }}</strong>
              </div>
            </div>

            <div v-if="mediaItems.length" class="media-gallery">
              <div
                v-for="(item, index) in mediaItems"
                :key="item.url"
                class="media-item"
                role="button"
                tabindex="0"
                @click="openMediaPreview(index)"
                @keydown.enter.prevent="openMediaPreview(index)"
                @keydown.space.prevent="openMediaPreview(index)"
              >
                <img v-if="item.type === 'image'" :src="item.url" :alt="detail.title" loading="lazy" />
                <video v-else :src="item.url" controls />
                <span class="media-tip">{{ item.type === 'image' ? '点击查看大图' : '点击放大播放' }}</span>
              </div>
            </div>

            <div class="content-body">
              {{ detail.content }}
            </div>
          </section>

          <aside class="side">
            <section class="side-panel">
              <h3>内容操作</h3>

              <div class="action-grid">
                <el-button
                  :icon="Pointer"
                  :type="liked ? 'primary' : 'default'"
                  :loading="actionLoading"
                  @click="handleToggleLike"
                >
                  {{ liked ? '已点赞' : '点赞' }}
                </el-button>

                <el-button
                  :icon="Star"
                  :type="favorited ? 'warning' : 'default'"
                  :loading="actionLoading"
                  @click="handleToggleFavorite"
                >
                  {{ favorited ? '已收藏' : '收藏' }}
                </el-button>

                <el-button :icon="Warning" @click="openReportDialog">举报</el-button>

                <el-button
                  v-if="isTradeContent && Number(detail.userId) !== Number(currentUserId)"
                  type="success"
                  :icon="ChatDotRound"
                  @click="goChatSeller"
                >
                  联系卖家
                </el-button>

                <el-button
                  v-if="userStore.isAdmin"
                  type="warning"
                  plain
                  :icon="Star"
                  :loading="topSubmitting"
                  @click="handleToggleTop"
                >
                  {{ detail.isTop === 1 ? '取消置顶' : '置顶帖子' }}
                </el-button>

                <el-button
                  v-if="userStore.isAdmin"
                  type="warning"
                  plain
                  :icon="RefreshLeft"
                  :loading="returnSubmitting"
                  @click="openReturnDialog"
                >
                  撤销帖子
                </el-button>
              </div>
            </section>

            <section class="side-panel">
              <h3>发布信息</h3>

              <div class="info-list">
                <div>
                  <span>发布人</span>
                  <strong>{{ contentAuthor?.nickname || contentAuthor?.username || `用户${detail.userId}` }}</strong>
                </div>
                <div>
                  <span>分类</span>
                  <strong>{{ categoryName }}</strong>
                </div>
                <div>
                  <span>可见范围</span>
                  <strong>{{ detail.isPrivate === 1 ? '私密' : '公开' }}</strong>
                </div>
                <template v-if="isTradeContent">
                  <div>
                    <span>价格</span>
                    <strong>￥{{ formatPrice(detail.price) }}</strong>
                  </div>
                  <div>
                    <span>交易状态</span>
                    <strong>{{ tradeStatusText }}</strong>
                  </div>
                </template>
                <div>
                  <span>发布时间</span>
                  <strong>{{ formatTime(detail.createTime) }}</strong>
                </div>
                <div>
                  <span>更新时间</span>
                  <strong>{{ formatTime(detail.updateTime) }}</strong>
                </div>
              </div>
            </section>
          </aside>

          <section class="comments">
            <div class="section-head">
              <h3>评论区</h3>
              <span>{{ displayCommentTotal }} 条评论</span>
            </div>

            <el-form
              ref="commentFormRef"
              :model="commentForm"
              :rules="commentRules"
              class="comment-form"
            >
              <div v-if="replyTarget" class="reply-banner">
                <span>正在回复 {{ getCommentUserName(replyTarget) }}</span>
                <el-button link type="primary" @click="cancelReply">取消回复</el-button>
              </div>

              <el-form-item prop="commentText">
                <el-input
                  v-model="commentForm.commentText"
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  show-word-limit
                  resize="none"
                  :placeholder="replyTarget ? `回复 ${getCommentUserName(replyTarget)}...` : '写下你的想法...'"
                />
              </el-form-item>

              <div class="comment-submit">
                <el-button v-if="replyTarget" @click="cancelReply">取消</el-button>
                <el-button type="primary" :loading="commentSubmitting" @click="handleSubmitComment">
                  {{ replyTarget ? '回复评论' : '发表评论' }}
                </el-button>
              </div>
            </el-form>

            <div class="comment-list" v-loading="commentLoading">
              <el-empty
                v-if="!commentLoading && rootComments.length === 0"
                description="暂无评论"
              />

              <template v-for="comment in rootComments" :key="comment.id">
                <article class="comment-item">
                  <UserAvatar
                    clickable
                    :size="49"
                    :src="comment.avatar"
                    :text="getCommentUserName(comment)"
                    :role-id="comment.roleId"
                    @click="goUserProfile(comment.userId)"
                  />

                  <div class="comment-body">
                    <div class="comment-head">
                      <div>
                        <strong>
                          {{ getCommentUserName(comment) }}
                          <el-tag v-if="Number(comment.isTop) === 1" size="small" type="warning" effect="plain">
                            置顶
                          </el-tag>
                        </strong>
                        <span>{{ formatTime(comment.createTime) }}</span>
                      </div>

                      <div class="comment-actions">
                        <el-button
                          link
                          :type="Number(comment.myReactionType) === 1 ? 'primary' : 'default'"
                          :icon="CaretTop"
                          @click="handleReactComment(comment, 1)"
                        >
                          {{ comment.likeCount || 0 }}
                        </el-button>

                        <el-button
                          link
                          :type="Number(comment.myReactionType) === 2 ? 'danger' : 'default'"
                          :icon="CaretBottom"
                          @click="handleReactComment(comment, 2)"
                        >
                          {{ comment.dislikeCount || 0 }}
                        </el-button>

                        <el-button
                          link
                          type="primary"
                          :icon="ChatDotRound"
                          @click="handleReplyComment(comment)"
                        >
                          回复
                        </el-button>

                        <el-button
                          v-if="canManageCommentTop"
                          link
                          type="warning"
                          :icon="Star"
                          :loading="topCommentActionId === comment.id"
                          @click="handleToggleCommentTop(comment)"
                        >
                          {{ Number(comment.isTop) === 1 ? '取消置顶' : '置顶' }}
                        </el-button>

                        <el-button
                          v-if="canDeleteComment(comment)"
                          link
                          type="danger"
                          :icon="Delete"
                          :loading="deletingCommentId === comment.id"
                          @click="handleDeleteComment(comment)"
                        >
                          删除
                        </el-button>
                      </div>
                    </div>

                    <p class="comment-text">
                      {{ comment.commentText || comment.content || '这条评论没有内容' }}
                    </p>

                    <div v-if="getReplies(comment.id).length" class="reply-list">
                      <article
                        v-for="reply in getReplies(comment.id)"
                        :key="reply.id"
                        class="reply-item"
                      >
                        <UserAvatar
                          clickable
                          :size="39"
                          :src="reply.avatar"
                          :text="getCommentUserName(reply)"
                          :role-id="reply.roleId"
                          @click="goUserProfile(reply.userId)"
                        />

                        <div class="reply-body">
                          <div class="comment-head">
                            <div>
                              <strong>
                                {{ getCommentUserName(reply) }}
                                <el-tag v-if="Number(reply.isTop) === 1" size="small" type="warning" effect="plain">
                                  置顶
                                </el-tag>
                              </strong>
                              <span>{{ formatTime(reply.createTime) }}</span>
                            </div>

                            <div class="comment-actions">
                              <el-button
                                link
                                :type="Number(reply.myReactionType) === 1 ? 'primary' : 'default'"
                                :icon="CaretTop"
                                @click="handleReactComment(reply, 1)"
                              >
                                {{ reply.likeCount || 0 }}
                              </el-button>

                              <el-button
                                link
                                :type="Number(reply.myReactionType) === 2 ? 'danger' : 'default'"
                                :icon="CaretBottom"
                                @click="handleReactComment(reply, 2)"
                              >
                                {{ reply.dislikeCount || 0 }}
                              </el-button>

                              <el-button
                                link
                                type="primary"
                                :icon="ChatDotRound"
                                @click="handleReplyComment(reply)"
                              >
                                回复
                              </el-button>

                              <el-button
                                v-if="canManageCommentTop"
                                link
                                type="warning"
                                :icon="Star"
                                :loading="topCommentActionId === reply.id"
                                @click="handleToggleCommentTop(reply)"
                              >
                                {{ Number(reply.isTop) === 1 ? '取消置顶' : '置顶' }}
                              </el-button>

                              <el-button
                                v-if="canDeleteComment(reply)"
                                link
                                type="danger"
                                :icon="Delete"
                                :loading="deletingCommentId === reply.id"
                                @click="handleDeleteComment(reply)"
                              >
                                删除
                              </el-button>
                            </div>
                          </div>

                          <p class="comment-text">
                            {{ reply.commentText || reply.content || '这条评论没有内容' }}
                          </p>
                        </div>
                      </article>
                    </div>
                  </div>
                </article>
              </template>
            </div>

            <div v-if="commentHasMore" class="comment-more">
              <el-button :loading="commentLoadingMore" @click="loadMoreComments">
                加载更多评论
              </el-button>
            </div>
          </section>
        </div>
      </template>
    </main>

    <el-dialog v-model="reportDialogVisible" title="举报内容" width="520px">
      <el-form ref="reportFormRef" :model="reportForm" :rules="reportRules" label-position="top">
        <el-form-item label="举报类型" prop="reportType">
          <el-select v-model="reportForm.reportType" class="full-width" placeholder="请选择举报类型">
            <el-option label="虚假信息" value="虚假信息" />
            <el-option label="违法违规" value="违法违规" />
            <el-option label="广告骚扰" value="广告骚扰" />
            <el-option label="不友善内容" value="不友善内容" />
            <el-option label="其他问题" value="其他问题" />
          </el-select>
        </el-form-item>
        <el-form-item label="举报原因" prop="reportReason">
          <el-input
            v-model="reportForm.reportReason"
            type="textarea"
            :rows="4"
            maxlength="255"
            show-word-limit
            placeholder="请说明具体原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reportSubmitting" @click="handleSubmitReport">
          提交举报
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="returnDialogVisible" title="撤销帖子" width="520px">
      <el-form ref="returnFormRef" :model="returnForm" :rules="returnRules" label-position="top">
        <el-form-item label="撤销原因" prop="reason">
          <el-input
            v-model="returnForm.reason"
            type="textarea"
            :rows="4"
            maxlength="255"
            show-word-limit
            placeholder="请说明撤销原因，发布者会收到通知"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="returnSubmitting" @click="handleReturnAudit">
          确认撤销
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="authDialogVisible" title="登录后继续使用" width="360px" class="auth-dialog">
      <p>登录或注册后可以发布内容、点赞收藏、发表评论、举报和发起私聊。</p>
      <template #footer>
        <el-button @click="router.push('/register')">注册账号</el-button>
        <el-button type="primary" @click="router.push('/login')">去登录</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="mediaPreviewVisible"
      class="media-preview-dialog"
      width="min(92vw, 1100px)"
      append-to-body
      destroy-on-close
      :show-close="true"
    >
      <template #header>
        <div class="media-preview-head">
          <span>{{ currentPreviewItem?.type === 'image' ? '图片预览' : '视频预览' }}</span>
          <small>{{ previewMediaIndex + 1 }} / {{ mediaItems.length }}</small>
        </div>
      </template>

      <div v-if="currentPreviewItem" class="media-preview-body">
        <el-button
          v-if="mediaItems.length > 1"
          class="preview-nav preview-prev"
          circle
          :icon="ArrowLeft"
          @click="switchMediaPreview(-1)"
        />
        <img
          v-if="currentPreviewItem.type === 'image'"
          :src="currentPreviewItem.url"
          :alt="detail?.title || '图片预览'"
        />
        <video v-else :src="currentPreviewItem.url" controls autoplay />
        <el-button
          v-if="mediaItems.length > 1"
          class="preview-nav preview-next"
          circle
          :icon="ArrowRight"
          @click="switchMediaPreview(1)"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ArrowRight,
  ChatDotRound,
  CaretBottom,
  CaretTop,
  Delete,
  EditPen,
  Pointer,
  RefreshLeft,
  Star,
  User,
  View,
  Warning
} from '@element-plus/icons-vue'
import { deleteContentApi, getContentDetailApi, getPublicContentDetailApi, returnContentAuditApi, toggleContentTopApi } from '../api/content'
import { getCategoryListApi } from '../api/category'
import { addCommentApi, deleteCommentApi, getCommentPageApi, reactCommentApi, toggleCommentTopApi } from '../api/comment'
import { getUserInfoByIdApi } from '../api/user'
import { useUserStore } from '../stores/user'
import { addLikeApi, deleteLikeApi, getMyLikeListApi } from '../api/like'
import { addFavoriteApi, deleteFavoriteApi, getMyFavoriteListApi } from '../api/favorite'
import { addReportApi } from '../api/report'
import UserAvatar from '../components/UserAvatar.vue'
import { formatBeijingDateTime } from '../utils/time'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const actionLoading = ref(false)
const authDialogVisible = ref(false)
const liked = ref(false)
const favorited = ref(false)
const detail = ref(null)
const contentAuthor = ref(null)
const categories = ref([])
const mediaPreviewVisible = ref(false)
const previewMediaIndex = ref(0)

const reportDialogVisible = ref(false)
const reportSubmitting = ref(false)
const reportFormRef = ref()
const reportForm = reactive({
  reportType: '',
  reportReason: ''
})
const reportRules = {
  reportType: [{ required: true, message: '请选择举报类型', trigger: 'change' }],
  reportReason: [{ required: true, message: '请输入举报原因', trigger: 'blur' }]
}

const returnDialogVisible = ref(false)
const returnSubmitting = ref(false)
const topSubmitting = ref(false)
const returnFormRef = ref()
const returnForm = reactive({
  reason: ''
})
const returnRules = {
  reason: [{ required: true, message: '请输入撤销原因', trigger: 'blur' }]
}

const commentLoading = ref(false)
const commentLoadingMore = ref(false)
const commentSubmitting = ref(false)
const deletingCommentId = ref(null)
const topCommentActionId = ref(null)
const comments = ref([])
const commentPage = ref(1)
const commentPageSize = 20
const commentTotal = ref(0)
const commentHasMore = ref(false)
const commentFormRef = ref()
const commentForm = reactive({
  commentText: ''
})
const commentRules = {
  commentText: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 1, max: 500, message: '评论长度应为 1 到 500 个字符', trigger: 'blur' }
  ]
}
const replyTarget = ref(null)

const categoryName = computed(() => {
  const category = categories.value.find(item => item.id === detail.value?.categoryId)
  return detail.value?.categoryName || category?.name || '未分类'
})

const tagType = computed(() => {
  const map = {
    '失物招领': 'success',
    '二手交易': 'warning',
    '活动通知': 'primary',
    '校园求助': 'danger',
    '课程资料': 'info',
    '其他服务': ''
  }
  return map[categoryName.value] || ''
})

const isTradeContent = computed(() => categoryName.value === '二手交易')

const tradeStatusText = computed(() => {
  const map = { 0: '在售', 1: '已预定', 2: '已售出' }
  return map[Number(detail.value?.tradeStatus ?? 0)] || '在售'
})

const mediaItems = computed(() => {
  const raw = detail.value?.mediaUrls
  if (raw) {
    try {
      const list = JSON.parse(raw)
      if (Array.isArray(list)) {
        return list.filter(Boolean).map(url => ({ url, type: getMediaType(url) }))
      }
    } catch {
      return [{ url: raw, type: getMediaType(raw) }]
    }
  }
  if (detail.value?.coverImg) {
    return [{ url: detail.value.coverImg, type: getMediaType(detail.value.coverImg) }]
  }
  return []
})

const currentPreviewItem = computed(() => mediaItems.value[previewMediaIndex.value] || null)

const statusText = computed(() => {
  const map = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已下架'
  }
  return map[detail.value?.status] || '未知'
})

const currentUserId = computed(() => userStore.userInfo?.id)
const displayCommentTotal = computed(() => Number(detail.value?.commentCount ?? commentTotal.value ?? 0))
const canViewAuditReason = computed(() => {
  if (!detail.value?.auditReason) return false
  if (userStore.isAdmin) return true
  return Number(currentUserId.value) === Number(detail.value?.userId)
})

const canManageCommentTop = computed(() => {
  if (!detail.value || !currentUserId.value) return false
  return userStore.isAdmin || Number(detail.value.userId) === Number(currentUserId.value)
})

const commentMap = computed(() => {
  return new Map(comments.value.map(item => [Number(item.id), item]))
})

const rootComments = computed(() => {
  return comments.value.filter(comment => {
    const parentId = comment.parentId == null ? null : Number(comment.parentId)
    return parentId == null || !commentMap.value.has(parentId)
  })
})

const formatTime = value => {
  return formatBeijingDateTime(value)
}

const formatPrice = value => Number(value || 0).toFixed(2)

const getMediaType = url => /\.(mp4|webm|mov)$/i.test(String(url).split('?')[0]) ? 'video' : 'image'

const getCommentUserName = comment => {
  return comment.nickname || comment.username || comment.userName || `用户${comment.userId || ''}`
}

const canDeleteComment = comment => {
  return Number(comment.userId) === Number(currentUserId.value) || userStore.isAdmin
}

const getReplies = parentId => {
  return comments.value.filter(comment => Number(comment.parentId) === Number(parentId))
}

const goUserProfile = userId => {
  if (!userId) return
  router.push(`/profile/${userId}`)
}

const goEditContent = () => {
  if (!detail.value?.id) return
  router.push(`/content/edit/${detail.value.id}`)
}

const showAuthDialog = () => {
  authDialogVisible.value = true
}

const ensureLogin = () => {
  if (userStore.isLogin) return true
  showAuthDialog()
  return false
}

const goAuthRequired = path => {
  if (!ensureLogin()) return
  router.push(path)
}

const goChatSeller = () => {
  if (!ensureLogin()) return
  if (!detail.value?.userId) return
  router.push(`/chat/${detail.value.userId}`)
}

const openMediaPreview = index => {
  previewMediaIndex.value = index
  mediaPreviewVisible.value = true
}

const switchMediaPreview = delta => {
  const total = mediaItems.value.length
  if (!total) return
  previewMediaIndex.value = (previewMediaIndex.value + delta + total) % total
}

const handleDeleteContent = async () => {
  if (!detail.value?.id) return

  await ElMessageBox.confirm(`确定删除帖子「${detail.value.title}」吗？删除后不可恢复。`, '删除帖子', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning'
  })

  await deleteContentApi(detail.value.id)
  ElMessage.success('帖子已删除')
  router.push(isTradeContent.value ? '/trade' : '/home')
}

const handleReactComment = async (comment, reactionType) => {
  if (!ensureLogin()) return
  const currentReaction = Number(comment.myReactionType || 0)
  const nextReaction = currentReaction === reactionType ? 0 : reactionType

  await reactCommentApi(comment.id, nextReaction)
  await loadComments()
}

const handleReplyComment = comment => {
  if (!ensureLogin()) return
  replyTarget.value = comment
}

const cancelReply = () => {
  replyTarget.value = null
}

const loadDetail = async () => {
  loading.value = true
  try {
    const [contentRes, categoryRes] = await Promise.all([
      userStore.isLogin ? getContentDetailApi(route.params.id) : getPublicContentDetailApi(route.params.id),
      getCategoryListApi()
    ])
    detail.value = contentRes.data
    categories.value = categoryRes.data || []
    contentAuthor.value = null
    if (detail.value?.userId) {
      const authorRes = await getUserInfoByIdApi(detail.value.userId)
      contentAuthor.value = authorRes.data || null
    }
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  commentLoading.value = true
  try {
    commentPage.value = 1
    const res = await getCommentPageApi(route.params.id, {
      page: commentPage.value,
      pageSize: commentPageSize
    })
    const data = res.data || {}
    comments.value = data.records || []
    commentTotal.value = Number(data.total || 0)
    commentHasMore.value = Boolean(data.hasMore)
  } finally {
    commentLoading.value = false
  }
}

const reloadLoadedComments = async () => {
  const loadedPages = Math.max(commentPage.value, 1)
  const res = await getCommentPageApi(route.params.id, {
    page: 1,
    pageSize: loadedPages * commentPageSize
  })
  const data = res.data || {}
  comments.value = data.records || []
  commentTotal.value = Number(data.total || 0)
  commentHasMore.value = Boolean(data.hasMore)
}

const loadMoreComments = async () => {
  if (commentLoadingMore.value || !commentHasMore.value) return
  commentLoadingMore.value = true
  try {
    const nextPage = commentPage.value + 1
    const res = await getCommentPageApi(route.params.id, {
      page: nextPage,
      pageSize: commentPageSize
    })
    const data = res.data || {}
    comments.value = comments.value.concat(data.records || [])
    commentPage.value = nextPage
    commentTotal.value = Number(data.total || 0)
    commentHasMore.value = Boolean(data.hasMore)
  } finally {
    commentLoadingMore.value = false
  }
}

const loadLikeState = async () => {
  if (!userStore.isLogin) {
    liked.value = false
    return
  }
  const res = await getMyLikeListApi()
  const list = res.data || []
  const currentId = Number(route.params.id)
  liked.value = list.some(item => Number(item.contentId ?? item.content_id) === currentId)
}

const loadFavoriteState = async () => {
  if (!userStore.isLogin) {
    favorited.value = false
    return
  }
  const res = await getMyFavoriteListApi()
  const list = res.data || []
  const currentId = Number(route.params.id)
  favorited.value = list.some(item => Number(item.contentId ?? item.content_id) === currentId)
}

const handleSubmitComment = async () => {
  if (!ensureLogin()) return
  await commentFormRef.value.validate()
  commentSubmitting.value = true
  try {
    await addCommentApi({
      contentId: Number(route.params.id),
      parentId: replyTarget.value?.id ?? null,
      commentText: commentForm.commentText
    })
    ElMessage.success(replyTarget.value ? '回复成功' : '评论成功')
    commentFormRef.value.resetFields()
    replyTarget.value = null
    await reloadLoadedComments()
    if (detail.value) {
      detail.value.commentCount = Number(detail.value.commentCount || 0) + 1
    }
  } finally {
    commentSubmitting.value = false
  }
}

const handleDeleteComment = async comment => {
  await ElMessageBox.confirm('确定删除这条评论吗？', '删除评论', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  })

  deletingCommentId.value = comment.id
  try {
    await deleteCommentApi(comment.id)
    if (replyTarget.value?.id === comment.id) {
      replyTarget.value = null
    }
    ElMessage.success('删除成功')
    await reloadLoadedComments()
    if (detail.value) {
      detail.value.commentCount = Math.max(Number(detail.value.commentCount || 0) - 1, 0)
    }
  } finally {
    deletingCommentId.value = null
  }
}

const handleToggleCommentTop = async comment => {
  if (!ensureLogin()) return
  if (!comment?.id) return

  topCommentActionId.value = comment.id
  try {
    await toggleCommentTopApi(comment.id)
    ElMessage.success(Number(comment.isTop) === 1 ? '已取消置顶' : '评论已置顶')
    await reloadLoadedComments()
  } finally {
    topCommentActionId.value = null
  }
}

const handleToggleLike = async () => {
  if (!ensureLogin()) return
  if (!detail.value) return

  actionLoading.value = true
  try {
    if (liked.value) {
      await deleteLikeApi(detail.value.id)
      liked.value = false
      detail.value.likeCount = Math.max((detail.value.likeCount || 1) - 1, 0)
      ElMessage.success('已取消点赞')
    } else {
      await addLikeApi(detail.value.id)
      liked.value = true
      detail.value.likeCount = (detail.value.likeCount || 0) + 1
      ElMessage.success('点赞成功')
    }
  } finally {
    actionLoading.value = false
  }
}

const handleToggleFavorite = async () => {
  if (!ensureLogin()) return
  if (!detail.value) return

  actionLoading.value = true
  try {
    if (favorited.value) {
      await deleteFavoriteApi(detail.value.id)
      favorited.value = false
      detail.value.favoriteCount = Math.max((detail.value.favoriteCount || 1) - 1, 0)
      ElMessage.success('已取消收藏')
    } else {
      await addFavoriteApi(detail.value.id)
      favorited.value = true
      detail.value.favoriteCount = (detail.value.favoriteCount || 0) + 1
      ElMessage.success('收藏成功')
    }
  } finally {
    actionLoading.value = false
  }
}

const openReportDialog = () => {
  if (!ensureLogin()) return
  reportForm.reportType = ''
  reportForm.reportReason = ''
  reportDialogVisible.value = true
}

const handleSubmitReport = async () => {
  await reportFormRef.value.validate()

  reportSubmitting.value = true
  try {
    await addReportApi({
      contentId: Number(route.params.id),
      reportType: reportForm.reportType,
      reportReason: reportForm.reportReason
    })
    ElMessage.success('举报已提交')
    reportDialogVisible.value = false
  } finally {
    reportSubmitting.value = false
  }
}

const openReturnDialog = () => {
  returnForm.reason = ''
  returnDialogVisible.value = true
}

const handleReturnAudit = async () => {
  await returnFormRef.value.validate()

  returnSubmitting.value = true
  try {
    await returnContentAuditApi({
      contentId: Number(route.params.id),
      reason: returnForm.reason
    })
    ElMessage.success('已撤销帖子，并通知发布者修改')
    returnDialogVisible.value = false
    await loadDetail()
  } finally {
    returnSubmitting.value = false
  }
}

const handleToggleTop = async () => {
  if (!detail.value?.id) return
  topSubmitting.value = true
  try {
    await toggleContentTopApi(detail.value.id)
    detail.value.isTop = detail.value.isTop === 1 ? 0 : 1
    ElMessage.success(detail.value.isTop === 1 ? '帖子已置顶' : '已取消置顶')
  } finally {
    topSubmitting.value = false
  }
}

onMounted(async () => {
  await loadDetail()
  await Promise.all([loadComments(), loadLikeState(), loadFavoriteState()])
})
</script>

<style scoped>
.detail-page {
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

.brand {
  display: flex;
  align-items: center;
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
  line-height: 1.2;
}

.brand p {
  margin: 3px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-main {
  width: min(1180px, calc(100% - 44px));
  margin: 0 auto;
  padding: 28px 0 40px;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 310px;
  gap: 22px;
}

.article {
  grid-column: 1 / 2;
  padding: 0;
}

.article-head,
.content-body,
.side-panel,
.comments {
  padding: 24px;
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.article-head {
  margin-bottom: 16px;
}

.tag-row,
.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.tag-row {
  color: var(--muted);
  font-size: 13px;
}

.author-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-top: 14px;
  padding: 8px 12px 8px 8px;
  border-radius: 999px;
  background: var(--theme-tint-soft);
  border: 1px solid var(--border);
  cursor: pointer;
}

.author-chip strong {
  display: block;
  color: var(--primary-deep);
  font-size: 14px;
}

.author-chip span {
  display: block;
  color: var(--muted);
  font-size: 12px;
}

.article h2 {
  margin: 16px 0 12px;
  color: var(--primary-deep);
  font-size: 32px;
  line-height: 1.2;
}

.meta-row {
  color: var(--muted);
  font-size: 13px;
}

.trade-summary {
  display: grid;
  grid-template-columns: 1.3fr repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin: 18px 0;
}

.trade-summary div {
  padding: 14px;
  border-radius: 14px;
  background: rgba(245, 158, 11, 0.09);
  border: 1px solid rgba(245, 158, 11, 0.16);
}

.trade-summary span {
  display: block;
  color: var(--muted);
  font-size: 12px;
}

.trade-summary strong {
  display: block;
  margin-top: 6px;
  color: var(--primary-deep);
  font-size: 15px;
}

.trade-summary div:first-child strong {
  color: #dc2626;
  font-size: 26px;
}

.meta-row span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.audit-reason {
  display: grid;
  gap: 6px;
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(245, 158, 11, 0.08);
  border: 1px solid rgba(245, 158, 11, 0.18);
}

.audit-reason span {
  color: var(--muted);
  font-size: 13px;
}

.audit-reason strong {
  color: var(--text);
  line-height: 1.7;
  white-space: pre-wrap;
}

.media-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.media-item {
  position: relative;
  overflow: hidden;
  min-height: 180px;
  max-height: 260px;
  aspect-ratio: 16 / 10;
  border-radius: 8px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
  cursor: zoom-in;
}

.media-item img,
.media-item video {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
}

.media-item video {
  cursor: pointer;
}

.media-tip {
  position: absolute;
  right: 10px;
  bottom: 10px;
  padding: 5px 8px;
  border-radius: 6px;
  background: rgba(17, 24, 39, 0.72);
  color: #fff;
  font-size: 12px;
  line-height: 1;
  opacity: 0;
  transition: opacity 0.16s ease;
  pointer-events: none;
}

.media-item:hover .media-tip {
  opacity: 1;
}

.media-preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: var(--primary-deep);
  font-weight: 700;
}

.media-preview-head small {
  color: var(--muted);
  font-weight: 500;
}

.media-preview-body {
  position: relative;
  display: grid;
  place-items: center;
  min-height: min(72vh, 720px);
  background: #0f172a;
  border-radius: 8px;
  overflow: hidden;
}

.media-preview-body img,
.media-preview-body video {
  max-width: 100%;
  max-height: min(72vh, 720px);
  display: block;
  object-fit: contain;
}

.preview-nav {
  position: absolute;
  top: 50%;
  z-index: 2;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.88);
  border: 0;
}

.preview-prev {
  left: 14px;
}

.preview-next {
  right: 14px;
}

.cover {
  margin-bottom: 16px;
  overflow: hidden;
  border-radius: 18px;
  background: var(--surface-soft);
}

.cover img {
  width: 100%;
  display: block;
  object-fit: cover;
}

.content-body {
  color: var(--text);
  line-height: 1.9;
  white-space: pre-wrap;
}

.side {
  display: grid;
  gap: 16px;
  align-content: start;
}

.side-panel h3,
.section-head h3 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 18px;
}

.action-grid {
  display: grid;
  gap: 10px;
  margin-top: 16px;
}

.action-grid .el-button {
  width: 100%;
  margin-left: 0;
  justify-content: center;
}

.info-list {
  display: grid;
  gap: 14px;
  margin-top: 16px;
}

.info-list div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}

.info-list div:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.info-list span {
  color: var(--muted);
  font-size: 13px;
}

.info-list strong {
  color: var(--text);
  font-size: 13px;
  text-align: right;
}

.comments {
  grid-column: 1 / -1;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.section-head span {
  color: var(--muted);
  font-size: 14px;
}

.comment-form {
  margin-bottom: 22px;
}

.reply-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  margin-bottom: 14px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--theme-tint-soft);
  color: var(--primary-deep);
  font-size: 14px;
}

.comment-submit {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.comment-list {
  min-height: 120px;
  display: grid;
  gap: 16px;
}

.comment-item,
.reply-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.clickable-avatar {
  cursor: pointer;
}

.comment-body,
.reply-body {
  min-width: 0;
  flex: 1;
}

.comment-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.comment-actions .el-button {
  padding-left: 0;
  padding-right: 0;
}

.comment-head strong {
  display: block;
  color: var(--primary-deep);
  font-size: 14px;
}

.comment-head span {
  display: block;
  margin-top: 4px;
  color: var(--muted);
  font-size: 12px;
}

.comment-text {
  margin: 10px 0 0;
  color: var(--text);
  line-height: 1.8;
  white-space: pre-wrap;
}

.reply-list {
  display: grid;
  gap: 12px;
  margin-top: 14px;
  padding-left: 14px;
  border-left: 2px solid var(--border);
}

.reply-item {
  padding: 12px;
  border-radius: 14px;
  background: var(--theme-tint-soft);
  border: 1px solid var(--border);
}

.comment-more {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}

.full-width {
  width: 100%;
}

@media (max-width: 980px) {
  .topbar {
    height: auto;
    padding: 16px 22px;
    align-items: flex-start;
    flex-direction: column;
  }

  .top-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .detail-layout {
    grid-template-columns: 1fr;
  }

  .trade-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .detail-main {
    width: min(100% - 28px, 1180px);
    padding-top: 18px;
  }

  .article-head,
  .content-body,
  .side-panel,
  .comments {
    padding: 22px;
  }

  .article h2 {
    font-size: 24px;
  }

  .trade-summary {
    grid-template-columns: 1fr;
  }
}
</style>

