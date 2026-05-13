-- 上门做饭 MVP 数据库脚本(MySQL)
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
    gender                  char(1)       DEFAULT NULL COMMENT '性别（0男 1女 2未知）',
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
    audit_status            varchar(30)   DEFAULT '0' COMMENT '审核状态（0待审核1已通过 2已拒绝）',
    audit_reason            varchar(500)  DEFAULT NULL COMMENT '审核原因',
    audit_by                bigint(20)    DEFAULT NULL COMMENT '审核人',
    audit_time              datetime      DEFAULT NULL COMMENT '审核时间',
    chef_status             varchar(30)   DEFAULT '0' COMMENT '厨师状态（0正常 1暂停 2禁用 3离职）',
    resign_reason           varchar(500)  DEFAULT NULL COMMENT '离职原因',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

CREATE TABLE IF NOT EXISTS dc_cook_config (
    config_id             bigint(20)    NOT NULL COMMENT '配置ID',
    tenant_id             varchar(20)   DEFAULT '000000' COMMENT '租户ID',
    config_name           varchar(100)  NOT NULL COMMENT '配置名称',
    config_key            varchar(100)  NOT NULL COMMENT '配置键',
    config_value          varchar(1000) NOT NULL COMMENT '配置键',
    value_type            varchar(30)   DEFAULT 'STRING' COMMENT '值类型（STRING字符串NUMBER数字JSON对象 PERCENT百分比）',
    config_type           varchar(30)   DEFAULT 'BUSINESS' COMMENT '配置分类（BUSINESS业务 ORDER订单 REFUND退款 SETTLEMENT结算 RESERVE预约 MESSAGE消息 ANNOUNCEMENT公告）',
    rule_flag             char(1)       DEFAULT '1' COMMENT '规则配置（1是）是）',
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
    channel               varchar(30)   NOT NULL COMMENT '渠道（IN_APP站内信 WECHAT微信 SMS短信）',
    receiver_type         varchar(30)   DEFAULT NULL COMMENT '接收者类型（USER用户 CHEF厨师 ADMIN管理员）',
    receiver_id           bigint(20)    DEFAULT NULL COMMENT '接收者ID',
    receiver_mobile_mask  varchar(40)   DEFAULT NULL COMMENT '手机号掩码',
    receiver_openid_mask  varchar(80)   DEFAULT NULL COMMENT 'OpenID掩码',
    related_order_id      bigint(20)    DEFAULT NULL COMMENT '关联订单ID',
    related_order_no      varchar(40)   DEFAULT NULL COMMENT '关联订单号',
    related_biz_type      varchar(60)   DEFAULT NULL COMMENT '关联业务类型',
    related_biz_id        bigint(20)    DEFAULT NULL COMMENT '关联业务ID',
    content_summary       varchar(1000) DEFAULT NULL COMMENT '内容摘要',
    send_status           varchar(30)   DEFAULT '0' COMMENT '发送状态（0待发送 1已发送 2失败 3发送中，兼容旧值PENDING/SENT/FAILED/SENDING）',
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
    user_remark               varchar(1000) DEFAULT NULL COMMENT '用户备注（口味忌口等）',
    service_start_time        datetime      NOT NULL COMMENT '服务开始时间',
    service_end_time          datetime      NOT NULL COMMENT '服务结束时间',
    service_started_flag      char(1)       DEFAULT '0' COMMENT 'service started flag (0=no 1=yes)',
    service_started_time      datetime      DEFAULT NULL COMMENT 'actual service started time',
    status                    varchar(40)   NOT NULL DEFAULT '0' COMMENT '订单状态（0待响应 1待支付 2报价异议中 3待服务 4待确认 5已完成 6已拒单关闭 7响应超时关闭 8异议超时关闭 9支付超时关闭 10已取消 11退款中 12已退款 13退款失败）',
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

UPDATE dc_cook_order
SET service_started_flag = CASE
    WHEN service_started_time IS NULL THEN '0'
    ELSE '1'
END
WHERE service_started_flag IS NULL
   OR service_started_flag = '';

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
    complaint_adjusted  char(1)       DEFAULT '0' COMMENT '投诉调整标志（1是）已调整）',
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
    status          varchar(30)   DEFAULT '0' COMMENT '状态（0待处理 1成立 2不成立，兼容旧值PENDING/ESTABLISHED/REJECTED）',
    handle_result   varchar(1000) DEFAULT NULL COMMENT '处理说明',
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
    del_flag     char(1)       DEFAULT '0' COMMENT '删除标志',
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
    status       varchar(20)   DEFAULT '0' COMMENT '状态（0待处理 1已回复 2已关闭，兼容旧值PENDING/REPLIED/CLOSED）',
    handler_id   bigint(20)    DEFAULT NULL COMMENT '处理人ID',
    handle_time  datetime      DEFAULT NULL COMMENT '处理时间',
    close_time   datetime      DEFAULT NULL COMMENT '关闭时间',
    create_dept  bigint(20)    DEFAULT NULL COMMENT '创建部门',
    create_by    bigint(20)    DEFAULT NULL COMMENT '创建者',
    create_time  datetime      DEFAULT NULL COMMENT '创建时间',
    update_by    bigint(20)    DEFAULT NULL COMMENT '更新者',
    update_time  datetime     DEFAULT NULL COMMENT '更新时间',
    remark       varchar(500)  DEFAULT NULL COMMENT '备注',
    del_flag     char(1)       DEFAULT '0' COMMENT '删除标志',
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
(300102, '000000', '订单问题', '如何取消订单', '未付款订单可在订单详情取消；已付款订单需满足取消规则后提交取消申请。', '取消订单,退款,不想要', 2, '0', 103, 1, NOW(), '首版FAQ'),
(300103, '000000', '支付问题', '支付超时怎么办', '支付超时后订单会自动关闭，可重新发起预约。', '支付超时,付款超时,无法支付', 3, '0', 103, 1, NOW(), '首版FAQ'),
(300104, '000000', '服务问题', '服务厨师什么时候上门', '服务厨师会按预约时间上门服务，请保持联系电话畅通。', '上门时间,什么时候来,服务厨师', 4, '0', 103, 1, NOW(), '首版FAQ'),
(300105, '000000', '工单问题', '机器人无法回答怎么办', '机器人无法回答的问题可以提交工单，后台处理后会展示处理回复。', '无法回答,工单,提交问题', 5, '0', 103, 1, NOW(), '首版FAQ');

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
    status                 varchar(30)   DEFAULT '0' COMMENT '结算状态（0已生成/1复核中/2已确认/3已发放，兼容旧值GENERATED/REVIEWING/CONFIRMED/PAID）',
    review_reason_type     varchar(40)   DEFAULT NULL COMMENT '复核原因类型',
    review_remark          varchar(500)  DEFAULT NULL COMMENT '复核申请说明',
    review_result          varchar(20)   DEFAULT NULL COMMENT '复核处理结果（KEEP/REGENERATE）',
    review_reply           varchar(500)  DEFAULT NULL COMMENT '复核处理说明',
    review_apply_time      datetime      DEFAULT NULL COMMENT '复核申请时间',
    review_handle_time     datetime      DEFAULT NULL COMMENT '复核处理时间',
    manual_flag            char(1)       DEFAULT '0' COMMENT '人工调整标志',
    generated_time         datetime      DEFAULT NULL COMMENT '生成时间',
    confirm_time           datetime      DEFAULT NULL COMMENT '确认时间',
    pay_time               datetime      DEFAULT NULL COMMENT '发放时间',
    pay_remark             varchar(500)  DEFAULT NULL COMMENT '打款说明',
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

UPDATE dc_cook_chef
SET audit_status = CASE UPPER(audit_status)
    WHEN 'PENDING' THEN '0'
    WHEN 'APPROVED' THEN '1'
    WHEN 'REJECTED' THEN '2'
    ELSE audit_status
END
WHERE audit_status IN ('PENDING', 'APPROVED', 'REJECTED');

UPDATE dc_cook_chef
SET chef_status = CASE UPPER(chef_status)
    WHEN 'APPROVED' THEN '0'
    WHEN 'PENDING_REVIEW' THEN '0'
    WHEN 'NORMAL' THEN '0'
    WHEN 'AVAILABLE' THEN '0'
    WHEN 'PAUSED' THEN '1'
    WHEN 'DISABLED' THEN '2'
    WHEN 'RESIGNED' THEN '3'
    ELSE chef_status
END
WHERE chef_status IN ('APPROVED', 'PENDING_REVIEW', 'NORMAL', 'AVAILABLE', 'PAUSED', 'DISABLED', 'RESIGNED');

UPDATE dc_cook_order
SET status = CASE UPPER(status)
    WHEN 'WAITING_RESPONSE' THEN '0'
    WHEN 'WAITING_PAY' THEN '1'
    WHEN 'PRICE_OBJECTION' THEN '2'
    WHEN 'WAITING_SERVICE' THEN '3'
    WHEN 'WAITING_CONFIRM' THEN '4'
    WHEN 'COMPLETED' THEN '5'
    WHEN 'REJECTED_CLOSED' THEN '6'
    WHEN 'RESPONSE_TIMEOUT_CLOSED' THEN '7'
    WHEN 'OBJECTION_TIMEOUT_CLOSED' THEN '8'
    WHEN 'PAY_TIMEOUT_CLOSED' THEN '9'
    WHEN 'CANCELED' THEN '10'
    WHEN 'REFUNDING' THEN '11'
    WHEN 'REFUNDED' THEN '12'
    WHEN 'REFUND_FAILED' THEN '13'
    ELSE status
END
WHERE status IN (
    'WAITING_RESPONSE', 'WAITING_PAY', 'PRICE_OBJECTION', 'WAITING_SERVICE', 'WAITING_CONFIRM',
    'COMPLETED', 'REJECTED_CLOSED', 'RESPONSE_TIMEOUT_CLOSED', 'OBJECTION_TIMEOUT_CLOSED',
    'PAY_TIMEOUT_CLOSED', 'CANCELED', 'REFUNDING', 'REFUNDED', 'REFUND_FAILED'
);

UPDATE dc_cook_message
SET send_status = CASE UPPER(send_status)
    WHEN 'PENDING' THEN '0'
    WHEN 'SENT' THEN '1'
    WHEN 'SUCCESS' THEN '1'
    WHEN 'FAILED' THEN '2'
    WHEN 'SENDING' THEN '3'
    ELSE send_status
END
WHERE send_status IN ('PENDING', 'SENT', 'SUCCESS', 'FAILED', 'SENDING');

UPDATE dc_cook_complaint
SET status = CASE UPPER(status)
    WHEN 'PENDING' THEN '0'
    WHEN 'ESTABLISHED' THEN '1'
    WHEN 'REJECTED' THEN '2'
    ELSE status
END
WHERE status IN ('PENDING', 'ESTABLISHED', 'REJECTED');

UPDATE dc_cook_settlement
SET status = CASE UPPER(status)
    WHEN 'GENERATED' THEN '0'
    WHEN 'MANUAL' THEN '0'
    WHEN 'REVIEWING' THEN '1'
    WHEN 'CONFIRMED' THEN '2'
    WHEN 'PAID' THEN '3'
    WHEN 'PAID_OFFLINE' THEN '3'
    ELSE status
END
WHERE status IN ('GENERATED', 'MANUAL', 'REVIEWING', 'CONFIRMED', 'PAID', 'PAID_OFFLINE');

UPDATE dc_cook_support_ticket
SET status = CASE UPPER(status)
    WHEN 'PENDING' THEN '0'
    WHEN 'REPLIED' THEN '1'
    WHEN 'CLOSED' THEN '2'
    ELSE status
END
WHERE status IN ('PENDING', 'REPLIED', 'CLOSED');

-- ----------------------------
-- 小程序认证客户端
-- ----------------------------

-- 确保 App 客户端支持账号密码登录注册
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
    'dc.cooking.platform.rate',
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
(300004, '000000', '平台抽成比例', 'dc.cooking.platform.rate', '0.20', 'PERCENT', 'SETTLEMENT', '1', NOW(), 'PUBLISHED', '初始默认值', '平台抽成比例为20%，调整将提前3天公告通知。', 103, 1, NOW(), NULL, NULL, '平台抽成比例'),
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
(300003, '菜品管理', 300000, 3, 'dish', 'cooking/dish/index', '', 1, 0, 'C', '0', '0', 'cooking:dish:list', 'category', 103, 1, NOW(), NULL, NULL, '菜品管理'),
(300004, '投诉记录', 300000, 4, 'complaint', 'cooking/complaint/index', '', 1, 0, 'C', '0', '0', 'cooking:complaint:list', 'message', 103, 1, NOW(), NULL, NULL, '投诉记录'),
(300005, '结算管理', 300000, 5, 'settlement', 'cooking/settlement/index', '', 1, 0, 'C', '0', '0', 'cooking:settlement:list', 'money', 103, 1, NOW(), NULL, NULL, '月度结算'),
(300006, '业务配置', 300000, 6, 'config', 'cooking/config/index', '', 1, 0, 'C', '0', '0', 'cooking:config:list', 'edit', 103, 1, NOW(), NULL, NULL, '业务配置'),
(300007, '地址管理', 300000, 7, 'address', 'cooking/address/index', '', 1, 0, 'C', '0', '0', 'cooking:address:list', 'form', 103, 1, NOW(), NULL, NULL, '地址管理'),
(300008, '服务区域', 300000, 8, 'area', 'cooking/area/index', '', 1, 0, 'C', '1', '0', 'cooking:area:list', 'tree', 103, 1, NOW(), NULL, NULL, '服务区域'),
(300009, '消息记录', 300000, 9, 'message', 'cooking/message/index', '', 1, 0, 'C', '0', '0', 'cooking:message:list', 'message', 103, 1, NOW(), NULL, NULL, '消息记录'),
(300010, '评价管理', 300000, 10, 'review', 'cooking/review/index', '', 1, 0, 'C', '0', '0', 'cooking:review:list', 'star', 103, 1, NOW(), NULL, NULL, '评价管理'),
(300011, 'FAQ管理', 300000, 11, 'faq', 'cooking/faq/index', '', 1, 0, 'C', '0', '0', 'cooking:supportFaq:list', 'question', 103, 1, NOW(), NULL, NULL, 'FAQ管理'),
(300012, '工单管理', 300000, 12, 'ticket', 'cooking/ticket/index', '', 1, 0, 'C', '0', '0', 'cooking:supportTicket:list', 'form', 103, 1, NOW(), NULL, NULL, '工单管理');

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
(300032, '区域编辑', 300008, 1, '', '', '', 1, 0, 'F', '1', '0', 'cooking:area:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300033, '消息查看', 300009, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:message:query', '#', 103, 1, NOW(), NULL, NULL, ''),
(300034, '评价编辑', 300010, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:review:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300040, 'FAQ新增', 300011, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:supportFaq:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(300041, 'FAQ编辑', 300011, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:supportFaq:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300042, 'FAQ删除', 300011, 3, '', '', '', 1, 0, 'F', '0', '0', 'cooking:supportFaq:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(300043, '工单处理', 300012, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:supportTicket:handle', '#', 103, 1, NOW(), NULL, NULL, ''),
(300044, '工单关闭', 300012, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:supportTicket:close', '#', 103, 1, NOW(), NULL, NULL, '');

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

-- 回填历史服务厨师手机号到用户表，保证后台用户管理可回显手机号。
UPDATE sys_user u
JOIN dc_cook_chef c
    ON c.tenant_id COLLATE utf8mb4_general_ci = u.tenant_id COLLATE utf8mb4_general_ci
    AND c.user_id = u.user_id
    AND c.del_flag = '0'
LEFT JOIN sys_user ux
    ON ux.tenant_id COLLATE utf8mb4_general_ci = u.tenant_id COLLATE utf8mb4_general_ci
    AND ux.user_id <> u.user_id
    AND ux.del_flag = '0'
    AND ux.phonenumber COLLATE utf8mb4_general_ci = c.mobile COLLATE utf8mb4_general_ci
SET u.phonenumber = c.mobile
WHERE u.del_flag = '0'
  AND (u.phonenumber IS NULL OR u.phonenumber = '')
  AND c.mobile IS NOT NULL
  AND c.mobile <> ''
  AND ux.user_id IS NULL;

-- ----------------------------
-- SnailJob 月度结算自动生成任务
-- 每月 1 日 02:00 自动生成上月结算，生成后需运营人员确认、平台打款
-- ----------------------------
INSERT INTO sj_job
(namespace_id, biz_id, group_name, job_name, args_str, args_type, next_trigger_at, job_status, task_type, route_key, executor_type, executor_info, trigger_type, trigger_interval, block_strategy, executor_timeout, max_retry_times, parallel_num, retry_interval, bucket_index, resident, notify_ids, owner_id, labels, description, ext_attrs, deleted, create_dt, update_dt)
VALUES
('dev', 'cooking-monthly-settlement', 'ruoyi_group', '上门做饭月度结算自动生成', NULL, 1, UNIX_TIMESTAMP() * 1000, 1, 1, 4, 1, 'cookingMonthlySettlementTask', 1, '0 0 2 1 * ?', 1, 600, 3, 1, 60, 0, 0, '', 1, 'cooking,settlement', '每月1日02:00自动生成上月结算，生成后需运营人员确认后平台打款', '', 0, NOW(), NOW()),
('prod', 'cooking-monthly-settlement', 'ruoyi_group', '上门做饭月度结算自动生成', NULL, 1, UNIX_TIMESTAMP() * 1000, 1, 1, 4, 1, 'cookingMonthlySettlementTask', 1, '0 0 2 1 * ?', 1, 600, 3, 1, 60, 0, 0, '', 1, 'cooking,settlement', '每月1日02:00自动生成上月结算，生成后需运营人员确认后平台打款', '', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    group_name = VALUES(group_name),
    job_name = VALUES(job_name),
    job_status = VALUES(job_status),
    executor_type = VALUES(executor_type),
    executor_info = VALUES(executor_info),
    trigger_type = VALUES(trigger_type),
    trigger_interval = VALUES(trigger_interval),
    block_strategy = VALUES(block_strategy),
    executor_timeout = VALUES(executor_timeout),
    max_retry_times = VALUES(max_retry_times),
    retry_interval = VALUES(retry_interval),
    labels = VALUES(labels),
    description = VALUES(description),
    deleted = 0,
    update_dt = NOW();

SET FOREIGN_KEY_CHECKS = 1;
