-- 上门做饭：客服机器人 FAQ 与工单最小闭环

CREATE TABLE IF NOT EXISTS dc_cook_faq (
    faq_id       bigint(20)    NOT NULL COMMENT 'FAQ ID',
    tenant_id    varchar(20)   DEFAULT '000000' COMMENT '租户ID',
    category     varchar(60)   NOT NULL COMMENT '分类',
    question     varchar(200)  NOT NULL COMMENT '问题',
    answer       varchar(1000) NOT NULL COMMENT '自动回复',
    keywords     varchar(300)  DEFAULT NULL COMMENT '关键词，逗号分隔',
    sort         int           DEFAULT 0 COMMENT '排序',
    status       char(1)       DEFAULT '0' COMMENT '状态（0启用 1停用）',
    create_dept  bigint(20)    DEFAULT NULL COMMENT '创建部门',
    create_by    bigint(20)    DEFAULT NULL COMMENT '创建者',
    create_time  datetime      DEFAULT NULL COMMENT '创建时间',
    update_by    bigint(20)    DEFAULT NULL COMMENT '更新者',
    update_time  datetime      DEFAULT NULL COMMENT '更新时间',
    remark       varchar(500)  DEFAULT NULL COMMENT '备注',
    del_flag     char(1)       DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (faq_id),
    KEY idx_dc_cook_faq_status (tenant_id, status, category, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上门做饭FAQ表';

CREATE TABLE IF NOT EXISTS dc_cook_support_ticket (
    ticket_id    bigint(20)    NOT NULL COMMENT '工单ID',
    tenant_id    varchar(20)   DEFAULT '000000' COMMENT '租户ID',
    user_id      bigint(20)    NOT NULL COMMENT '用户ID',
    order_id     bigint(20)    DEFAULT NULL COMMENT '关联订单ID',
    question     varchar(1000) NOT NULL COMMENT '用户问题',
    reply        varchar(1000) DEFAULT NULL COMMENT '处理回复',
    status       varchar(20)   DEFAULT 'PENDING' COMMENT '状态（PENDING待处理 REPLIED已回复 CLOSED已关闭）',
    handler_id   bigint(20)    DEFAULT NULL COMMENT '处理人ID',
    handle_time  datetime      DEFAULT NULL COMMENT '处理时间',
    close_time   datetime      DEFAULT NULL COMMENT '关闭时间',
    create_dept  bigint(20)    DEFAULT NULL COMMENT '创建部门',
    create_by    bigint(20)    DEFAULT NULL COMMENT '创建者',
    create_time  datetime      DEFAULT NULL COMMENT '创建时间',
    update_by    bigint(20)    DEFAULT NULL COMMENT '更新者',
    update_time  datetime      DEFAULT NULL COMMENT '更新时间',
    remark       varchar(500)  DEFAULT NULL COMMENT '备注',
    del_flag     char(1)       DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (ticket_id),
    KEY idx_dc_cook_support_ticket_user (tenant_id, user_id, status, create_time),
    KEY idx_dc_cook_support_ticket_order (tenant_id, order_id),
    KEY idx_dc_cook_support_ticket_status (tenant_id, status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上门做饭客服工单表';

DELETE FROM dc_cook_faq WHERE faq_id BETWEEN 300101 AND 300105;

INSERT INTO dc_cook_faq
(faq_id, tenant_id, category, question, answer, keywords, sort, status, create_dept, create_by, create_time, remark)
VALUES
(300101, '000000', '订单问题', '如何查看订单状态', '可在订单详情查看当前状态，也可以在客服机器人中输入订单状态查询。', '订单状态,进度,查看订单', 1, '0', 103, 1, NOW(), '首版FAQ'),
(300102, '000000', '订单问题', '如何取消订单', '未付款订单可在订单详情取消；已付款订单需满足取消规则后提交取消申请。', '取消订单,退单,不想要', 2, '0', 103, 1, NOW(), '首版FAQ'),
(300103, '000000', '支付问题', '支付超时怎么办', '支付超时后订单会自动关闭，可重新发起预约。', '支付超时,付款超时,无法支付', 3, '0', 103, 1, NOW(), '首版FAQ'),
(300104, '000000', '服务问题', '做饭人员什么时候上门', '做饭人员会按预约时间上门服务，请保持联系电话畅通。', '上门时间,什么时候来,做饭人员', 4, '0', 103, 1, NOW(), '首版FAQ'),
(300105, '000000', '工单问题', '机器人无法回答怎么办', '机器人无法回答的问题可以提交工单，后台处理后会展示处理回复。', '无法回答,工单,提交问题', 5, '0', 103, 1, NOW(), '首版FAQ');

DELETE FROM sys_menu WHERE menu_id BETWEEN 300040 AND 300049;
DELETE FROM sys_menu WHERE menu_id IN (300011, 300012);

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
(300011, 'FAQ管理', 300000, 11, 'faq', 'cooking/faq/index', '', 1, 0, 'C', '0', '0', 'cooking:supportFaq:list', 'question', 103, 1, NOW(), NULL, NULL, 'FAQ管理'),
(300012, '工单管理', 300000, 12, 'ticket', 'cooking/ticket/index', '', 1, 0, 'C', '0', '0', 'cooking:supportTicket:list', 'form', 103, 1, NOW(), NULL, NULL, '工单管理');

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
(300040, 'FAQ新增', 300011, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:supportFaq:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(300041, 'FAQ编辑', 300011, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:supportFaq:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300042, 'FAQ删除', 300011, 3, '', '', '', 1, 0, 'F', '0', '0', 'cooking:supportFaq:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(300043, '工单处理', 300012, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:supportTicket:handle', '#', 103, 1, NOW(), NULL, NULL, ''),
(300044, '工单关闭', 300012, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:supportTicket:close', '#', 103, 1, NOW(), NULL, NULL, '');
