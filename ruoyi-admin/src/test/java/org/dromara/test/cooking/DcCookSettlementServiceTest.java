package org.dromara.test.cooking;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.cooking.DcCookSettlement;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.mapper.cooking.DcCookSettlementMapper;
import org.dromara.system.service.cooking.IDcCookConfigService;
import org.dromara.system.service.impl.cooking.DcCookSettlementServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("Cooking settlement month query rules")
@Tag("dev")
public class DcCookSettlementServiceTest {

    @Test
    @DisplayName("completed settlement orders use business-time month instead of order number prefix")
    void completedOrdersUseBusinessMonthWindow() throws Exception {
        initTableInfo(DcCookOrder.class);

        LambdaQueryWrapper<DcCookOrder> wrapper = invokeOrderWrapper(newService(), 88L, "2026-05");
        Map<String, Object> values = wrapper.getParamNameValuePairs();

        assertTrue(values.containsValue(88L));
        assertTrue(values.containsValue(org.dromara.system.domain.cooking.DcCookOrderStatus.COMPLETED));
        assertTrue(values.containsValue(monthStart("2026-05")));
        assertTrue(values.containsValue(nextMonthStart("2026-05")));
        assertTrue(wrapper.getSqlSegment().contains("completeTime"));
        assertTrue(wrapper.getSqlSegment().contains("confirmTime"));
        assertTrue(wrapper.getSqlSegment().contains("payTime"));
        assertFalse(wrapper.getSqlSegment().contains("orderNo"));
    }

    @Test
    @DisplayName("chef cancel violations use cancel time month instead of order number prefix")
    void chefCancelViolationsUseCancelMonthWindow() throws Exception {
        initTableInfo(DcCookOrder.class);

        LambdaQueryWrapper<DcCookOrder> wrapper = invokeCancelWrapper(newService(), 99L, "2026-05");
        Map<String, Object> values = wrapper.getParamNameValuePairs();

        assertTrue(values.containsValue(99L));
        assertTrue(values.containsValue(org.dromara.system.domain.cooking.DcCookOrderStatus.CANCEL_CHEF));
        assertTrue(values.containsValue(monthStart("2026-05")));
        assertTrue(values.containsValue(nextMonthStart("2026-05")));
        assertTrue(wrapper.getSqlSegment().contains("cancelTime"));
        assertFalse(wrapper.getSqlSegment().contains("orderNo"));
    }

    private DcCookSettlementServiceImpl newService() {
        return new DcCookSettlementServiceImpl(
            mock(DcCookSettlementMapper.class),
            mock(DcCookOrderMapper.class),
            mock(DcCookChefMapper.class),
            mock(IDcCookConfigService.class)
        );
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryWrapper<DcCookOrder> invokeOrderWrapper(DcCookSettlementServiceImpl service, Long chefId, String month)
        throws Exception {
        Method method = DcCookSettlementServiceImpl.class.getDeclaredMethod("buildCompletedOrderMonthWrapper", Long.class, String.class);
        method.setAccessible(true);
        return (LambdaQueryWrapper<DcCookOrder>) method.invoke(service, chefId, month);
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryWrapper<DcCookOrder> invokeCancelWrapper(DcCookSettlementServiceImpl service, Long chefId, String month)
        throws Exception {
        Method method = DcCookSettlementServiceImpl.class.getDeclaredMethod("buildChefCancelMonthWrapper", Long.class, String.class);
        method.setAccessible(true);
        return (LambdaQueryWrapper<DcCookOrder>) method.invoke(service, chefId, month);
    }

    private Date monthStart(String month) {
        return Date.from(java.time.YearMonth.parse(month).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date nextMonthStart(String month) {
        return Date.from(java.time.YearMonth.parse(month).plusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
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
