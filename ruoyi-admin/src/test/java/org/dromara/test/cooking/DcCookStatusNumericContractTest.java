package org.dromara.test.cooking;

import org.dromara.system.domain.cooking.DcCookComplaintStatus;
import org.dromara.system.domain.cooking.DcCookChefStatus;
import org.dromara.system.domain.cooking.DcCookMessageStatus;
import org.dromara.system.domain.cooking.DcCookOrderStatus;
import org.dromara.system.domain.cooking.DcCookSettlementStatus;
import org.dromara.system.domain.cooking.DcCookSupportTicketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Cooking numeric status contract")
@Tag("dev")
class DcCookStatusNumericContractTest {

    @Test
    @DisplayName("shared cooking status constants use numeric storage values")
    void sharedCookingStatusConstantsUseNumericStorageValues() {
        assertEquals("0", DcCookComplaintStatus.PENDING);
        assertEquals("1", DcCookComplaintStatus.ESTABLISHED);
        assertEquals("2", DcCookComplaintStatus.REJECTED);

        assertEquals("0", DcCookSettlementStatus.GENERATED);
        assertEquals("1", DcCookSettlementStatus.REVIEWING);
        assertEquals("2", DcCookSettlementStatus.CONFIRMED);
        assertEquals("3", DcCookSettlementStatus.PAID);

        assertEquals("0", DcCookChefStatus.AUDIT_PENDING);
        assertEquals("1", DcCookChefStatus.AUDIT_APPROVED);
        assertEquals("2", DcCookChefStatus.AUDIT_REJECTED);
        assertEquals("0", DcCookChefStatus.NORMAL);
        assertEquals("1", DcCookChefStatus.PAUSED);
        assertEquals("2", DcCookChefStatus.DISABLED);
        assertEquals("3", DcCookChefStatus.RESIGNED);
        assertTrue(DcCookChefStatus.compatibleChefStatuses(DcCookChefStatus.NORMAL).contains("AVAILABLE"));

        assertEquals("0", DcCookMessageStatus.PENDING);
        assertEquals("1", DcCookMessageStatus.SENT);
        assertEquals("2", DcCookMessageStatus.FAILED);
        assertEquals("3", DcCookMessageStatus.SENDING);
        assertEquals("0", DcCookSupportTicketStatus.PENDING);
        assertEquals("1", DcCookSupportTicketStatus.REPLIED);
        assertEquals("2", DcCookSupportTicketStatus.CLOSED);

        List<String> orderStatuses = List.of(
            DcCookOrderStatus.WAITING_RESPONSE,
            DcCookOrderStatus.REJECTED_CLOSED,
            DcCookOrderStatus.RESPONSE_TIMEOUT_CLOSED,
            DcCookOrderStatus.WAITING_PAY,
            DcCookOrderStatus.PRICE_OBJECTION,
            DcCookOrderStatus.OBJECTION_TIMEOUT_CLOSED,
            DcCookOrderStatus.PAY_TIMEOUT_CLOSED,
            DcCookOrderStatus.WAITING_SERVICE,
            DcCookOrderStatus.WAITING_CONFIRM,
            DcCookOrderStatus.COMPLETED,
            DcCookOrderStatus.CANCELED,
            DcCookOrderStatus.REFUNDING,
            DcCookOrderStatus.REFUNDED,
            DcCookOrderStatus.REFUND_FAILED
        );
        assertEquals(14, orderStatuses.stream().distinct().count());
        assertTrue(orderStatuses.stream().allMatch(status -> status.matches("\\d+")));
    }

    @Test
    @DisplayName("init sql keeps complaint and settlement status defaults numeric")
    void initSqlKeepsComplaintAndSettlementStatusDefaultsNumeric() throws Exception {
        String sql = Files.readString(findRepoRoot().resolve("script/sql/demand_cooking.sql"));
        String complaintBlock = tableBlock(sql, "dc_cook_complaint");
        String settlementBlock = tableBlock(sql, "dc_cook_settlement");
        String messageBlock = tableBlock(sql, "dc_cook_message");
        String ticketBlock = tableBlock(sql, "dc_cook_support_ticket");

        assertTrue(complaintBlock.contains("DEFAULT '0' COMMENT '状态（0待处理 1成立 2不成立"));
        assertFalse(complaintBlock.contains("DEFAULT 'PENDING'"));

        assertTrue(settlementBlock.contains("DEFAULT '0' COMMENT '结算状态（0已生成/1复核中/2已确认/3已发放"));
        assertFalse(settlementBlock.contains("DEFAULT 'GENERATED'"));

        assertTrue(messageBlock.contains("DEFAULT '0' COMMENT '发送状态（0待发送 1已发送 2失败 3发送中"));
        assertFalse(messageBlock.contains("DEFAULT 'PENDING'"));

        assertTrue(ticketBlock.contains("DEFAULT '0' COMMENT '状态（0待处理 1已回复 2已关闭"));
        assertFalse(ticketBlock.contains("DEFAULT 'PENDING'"));
    }

    private Path findRepoRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("script/sql/demand_cooking.sql"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate demand_cooking.sql");
    }

    private String tableBlock(String sql, String tableName) {
        String marker = "CREATE TABLE IF NOT EXISTS " + tableName;
        int start = sql.indexOf(marker);
        assertTrue(start >= 0, "missing table " + tableName);
        int end = sql.indexOf(") ENGINE=InnoDB", start);
        assertTrue(end > start, "missing table end " + tableName);
        return sql.substring(start, end);
    }
}
