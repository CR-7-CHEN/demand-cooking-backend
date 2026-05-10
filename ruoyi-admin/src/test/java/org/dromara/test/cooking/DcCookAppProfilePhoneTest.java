package org.dromara.test.cooking;

import org.dromara.system.domain.bo.cooking.DcCookAppProfileBo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Cooking app profile phone")
@Tag("dev")
class DcCookAppProfilePhoneTest {

    @Test
    @DisplayName("app profile payload supports phonenumber")
    void appProfilePayloadSupportsPhonenumber() throws Exception {
        assertNotNull(DcCookAppProfileBo.class.getDeclaredField("phonenumber"));
        assertNotNull(DcCookAppProfileBo.class.getMethod("getPhonenumber"));
        assertNotNull(DcCookAppProfileBo.class.getMethod("setPhonenumber", String.class));
    }
}
