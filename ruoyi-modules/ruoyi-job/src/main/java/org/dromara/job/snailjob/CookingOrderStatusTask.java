package org.dromara.job.snailjob;

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.model.dto.ExecuteResult;
import lombok.RequiredArgsConstructor;
import org.dromara.system.service.cooking.IDcCookOrderStatusService;
import org.springframework.stereotype.Component;

/**
 * Scheduled cooking order status processor.
 */
@RequiredArgsConstructor
@Component
@JobExecutor(name = "cookingOrderStatusTask")
public class CookingOrderStatusTask {

    private final IDcCookOrderStatusService orderStatusService;

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        int processed = orderStatusService.processScheduledStatusTransitions();
        SnailJobLog.REMOTE.info("cookingOrderStatusTask processed {} orders", processed);
        return ExecuteResult.success(processed);
    }
}
