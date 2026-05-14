const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { join } = require('node:path');
const { test } = require('node:test');

function source(...segments) {
  return readFileSync(join(__dirname, '..', ...segments), 'utf8');
}

const orderService = source('ruoyi-modules', 'ruoyi-system', 'src', 'main', 'java', 'org', 'dromara', 'system', 'service', 'impl', 'cooking', 'DcCookOrderServiceImpl.java');
const orderController = source('ruoyi-modules', 'ruoyi-system', 'src', 'main', 'java', 'org', 'dromara', 'system', 'controller', 'cooking', 'DcCookOrderController.java');
const reviewService = source('ruoyi-modules', 'ruoyi-system', 'src', 'main', 'java', 'org', 'dromara', 'system', 'service', 'impl', 'cooking', 'DcCookReviewServiceImpl.java');
const complaintService = source('ruoyi-modules', 'ruoyi-system', 'src', 'main', 'java', 'org', 'dromara', 'system', 'service', 'impl', 'cooking', 'DcCookComplaintServiceImpl.java');
const orderVo = source('ruoyi-modules', 'ruoyi-system', 'src', 'main', 'java', 'org', 'dromara', 'system', 'domain', 'vo', 'cooking', 'DcCookOrderVo.java');

test('miniapp-visible cooking order errors are Chinese and no targeted English messages remain', () => {
  assert.match(orderService, /"\u8ba2\u5355\u72b6\u6001\u4e0d\u6b63\u786e"/);
  assert.match(orderController, /"\u65e0\u6743\u64cd\u4f5c\u8be5\u8ba2\u5355"/);
  assert.match(reviewService, /"\u8ba2\u5355\u5df2\u8bc4\u4ef7"/);
  assert.match(reviewService, /"\u4ec5\u5df2\u5b8c\u6210\u8ba2\u5355\u53ef\u8bc4\u4ef7"/);
  assert.match(reviewService, /"\u65e0\u6743\u8bc4\u4ef7\u8be5\u8ba2\u5355"/);
  assert.match(complaintService, /"\u4ec5\u5df2\u5b8c\u6210\u8ba2\u5355\u53ef\u6295\u8bc9"/);
  assert.match(complaintService, /"\u65e0\u6743\u6295\u8bc9\u8be5\u8ba2\u5355"/);

  for (const text of [orderService, orderController, reviewService, complaintService]) {
    assert.doesNotMatch(text, /service start time not reached/);
    assert.doesNotMatch(text, /no permission to operate this order/);
    assert.doesNotMatch(text, /invalid order status/);
    assert.doesNotMatch(text, /order already reviewed/);
    assert.doesNotMatch(text, /only completed order can be reviewed/);
    assert.doesNotMatch(text, /no permission to review this order/);
    assert.doesNotMatch(text, /only completed order can be complained/);
    assert.doesNotMatch(text, /no permission to complain this order/);
  }
});

test('order detail response exposes structured review and complaint status fields with Chinese display text', () => {
  assert.match(orderVo, /private Long reviewId;/);
  assert.match(orderVo, /private Boolean reviewed;/);
  assert.match(orderVo, /private String reviewStatusText;/);
  assert.match(orderVo, /private Long complaintId;/);
  assert.match(orderVo, /private Boolean complained;/);
  assert.match(orderVo, /private String complaintStatusText;/);

  assert.match(orderService, /DcCookReviewMapper/);
  assert.match(orderService, /DcCookComplaintMapper/);
  assert.match(orderService, /setReviewStatusText\(\s*reviewed \? "\u5df2\u8bc4\u4ef7" : "\u672a\u8bc4\u4ef7"\s*\)/);
  assert.match(orderService, /setComplaintStatusText\(\s*complained \? "\u5df2\u6295\u8bc9" : "\u672a\u6295\u8bc9"\s*\)/);
});
