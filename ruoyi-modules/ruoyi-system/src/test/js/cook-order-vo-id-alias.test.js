const { readFileSync } = require('node:fs')
const { resolve } = require('node:path')
const { describe, it } = require('node:test')
const assert = require('node:assert/strict')

const moduleRoot = resolve(__dirname, '..', '..', 'main', 'java', 'org', 'dromara', 'system')
const orderVo = readFileSync(resolve(moduleRoot, 'domain', 'vo', 'cooking', 'DcCookOrderVo.java'), 'utf8')
const orderService = readFileSync(resolve(moduleRoot, 'service', 'impl', 'cooking', 'DcCookOrderServiceImpl.java'), 'utf8')

describe('cook order vo id alias compatibility', () => {
  it('exposes id as a compatibility alias for orderId', () => {
    assert.match(orderVo, /private Long orderId;/)
    assert.match(orderVo, /public Long getId\(\)\s*\{\s*return orderId;\s*\}/)
    assert.match(orderVo, /public void setId\(Long id\)\s*\{\s*this\.orderId = id;\s*\}/)
  })

  it('keeps list and detail responses on the same order vo output chain', () => {
    assert.match(orderService, /public DcCookOrderVo queryById\(Long orderId\)\s*\{\s*DcCookOrderVo vo = baseMapper\.selectVoById\(orderId\);/)
    assert.match(orderService, /public TableDataInfo<DcCookOrderVo> queryPageList\(DcCookOrderBo bo,\s*PageQuery pageQuery\)\s*\{\s*Page<DcCookOrderVo> page = baseMapper\.selectVoPage\(pageQuery\.build\(\), buildQueryWrapper\(bo\)\);/)
  })
})
