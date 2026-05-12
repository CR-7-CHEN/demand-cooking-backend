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
  'DcCookSettlementServiceImpl.java'
)

const source = fs.readFileSync(servicePath, 'utf8')

test('settlement payable amount is base salary plus chef commission minus violation deduction', () => {
  assert.match(source, /settlement\.setChefCommission\(chefCommission\);/)
  assert.match(source, /settlement\.setViolationDeduction\(violationDeduction\);/)
  assert.match(source, /settlement\.setPayableAmount\(baseSalary\.add\(chefCommission\)\.subtract\(violationDeduction\)\);/)
})
