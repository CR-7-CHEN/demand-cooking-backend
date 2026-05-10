-- 隐藏服务区域菜单及其下权限按钮（不删除，仅设为不可见）
UPDATE sys_menu
SET visible = '1'
WHERE menu_id = 300008
   OR parent_id = 300008;
