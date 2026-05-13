package org.dromara.system.domain.cooking;

import org.dromara.common.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cooking support ticket status constants.
 */
public final class DcCookSupportTicketStatus {

    public static final String PENDING = "0";
    public static final String REPLIED = "1";
    public static final String CLOSED = "2";

    private static final Map<String, String> LEGACY_TO_NUMERIC = Map.of(
        "PENDING", PENDING,
        "REPLIED", REPLIED,
        "CLOSED", CLOSED
    );

    private static final Map<String, List<String>> NUMERIC_TO_LEGACY = Map.of(
        PENDING, List.of("PENDING"),
        REPLIED, List.of("REPLIED"),
        CLOSED, List.of("CLOSED")
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

    private DcCookSupportTicketStatus() {
    }
}
