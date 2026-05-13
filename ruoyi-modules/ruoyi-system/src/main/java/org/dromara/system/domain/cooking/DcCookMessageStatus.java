package org.dromara.system.domain.cooking;

import org.dromara.common.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cooking message send status constants.
 */
public final class DcCookMessageStatus {

    public static final String PENDING = "0";
    public static final String SENT = "1";
    public static final String FAILED = "2";
    public static final String SENDING = "3";

    private static final Map<String, String> LEGACY_TO_NUMERIC = Map.of(
        "PENDING", PENDING,
        "SENT", SENT,
        "SUCCESS", SENT,
        "FAILED", FAILED,
        "SENDING", SENDING
    );

    private static final Map<String, List<String>> NUMERIC_TO_LEGACY = Map.of(
        PENDING, List.of("PENDING"),
        SENT, List.of("SENT", "SUCCESS"),
        FAILED, List.of("FAILED"),
        SENDING, List.of("SENDING")
    );

    public static String normalize(String status) {
        String normalized = StringUtils.trim(status);
        if (StringUtils.isBlank(normalized)) {
            return normalized;
        }
        return LEGACY_TO_NUMERIC.getOrDefault(normalized.toUpperCase(), normalized);
    }

    public static List<String> compatibleStatuses(String status) {
        String normalized = normalize(status);
        if (StringUtils.isBlank(normalized)) {
            return List.of();
        }
        Set<String> values = new LinkedHashSet<>();
        values.add(normalized);
        List<String> legacyValues = NUMERIC_TO_LEGACY.get(normalized);
        if (legacyValues != null) {
            values.addAll(legacyValues);
        }
        return new ArrayList<>(values);
    }

    private DcCookMessageStatus() {
    }
}
