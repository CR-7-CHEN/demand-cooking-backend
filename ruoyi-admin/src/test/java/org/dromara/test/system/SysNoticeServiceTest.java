package org.dromara.test.system;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.system.domain.SysNotice;
import org.dromara.system.domain.bo.SysNoticeBo;
import org.dromara.system.mapper.SysNoticeMapper;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.impl.SysNoticeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("System notice query rules")
class SysNoticeServiceTest {

    @Test
    @DisplayName("buildQueryWrapper includes notice status filter")
    void buildQueryWrapperIncludesStatusFilter() throws Exception {
        initTableInfo(SysNotice.class);

        SysNoticeServiceImpl service = new SysNoticeServiceImpl(
            mock(SysNoticeMapper.class),
            mock(SysUserMapper.class)
        );
        SysNoticeBo bo = new SysNoticeBo();
        bo.setNoticeTitle("workbench notice");
        bo.setNoticeType("2");
        bo.setStatus("0");

        LambdaQueryWrapper<SysNotice> wrapper = invokeBuildQueryWrapper(service, bo);

        assertTrue(wrapper.getSqlSegment().contains("status"));
    }

    @Test
    @DisplayName("app notice list only queries normal announcement records")
    void appNoticeListOnlyQueriesNormalAnnouncements() {
        initTableInfo(SysNotice.class);

        SysNoticeMapper mapper = mock(SysNoticeMapper.class);
        SysNoticeServiceImpl service = new SysNoticeServiceImpl(
            mapper,
            mock(SysUserMapper.class)
        );
        when(mapper.selectVoList(any())).thenReturn(List.of());

        service.selectAppNoticeList();

        verify(mapper).selectVoList(argThat((LambdaQueryWrapper<SysNotice> wrapper) -> wrapper.getSqlSegment().contains("status")
            && wrapper.getSqlSegment().contains("notice_type")
            && wrapper.getSqlSegment().contains("create_time")));
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryWrapper<SysNotice> invokeBuildQueryWrapper(SysNoticeServiceImpl service, SysNoticeBo bo)
        throws Exception {
        Method method = SysNoticeServiceImpl.class.getDeclaredMethod("buildQueryWrapper", SysNoticeBo.class);
        method.setAccessible(true);
        return (LambdaQueryWrapper<SysNotice>) method.invoke(service, bo);
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
