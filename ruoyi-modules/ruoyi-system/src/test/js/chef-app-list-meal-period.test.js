const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')
const { describe, it } = require('node:test')
const assert = require('node:assert/strict')

const moduleRoot = resolve(__dirname, '..', '..', 'main', 'java', 'org', 'dromara', 'system')
const bo = readFileSync(resolve(moduleRoot, 'domain', 'bo', 'cooking', 'DcCookChefBo.java'), 'utf8')
const service = readFileSync(resolve(moduleRoot, 'service', 'impl', 'cooking', 'DcCookChefServiceImpl.java'), 'utf8')

describe('app chef list meal period filter', () => {
  it('accepts mealPeriod in the chef list BO', () => {
    assert.match(bo, /private String mealPeriod;/)
  })

  it('only applies mealPeriod filtering on the app list query wrapper', () => {
    assert.match(service, /buildAppWrapper\(DcCookChefBo bo\)[\s\S]*applyMealPeriodFilter\(lqw,\s*bo\.getMealPeriod\(\)\)/)
    assert.match(service, /private void applyMealPeriodFilter\(LambdaQueryWrapper<DcCookChef> lqw,\s*String mealPeriod\)/)
    assert.match(service, /dc_cook_chef_time/)
    assert.match(service, /List<String> mealRemarks = resolveMealRemarks\(mealPeriod\)/)
    assert.match(service, /and remark in \(\{0\}, \{1\}\)/)
    assert.doesNotMatch(service, /time\(start_time\)/)
    assert.doesNotMatch(service, /time\(end_time\)/)
  })

  it('accepts historical meal remark aliases when filtering app chef list', () => {
    assert.match(service, /private List<String> resolveMealRemarks\(String mealPeriod\)/)
    assert.match(service, /return List\.of\("早餐", "早饭"\)/)
    assert.match(service, /return List\.of\("午餐", "午饭"\)/)
    assert.match(service, /return List\.of\("晚餐", "晚饭"\)/)
    assert.match(service, /and remark in \(\{0\}, \{1\}\)/)
  })
})
