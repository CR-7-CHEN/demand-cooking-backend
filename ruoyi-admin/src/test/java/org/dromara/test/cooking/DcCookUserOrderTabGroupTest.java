package org.dromara.test.cooking;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.system.domain.bo.cooking.DcCookOrderBo;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookAddressMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookChefTimeMapper;
import org.dromara.system.mapper.cooking.DcCookMessageMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.impl.cooking.DcCookOrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("Cooking user order tab groups")
@Tag("dev")
public class DcCookUserOrderTabGroupTest {

    @Test
    @DisplayName("resolves frontend order tabs to backend status groups")
    void resolvesStatusesForUserTabs() {
        assertEquals(List.of(DcCookOrderStatus.WAITING_RESPONSE),
            DcCookOrderStatus.statusesForUserTab("reserved"));
        assertEquals(List.of(DcCookOrderStatus.WAITING_PAY, DcCookOrderStatus.PRICE_OBJECTION),
            DcCookOrderStatus.statusesForUserTab("payment"));
        assertEquals(List.of(DcCookOrderStatus.WAITING_SERVICE, DcCookOrderStatus.WAITING_CONFIRM),
            DcCookOrderStatus.statusesForUserTab("serving"));
        assertEquals(List.of(DcCookOrderStatus.COMPLETED),
            DcCookOrderStatus.statusesForUserTab("completed"));
    }

    @Test
    @DisplayName("statusGroup filter builds grouped status query")
    void statusGroupFilterBuildsGroupedQuery() throws Exception {
        initTableInfo(DcCookOrder.class);
        DcCookOrderServiceImpl service = newService();
        DcCookOrderBo bo = new DcCookOrderBo();
        bo.setUserId(100L);
        bo.setStatusGroup("payment");

        LambdaQueryWrapper<DcCookOrder> wrapper = invokeBuildQueryWrapper(service, bo);
        String sqlSegment = wrapper.getSqlSegment();
        Map<String, Object> values = wrapper.getParamNameValuePairs();

        assertTrue(containsParamValue(values, 100L));
        assertTrue(containsParamValue(values, DcCookOrderStatus.WAITING_PAY));
        assertTrue(containsParamValue(values, DcCookOrderStatus.PRICE_OBJECTION));
        assertTrue(sqlSegment.contains("status"));
    }

    @Test
    @DisplayName("chef name filter builds chef subquery for admin order list")
    void chefNameFilterBuildsChefSubquery() throws Exception {
        initTableInfo(DcCookOrder.class);
        DcCookOrderServiceImpl service = newService();
        DcCookOrderBo bo = new DcCookOrderBo();
        bo.setChefName("Chef A");

        LambdaQueryWrapper<DcCookOrder> wrapper = invokeBuildQueryWrapper(service, bo);
        String sqlSegment = wrapper.getSqlSegment();
        Map<String, Object> values = wrapper.getParamNameValuePairs();

        assertTrue(containsParamValue(values, "Chef A"));
        assertTrue(sqlSegment.contains("dc_cook_chef"));
        assertTrue(sqlSegment.contains("chef_name"));
    }

    private boolean containsParamValue(Map<String, Object> values, Object expected) {
        return values.values().stream().anyMatch(value -> {
            if (expected.equals(value)) {
                return true;
            }
            if (value instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    if (expected.equals(item)) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private DcCookOrderServiceImpl newService() {
        return new DcCookOrderServiceImpl(
            mock(DcCookOrderMapper.class),
            mock(DcCookChefMapper.class),
            mock(DcCookChefTimeMapper.class),
            mock(DcCookAddressMapper.class),
            mock(DcCookMessageMapper.class),
            mock(SysUserMapper.class),
            mock(IDcCookConfigService.class)
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

    @SuppressWarnings("unchecked")
    private LambdaQueryWrapper<DcCookOrder> invokeBuildQueryWrapper(DcCookOrderServiceImpl service, DcCookOrderBo bo)
        throws Exception {
        Method method = DcCookOrderServiceImpl.class.getDeclaredMethod("buildQueryWrapper", DcCookOrderBo.class);
        method.setAccessible(true);
        return (LambdaQueryWrapper<DcCookOrder>) method.invoke(service, bo);
    }
}
