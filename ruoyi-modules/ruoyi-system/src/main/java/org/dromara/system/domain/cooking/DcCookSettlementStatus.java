package org.dromara.system.domain.cooking;

import org.dromara.common.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Settlement status constants.
 */
public final class DcCookSettlementStatus {

    public static final String GENERATED = "0";
    public static final String REVIEWING = "1";
    public static final String CONFIRMED = "2";
    public static final String PAID = "3";
    public static final String LEGACY_GENERATED = "GENERATED";
    public static final String LEGACY_REVIEWING = "REVIEWING";
    public static final String LEGACY_CONFIRMED = "CONFIRMED";
    public static final String LEGACY_PAID = "PAID";
    public static final String PAID_OFFLINE = "PAID_OFFLINE";
    public static final String REVIEW_RESULT_KEEP = "KEEP";
    public static final String REVIEW_RESULT_REGENERATE = "REGENERATE";
    public static final String MANUAL = "MANUAL";

    private static final Map<String, String> LEGACY_TO_NUMERIC = Map.of(
        LEGACY_GENERATED, GENERATED,
        LEGACY_REVIEWING, REVIEWING,
        LEGACY_CONFIRMED, CONFIRMED,
        LEGACY_PAID, PAID,
        PAID_OFFLINE, PAID,
        MANUAL, GENERATED
    );

    private static final Map<String, String> NUMERIC_TO_LEGACY = Map.of(
        GENERATED, LEGACY_GENERATED,
        REVIEWING, LEGACY_REVIEWING,
        CONFIRMED, LEGACY_CONFIRMED,
        PAID, LEGACY_PAID
    );

    public static String normalize(String status) {
        String normalized = StringUtils.trim(status);
        if (StringUtils.isBlank(normalized)) {
            return GENERATED;
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
        if (PAID.equals(normalized)) {
            values.add(PAID_OFFLINE);
        }
        if (GENERATED.equals(normalized)) {
            values.add(MANUAL);
        }
        return new ArrayList<>(values);
    }

    private DcCookSettlementStatus() {
    }
}
