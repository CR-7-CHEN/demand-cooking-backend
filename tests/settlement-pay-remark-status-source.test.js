const test = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

const root = path.join(__dirname, '..')
const service = fs.readFileSync(path.join(
  root,
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
), 'utf8')
const status = fs.readFileSync(path.join(
  root,
  'ruoyi-modules',
  'ruoyi-system',
  'src',
  'main',
  'java',
  'org',
  'dromara',
  'system',
  'domain',
  'cooking',
  'DcCookSettlementStatus.java'
), 'utf8')
const sql = fs.readFileSync(path.join(root, 'script', 'sql', 'demand_cooking.sql'), 'utf8')

test('settlement pay requires pay remark before writing paid status', () => {
  assert.match(service, /String payRemark = trimToNull\(bo\.getPayRemark\(\)\);/)
  assert.match(service, /if \(payRemark == null\) \{\s*throw new ServiceException\("payRemark is required"\);/s)
  assert.match(service, /settlement\.setStatus\(DcCookSettlementStatus\.PAID\);/)
  assert.match(service, /settlement\.setPayRemark\(payRemark\);/)
})

test('settlement status constants are numeric and legacy English values remain query compatible', () => {
  assert.match(status, /GENERATED = "0"/)
  assert.match(status, /REVIEWING = "1"/)
  assert.match(status, /CONFIRMED = "2"/)
  assert.match(status, /PAID = "3"/)
  assert.match(status, /LEGACY_GENERATED/)
  assert.match(status, /LEGACY_REVIEWING/)
  assert.match(status, /LEGACY_CONFIRMED/)
  assert.match(status, /LEGACY_PAID/)
  assert.match(status, /compatibleStatuses/)
  assert.match(service, /compatibleStatusValues/)
  assert.match(service, /lqw\.in\(DcCookSettlement::getStatus, compatibleStatusValues\(bo\.getStatus\(\)\)\);/)
})

test('settlement sql documents numeric status and pay remark', () => {
  assert.match(sql, /DEFAULT '0' COMMENT '结算状态（0已生成\/1复核中\/2已确认\/3已发放/)
  assert.match(sql, /pay_remark\s+varchar\(500\)\s+DEFAULT NULL COMMENT '打款说明'/)
})
