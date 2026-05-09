package org.dromara.job.snailjob;

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.model.dto.ExecuteResult;
import lombok.RequiredArgsConstructor;
import org.dromara.system.service.cooking.IDcCookSettlementService;
import org.springframework.stereotype.Component;

/**
 * Scheduled cooking monthly settlement generator.
 */
@RequiredArgsConstructor
@Component
@JobExecutor(name = "cookingMonthlySettlementTask")
public class CookingMonthlySettlementTask {

    private final IDcCookSettlementService settlementService;

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        int generated = settlementService.generatePreviousMonthSettlements();
        SnailJobLog.REMOTE.info("cookingMonthlySettlementTask generated {} settlements", generated);
        return ExecuteResult.success(generated);
    }
}
