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

    public static final String USER_TAB_RESERVED = "reserved";
    public static final String USER_TAB_PAYMENT = "payment";
    public static final String USER_TAB_SERVING = "serving";
    public static final String USER_TAB_COMPLETED = "completed";
    public static final String USER_TAB_CLOSED = "closed";

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

    public static final List<String> USER_TAB_RESERVED_STATUSES = Arrays.asList(
        WAITING_RESPONSE
    );

    public static final List<String> USER_TAB_PAYMENT_STATUSES = Arrays.asList(
        WAITING_PAY, PRICE_OBJECTION
    );

    public static final List<String> USER_TAB_SERVING_STATUSES = Arrays.asList(
        WAITING_SERVICE, WAITING_CONFIRM
    );

    public static final List<String> USER_TAB_COMPLETED_STATUSES = Arrays.asList(
        COMPLETED
    );

    public static final List<String> USER_TAB_CLOSED_STATUSES = Arrays.asList(
        REJECTED_CLOSED, RESPONSE_TIMEOUT_CLOSED, OBJECTION_TIMEOUT_CLOSED,
        PAY_TIMEOUT_CLOSED, CANCELED, REFUNDING, REFUNDED, REFUND_FAILED
    );

    public static final String CANCEL_USER_UNPAID = "USER_UNPAID";
    public static final String CANCEL_USER_PAID = "USER_PAID";
    public static final String CANCEL_CHEF = "CHEF";

    public static final String COMPLETE_BY_CHEF = "CHEF";
    public static final String COMPLETE_BY_SYSTEM = "SYSTEM";

    public static List<String> statusesForUserTab(String tab) {
        if (USER_TAB_RESERVED.equals(tab)) {
            return USER_TAB_RESERVED_STATUSES;
        }
        if (USER_TAB_PAYMENT.equals(tab)) {
            return USER_TAB_PAYMENT_STATUSES;
        }
        if (USER_TAB_SERVING.equals(tab)) {
            return USER_TAB_SERVING_STATUSES;
        }
        if (USER_TAB_COMPLETED.equals(tab)) {
            return USER_TAB_COMPLETED_STATUSES;
        }
        if (USER_TAB_CLOSED.equals(tab)) {
            return USER_TAB_CLOSED_STATUSES;
        }
        return List.of();
    }

    private DcCookOrderStatus() {
    }
}
