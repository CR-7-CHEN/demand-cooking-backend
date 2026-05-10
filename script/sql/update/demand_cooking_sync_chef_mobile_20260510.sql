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
