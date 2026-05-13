package org.dromara.system.domain.cooking;

import org.dromara.common.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cooking order status constants.
 */
public final class DcCookOrderStatus {

    public static final String WAITING_RESPONSE = "0";
    public static final String WAITING_PAY = "1";
    public static final String PRICE_OBJECTION = "2";
    public static final String WAITING_SERVICE = "3";
    public static final String WAITING_CONFIRM = "4";
    public static final String COMPLETED = "5";
    public static final String REJECTED_CLOSED = "6";
    public static final String RESPONSE_TIMEOUT_CLOSED = "7";
    public static final String OBJECTION_TIMEOUT_CLOSED = "8";
    public static final String PAY_TIMEOUT_CLOSED = "9";
    public static final String CANCELED = "10";
    public static final String REFUNDING = "11";
    public static final String REFUNDED = "12";
    public static final String REFUND_FAILED = "13";

    public static final String LEGACY_WAITING_RESPONSE = "WAITING_RESPONSE";
    public static final String LEGACY_WAITING_PAY = "WAITING_PAY";
    public static final String LEGACY_PRICE_OBJECTION = "PRICE_OBJECTION";
    public static final String LEGACY_WAITING_SERVICE = "WAITING_SERVICE";
    public static final String LEGACY_WAITING_CONFIRM = "WAITING_CONFIRM";
    public static final String LEGACY_COMPLETED = "COMPLETED";
    public static final String LEGACY_REJECTED_CLOSED = "REJECTED_CLOSED";
    public static final String LEGACY_RESPONSE_TIMEOUT_CLOSED = "RESPONSE_TIMEOUT_CLOSED";
    public static final String LEGACY_OBJECTION_TIMEOUT_CLOSED = "OBJECTION_TIMEOUT_CLOSED";
    public static final String LEGACY_PAY_TIMEOUT_CLOSED = "PAY_TIMEOUT_CLOSED";
    public static final String LEGACY_CANCELED = "CANCELED";
    public static final String LEGACY_REFUNDING = "REFUNDING";
    public static final String LEGACY_REFUNDED = "REFUNDED";
    public static final String LEGACY_REFUND_FAILED = "REFUND_FAILED";

    private static final Map<String, String> LEGACY_TO_NUMERIC = Map.ofEntries(
        Map.entry(LEGACY_WAITING_RESPONSE, WAITING_RESPONSE),
        Map.entry(LEGACY_WAITING_PAY, WAITING_PAY),
        Map.entry(LEGACY_PRICE_OBJECTION, PRICE_OBJECTION),
        Map.entry(LEGACY_WAITING_SERVICE, WAITING_SERVICE),
        Map.entry(LEGACY_WAITING_CONFIRM, WAITING_CONFIRM),
        Map.entry(LEGACY_COMPLETED, COMPLETED),
        Map.entry(LEGACY_REJECTED_CLOSED, REJECTED_CLOSED),
        Map.entry(LEGACY_RESPONSE_TIMEOUT_CLOSED, RESPONSE_TIMEOUT_CLOSED),
        Map.entry(LEGACY_OBJECTION_TIMEOUT_CLOSED, OBJECTION_TIMEOUT_CLOSED),
        Map.entry(LEGACY_PAY_TIMEOUT_CLOSED, PAY_TIMEOUT_CLOSED),
        Map.entry(LEGACY_CANCELED, CANCELED),
        Map.entry(LEGACY_REFUNDING, REFUNDING),
        Map.entry(LEGACY_REFUNDED, REFUNDED),
        Map.entry(LEGACY_REFUND_FAILED, REFUND_FAILED)
    );

    private static final Map<String, String> NUMERIC_TO_LEGACY = Map.ofEntries(
        Map.entry(WAITING_RESPONSE, LEGACY_WAITING_RESPONSE),
        Map.entry(WAITING_PAY, LEGACY_WAITING_PAY),
        Map.entry(PRICE_OBJECTION, LEGACY_PRICE_OBJECTION),
        Map.entry(WAITING_SERVICE, LEGACY_WAITING_SERVICE),
        Map.entry(WAITING_CONFIRM, LEGACY_WAITING_CONFIRM),
        Map.entry(COMPLETED, LEGACY_COMPLETED),
        Map.entry(REJECTED_CLOSED, LEGACY_REJECTED_CLOSED),
        Map.entry(RESPONSE_TIMEOUT_CLOSED, LEGACY_RESPONSE_TIMEOUT_CLOSED),
        Map.entry(OBJECTION_TIMEOUT_CLOSED, LEGACY_OBJECTION_TIMEOUT_CLOSED),
        Map.entry(PAY_TIMEOUT_CLOSED, LEGACY_PAY_TIMEOUT_CLOSED),
        Map.entry(CANCELED, LEGACY_CANCELED),
        Map.entry(REFUNDING, LEGACY_REFUNDING),
        Map.entry(REFUNDED, LEGACY_REFUNDED),
        Map.entry(REFUND_FAILED, LEGACY_REFUND_FAILED)
    );

    public static final String USER_TAB_RESERVED = "reserved";
    public static final String USER_TAB_PAYMENT = "payment";
    public static final String USER_TAB_PAID = "paid";
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

    public static final List<String> USER_TAB_PAID_STATUSES = Arrays.asList(
        WAITING_SERVICE
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
        if (USER_TAB_PAID.equals(tab)) {
            return USER_TAB_PAID_STATUSES;
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

    public static String normalize(String status) {
        String normalized = StringUtils.trim(status);
        if (StringUtils.isBlank(normalized)) {
            return normalized;
        }
        return LEGACY_TO_NUMERIC.getOrDefault(normalized.toUpperCase(), normalized);
    }

    public static boolean matches(String actualStatus, String expectedStatus) {
        return normalize(expectedStatus).equals(normalize(actualStatus));
    }

    public static List<String> compatibleStatuses(String status) {
        String normalized = normalize(status);
        if (StringUtils.isBlank(normalized)) {
            return List.of();
        }
        Set<String> values = new LinkedHashSet<>();
        values.add(normalized);
        String legacy = NUMERIC_TO_LEGACY.get(normalized);
        if (legacy != null) {
            values.add(legacy);
        }
        return new ArrayList<>(values);
    }

    public static List<String> compatibleStatuses(List<String> statuses) {
        Set<String> values = new LinkedHashSet<>();
        for (String status : statuses) {
            values.addAll(compatibleStatuses(status));
        }
        return new ArrayList<>(values);
    }

    private DcCookOrderStatus() {
    }
}
