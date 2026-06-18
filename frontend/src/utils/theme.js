const THEME_STORAGE_KEY = 'campus_theme'
const BACKGROUND_STORAGE_KEY = 'campus_background'
const CUSTOM_BACKGROUND_STORAGE_KEY = 'campus_custom_background'
const CUSTOM_BACKGROUND_DB = 'campus_appearance'
const CUSTOM_BACKGROUND_STORE = 'kv'
const CUSTOM_BACKGROUND_ID = 'globalBackground'
const DEFAULT_THEME_KEY = 'white'

const SYSTEM_BACKGROUND_SIZE = 'auto, auto, auto, auto, auto, 28px 28px, 28px 28px, auto, 18px 18px, auto'

export const themes = [
  {
    key: 'black',
    name: '曜石黑',
    description: '默认主题，深色背景和高对比界面。',
    colors: {
      bg: '#151922',
      bgStart: '#202838',
      bgEnd: '#151922',
      text: '#f4f7fb',
      muted: '#b8c2d2',
      primary: '#38bdf8',
      primaryDeep: '#0ea5e9',
      accent: '#f59e0b',
      border: 'rgba(226, 232, 240, 0.16)',
      shadow: '0 22px 64px rgba(2, 6, 23, 0.34)',
      glowPrimary: 'rgba(56, 189, 248, 0.17)',
      glowAccent: 'rgba(245, 158, 11, 0.13)',
      surface: 'rgba(30, 38, 51, 0.92)',
      surfaceSoft: 'rgba(43, 53, 69, 0.82)',
      tint: 'rgba(37, 55, 75, 0.92)',
      tintSoft: 'rgba(47, 61, 80, 0.84)',
      control: 'rgba(38, 49, 65, 0.9)',
      controlHover: 'rgba(47, 63, 84, 0.94)',
      topbar: 'rgba(24, 31, 42, 0.9)',
      hero: 'linear-gradient(135deg, rgba(30, 41, 59, 0.96), rgba(14, 165, 233, 0.76))',
      systemSheen: 'linear-gradient(120deg, rgba(255, 255, 255, 0.09), transparent 32%, rgba(56, 189, 248, 0.08) 62%, transparent 100%)',
      systemPattern: 'rgba(226, 232, 240, 0.05)',
      systemPatternSoft: 'rgba(226, 232, 240, 0.035)',
      systemPaper: 'rgba(255, 255, 255, 0.026)',
      systemFiber: 'rgba(255, 255, 255, 0.032)'
    }
  },
  {
    key: 'white',
    name: '瓷白',
    description: '干净明亮的白色主题，适合轻量阅读。',
    colors: {
      bg: '#f7f9fc',
      bgStart: '#ffffff',
      bgEnd: '#edf2f8',
      text: '#182235',
      muted: '#64748b',
      primary: '#64748b',
      primaryDeep: '#263244',
      accent: '#0ea5e9',
      border: 'rgba(15, 23, 42, 0.1)',
      shadow: '0 18px 50px rgba(15, 23, 42, 0.08)',
      glowPrimary: 'rgba(148, 163, 184, 0.16)',
      glowAccent: 'rgba(14, 165, 233, 0.09)',
      surface: 'rgba(255, 255, 255, 0.94)',
      surfaceSoft: 'rgba(244, 247, 251, 0.9)',
      tint: 'rgba(248, 250, 252, 0.94)',
      tintSoft: 'rgba(241, 245, 249, 0.9)',
      control: 'rgba(248, 250, 252, 0.94)',
      controlHover: 'rgba(236, 241, 247, 0.96)',
      topbar: 'rgba(255, 255, 255, 0.9)',
      hero: 'linear-gradient(135deg, rgba(100, 116, 139, 0.92), rgba(14, 165, 233, 0.58))',
      systemSheen: 'linear-gradient(120deg, rgba(255, 255, 255, 0.82), transparent 34%, rgba(203, 213, 225, 0.26) 64%, transparent 100%)',
      systemPattern: 'rgba(15, 23, 42, 0.028)',
      systemPatternSoft: 'rgba(15, 23, 42, 0.018)',
      systemPaper: 'rgba(15, 23, 42, 0.014)',
      systemFiber: 'rgba(15, 23, 42, 0.024)'
    }
  },
  {
    key: 'teal',
    name: '清新青绿',
    description: '适合校园服务和信息流。',
    colors: {
      bg: '#f1fbf8',
      bgStart: '#f8fffd',
      bgEnd: '#dff4ee',
      text: '#1f2937',
      muted: '#60746e',
      primary: '#0f766e',
      primaryDeep: '#134e4a',
      accent: '#f59e0b',
      border: 'rgba(15, 118, 110, 0.14)',
      shadow: '0 18px 52px rgba(15, 118, 110, 0.1)',
      glowPrimary: 'rgba(20, 184, 166, 0.18)',
      glowAccent: 'rgba(245, 158, 11, 0.11)',
      surface: 'rgba(240, 253, 250, 0.9)',
      surfaceSoft: 'rgba(213, 250, 241, 0.72)',
      tint: 'rgba(222, 252, 244, 0.88)',
      tintSoft: 'rgba(201, 246, 235, 0.68)',
      control: 'rgba(232, 252, 247, 0.92)',
      controlHover: 'rgba(204, 251, 241, 0.88)',
      topbar: 'rgba(240, 253, 250, 0.9)',
      hero: 'linear-gradient(135deg, rgba(13, 148, 136, 0.94), rgba(19, 78, 74, 0.84))',
      systemSheen: 'linear-gradient(120deg, rgba(255, 255, 255, 0.72), transparent 34%, rgba(153, 246, 228, 0.3) 62%, transparent 100%)',
      systemPattern: 'rgba(15, 118, 110, 0.05)',
      systemPatternSoft: 'rgba(15, 118, 110, 0.032)',
      systemPaper: 'rgba(15, 118, 110, 0.024)',
      systemFiber: 'rgba(20, 184, 166, 0.045)'
    }
  },
  {
    key: 'blue',
    name: '学院蓝',
    description: '更稳重的蓝色主题，适合公告和管理场景。',
    colors: {
      bg: '#f2f7ff',
      bgStart: '#f9fcff',
      bgEnd: '#dfebff',
      text: '#172033',
      muted: '#62708a',
      primary: '#2563eb',
      primaryDeep: '#1e3a8a',
      accent: '#0f766e',
      border: 'rgba(37, 99, 235, 0.15)',
      shadow: '0 18px 54px rgba(37, 99, 235, 0.12)',
      glowPrimary: 'rgba(37, 99, 235, 0.17)',
      glowAccent: 'rgba(15, 118, 110, 0.1)',
      surface: 'rgba(239, 246, 255, 0.9)',
      surfaceSoft: 'rgba(220, 234, 255, 0.72)',
      tint: 'rgba(225, 238, 255, 0.88)',
      tintSoft: 'rgba(207, 226, 255, 0.66)',
      control: 'rgba(232, 241, 255, 0.92)',
      controlHover: 'rgba(211, 228, 255, 0.9)',
      topbar: 'rgba(239, 246, 255, 0.9)',
      hero: 'linear-gradient(135deg, rgba(37, 99, 235, 0.94), rgba(30, 64, 175, 0.82))',
      systemSheen: 'linear-gradient(120deg, rgba(255, 255, 255, 0.72), transparent 34%, rgba(191, 219, 254, 0.32) 62%, transparent 100%)',
      systemPattern: 'rgba(37, 99, 235, 0.048)',
      systemPatternSoft: 'rgba(37, 99, 235, 0.03)',
      systemPaper: 'rgba(37, 99, 235, 0.022)',
      systemFiber: 'rgba(37, 99, 235, 0.04)'
    }
  },
  {
    key: 'green',
    name: '林荫绿',
    description: '更柔和的绿色主题，阅读感更轻。',
    colors: {
      bg: '#f2faf4',
      bgStart: '#fbfffc',
      bgEnd: '#e0f3e5',
      text: '#1f2a24',
      muted: '#607166',
      primary: '#15803d',
      primaryDeep: '#166534',
      accent: '#0ea5e9',
      border: 'rgba(21, 128, 61, 0.14)',
      shadow: '0 18px 52px rgba(21, 128, 61, 0.1)',
      glowPrimary: 'rgba(34, 197, 94, 0.16)',
      glowAccent: 'rgba(14, 165, 233, 0.09)',
      surface: 'rgba(240, 253, 244, 0.9)',
      surfaceSoft: 'rgba(221, 249, 228, 0.74)',
      tint: 'rgba(228, 250, 233, 0.88)',
      tintSoft: 'rgba(209, 244, 219, 0.68)',
      control: 'rgba(234, 251, 239, 0.92)',
      controlHover: 'rgba(213, 246, 223, 0.9)',
      topbar: 'rgba(240, 253, 244, 0.9)',
      hero: 'linear-gradient(135deg, rgba(21, 128, 61, 0.94), rgba(22, 101, 52, 0.82))',
      systemSheen: 'linear-gradient(120deg, rgba(255, 255, 255, 0.72), transparent 34%, rgba(187, 247, 208, 0.32) 62%, transparent 100%)',
      systemPattern: 'rgba(21, 128, 61, 0.045)',
      systemPatternSoft: 'rgba(21, 128, 61, 0.03)',
      systemPaper: 'rgba(21, 128, 61, 0.022)',
      systemFiber: 'rgba(34, 197, 94, 0.04)'
    }
  },
  {
    key: 'rose',
    name: '暖橙玫瑰',
    description: '更活泼的暖色主题，适合交易和活动氛围。',
    colors: {
      bg: '#fff7f4',
      bgStart: '#fffdfb',
      bgEnd: '#ffe3e7',
      text: '#2b2024',
      muted: '#7a6670',
      primary: '#e11d48',
      primaryDeep: '#9f1239',
      accent: '#f59e0b',
      border: 'rgba(225, 29, 72, 0.15)',
      shadow: '0 18px 52px rgba(225, 29, 72, 0.11)',
      glowPrimary: 'rgba(225, 29, 72, 0.14)',
      glowAccent: 'rgba(245, 158, 11, 0.15)',
      surface: 'rgba(255, 241, 242, 0.9)',
      surfaceSoft: 'rgba(255, 228, 230, 0.74)',
      tint: 'rgba(255, 233, 235, 0.88)',
      tintSoft: 'rgba(255, 220, 224, 0.68)',
      control: 'rgba(255, 238, 239, 0.92)',
      controlHover: 'rgba(255, 223, 226, 0.9)',
      topbar: 'rgba(255, 241, 242, 0.9)',
      hero: 'linear-gradient(135deg, rgba(225, 29, 72, 0.9), rgba(245, 158, 11, 0.68))',
      systemSheen: 'linear-gradient(120deg, rgba(255, 255, 255, 0.72), transparent 34%, rgba(253, 186, 116, 0.28) 62%, transparent 100%)',
      systemPattern: 'rgba(225, 29, 72, 0.045)',
      systemPatternSoft: 'rgba(245, 158, 11, 0.032)',
      systemPaper: 'rgba(225, 29, 72, 0.022)',
      systemFiber: 'rgba(245, 158, 11, 0.045)'
    }
  }
]

export const backgrounds = [
  {
    key: 'system',
    name: '系统默认',
    description: '跟随当前主题的轻纹理背景。',
    value: `
      var(--system-sheen),
      radial-gradient(circle at 12% 10%, var(--glow-primary), transparent 30%),
      radial-gradient(circle at 88% 14%, var(--glow-accent), transparent 28%),
      radial-gradient(circle at 36% 76%, var(--glow-primary), transparent 34%),
      radial-gradient(circle at 82% 82%, var(--glow-accent), transparent 30%),
      repeating-linear-gradient(0deg, var(--system-pattern) 0 1px, transparent 1px 28px),
      repeating-linear-gradient(90deg, var(--system-pattern-soft) 0 1px, transparent 1px 28px),
      repeating-linear-gradient(115deg, transparent 0 12px, var(--system-paper) 12px 13px, transparent 13px 30px),
      radial-gradient(circle at 1px 1px, var(--system-fiber) 1px, transparent 1.6px),
      linear-gradient(135deg, var(--bg-start), var(--bg-end))
    `
  }
]

export function getTheme(key) {
  return themes.find(item => item.key === key) || themes[0]
}

export function getSavedThemeKey() {
  return localStorage.getItem(THEME_STORAGE_KEY) || DEFAULT_THEME_KEY
}

export function getBackground(key) {
  return backgrounds.find(item => item.key === key) || backgrounds[0]
}

export function getSavedBackgroundKey() {
  return localStorage.getItem(BACKGROUND_STORAGE_KEY) || 'system'
}

export function getSavedBackgroundUrl() {
  return localStorage.getItem(CUSTOM_BACKGROUND_STORAGE_KEY) || ''
}

function openAppearanceDb() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(CUSTOM_BACKGROUND_DB, 1)
    request.onupgradeneeded = () => {
      request.result.createObjectStore(CUSTOM_BACKGROUND_STORE)
    }
    request.onsuccess = () => resolve(request.result)
    request.onerror = () => reject(request.error)
  })
}

export async function getSavedBackgroundBlobUrl() {
  try {
    const db = await openAppearanceDb()
    return await new Promise((resolve, reject) => {
      const request = db.transaction(CUSTOM_BACKGROUND_STORE, 'readonly')
        .objectStore(CUSTOM_BACKGROUND_STORE)
        .get(CUSTOM_BACKGROUND_ID)
      request.onsuccess = () => resolve(request.result ? URL.createObjectURL(request.result) : '')
      request.onerror = () => reject(request.error)
    })
  } catch {
    return ''
  }
}

export async function saveBackgroundBlob(blob) {
  const db = await openAppearanceDb()
  await new Promise((resolve, reject) => {
    const request = db.transaction(CUSTOM_BACKGROUND_STORE, 'readwrite')
      .objectStore(CUSTOM_BACKGROUND_STORE)
      .put(blob, CUSTOM_BACKGROUND_ID)
    request.onsuccess = () => resolve()
    request.onerror = () => reject(request.error)
  })
}

export async function removeBackgroundBlob() {
  const db = await openAppearanceDb()
  await new Promise((resolve, reject) => {
    const request = db.transaction(CUSTOM_BACKGROUND_STORE, 'readwrite')
      .objectStore(CUSTOM_BACKGROUND_STORE)
      .delete(CUSTOM_BACKGROUND_ID)
    request.onsuccess = () => resolve()
    request.onerror = () => reject(request.error)
  })
}

export function previewTheme(key) {
  const theme = getTheme(key)
  const root = document.documentElement
  const colors = theme.colors

  const entries = {
    '--bg': colors.bg,
    '--panel': colors.surface,
    '--panel-solid': colors.surface,
    '--text': colors.text,
    '--muted': colors.muted,
    '--primary': colors.primary,
    '--primary-deep': colors.primaryDeep,
    '--accent': colors.accent,
    '--border': colors.border,
    '--shadow': colors.shadow,
    '--bg-start': colors.bgStart,
    '--bg-end': colors.bgEnd,
    '--glow-primary': colors.glowPrimary,
    '--glow-accent': colors.glowAccent,
    '--surface': colors.surface,
    '--surface-soft': colors.surfaceSoft,
    '--topbar-bg': colors.topbar,
    '--hero-gradient': colors.hero,
    '--theme-tint': colors.tint,
    '--theme-tint-soft': colors.tintSoft,
    '--control-bg': colors.control,
    '--control-hover': colors.controlHover,
    '--system-sheen': colors.systemSheen,
    '--system-pattern': colors.systemPattern,
    '--system-pattern-soft': colors.systemPatternSoft,
    '--system-paper': colors.systemPaper,
    '--system-fiber': colors.systemFiber,
    '--el-bg-color': colors.surface,
    '--el-bg-color-overlay': colors.surface,
    '--el-fill-color': colors.surfaceSoft,
    '--el-fill-color-blank': colors.surface,
    '--el-fill-color-light': colors.surfaceSoft,
    '--el-border-color': colors.border,
    '--el-border-color-light': colors.border,
    '--el-text-color-primary': colors.text,
    '--el-text-color-regular': colors.muted
  }

  Object.entries(entries).forEach(([name, value]) => {
    root.style.setProperty(name, value)
  })

  return theme
}

export function applyTheme(key) {
  const theme = previewTheme(key)
  localStorage.setItem(THEME_STORAGE_KEY, theme.key)
  return theme
}

export function previewBackground(key, customUrl = getSavedBackgroundUrl()) {
  const root = document.documentElement
  if (key === 'custom' && customUrl) {
    root.style.setProperty('--app-background', `url("${customUrl}")`)
    root.style.setProperty('--app-background-size', 'cover')
    root.style.setProperty('--app-background-position', 'center')
    root.style.setProperty('--app-background-repeat', 'no-repeat')
    return { key: 'custom', name: '自定义背景', value: customUrl }
  }

  const background = getBackground('system')
  root.style.setProperty('--app-background', background.value)
  root.style.setProperty('--app-background-size', SYSTEM_BACKGROUND_SIZE)
  root.style.setProperty('--app-background-position', 'center')
  root.style.setProperty('--app-background-repeat', 'repeat')
  return background
}

export function applyBackground(key, customUrl = getSavedBackgroundUrl()) {
  const background = previewBackground(key, customUrl)
  localStorage.setItem(BACKGROUND_STORAGE_KEY, background.key)
  if (background.key === 'custom' && customUrl) {
    if (!customUrl.startsWith('blob:')) {
      localStorage.setItem(CUSTOM_BACKGROUND_STORAGE_KEY, customUrl)
    }
  } else {
    localStorage.removeItem(CUSTOM_BACKGROUND_STORAGE_KEY)
  }
  return background
}

export function previewAppearance({
  themeKey = getSavedThemeKey(),
  backgroundKey = getSavedBackgroundKey(),
  customUrl = getSavedBackgroundUrl()
} = {}) {
  const theme = previewTheme(themeKey)
  const background = previewBackground(backgroundKey, customUrl)
  return { theme, background }
}

export function applyAppearance({
  themeKey = getSavedThemeKey(),
  backgroundKey = getSavedBackgroundKey(),
  customUrl = getSavedBackgroundUrl()
} = {}) {
  const theme = previewTheme(themeKey)
  const background = applyBackground(backgroundKey, customUrl)
  localStorage.setItem(THEME_STORAGE_KEY, theme.key)
  return { theme, background }
}
