package org.dromara.system.controller.cooking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DcCookPermissionHelperTest {

    @Test
    void ownsOrderRequiresSameUserId() {
        assertThat(DcCookPermissionHelper.ownsOrder(10L, 10L)).isTrue();
        assertThat(DcCookPermissionHelper.ownsOrder(10L, 11L)).isFalse();
        assertThat(DcCookPermissionHelper.ownsOrder(null, 10L)).isFalse();
        assertThat(DcCookPermissionHelper.ownsOrder(10L, null)).isFalse();
    }

    @Test
    void servesOrderRequiresSameChefId() {
        assertThat(DcCookPermissionHelper.servesOrder(20L, 20L)).isTrue();
        assertThat(DcCookPermissionHelper.servesOrder(20L, 21L)).isFalse();
        assertThat(DcCookPermissionHelper.servesOrder(null, 20L)).isFalse();
        assertThat(DcCookPermissionHelper.servesOrder(20L, null)).isFalse();
    }
}
