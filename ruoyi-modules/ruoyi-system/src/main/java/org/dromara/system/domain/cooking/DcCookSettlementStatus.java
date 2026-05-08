package org.dromara.system.domain.cooking;

/**
 * Settlement status constants.
 */
public final class DcCookSettlementStatus {

    public static final String GENERATED = "GENERATED";
    public static final String REVIEWING = "REVIEWING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String PAID = "PAID";
    public static final String PAID_OFFLINE = "PAID_OFFLINE";
    public static final String REVIEW_RESULT_KEEP = "KEEP";
    public static final String REVIEW_RESULT_REGENERATE = "REGENERATE";
    public static final String MANUAL = "MANUAL";

    private DcCookSettlementStatus() {
    }
}
