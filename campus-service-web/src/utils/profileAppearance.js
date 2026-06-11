const STORAGE_PREFIX = 'campus_profile_extra:'

export function getProfileExtra(userId) {
  if (!userId) return {}
  try {
    return JSON.parse(localStorage.getItem(`${STORAGE_PREFIX}${userId}`) || '{}')
  } catch {
    return {}
  }
}

export function saveProfileExtra(userId, extra) {
  if (!userId) return
  localStorage.setItem(`${STORAGE_PREFIX}${userId}`, JSON.stringify(extra || {}))
}

export function getDisplayedAvatar(user, extra) {
  return user?.avatar || extra?.avatar || ''
}

export function getDisplayedBackground(user, extra) {
  return user?.profileBackground || extra?.background || ''
}

export function maskPhone(phone) {
  if (!phone) return '未填写'
  const text = String(phone)
  if (text.length < 7) return '用户已隐藏'
  return `${text.slice(0, 3)}****${text.slice(-4)}`
}

export function maskEmail(email) {
  if (!email) return '未填写'
  const [name, domain] = String(email).split('@')
  if (!domain) return '用户已隐藏'
  const prefix = name.slice(0, 2) || '*'
  return `${prefix}***@${domain}`
}
