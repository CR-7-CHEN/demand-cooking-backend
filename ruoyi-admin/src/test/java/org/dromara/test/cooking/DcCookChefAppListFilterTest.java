package org.dromara.test.cooking;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.system.domain.bo.cooking.DcCookChefBo;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
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

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("Cooking chef app list filters")
@Tag("dev")
public class DcCookChefAppListFilterTest {

    @Test
    @DisplayName("app list only includes chefs with user confirmed completed services")
    void buildAppWrapperRequiresCompletedOrders() throws Exception {
        initTableInfo(DcCookChef.class);
        DcCookChefServiceImpl service = new DcCookChefServiceImpl(
            mock(DcCookChefMapper.class),
            mock(DcCookChefTimeMapper.class),
            mock(DcCookOrderMapper.class),
            mock(DcCookReviewMapper.class),
            mock(DcCookSettlementMapper.class),
            mock(IDcCookConfigService.class)
        );

        LambdaQueryWrapper<DcCookChef> wrapper = invokeBuildAppWrapper(service, new DcCookChefBo());
        Map<String, Object> values = wrapper.getParamNameValuePairs();

        assertTrue(values.containsValue(DcCookOrderStatus.COMPLETED));
        assertTrue(wrapper.getSqlSegment().contains("dc_cook_order"));
        assertTrue(wrapper.getSqlSegment().contains("status"));
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryWrapper<DcCookChef> invokeBuildAppWrapper(DcCookChefServiceImpl service, DcCookChefBo bo)
        throws Exception {
        Method method = DcCookChefServiceImpl.class.getDeclaredMethod("buildAppWrapper", DcCookChefBo.class);
        method.setAccessible(true);
        return (LambdaQueryWrapper<DcCookChef>) method.invoke(service, bo);
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
