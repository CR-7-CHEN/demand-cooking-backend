package org.dromara.test.cooking;

import org.dromara.common.core.domain.R;
import org.dromara.system.controller.cooking.DcCookChefController;
import org.dromara.system.domain.vo.SysOssUploadVo;
import org.dromara.system.service.ISysOssService;
import org.dromara.system.service.cooking.IDcCookChefService;
import org.dromara.system.service.cooking.IDcCookChefTimeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("Cooking chef upload")
@Tag("dev")
public class DcCookChefUploadTest {

    @Test
    @DisplayName("stores no-extension mini-program avatar locally and returns image url")
    void uploadAvatarWithoutExtensionReturnsLocalImageUrl() throws Exception {
        DcCookChefController controller = new DcCookChefController(
            mock(IDcCookChefService.class),
            mock(IDcCookChefTimeService.class),
            mock(ISysOssService.class)
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "wxfile",
            "image/png",
            new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10}
        );

        R<SysOssUploadVo> result = controller.upload(file, request);

        assertEquals(200, result.getCode());
        String url = result.getData().getUrl();
        assertTrue(url.startsWith("http://localhost:8080/cooking/chef/image/"));
        assertTrue(url.endsWith(".png"));

        String fileName = url.substring(url.lastIndexOf('/') + 1);
        Path localFile = Path.of(System.getProperty("user.dir"), "uploads", "cooking", "chef", fileName);
        assertTrue(Files.exists(localFile));
        Files.deleteIfExists(localFile);
        assertFalse(Files.exists(localFile));
    }
}
