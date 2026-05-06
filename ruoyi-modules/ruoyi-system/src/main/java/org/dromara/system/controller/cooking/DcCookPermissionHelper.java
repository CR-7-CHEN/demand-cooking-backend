package org.dromara.system.controller.cooking;

import java.util.Objects;

final class DcCookPermissionHelper {

    private DcCookPermissionHelper() {
    }

    static boolean ownsOrder(Long loginUserId, Long orderUserId) {
        return loginUserId != null && Objects.equals(loginUserId, orderUserId);
    }

    static boolean servesOrder(Long loginChefId, Long orderChefId) {
        return loginChefId != null && Objects.equals(loginChefId, orderChefId);
    }
}
