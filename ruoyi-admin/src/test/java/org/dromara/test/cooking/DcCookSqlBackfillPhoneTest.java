package org.dromara.test.cooking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Cooking SQL phone backfill")
@Tag("dev")
class DcCookSqlBackfillPhoneTest {

    @Test
    @DisplayName("demand cooking sql backfills chef mobile to user phone")
    void demandCookingSqlBackfillsChefMobileToUserPhone() throws Exception {
        String sql = Files.readString(Path.of("..", "script", "sql", "demand_cooking.sql").normalize());
        assertTrue(sql.contains("UPDATE sys_user u"));
        assertTrue(sql.contains("JOIN dc_cook_chef c"));
        assertTrue(sql.contains("u.phonenumber = c.mobile"));
    }
}
