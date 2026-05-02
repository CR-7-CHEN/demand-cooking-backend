package org.dromara.system.domain.cooking;

import java.util.Arrays;
import java.util.List;

/**
 * Cooking order status constants.
 */
public final class DcCookOrderStatus {

    public static final String WAITING_RESPONSE = "WAITING_RESPONSE";
    public static final String REJECTED_CLOSED = "REJECTED_CLOSED";
    public static final String RESPONSE_TIMEOUT_CLOSED = "RESPONSE_TIMEOUT_CLOSED";
    public static final String WAITING_PAY = "WAITING_PAY";
    public static final String PRICE_OBJECTION = "PRICE_OBJECTION";
    public static final String OBJECTION_TIMEOUT_CLOSED = "OBJECTION_TIMEOUT_CLOSED";
    public static final String PAY_TIMEOUT_CLOSED = "PAY_TIMEOUT_CLOSED";
    public static final String WAITING_SERVICE = "WAITING_SERVICE";
    public static final String WAITING_CONFIRM = "WAITING_CONFIRM";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELED = "CANCELED";
    public static final String REFUNDING = "REFUNDING";
    public static final String REFUNDED = "REFUNDED";
    public static final String REFUND_FAILED = "REFUND_FAILED";

    public static final List<String> UNPAID_CANCELABLE = Arrays.asList(
        WAITING_RESPONSE, WAITING_PAY, PRICE_OBJECTION
    );

    public static final List<String> TERMINAL_STATUSES = Arrays.asList(
        REJECTED_CLOSED, RESPONSE_TIMEOUT_CLOSED, OBJECTION_TIMEOUT_CLOSED,
        PAY_TIMEOUT_CLOSED, COMPLETED, CANCELED, REFUNDED, REFUND_FAILED
    );

    public static final List<String> REFUND_EXCLUDED = Arrays.asList(
        CANCELED, REFUNDING, REFUNDED, REFUND_FAILED
    );

    public static final String CANCEL_USER_UNPAID = "USER_UNPAID";
    public static final String CANCEL_USER_PAID = "USER_PAID";
    public static final String CANCEL_CHEF = "CHEF";

    public static final String COMPLETE_BY_CHEF = "CHEF";
    public static final String COMPLETE_BY_SYSTEM = "SYSTEM";

    private DcCookOrderStatus() {
    }
}
