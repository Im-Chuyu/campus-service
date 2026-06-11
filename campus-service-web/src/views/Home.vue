<template>
  <div class="home-page">
    <header class="topbar">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">C</div>
        <div>
          <h1>校园服务平台</h1>
          <p>首页</p>
        </div>
      </div>

      <nav class="nav">
        <button class="nav-item active" @click="goHomeCategories">首页</button>
        <button
          class="nav-item"
          @click="$router.push('/trade')"
        >
          二手交易
        </button>
        <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="nav-badge">
          <button class="nav-item" @click="goAuthRequired('/message')">消息</button>
        </el-badge>
        <button class="nav-item" @click="goAuthRequired('/profile')">我的</button>
        <button class="nav-item" @click="goAuthRequired('/friends')">好友</button>
        <button class="nav-item" @click="openFeedbackDialog">反馈</button>
        <button v-if="userStore.isAdmin" class="nav-item" @click="$router.push('/admin')">管理后台</button>
      </nav>

      <div class="user-area">
        <button @click="goAuthRequired('/publish')">
          <el-icon><EditPen /></el-icon>
          <span>发布内容</span>
        </button>

        <el-dropdown v-if="userStore.isLogin">
          <div class="user-chip">
            <UserAvatar
              :size="42"
              :src="userStore.userInfo?.avatar"
              :text="avatarText"
              :role-id="userStore.userInfo?.roleId"
            />
            <span class="user-name">{{ displayName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>

          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/profile')">
                个人中心
              </el-dropdown-item>
              <el-dropdown-item @click="$router.push('/my-content')">
                我的发布
              </el-dropdown-item>
              <el-dropdown-item @click="$router.push('/my-favorite')">
                我的收藏
              </el-dropdown-item>
              <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin')">
                管理后台
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <button v-else class="guest-chip" @click="showAuthDialog">
          <UserAvatar :size="42" text="游" />
          <span class="user-name">游客</span>
          <el-icon><ArrowDown /></el-icon>
        </button>
      </div>
    </header>

    <main class="main">
      <section class="welcome-band" :class="{ 'custom-hero-bg': Boolean(heroNoticeBackground) }" :style="heroBackgroundStyle">
        <div class="welcome-copy">
          <p class="eyebrow">Hi, {{ displayName }}</p>
          <h2>{{ heroNoticeTitle }}</h2>
          <p>{{ heroNoticeContent }}</p>

          <div class="hero-actions">
            <el-button type="primary" :icon="EditPen" @click="goAuthRequired('/publish')">
              发布内容
            </el-button>
          </div>
        </div>

        <div class="campus-note today-note">
          <span>今日栏</span>
          <strong>{{ todayDateText }}</strong>
          <p>{{ currentTimeText }}</p>
          <div class="weather-main">
            <el-icon>
              <component :is="weatherIcon" />
            </el-icon>
            <div>
              <strong>{{ weatherSummary }}</strong>
              <span>{{ weatherLocationText }}</span>
            </div>
          </div>
          <div class="quick-stats weather-stats">
            <div>
              <strong>{{ weatherTemperatureText }}</strong>
              <span>当前温度</span>
            </div>
            <div>
              <strong>{{ weatherWindText }}</strong>
              <span>风速</span>
            </div>
          </div>
        </div>
      </section>

      <section class="toolbar">
        <el-select
          v-model="query.categoryId"
          size="large"
          class="category-select"
          placeholder="搜索分类"
        >
          <el-option
            v-for="item in categories"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>

        <el-input
          v-model="query.keyword"
          size="large"
          class="search"
          placeholder="搜索标题、关键词或内容"
          clearable
          @keyup.enter="reloadFirstPage"
          @clear="reloadFirstPage"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-button size="large" type="primary" :icon="Search" @click="reloadFirstPage">搜索</el-button>

        <el-select
          v-model="query.sort"
          size="large"
          class="sort-select"
          @change="reloadFirstPage"
        >
          <el-option label="最新发布" value="latest" />
          <el-option label="浏览最多" value="views" />
          <el-option label="点赞最多" value="likes" />
          <el-option label="收藏最多" value="favorites" />
        </el-select>
      </section>

      <section class="category-showcase">
        <button
          v-for="item in categories"
          :key="item.id"
          class="category-card"
          :class="{ active: query.categoryId === item.id }"
          @click="query.categoryId = item.id"
        >
          <span class="category-icon">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
          </span>
          <strong>{{ item.name }}</strong>
          <small>{{ item.description }}</small>
        </button>
      </section>

      <div class="content-layout">
        <section class="feed" v-loading="loading && contentList.length === 0">
          <div v-if="showActivitySubCategories" class="activity-subcategory-strip">
            <button
              class="activity-subcategory-card"
              :class="{ active: query.activitySubCategoryId === null }"
              @click="selectActivitySubCategory(null)"
            >
              <strong>全部活动</strong>
              <span>查看所有活动通知</span>
            </button>
            <button
              v-for="item in activitySubCategories"
              :key="item.id"
              class="activity-subcategory-card"
              :class="{ active: query.activitySubCategoryId === item.id }"
              @click="selectActivitySubCategory(item.id)"
            >
              <strong>{{ item.name }}</strong>
              <span>按该分类筛选</span>
            </button>
          </div>

          <div class="feed-head">
            <div>
              <h3>{{ activeCategoryName || '校园精选' }}</h3>
              <p>{{ activeCategoryDescription }}</p>
            </div>
            <span v-if="loading && contentList.length > 0" class="feed-loading-text">加载中...</span>
          </div>

          <article
            v-for="item in contentList"
            :key="item.id"
            class="content-card"
            @click="goDetail(item.id)"
          >
            <div class="card-main">
              <div class="card-head">
                <div class="author-line">
                  <UserAvatar
                    clickable
                    :size="42"
                    :src="item.authorAvatar"
                    :text="item.authorName"
                    :role-id="item.authorRoleId"
                    @click.stop="goUserProfile(item.userId)"
                  />
                  <div>
                    <strong>{{ item.authorName }}</strong>
                    <span>{{ item.createTimeText }}</span>
                  </div>
                </div>
                <div class="tag-stack">
                  <el-tag v-if="item.isTop === 1" size="small" type="warning">置顶</el-tag>
                  <el-tag size="small" :type="item.tagType">
                    {{ item.categoryName }}
                  </el-tag>
                  <el-tag v-if="item.activitySubCategoryName" size="small" type="primary" effect="plain">
                    {{ item.activitySubCategoryName }}
                  </el-tag>
                </div>
              </div>

              <h3>{{ item.title }}</h3>
              <p>{{ item.summary }}</p>

              <div class="meta-row">
                <span>
                  <el-icon><View /></el-icon>
                  {{ item.viewCount }}
                </span>
                <span>
                  <el-icon><Pointer /></el-icon>
                  {{ item.likeCount }}
                </span>
                <span>
                  <el-icon><Star /></el-icon>
                  {{ item.favoriteCount }}
                </span>
                <span>
                  <el-icon><ChatDotRound /></el-icon>
                  {{ item.commentCount }}
                </span>
              </div>
            </div>

            <div class="cover" v-if="item.coverImg">
              <img :src="item.coverImg" :alt="item.title" />
            </div>
          </article>

          <el-empty
            v-if="!loading && contentList.length === 0"
            description="暂无内容"
          />

          <el-pagination
            class="home-pagination"
            background
            layout="total, prev, pager, next, jumper"
            :total="contentPage.total"
            v-model:current-page="contentPage.page"
            :page-size="contentPage.pageSize"
            @current-change="handlePageChange"
          />
        </section>

        <aside class="side">
          <section class="side-panel">
            <div class="side-head">
              <h3>校园公告</h3>
              <el-button link type="primary" @click="goAuthRequired('/notice')">更多</el-button>
            </div>

            <div class="notice-list">
              <button
                v-for="notice in notices"
                :key="notice.id"
                class="notice-item"
                @click="goNoticeDetail(notice.id)"
              >
                <span class="notice-dot"></span>
                <div>
                  <strong>{{ notice.title }}</strong>
                  <p>{{ notice.content || '暂无公告内容' }}</p>
                  <small>{{ notice.createTimeText }}</small>
                </div>
              </button>
            </div>
          </section>

        </aside>
      </div>
    </main>

    <el-dialog v-model="authDialogVisible" title="登录后继续使用" width="360px" class="auth-dialog">
      <p>游客只能浏览第一页内容。登录或注册后可以查看更多内容、发布帖子、收藏和参与互动。</p>
      <template #footer>
        <el-button @click="router.push('/register')">注册账号</el-button>
        <el-button type="primary" @click="router.push('/login')">去登录</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="feedbackDialogVisible" title="平台反馈" width="520px">
      <el-form ref="feedbackFormRef" :model="feedbackForm" :rules="feedbackRules" label-position="top">
        <el-form-item label="反馈内容" prop="content">
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请输入使用问题、功能建议或其他反馈"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="feedbackSubmitting" @click="handleSubmitFeedback">提交反馈</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowDown,
  Bell,
  ChatDotRound,
  Cloudy,
  Collection,
  EditPen,
  HelpFilled,
  Lightning,
  MostlyCloudy,
  PartlyCloudy,
  Pointer,
  Pouring,
  Search,
  Star,
  Sunny,
  View
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getContentPageApi } from '../api/content'
import { getCategoryListApi } from '../api/category'
import { getActivitySubCategoryListApi } from '../api/activitySubCategory'
import { getPublishedNoticeListApi } from '../api/notice'
import { getPublicHomeHeroConfigApi } from '../api/homeHero'
import { getUnreadMessageCountApi, submitFeedbackApi } from '../api/message'
import UserAvatar from '../components/UserAvatar.vue'
import { ElMessage } from 'element-plus'
import { formatBeijingClock, formatBeijingDateTime, formatBeijingWeekdayDate, parseBeijingTime } from '../utils/time'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const unreadCount = ref(0)
const authDialogVisible = ref(false)
const feedbackDialogVisible = ref(false)
const feedbackSubmitting = ref(false)
const feedbackFormRef = ref()
const contentList = ref([])
const contentPage = reactive({ page: 1, pageSize: 20, total: 0 })
const allCategories = ref([])
const categories = ref([])
const activitySubCategories = ref([])
const notices = ref([])
const homeHeroConfig = reactive({
  title: '今天的校园动态，都从这里开始',
  content: '发布校园信息、查看活动通知、寻找失物线索，让校园服务更集中也更高效。',
  background: ''
})
const nowTime = ref(new Date())
const weather = reactive({
  loading: false,
  ready: false,
  temperature: null,
  windSpeed: null,
  weatherCode: null,
  locationText: '当前位置'
})
const DEFAULT_WEATHER_LOCATION = {
  latitude: 26.5667,
  longitude: 107.9833,
  text: '默认城市 贵州凯里'
}
let clockTimer = null

const displayName = computed(() => {
  if (!userStore.isLogin) return '游客'
  return userStore.userInfo?.nickname || userStore.userInfo?.username || '同学'
})

const avatarText = computed(() => {
  return displayName.value.slice(0, 1)
})

const query = reactive({
  keyword: '',
  categoryId: null,
  activitySubCategoryId: null,
  sort: 'latest'
})
const feedbackForm = reactive({
  content: ''
})
const feedbackRules = {
  content: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
    { max: 500, message: '反馈内容不能超过500个字符', trigger: 'blur' }
  ]
}

const HOME_FIXED_CATEGORY_ORDER = ['校园论坛', '活动通知', '失物招领']
const HOME_EXCLUDED_CATEGORY_NAMES = ['二手交易']
const categoryDescriptionMap = {
  校园论坛: '讨论学习生活，分享校园见闻',
  活动通知: '讲座、社团、比赛和校园安排',
  失物招领: '丢失、捡到、认领线索集中查看'
}
const categoryIconMap = {
  失物招领: Search,
  活动通知: Bell,
  校园论坛: HelpFilled
}

const categoryNameMap = computed(() => {
  const map = {}
  allCategories.value.forEach(item => {
    map[item.id] = item.name
  })
  return map
})

const contentCount = computed(() => contentPage.total)
const noticeCount = computed(() => notices.value.length)
const activeCategory = computed(() => categories.value.find(item => item.id === query.categoryId))
const activeCategoryName = computed(() => activeCategory.value?.name || '')
const showActivitySubCategories = computed(() => activeCategoryName.value === '活动通知' && activitySubCategories.value.length > 0)
const activeCategoryDescription = computed(() => {
  return activeCategory.value?.description || '按发布时间浏览首页开放的校园内容。'
})
const heroNoticeTitle = computed(() => homeHeroConfig.title || '今天的校园动态，都从这里开始')
const heroNoticeContent = computed(() => homeHeroConfig.content || '发布校园信息、查看活动通知、寻找失物线索，让校园服务更集中也更高效。')
const heroNoticeBackground = computed(() => homeHeroConfig.background || '')
const heroBackgroundStyle = computed(() => (
  heroNoticeBackground.value ? { '--home-hero-bg': `url("${heroNoticeBackground.value}")` } : {}
))
const todayDateText = computed(() => {
  return formatBeijingWeekdayDate(nowTime.value)
})
const currentTimeText = computed(() => {
  return formatBeijingClock(nowTime.value)
})
const weatherMeta = computed(() => getWeatherMeta(weather.weatherCode))
const weatherIcon = computed(() => weatherMeta.value.icon)
const weatherSummary = computed(() => {
  if (weather.loading) return '天气获取中'
  if (!weather.ready) return '天气待更新'
  return weatherMeta.value.label
})
const weatherLocationText = computed(() => weather.ready ? weather.locationText : '允许定位后显示实时天气')
const weatherTemperatureText = computed(() => weather.ready && weather.temperature !== null ? `${Math.round(weather.temperature)}°C` : '--')
const weatherWindText = computed(() => weather.ready && weather.windSpeed !== null ? `${Math.round(weather.windSpeed)}km/h` : '--')

const getCategoryIcon = name => {
  return categoryIconMap[name] || Collection
}

const getWeatherMeta = code => {
  if (code === null || code === undefined) {
    return { label: '天气待更新', icon: PartlyCloudy }
  }

  if ([0, 1].includes(code)) return { label: '晴朗', icon: Sunny }
  if ([2].includes(code)) return { label: '少云', icon: PartlyCloudy }
  if ([3, 45, 48].includes(code)) return { label: '多云', icon: Cloudy }
  if ([51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82].includes(code)) return { label: '有雨', icon: Pouring }
  if ([71, 73, 75, 77, 85, 86].includes(code)) return { label: '降雪', icon: MostlyCloudy }
  if ([95, 96, 99].includes(code)) return { label: '雷雨', icon: Lightning }
  return { label: '天气正常', icon: PartlyCloudy }
}

const isSecureWeatherContext = () => {
  return window.isSecureContext || ['localhost', '127.0.0.1'].includes(window.location.hostname)
}

const loadWeatherByLocation = async (latitude, longitude, locationText = '当前位置') => {
  weather.loading = true
  try {
    const url = new URL('https://api.open-meteo.com/v1/forecast')
    url.searchParams.set('latitude', latitude)
    url.searchParams.set('longitude', longitude)
    url.searchParams.set('current', 'temperature_2m,weather_code,wind_speed_10m')
    url.searchParams.set('timezone', 'auto')

    const response = await fetch(url.toString())
    if (!response.ok) throw new Error('weather request failed')
    const data = await response.json()
    weather.temperature = data.current?.temperature_2m ?? null
    weather.windSpeed = data.current?.wind_speed_10m ?? null
    weather.weatherCode = data.current?.weather_code ?? null
    weather.ready = weather.temperature !== null || weather.weatherCode !== null
    weather.locationText = weather.ready ? locationText : weather.locationText
  } catch {
    weather.ready = false
  } finally {
    weather.loading = false
  }
}

const loadDefaultWeather = () => {
  weather.locationText = DEFAULT_WEATHER_LOCATION.text
  return loadWeatherByLocation(
    DEFAULT_WEATHER_LOCATION.latitude,
    DEFAULT_WEATHER_LOCATION.longitude,
    DEFAULT_WEATHER_LOCATION.text
  )
}

const loadWeather = () => {
  if (!isSecureWeatherContext()) {
    loadDefaultWeather()
    return
  }

  if (!navigator.geolocation) {
    loadDefaultWeather()
    return
  }

  weather.loading = true
  navigator.geolocation.getCurrentPosition(
    position => {
      loadWeatherByLocation(position.coords.latitude, position.coords.longitude, '当前位置')
    },
    () => {
      loadDefaultWeather()
    },
    {
      enableHighAccuracy: false,
      timeout: 8000,
      maximumAge: 30 * 60 * 1000
    }
  )
}

const isCategoryEnabled = item => {
  return item?.status === undefined || item?.status === null || Number(item.status) === 1
}

const isHomeCategory = item => {
  return isCategoryEnabled(item) && !HOME_EXCLUDED_CATEGORY_NAMES.includes(item.name)
}

const getCategorySortWeight = name => {
  const fixedIndex = HOME_FIXED_CATEGORY_ORDER.indexOf(name)
  return fixedIndex === -1 ? HOME_FIXED_CATEGORY_ORDER.length : fixedIndex
}

const formatTime = value => {
  if (!value) return ''

  const date = parseBeijingTime(value)
  if (!date) return value

  const now = Date.now()
  const diff = now - date.getTime()
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`

  return formatBeijingDateTime(value)
}

const getTagType = categoryName => {
  const map = {
    失物招领: 'success',
    二手交易: 'warning',
    活动通知: 'primary',
    校园论坛: 'info'
  }

  return map[categoryName] || ''
}

const limitPreview = value => {
  const text = String(value || '').replace(/\s+/g, ' ').trim()
  return text.length > 80 ? `${text.slice(0, 80)}...` : text
}

const normalizeContent = item => {
  const categoryName = item.categoryName || categoryNameMap.value[item.categoryId] || '未分类'
  const authorName = item.authorNickname || item.authorUsername || `用户${item.userId || ''}`

  return {
    ...item,
    categoryName,
    authorName,
    tagType: getTagType(categoryName),
    summary: limitPreview(item.summary || item.content),
    createTimeText: formatTime(item.createTime),
    viewCount: item.viewCount || 0,
    likeCount: item.likeCount || 0,
    favoriteCount: item.favoriteCount || 0,
    commentCount: item.commentCount || 0
  }
}

const loadUnreadCount = async () => {
  if (!userStore.isLogin) {
    unreadCount.value = 0
    return
  }
  const res = await getUnreadMessageCountApi()
  unreadCount.value = res.data || 0
}

const loadCategories = async () => {
  const res = await getCategoryListApi()
  const list = res.data || []
  allCategories.value = list

  categories.value = list
    .filter(isHomeCategory)
    .sort((a, b) => {
      const sortA = Number(a.sort ?? 0)
      const sortB = Number(b.sort ?? 0)
      const weightA = getCategorySortWeight(a.name)
      const weightB = getCategorySortWeight(b.name)

      if (weightA !== weightB) return weightA - weightB
      if (sortA !== sortB) return sortA - sortB
      return Number(a.id || 0) - Number(b.id || 0)
    })
    .map(item => ({
      ...item,
      icon: getCategoryIcon(item.name),
      description: categoryDescriptionMap[item.name] || '查看相关校园内容'
    }))

  const selectedExists = categories.value.some(item => item.id === query.categoryId)
  if ((!query.categoryId || !selectedExists) && categories.value.length) {
    const defaultCategory = categories.value.find(item => item.name === '校园论坛') || categories.value[0]
    query.categoryId = defaultCategory.id
  }
}

const loadActivitySubCategories = async () => {
  const res = await getActivitySubCategoryListApi()
  activitySubCategories.value = res.data || []
}

const loadNotices = async () => {
  const res = await getPublishedNoticeListApi()
  notices.value = (res.data || []).slice(0, 5).map(item => ({
    ...item,
    createTimeText: formatTime(item.createTime)
  }))
}

const loadHomeHeroConfig = async () => {
  const res = await getPublicHomeHeroConfigApi()
  const config = res.data || {}
  homeHeroConfig.title = config.title || homeHeroConfig.title
  homeHeroConfig.content = config.content || homeHeroConfig.content
  homeHeroConfig.background = config.background || ''
}

const loadContentList = async () => {
  if (!userStore.isLogin && contentPage.page > 1) {
    contentPage.page = 1
    showAuthDialog()
    return
  }

  loading.value = true

  try {
    const params = {
      title: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      activitySubCategoryId: activeCategoryName.value === '活动通知' ? query.activitySubCategoryId || undefined : undefined,
      status: 1,
      sort: query.sort,
      page: contentPage.page,
      pageSize: contentPage.pageSize
    }

    const res = await getContentPageApi(params)
    const data = res.data || {}
    contentPage.total = Number(data.total || 0)
    contentList.value = (data.records || []).map(normalizeContent)
  } finally {
    loading.value = false
  }
}

const reloadFirstPage = () => {
  contentPage.page = 1
  loadContentList()
}

const selectActivitySubCategory = id => {
  query.activitySubCategoryId = id
  reloadFirstPage()
}

const showAuthDialog = () => {
  authDialogVisible.value = true
}

const goAuthRequired = path => {
  if (!userStore.isLogin) {
    showAuthDialog()
    return
  }
  router.push(path)
}

const openFeedbackDialog = () => {
  if (!userStore.isLogin) {
    showAuthDialog()
    return
  }
  feedbackForm.content = ''
  feedbackDialogVisible.value = true
}

const handleSubmitFeedback = async () => {
  await feedbackFormRef.value.validate()
  feedbackSubmitting.value = true
  try {
    await submitFeedbackApi({ content: feedbackForm.content.trim() })
    ElMessage.success('反馈已提交')
    feedbackDialogVisible.value = false
  } finally {
    feedbackSubmitting.value = false
  }
}

const handlePageChange = page => {
  if (!userStore.isLogin && page > 1) {
    contentPage.page = 1
    showAuthDialog()
    return
  }
  loadContentList()
}

const goDetail = id => {
  router.push(`/content/${id}`)
}

const goUserProfile = id => {
  if (!id) return
  router.push(`/profile/${id}`)
}

const goNoticeDetail = id => {
  if (!id) return
  if (!userStore.isLogin) {
    showAuthDialog()
    return
  }
  router.push(`/notice/${id}`)
}

const goHomeCategories = () => {
  if (categories.value.length) {
    query.categoryId = categories.value[0].id
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

watch(
  () => query.categoryId,
  () => {
    query.activitySubCategoryId = null
    reloadFirstPage()
  }
)

onMounted(async () => {
  clockTimer = window.setInterval(() => {
    nowTime.value = new Date()
  }, 1000)
  loadWeather()

  await loadCategories()
  await loadActivitySubCategories()
  await Promise.all([
    loadContentList(),
    loadHomeHeroConfig(),
    loadNotices(),
    loadUnreadCount()
  ])
})

onBeforeUnmount(() => {
  if (clockTimer) {
    window.clearInterval(clockTimer)
    clockTimer = null
  }
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  height: 72px;
  display: grid;
  grid-template-columns: 280px 1fr auto;
  align-items: center;
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

.nav {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.nav-badge {
  display: inline-flex;
}

.nav-item {
  height: 38px;
  padding: 0 18px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  font-size: 14px;
}

.nav-item.active,
.nav-item:hover {
  background: var(--theme-tint-soft);
  color: var(--primary-deep);
}

.user-area {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-area > button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 14px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--surface-soft);
  color: var(--text);
  cursor: pointer;
  transition: border-color 0.18s ease, background 0.18s ease, color 0.18s ease;
}

.user-area > button:hover {
  border-color: var(--primary);
  background: var(--surface);
  color: var(--primary);
}

.user-chip {
  position: relative;
  height: 52px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px 5px 5px;
  border-radius: 999px;
  background: var(--surface);
  border: 1px solid var(--border);
  cursor: pointer;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main {
  width: min(1180px, calc(100% - 44px));
  margin: 0 auto;
  padding: 28px 0 40px;
}

.welcome-band {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 24px;
  align-items: center;
  padding: 34px;
  border-radius: var(--radius);
  background:
    var(--hero-gradient),
    var(--primary);
  color: #fff;
  box-shadow: var(--shadow);
}

.welcome-band.custom-hero-bg {
  overflow: hidden;
  background-image: var(--home-hero-bg);
  background-size: cover;
  background-position: center;
}

.eyebrow {
  margin: 0 0 10px;
  font-size: 13px;
  opacity: 0.82;
  font-weight: 700;
}

.welcome-band h2 {
  margin: 0;
  font-size: 32px;
  line-height: 1.25;
}

.welcome-band p {
  margin: 12px 0 0;
  max-width: 640px;
  line-height: 1.8;
  opacity: 0.86;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 22px;
}

.hero-actions .el-button {
  margin-left: 0;
}

.campus-note {
  padding: 20px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(12px);
  transition: transform 0.18s ease, background 0.18s ease;
}

.campus-note:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.2);
}

.campus-note > span {
  color: rgba(255, 255, 255, 0.78);
  font-size: 12px;
}

.campus-note > strong {
  display: block;
  margin-top: 8px;
  font-size: 18px;
}

.campus-note > p {
  display: -webkit-box;
  max-width: none;
  min-height: 50px;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.today-note {
  cursor: default;
}

.today-note > strong {
  font-size: 22px;
}

.today-note > p {
  display: block;
  min-height: 0;
  margin-top: 6px;
  font-size: 34px;
  line-height: 1.18;
  font-weight: 800;
  letter-spacing: 0;
  opacity: 0.98;
}

.weather-main {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 18px;
  padding: 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
}

.weather-main .el-icon {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.2);
  font-size: 28px;
}

.weather-main strong,
.weather-main span {
  display: block;
}

.weather-main strong {
  font-size: 18px;
}

.weather-main span {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 12px;
}

.weather-stats {
  margin-top: 12px;
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.quick-stats div {
  padding: 14px 10px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
  text-align: center;
}

.quick-stats strong {
  display: block;
  font-size: 24px;
}

.quick-stats span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.82;
}

.toolbar {
  display: grid;
  grid-template-columns: 170px minmax(0, 1fr) 110px 150px;
  gap: 14px;
  margin-top: 22px;
}

.category-showcase {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.category-card {
  min-height: 118px;
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  grid-template-rows: auto auto;
  gap: 8px 12px;
  align-items: center;
  padding: 18px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--text);
  cursor: pointer;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.05);
  text-align: left;
  transition: transform 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.category-icon {
  grid-row: 1 / 3;
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  background: var(--theme-tint-soft);
  color: var(--primary);
  font-size: 22px;
}

.category-card strong {
  color: var(--primary-deep);
  font-size: 17px;
}

.category-card small {
  color: var(--muted);
  line-height: 1.5;
}

.category-card.active,
.category-card:hover {
  transform: translateY(-2px);
  background: var(--theme-tint);
  border-color: var(--primary);
}

.content-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 310px;
  gap: 22px;
  margin-top: 20px;
}

.feed {
  display: grid;
  gap: 16px;
}

.activity-subcategory-strip {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(132px, 1fr));
  gap: 10px;
}

.activity-subcategory-card {
  min-height: 76px;
  padding: 13px 14px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--surface);
  color: var(--text);
  text-align: left;
  cursor: pointer;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.05);
  transition: transform 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.activity-subcategory-card strong,
.activity-subcategory-card span {
  display: block;
}

.activity-subcategory-card strong {
  color: var(--primary-deep);
  font-size: 15px;
}

.activity-subcategory-card span {
  margin-top: 6px;
  color: var(--muted);
  font-size: 12px;
}

.activity-subcategory-card.active,
.activity-subcategory-card:hover {
  transform: translateY(-2px);
  border-color: var(--primary);
  background: var(--theme-tint-soft);
}

.feed-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 2px 2px 0;
}

.feed-head h3 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 22px;
}

.feed-head p {
  margin: 6px 0 0;
  color: var(--muted);
}

.feed-loading-text {
  flex: 0 0 auto;
  padding: 7px 12px;
  border-radius: 999px;
  background: var(--theme-tint-soft);
  color: var(--primary-deep);
  font-size: 13px;
  font-weight: 700;
}

.content-card {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 18px;
  padding: 22px;
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.content-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.11);
}

.card-head,
.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-head {
  justify-content: space-between;
  align-items: flex-start;
}

.tag-stack {
  display: inline-flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 6px;
}

.author-line {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-line > div {
  min-width: 0;
}

.author-line strong {
  display: block;
  color: var(--primary-deep);
  font-size: 14px;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.author-line span {
  display: block;
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
}

.time {
  color: var(--muted);
  font-size: 13px;
}

.content-card h3 {
  margin: 14px 0 8px;
  color: var(--primary-deep);
  font-size: 20px;
}

.content-card p {
  margin: 0;
  color: var(--muted);
  line-height: 1.8;
}

.meta-row {
  margin-top: 18px;
  flex-wrap: wrap;
  color: var(--muted);
  font-size: 13px;
}

.meta-row span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.cover {
  width: 150px;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  border-radius: 16px;
  background: var(--surface-soft);
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.home-pagination {
  width: 100%;
  justify-content: center;
  margin-top: 8px;
}

.home-pagination :deep(.el-pagination) {
  justify-content: center;
}

.side {
  display: grid;
  gap: 16px;
  align-content: start;
}

.side-panel {
  padding: 20px;
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.side-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.side-head h3 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 18px;
}

.notice-list {
  display: grid;
  gap: 10px;
}

.notice-item {
  display: grid;
  grid-template-columns: 8px minmax(0, 1fr);
  gap: 10px;
  width: 100%;
  padding: 12px;
  border: 1px solid transparent;
  border-radius: 14px;
  background: var(--surface-soft);
  cursor: pointer;
  text-align: left;
  transition: background 0.18s ease, border-color 0.18s ease;
}

.notice-item:hover {
  background: var(--theme-tint-soft);
  border-color: var(--primary);
}

.notice-dot {
  width: 8px;
  height: 8px;
  margin-top: 7px;
  border-radius: 999px;
  background: var(--primary);
}

.notice-item strong {
  display: block;
  color: var(--text);
  font-size: 14px;
  line-height: 1.5;
}

.notice-item p {
  display: -webkit-box;
  margin: 5px 0 0;
  overflow: hidden;
  color: var(--muted);
  line-height: 1.5;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notice-item small {
  display: block;
  margin-top: 6px;
  color: var(--muted);
  font-size: 12px;
}

@media (max-width: 980px) {
  .topbar {
    height: auto;
    grid-template-columns: 1fr;
    padding: 16px 22px;
  }

  .nav {
    justify-content: flex-start;
  }

  .user-area {
    justify-content: space-between;
  }

  .welcome-band {
    grid-template-columns: 1fr;
  }

  .quick-stats {
    grid-template-columns: repeat(3, 1fr);
  }

  .toolbar {
    grid-template-columns: 1fr;
  }

  .content-layout {
    grid-template-columns: 1fr;
  }

  .category-showcase {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .main {
    width: min(100% - 28px, 1180px);
    padding-top: 18px;
  }

  .welcome-band {
    padding: 22px;
  }

  .welcome-band h2 {
    font-size: 25px;
  }

  .quick-stats {
    grid-template-columns: 1fr;
  }

  .content-card {
    grid-template-columns: 1fr;
  }

  .cover {
    width: 100%;
  }

  .user-area {
    align-items: stretch;
    flex-direction: column;
  }

  .user-chip {
    width: 100%;
    justify-content: center;
  }
}
</style>

