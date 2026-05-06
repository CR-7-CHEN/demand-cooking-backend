package org.dromara.test.cooking;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.system.domain.cooking.DcCookChef;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.cooking.DcCookComplaint;
import org.dromara.system.domain.cooking.DcCookOrder;
import org.dromara.system.domain.vo.cooking.DcCookDashboardOverviewVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.mapper.cooking.DcCookChefMapper;
import org.dromara.system.mapper.cooking.DcCookComplaintMapper;
import org.dromara.system.mapper.cooking.DcCookOrderMapper;
import org.dromara.system.service.impl.cooking.DcCookDashboardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Cooking dashboard revenue trend")
@Tag("dev")
public class DcCookDashboardServiceTest {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MM.dd");

    @Test
    @DisplayName("overview returns recent seven days with concrete date labels")
    public void overviewReturnsRecentSevenDaysWithConcreteDateLabels() {
        DcCookOrderMapper orderMapper = mock(DcCookOrderMapper.class);
        DcCookChefMapper chefMapper = mock(DcCookChefMapper.class);
        DcCookComplaintMapper complaintMapper = mock(DcCookComplaintMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        DcCookDashboardServiceImpl service = new DcCookDashboardServiceImpl(orderMapper, chefMapper, complaintMapper, userMapper);
        initTableInfo(DcCookOrder.class);
        initTableInfo(DcCookChef.class);
        initTableInfo(DcCookComplaint.class);
        initTableInfo(SysUser.class);

        when(orderMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(orderMapper.selectList(any(Wrapper.class))).thenReturn(List.of(), List.of());
        when(orderMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(emptyPage());
        when(chefMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(complaintMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(userMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        DcCookDashboardOverviewVo overview = service.overview("month");

        assertEquals("week", overview.getTrendMode());
        assertEquals(7, overview.getRevenueTrend().size());

        LocalDate firstDate = LocalDate.now().minusDays(6);
        for (int i = 0; i < overview.getRevenueTrend().size(); i++) {
            LocalDate expectedDate = firstDate.plusDays(i);
            DcCookDashboardOverviewVo.TrendItem item = overview.getRevenueTrend().get(i);
            assertEquals(DATE_FORMAT.format(expectedDate), item.getDate());
            assertEquals(LABEL_FORMAT.format(expectedDate), item.getLabel());
            assertEquals(0, item.getAmount().signum());
        }
    }

    private Page<DcCookOrder> emptyPage() {
        Page<DcCookOrder> page = new Page<>(1, 4);
        page.setRecords(List.of());
        return page;
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
