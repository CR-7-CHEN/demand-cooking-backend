const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')
const { describe, it } = require('node:test')
const assert = require('node:assert/strict')

const servicePath = resolve(
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
  'DcCookChefServiceImpl.java'
)
const service = readFileSync(servicePath, 'utf8')

describe('chef available time text formatting', () => {
  it('does not truncate availableTimeText to only three future slots', () => {
    const formatMethod = service.match(/private String formatAvailableTimeText\(List<DcCookChefTimeVo> times\) \{[\s\S]*?\n    \}/)
    assert.ok(formatMethod, 'formatAvailableTimeText method should exist')
    assert.doesNotMatch(formatMethod[0], /\.limit\(3\)/)
  })
})
