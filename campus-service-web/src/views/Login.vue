<template>
  <div class="login-page">
    <div class="hero">
      <div class="hero-copy">
        <p class="eyebrow">Campus Service Platform</p>
        <h1>校园信息服务平台</h1>
        <p class="desc">
          面向校园场景的信息发布、二手流转、活动公告、站内沟通与内容治理系统，帮助师生更高效地连接需求和资源。
        </p>

        <div class="stats">
          <div>
            <strong>统一</strong>
            <span>信息入口</span>
          </div>
          <div>
            <strong>实时</strong>
            <span>消息通知</span>
          </div>
          <div>
            <strong>可控</strong>
            <span>内容治理</span>
          </div>
        </div>
      </div>

      <div class="login-card">
        <div class="card-head">
          <h2>账号登录</h2>
          <p>支持用户名、手机号和邮箱登录，登录后可发布、收藏、评论和处理个人消息。</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="账号" prop="username">
            <el-input
              v-model="form.username"
              size="large"
              placeholder="请输入用户名、手机号或邮箱"
              clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              size="large"
              type="password"
              show-password
              placeholder="请输入密码"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item v-if="needCaptcha" label="安全验证" prop="captchaAnswer">
            <el-input
              v-model="form.captchaAnswer"
              size="large"
              placeholder="请输入计算结果"
              clearable
            >
              <template #prepend>{{ captchaQuestion }}</template>
            </el-input>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>

          <el-button
            size="large"
            class="guest-btn"
            @click="enterAsGuest"
          >
            游客进入
          </el-button>
        </el-form>

        <div class="extra">
          <span>还没有账号？</span>
          <el-button link type="primary" @click="$router.push('/register')">
            去注册
          </el-button>
          <el-divider direction="vertical" />
          <el-button link type="primary" @click="$router.push('/forgot-password')">
            忘记密码
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  captchaAnswer: ''
})

const loginFailCount = ref(Number(sessionStorage.getItem('login_fail_count') || 0))
const captcha = reactive({
  left: 0,
  right: 0,
  operator: '+',
  answer: 0
})

const needCaptcha = computed(() => loginFailCount.value >= 6)
const captchaQuestion = computed(() => `${captcha.left} ${captcha.operator} ${captcha.right} = ?`)

const generateCaptcha = () => {
  const operators = ['+', '-', '*', '/']
  const operator = operators[Math.floor(Math.random() * operators.length)]
  let left = Math.floor(Math.random() * 20) + 1
  let right = Math.floor(Math.random() * 9) + 1
  let answer = 0

  if (operator === '+') answer = left + right
  if (operator === '-') {
    if (left < right) [left, right] = [right, left]
    answer = left - right
  }
  if (operator === '*') {
    left = Math.floor(Math.random() * 9) + 2
    right = Math.floor(Math.random() * 9) + 2
    answer = left * right
  }
  if (operator === '/') {
    answer = Math.floor(Math.random() * 9) + 2
    right = Math.floor(Math.random() * 9) + 2
    left = answer * right
  }

  captcha.left = left
  captcha.right = right
  captcha.operator = operator
  captcha.answer = answer
  form.captchaAnswer = ''
}

if (needCaptcha.value) {
  generateCaptcha()
}

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 50, message: '账号长度应为 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度应为 6 到 100 个字符', trigger: 'blur' }
  ],
  captchaAnswer: [
    {
      validator: (rule, value, callback) => {
        if (!needCaptcha.value) {
          callback()
          return
        }
        if (!value) {
          callback(new Error('请输入计算结果'))
          return
        }
        if (Number(value) !== captcha.answer) {
          callback(new Error('计算结果不正确'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const recordLoginFailure = () => {
  loginFailCount.value += 1
  sessionStorage.setItem('login_fail_count', String(loginFailCount.value))
  if (needCaptcha.value) {
    generateCaptcha()
  }
}

const clearLoginFailure = () => {
  loginFailCount.value = 0
  sessionStorage.removeItem('login_fail_count')
}

const handleLogin = async () => {
  if (needCaptcha.value && Number(form.captchaAnswer) !== captcha.answer) {
    ElMessage.warning('请先完成安全验证')
    generateCaptcha()
    return
  }
  await formRef.value.validate()

  loading.value = true
  try {
    await userStore.login(form)
    clearLoginFailure()
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (error) {
    recordLoginFailure()
  } finally {
    loading.value = false
  }
}

const enterAsGuest = () => {
  userStore.logout()
  router.push('/home')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.hero {
  width: min(1160px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(360px, 0.92fr);
  gap: 40px;
  align-items: center;
}

.hero-copy {
  padding: 24px 0;
}

.eyebrow {
  margin: 0 0 14px;
  color: var(--primary);
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin: 0;
  font-size: 64px;
  line-height: 1.06;
  color: var(--primary-deep);
}

.desc {
  width: min(560px, 100%);
  margin: 22px 0 0;
  color: var(--muted);
  font-size: 17px;
  line-height: 1.8;
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 132px));
  gap: 14px;
  margin-top: 34px;
}

.stats div {
  padding: 16px;
  border-radius: 16px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.stats strong {
  display: block;
  color: var(--primary-deep);
  font-size: 22px;
  line-height: 1.2;
}

.stats span {
  display: block;
  margin-top: 6px;
  color: var(--muted);
  font-size: 13px;
}

.login-card {
  padding: 34px;
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  backdrop-filter: blur(16px);
}

.card-head {
  margin-bottom: 26px;
}

.card-head h2 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 30px;
}

.card-head p {
  margin: 10px 0 0;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.7;
}

.submit-btn {
  width: 100%;
  height: 46px;
  margin-top: 4px;
}

.guest-btn {
  width: 100%;
  height: 46px;
  margin-top: 12px;
  margin-left: 0;
}

.extra {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  margin-top: 18px;
  color: var(--muted);
  font-size: 14px;
}

@media (max-width: 900px) {
  .login-page {
    padding: 22px;
  }

  .hero {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .hero-copy h1 {
    font-size: 42px;
  }

  .stats {
    grid-template-columns: 1fr;
  }

  .login-card {
    padding: 24px;
  }
}
</style>

