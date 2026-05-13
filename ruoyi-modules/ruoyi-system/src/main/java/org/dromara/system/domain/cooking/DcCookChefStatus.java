package org.dromara.system.domain.cooking;

import org.dromara.common.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cooking chef audit and availability status constants.
 */
public final class DcCookChefStatus {

    public static final String AUDIT_PENDING = "0";
    public static final String AUDIT_APPROVED = "1";
    public static final String AUDIT_REJECTED = "2";

    public static final String NORMAL = "0";
    public static final String PAUSED = "1";
    public static final String DISABLED = "2";
    public static final String RESIGNED = "3";

    private static final Map<String, String> LEGACY_AUDIT_TO_NUMERIC = Map.of(
        "PENDING", AUDIT_PENDING,
        "APPROVED", AUDIT_APPROVED,
        "REJECTED", AUDIT_REJECTED
    );

    private static final Map<String, List<String>> AUDIT_NUMERIC_TO_LEGACY = Map.of(
        AUDIT_PENDING, List.of("PENDING"),
        AUDIT_APPROVED, List.of("APPROVED"),
        AUDIT_REJECTED, List.of("REJECTED")
    );

    private static final Map<String, String> LEGACY_CHEF_TO_NUMERIC = Map.of(
        "APPROVED", NORMAL,
        "NORMAL", NORMAL,
        "PAUSED", PAUSED,
        "DISABLED", DISABLED,
        "RESIGNED", RESIGNED
    );

    private static final Map<String, List<String>> CHEF_NUMERIC_TO_LEGACY = Map.of(
        NORMAL, List.of("APPROVED", "NORMAL", "AVAILABLE"),
        PAUSED, List.of("PAUSED"),
        DISABLED, List.of("DISABLED"),
        RESIGNED, List.of("RESIGNED")
    );

    public static String normalizeAuditStatus(String status) {
        return normalize(status, LEGACY_AUDIT_TO_NUMERIC);
    }

    public static String normalizeChefStatus(String status) {
        return normalize(status, LEGACY_CHEF_TO_NUMERIC);
    }

    public static boolean matchesAuditStatus(String actualStatus, String expectedStatus) {
        return normalizeAuditStatus(expectedStatus).equals(normalizeAuditStatus(actualStatus));
    }

    public static boolean matchesChefStatus(String actualStatus, String expectedStatus) {
        return normalizeChefStatus(expectedStatus).equals(normalizeChefStatus(actualStatus));
    }

    public static boolean isValidAuditStatus(String status) {
        String normalized = normalizeAuditStatus(status);
        return AUDIT_PENDING.equals(normalized) || AUDIT_APPROVED.equals(normalized) || AUDIT_REJECTED.equals(normalized);
    }

    public static boolean isValidChefStatus(String status) {
        String normalized = normalizeChefStatus(status);
        return NORMAL.equals(normalized) || PAUSED.equals(normalized) || DISABLED.equals(normalized) || RESIGNED.equals(normalized);
    }

    public static List<String> compatibleAuditStatuses(String status) {
        return compatibleStatuses(normalizeAuditStatus(status), AUDIT_NUMERIC_TO_LEGACY);
    }

    public static List<String> compatibleChefStatuses(String status) {
        return compatibleStatuses(normalizeChefStatus(status), CHEF_NUMERIC_TO_LEGACY);
    }

    private static String normalize(String status, Map<String, String> legacyMap) {
        String normalized = StringUtils.trim(status);
        if (StringUtils.isBlank(normalized)) {
            return normalized;
        }
        return legacyMap.getOrDefault(normalized.toUpperCase(), normalized);
    }

    private static List<String> compatibleStatuses(String normalized, Map<String, List<String>> numericToLegacy) {
        if (StringUtils.isBlank(normalized)) {
            return List.of();
        }
        Set<String> values = new LinkedHashSet<>();
        values.add(normalized);
        List<String> legacyValues = numericToLegacy.get(normalized);
        if (legacyValues != null) {
            values.addAll(legacyValues);
        }
        return new ArrayList<>(values);
    }

    private DcCookChefStatus() {
    }
}
