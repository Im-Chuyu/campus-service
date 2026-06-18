const BEIJING_TIME_ZONE = 'Asia/Shanghai'
const LOCAL_DATE_TIME_RE = /^(\d{4})-(\d{2})-(\d{2})[T\s](\d{2}):(\d{2})(?::(\d{2}))?/
const ZONED_DATE_TIME_RE = /(?:Z|[+-]\d{2}:?\d{2})$/

export const parseBeijingTime = value => {
  if (!value) return null
  if (value instanceof Date) return Number.isNaN(value.getTime()) ? null : value

  const text = String(value)
  if (!ZONED_DATE_TIME_RE.test(text)) {
    const match = text.match(LOCAL_DATE_TIME_RE)
    if (match) {
      const [, year, month, day, hour, minute, second = '0'] = match
      return new Date(Date.UTC(
        Number(year),
        Number(month) - 1,
        Number(day),
        Number(hour) - 8,
        Number(minute),
        Number(second)
      ))
    }
  }

  const date = new Date(text)
  return Number.isNaN(date.getTime()) ? null : date
}

const partsToMap = parts => {
  return Object.fromEntries(parts.filter(part => part.type !== 'literal').map(part => [part.type, part.value]))
}

export const formatBeijingDateTime = value => {
  const date = parseBeijingTime(value)
  if (!date) return value || '-'

  const parts = partsToMap(new Intl.DateTimeFormat('zh-CN', {
    timeZone: BEIJING_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).formatToParts(date))

  return `${parts.year}-${parts.month}-${parts.day} ${parts.hour}:${parts.minute}`
}

export const formatBeijingDate = value => {
  const date = parseBeijingTime(value)
  if (!date) return value || '-'

  const parts = partsToMap(new Intl.DateTimeFormat('zh-CN', {
    timeZone: BEIJING_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).formatToParts(date))

  return `${parts.year}-${parts.month}-${parts.day}`
}

export const formatBeijingClock = value => {
  const date = parseBeijingTime(value)
  if (!date) return ''

  const parts = partsToMap(new Intl.DateTimeFormat('zh-CN', {
    timeZone: BEIJING_TIME_ZONE,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).formatToParts(date))

  return `${parts.hour}:${parts.minute}:${parts.second}`
}

export const formatBeijingWeekdayDate = value => {
  const date = parseBeijingTime(value)
  if (!date) return ''
  return new Intl.DateTimeFormat('zh-CN', {
    timeZone: BEIJING_TIME_ZONE,
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  }).format(date)
}
