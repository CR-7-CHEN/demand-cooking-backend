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

test('dashboard recent orders requests ten items ordered by create time desc', () => {
  assert.match(source, /selectPage\(new Page<>\(1,\s*10\)/)
  assert.match(source, /orderByDesc\(DcCookOrder::getCreateTime\)/)
  assert.doesNotMatch(source, /selectPage\(new Page<>\(1,\s*4\)/)
})
