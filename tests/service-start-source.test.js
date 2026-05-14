const test = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

const repoRoot = path.join(__dirname, '..')
const serviceSource = fs.readFileSync(path.join(
  repoRoot,
  'ruoyi-modules',
  'ruoyi-system',
  'src',
  'main',
  'java',
  'org',
  'dromara',
  'system',
  'service',
  'impl',
  'cooking',
  'DcCookOrderServiceImpl.java'
), 'utf8')

const serviceStartMethod = serviceSource.match(/public Boolean serviceStart\(DcCookOrderActionBo bo\) \{([\s\S]*?)\n    \}/)

test('serviceStart allows early start, records actual start time, and does not notify user yet', () => {
  assert.ok(serviceStartMethod, 'serviceStart method should exist')
  const body = serviceStartMethod[1]

  assert.match(body, /assertStatus\(order,\s*DcCookOrderStatus\.WAITING_SERVICE\)/)
  assert.match(body, /Date startedAt = new Date\(\)/)
  assert.match(body, /order\.setServiceStartedFlag\("1"\)/)
  assert.match(body, /order\.setServiceStartedTime\(startedAt\)/)
  assert.doesNotMatch(body, /startedAt\.before\(order\.getServiceStartTime\(\)\)/)
  assert.doesNotMatch(body, /服务开始时间未到/)
  assert.doesNotMatch(body, /recordMessage\("SERVICE_START",\s*"USER"/)
  assert.doesNotMatch(body, /order\.setServiceStartTime\(/)
})
