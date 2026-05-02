-- Demand Cooking MVP database script for MySQL.
-- Execute after the base RuoYi-Vue-Plus scripts: ry_vue_5.X.sql, ry_job.sql, ry_workflow.sql.
-- This file is intentionally isolated from upstream RuoYi scripts.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Core business tables
-- ----------------------------

CREATE TABLE IF NOT EXISTS dc_cook_area (
    area_id          bigint(20)   NOT NULL COMMENT 'service area id',
    tenant_id        varchar(20)  DEFAULT '000000' COMMENT 'tenant id',
    area_code        varchar(64)  NOT NULL COMMENT 'area code',
    area_name        varchar(100) NOT NULL COMMENT 'area name',
    parent_code      varchar(64)  DEFAULT NULL COMMENT 'parent area code',
    area_level       varchar(20)  DEFAULT 'CITY' COMMENT 'area level',
    status           char(1)      DEFAULT '0' COMMENT '0 enabled, 1 disabled',
    sort             int          DEFAULT 0 COMMENT 'sort order',
    create_dept      bigint(20)   DEFAULT NULL COMMENT 'create dept',
    create_by        bigint(20)   DEFAULT NULL COMMENT 'creator',
    create_time      datetime     DEFAULT NULL COMMENT 'create time',
    update_by        bigint(20)   DEFAULT NULL COMMENT 'updater',
    update_time      datetime     DEFAULT NULL COMMENT 'update time',
    remark           varchar(500) DEFAULT NULL COMMENT 'remark',
    del_flag         char(1)      DEFAULT '0' COMMENT 'delete flag',
    PRIMARY KEY (area_id),
    UNIQUE KEY uk_dc_cook_area_code (tenant_id, area_code),
    KEY idx_dc_cook_area_status (tenant_id, status, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand cooking service area';

CREATE TABLE IF NOT EXISTS dc_cook_chef (
    chef_id                 bigint(20)    NOT NULL COMMENT 'chef id',
    tenant_id               varchar(20)   DEFAULT '000000' COMMENT 'tenant id',
    user_id                 bigint(20)    DEFAULT NULL COMMENT 'linked system user id',
    chef_name               varchar(80)   NOT NULL COMMENT 'chef name',
    gender                  char(1)       DEFAULT NULL COMMENT 'gender',
    age                     int           DEFAULT NULL COMMENT 'age',
    mobile                  varchar(20)   NOT NULL COMMENT 'chef mobile',
    avatar_url              varchar(500)  DEFAULT NULL COMMENT 'avatar url',
    area_id                 bigint(20)    DEFAULT NULL COMMENT 'main service area id',
    area_name               varchar(100)  DEFAULT NULL COMMENT 'main service area name',
    skill_tags              varchar(500)  DEFAULT NULL COMMENT 'specialty tags',
    intro                   varchar(1000) DEFAULT NULL COMMENT 'profile intro',
    health_cert_no          varchar(100)  DEFAULT NULL COMMENT 'health certificate no',
    health_cert_image_url   varchar(500)  DEFAULT NULL COMMENT 'health certificate image url',
    health_cert_expire_date datetime      DEFAULT NULL COMMENT 'health certificate expire date',
    base_salary             decimal(12,2) DEFAULT 0.00 COMMENT 'personal base salary',
    rating                  decimal(3,2)  DEFAULT 5.00 COMMENT 'average rating',
    completed_orders        bigint(20)    DEFAULT 0 COMMENT 'completed order count',
    recommend_flag          char(1)       DEFAULT '0' COMMENT '1 recommended',
    audit_status            varchar(30)   DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED',
    audit_reason            varchar(500)  DEFAULT NULL COMMENT 'audit reason',
    chef_status             varchar(30)   DEFAULT 'PENDING_REVIEW' COMMENT 'APPROVED, PAUSED, DISABLED, RESIGNED',
    create_dept             bigint(20)    DEFAULT NULL COMMENT 'create dept',
    create_by               bigint(20)    DEFAULT NULL COMMENT 'creator',
    create_time             datetime      DEFAULT NULL COMMENT 'create time',
    update_by               bigint(20)    DEFAULT NULL COMMENT 'updater',
    update_time             datetime      DEFAULT NULL COMMENT 'update time',
    remark                  varchar(500)  DEFAULT NULL COMMENT 'remark',
    del_flag                char(1)       DEFAULT '0' COMMENT 'delete flag',
    PRIMARY KEY (chef_id),
    UNIQUE KEY uk_dc_cook_chef_mobile (tenant_id, mobile, del_flag),
    KEY idx_dc_cook_chef_user (tenant_id, user_id),
    KEY idx_dc_cook_chef_area_status (tenant_id, area_id, audit_status, chef_status, del_flag),
    KEY idx_dc_cook_chef_sort (tenant_id, recommend_flag, completed_orders, rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand cooking chef';

CREATE TABLE IF NOT EXISTS dc_cook_address (
    address_id        bigint(20)    NOT NULL COMMENT 'address id',
    tenant_id         varchar(20)   DEFAULT '000000' COMMENT 'tenant id',
    user_id           bigint(20)    NOT NULL COMMENT 'user id',
    contact_name      varchar(80)   NOT NULL COMMENT 'contact name',
    contact_phone     varchar(20)   NOT NULL COMMENT 'contact phone',
    area_code         varchar(64)   DEFAULT NULL COMMENT 'area code',
    area_name         varchar(100)  DEFAULT NULL COMMENT 'area name',
    detail_address    varchar(500)  NOT NULL COMMENT 'detail address',
    house_number      varchar(100)  DEFAULT NULL COMMENT 'doorplate',
    longitude         decimal(12,6) DEFAULT NULL COMMENT 'longitude',
    latitude          decimal(12,6) DEFAULT NULL COMMENT 'latitude',
    default_flag      char(1)       DEFAULT '0' COMMENT '1 default address',
    source_address_id bigint(20)    DEFAULT NULL COMMENT 'source address for snapshot',
    snapshot_type     varchar(20)   DEFAULT 'NORMAL' COMMENT 'NORMAL, ORDER',
    snapshot_time     datetime      DEFAULT NULL COMMENT 'snapshot time',
    create_dept       bigint(20)    DEFAULT NULL COMMENT 'create dept',
    create_by         bigint(20)    DEFAULT NULL COMMENT 'creator',
    create_time       datetime      DEFAULT NULL COMMENT 'create time',
    update_by         bigint(20)    DEFAULT NULL COMMENT 'updater',
    update_time       datetime      DEFAULT NULL COMMENT 'update time',
    remark            varchar(500)  DEFAULT NULL COMMENT 'remark',
    del_flag          char(1)       DEFAULT '0' COMMENT 'delete flag',
    PRIMARY KEY (address_id),
    KEY idx_dc_cook_address_user (tenant_id, user_id, snapshot_type, del_flag),
    KEY idx_dc_cook_address_area (tenant_id, area_code, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand cooking user address and order address snapshot';

CREATE TABLE IF NOT EXISTS dc_cook_dish (
    dish_id      bigint(20)    NOT NULL COMMENT 'dish id',
    tenant_id    varchar(20)   DEFAULT '000000' COMMENT 'tenant id',
    dish_name    varchar(100)  NOT NULL COMMENT 'dish name',
    category     varchar(60)   DEFAULT NULL COMMENT 'dish category',
    cuisine      varchar(60)   DEFAULT NULL COMMENT 'cuisine',
    image_url    varchar(500)  DEFAULT NULL COMMENT 'image url',
    description  varchar(1000) DEFAULT NULL COMMENT 'description',
    status       char(1)       DEFAULT '0' COMMENT '0 enabled, 1 disabled',
    sort         int           DEFAULT 0 COMMENT 'sort order',
    create_dept  bigint(20)    DEFAULT NULL COMMENT 'create dept',
    create_by    bigint(20)    DEFAULT NULL COMMENT 'creator',
    create_time  datetime      DEFAULT NULL COMMENT 'create time',
    update_by    bigint(20)    DEFAULT NULL COMMENT 'updater',
    update_time  datetime      DEFAULT NULL COMMENT 'update time',
    remark       varchar(500)  DEFAULT NULL COMMENT 'remark',
    del_flag     char(1)       DEFAULT '0' COMMENT 'delete flag',
    PRIMARY KEY (dish_id),
    KEY idx_dc_cook_dish_status (tenant_id, status, del_flag),
    KEY idx_dc_cook_dish_category (tenant_id, category, cuisine)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand cooking dish library';

CREATE TABLE IF NOT EXISTS dc_cook_config (
    config_id             bigint(20)    NOT NULL COMMENT 'config id',
    tenant_id             varchar(20)   DEFAULT '000000' COMMENT 'tenant id',
    config_name           varchar(100)  NOT NULL COMMENT 'config name',
    config_key            varchar(100)  NOT NULL COMMENT 'config key',
    config_value          varchar(1000) NOT NULL COMMENT 'config value',
    value_type            varchar(30)   DEFAULT 'STRING' COMMENT 'STRING, NUMBER, JSON, PERCENT',
    config_type           varchar(30)   DEFAULT 'BUSINESS' COMMENT 'business type',
    rule_flag             char(1)       DEFAULT '1' COMMENT '1 rule config',
    effective_time        datetime      DEFAULT NULL COMMENT 'effective time',
    publish_status        varchar(30)   DEFAULT 'PUBLISHED' COMMENT 'publish status',
    change_reason         varchar(500)  DEFAULT NULL COMMENT 'change reason',
    announcement_content  varchar(1000) DEFAULT NULL COMMENT 'announcement content',
    create_dept           bigint(20)    DEFAULT NULL COMMENT 'create dept',
    create_by             bigint(20)    DEFAULT NULL COMMENT 'creator',
    create_time           datetime      DEFAULT NULL COMMENT 'create time',
    update_by             bigint(20)    DEFAULT NULL COMMENT 'updater',
    update_time           datetime      DEFAULT NULL COMMENT 'update time',
    remark                varchar(500)  DEFAULT NULL COMMENT 'remark',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_dc_cook_config_key (tenant_id, config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand cooking business config';

CREATE TABLE IF NOT EXISTS dc_cook_message (
    message_id            bigint(20)    NOT NULL COMMENT 'message id',
    tenant_id             varchar(20)   DEFAULT '000000' COMMENT 'tenant id',
    message_type          varchar(60)   NOT NULL COMMENT 'message type',
    channel               varchar(30)   NOT NULL COMMENT 'WECHAT, SMS, IN_APP',
    receiver_type         varchar(30)   DEFAULT NULL COMMENT 'USER, CHEF, ADMIN',
    receiver_id           bigint(20)    DEFAULT NULL COMMENT 'receiver id',
    receiver_mobile_mask  varchar(40)   DEFAULT NULL COMMENT 'masked mobile',
    receiver_openid_mask  varchar(80)   DEFAULT NULL COMMENT 'masked openid',
    related_order_id      bigint(20)    DEFAULT NULL COMMENT 'related order id',
    related_order_no      varchar(40)   DEFAULT NULL COMMENT 'related order no',
    related_biz_type      varchar(60)   DEFAULT NULL COMMENT 'related biz type',
    related_biz_id        bigint(20)    DEFAULT NULL COMMENT 'related biz id',
    content_summary       varchar(1000) DEFAULT NULL COMMENT 'content summary',
    send_status           varchar(30)   DEFAULT 'PENDING' COMMENT 'PENDING, SUCCESS, FAILED',
    send_time             datetime      DEFAULT NULL COMMENT 'send time',
    fail_reason           varchar(500)  DEFAULT NULL COMMENT 'fail reason',
    create_dept           bigint(20)    DEFAULT NULL COMMENT 'create dept',
    create_by             bigint(20)    DEFAULT NULL COMMENT 'creator',
    create_time           datetime      DEFAULT NULL COMMENT 'create time',
    update_by             bigint(20)    DEFAULT NULL COMMENT 'updater',
    update_time           datetime      DEFAULT NULL COMMENT 'update time',
    remark                varchar(500)  DEFAULT NULL COMMENT 'remark',
    del_flag              char(1)       DEFAULT '0' COMMENT 'delete flag',
    PRIMARY KEY (message_id),
    KEY idx_dc_cook_message_order (tenant_id, related_order_id),
    KEY idx_dc_cook_message_status (tenant_id, channel, send_status, create_time),
    KEY idx_dc_cook_message_receiver (tenant_id, receiver_type, receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand cooking message record';

CREATE TABLE IF NOT EXISTS dc_cook_order (
    order_id                  bigint(20)    NOT NULL COMMENT 'order id',
    tenant_id                 varchar(20)   DEFAULT '000000' COMMENT 'tenant id',
    order_no                  varchar(40)   NOT NULL COMMENT 'order no ODYYYYMMDDNNNN',
    user_id                   bigint(20)    NOT NULL COMMENT 'user id',
    chef_id                   bigint(20)    NOT NULL COMMENT 'chef id',
    address_id                bigint(20)    DEFAULT NULL COMMENT 'source address id',
    contact_name              varchar(80)   DEFAULT NULL COMMENT 'contact name snapshot',
    contact_phone             varchar(20)   DEFAULT NULL COMMENT 'contact phone snapshot',
    service_area              varchar(100)  DEFAULT NULL COMMENT 'service area snapshot',
    address_snapshot          varchar(1000) DEFAULT NULL COMMENT 'full address snapshot',
    dish_snapshot             text          DEFAULT NULL COMMENT 'dish snapshot json/text',
    user_remark               varchar(1000) DEFAULT NULL COMMENT 'taste, ingredient and user remark',
    service_start_time        datetime      NOT NULL COMMENT 'service start time',
    service_end_time          datetime      NOT NULL COMMENT 'service end time',
    status                    varchar(40)   NOT NULL COMMENT 'order status',
    quote_amount              decimal(12,2) DEFAULT NULL COMMENT 'quoted service amount',
    quote_remark              varchar(1000) DEFAULT NULL COMMENT 'quote remark',
    quote_time                datetime      DEFAULT NULL COMMENT 'quote time',
    quote_update_count        int           DEFAULT 0 COMMENT 'quote update count after objection',
    pay_deadline              datetime      DEFAULT NULL COMMENT 'pay deadline',
    pay_amount                decimal(12,2) DEFAULT NULL COMMENT 'paid amount',
    pay_no                    varchar(80)   DEFAULT NULL COMMENT 'payment no',
    pay_time                  datetime      DEFAULT NULL COMMENT 'pay time',
    objection_count           int           DEFAULT 0 COMMENT 'objection count',
    objection_reason          varchar(200)  DEFAULT NULL COMMENT 'objection reason',
    objection_remark          varchar(1000) DEFAULT NULL COMMENT 'objection remark',
    objection_time            datetime      DEFAULT NULL COMMENT 'objection time',
    objection_handle_time     datetime      DEFAULT NULL COMMENT 'objection handle time',
    cancel_type               varchar(40)   DEFAULT NULL COMMENT 'USER_UNPAID, USER_PAID, CHEF',
    cancel_reason             varchar(1000) DEFAULT NULL COMMENT 'cancel reason',
    cancel_time               datetime      DEFAULT NULL COMMENT 'cancel time',
    refund_amount             decimal(12,2) DEFAULT NULL COMMENT 'refund amount',
    refund_fee_amount         decimal(12,2) DEFAULT NULL COMMENT 'cancel service fee amount',
    refund_fee_rate           decimal(6,4)  DEFAULT NULL COMMENT 'cancel service fee rate',
    service_complete_time     datetime      DEFAULT NULL COMMENT 'service complete time',
    service_complete_type     varchar(40)   DEFAULT NULL COMMENT 'CHEF, SYSTEM',
    confirm_time              datetime      DEFAULT NULL COMMENT 'user confirm time',
    complete_time             datetime      DEFAULT NULL COMMENT 'final complete time',
    create_dept               bigint(20)    DEFAULT NULL COMMENT 'create dept',
    create_by                 bigint(20)    DEFAULT NULL COMMENT 'creator',
    create_time               datetime      DEFAULT NULL COMMENT 'create time',
    update_by                 bigint(20)    DEFAULT NULL COMMENT 'updater',
    update_time               datetime      DEFAULT NULL COMMENT 'update time',
    remark                    varchar(500)  DEFAULT NULL COMMENT 'remark',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_dc_cook_order_no (tenant_id, order_no),
    KEY idx_dc_cook_order_user_status (tenant_id, user_id, status, create_time),
    KEY idx_dc_cook_order_chef_status (tenant_id, chef_id, status, service_start_time),
    KEY idx_dc_cook_order_timeout (tenant_id, status, pay_deadline, service_end_time),
    KEY idx_dc_cook_order_month (tenant_id, chef_id, complete_time, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand cooking order';

CREATE TABLE IF NOT EXISTS dc_cook_review (
    review_id           bigint(20)    NOT NULL COMMENT 'review id',
    tenant_id           varchar(20)   DEFAULT '000000' COMMENT 'tenant id',
    order_id            bigint(20)    NOT NULL COMMENT 'order id',
    order_no            varchar(40)   NOT NULL COMMENT 'order no',
    user_id             bigint(20)    NOT NULL COMMENT 'user id',
    chef_id             bigint(20)    NOT NULL COMMENT 'chef id',
    rating              decimal(3,2)  NOT NULL COMMENT 'rating',
    content             varchar(1000) DEFAULT NULL COMMENT 'review content',
    image_urls          varchar(1000) DEFAULT NULL COMMENT 'image urls',
    display_status      varchar(30)   DEFAULT 'SHOW' COMMENT 'SHOW, HIDE',
    complaint_adjusted  char(1)       DEFAULT '0' COMMENT '1 rating adjusted by complaint',
    review_time         datetime      DEFAULT NULL COMMENT 'review time',
    create_dept         bigint(20)    DEFAULT NULL COMMENT 'create dept',
    create_by           bigint(20)    DEFAULT NULL COMMENT 'creator',
    create_time         datetime      DEFAULT NULL COMMENT 'create time',
    update_by           bigint(20)    DEFAULT NULL COMMENT 'updater',
    update_time         datetime      DEFAULT NULL COMMENT 'update time',
    remark              varchar(500)  DEFAULT NULL COMMENT 'remark',
    PRIMARY KEY (review_id),
    UNIQUE KEY uk_dc_cook_review_order (tenant_id, order_id),
    KEY idx_dc_cook_review_chef (tenant_id, chef_id, display_status, review_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand cooking review';

CREATE TABLE IF NOT EXISTS dc_cook_complaint (
    complaint_id    bigint(20)    NOT NULL COMMENT 'complaint id',
    tenant_id       varchar(20)   DEFAULT '000000' COMMENT 'tenant id',
    order_id        bigint(20)    NOT NULL COMMENT 'order id',
    order_no        varchar(40)   NOT NULL COMMENT 'order no',
    user_id         bigint(20)    NOT NULL COMMENT 'user id',
    chef_id         bigint(20)    NOT NULL COMMENT 'chef id',
    complaint_type  varchar(60)   DEFAULT NULL COMMENT 'complaint type',
    content         varchar(1000) NOT NULL COMMENT 'complaint content',
    image_urls      varchar(1000) DEFAULT NULL COMMENT 'image urls',
    status          varchar(30)   DEFAULT 'PENDING' COMMENT 'PENDING, VALID, INVALID',
    handle_result   varchar(1000) DEFAULT NULL COMMENT 'handle result',
    handler_id      bigint(20)    DEFAULT NULL COMMENT 'handler id',
    submit_time     datetime      DEFAULT NULL COMMENT 'submit time',
    handle_time     datetime      DEFAULT NULL COMMENT 'handle time',
    create_dept     bigint(20)    DEFAULT NULL COMMENT 'create dept',
    create_by       bigint(20)    DEFAULT NULL COMMENT 'creator',
    create_time     datetime      DEFAULT NULL COMMENT 'create time',
    update_by       bigint(20)    DEFAULT NULL COMMENT 'updater',
    update_time     datetime      DEFAULT NULL COMMENT 'update time',
    remark          varchar(500)  DEFAULT NULL COMMENT 'remark',
    PRIMARY KEY (complaint_id),
    UNIQUE KEY uk_dc_cook_complaint_order (tenant_id, order_id),
    KEY idx_dc_cook_complaint_status (tenant_id, status, submit_time),
    KEY idx_dc_cook_complaint_chef (tenant_id, chef_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand cooking complaint';

CREATE TABLE IF NOT EXISTS dc_cook_settlement (
    settlement_id          bigint(20)    NOT NULL COMMENT 'settlement id',
    tenant_id              varchar(20)   DEFAULT '000000' COMMENT 'tenant id',
    chef_id                bigint(20)    NOT NULL COMMENT 'chef id',
    settlement_month       varchar(7)    NOT NULL COMMENT 'yyyy-MM',
    base_salary            decimal(12,2) DEFAULT 0.00 COMMENT 'personal base salary',
    order_count            int           DEFAULT 0 COMMENT 'completed order count',
    order_amount           decimal(12,2) DEFAULT 0.00 COMMENT 'completed order amount',
    chef_rate              decimal(6,4)  DEFAULT 0.8000 COMMENT 'chef rate',
    chef_commission        decimal(12,2) DEFAULT 0.00 COMMENT 'chef commission before deduction',
    platform_rate          decimal(6,4)  DEFAULT 0.2000 COMMENT 'platform commission rate',
    platform_commission    decimal(12,2) DEFAULT 0.00 COMMENT 'platform commission',
    violation_count        int           DEFAULT 0 COMMENT 'monthly chef violation count',
    violation_deduction    decimal(12,2) DEFAULT 0.00 COMMENT 'violation deduction',
    final_commission       decimal(12,2) DEFAULT 0.00 COMMENT 'commission after deduction',
    payable_amount         decimal(12,2) DEFAULT 0.00 COMMENT 'offline payable amount',
    status                 varchar(30)   DEFAULT 'GENERATED' COMMENT 'GENERATED, CONFIRMED, PAID',
    manual_flag            char(1)       DEFAULT '0' COMMENT '1 manually adjusted',
    generated_time         datetime      DEFAULT NULL COMMENT 'generated time',
    create_dept            bigint(20)    DEFAULT NULL COMMENT 'create dept',
    create_by              bigint(20)    DEFAULT NULL COMMENT 'creator',
    create_time            datetime      DEFAULT NULL COMMENT 'create time',
    update_by              bigint(20)    DEFAULT NULL COMMENT 'updater',
    update_time            datetime      DEFAULT NULL COMMENT 'update time',
    remark                 varchar(500)  DEFAULT NULL COMMENT 'remark',
    PRIMARY KEY (settlement_id),
    UNIQUE KEY uk_dc_cook_settlement_month (tenant_id, chef_id, settlement_month),
    KEY idx_dc_cook_settlement_status (tenant_id, settlement_month, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Demand cooking monthly settlement';

-- ----------------------------
-- Default business config
-- ----------------------------

DELETE FROM dc_cook_config WHERE config_key IN (
    'cooking.response.timeout.minutes',
    'cooking.pay.timeout.minutes',
    'cooking.cancel.fee.rate',
    'cooking.platform.commission.rate',
    'cooking.service.duration.hours',
    'cooking.reserve.min.advance.minutes',
    'cooking.reserve.future.days',
    'cooking.sms.template.quote',
    'cooking.sms.template.service.reminder',
    'cooking.commission.announcement'
);

INSERT INTO dc_cook_config
(config_id, tenant_id, config_name, config_key, config_value, value_type, config_type, rule_flag, effective_time, publish_status, change_reason, announcement_content, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
(300001, '000000', 'Chef response timeout minutes', 'cooking.response.timeout.minutes', '30', 'NUMBER', 'ORDER', '1', NOW(), 'PUBLISHED', 'MVP default', NULL, 103, 1, NOW(), NULL, NULL, 'Chef must respond before timeout'),
(300002, '000000', 'User pay timeout minutes', 'cooking.pay.timeout.minutes', '30', 'NUMBER', 'ORDER', '1', NOW(), 'PUBLISHED', 'MVP default', NULL, 103, 1, NOW(), NULL, NULL, 'User must pay before timeout'),
(300003, '000000', 'Paid cancel fee rate', 'cooking.cancel.fee.rate', '0.10', 'PERCENT', 'REFUND', '1', NOW(), 'PUBLISHED', 'MVP default', NULL, 103, 1, NOW(), NULL, NULL, 'Paid cancellation fee rate after 10 minutes'),
(300004, '000000', 'Platform commission rate', 'cooking.platform.commission.rate', '0.20', 'PERCENT', 'SETTLEMENT', '1', NOW(), 'PUBLISHED', 'MVP default', 'Platform commission rate is 20%, changes are announced at least 3 days in advance.', 103, 1, NOW(), NULL, NULL, 'Platform commission rate'),
(300005, '000000', 'Service duration hours', 'cooking.service.duration.hours', '3', 'NUMBER', 'RESERVE', '1', NOW(), 'PUBLISHED', 'MVP default', NULL, 103, 1, NOW(), NULL, NULL, 'Each appointment locks three hours'),
(300006, '000000', 'Minimum advance reservation minutes', 'cooking.reserve.min.advance.minutes', '60', 'NUMBER', 'RESERVE', '1', NOW(), 'PUBLISHED', 'MVP default', NULL, 103, 1, NOW(), NULL, NULL, 'User cannot reserve immediate or past time'),
(300007, '000000', 'Future reservation days', 'cooking.reserve.future.days', '3', 'NUMBER', 'RESERVE', '1', NOW(), 'PUBLISHED', 'MVP default', NULL, 103, 1, NOW(), NULL, NULL, 'User can reserve within future 3 days'),
(300008, '000000', 'Quote SMS template', 'cooking.sms.template.quote', 'Your cooking service quote is ready.', 'STRING', 'MESSAGE', '0', NOW(), 'PUBLISHED', 'MVP default', NULL, 103, 1, NOW(), NULL, NULL, 'Placeholder SMS template'),
(300009, '000000', 'Service reminder SMS template', 'cooking.sms.template.service.reminder', 'Your cooking service starts in 30 minutes.', 'STRING', 'MESSAGE', '0', NOW(), 'PUBLISHED', 'MVP default', NULL, 103, 1, NOW(), NULL, NULL, 'Placeholder SMS template'),
(300010, '000000', 'Commission announcement', 'cooking.commission.announcement', 'Platform commission rate is 20%.', 'STRING', 'ANNOUNCEMENT', '0', NOW(), 'PUBLISHED', 'MVP default', 'Platform commission rate is 20%.', 103, 1, NOW(), NULL, NULL, 'Workbench announcement');

-- ----------------------------
-- Seed data for local smoke testing
-- ----------------------------

INSERT IGNORE INTO dc_cook_area
(area_id, tenant_id, area_code, area_name, parent_code, area_level, status, sort, create_dept, create_by, create_time, remark, del_flag)
VALUES
(300001, '000000', 'DEMO-SH-CENTRAL', 'Demo Central District', NULL, 'DISTRICT', '0', 1, 103, 1, NOW(), 'Demo service area', '0');

INSERT IGNORE INTO dc_cook_dish
(dish_id, tenant_id, dish_name, category, cuisine, image_url, description, status, sort, create_dept, create_by, create_time, remark, del_flag)
VALUES
(300001, '000000', 'Home-style braised pork', 'Meat', 'Home Cooking', NULL, 'Demo dish', '0', 1, 103, 1, NOW(), 'Demo dish', '0'),
(300002, '000000', 'Seasonal vegetable stir fry', 'Vegetable', 'Home Cooking', NULL, 'Demo dish', '0', 2, 103, 1, NOW(), 'Demo dish', '0');

-- ----------------------------
-- Admin menu and permissions
-- Uses high ids to avoid upstream RuoYi menu conflicts.
-- ----------------------------

DELETE FROM sys_menu WHERE menu_id BETWEEN 300000 AND 300099;

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
(300000, 'Demand Cooking', 0, 6, 'cooking', NULL, '', 1, 0, 'M', '0', '0', '', 'shopping', 103, 1, NOW(), NULL, NULL, 'Demand cooking directory'),
(300001, 'Chef Management', 300000, 1, 'chef', 'cooking/chef/index', '', 1, 0, 'C', '0', '0', 'cooking:chef:list', 'people', 103, 1, NOW(), NULL, NULL, 'Chef management'),
(300002, 'Order Management', 300000, 2, 'order', 'cooking/order/index', '', 1, 0, 'C', '0', '0', 'cooking:order:list', 'list', 103, 1, NOW(), NULL, NULL, 'Order management'),
(300003, 'Dish Library', 300000, 3, 'dish', 'cooking/dish/index', '', 1, 0, 'C', '0', '0', 'cooking:dish:list', 'category', 103, 1, NOW(), NULL, NULL, 'Dish library'),
(300004, 'Complaint Records', 300000, 4, 'complaint', 'cooking/complaint/index', '', 1, 0, 'C', '0', '0', 'cooking:complaint:list', 'message', 103, 1, NOW(), NULL, NULL, 'Complaint records'),
(300005, 'Settlement', 300000, 5, 'settlement', 'cooking/settlement/index', '', 1, 0, 'C', '0', '0', 'cooking:settlement:list', 'money', 103, 1, NOW(), NULL, NULL, 'Monthly settlement'),
(300006, 'Business Config', 300000, 6, 'config', 'cooking/config/index', '', 1, 0, 'C', '0', '0', 'cooking:config:list', 'setting', 103, 1, NOW(), NULL, NULL, 'Business config'),
(300007, 'Address Management', 300000, 7, 'address', 'cooking/address/index', '', 1, 0, 'C', '0', '0', 'cooking:address:list', 'form', 103, 1, NOW(), NULL, NULL, 'Address management'),
(300008, 'Service Area', 300000, 8, 'area', 'cooking/area/index', '', 1, 0, 'C', '0', '0', 'cooking:area:list', 'tree', 103, 1, NOW(), NULL, NULL, 'Service area'),
(300009, 'Message Records', 300000, 9, 'message', 'cooking/message/index', '', 1, 0, 'C', '0', '0', 'cooking:message:list', 'message', 103, 1, NOW(), NULL, NULL, 'Message records'),
(300010, 'Review Management', 300000, 10, 'review', 'cooking/review/index', '', 1, 0, 'C', '0', '0', 'cooking:review:list', 'star', 103, 1, NOW(), NULL, NULL, 'Review management');

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
(300020, 'Chef Add', 300001, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:chef:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(300021, 'Chef Edit', 300001, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:chef:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300022, 'Chef Remove', 300001, 3, '', '', '', 1, 0, 'F', '0', '0', 'cooking:chef:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(300023, 'Chef Audit', 300001, 4, '', '', '', 1, 0, 'F', '0', '0', 'cooking:chef:audit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300024, 'Order Edit', 300002, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:order:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300025, 'Refund Retry', 300002, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:order:refundRetry', '#', 103, 1, NOW(), NULL, NULL, ''),
(300026, 'Dish Add', 300003, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:dish:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(300027, 'Dish Edit', 300003, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:dish:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300028, 'Dish Remove', 300003, 3, '', '', '', 1, 0, 'F', '0', '0', 'cooking:dish:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(300029, 'Complaint Handle', 300004, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:complaint:handle', '#', 103, 1, NOW(), NULL, NULL, ''),
(300030, 'Settlement Confirm', 300005, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:settlement:confirm', '#', 103, 1, NOW(), NULL, NULL, ''),
(300031, 'Config Edit', 300006, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:config:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300032, 'Area Edit', 300008, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:area:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300033, 'Message View', 300009, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:message:query', '#', 103, 1, NOW(), NULL, NULL, ''),
(300034, 'Review Edit', 300010, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:review:edit', '#', 103, 1, NOW(), NULL, NULL, '');

SET FOREIGN_KEY_CHECKS = 1;
