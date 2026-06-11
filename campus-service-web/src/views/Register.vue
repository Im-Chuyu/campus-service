<template>
  <div class="register-page">
    <div class="register-shell">
      <div class="brand-panel">
        <p class="eyebrow">Account Registration</p>
        <h1>加入校园服务平台</h1>
        <p class="desc">
          完成账号注册后，你可以参与校园信息发布、物品流转、活动互动和站内沟通，并通过统一消息中心接收重要提醒。
        </p>

        <div class="feature-list">
          <div>
            <el-icon><EditPen /></el-icon>
            <span>发布和管理个人内容</span>
          </div>
          <div>
            <el-icon><Star /></el-icon>
            <span>收藏关注的重要信息</span>
          </div>
          <div>
            <el-icon><ChatDotRound /></el-icon>
            <span>通过私聊连接相关同学</span>
          </div>
        </div>
      </div>

      <div class="register-card">
        <div class="card-head">
          <h2>账号注册</h2>
          <p>请填写真实可用的信息，方便后续找回和消息通知。</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleRegister"
        >
          <div class="form-grid">
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="form.username"
                size="large"
                placeholder="字母开头，4-20位"
                clearable
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input
                v-model="form.nickname"
                size="large"
                placeholder="请输入昵称"
                maxlength="16"
                show-word-limit
                clearable
              >
                <template #prefix>
                  <el-icon><Postcard /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="form.phone"
                size="large"
                placeholder="请输入手机号"
                clearable
              >
                <template #prefix>
                  <el-icon><Iphone /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="form.email"
                size="large"
                placeholder="可选"
                clearable
              >
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                size="large"
                type="password"
                show-password
                placeholder="8-20位，包含字母和数字"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                size="large"
                type="password"
                show-password
                placeholder="请再次输入密码"
              >
                <template #prefix>
                  <el-icon><Key /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="验证码" prop="code" class="span-2">
              <el-input
                v-model="form.code"
                size="large"
                maxlength="6"
                placeholder="请输入手机号收到的验证码"
                clearable
              >
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
                <template #append>
                  <el-button :loading="codeLoading" :disabled="codeCountdown > 0" @click="sendRegisterCode">
                    {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>

          </div>

          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注册账号
          </el-button>
        </el-form>

        <div class="extra">
          <span>已有账号？</span>
          <el-button link type="primary" @click="$router.push('/login')">
            返回登录
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Lock,
  Key,
  Iphone,
  Message,
  Postcard,
  EditPen,
  Star,
  ChatDotRound
} from '@element-plus/icons-vue'
import { registerApi, sendCodeApi } from '../api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const codeLoading = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  email: '',
  code: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }

  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }

  callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_.&-]{3,19}$/,
      message: '用户名必须以字母开头，可包含字母、数字、下划线、短横线、点号和&，长度4-20位',
      trigger: 'blur'
    }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 16, message: '昵称长度不能超过16个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d_!@#$%^&*()\-+=]{8,20}$/,
      message: '密码必须包含字母和数字，长度8-20位',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '手机号格式不正确',
      trigger: 'blur'
    }
  ],
  email: [
    {
      pattern: /^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$/,
      message: '邮箱格式不正确',
      trigger: 'blur'
    }
  ]
}

const startCountdown = () => {
  codeCountdown.value = 60
  clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    codeCountdown.value -= 1
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const sendRegisterCode = async () => {
  if (codeCountdown.value > 0 || codeLoading.value) return
  await formRef.value.validateField('phone')
  codeLoading.value = true
  try {
    const res = await sendCodeApi({
      scene: 'register',
      channel: 'phone',
      target: form.phone
    })
    await ElMessageBox.alert(res.msg || `验证码：${res.data}`, '验证码', {
      confirmButtonText: '知道了'
    })
    startCountdown()
  } finally {
    codeLoading.value = false
  }
}

const handleRegister = async () => {
  form.nickname = form.nickname.trim()
  await formRef.value.validate()

  loading.value = true
  try {
    await registerApi({
      ...form,
      email: form.email.trim() || null
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => {
  clearInterval(countdownTimer)
})
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.register-shell {
  width: min(1180px, 100%);
  display: grid;
  grid-template-columns: 0.86fr 1.14fr;
  gap: 32px;
  align-items: stretch;
}

.brand-panel,
.register-card {
  border-radius: var(--radius);
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  backdrop-filter: blur(16px);
}

.brand-panel {
  padding: 38px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.eyebrow {
  margin: 0 0 14px;
  color: var(--primary);
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
}

.brand-panel h1 {
  margin: 0;
  color: var(--primary-deep);
  font-size: 46px;
  line-height: 1.12;
}

.desc {
  margin: 20px 0 0;
  color: var(--muted);
  line-height: 1.8;
  font-size: 16px;
}

.feature-list {
  display: grid;
  gap: 12px;
  margin-top: 34px;
}

.feature-list div {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 14px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
  color: var(--primary-deep);
}

.feature-list .el-icon {
  color: var(--primary);
  font-size: 18px;
}

.register-card {
  padding: 34px;
}

.card-head {
  margin-bottom: 24px;
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

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 18px;
}

.span-2 {
  grid-column: 1 / -1;
}

.submit-btn {
  width: 100%;
  height: 46px;
  margin-top: 4px;
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

@media (max-width: 980px) {
  .register-page {
    padding: 22px;
  }

  .register-shell {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    padding: 28px;
  }

  .brand-panel h1 {
    font-size: 34px;
  }

  .register-card {
    padding: 24px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>

