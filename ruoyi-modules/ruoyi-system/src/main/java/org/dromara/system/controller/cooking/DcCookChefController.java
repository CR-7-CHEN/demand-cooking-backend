package org.dromara.system.controller.cooking;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.io.FileUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.HttpStatus;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.file.MimeTypeUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.dromara.system.domain.bo.cooking.DcCookChefBo;
import org.dromara.system.domain.bo.cooking.DcCookChefTimeBo;
import org.dromara.system.domain.vo.SysOssUploadVo;
import org.dromara.system.domain.vo.cooking.DcCookChefCommissionOrdersVo;
import org.dromara.system.domain.vo.cooking.DcCookChefWorkbenchVo;
import org.dromara.system.domain.vo.cooking.DcCookChefVo;
import org.dromara.system.domain.vo.cooking.DcCookChefTimeVo;
import org.dromara.system.service.ISysOssService;
import org.dromara.system.service.cooking.IDcCookChefService;
import org.dromara.system.service.cooking.IDcCookChefTimeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class DcCookChefController {

    private static final String CHEF_IMAGE_PATH = "/cooking/chef/image/";
    private static final String AUDIT_PENDING = "0";
    private static final Path CHEF_UPLOAD_DIR = Path.of(System.getProperty("user.dir"), "uploads", "cooking", "chef");

    private final IDcCookChefService chefService;
    private final IDcCookChefTimeService chefTimeService;
    private final ISysOssService ossService;

    @GetMapping("/cooking/chef/list")
    public TableDataInfo<DcCookChefVo> list(DcCookChefBo bo, PageQuery pageQuery) {
        return chefService.queryPageList(bo, pageQuery);
    }

    @GetMapping("/cooking/chef/{chefId}")
    public R<DcCookChefVo> getInfo(@PathVariable Long chefId) {
        return R.ok(chefService.queryById(chefId));
    }

    @PostMapping("/cooking/chef")
    public R<Void> add(@RequestBody DcCookChefBo bo) {
        return chefService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/chef")
    public R<Void> edit(@RequestBody DcCookChefBo bo) {
        return chefService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @DeleteMapping("/cooking/chef/{chefIds}")
    public R<Void> remove(@PathVariable Long[] chefIds) {
        return chefService.deleteWithValidByIds(Arrays.asList(chefIds), true) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/chef/audit")
    public R<Void> audit(@RequestBody DcCookChefBo bo) {
        return chefService.audit(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/chef/status")
    public R<Void> status(@RequestBody DcCookChefBo bo) {
        return chefService.changeStatus(bo) ? R.ok() : R.fail();
    }

    @GetMapping("/cooking/app/chef/list")
    public TableDataInfo<DcCookChefVo> appList(DcCookChefBo bo, PageQuery pageQuery) {
        return chefService.queryAppPageList(bo, pageQuery);
    }

    @GetMapping("/cooking/app/chef/{chefId}")
    public R<DcCookChefVo> appDetail(@PathVariable Long chefId) {
        return R.ok(chefService.queryDisplayById(chefId));
    }

    @GetMapping("/cooking/chef/my")
    public R<DcCookChefVo> my() {
        return R.ok(chefService.queryByUserId(LoginHelper.getUserId()));
    }

    @GetMapping("/cooking/chef/workbench")
    public R<DcCookChefWorkbenchVo> workbench() {
        return R.ok(chefService.queryWorkbench(LoginHelper.getUserId()));
    }

    @GetMapping("/cooking/chef/commission/orders")
    public R<DcCookChefCommissionOrdersVo> commissionOrders(@RequestParam(required = false) String month) {
        return R.ok(chefService.queryCommissionOrders(LoginHelper.getUserId(), month));
    }

    @PutMapping("/cooking/chef/my")
    public R<Void> updateMy(@RequestBody DcCookChefBo bo) {
        Long userId = LoginHelper.getUserId();
        DcCookChefVo mine = chefService.queryByUserId(userId);
        if (mine != null) {
            if (AUDIT_PENDING.equals(mine.getAuditStatus())) {
                throw new ServiceException("当前入驻申请正在审核中，请勿重复提交");
            }
            bo.setChefId(mine.getChefId());
        }
        bo.setUserId(userId);
        return chefService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @PostMapping("/cooking/chef/apply")
    public R<Void> apply(@RequestBody DcCookChefBo bo) {
        if (bo.getUserId() == null) {
            bo.setUserId(LoginHelper.getUserId());
        }
        return chefService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @PostMapping(value = "/cooking/chef/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysOssUploadVo> upload(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return R.fail("上传图片不能为空");
        }
        String extension = resolveImageExtension(file);
        if (StringUtils.isBlank(extension)) {
            return R.fail("文件格式不正确，请上传" + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + "格式");
        }
        return R.ok(uploadLocalImage(file, extension, request));
    }

    @SaIgnore
    @GetMapping(CHEF_IMAGE_PATH + "{fileName:.+}")
    public ResponseEntity<Resource> image(@PathVariable String fileName) {
        Path imagePath = CHEF_UPLOAD_DIR.resolve(fileName).normalize();
        if (!imagePath.startsWith(CHEF_UPLOAD_DIR) || !Files.isRegularFile(imagePath)) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String contentType = Files.probeContentType(imagePath);
            if (StringUtils.isNotBlank(contentType)) {
                mediaType = MediaType.parseMediaType(contentType);
            }
        } catch (IOException ignored) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
            .contentType(mediaType)
            .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
            .body(new FileSystemResource(imagePath));
    }

    private SysOssUploadVo uploadLocalImage(MultipartFile file, String extension, HttpServletRequest request) {
        try {
            Files.createDirectories(CHEF_UPLOAD_DIR);
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path target = CHEF_UPLOAD_DIR.resolve(fileName).normalize();
            if (!target.startsWith(CHEF_UPLOAD_DIR)) {
                throw new ServiceException("invalid upload path");
            }
            file.transferTo(target.toFile());
            String originalName = StringUtils.isNotBlank(file.getOriginalFilename()) ? file.getOriginalFilename() : fileName;
            String url = buildRequestBaseUrl(request) + CHEF_IMAGE_PATH + fileName;
            SysOssUploadVo uploadVo = new SysOssUploadVo();
            uploadVo.setUrl(url);
            uploadVo.setFileName(originalName);
            uploadVo.setOssId("");
            return uploadVo;
        } catch (IOException e) {
            throw new ServiceException("upload image failed: " + e.getMessage());
        }
    }

    private String buildRequestBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && serverPort == 80)
            || ("https".equalsIgnoreCase(scheme) && serverPort == 443);
        return scheme + "://" + serverName + (defaultPort ? "" : ":" + serverPort)
            + (StringUtils.isBlank(contextPath) ? "" : contextPath);
    }

    private R<SysOssUploadVo> uploadOss(MultipartFile file, String extension) {
        var oss = ossService.upload(normalizeImageFilename(file, extension));
        SysOssUploadVo uploadVo = new SysOssUploadVo();
        uploadVo.setUrl(oss.getUrl());
        uploadVo.setFileName(oss.getOriginalName());
        uploadVo.setOssId(oss.getOssId().toString());
        return R.ok(uploadVo);
    }

    @PostMapping("/cooking/chef/pause")
    public R<Void> pause() {
        return chefService.pause(LoginHelper.getUserId()) ? R.ok() : R.fail();
    }

    @PostMapping("/cooking/chef/resume")
    public R<Void> resume() {
        return chefService.resume(LoginHelper.getUserId()) ? R.ok() : R.fail();
    }

    @PostMapping("/cooking/chef/resign")
    public R<Void> resign(@RequestBody(required = false) DcCookChefBo bo) {
        String resignReason = bo == null ? null : bo.getResignReason();
        return chefService.resign(LoginHelper.getUserId(), resignReason) ? R.ok() : R.fail();
    }

    @GetMapping("/cooking/chef/time")
    public R<List<DcCookChefTimeVo>> timeList(DcCookChefTimeBo bo) {
        DcCookChefVo chef = requireCurrentChef();
        bo.setChefId(chef.getChefId());
        return R.ok(chefTimeService.queryList(bo));
    }

    @PostMapping("/cooking/chef/time")
    public R<Void> addTime(@RequestBody DcCookChefTimeBo bo) {
        DcCookChefVo chef = requireCurrentChef();
        bo.setChefId(chef.getChefId());
        return chefTimeService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @PutMapping("/cooking/chef/time")
    public R<Void> updateTime(@RequestBody DcCookChefTimeBo bo) {
        DcCookChefVo chef = requireCurrentChef();
        bo.setChefId(chef.getChefId());
        return chefTimeService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @DeleteMapping("/cooking/chef/time")
    public R<Void> deleteTime(@RequestParam(value = "id", required = false) Long timeId,
                              @RequestBody(required = false) Map<String, Long> body) {
        if (timeId == null && body != null) {
            timeId = body.get("timeId");
            if (timeId == null) {
                timeId = body.get("id");
            }
        }
        DcCookChefVo chef = requireCurrentChef();
        return chefTimeService.deleteById(timeId, chef.getChefId()) ? R.ok() : R.fail();
    }

    private DcCookChefVo requireCurrentChef() {
        DcCookChefVo chef = chefService.queryByUserId(LoginHelper.getUserId());
        if (chef == null) {
            throw new ServiceException("请先申请成为服务厨师", HttpStatus.FORBIDDEN);
        }
        return chef;
    }

    private String resolveImageExtension(MultipartFile file) {
        String extension = FileUtil.extName(file.getOriginalFilename());
        if (StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION)) {
            return extension.toLowerCase(Locale.ROOT);
        }
        String contentType = file.getContentType();
        if (StringUtils.equalsAnyIgnoreCase(contentType, MimeTypeUtils.IMAGE_PNG)) {
            return "png";
        }
        if (StringUtils.equalsAnyIgnoreCase(contentType, MimeTypeUtils.IMAGE_JPG, MimeTypeUtils.IMAGE_JPEG)) {
            return "jpg";
        }
        if (StringUtils.equalsAnyIgnoreCase(contentType, MimeTypeUtils.IMAGE_GIF)) {
            return "gif";
        }
        if (StringUtils.equalsAnyIgnoreCase(contentType, MimeTypeUtils.IMAGE_BMP)) {
            return "bmp";
        }
        return detectImageExtension(file);
    }

    private String detectImageExtension(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(8);
            if (header.length >= 8
                && (header[0] & 0xFF) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') {
                return "png";
            }
            if (header.length >= 3
                && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return "jpg";
            }
            if (header.length >= 6
                && header[0] == 'G' && header[1] == 'I' && header[2] == 'F') {
                return "gif";
            }
            if (header.length >= 2 && header[0] == 'B' && header[1] == 'M') {
                return "bmp";
            }
        } catch (IOException ignored) {
            return "";
        }
        return "";
    }

    private MultipartFile normalizeImageFilename(MultipartFile file, String extension) {
        if (StringUtils.isNotBlank(FileUtil.extName(file.getOriginalFilename()))) {
            return file;
        }
        String filename = "chef-image." + extension;
        return new MultipartFile() {
            @Override
            public String getName() {
                return file.getName();
            }

            @Override
            public String getOriginalFilename() {
                return filename;
            }

            @Override
            public String getContentType() {
                return file.getContentType();
            }

            @Override
            public boolean isEmpty() {
                return file.isEmpty();
            }

            @Override
            public long getSize() {
                return file.getSize();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return file.getBytes();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return file.getInputStream();
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                file.transferTo(dest);
            }
        };
    }
}
