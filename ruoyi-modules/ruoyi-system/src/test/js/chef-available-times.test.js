const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')
const { describe, it } = require('node:test')
const assert = require('node:assert/strict')

const moduleRoot = resolve(__dirname, '..', '..', 'main', 'java', 'org', 'dromara', 'system')
const bo = readFileSync(resolve(moduleRoot, 'domain', 'bo', 'cooking', 'DcCookChefBo.java'), 'utf8')
const service = readFileSync(resolve(moduleRoot, 'service', 'impl', 'cooking', 'DcCookChefServiceImpl.java'), 'utf8')
const timeService = readFileSync(resolve(moduleRoot, 'service', 'impl', 'cooking', 'DcCookChefTimeServiceImpl.java'), 'utf8')

describe('chef application available times persistence', () => {
  it('accepts availableTimes on the chef BO and persists them with chef apply/update', () => {
    assert.match(bo, /List<DcCookChefTimeBo>\s+availableTimes/)
    assert.match(service, /saveAvailableTimes\(add\.getChefId\(\),\s*bo\.getAvailableTimes\(\)\)/)
    assert.match(service, /saveAvailableTimes\(bo\.getChefId\(\),\s*bo\.getAvailableTimes\(\)\)/)
    assert.match(service, /private void saveAvailableTimes\(Long chefId,\s*List<DcCookChefTimeBo> availableTimes\)/)
  })

  it('hydrates available time text for backend chef management lists', () => {
    assert.match(service, /queryPageList\(DcCookChefBo bo,\s*PageQuery pageQuery\)[\s\S]*hydrateAvailableTimes\(page\.getRecords\(\)\)/)
    assert.match(service, /queryList\(DcCookChefBo bo\)[\s\S]*hydrateAvailableTimes\(list\)/)
  })

  it('validates meal selection remark when saving available times', () => {
    assert.match(timeService, /MEAL_REMARK_OPTIONS = List\.of\("早餐", "午餐", "晚餐"\)/)
    assert.match(timeService, /remark must be one of 早餐\/午餐\/晚餐/)
    assert.match(service, /DcCookChefTimeServiceImpl\.isValidMealRemark\(time\.getRemark\(\)\)/)
  })
})
