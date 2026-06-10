package com.mmva.newsapp.domain.news.service.media;

import com.mmva.newsapp.domain.news.dto.core.ImageProcessingResponseDto;
import com.mmva.newsapp.domain.news.dto.core.ThumbnailResponseDto;
import com.mmva.newsapp.domain.news.dto.core.ThumbnailResponseDto.ThumbnailSource;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.coobird.thumbnailator.Thumbnails;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of ThumbnailService.
 * 
 * <h3>Features:</h3>
 * <ul>
 * <li>Custom thumbnail upload</li>
 * <li>Image resize using Thumbnailator</li>
 * <li>Video frame extraction using Jcodec</li>
 * <li>Consistent naming: {sanitized-name}_{uuid}_thumb.jpg</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThumbnailServiceImpl implements ThumbnailService {

    // ========================================
    // Constants
    // ========================================

    private static final String THUMBNAIL_SUFFIX = "_thumb.jpg";
    private static final String THUMBNAIL_CONTENT_TYPE = "image/jpeg";
    private static final int MAX_FILENAME_LENGTH = 50;

    // Supported image formats (via Java ImageIO + TwelveMonkeys extensions)
    // Note: AVIF removed - requires native libraries not included in standard Java
    // ImageIO
    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp");

    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo",
            "video/x-ms-wmv", "video/webm", "video/ogg");

    // ========================================
    // Dependencies
    // ========================================

    private final MediaUrlService mediaUrlService;
    private final NewsImageProcessingService newsImageProcessingService;

    // ========================================
    // Configuration
    // ========================================

    @Value("${media.entities.news.thumbnails:${media.root-path}/processed/news/thumbnails}")
    private String thumbnailStoragePath;

    // ========================================
    // Custom Thumbnail Upload
    // ========================================

    /**
     * Maximum file size for custom thumbnail uploads (5 MB).
     * Images larger than this should be compressed client-side first.
     */
    private static final long MAX_THUMBNAIL_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    @Override
    public ThumbnailResponseDto saveCustomThumbnail(MultipartFile file, String originalMediaName) throws IOException {
        log.info("ThumbnailService: Processing custom thumbnail for media: {}, uploadSize={}KB",
                originalMediaName, file.getSize() / 1024);

        // Validate file type and size
        validateCustomThumbnailFile(file);

        // Delegate to NewsImageProcessingService to generate all sizes
        var imageResponse = newsImageProcessingService.processImage(file, originalMediaName);

        log.info("ThumbnailService: Custom thumbnail processed via NewsImageProcessingService - filename={}",
                imageResponse.getThumbnail().getFilename());

        // Return thumbnail variant for backward compatibility
        var thumbnailVariant = imageResponse.getThumbnail();
        return ThumbnailResponseDto.builder()
                .filename(thumbnailVariant.getFilename())
                .url(thumbnailVariant.getUrl())
                .filePath(thumbnailVariant.getFilePath())
                .originalFilename(originalMediaName)
                .fileSize(thumbnailVariant.getFileSize())
                .source(ThumbnailSource.CUSTOM_UPLOAD)
                .build();
    }

    /**
     * Validates custom thumbnail file for type and size constraints.
     * 
     * @param file the uploaded thumbnail file
     * @throws InvalidRequestException if validation fails
     */
    private void validateCustomThumbnailFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("thumbnailFile", "Thumbnail image file is required");
        }

        // Validate file type
        if (!isImage(file.getContentType())) {
            throw new InvalidRequestException("thumbnailFile",
                    String.format("Invalid thumbnail file type: '%s'. Supported formats: JPEG, PNG, GIF, WebP, BMP",
                            file.getContentType()));
        }

        // Validate file size (max 5MB - will be compressed during resize)
        if (file.getSize() > MAX_THUMBNAIL_FILE_SIZE) {
            throw new InvalidRequestException("thumbnailFile",
                    String.format("Thumbnail file size (%d KB) exceeds maximum allowed size (%d KB). " +
                            "Please compress the image before uploading.",
                            file.getSize() / 1024, MAX_THUMBNAIL_FILE_SIZE / 1024));
        }

        log.debug("Custom thumbnail validation passed - type={}, size={}KB",
                file.getContentType(), file.getSize() / 1024);
    }

    // ========================================
    // Image Thumbnail Generation
    // ========================================

    @Override
    public ThumbnailResponseDto generateFromImage(MultipartFile imageFile) throws IOException {
        return generateFromImage(imageFile, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public ThumbnailResponseDto generateFromImage(MultipartFile imageFile, int width, int height) throws IOException {
        log.info("ThumbnailService: Generating thumbnail from image: {}", imageFile.getOriginalFilename());

        validateImageFile(imageFile);

        // Delegate to NewsImageProcessingService to generate all sizes
        var imageResponse = newsImageProcessingService.processImage(imageFile, imageFile.getOriginalFilename());

        log.info("ThumbnailService: Image processed via NewsImageProcessingService - filename={}",
                imageResponse.getThumbnail().getFilename());

        // Return thumbnail variant for backward compatibility
        var thumbnailVariant = imageResponse.getThumbnail();
        return ThumbnailResponseDto.builder()
                .filename(thumbnailVariant.getFilename())
                .url(thumbnailVariant.getUrl())
                .filePath(thumbnailVariant.getFilePath())
                .originalFilename(imageFile.getOriginalFilename())
                .fileSize(thumbnailVariant.getFileSize())
                .source(ThumbnailSource.IMAGE_RESIZE)
                .build();
    }

    @Override
    public ThumbnailResponseDto generatePushNotificationThumbnail(MultipartFile imageFile) throws IOException {
        log.info("ThumbnailService: Generating push notification optimized thumbnail ({}x{}) from: {}",
                PUSH_NOTIFICATION_WIDTH, PUSH_NOTIFICATION_HEIGHT, imageFile.getOriginalFilename());
        return generateFromImage(imageFile, PUSH_NOTIFICATION_WIDTH, PUSH_NOTIFICATION_HEIGHT);
    }

    // ========================================
    // Video Thumbnail Generation
    // ========================================

    @Override
    public ThumbnailResponseDto generateFromVideo(MultipartFile videoFile) throws IOException {
        return generateFromVideo(videoFile, DEFAULT_FRAME_TIME_SECONDS);
    }

    @Override
    public ThumbnailResponseDto generateFromVideo(MultipartFile videoFile, double timeInSeconds) throws IOException {
        log.info("ThumbnailService: Extracting frame from video: {} at {}s",
                videoFile.getOriginalFilename(), timeInSeconds);

        validateVideoFile(videoFile);

        // Delegate to NewsImageProcessingService to generate all sizes from video
        var imageResponse = newsImageProcessingService.processVideo(videoFile, videoFile.getOriginalFilename());

        log.info("ThumbnailService: Video processed via NewsImageProcessingService - filename={}",
                imageResponse.getThumbnail().getFilename());

        // Return thumbnail variant for backward compatibility
        var thumbnailVariant = imageResponse.getThumbnail();
        return ThumbnailResponseDto.builder()
                .filename(thumbnailVariant.getFilename())
                .url(thumbnailVariant.getUrl())
                .filePath(thumbnailVariant.getFilePath())
                .originalFilename(videoFile.getOriginalFilename())
                .fileSize(thumbnailVariant.getFileSize())
                .source(ThumbnailSource.VIDEO_FRAME)
                .build();
    }

    // ========================================
    // Generate from Saved Media
    // ========================================

    @Override
    public ThumbnailResponseDto generateFromSavedMedia(String mediaFilePath, String originalFilename)
            throws IOException {
        log.info("ThumbnailService: Generating thumbnail from saved media: {}", mediaFilePath);

        Path mediaPath = Paths.get(mediaFilePath);
        if (!Files.exists(mediaPath)) {
            throw new ResourceNotFoundException("Media file", "path", mediaFilePath);
        }

        String contentType = Files.probeContentType(mediaPath);

        // Create a temporary MultipartFile from the file path for compatibility with
        // NewsImageProcessingService
        MultipartFile tempFile = createMultipartFileFromPath(mediaPath, originalFilename, contentType);

        ImageProcessingResponseDto imageResponse;
        if (isImage(contentType)) {
            // Process image via NewsImageProcessingService
            imageResponse = newsImageProcessingService.processImage(tempFile, originalFilename);
        } else if (isVideo(contentType)) {
            // Process video via NewsImageProcessingService
            imageResponse = newsImageProcessingService.processVideo(tempFile, originalFilename);
        } else {
            throw new InvalidRequestException("mediaFile",
                    "Unsupported media type for thumbnail generation: " + contentType);
        }

        log.info("ThumbnailService: Media processed via NewsImageProcessingService - filename={}",
                imageResponse.getThumbnail().getFilename());

        // Return thumbnail variant for backward compatibility
        var thumbnailVariant = imageResponse.getThumbnail();
        return ThumbnailResponseDto.builder()
                .filename(thumbnailVariant.getFilename())
                .url(thumbnailVariant.getUrl())
                .filePath(thumbnailVariant.getFilePath())
                .originalFilename(originalFilename)
                .fileSize(thumbnailVariant.getFileSize())
                .source(isImage(contentType) ? ThumbnailSource.IMAGE_RESIZE : ThumbnailSource.VIDEO_FRAME)
                .build();
    }

    // ========================================
    // Thumbnail Retrieval Methods
    // ========================================

    @Override
    public String getThumbnailPath(String filename) {
        return newsImageProcessingService.getImagePath(filename, "thumb");
    }

    @Override
    public boolean thumbnailExists(String filename) {
        return newsImageProcessingService.imageExists(filename, "thumb");
    }

    @Override
    public boolean deleteThumbnail(String filename) {
        return newsImageProcessingService.deleteImage(filename, "thumb");
    }

    // ========================================
    // Utility Methods
    // ========================================

    @Override
    public boolean isImage(String contentType) {
        return contentType != null && IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    @Override
    public boolean isVideo(String contentType) {
        return contentType != null && VIDEO_TYPES.contains(contentType.toLowerCase());
    }

    @Override
    public String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "file";
        }

        // Remove file extension
        int dotIndex = filename.lastIndexOf('.');
        String nameWithoutExt = (dotIndex > 0) ? filename.substring(0, dotIndex) : filename;

        // Sanitize: lowercase, replace spaces/special chars with hyphens
        String sanitized = nameWithoutExt
                .toLowerCase()
                .replaceAll("[^a-z0-9\\-]", "-") // Replace non-alphanumeric with hyphen
                .replaceAll("-+", "-") // Collapse multiple hyphens
                .replaceAll("^-|-$", ""); // Trim leading/trailing hyphens

        // Limit length
        if (sanitized.length() > MAX_FILENAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_FILENAME_LENGTH);
        }

        // Ensure not empty
        if (sanitized.isEmpty()) {
            sanitized = "file";
        }

        return sanitized;
    }

    @Override
    public String generateThumbnailFilename(String originalFilename) {
        String sanitized = sanitizeFilename(originalFilename);
        String shortUuid = UUID.randomUUID().toString().substring(0, 8);
        return sanitized + "_" + shortUuid + THUMBNAIL_SUFFIX;
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("file", "Image file is required");
        }
        if (!isImage(file.getContentType())) {
            throw new InvalidRequestException("file", "Invalid image file type: " + file.getContentType());
        }
    }

    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("file", "Video file is required");
        }
        if (!isVideo(file.getContentType())) {
            throw new InvalidRequestException("file", "Invalid video file type: " + file.getContentType());
        }
    }

    private void ensureStorageDirectoryExists() throws IOException {
        Path storagePath = Paths.get(thumbnailStoragePath);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
            log.info("ThumbnailService: Created thumbnail storage directory: {}", thumbnailStoragePath);
        }
    }

    private Path saveToTempFile(MultipartFile file) throws IOException {
        Path tempFile = Files.createTempFile("video_", "_" + file.getOriginalFilename());
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    private Path saveAndResizeImage(InputStream inputStream, String filename) throws IOException {
        return saveAndResizeImage(inputStream, filename, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    private Path saveAndResizeImage(InputStream inputStream, String filename, int width, int height)
            throws IOException {
        ensureStorageDirectoryExists();

        Path outputPath = Paths.get(thumbnailStoragePath, filename);

        // Use Thumbnailator for high-quality resizing
        Thumbnails.of(inputStream)
                .size(width, height)
                .outputFormat("jpg")
                .outputQuality(0.85)
                .toFile(outputPath.toFile());

        log.debug("ThumbnailService: Saved resized thumbnail: {} ({}x{})", filename, width, height);
        return outputPath;
    }

    private Path extractVideoFrame(Path videoPath, String thumbnailFilename, double timeInSeconds) throws IOException {
        ensureStorageDirectoryExists();

        Path outputPath = Paths.get(thumbnailStoragePath, thumbnailFilename);

        try {
            // Use Jcodec to extract frame
            File videoFile = videoPath.toFile();
            int frameNumber = (int) (timeInSeconds * 24); // Assume 24fps, adjust frame number

            FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile));

            // Seek to approximate frame
            Picture picture = null;
            for (int i = 0; i <= frameNumber && (picture = grab.getNativeFrame()) != null; i++) {
                // Keep grabbing until we reach desired frame
            }

            if (picture == null) {
                // If we couldn't get to the desired frame, get the first frame
                grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile));
                picture = grab.getNativeFrame();
            }

            if (picture == null) {
                throw new InvalidRequestException("video", "Could not extract frame from video");
            }

            // Convert to BufferedImage
            BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);

            // Resize and save
            Thumbnails.of(bufferedImage)
                    .size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
                    .outputFormat("jpg")
                    .outputQuality(0.85)
                    .toFile(outputPath.toFile());

            log.info("ThumbnailService: Extracted and saved video frame as thumbnail: {}", thumbnailFilename);
            return outputPath;

        } catch (Exception e) {
            log.error("ThumbnailService: Failed to extract video frame", e);
            throw new IOException("Failed to extract video frame: " + e.getMessage(), e);
        }
    }

    private ThumbnailResponseDto buildResponse(Path thumbnailPath, String originalFilename, ThumbnailSource source)
            throws IOException {

        String filename = thumbnailPath.getFileName().toString();
        long fileSize = Files.size(thumbnailPath);

        // Get image dimensions
        BufferedImage image = ImageIO.read(thumbnailPath.toFile());
        int width = image != null ? image.getWidth() : DEFAULT_WIDTH;
        int height = image != null ? image.getHeight() : DEFAULT_HEIGHT;

        String url = mediaUrlService.buildThumbnailUrl(filename);

        return ThumbnailResponseDto.builder()
                .filename(filename)
                .url(url)
                .filePath(thumbnailPath.toString())
                .originalFilename(originalFilename)
                .fileSize(fileSize)
                .contentType(THUMBNAIL_CONTENT_TYPE)
                .width(width)
                .height(height)
                .source(source)
                .build();
    }

    /**
     * Creates a MultipartFile from a file path for compatibility with
     * NewsImageProcessingService.
     * This is a utility method to bridge the old file-path based API with the new
     * MultipartFile-based API.
     *
     * @param filePath    the path to the file
     * @param filename    the original filename
     * @param contentType the content type
     * @return a MultipartFile wrapper
     * @throws IOException if file cannot be read
     */
    private MultipartFile createMultipartFileFromPath(Path filePath, String filename, String contentType)
            throws IOException {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return filename;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public boolean isEmpty() {
                try {
                    return Files.size(filePath) == 0;
                } catch (IOException e) {
                    return true; // Assume empty if we can't check
                }
            }

            @Override
            public long getSize() {
                try {
                    return Files.size(filePath);
                } catch (IOException e) {
                    return 0;
                }
            }

            @Override
            public byte[] getBytes() throws IOException {
                return Files.readAllBytes(filePath);
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return Files.newInputStream(filePath);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.copy(filePath, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        };
    }
}
