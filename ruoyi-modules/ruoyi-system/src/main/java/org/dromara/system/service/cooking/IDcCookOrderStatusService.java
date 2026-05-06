package org.dromara.system.service.cooking;

public interface IDcCookOrderStatusService {

    /**
     * Process scheduled cooking order status transitions.
     *
     * @return processed order count
     */
    int processScheduledStatusTransitions();
}
