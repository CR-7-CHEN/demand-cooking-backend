-- 服务厨师可预约时间表
-- 用于补齐已部署库缺失的 dc_cook_chef_time 表。
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
