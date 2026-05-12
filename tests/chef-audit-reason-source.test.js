const test = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

const servicePath = path.join(
  __dirname,
  '..',
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
  'DcCookChefServiceImpl.java'
)

const source = fs.readFileSync(servicePath, 'utf8')

test('chef audit clears old reject reason explicitly when audit is not rejected', () => {
  assert.match(source, /Wrappers\.lambdaUpdate\(DcCookChef\.class\)/)
  assert.match(source, /\.set\(DcCookChef::getAuditReason,\s*null\)/)
  assert.match(source, /\.eq\(DcCookChef::getChefId,\s*bo\.getChefId\(\)\)/)
})
