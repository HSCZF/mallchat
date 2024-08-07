package com.hs.mallchat.oss;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.hs.mallchat.oss.domain.OssReq;
import com.hs.mallchat.oss.domain.OssResp;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Description:
 * 其实这里应该是一个接口，因为太多方法了
 * @Author: CZF
 * @Create: 2024/8/7 - 16:20
 */
@Slf4j
@AllArgsConstructor
public class MinIOTemplate {

    /**
     * MinIO 客户端
     */
    MinioClient minioClient;

    /**
     * MinIO 配置类
     */
    OssProperties ossProperties;

    /**
     * 查询所有存储桶
     *
     * @return Bucket 集合
     */
    @SneakyThrows
    public List<Bucket> listBuckets() {
        return minioClient.listBuckets();
    }

    /**
     * 桶是否存在
     *
     * @param bucketName 桶名
     * @return 是否存在
     */
    @SneakyThrows
    public Boolean bucketExists(String bucketName) {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 创建存储桶
     *
     * @param bucketName 桶名
     */
    @SneakyThrows
    public void makeBucket(String bucketName) {
        if (!bucketExists(bucketName)) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 删除一个空桶 如果存储桶存在对象不为空时，删除会报错。
     *
     * @param bucketName 桶名
     */
    @SneakyThrows
    public void removeBucket(String bucketName) {
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    /**
     * 获取带签名的临时上传元数据对象，前端可获取后，直接上传到Minio
     *
     * @param bucketName
     * @param fileName
     * @return
     */
    @SneakyThrows
    public Map<String, String> getPreSignedPostFormData(String bucketName, String fileName){
        // 为存储桶创建一个上传策略，过期时间为7天
        PostPolicy policy = new PostPolicy(bucketName, ZonedDateTime.now().plusDays(7));
        // 设置一个参数key，值为上传对象的名称
        policy.addEqualsCondition("key", fileName);
        // 添加Content-Type以"image/"开头，表示只能上传照片
        policy.addStartsWithCondition("Content-Type", "image/");
        // 设置上传文件的大小 64kiB to 10MiB.
        policy.addContentLengthRangeCondition(64 * 1024, 10 * 1024 * 1024);
        return minioClient.getPresignedPostFormData(policy);
    }

    /**
     * 返回临时带签名、过期时间一天、PUT请求方式的访问URL
     */
    @SneakyThrows
    public OssResp getPreSignedObjectUrl(OssReq req) {
        // 根据请求是否启用自动路径生成规则，构建文件的绝对路径
        String absolutePath = req.isAutoPath() ? generateAutoPath(req) : req.getFilePath() + StrUtil.SLASH + req.getFileName();

        // 生成用于上传文件的预签名URL
        // 这里的参数包括HTTP方法、存储桶名称、目标文件路径和URL过期时间（设置为24小时）
        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(ossProperties.getBucketName())
                        .object(absolutePath)
                        .expiry(60 * 60 * 24)
                        .build());

        // 构建并返回包含上传和下载URL的响应对象
        return OssResp.builder()
                .uploadUrl(url)
                .downloadUrl(getDownloadUrl(ossProperties.getBucketName(), absolutePath))
                .build();
    }

    private String getDownloadUrl(String bucket, String pathFile) {
        return ossProperties.getEndpoint() + StrUtil.SLASH + bucket + pathFile;
    }

    /**
     * 生成随机文件名，防止重复
     *
     * @return
     */
    private String generateAutoPath(OssReq req) {
        String uid = Optional.ofNullable(req.getUid()).map(String::valueOf).orElse("00000");
        UUID uuid = UUID.fastUUID();
        String suffix = FileNameUtil.getSuffix(req.getFileName());
        String yearAndMonth = DateUtil.format(new Date(), DatePattern.NORM_MONTH_PATTERN);
        return req.getFilePath() + StrUtil.SLASH + yearAndMonth + StrUtil.SLASH + uid + StrUtil.SLASH + uuid + StrUtil.DOT + suffix;
    }

    /**
     * GetObject接口用于获取某个文件（Object）。此操作需要对此Object具有读权限。
     *
     * @param bucketName  桶名
     * @param ossFilePath Oss文件路径
     */
    @SneakyThrows
    public InputStream getObject(String bucketName, String ossFilePath) {
        return minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(ossFilePath).build());
    }

    /**
     * 查询桶的对象信息
     *
     * @param bucketName 桶名
     * @param recursive  是否递归查询
     * @return
     */
    @SneakyThrows
    public Iterable<Result<Item>> listObjects(String bucketName, boolean recursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).recursive(recursive).build());
    }

}
