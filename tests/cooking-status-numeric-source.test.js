const test = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

const repoRoot = path.join(__dirname, '..')
const statusDir = path.join(
  repoRoot,
  'ruoyi-modules',
  'ruoyi-system',
  'src',
  'main',
  'java',
  'org',
  'dromara',
  'system',
  'domain',
  'cooking'
)
const serviceDir = path.join(
  repoRoot,
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
  'cooking'
)
const sqlSource = fs.readFileSync(path.join(repoRoot, 'script', 'sql', 'demand_cooking.sql'), 'utf8')

test('message statuses use numeric storage with legacy compatibility', () => {
  const messageStatus = fs.readFileSync(path.join(statusDir, 'DcCookMessageStatus.java'), 'utf8')
  const messageService = fs.readFileSync(path.join(serviceDir, 'DcCookMessageServiceImpl.java'), 'utf8')
  const orderService = fs.readFileSync(path.join(serviceDir, 'DcCookOrderServiceImpl.java'), 'utf8')

  assert.match(messageStatus, /PENDING = "0"/)
  assert.match(messageStatus, /SENT = "1"/)
  assert.match(messageStatus, /FAILED = "2"/)
  assert.match(messageStatus, /SENDING = "3"/)
  assert.match(messageService, /DcCookMessageStatus\.compatibleStatuses\(bo\.getSendStatus\(\)\)/)
  assert.match(orderService, /message\.setSendStatus\(DcCookMessageStatus\.SENT\)/)
  assert.doesNotMatch(orderService, /message\.setSendStatus\("SENT"\)/)
})

test('init sql stores remaining cooking status fields as numeric defaults', () => {
  assert.match(sqlSource, /audit_status\s+varchar\(30\)\s+DEFAULT '0'/)
  assert.match(sqlSource, /chef_status\s+varchar\(30\)\s+DEFAULT '0'/)
  assert.match(sqlSource, /status\s+varchar\(40\)\s+NOT NULL DEFAULT '0'/)
  assert.match(sqlSource, /WHEN 'PENDING_REVIEW' THEN '0'/)
  assert.match(sqlSource, /send_status\s+varchar\(30\)\s+DEFAULT '0' COMMENT '发送状态（0待发送 1已发送 2失败 3发送中/)
  assert.match(sqlSource, /status\s+varchar\(20\)\s+DEFAULT '0' COMMENT '状态（0待处理 1已回复 2已关闭/)
  assert.match(sqlSource, /UPDATE dc_cook_message[\s\S]*WHEN 'SENT' THEN '1'/)
  assert.match(sqlSource, /UPDATE dc_cook_support_ticket[\s\S]*WHEN 'CLOSED' THEN '2'/)
})

test('init sql is safe for a fresh MySQL 8 schema without duplicate add-column patches', () => {
  assert.doesNotMatch(sqlSource, /ADD COLUMN IF NOT EXISTS/i)
  assert.doesNotMatch(sqlSource, /ALTER TABLE dc_cook_chef[\s\S]*ADD COLUMN/i)
  assert.doesNotMatch(sqlSource, /ALTER TABLE dc_cook_order[\s\S]*ADD COLUMN/i)
  assert.doesNotMatch(sqlSource, /ALTER TABLE dc_cook_settlement[\s\S]*ADD COLUMN/i)

  const chefTable = sqlSource.match(/CREATE TABLE IF NOT EXISTS dc_cook_chef \(([\s\S]*?)\) ENGINE=InnoDB/)
  const orderTable = sqlSource.match(/CREATE TABLE IF NOT EXISTS dc_cook_order \(([\s\S]*?)\) ENGINE=InnoDB/)
  const settlementTable = sqlSource.match(/CREATE TABLE IF NOT EXISTS dc_cook_settlement \(([\s\S]*?)\) ENGINE=InnoDB/)
  assert.ok(chefTable, 'dc_cook_chef create table should exist')
  assert.ok(orderTable, 'dc_cook_order create table should exist')
  assert.ok(settlementTable, 'dc_cook_settlement create table should exist')

  assert.match(chefTable[1], /audit_by\s+bigint\(20\)\s+DEFAULT NULL/)
  assert.match(chefTable[1], /audit_time\s+datetime\s+DEFAULT NULL/)
  assert.match(orderTable[1], /service_started_flag\s+char\(1\)\s+DEFAULT '0'/)
  assert.match(orderTable[1], /service_started_time\s+datetime\s+DEFAULT NULL/)
  assert.match(settlementTable[1], /review_reason_type\s+varchar\(40\)\s+DEFAULT NULL/)
  assert.match(settlementTable[1], /review_remark\s+varchar\(500\)\s+DEFAULT NULL/)
  assert.match(settlementTable[1], /review_result\s+varchar\(20\)\s+DEFAULT NULL/)
  assert.match(settlementTable[1], /review_reply\s+varchar\(500\)\s+DEFAULT NULL/)
  assert.match(settlementTable[1], /review_apply_time\s+datetime\s+DEFAULT NULL/)
  assert.match(settlementTable[1], /review_handle_time\s+datetime\s+DEFAULT NULL/)
  assert.match(settlementTable[1], /confirm_time\s+datetime\s+DEFAULT NULL/)
  assert.match(settlementTable[1], /pay_time\s+datetime\s+DEFAULT NULL/)
  assert.match(settlementTable[1], /pay_remark\s+varchar\(500\)\s+DEFAULT NULL/)
})
