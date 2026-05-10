-- 同步服务厨师状态字段默认值与注释为数字口径。
ALTER TABLE dc_cook_chef
    MODIFY COLUMN audit_status varchar(30) DEFAULT '0' COMMENT '审核状态（0待审核 1已通过 2已拒绝）';

ALTER TABLE dc_cook_chef
    MODIFY COLUMN chef_status varchar(30) DEFAULT '0' COMMENT '厨师状态（0正常 1暂停 2禁用 3离职）';
