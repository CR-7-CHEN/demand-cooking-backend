package org.dromara.test.cooking;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.cooking.DcCookChefBo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookChefTimeMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookReviewMapper;
import org.dromara.system.mapper.cooking.DcCookSettlementMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.impl.cooking.DcCookChefServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cooking chef mobile sync")
@Tag("dev")
class DcCookChefMobileSyncTest {

    @Test
    @DisplayName("chef application mobile syncs to sys user phone")
    void syncUserPhoneStoresChefMobileOnSysUser() throws Exception {
        initTableInfo(SysUser.class);
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        DcCookChefServiceImpl service = newService(chefMapper, userMapper);

        DcCookChefBo bo = new DcCookChefBo();
        bo.setUserId(100L);
        bo.setMobile("13800138000");

        Method method = DcCookChefServiceImpl.class.getDeclaredMethod("syncUserPhone", DcCookChefBo.class);
        method.setAccessible(true);
        method.invoke(service, bo);

        verify(userMapper).update(isNull(SysUser.class), any(LambdaUpdateWrapper.class));
    }

    @Test
    @DisplayName("chef application rejects mobile used by another sys user")
    void insertByBoRejectsMobileUsedByAnotherUser() {
        initTableInfo(SysUser.class);
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        DcCookChefServiceImpl service = newService(chefMapper, userMapper);
        when(userMapper.exists(any())).thenReturn(true);

        DcCookChefBo bo = new DcCookChefBo();
        bo.setUserId(100L);
        bo.setMobile("13800138000");

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            Method method = DcCookChefServiceImpl.class.getDeclaredMethod("validateUserPhoneAvailable", DcCookChefBo.class);
            method.setAccessible(true);
            method.invoke(service, bo);
        });
        assertTrue(exception.getCause() instanceof ServiceException);
    }

    private DcCookChefServiceImpl newService(DcCookChefMapper chefMapper, SysUserMapper userMapper) {
        return new DcCookChefServiceImpl(
            chefMapper,
            mock(DcCookChefTimeMapper.class),
            mock(DcCookOrderMapper.class),
            mock(DcCookReviewMapper.class),
            mock(DcCookSettlementMapper.class),
            mock(IDcCookConfigService.class),
            userMapper
        );
    }

    private void initTableInfo(Class<?> entityClass) {
        if (TableInfoHelper.getTableInfo(entityClass) != null) {
            return;
        }
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), entityClass.getName());
        assistant.setCurrentNamespace(entityClass.getName());
        TableInfoHelper.initTableInfo(assistant, entityClass);
    }
}
