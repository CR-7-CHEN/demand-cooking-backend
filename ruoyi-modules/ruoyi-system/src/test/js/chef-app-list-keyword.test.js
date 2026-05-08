const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')
const { describe, it } = require('node:test')
const assert = require('node:assert/strict')

const moduleRoot = resolve(__dirname, '..', '..', 'main', 'java', 'org', 'dromara', 'system')
const bo = readFileSync(resolve(moduleRoot, 'domain', 'bo', 'cooking', 'DcCookChefBo.java'), 'utf8')
const service = readFileSync(resolve(moduleRoot, 'service', 'impl', 'cooking', 'DcCookChefServiceImpl.java'), 'utf8')

describe('app chef list keyword filter', () => {
  it('accepts a homepage keyword parameter', () => {
    assert.match(bo, /private String keyword;/)
  })

  it('matches keyword against chef name and cuisine tags only', () => {
    assert.match(service, /String keyword = StringUtils\.trim\(bo\.getKeyword\(\)\)/)
    assert.match(service, /like\(DcCookChef::getChefName,\s*keyword\)\.or\(\)\.like\(DcCookChef::getSkillTags,\s*keyword\)/)

    const keywordBlock = service.match(/lqw\.and\(StringUtils\.isNotBlank\(keyword\),[\s\S]*?DcCookChef::getSkillTags,\s*keyword\)\);/)
    assert.ok(keywordBlock, 'expected a bounded keyword filter block')
    assert.doesNotMatch(keywordBlock[0], /DcCookChef::getAreaName/)
  })
})
