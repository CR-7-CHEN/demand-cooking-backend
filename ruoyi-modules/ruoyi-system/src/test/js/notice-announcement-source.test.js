const test = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

const controllerPath = path.join(
  __dirname,
  '..',
  '..',
  'main',
  'java',
  'org',
  'dromara',
  'system',
  'controller',
  'cooking',
  'DcCookNoticeController.java'
)
const legacyControllerPath = path.join(
  __dirname,
  '..',
  '..',
  'main',
  'java',
  'org',
  'dromara',
  'system',
  'controller',
  'cooking',
  'DcCookConfigController.java'
)
const servicePath = path.join(
  __dirname,
  '..',
  '..',
  'main',
  'java',
  'org',
  'dromara',
  'system',
  'service',
  'impl',
  'SysNoticeServiceImpl.java'
)
const configServicePath = path.join(
  __dirname,
  '..',
  '..',
  'main',
  'java',
  'org',
  'dromara',
  'system',
  'service',
  'impl',
  'cooking',
  'DcCookConfigServiceImpl.java'
)

test('app workbench announcements are sourced from system notices', () => {
  assert.ok(fs.existsSync(controllerPath), 'DcCookNoticeController.java should exist')
  const source = fs.readFileSync(controllerPath, 'utf8')
  const legacySource = fs.readFileSync(legacyControllerPath, 'utf8')
  const serviceSource = fs.readFileSync(servicePath, 'utf8')
  const configServiceSource = fs.readFileSync(configServicePath, 'utf8')

  assert.match(source, /@GetMapping\("\/cooking\/notice\/announcement"\)/)
  assert.match(source, /ISysNoticeService/)
  assert.match(source, /selectAppNoticeList\(\)/)
  assert.match(legacySource, /@GetMapping\("\/cooking\/config\/commission\/announcement"\)/)
  assert.match(legacySource, /noticeService\.selectAppNoticeList\(\)/)
  assert.match(serviceSource, /selectAppNoticeList\(\)/)
  assert.match(serviceSource, /getStatus,\s*"0"/)
  assert.match(serviceSource, /getNoticeType,\s*"2"/)
  assert.match(configServiceSource, /getConfigType,\s*"ANNOUNCEMENT"/)
  assert.match(configServiceSource, /"ANNOUNCEMENT"\.equalsIgnoreCase\(bo\.getConfigType\(\)\)/)
  assert.match(configServiceSource, /announcement should be maintained in system notice/)
  assert.doesNotMatch(source, /DcCookConfig/)
  assert.doesNotMatch(source, /commission\/announcement/)
  assert.doesNotMatch(legacySource, /queryList\(bo\)/)
})
