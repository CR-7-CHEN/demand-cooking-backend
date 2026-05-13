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
const sqlPath = path.join(__dirname, '..', 'script', 'sql', 'demand_cooking.sql')
const domainPath = path.join(
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
  'domain',
  'cooking',
  'DcCookChef.java'
)
const voPath = path.join(
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
  'domain',
  'vo',
  'cooking',
  'DcCookChefVo.java'
)
const sqlSource = fs.readFileSync(sqlPath, 'utf8')
const domainSource = fs.readFileSync(domainPath, 'utf8')
const voSource = fs.readFileSync(voPath, 'utf8')

test('chef audit clears old reject reason explicitly when audit is not rejected', () => {
  assert.match(source, /Wrappers\.lambdaUpdate\(DcCookChef\.class\)/)
  assert.match(source, /\.set\(DcCookChef::getAuditReason,\s*null\)/)
  assert.match(source, /\.eq\(DcCookChef::getChefId,\s*bo\.getChefId\(\)\)/)
})

test('chef audit schema and DTOs expose audit operator fields', () => {
  assert.match(sqlSource, /audit_by\s+bigint\(20\)\s+DEFAULT NULL COMMENT '审核人'/)
  assert.match(sqlSource, /audit_time\s+datetime\s+DEFAULT NULL COMMENT '审核时间'/)
  const chefTable = sqlSource.match(/CREATE TABLE IF NOT EXISTS dc_cook_chef \(([\s\S]*?)\) ENGINE=InnoDB/)
  assert.ok(chefTable, 'dc_cook_chef create table should exist')
  assert.match(chefTable[1], /audit_by\s+bigint\(20\)\s+DEFAULT NULL/)
  assert.match(chefTable[1], /audit_time\s+datetime\s+DEFAULT NULL/)
  assert.match(domainSource, /private Long auditBy;/)
  assert.match(domainSource, /private Date auditTime;/)
  assert.match(voSource, /private Long auditBy;/)
  assert.match(voSource, /private Date auditTime;/)
})

test('chef audit writes current operator and timestamp for every audit status transition', () => {
  assert.match(source, /import org\.dromara\.common\.satoken\.utils\.LoginHelper;/)
  assert.match(source, /Long auditBy = resolveAuditBy\(bo\);/)
  assert.match(source, /Date auditTime = new Date\(\);/)
  assert.match(source, /\.set\(DcCookChef::getAuditBy,\s*auditBy\)/)
  assert.match(source, /\.set\(DcCookChef::getAuditTime,\s*auditTime\)/)
  assert.match(source, /private Long resolveAuditBy\(DcCookChefBo bo\)/)
})
