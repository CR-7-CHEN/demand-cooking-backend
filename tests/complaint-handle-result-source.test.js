const test = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

const backendRoot = path.join(__dirname, '..')
const serviceSource = fs.readFileSync(path.join(
  backendRoot,
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
  'DcCookComplaintServiceImpl.java'
), 'utf8')
const statusSource = fs.readFileSync(path.join(
  backendRoot,
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
  'DcCookComplaintStatus.java'
), 'utf8')
const dashboardSource = fs.readFileSync(path.join(
  backendRoot,
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

test('complaint handle requires a non-empty handle result before updating status', () => {
  assert.match(serviceSource, /StringUtils\.isBlank\(bo\.getHandleResult\(\)\)/)
  assert.match(serviceSource, /throw new ServiceException\("处理说明不能为空"\)/)
  assert.match(serviceSource, /complaint\.setHandleResult\(bo\.getHandleResult\(\)\)/)
})

test('complaint status constants are numeric and legacy English statuses are still query compatible', () => {
  assert.match(statusSource, /public static final String PENDING = "0";/)
  assert.match(statusSource, /public static final String ESTABLISHED = "1";/)
  assert.match(statusSource, /public static final String REJECTED = "2";/)
  assert.match(statusSource, /public static final String LEGACY_PENDING = "PENDING";/)
  assert.match(statusSource, /legacyValues\(String status\)/)
  assert.match(serviceSource, /\.in\(StringUtils\.isNotBlank\(bo\.getStatus\(\)\),\s*DcCookComplaint::getStatus,\s*DcCookComplaintStatus\.compatibleStatuses\(bo\.getStatus\(\)\)\)/)
  assert.match(dashboardSource, /\.in\(DcCookComplaint::getStatus,\s*DcCookComplaintStatus\.compatibleStatuses\(DcCookComplaintStatus\.PENDING\)\)/)
})
