-- 做饭人员离职原因字段。
ALTER TABLE dc_cook_chef
    ADD COLUMN resign_reason varchar(500) DEFAULT NULL COMMENT '离职原因' AFTER chef_status;
