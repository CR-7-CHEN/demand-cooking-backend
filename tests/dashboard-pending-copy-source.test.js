const test = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

const source = fs.readFileSync(path.join(
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
  'DcCookDashboardServiceImpl.java'
), 'utf8')

test('dashboard pending item labels user complaints as pending handling', () => {
  assert.match(source, /pendingItem\("complaintReply",\s*"用户投诉待处理"/)
  assert.doesNotMatch(source, /用户投诉待回复/)
})
