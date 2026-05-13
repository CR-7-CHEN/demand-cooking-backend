package org.dromara.system.domain.cooking;

import org.dromara.common.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Complaint status constants.
 */
public final class DcCookComplaintStatus {

    public static final String PENDING = "0";
    public static final String ESTABLISHED = "1";
    public static final String REJECTED = "2";
    public static final String LEGACY_PENDING = "PENDING";
    public static final String LEGACY_ESTABLISHED = "ESTABLISHED";
    public static final String LEGACY_REJECTED = "REJECTED";

    private static final Map<String, String> LEGACY_TO_NUMERIC = Map.of(
        LEGACY_PENDING, PENDING,
        LEGACY_ESTABLISHED, ESTABLISHED,
        LEGACY_REJECTED, REJECTED
    );

    private static final Map<String, String> NUMERIC_TO_LEGACY = Map.of(
        PENDING, LEGACY_PENDING,
        ESTABLISHED, LEGACY_ESTABLISHED,
        REJECTED, LEGACY_REJECTED
    );

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

    public static String[] legacyValues(String status) {
        return compatibleStatuses(status).toArray(String[]::new);
    }

    private DcCookComplaintStatus() {
    }
}
