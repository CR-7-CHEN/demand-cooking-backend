-- 涓婇棬鍋氶キ MVP 鏁版嵁搴撹剼鏈?(MySQL)
-- 璇峰湪鑻ヤ緷鍩虹鑴氭湰 ry_vue_5.X.sql銆乺y_job.sql銆乺y_workflow.sql 涔嬪悗鎵ц銆?

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 鏍稿績涓氬姟琛?
-- ----------------------------

CREATE TABLE IF NOT EXISTS dc_cook_area (
    area_id          bigint(20)   NOT NULL COMMENT '鏈嶅姟鍖哄煙ID',
    tenant_id        varchar(20)  DEFAULT '000000' COMMENT '绉熸埛ID',
    area_code        varchar(64)  NOT NULL COMMENT '鍖哄煙缂栫爜',
    area_name        varchar(100) NOT NULL COMMENT '鍖哄煙鍚嶇О',
    parent_code      varchar(64)  DEFAULT NULL COMMENT '鐖剁骇鍖哄煙缂栫爜',
    area_level       varchar(20)  DEFAULT 'CITY' COMMENT '鍖哄煙灞傜骇',
    status           char(1)      DEFAULT '0' COMMENT '鐘舵€侊紙0鍚敤 1鍋滅敤锛?,
    sort             int          DEFAULT 0 COMMENT '鎺掑簭',
    create_dept      bigint(20)   DEFAULT NULL COMMENT '鍒涘缓閮ㄩ棬',
    create_by        bigint(20)   DEFAULT NULL COMMENT '鍒涘缓鑰?,
    create_time      datetime     DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    update_by        bigint(20)   DEFAULT NULL COMMENT '鏇存柊鑰?,
    update_time      datetime     DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    remark           varchar(500) DEFAULT NULL COMMENT '澶囨敞',
    del_flag         char(1)      DEFAULT '0' COMMENT '鍒犻櫎鏍囧織',
    PRIMARY KEY (area_id),
    UNIQUE KEY uk_dc_cook_area_code (tenant_id, area_code),
    KEY idx_dc_cook_area_status (tenant_id, status, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鏈嶅姟鍖哄煙琛?;

CREATE TABLE IF NOT EXISTS dc_cook_chef (
    chef_id                 bigint(20)    NOT NULL COMMENT '鍘ㄥ笀ID',
    tenant_id               varchar(20)   DEFAULT '000000' COMMENT '绉熸埛ID',
    user_id                 bigint(20)    DEFAULT NULL COMMENT '鍏宠仈绯荤粺鐢ㄦ埛ID',
    chef_name               varchar(80)   NOT NULL COMMENT '鍘ㄥ笀濮撳悕',
    gender                  char(1)       DEFAULT NULL COMMENT '鎬у埆锛?鐢?1濂?2鏈煡锛?,
    age                     int           DEFAULT NULL COMMENT '骞撮緞',
    mobile                  varchar(20)   NOT NULL COMMENT '鎵嬫満鍙?,
    avatar_url              varchar(500)  DEFAULT NULL COMMENT '澶村儚鍦板潃',
    work_image_urls         varchar(2000) DEFAULT NULL COMMENT '浣滃搧鍥惧湴鍧€锛屽涓€楀彿鍒嗛殧',
    area_id                 bigint(20)    DEFAULT NULL COMMENT '涓绘湇鍔″尯鍩烮D',
    area_name               varchar(100)  DEFAULT NULL COMMENT '涓绘湇鍔″尯鍩熷悕绉?,
    skill_tags              varchar(500)  DEFAULT NULL COMMENT '鎿呴暱鏍囩',
    intro                   varchar(1000) DEFAULT NULL COMMENT '涓汉绠€浠?,
    health_cert_no          varchar(100)  DEFAULT NULL COMMENT '鍋ュ悍璇佺紪鍙?,
    health_cert_image_url   varchar(500)  DEFAULT NULL COMMENT '鍋ュ悍璇佸浘鐗囧湴鍧€',
    health_cert_expire_date datetime      DEFAULT NULL COMMENT '鍋ュ悍璇佸埌鏈熸棩鏈?,
    base_salary             decimal(12,2) DEFAULT 0.00 COMMENT '涓汉搴曡柂',
    rating                  decimal(3,2)  DEFAULT 5.00 COMMENT '骞冲潎璇勫垎',
    completed_orders        bigint(20)    DEFAULT 0 COMMENT '宸插畬鎴愯鍗曟暟',
    recommend_flag          char(1)       DEFAULT '0' COMMENT '鎺ㄨ崘鏍囧織锛?鎺ㄨ崘锛?,
    audit_status            varchar(30)   DEFAULT '0' COMMENT '瀹℃牳鐘舵€侊紙0寰呭鏍?1宸查€氳繃 2宸叉嫆缁濓級',
    audit_reason            varchar(500)  DEFAULT NULL COMMENT '瀹℃牳鍘熷洜',
    chef_status             varchar(30)   DEFAULT '0' COMMENT '鍘ㄥ笀鐘舵€侊紙0姝ｅ父 1鏆傚仠 2绂佺敤 3绂昏亴锛?,
    resign_reason           varchar(500)  DEFAULT NULL COMMENT '绂昏亴鍘熷洜',
    create_dept             bigint(20)    DEFAULT NULL COMMENT '鍒涘缓閮ㄩ棬',
    create_by               bigint(20)    DEFAULT NULL COMMENT '鍒涘缓鑰?,
    create_time             datetime      DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    update_by               bigint(20)    DEFAULT NULL COMMENT '鏇存柊鑰?,
    update_time             datetime      DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    remark                  varchar(500)  DEFAULT NULL COMMENT '澶囨敞',
    del_flag                char(1)       DEFAULT '0' COMMENT '鍒犻櫎鏍囧織',
    PRIMARY KEY (chef_id),
    UNIQUE KEY uk_dc_cook_chef_mobile (tenant_id, mobile, del_flag),
    KEY idx_dc_cook_chef_user (tenant_id, user_id),
    KEY idx_dc_cook_chef_area_status (tenant_id, area_id, audit_status, chef_status, del_flag),
    KEY idx_dc_cook_chef_sort (tenant_id, recommend_flag, completed_orders, rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍘ㄥ笀琛?;

CREATE TABLE IF NOT EXISTS dc_cook_chef_time (
    time_id     bigint(20)   NOT NULL COMMENT '鍙绾︽椂闂碔D',
    tenant_id   varchar(20)  DEFAULT '000000' COMMENT '绉熸埛ID',
    chef_id     bigint(20)   NOT NULL COMMENT '鍘ㄥ笀ID',
    start_time  datetime     NOT NULL COMMENT '鍙绾﹀紑濮嬫椂闂?,
    end_time    datetime     NOT NULL COMMENT '鍙绾︾粨鏉熸椂闂?,
    status      char(1)      DEFAULT '0' COMMENT '鐘舵€侊紙0鍚敤 1鍋滅敤锛?,
    create_dept bigint(20)   DEFAULT NULL COMMENT '鍒涘缓閮ㄩ棬',
    create_by   bigint(20)   DEFAULT NULL COMMENT '鍒涘缓鑰?,
    create_time datetime     DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    update_by   bigint(20)   DEFAULT NULL COMMENT '鏇存柊鑰?,
    update_time datetime     DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    remark      varchar(500) DEFAULT NULL COMMENT '澶囨敞',
    del_flag    char(1)      DEFAULT '0' COMMENT '鍒犻櫎鏍囧織',
    PRIMARY KEY (time_id),
    KEY idx_dc_cook_chef_time_chef (tenant_id, chef_id, start_time, end_time, status, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鍘ㄥ笀鍙绾︽椂闂磋〃';

CREATE TABLE IF NOT EXISTS dc_cook_address (
    address_id        bigint(20)    NOT NULL COMMENT '鍦板潃ID',
    tenant_id         varchar(20)   DEFAULT '000000' COMMENT '绉熸埛ID',
    user_id           bigint(20)    NOT NULL COMMENT '鐢ㄦ埛ID',
    contact_name      varchar(80)   NOT NULL COMMENT '鑱旂郴浜哄鍚?,
    contact_phone     varchar(20)   NOT NULL COMMENT '鑱旂郴浜虹數璇?,
    area_code         varchar(64)   DEFAULT NULL COMMENT '鍖哄煙缂栫爜',
    area_name         varchar(100)  DEFAULT NULL COMMENT '鍖哄煙鍚嶇О',
    detail_address    varchar(500)  NOT NULL COMMENT '璇︾粏鍦板潃',
    house_number      varchar(100)  DEFAULT NULL COMMENT '闂ㄧ墝鍙?,
    longitude         decimal(12,6) DEFAULT NULL COMMENT '缁忓害',
    latitude          decimal(12,6) DEFAULT NULL COMMENT '绾害',
    default_flag      char(1)       DEFAULT '0' COMMENT '榛樿鍦板潃锛?鏄級',
    source_address_id bigint(20)    DEFAULT NULL COMMENT '蹇収鏉ユ簮鍦板潃ID',
    snapshot_type     varchar(20)   DEFAULT 'NORMAL' COMMENT '绫诲瀷锛圢ORMAL鏅€?ORDER璁㈠崟蹇収锛?,
    snapshot_time     datetime      DEFAULT NULL COMMENT '蹇収鏃堕棿',
    create_dept       bigint(20)    DEFAULT NULL COMMENT '鍒涘缓閮ㄩ棬',
    create_by         bigint(20)    DEFAULT NULL COMMENT '鍒涘缓鑰?,
    create_time       datetime      DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    update_by         bigint(20)    DEFAULT NULL COMMENT '鏇存柊鑰?,
    update_time       datetime      DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    remark            varchar(500)  DEFAULT NULL COMMENT '澶囨敞',
    del_flag          char(1)       DEFAULT '0' COMMENT '鍒犻櫎鏍囧織',
    PRIMARY KEY (address_id),
    KEY idx_dc_cook_address_user (tenant_id, user_id, snapshot_type, del_flag),
    KEY idx_dc_cook_address_area (tenant_id, area_code, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鐢ㄦ埛鍦板潃琛?;

CREATE TABLE IF NOT EXISTS dc_cook_dish (
    dish_id      bigint(20)    NOT NULL COMMENT '鑿滃搧ID',
    tenant_id    varchar(20)   DEFAULT '000000' COMMENT '绉熸埛ID',
    dish_name    varchar(100)  NOT NULL COMMENT '鑿滃搧鍚嶇О',
    category     varchar(60)   DEFAULT NULL COMMENT '鑿滃搧鍒嗙被',
    cuisine      varchar(60)   DEFAULT NULL COMMENT '鑿滅郴',
    image_url    varchar(500)  DEFAULT NULL COMMENT '鍥剧墖鍦板潃',
    description  varchar(1000) DEFAULT NULL COMMENT '鑿滃搧鎻忚堪',
    status       char(1)       DEFAULT '0' COMMENT '鐘舵€侊紙0鍚敤 1鍋滅敤锛?,
    sort         int           DEFAULT 0 COMMENT '鎺掑簭',
    create_dept  bigint(20)    DEFAULT NULL COMMENT '鍒涘缓閮ㄩ棬',
    create_by    bigint(20)    DEFAULT NULL COMMENT '鍒涘缓鑰?,
    create_time  datetime      DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    update_by    bigint(20)    DEFAULT NULL COMMENT '鏇存柊鑰?,
    update_time  datetime      DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    remark       varchar(500)  DEFAULT NULL COMMENT '澶囨敞',
    del_flag     char(1)       DEFAULT '0' COMMENT '鍒犻櫎鏍囧織',
    PRIMARY KEY (dish_id),
    KEY idx_dc_cook_dish_status (tenant_id, status, del_flag),
    KEY idx_dc_cook_dish_category (tenant_id, category, cuisine)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鑿滃搧搴?;

CREATE TABLE IF NOT EXISTS dc_cook_config (
    config_id             bigint(20)    NOT NULL COMMENT '閰嶇疆ID',
    tenant_id             varchar(20)   DEFAULT '000000' COMMENT '绉熸埛ID',
    config_name           varchar(100)  NOT NULL COMMENT '閰嶇疆鍚嶇О',
    config_key            varchar(100)  NOT NULL COMMENT '閰嶇疆閿?,
    config_value          varchar(1000) NOT NULL COMMENT '閰嶇疆鍊?,
    value_type            varchar(30)   DEFAULT 'STRING' COMMENT '鍊肩被鍨嬶紙STRING瀛楃涓?NUMBER鏁板€?JSON瀵硅薄 PERCENT鐧惧垎姣旓級',
    config_type           varchar(30)   DEFAULT 'BUSINESS' COMMENT '閰嶇疆鍒嗙被锛圔USINESS涓氬姟 ORDER璁㈠崟 REFUND閫€娆?SETTLEMENT缁撶畻 RESERVE棰勭害 MESSAGE娑堟伅 ANNOUNCEMENT鍏憡锛?,
    rule_flag             char(1)       DEFAULT '1' COMMENT '瑙勫垯閰嶇疆锛?鏄級',
    effective_time        datetime      DEFAULT NULL COMMENT '鐢熸晥鏃堕棿',
    publish_status        varchar(30)   DEFAULT 'PUBLISHED' COMMENT '鍙戝竷鐘舵€侊紙PUBLISHED宸插彂甯?DRAFT鑽夌 UNPUBLISHED鏈彂甯冿級',
    change_reason         varchar(500)  DEFAULT NULL COMMENT '鍙樻洿鍘熷洜',
    announcement_content  varchar(1000) DEFAULT NULL COMMENT '鍏憡鍐呭',
    create_dept           bigint(20)    DEFAULT NULL COMMENT '鍒涘缓閮ㄩ棬',
    create_by             bigint(20)    DEFAULT NULL COMMENT '鍒涘缓鑰?,
    create_time           datetime      DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    update_by             bigint(20)    DEFAULT NULL COMMENT '鏇存柊鑰?,
    update_time           datetime      DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    remark                varchar(500)  DEFAULT NULL COMMENT '澶囨敞',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_dc_cook_config_key (tenant_id, config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='涓氬姟閰嶇疆琛?;

CREATE TABLE IF NOT EXISTS dc_cook_message (
    message_id            bigint(20)    NOT NULL COMMENT '娑堟伅ID',
    tenant_id             varchar(20)   DEFAULT '000000' COMMENT '绉熸埛ID',
    message_type          varchar(60)   NOT NULL COMMENT '娑堟伅绫诲瀷',
    channel               varchar(30)   NOT NULL COMMENT '娓犻亾锛圵ECHAT寰俊 SMS鐭俊 IN_APP绔欏唴锛?,
    receiver_type         varchar(30)   DEFAULT NULL COMMENT '鎺ユ敹鑰呯被鍨嬶紙USER鐢ㄦ埛 CHEF鍘ㄥ笀 ADMIN绠＄悊鍛橈級',
    receiver_id           bigint(20)    DEFAULT NULL COMMENT '鎺ユ敹鑰匢D',
    receiver_mobile_mask  varchar(40)   DEFAULT NULL COMMENT '鎵嬫満鍙锋帺鐮?,
    receiver_openid_mask  varchar(80)   DEFAULT NULL COMMENT 'OpenID鎺╃爜',
    related_order_id      bigint(20)    DEFAULT NULL COMMENT '鍏宠仈璁㈠崟ID',
    related_order_no      varchar(40)   DEFAULT NULL COMMENT '鍏宠仈璁㈠崟鍙?,
    related_biz_type      varchar(60)   DEFAULT NULL COMMENT '鍏宠仈涓氬姟绫诲瀷',
    related_biz_id        bigint(20)    DEFAULT NULL COMMENT '鍏宠仈涓氬姟ID',
    content_summary       varchar(1000) DEFAULT NULL COMMENT '鍐呭鎽樿',
    send_status           varchar(30)   DEFAULT 'PENDING' COMMENT '鍙戦€佺姸鎬侊紙PENDING寰呭彂閫?SUCCESS鎴愬姛 FAILED澶辫触锛?,
    send_time             datetime      DEFAULT NULL COMMENT '鍙戦€佹椂闂?,
    fail_reason           varchar(500)  DEFAULT NULL COMMENT '澶辫触鍘熷洜',
    create_dept           bigint(20)    DEFAULT NULL COMMENT '鍒涘缓閮ㄩ棬',
    create_by             bigint(20)    DEFAULT NULL COMMENT '鍒涘缓鑰?,
    create_time           datetime      DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    update_by             bigint(20)    DEFAULT NULL COMMENT '鏇存柊鑰?,
    update_time           datetime      DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    remark                varchar(500)  DEFAULT NULL COMMENT '澶囨敞',
    del_flag              char(1)       DEFAULT '0' COMMENT '鍒犻櫎鏍囧織',
    PRIMARY KEY (message_id),
    KEY idx_dc_cook_message_order (tenant_id, related_order_id),
    KEY idx_dc_cook_message_status (tenant_id, channel, send_status, create_time),
    KEY idx_dc_cook_message_receiver (tenant_id, receiver_type, receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='娑堟伅璁板綍琛?;

CREATE TABLE IF NOT EXISTS dc_cook_order (
    order_id                  bigint(20)    NOT NULL COMMENT '璁㈠崟ID',
    tenant_id                 varchar(20)   DEFAULT '000000' COMMENT '绉熸埛ID',
    order_no                  varchar(40)   NOT NULL COMMENT '璁㈠崟鍙凤紙ODYYYYMMDDNNNN锛?,
    user_id                   bigint(20)    NOT NULL COMMENT '鐢ㄦ埛ID',
    chef_id                   bigint(20)    NOT NULL COMMENT '鍘ㄥ笀ID',
    address_id                bigint(20)    DEFAULT NULL COMMENT '鏉ユ簮鍦板潃ID',
    contact_name              varchar(80)   DEFAULT NULL COMMENT '鑱旂郴浜哄鍚嶅揩鐓?,
    contact_phone             varchar(20)   DEFAULT NULL COMMENT '鑱旂郴浜虹數璇濆揩鐓?,
    service_area              varchar(100)  DEFAULT NULL COMMENT '鏈嶅姟鍖哄煙蹇収',
    address_snapshot          varchar(1000) DEFAULT NULL COMMENT '瀹屾暣鍦板潃蹇収',
    dish_snapshot             text          DEFAULT NULL COMMENT '鑿滃搧蹇収锛圝SON锛?,
    user_remark               varchar(1000) DEFAULT NULL COMMENT '鐢ㄦ埛澶囨敞锛堝彛鍛?蹇屽彛绛夛級',
    service_start_time        datetime      NOT NULL COMMENT '鏈嶅姟寮€濮嬫椂闂?,
    service_end_time          datetime      NOT NULL COMMENT '鏈嶅姟缁撴潫鏃堕棿',
    status                    varchar(40)   NOT NULL COMMENT '璁㈠崟鐘舵€?,
    quote_amount              decimal(12,2) DEFAULT NULL COMMENT '鎶ヤ环閲戦',
    quote_remark              varchar(1000) DEFAULT NULL COMMENT '鎶ヤ环澶囨敞',
    quote_time                datetime      DEFAULT NULL COMMENT '鎶ヤ环鏃堕棿',
    quote_update_count        int           DEFAULT 0 COMMENT '寮傝鍚庢姤浠锋洿鏂版鏁?,
    pay_deadline              datetime      DEFAULT NULL COMMENT '鏀粯鎴鏃堕棿',
    pay_amount                decimal(12,2) DEFAULT NULL COMMENT '瀹炰粯閲戦',
    pay_no                    varchar(80)   DEFAULT NULL COMMENT '鏀粯娴佹按鍙?,
    pay_time                  datetime      DEFAULT NULL COMMENT '鏀粯鏃堕棿',
    objection_count           int           DEFAULT 0 COMMENT '寮傝娆℃暟',
    objection_reason          varchar(200)  DEFAULT NULL COMMENT '寮傝鍘熷洜',
    objection_remark          varchar(1000) DEFAULT NULL COMMENT '寮傝澶囨敞',
    objection_time            datetime      DEFAULT NULL COMMENT '寮傝鏃堕棿',
    objection_handle_time     datetime      DEFAULT NULL COMMENT '寮傝澶勭悊鏃堕棿',
    cancel_type               varchar(40)   DEFAULT NULL COMMENT '鍙栨秷绫诲瀷锛圲SER_UNPAID鏈粯鍙栨秷 USER_PAID宸蹭粯鍙栨秷 CHEF鍘ㄥ笀鍙栨秷锛?,
    cancel_reason             varchar(1000) DEFAULT NULL COMMENT '鍙栨秷鍘熷洜',
    cancel_time               datetime      DEFAULT NULL COMMENT '鍙栨秷鏃堕棿',
    refund_amount             decimal(12,2) DEFAULT NULL COMMENT '閫€娆鹃噾棰?,
    refund_fee_amount         decimal(12,2) DEFAULT NULL COMMENT '鍙栨秷鎵嬬画璐归噾棰?,
    refund_fee_rate           decimal(6,4)  DEFAULT NULL COMMENT '鍙栨秷鎵嬬画璐规瘮渚?,
    service_complete_time     datetime      DEFAULT NULL COMMENT '鏈嶅姟瀹屾垚鏃堕棿',
    service_complete_type     varchar(40)   DEFAULT NULL COMMENT '瀹屾垚鏂瑰紡锛圕HEF鍘ㄥ笀纭 SYSTEM绯荤粺鑷姩锛?,
    confirm_time              datetime      DEFAULT NULL COMMENT '鐢ㄦ埛纭鏃堕棿',
    complete_time             datetime      DEFAULT NULL COMMENT '鏈€缁堝畬鎴愭椂闂?,
    create_dept               bigint(20)    DEFAULT NULL COMMENT '鍒涘缓閮ㄩ棬',
    create_by                 bigint(20)    DEFAULT NULL COMMENT '鍒涘缓鑰?,
    create_time               datetime      DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    update_by                 bigint(20)    DEFAULT NULL COMMENT '鏇存柊鑰?,
    update_time               datetime      DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    remark                    varchar(500)  DEFAULT NULL COMMENT '澶囨敞',
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_dc_cook_order_no (tenant_id, order_no),
    KEY idx_dc_cook_order_user_status (tenant_id, user_id, status, create_time),
    KEY idx_dc_cook_order_chef_status (tenant_id, chef_id, status, service_start_time),
    KEY idx_dc_cook_order_timeout (tenant_id, status, pay_deadline, service_end_time),
    KEY idx_dc_cook_order_month (tenant_id, chef_id, complete_time, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璁㈠崟琛?;

CREATE TABLE IF NOT EXISTS dc_cook_review (
    review_id           bigint(20)    NOT NULL COMMENT '璇勪环ID',
    tenant_id           varchar(20)   DEFAULT '000000' COMMENT '绉熸埛ID',
    order_id            bigint(20)    NOT NULL COMMENT '璁㈠崟ID',
    order_no            varchar(40)   NOT NULL COMMENT '璁㈠崟鍙?,
    user_id             bigint(20)    NOT NULL COMMENT '鐢ㄦ埛ID',
    chef_id             bigint(20)    NOT NULL COMMENT '鍘ㄥ笀ID',
    rating              decimal(3,2)  NOT NULL COMMENT '璇勫垎',
    content             varchar(1000) DEFAULT NULL COMMENT '璇勪环鍐呭',
    image_urls          varchar(1000) DEFAULT NULL COMMENT '鍥剧墖鍦板潃',
    display_status      varchar(30)   DEFAULT 'SHOW' COMMENT '鏄剧ず鐘舵€侊紙SHOW鏄剧ず HIDE闅愯棌锛?,
    complaint_adjusted  char(1)       DEFAULT '0' COMMENT '鎶曡瘔璋冩暣鏍囧織锛?宸茶皟鏁达級',
    review_time         datetime      DEFAULT NULL COMMENT '璇勪环鏃堕棿',
    create_dept         bigint(20)    DEFAULT NULL COMMENT '鍒涘缓閮ㄩ棬',
    create_by           bigint(20)    DEFAULT NULL COMMENT '鍒涘缓鑰?,
    create_time         datetime      DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    update_by           bigint(20)    DEFAULT NULL COMMENT '鏇存柊鑰?,
    update_time         datetime      DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    remark              varchar(500)  DEFAULT NULL COMMENT '澶囨敞',
    PRIMARY KEY (review_id),
    UNIQUE KEY uk_dc_cook_review_order (tenant_id, order_id),
    KEY idx_dc_cook_review_chef (tenant_id, chef_id, display_status, review_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='璇勪环琛?;

CREATE TABLE IF NOT EXISTS dc_cook_complaint (
    complaint_id    bigint(20)    NOT NULL COMMENT '鎶曡瘔ID',
    tenant_id       varchar(20)   DEFAULT '000000' COMMENT '绉熸埛ID',
    order_id        bigint(20)    NOT NULL COMMENT '璁㈠崟ID',
    order_no        varchar(40)   NOT NULL COMMENT '璁㈠崟鍙?,
    user_id         bigint(20)    NOT NULL COMMENT '鐢ㄦ埛ID',
    chef_id         bigint(20)    NOT NULL COMMENT '鍘ㄥ笀ID',
    complaint_type  varchar(60)   DEFAULT NULL COMMENT '鎶曡瘔绫诲瀷',
    content         varchar(1000) NOT NULL COMMENT '鎶曡瘔鍐呭',
    image_urls      varchar(1000) DEFAULT NULL COMMENT '鍥剧墖鍦板潃',
    status          varchar(30)   DEFAULT 'PENDING' COMMENT '鐘舵€侊紙PENDING寰呭鐞?VALID鎴愮珛 INVALID涓嶆垚绔嬶級',
    handle_result   varchar(1000) DEFAULT NULL COMMENT '澶勭悊缁撴灉',
    handler_id      bigint(20)    DEFAULT NULL COMMENT '澶勭悊浜篒D',
    submit_time     datetime      DEFAULT NULL COMMENT '鎻愪氦鏃堕棿',
    handle_time     datetime      DEFAULT NULL COMMENT '澶勭悊鏃堕棿',
    create_dept     bigint(20)    DEFAULT NULL COMMENT '鍒涘缓閮ㄩ棬',
    create_by       bigint(20)    DEFAULT NULL COMMENT '鍒涘缓鑰?,
    create_time     datetime      DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    update_by       bigint(20)    DEFAULT NULL COMMENT '鏇存柊鑰?,
    update_time     datetime      DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    remark          varchar(500)  DEFAULT NULL COMMENT '澶囨敞',
    PRIMARY KEY (complaint_id),
    UNIQUE KEY uk_dc_cook_complaint_order (tenant_id, order_id),
    KEY idx_dc_cook_complaint_status (tenant_id, status, submit_time),
    KEY idx_dc_cook_complaint_chef (tenant_id, chef_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鎶曡瘔璁板綍琛?;

CREATE TABLE IF NOT EXISTS dc_cook_settlement (
    settlement_id          bigint(20)    NOT NULL COMMENT '缁撶畻ID',
    tenant_id              varchar(20)   DEFAULT '000000' COMMENT '绉熸埛ID',
    chef_id                bigint(20)    NOT NULL COMMENT '鍘ㄥ笀ID',
    settlement_month       varchar(7)    NOT NULL COMMENT '缁撶畻鏈堜唤锛坹yyy-MM锛?,
    base_salary            decimal(12,2) DEFAULT 0.00 COMMENT '涓汉搴曡柂',
    order_count            int           DEFAULT 0 COMMENT '宸插畬鎴愯鍗曟暟',
    order_amount           decimal(12,2) DEFAULT 0.00 COMMENT '宸插畬鎴愯鍗曢噾棰?,
    chef_rate              decimal(6,4)  DEFAULT 0.8000 COMMENT '鍘ㄥ笀鍒嗘垚姣斾緥',
    chef_commission        decimal(12,2) DEFAULT 0.00 COMMENT '鍘ㄥ笀浣ｉ噾锛堟墸闄ゅ墠锛?,
    platform_rate          decimal(6,4)  DEFAULT 0.2000 COMMENT '骞冲彴鎶芥垚姣斾緥',
    platform_commission    decimal(12,2) DEFAULT 0.00 COMMENT '骞冲彴浣ｉ噾',
    violation_count        int           DEFAULT 0 COMMENT '鏈堝害杩濊娆℃暟',
    violation_deduction    decimal(12,2) DEFAULT 0.00 COMMENT '杩濊鎵ｆ',
    final_commission       decimal(12,2) DEFAULT 0.00 COMMENT '鏈€缁堜剑閲戯紙鎵ｉ櫎鍚庯級',
    payable_amount         decimal(12,2) DEFAULT 0.00 COMMENT '搴斿彂閲戦',
    status                 varchar(30)   DEFAULT 'GENERATED' COMMENT '主状态（GENERATED/REVIEWING/CONFIRMED/PAID）',
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
    pay_remark             varchar(500)  DEFAULT NULL COMMENT '发放说明',
    create_dept            bigint(20)    DEFAULT NULL COMMENT '鍒涘缓閮ㄩ棬',
    create_by              bigint(20)    DEFAULT NULL COMMENT '鍒涘缓鑰?,
    create_time            datetime      DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
    update_by              bigint(20)    DEFAULT NULL COMMENT '鏇存柊鑰?,
    update_time            datetime      DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
    remark                 varchar(500)  DEFAULT NULL COMMENT '澶囨敞',
    PRIMARY KEY (settlement_id),
    UNIQUE KEY uk_dc_cook_settlement_month (tenant_id, chef_id, settlement_month),
    KEY idx_dc_cook_settlement_status (tenant_id, settlement_month, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鏈堝害缁撶畻琛?;

ALTER TABLE dc_cook_settlement
    MODIFY COLUMN status varchar(30) DEFAULT 'GENERATED' COMMENT '主状态（GENERATED/REVIEWING/CONFIRMED/PAID）',
    ADD COLUMN IF NOT EXISTS review_reason_type varchar(40) DEFAULT NULL COMMENT '复核原因类型' AFTER status,
    ADD COLUMN IF NOT EXISTS review_remark varchar(500) DEFAULT NULL COMMENT '复核申请说明' AFTER review_reason_type,
    ADD COLUMN IF NOT EXISTS review_result varchar(20) DEFAULT NULL COMMENT '复核处理结果（KEEP/REGENERATE）' AFTER review_remark,
    ADD COLUMN IF NOT EXISTS review_reply varchar(500) DEFAULT NULL COMMENT '复核处理说明' AFTER review_result,
    ADD COLUMN IF NOT EXISTS review_apply_time datetime DEFAULT NULL COMMENT '复核申请时间' AFTER review_reply,
    ADD COLUMN IF NOT EXISTS review_handle_time datetime DEFAULT NULL COMMENT '复核处理时间' AFTER review_apply_time,
    ADD COLUMN IF NOT EXISTS confirm_time datetime DEFAULT NULL COMMENT '确认时间' AFTER generated_time,
    ADD COLUMN IF NOT EXISTS pay_time datetime DEFAULT NULL COMMENT '发放时间' AFTER confirm_time,
    ADD COLUMN IF NOT EXISTS pay_remark varchar(500) DEFAULT NULL COMMENT '发放说明' AFTER pay_time;

-- ----------------------------
-- 灏忕▼搴忚璇佸鎴风
-- ----------------------------

-- 纭繚 App 瀹㈡埛绔敮鎸佽处鍙峰瘑鐮佺櫥褰?娉ㄥ唽
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
-- 榛樿涓氬姟閰嶇疆
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
(300001, '000000', '鍘ㄥ笀鍝嶅簲瓒呮椂鏃堕棿(鍒嗛挓)', 'cooking.response.timeout.minutes', '30', 'NUMBER', 'ORDER', '1', NOW(), 'PUBLISHED', '鍒濆榛樿鍊?, NULL, 103, 1, NOW(), NULL, NULL, '鍘ㄥ笀蹇呴』鍦ㄨ秴鏃跺墠鍝嶅簲璁㈠崟'),
(300002, '000000', '鐢ㄦ埛鏀粯瓒呮椂鏃堕棿(鍒嗛挓)', 'cooking.pay.timeout.minutes', '30', 'NUMBER', 'ORDER', '1', NOW(), 'PUBLISHED', '鍒濆榛樿鍊?, NULL, 103, 1, NOW(), NULL, NULL, '鐢ㄦ埛蹇呴』鍦ㄨ秴鏃跺墠瀹屾垚鏀粯'),
(300003, '000000', '宸蹭粯娆惧彇娑堟墜缁垂姣斾緥', 'cooking.cancel.fee.rate', '0.10', 'PERCENT', 'REFUND', '1', NOW(), 'PUBLISHED', '鍒濆榛樿鍊?, NULL, 103, 1, NOW(), NULL, NULL, '浠樻10鍒嗛挓鍚庡彇娑堟敹鍙栨墜缁垂姣斾緥'),
(300004, '000000', '骞冲彴鎶芥垚姣斾緥', 'cooking.platform.commission.rate', '0.20', 'PERCENT', 'SETTLEMENT', '1', NOW(), 'PUBLISHED', '鍒濆榛樿鍊?, '骞冲彴鎶芥垚姣斾緥涓?0%锛岃皟鏁村皢鎻愬墠3澶╁叕鍛婇€氱煡銆?, 103, 1, NOW(), NULL, NULL, '骞冲彴鎶芥垚姣斾緥'),
(300005, '000000', '鏈嶅姟鏃堕暱(灏忔椂)', 'cooking.service.duration.hours', '3', 'NUMBER', 'RESERVE', '1', NOW(), 'PUBLISHED', '鍒濆榛樿鍊?, NULL, 103, 1, NOW(), NULL, NULL, '姣忔棰勭害閿佸畾3灏忔椂'),
(300006, '000000', '鏈€灏戞彁鍓嶉绾︽椂闂?鍒嗛挓)', 'cooking.reserve.min.advance.minutes', '60', 'NUMBER', 'RESERVE', '1', NOW(), 'PUBLISHED', '鍒濆榛樿鍊?, NULL, 103, 1, NOW(), NULL, NULL, '鐢ㄦ埛涓嶈兘棰勭害鍗虫椂鎴栬繃鍘荤殑鏃堕棿'),
(300007, '000000', '鍙绾︽湭鏉ュぉ鏁?, 'cooking.reserve.future.days', '3', 'NUMBER', 'RESERVE', '1', NOW(), 'PUBLISHED', '鍒濆榛樿鍊?, NULL, 103, 1, NOW(), NULL, NULL, '鐢ㄦ埛鍙绾︽湭鏉?澶╁唴鐨勬椂闂?),
(300008, '000000', '鐢ㄦ埛纭瓒呮椂鏃堕棿(灏忔椂)', 'cooking.confirm.timeout.hours', '24', 'NUMBER', 'ORDER', '1', NOW(), 'PUBLISHED', '鍒濆榛樿鍊?, NULL, 103, 1, NOW(), NULL, NULL, '鏈嶅姟瀹屾垚鍚庣敤鎴锋湭纭鍒欒嚜鍔ㄥ畬鎴?),
(300009, '000000', '鎶ヤ环鐭俊妯℃澘', 'cooking.sms.template.quote', '鎮ㄧ殑涓婇棬鍋氶キ鏈嶅姟鎶ヤ环宸茬敓鎴愶紝璇峰強鏃舵煡鐪嬨€?, 'STRING', 'MESSAGE', '0', NOW(), 'PUBLISHED', '鍒濆榛樿鍊?, NULL, 103, 1, NOW(), NULL, NULL, '鐭俊妯℃澘鍗犱綅'),
(300010, '000000', '鏈嶅姟鎻愰啋鐭俊妯℃澘', 'cooking.sms.template.service.reminder', '鎮ㄧ殑涓婇棬鍋氶キ鏈嶅姟灏嗗湪30鍒嗛挓鍚庡紑濮嬶紝璇峰仛濂藉噯澶囥€?, 'STRING', 'MESSAGE', '0', NOW(), 'PUBLISHED', '鍒濆榛樿鍊?, NULL, 103, 1, NOW(), NULL, NULL, '鐭俊妯℃澘鍗犱綅'),
(300011, '000000', '鎶芥垚鍏憡', 'cooking.commission.announcement', '骞冲彴鎶芥垚姣斾緥涓?0%銆?, 'STRING', 'ANNOUNCEMENT', '0', NOW(), 'PUBLISHED', '鍒濆榛樿鍊?, '骞冲彴鎶芥垚姣斾緥涓?0%銆?, 103, 1, NOW(), NULL, NULL, '宸ヤ綔鍙板叕鍛?);

-- ----------------------------
-- 绉嶅瓙鏁版嵁锛堟湰鍦版祴璇曠敤锛?
-- ----------------------------

REPLACE INTO dc_cook_area
(area_id, tenant_id, area_code, area_name, parent_code, area_level, status, sort, create_dept, create_by, create_time, remark, del_flag)
VALUES
(300001, '000000', 'DEMO-SH-CENTRAL', '绀轰緥涓績鍩庡尯', NULL, 'DISTRICT', '0', 1, 103, 1, NOW(), '绀轰緥鏈嶅姟鍖哄煙', '0');

REPLACE INTO dc_cook_dish
(dish_id, tenant_id, dish_name, category, cuisine, image_url, description, status, sort, create_dept, create_by, create_time, remark, del_flag)
VALUES
(300001, '000000', '绾㈢儳鑲?, '鑽よ彍', '瀹跺父鑿?, NULL, '缁忓吀瀹跺父鑿滐紝鑲ヨ€屼笉鑵?, '0', 1, 103, 1, NOW(), '绀轰緥鑿滃搧', '0'),
(300002, '000000', '鏃惰敩灏忕倰', '绱犺彍', '瀹跺父鑿?, NULL, '鏂伴矞鏃朵护钄彍', '0', 2, 103, 1, NOW(), '绀轰緥鑿滃搧', '0'),
(300003, '000000', '娓呰捀椴堥奔', '娴烽矞', '绮よ彍', NULL, '椴滃娓呰捀锛屼繚鐣欏師鍛?, '0', 3, 103, 1, NOW(), '绀轰緥鑿滃搧', '0'),
(300004, '000000', '楹诲﹩璞嗚厫', '璞嗗埗鍝?, '宸濊彍', NULL, '楹昏荆椴滈锛屼笅楗匠鍝?, '0', 4, 103, 1, NOW(), '绀轰緥鑿滃搧', '0'),
(300005, '000000', '绯栭唻鎺掗', '鑽よ彍', '瀹跺父鑿?, NULL, '閰哥敎鍙彛锛岃€佸皯鐨嗗疁', '0', 5, 103, 1, NOW(), '绀轰緥鑿滃搧', '0'),
(300006, '000000', '瑗跨孩鏌跨倰铔?, '绱犺彍', '瀹跺父鑿?, NULL, '瀹跺父缁忓吀锛岀畝鍗曠編鍛?, '0', 6, 103, 1, NOW(), '绀轰緥鑿滃搧', '0'),
(300007, '000000', '瀹繚楦′竵', '鑽よ彍', '宸濊彍', NULL, '鑺辩敓楦′竵锛岄杈ｅ紑鑳?, '0', 7, 103, 1, NOW(), '绀轰緥鑿滃搧', '0'),
(300008, '000000', '閰歌彍楸?, '娴烽矞', '宸濊彍', NULL, '閰歌荆椴滅編锛岄奔鑲夊婊?, '0', 8, 103, 1, NOW(), '绀轰緥鑿滃搧', '0');

-- ----------------------------
-- 鍚庡彴鑿滃崟鍙婃潈闄?
-- ----------------------------

DELETE FROM sys_menu WHERE menu_id BETWEEN 300000 AND 300099;

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
(300000, '涓婇棬鍋氶キ', 0, 6, 'cooking', NULL, '', 1, 0, 'M', '0', '0', '', 'shopping', 103, 1, NOW(), NULL, NULL, '涓婇棬鍋氶キ鐩綍'),
(300001, '鍘ㄥ笀绠＄悊', 300000, 1, 'chef', 'cooking/chef/index', '', 1, 0, 'C', '0', '0', 'cooking:chef:list', 'people', 103, 1, NOW(), NULL, NULL, '鍘ㄥ笀绠＄悊'),
(300002, '璁㈠崟绠＄悊', 300000, 2, 'order', 'cooking/order/index', '', 1, 0, 'C', '0', '0', 'cooking:order:list', 'list', 103, 1, NOW(), NULL, NULL, '璁㈠崟绠＄悊'),
(300003, '鑿滃搧搴?, 300000, 3, 'dish', 'cooking/dish/index', '', 1, 0, 'C', '0', '0', 'cooking:dish:list', 'category', 103, 1, NOW(), NULL, NULL, '鑿滃搧搴?),
(300004, '鎶曡瘔璁板綍', 300000, 4, 'complaint', 'cooking/complaint/index', '', 1, 0, 'C', '0', '0', 'cooking:complaint:list', 'message', 103, 1, NOW(), NULL, NULL, '鎶曡瘔璁板綍'),
(300005, '缁撶畻绠＄悊', 300000, 5, 'settlement', 'cooking/settlement/index', '', 1, 0, 'C', '0', '0', 'cooking:settlement:list', 'money', 103, 1, NOW(), NULL, NULL, '鏈堝害缁撶畻'),
(300006, '涓氬姟閰嶇疆', 300000, 6, 'config', 'cooking/config/index', '', 1, 0, 'C', '0', '0', 'cooking:config:list', 'edit', 103, 1, NOW(), NULL, NULL, '涓氬姟閰嶇疆'),
(300007, '鍦板潃绠＄悊', 300000, 7, 'address', 'cooking/address/index', '', 1, 0, 'C', '0', '0', 'cooking:address:list', 'form', 103, 1, NOW(), NULL, NULL, '鍦板潃绠＄悊'),
(300008, '鏈嶅姟鍖哄煙', 300000, 8, 'area', 'cooking/area/index', '', 1, 0, 'C', '0', '0', 'cooking:area:list', 'tree', 103, 1, NOW(), NULL, NULL, '鏈嶅姟鍖哄煙'),
(300009, '娑堟伅璁板綍', 300000, 9, 'message', 'cooking/message/index', '', 1, 0, 'C', '0', '0', 'cooking:message:list', 'message', 103, 1, NOW(), NULL, NULL, '娑堟伅璁板綍'),
(300010, '璇勪环绠＄悊', 300000, 10, 'review', 'cooking/review/index', '', 1, 0, 'C', '0', '0', 'cooking:review:list', 'star', 103, 1, NOW(), NULL, NULL, '璇勪环绠＄悊');

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES
(300020, '鍘ㄥ笀鏂板', 300001, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:chef:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(300021, '鍘ㄥ笀缂栬緫', 300001, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:chef:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300022, '鍘ㄥ笀鍒犻櫎', 300001, 3, '', '', '', 1, 0, 'F', '0', '0', 'cooking:chef:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(300023, '鍘ㄥ笀瀹℃牳', 300001, 4, '', '', '', 1, 0, 'F', '0', '0', 'cooking:chef:audit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300024, '璁㈠崟缂栬緫', 300002, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:order:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300025, '閫€娆鹃噸璇?, 300002, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:order:refundRetry', '#', 103, 1, NOW(), NULL, NULL, ''),
(300026, '鑿滃搧鏂板', 300003, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:dish:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(300027, '鑿滃搧缂栬緫', 300003, 2, '', '', '', 1, 0, 'F', '0', '0', 'cooking:dish:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300028, '鑿滃搧鍒犻櫎', 300003, 3, '', '', '', 1, 0, 'F', '0', '0', 'cooking:dish:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(300029, '鎶曡瘔澶勭悊', 300004, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:complaint:handle', '#', 103, 1, NOW(), NULL, NULL, ''),
(300030, '缁撶畻纭', 300005, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:settlement:confirm', '#', 103, 1, NOW(), NULL, NULL, ''),
(300031, '閰嶇疆缂栬緫', 300006, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:config:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300032, '鍖哄煙缂栬緫', 300008, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:area:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(300033, '娑堟伅鏌ョ湅', 300009, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:message:query', '#', 103, 1, NOW(), NULL, NULL, ''),
(300034, '璇勪环缂栬緫', 300010, 1, '', '', '', 1, 0, 'F', '0', '0', 'cooking:review:edit', '#', 103, 1, NOW(), NULL, NULL, '');

-- ----------------------------
-- 闅愯棌闈炲繀瑕侀《灞傝彍鍗曪紙淇濈暀锛氶椤点€佷笂闂ㄥ仛楗€佺郴缁熺鐞嗭級
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

-- 闅愯棌绯荤粺绠＄悊涓嬮潪蹇呰瀛愯彍鍗曪紙淇濈暀锛氱敤鎴风鐞?00銆佽鑹茬鐞?01銆佸弬鏁拌缃?06銆侀€氱煡鍏憡107銆佹枃浠剁鐞?18锛?
UPDATE sys_menu SET visible = '1' WHERE menu_id IN (102, 103, 104, 105, 108, 123) AND parent_id = 1;

-- 寮€鍚处鍙疯嚜鍔╂敞鍐岋紝渚涘皬绋嬪簭鐢ㄦ埛娉ㄥ唽浣跨敤
UPDATE sys_config SET config_value = 'true' WHERE config_key = 'sys.account.registerUser';

SET FOREIGN_KEY_CHECKS = 1;
