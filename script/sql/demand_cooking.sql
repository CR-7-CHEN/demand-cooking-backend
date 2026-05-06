-- 上门做饭 MVP 数据库脚本 (MySQL)
-- 请在若依基础脚本 ry_vue_5.X.sql、ry_job.sql、ry_workflow.sql 之后执行。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 核心业务表
-- ----------------------------

CREATE TABLE IF NOT EXISTS dc_cook_area (
    area_id          bigint(20)   NOT NULL COMMENT '服务区域ID',
    tenant_id        varchar(20)  DEFAULT '000000' COMMENT '租户ID',
    area_code        varchar(64)  NOT NULL COMMENT '区域编码',
    area_name        varchar(100) NOT NULL COMMENT '区域名称',
    parent_code      varchar(64)  DEFAULT NULL COMMENT '父级区域编码',
    area_level       varchar(20)  DEFAULT 'CITY' COMMENT '区域层级',
    status           char(1)      DEFAULT '0' COMMENT '状态（0启用 1停用）',
    sort             int          DEFAULT 0 COMMENT '排序',
    create_dept      bigint(20)   DEFAULT NULL COMMENT '创建部门',
    create_by        bigint(20)   DEFAULT NULL COMMENT '创建者',
    create_time      datetime     DEFAULT NULL COMMENT '创建时间',
    update_by        bigint(20)   DEFAULT NULL COMMENT '更新者',
    update_time      datetime     DEFAULT NULL COMMENT '更新时间',
    remark           varchar(500) DEFAULT NULL COMMENT '备注',
    del_flag         char(1)      DEFAULT '0' COMMENT '删除标志',
    PRIMARY KEY (area_id),
    UNIQUE KEY uk_dc_cook_area_code (tenant_id, area_code),
    KEY idx_dc_cook_area_status (tenant_id, status, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务区域表';

CREATE TABLE IF NOT EXISTS dc_cook_chef (
    chef_id                 bigint(20)    NOT NULL COMMENT '厨师ID',
    tenant_id               varchar(20)   DEFAULT '000000' COMMENT '租户ID',
    user_id                 bigint(20)    DEFAULT NULL COMMENT '关联系统用户ID',
    chef_name               varchar(80)   NOT NULL COMMENT '厨师姓名',
    gender                  char(1)       DEFAULT NULL COMMENT '性别',
    age                     int           DEFAULT NULL COMMENT '年龄',
    mobile                  varchar(20)   NOT NULL COMMENT '手机号',
    avatar_url              varchar(500)  DEFAULT NULL COMMENT '头像地址',
    work_image_urls         varchar(2000) DEFAULT NULL COMMENT '作品图地址，多个逗号分隔',
    area_id                 bigint(20)    DEFAULT NULL COMMENT '主服务区域ID',
    area_name               varchar(100)  DEFAULT NULL COMMENT '主服务区域名称',
    skill_tags              varchar(500)  DEFAULT NULL COMMENT '擅长标签',
    intro                   varchar(1000) DEFAULT NULL COMMENT '个人简介',
    health_cert_no          varchar(100)  DEFAULT NULL COMMENT '健康证编号',
    health_cert_image_url   varchar(500)  DEFAULT NULL COMMENT '健康证图片地址',
    health_cert_expire_date datetime      DEFAULT NULL COMMENT '健康证到期日期',
    base_salary             decimal(12,2) DEFAULT 0.00 COMMENT '个人底薪',
    rating                  decimal(3,2)  DEFAULT 5.00 COMMENT '平均评分',
    completed_orders        bigint(20)    DEFAULT 0 COMMENT '已完成订单数',
    recommend_flag          char(1)       DEFAULT '0' COMMENT '推荐标志（1推荐）',
    audit_status            varchar(30)   DEFAULT '0' COMMENT '审核状态（0待审核 1已通过 2已拒绝）',
    audit_reason            varchar(500)  DEFAULT NULL COMMENT '审核原因',
    chef_status             varchar(30)   DEFAULT '0' COMMENT '厨师状态（0正常 1暂停 2禁用 3离职）',
    create_dept             bigint(20)    DEFAULT NULL COMMENT '创建部门',
    create_by               bigint(20)    DEFAULT NULL COMMENT '创建者',
    create_time             datetime      DEFAULT NULL COMMENT '创建时间',
    update_by               bigint(20)    DEFAULT NULL COMMENT '更新者',
    update_time             datetime      DEFAULT NULL COMMENT '更新时间',
    remark                  varchar(500)  DEFAULT NULL COMMENT '备注',
    del_flag                char(1)       DEFAULT '0' COMMENT '删除标志',
    PRIMARY KEY (chef_id),
    UNIQUE KEY uk_dc_cook_chef_mobile (tenant_id, mobile, del_flag),
    KEY idx_dc_cook_chef_user (tenant_id, user_id),
    KEY idx_dc_cook_chef_area_status (tenant_id, area_id, audit_status, chef_status, del_flag),
    KEY idx_dc_cook_chef_sort (tenant_id, recommend_flag, completed_orders, rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='厨师表';

CREATE TABLE IF NOT EXISTS dc_cook_chef_time (
    time_id     bigint(20)   NOT NULL COMMENT '可预约时间ID',
    tenant_id   varchar(20)  DEFAULT '000000' COMMENT '租户ID',
    chef_id     bigint(20)   NOT NULL COMMENT '厨师ID',
    start_time  datetime     NOT NULL COMMENT '可预约开始时间',
    end_time    datetime     NOT NULL COMMENT '可预约结束时间',
    status      char(1)      DEFAULT '0' COMMENT '状态（0启用 1停用）',
    create_dept bigint(20)   DEFAULT NULL COMMENT '创建部门',
    create_by   bigint(20)   DEFAULT NULL COMMENT '创建者',
    create_time datetime     DEFAULT NULL COMMENT '创建时间',
    update_by   bigint(20)   DEFAULT NULL COMMENT '更新者',
    update_time datetime     DEFAULT NULL COMMENT '更新时间',
    remark      varchar(500) DEFAULT NULL COMMENT '备注',
    del_flag    char(1)      DEFAULT '0' COMMENT '删除标志',
    PRIMARY KEY (time_id),
    KEY idx_dc_cook_chef_time_chef (tenant_id, chef_id, start_time, end_time, status, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='厨师可预约时间表';

CREATE TABLE IF NOT EXISTS dc_cook_address (
    address_id        bigint(20)    NOT NULL COMMENT '地址ID',
    tenant_id         varchar(20)   DEFAULT '000000' COMMENT '租户ID',
    user_id           bigint(20)    NOT NULL COMMENT '用户ID',
    contact_name      varchar(80)   NOT NULL COMMENT '联系人姓名',
    contact_phone     varchar(20)   NOT NULL COMMENT '联系人电话',
    area_code         varchar(64)   DEFAULT NULL COMMENT '区域编码',
    area_name         varchar(100)  DEFAULT NULL COMMENT '区域名称',
    detail_address    varchar(500)  NOT NULL COMMENT '详细地址',
    house_number      varchar(100)  DEFAULT NULL COMMENT '门牌号',
    longitude         decimal(12,6) DEFAULT NULL COMMENT '经度',
    latitude          decimal(12,6) DEFAULT NULL COMMENT '纬度',
    default_flag      char(1)       DEFAULT '0' COMMENT '默认地址（1是）',
    source_address_id bigint(20)    DEFAULT NULL COMMENT '快照来源地址ID',
    snapshot_type     varchar(20)   DEFAULT 'NORMAL' COMMENT '类型（NORMAL普通 ORDER订单快照）',
    snapshot_time     datetime      DEFAULT NULL COMMENT '快照时间',
    create_dept       bigint(20)    DEFAULT NULL COMMENT '创建部门',
    create_by         bigint(20)    DEFAULT NULL COMMENT '创建者',
    create_time       datetime      DEFAULT NULL COMMENT '创建时间',
    update_by         bigint(20)    DEFAULT NULL COMMENT '更新者',
    update_time       datetime      DEFAULT NULL COMMENT '更新时间',
    remark            varchar(500)  DEFAULT NULL COMMENT '备注',
    del_flag          char(1)       DEFAULT '0' COMMENT '删除标志',
    PRIMARY KEY (address_id),
    KEY idx_dc_cook_address_user (tenant_id, user_id, snapshot_type, del_flag),
    KEY idx_dc_cook_address_area (tenant_id, area_code, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

CREATE TABLE IF NOT EXISTS dc_cook_dish (
    dish_id      bigint(20)    NOT NULL COMMENT '菜品ID',
    tenant_id    varchar(20)   DEFAULT '000000' COMMENT '租户ID',
    dish_name    varchar(100)  NOT NULL COMMENT '菜品名称',
    category     varchar(60)   DEFAULT NULL COMMENT '菜品分类',
    cuisine      varchar(60)   DEFAULT NULL COMMENT '菜系',
    image_url    varchar(500)  DEFAULT NULL COMMENT '图片地址',
    description  varchar(1000) DEFAULT NULL COMMENT '菜品描述',
    status       char(1)       DEFAULT '0' COMMENT '状态（0启用 1停用）',
    sort         int           DEFAULT 0 COMMENT '排序',
    create_dept  bigint(20)    DEFAULT NULL COMMENT '创建部门',
    create_by    bigint(20)    DEFAULT NULL COMMENT '创建者',
    create_time  datetime      DEFAULT NULL COMMENT '创建时间',
    update_by    bigint(20)    DEFAULT NULL COMMENT '更新者',
    update_time  datetime      DEFAULT NULL COMMENT '更新时间',
    remark       varchar(500)  DEFAULT NULL COMMENT '备注',
    del_flag     char(1)       DEFAULT '0' COMMENT '删除标志',
    PRIMARY KEY (dish_id),
    KEY idx_dc_cook_dish_status (tenant_id, status, del_flag),
    KEY idx_dc_cook_dish_category (tenant_id, category, cuisine)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品库';

CREATE TABLE IF NOT EXISTS dc_cook_config (
    config_id             bigint(20)    NOT NULL COMMENT '配置ID',
    tenant_id             varchar(20)   DEFAULT '000000' COMMENT '租户ID',
    config_name           varchar(100)  NOT NULL COMMENT '配置名称',
    config_key            varchar(100)  NOT NULL COMMENT '配置键',
    config_value          varchar(1000) NOT NULL COMMENT '配置值',
    value_type            varchar(30)   DEFAULT 'STRING' COMMENT '值类型（STRING字符串 NUMBER数值 JSON对象 PERCENT百分比）',
    config_type           varchar(30)   DEFAULT 'BUSINESS' COMMENT '配置分类（BUSINESS业务 ORDER订单 REFUND退款 SETTLEMENT结算 RESERVE预约 MESSAGE消息 ANNOUNCEMENT公告）',
    rule_flag             char(1)       DEFAULT '1' COMMENT '规则配置（1是）',
    effective_time        datetime      DEFAULT NULL COMMENT '生效时间',
    publish_status        varchar(30)   DEFAULT 'PUBLISHED' COMMENT '发布状态（PUBLISHED已发布 DRAFT草稿 UNPUBLISHED未发布）',
    change_reason         varchar(500)  DEFAULT NULL COMMENT '变更原因',
    announcement_content  varchar(1000) DEFAULT NULL COMMENT '公告内容',
    create_dept           bigint(20)    DEFAULT NULL COMMENT '创建部门',
    create_by             bigint(20)    DEFAULT NULL COMMENT '创建者',
    create_time           datetime      DEFAULT NULL COMMENT '创建时间',
    update_by             bigint(20)    DEFAULT NULL COMMENT '更新者',
    update_time           datetime      DEFAULT NULL COMMENT '更新时间',
    remark                varchar(500)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_dc_cook_config_key (tenant_id, config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务配置表';

CREATE TABLE IF NOT EXISTS dc_cook_message (
    message_id            bigint(20)    NOT NULL COMMENT '消息ID',
    tenant_id             varchar(20)   DEFAULT '000000' COMMENT '租户ID',
    message_type          varchar(60)   NOT NULL COMMENT '消息类型',
    channel               varchar(30)   NOT NULL COMMENT '渠道（WECHAT微信 SMS短信 IN_APP站内）',
    receiver_type         varchar(30)   DEFAULT NULL COMMENT '接收者类型（USER用户 CHEF厨师 ADMIN管理员）',
    receiver_id           bigint(20)    DEFAULT NULL COMMENT '接收者ID',
    receiver_mobile_mask  varchar(40)   DEFAULT NULL COMMENT '手机号掩码',
    receiver_openid_mask  varchar(80)   DEFAULT NULL COMMENT 'OpenID掩码',
    related_order_id      bigint(20)    DEFAULT NULL COMMENT '关联订单ID',
    related_order_no      varchar(40)   DEFAULT NULL COMMENT '关联订单号',
    related_biz_type      varchar(60)   DEFAULT NULL COMMENT '关联业务类型',
    related_biz_id        bigint(20)    DEFAULT NULL COMMENT '关联业务ID',
    content_summary       varchar(1000) DEFAULT NULL COMMENT '内容摘要',
    send_status           varchar(30)   DEFAULT 'PENDING' COMMENT '发送状态（PENDING待发送 SUCCESS成功 FAILED失败）',
    send_time             datetime      DEFAULT NULL COMMENT '发送时间',
    fail_reason           varchar(500)  DEFAULT NULL COMMENT '失败原因',
    create_dept           bigint(20)    DEFAULT NULL COMMENT '创建部门',
    create_by             bigint(20)    DEFAULT NULL COMMENT '创建者',
    create_time           datetime      DEFAULT NULL COMMENT '创建时间',
    update_by             bigint(20)    DEFAULT NULL COMMENT '更新者',
    update_time           datetime      DEFAULT NULL COMMENT '更新时间',
    remark                varchar(500)  DEFAULT NULL COMMENT '备注',
    del_flag              char(1)       DEFAULT '0' COMMENT '删除标志',
    PRIMARY KEY (message_id),
    KEY idx_dc_cook_message_order (tenant_id, related_order_id),
    KEY idx_dc_cook_message_status (tenant_id, channel, send_status, create_time),
    KEY idx_dc_cook_message_receiver (tenant_id, receiver_type, receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息记录表';

CREATE TABLE IF NOT EXISTS dc_cook_order (
    order_id                  bigint(20)    NOT NULL COMMENT '订单ID',
    tenant_id                 varchar(20)   DEFAULT '000000' COMMENT '租户ID',
    order_no                  varchar(40)   NOT NULL COMMENT '订单号（ODYYYYMMDDNNNN）',
    user_id                   bigint(20)    NOT NULL COMMENT '用户ID',
    chef_id                   bigint(20)    NOT NULL COMMENT '厨师ID',
    address_id                bigint(20)    DEFAULT NULL COMMENT '来源地址ID',
    contact_name              varchar(80)   DEFAULT NULL COMMENT '联系人姓名快照',
    contact_phone             varchar(20)   DEFAULT NULL COMMENT '联系人电话快照',
    service_area              varchar(100)  DEFAULT NULL COMMENT '服务区域快照',
    address_snapshot          varchar(1000) DEFAULT NULL COMMENT '完整地址快照',
    dish_snapshot             text          DEFAULT NULL COMMENT '菜品快照（JSON）',
    user_remark               varchar(1000) DEFAULT NULL COMMENT '用户备注（口味/忌口等）',
    service_start_time        datetime      NOT NULL COMMENT '服务开始时间',
    service_end_time          datetime      NOT NULL COMMENT '服务结束时间',
    status                    varchar(40)   NOT NULL COMMENT '订单状态',
    quote_amount              decimal(12,2) DEFAULT NULL COMMENT '报价金额',
    quote_remark              varchar(1000) DEFAULT NULL COMMENT '报价备注',
    quote_time                datetime      DEFAULT NULL COMMENT '报价时间',
    quote_update_count        int           DEFAULT 0 COMMENT '异议后报价更新次数',
    pay_deadline              datetime      DEFAULT NULL COMMENT '支付截止时间',
    pay_amount                decimal(12,2) DEFAULT NULL COMMENT '实付金额',
    pay_no                    varchar(80)   DEFAULT NULL COMMENT '支付流水号',
    pay_time                  datetime      DEFAULT NULL COMMENT '支付时间',
    objection_count           int           DEFAULT 0 COMMENT '异议次数',
    objection_reason          varchar(200)  DEFAULT NULL COMMENT '异议原因',
    objection_remark          varchar(1000) DEFAULT NULL COMMENT '异议备注',
    objection_time            datetime      DEFAULT NULL COMMENT '异议时间',
    objection_handle_time     datetime      DEFAULT NULL COMMENT '异议处理时间',
    cancel_type               varchar(40)   DEFAULT NULL COMMENT '取消类型（USER_UNPAID未付取消 USER_PAID已付取消 CHEF厨师取消）',
    cancel_reason             varchar(1000) DEFAULT NULL COMMENT '取消原因',
    cancel_time               datetime      DEFAULT NULL COMMENT '取消时间',
    refund_amount             decimal(12,2) DEFAULT NULL COMMENT '退款金额',
    refund_fee_amount         decimal(12,2) DEFAULT NULL COMMENT '取消手续费金额',
    refund_fee_rate           decimal(6,4)  DEFAULT NULL COMMENT '取消手续费比例',
    service_complete_time     datetime      DEFAULT NULL COMMENT '服务完成时间',
    service_complete_type     varchar(40)   DEFAULT NULL COMMENT '完成方式（CHEF厨师确认 SYSTEM系统自动）',
    confirm_time              datetime      DEFAULT NULL COMMENT '用户确认时间',
    complete_time             datetime      DEFAULT NULL COMMENT '最终完成时间',
    create_dept               bigint(20)    DEFAULT NULL COMMENT '创建部门',
    create_by                 bigint(20)    DEFAULT NULL COMMENT '创建者',
    create_time               datetime      DEFAULT NULL COMMENT '创建时间',
    update_by                 bigint(20)    DEFAULT NULL COMMENT '更新者',
    update_time               datetime      DEFAULT NULL COMMENT '更新时间',
    remark                    varchar(500)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_dc_cook_order_no (tenant_id, order_no),
    KEY idx_dc_cook_order_user_status (tenant_id, user_id, status, create_time),
    KEY idx_dc_cook_order_chef_status (tenant_id, chef_id, status, service_start_time),
    KEY idx_dc_cook_order_timeout (tenant_id, status, pay_deadline, service_end_time),
    KEY idx_dc_cook_order_month (tenant_id, chef_id, complete_time, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE IF NOT EXISTS dc_cook_review (
    review_id           bigint(20)    NOT NULL COMMENT '评价ID',
    tenant_id           varchar(20)   DEFAULT '000000' COMMENT '租户ID',
    order_id            bigint(20)    NOT NULL COMMENT '订单ID',
    order_no            varchar(40)   NOT NULL COMMENT '订单号',
    user_id             bigint(20)    NOT NULL COMMENT '用户ID',
    chef_id             bigint(20)    NOT NULL COMMENT '厨师ID',
    rating              decimal(3,2)  NOT NULL COMMENT '评分',
    content             varchar(1000) DEFAULT NULL COMMENT '评价内容',
    image_urls          varchar(1000) DEFAULT NULL COMMENT '图片地址',
    display_status      varchar(30)   DEFAULT 'SHOW' COMMENT '显示状态（SHOW显示 HIDE隐藏）',
    complaint_adjusted  char(1)       DEFAULT '0' COMMENT '投诉调整标志（1已调整）',
    review_time         datetime      DEFAULT NULL COMMENT '评价时间',
    create_dept         bigint(20)    DEFAULT NULL COMMENT '创建部门',
    create_by           bigint(20)    DEFAULT NULL COMMENT '创建者',
    create_time         datetime      DEFAULT NULL COMMENT '创建时间',
    update_by           bigint(20)    DEFAULT NULL COMMENT '更新者',
    update_time         datetime      DEFAULT NULL COMMENT '更新时间',
    remark              varchar(500)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (review_id),
    UNIQUE KEY uk_dc_cook_review_order (tenant_id, order_id),
    KEY idx_dc_cook_review_chef (tenant_id, chef_id, display_status, review_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

CREATE TABLE IF NOT EXISTS dc_cook_complaint (
    complaint_id    bigint(20)    NOT NULL COMMENT '投诉ID',
    tenant_id       varchar(20)   DEFAULT '000000' COMMENT '租户ID',
    order_id        bigint(20)    NOT NULL COMMENT '订单ID',
    order_no        varchar(40)   NOT NULL COMMENT '订单号',
    user_id         bigint(20)    NOT NULL COMMENT '用户ID',
    chef_id         bigint(20)    NOT NULL COMMENT '厨师ID',
    complaint_type  varchar(60)   DEFAULT NULL COMMENT '投诉类型',
    content         varchar(1000) NOT NULL COMMENT '投诉内容',
    image_urls      varchar(1000) DEFAULT NULL COMMENT '图片地址',
    status          varchar(30)   DEFAULT 'PENDING' COMMENT '状态（PENDING待处理 VALID成立 INVALID不成立）',
    handle_result   varchar(1000) DEFAULT NULL COMMENT '处理结果',
    handler_id      bigint(20)    DEFAULT NULL COMMENT '处理人ID',
    submit_time     datetime      DEFAULT NULL COMMENT '提交时间',
    handle_time     datetime      DEFAULT NULL COMMENT '处理时间',
    create_dept     bigint(20)    DEFAULT NULL COMMENT '创建部门',
    create_by       bigint(20)    DEFAULT NULL COMMENT '创建者',
    create_time     datetime      DEFAULT NULL COMMENT '创建时间',
    update_by       bigint(20)    DEFAULT NULL COMMENT '更新者',
    update_time     datetime      DEFAULT NULL COMMENT '更新时间',
    remark          varchar(500)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (complaint_id),
    UNIQUE KEY uk_dc_cook_complaint_order (tenant_id, order_id),
    KEY idx_dc_cook_complaint_status (tenant_id, status, submit_time),
    KEY idx_dc_cook_complaint_chef (tenant_id, chef_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉记录表';

CREATE TABLE IF NOT EXISTS dc_cook_settlement (
    settlement_id          bigint(20)    NOT NULL COMMENT '结算ID',
    tenant_id              varchar(20)   DEFAULT '000000' COMMENT '租户ID',
    chef_id                bigint(20)    NOT NULL COMMENT '厨师ID',
    settlement_month       varchar(7)    NOT NULL COMMENT '结算月份（yyyy-MM）',
    base_salary            decimal(12,2) DEFAULT 0.00 COMMENT '个人底薪',
    order_count            int           DEFAULT 0 COMMENT '已完成订单数',
    order_amount           decimal(12,2) DEFAULT 0.00 COMMENT '已完成订单金额',
    chef_rate              decimal(6,4)  DEFAULT 0.8000 COMMENT '厨师分成比例',
    chef_commission        decimal(12,2) DEFAULT 0.00 COMMENT '厨师佣金（扣除前）',
    platform_rate          decimal(6,4)  DEFAULT 0.2000 COMMENT '平台抽成比例',
    platform_commission    decimal(12,2) DEFAULT 0.00 COMMENT '平台佣金',
    violation_count        int           DEFAULT 0 COMMENT '月度违规次数',
    violation_deduction    decimal(12,2) DEFAULT 0.00 COMMENT '违规扣款',
    final_commission       decimal(12,2) DEFAULT 0.00 COMMENT '最终佣金（扣除后）',
    payable_amount         decimal(12,2) DEFAULT 0.00 COMMENT '应发金额',
    status                 varchar(30)   DEFAULT 'GENERATED' COMMENT '状态（GENERATED已生成 CONFIRMED已确认 PAID已发放）',
    manual_flag            char(1)       DEFAULT '0' COMMENT '人工调整标志（1已调整）',
    generated_time         datetime      DEFAULT NULL COMMENT '生成时间',
    create_dept            bigint(20)    DEFAULT NULL COMMENT '创建部门',
    create_by              bigint(20)    DEFAULT NULL COMMENT '创建者',
    create_time            datetime      DEFAULT NULL COMMENT '创建时间',
    update_by              bigint(20)    DEFAULT NULL COMMENT '更新者',
    update_time            datetime      DEFAULT NULL COMMENT '更新时间',
    remark                 varchar(500)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (settlement_id),
    UNIQUE KEY uk_dc_cook_settlement_month (tenant_id, chef_id, settlement_month),
    KEY idx_dc_cook_settlement_status (tenant_id, settlement_month, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度结算表';

-- ----------------------------
-- 小程序认证客户端
-- ----------------------------

-- 确保 App 客户端支持账号密码登录/注册
UPDATE sys_client
SET grant_type = CASE
        WHEN grant_type IS NULL OR grant_type = '' THEN 'password'
        WHEN FIND_IN_SET('password', grant_type) = 0 THEN CONCAT(grant_type, ',password')
        ELSE grant_type
    END,
    status = '0'
WHERE client_id = '428a8310cd442757ae699df5d894f051';

DELETE FROM sys_client WHERE id = 300003 OR client_id = 'be7052a7e4f802c20df10a8d131adb12' OR client_key = 'xcx';

INSERT INTO sys_client
(id, client_id, client_key, client_secret, grant_type, device_type, active_timeout, timeout, status, del_flag, create_dept, create_by, create_time, update_by, update_time)
VALUES
(300003, 'be7052a7e4f802c20df10a8d131adb12', 'xcx', 'xcx123', 'xcx', 'xcx', 1800, 604800, '0', '0', 103, 1, NOW(), 1, NOW());

-- ----------------------------
-- 默认业务配置
-- ----------------------------

DELETE FROM dc_cook_config WHERE config_key IN (
    'cooking.response.timeout.minutes',
    'cooking.pay.timeout.minutes',
    'cooking.cancel.fee.rate',
    'cooking.platform.commission.rate',
    'cooking.service.duration.hours',
    'cooking.reserve.min.advance.minutes',
    'cooking.reserve.future.days',
    'cooking.confirm.timeout.hours',
    'cooking.sms.template.quote',
    'cooking.sms.template.service.reminder',
    'cooking.commission.announcement'
);

INSERT INTO dc_cook_config
(config_id, tenant_id, config_name, config_key, config_value, value_type, config_type, rule_flag, effective_time, publish_status, change_reason, announcement_content, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
(300001, '000000', '厨师响应超时时间(分钟)', 'cooking.response.timeout.minutes', '30', 'NUMBER', 'ORDER', '1', NOW(), 'PUBLISHED', '初始默认值', NULL, 103, 1, NOW(), NULL, NULL, '厨师必须在超时前响应订单'),
(300002, '000000', '用户支付超时时间(分钟)', 'cooking.pay.timeout.minutes', '30', 'NUMBER', 'ORDER', '1', NOW(), 'PUBLISHED', '初始默认值', NULL, 103, 1, NOW(), NULL, NULL, '用户必须在超时前完成支付'),
(300003, '000000', '已付款取消手续费比例', 'cooking.cancel.fee.rate', '0.10', 'PERCENT', 'REFUND', '1', NOW(), 'PUBLISHED', '初始默认值', NULL, 103, 1, NOW(), NULL, NULL, '付款10分钟后取消收取手续费比例'),
(300004, '000000', '平台抽成比例', 'cooking.platform.commission.rate', '0.20', 'PERCENT', 'SETTLEMENT', '1', NOW(), 'PUBLISHED', '初始默认值', '平台抽成比例为20%，调整将提前3天公告通知。', 103, 1, NOW(), NULL, NULL, '平台抽成比例'),
(300005, '000000', '服务时长(小时)', 'cooking.service.duration.hours', '3', 'NUMBER', 'RESERVE', '1', NOW(), 'PUBLISHED', '初始默认值', NULL, 103, 1, NOW(), NULL, NULL, '每次预约锁定3小时'),
(300006, '000000', '最少提前预约时间(分钟)', 'cooking.reserve.min.advance.minutes', '60', 'NUMBER', 'RESERVE', '1', NOW(), 'PUBLISHED', '初始默认值', NULL, 103, 1, NOW(), NULL, NULL, '用户不能预约即时或过去的时间'),
(300007, '000000', '可预约未来天数', 'cooking.reserve.future.days', '3', 'NUMBER', 'RESERVE', '1', NOW(), 'PUBLISHED', '初始默认值', NULL, 103, 1, NOW(), NULL, NULL, '用户可预约未来3天内的时间'),
(300008, '000000', '用户确认超时时间(小时)', 'cooking.confirm.timeout.hours', '24', 'NUMBER', 'ORDER', '1', NOW(), 'PUBLISHED', '初始默认值', NULL, 103, 1, NOW(), NULL, NULL, '服务完成后用户未确认则自动完成'),
(300009, '000000', '报价短信模板', 'cooking.sms.template.quote', '您的上门做饭服务报价已生成，请及时查看。', 'STRING', 'MESSAGE', '0', NOW(), 'PUBLISHED', '初始默认值', NULL, 103, 1, NOW(), NULL, NULL, '短信模板占位'),
(300010, '000000', '服务提醒短信模板', 'cooking.sms.template.service.reminder', '您的上门做饭服务将在30分钟后开始，请做好准备。', 'STRING', 'MESSAGE', '0', NOW(), 'PUBLISHED', '初始默认值', NULL, 103, 1, NOW(), NULL, NULL, '短信模板占位'),
(300011, '000000', '抽成公告', 'cooking.commission.announcement', '平台抽成比例为20%。', 'STRING', 'ANNOUNCEMENT', '0', NOW(), 'PUBLISHED', '初始默认值', '平台抽成比例为20%。', 103, 1, NOW(), NULL, NULL, '工作台公告');

-- ----------------------------
-- 种子数据（本地测试用）
-- ----------------------------

REPLACE INTO dc_cook_area
(area_id, tenant_id, area_code, area_name, parent_code, area_level, status, sort, create_dept, create_by, create_time, remark, del_flag)
VALUES
(300001, '000000', 'DEMO-SH-CENTRAL', '示例中心城区', NULL, 'DISTRICT', '0', 1, 103, 1, NOW(), '示例服务区域', '0');

REPLACE INTO dc_cook_dish
(dish_id, tenant_id, dish_name, category, cuisine, image_url, description, status, sort, create_dept, create_by, create_time, remark, del_flag)
VALUES
(300001, '000000', '红烧肉', '荤菜', '家常菜', NULL, '经典家常菜，肥而不腻', '0', 1, 103, 1, NOW(), '示例菜品', '0'),
(300002, '000000', '时蔬小炒', '素菜', '家常菜', NULL, '新鲜时令蔬菜', '0', 2, 103, 1, NOW(), '示例菜品', '0'),
(300003, '000000', '清蒸鲈鱼', '海鲜', '粤菜', NULL, '鲜嫩清蒸，保留原味', '0', 3, 103, 1, NOW(), '示例菜品', '0'),
(300004, '000000', '麻婆豆腐', '豆制品', '川菜', NULL, '麻辣鲜香，下饭佳品', '0', 4, 103, 1, NOW(), '示例菜品', '0'),
(300005, '000000', '糖醋排骨', '荤菜', '家常菜', NULL, '酸甜可口，老少皆宜', '0', 5, 103, 1, NOW(), '示例菜品', '0'),
(300006, '000000', '西红柿炒蛋', '素菜', '家常菜', NULL, '家常经典，简单美味', '0', 6, 103, 1, NOW(), '示例菜品', '0'),
(300007, '000000', '宫保鸡丁', '荤菜', '川菜', NULL, '花生鸡丁，香辣开胃', '0', 7, 103, 1, NOW(), '示例菜品', '0'),
(300008, '000000', '酸菜鱼', '海鲜', '川菜', NULL, '酸辣鲜美，鱼肉嫩滑', '0', 8, 103, 1, NOW(), '示例菜品', '0');

-- ----------------------------
-- 后台菜单及权限
-- ----------------------------

DELETE FROM sys_menu WHERE menu_id BETWEEN 300000 AND 300099;

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
(300000, '上门做饭', 0, 6, 'cooking', NULL, '', 1, 0, 'M', '0', '0', '', 'shopping', 103, 1, NOW(), NULL, NULL, '上门做饭目录'),
(300001, '厨师管理', 300000, 1, 'chef', 'cooking/chef/index', '', 1, 0, 'C', '0', '0', 'cooking:chef:list', 'people', 103, 1, NOW(), NULL, NULL, '厨师管理'),
(300002, '订单管理', 300000, 2, 'order', 'cooking/order/index', '', 1, 0, 'C', '0', '0', 'cooking:order:list', 'list', 103, 1, NOW(), NULL, NULL, '订单管理'),
(300003, '菜品库', 300000, 3, 'dish', 'cooking/dish/index', '', 1, 0, 'C', '0', '0', 'cooking:dish:list', 'category', 103, 1, NOW(), NULL, NULL, '菜品库'),
(300004, '投诉记录', 300000, 4, 'complaint', 'cooking/complaint/index', '', 1, 0, 'C', '0', '0', 'cooking:complaint:list', 'message', 103, 1, NOW(), NULL, NULL, '投诉记录'),
(300005, '结算管理', 300000, 5, 'settlement', 'cooking/settlement/index', '', 1, 0, 'C', '0', '0', 'cooking:settlement:list', 'money', 103, 1, NOW(), NULL, NULL, '月度结算'),
(300006, '业务配置', 300000, 6, 'config', 'cooking/config/index', '', 1, 0, 'C', '0', '0', 'cooking:config:list', 'edit', 103, 1, NOW(), NULL, NULL, '业务配置'),
(300007, '地址管理', 300000, 7, 'address', 'cooking/address/index', '', 1, 0, 'C', '0', '0', 'cooking:address:list', 'form', 103, 1, NOW(), NULL, NULL, '地址管理'),
(300008, '服务区域', 300000, 8, 'area', 'cooking/area/index', '', 1, 0, 'C', '0', '0', 'cooking:area:list', 'tree', 103, 1, NOW(), NULL, NULL, '服务区域'),
(300009, '消息记录', 300000, 9, 'message', 'cooking/message/index', '', 1, 0, 'C', '0', '0', 'cooking:message:list', 'message', 103, 1, NOW(), NULL, NULL, '消息记录'),
(300010, '评价管理', 300000, 10, 'review', 'cooking/review/index', '', 1, 0, 'C', '0', '0', 'cooking:review:list', 'star', 103, 1, NOW(), NULL, NULL, '评价管理');

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
(300020, '厨师新增', 300001, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:chef:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(300021, '厨师编辑', 300001, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:chef:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300022, '厨师删除', 300001, 3, '', '', '', 1, 0, 'F', '0', '0', 'cooking:chef:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(300023, '厨师审核', 300001, 4, '', '', '', 1, 0, 'F', '0', '0', 'cooking:chef:audit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300024, '订单编辑', 300002, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:order:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300025, '退款重试', 300002, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:order:refundRetry', '#', 103, 1, NOW(), NULL, NULL, ''),
(300026, '菜品新增', 300003, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:dish:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(300027, '菜品编辑', 300003, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:dish:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300028, '菜品删除', 300003, 3, '', '', '', 1, 0, 'F', '0', '0', 'cooking:dish:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(300029, '投诉处理', 300004, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:complaint:handle', '#', 103, 1, NOW(), NULL, NULL, ''),
(300030, '结算确认', 300005, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:settlement:confirm', '#', 103, 1, NOW(), NULL, NULL, ''),
(300031, '配置编辑', 300006, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:config:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300032, '区域编辑', 300008, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:area:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300033, '消息查看', 300009, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:message:query', '#', 103, 1, NOW(), NULL, NULL, ''),
(300034, '评价编辑', 300010, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:review:edit', '#', 103, 1, NOW(), NULL, NULL, '');

-- ----------------------------
-- 隐藏非必要顶层菜单（保留：首页、上门做饭、系统管理）
-- ----------------------------
UPDATE sys_menu SET visible = '1' WHERE menu_id IN (2, 3, 4, 5, 6) AND parent_id = 0;

-- Disable removed RuoYi template menus so dynamic routes do not load deleted frontend components.
UPDATE sys_menu SET status = '1', visible = '1' WHERE menu_id IN (
    3, 5, 6,
    115, 116, 121, 122,
    1055, 1056, 1057, 1058, 1059, 1060,
    1500, 1501, 1502, 1503, 1504, 1505, 1506, 1507, 1508, 1509, 1510, 1511,
    1606, 1607, 1608, 1609, 1610, 1611, 1612, 1613, 1614, 1615,
    11638, 11639, 11640, 11641, 11642, 11643, 11701
);

-- 隐藏系统管理下非必要子菜单（保留：用户管理100、角色管理101、参数设置106、通知公告107、文件管理118）
UPDATE sys_menu SET visible = '1' WHERE menu_id IN (102, 103, 104, 105, 108, 123) AND parent_id = 1;

-- 开启账号自助注册，供小程序用户注册使用
UPDATE sys_config SET config_value = 'true' WHERE config_key = 'sys.account.registerUser';

SET FOREIGN_KEY_CHECKS = 1;
