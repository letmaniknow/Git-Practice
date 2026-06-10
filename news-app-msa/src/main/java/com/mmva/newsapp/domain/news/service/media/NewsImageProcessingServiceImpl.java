package com.mmva.newsapp.domain.news.service.media;

import com.mmva.newsapp.config.ImageProcessingProperties;
import com.mmva.newsapp.domain.news.dto.core.ImageProcessingResponseDto;
import com.mmva.newsapp.domain.news.dto.core.ImageProcessingResponseDto.ImageVariant;
import com.mmva.newsapp.domain.news.dto.core.ImageProcessingResponseDto.ProcessingSource;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.coobird.thumbnailator.Thumbnails;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of NewsImageProcessingService.
 * Handles generation of configurable image sizes using Thumbnailator and
 * JCodec.
 *
 * <h3>Features:</h3>
 * <ul>
 * <li>Configurable size generation based on properties</li>
 * <li>Image resize using Thumbnailator with customizable settings</li>
 * <li>Video frame extraction using JCodec</li>
 * <li>Consistent naming: {sanitized-name}_{uuid}_{size}.jpg</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 2026-02-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsImageProcessingServiceImpl implements NewsImageProcessingService {

    // ========================================
    // Constants
    // ========================================

    private static final String IMAGE_SUFFIX = ".jpg";
    private static final String IMAGE_CONTENT_TYPE = "image/jpeg";
    private static final int MAX_FILENAME_LENGTH = 50;

    // Supported image formats (via Java ImageIO + TwelveMonkeys extensions)
    // Standard formats that Java ImageIO can process directly
    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp");

    // Modern image formats that require FFmpeg conversion to JPEG
    // These formats are auto-converted if FFmpeg has the necessary codec support
    private static final Set<String> MODERN_IMAGE_TYPES = Set.of(
            "image/avif", // AVIF - requires libavif in FFmpeg
            "image/heic", // HEIC - requires libheif in FFmpeg
            "image/heif", // HEIF - requires libheif in FFmpeg
            "image/jxl"); // JPEG XL - requires libjxl in FFmpeg

    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo",
            "video/x-ms-wmv", "video/webm", "video/ogg");

    // ========================================
    // Dependencies
    // ========================================

    private final MediaUrlService mediaUrlService;
    private final NewsMediaStorageService mediaStorageService;
    private final MediaPathUtils mediaPathUtils;
    private final ImageProcessingProperties imageProperties;
    private final FFmpegService ffmpegService;

    // ========================================
    // Configuration
    // ========================================

    @Value("${media.entities.news.processed:${media.root-path}/processed/news}")
    private String processedPath;

    // ========================================
    // Configuration Methods
    // ========================================

    @Override
    public Set<String> getEnabledSizes() {
        return imageProperties.getNewsEnabledSizes().keySet();
    }

    @Override
    public boolean isSizeEnabled(String size) {
        return imageProperties.isNewsSizeEnabled(size);
    }

    @Override
    public int[] getDimensions(String size) {
        ImageProcessingProperties.ImageSizeConfig config = imageProperties.getNewsSizeConfig(size);
        if (config != null) {
            return new int[] { config.getWidth(), config.getHeight() };
        }
        return null;
    }

    @Override
    public ImageProcessingResponseDto processImage(MultipartFile imageFile, String originalFilename)
            /**
             * Processes an image file and generates all enabled size variants (main,
             * thumbnail, card, hero)
             * using the default/configured dimensions for each size as defined in
             * application properties.
             * <p>
             * This is the standard method for normal media processing. It is recommended
             * for all
             * production and user-facing flows, as it ensures consistency and
             * maintainability.
             *
             * @param imageFile        the source image file
             * @param originalFilename the original filename for naming consistency
             * @return response with all enabled size variants and their details
             * @throws IOException if processing fails
             */
            throws IOException {
        log.info("NewsImageProcessingService: Processing image '{}' into enabled sizes: {}",
                originalFilename, getEnabledSizes());

        // Read the input stream once and convert to byte array for reuse
        log.debug("NewsImageProcessingService: Reading InputStream for '{}'", originalFilename);
        byte[] imageBytes = imageFile.getInputStream().readAllBytes();
        log.debug("NewsImageProcessingService: Successfully read {} bytes for '{}'", imageBytes.length,
                originalFilename);

        validateImageBytes(imageBytes, imageFile.getContentType());

        return processImage(imageBytes, originalFilename, imageFile.getContentType());
    }

    @Override
    public ImageProcessingResponseDto processImage(byte[] imageBytes, String originalFilename, String contentType)
            throws IOException {
        log.info("NewsImageProcessingService: Processing image bytes '{}' into enabled sizes: {}",
                originalFilename, getEnabledSizes());

        // Check if this is a modern image format that needs conversion
        if (isModernImageFormat(contentType)) {
            log.info("Detected modern image format: {}. Attempting auto-conversion to JPEG...", contentType);
            imageBytes = convertModernImageToJpeg(imageBytes, originalFilename, contentType);
            contentType = "image/jpeg"; // Update content type after conversion
            log.info("Successfully converted modern format to JPEG");
        }

        ImageProcessingResponseDto.ImageProcessingResponseDtoBuilder builder = ImageProcessingResponseDto.builder()
                .originalFilename(originalFilename)
                .source(ProcessingSource.IMAGE_PROCESS);

        // Generate a shared UUID for all sizes in this processing operation
        String sharedUuid = UUID.randomUUID().toString().substring(0, 8);
        log.debug("NewsImageProcessingService: Generated shared UUID '{}' for all sizes", sharedUuid);

        // Generate only enabled size variants
        Set<String> enabledSizes = getEnabledSizes();

        // --- MAIN VARIANT ---
        // Always generate the main variant (original size, or configured 'main' size if
        // present)
        int[] mainDimensions = getDimensions("main");
        int mainWidth, mainHeight;
        if (mainDimensions != null) {
            mainWidth = mainDimensions[0];
            mainHeight = mainDimensions[1];
        } else {
            // Fallback: use original image size (decode to get dimensions)
            try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
                BufferedImage originalImg = javax.imageio.ImageIO.read(bais);
                if (originalImg == null)
                    throw new IOException("Could not decode original image for main variant");
                mainWidth = originalImg.getWidth();
                mainHeight = originalImg.getHeight();
            }
        }
        try {
            ImageVariant mainVariant = generateImageVariant(imageBytes, originalFilename, "main", mainWidth, mainHeight,
                    sharedUuid);
            builder.main(mainVariant);
            log.debug("NewsImageProcessingService: main image generated and set as main: {}",
                    mainVariant.getFilename());
        } catch (Exception e) {
            log.error("NewsImageProcessingService: Failed to generate main variant for '{}': {}", originalFilename,
                    e.getMessage(), e);
            // Main is not mandatory for now, continue with other sizes
        }

        // --- OTHER VARIANTS ---
        for (String size : enabledSizes) {
            if ("main".equals(size))
                continue; // already handled
            log.debug("NewsImageProcessingService: Generating {} image for '{}'", size, originalFilename);

            int[] dimensions = getDimensions(size);
            if (dimensions == null) {
                log.warn("NewsImageProcessingService: No dimensions configured for size '{}', skipping", size);
                continue;
            }

            try {
                ImageVariant variant = generateImageVariant(imageBytes, originalFilename, size,
                        dimensions[0], dimensions[1], sharedUuid);
                log.debug("NewsImageProcessingService: {} image generated: {}", size, variant.getFilename());

                // Set the appropriate field in the builder
                switch (size) {
                    case "thumbnail" -> builder.thumbnail(variant);
                    case "card" -> builder.card(variant);
                    case "hero" -> builder.hero(variant);
                    default ->
                        log.warn("NewsImageProcessingService: Unknown size '{}', variant not set in response", size);
                }

            } catch (Exception e) {
                log.error("NewsImageProcessingService: Failed to generate {} variant for '{}': {}",
                        size, originalFilename, e.getMessage(), e);

                // Thumbnail is mandatory - fail the entire operation if it cannot be generated
                if ("thumbnail".equals(size)) {
                    throw new IOException("Failed to generate mandatory thumbnail variant: " + e.getMessage(), e);
                }

                // Continue with other sizes rather than failing completely
            }
        }

        log.info("NewsImageProcessingService: Successfully processed image '{}' into {} enabled sizes (plus main)",
                originalFilename, enabledSizes.size());

        return builder.build();
    }

    @Override
    public ImageProcessingResponseDto processImageWithCustomSizes(
            /**
             * Processes an image file and generates all size variants (main, thumbnail,
             * card, hero)
             * using custom dimensions provided at runtime, overriding the default
             * configuration.
             * <p>
             * This method is intended for special cases where non-standard sizes are
             * required,
             * such as admin tools, migration scripts, or one-off imports. It should NOT be
             * used
             * in normal user-facing flows unless there is a clear business need for custom
             * sizes.
             *
             * @param imageFile        the source image file
             * @param originalFilename the original filename for naming consistency
             * @param thumbnailWidth   custom thumbnail width
             * @param thumbnailHeight  custom thumbnail height
             * @param cardWidth        custom card width
             * @param cardHeight       custom card height
             * @param heroWidth        custom hero width
             * @param heroHeight       custom hero height
             * @param largeWidth       custom large width (not used in current variants)
             * @param largeHeight      custom large height (not used in current variants)
             * @return response with all size variants and their details
             * @throws IOException if processing fails
             */
            MultipartFile imageFile, String originalFilename,
            int thumbnailWidth, int thumbnailHeight,
            int cardWidth, int cardHeight,
            int heroWidth, int heroHeight,
            int largeWidth, int largeHeight) throws IOException {

        log.info("NewsImageProcessingService: Processing image '{}' with custom sizes", originalFilename);

        // Read the input stream once and convert to byte array for reuse
        byte[] imageBytes = imageFile.getInputStream().readAllBytes();

        validateImageBytes(imageBytes, imageFile.getContentType());

        // Generate a shared UUID for all sizes in this processing operation
        String sharedUuid = UUID.randomUUID().toString().substring(0, 8);
        log.debug("NewsImageProcessingService: Generated shared UUID '{}' for custom sizes", sharedUuid);

        // Generate all size variants with custom dimensions using the same image data
        ImageVariant thumbnail = generateImageVariant(imageBytes, originalFilename, "thumbnail", thumbnailWidth,
                thumbnailHeight, sharedUuid);
        ImageVariant card = generateImageVariant(imageBytes, originalFilename, "card", cardWidth, cardHeight,
                sharedUuid);
        ImageVariant hero = generateImageVariant(imageBytes, originalFilename, "hero", heroWidth, heroHeight,
                sharedUuid);

        return ImageProcessingResponseDto.builder()
                .originalFilename(originalFilename)
                .source(ProcessingSource.IMAGE_PROCESS)
                .thumbnail(thumbnail)
                .card(card)
                .hero(hero)
                .build();
    }

    // ========================================
    // Video Processing Methods
    // ========================================

    @Override
    public ImageProcessingResponseDto processVideo(MultipartFile videoFile, String originalFilename)
            throws IOException {
        return processVideoAtTime(videoFile, originalFilename, DEFAULT_FRAME_TIME_SECONDS);
    }

    @Override
    public ImageProcessingResponseDto processVideoAtTime(MultipartFile videoFile, String originalFilename,
            double frameTimeSeconds) throws IOException {

        log.info("NewsImageProcessingService: Extracting frames from video '{}' at {}s", originalFilename,
                frameTimeSeconds);
        log.info("NewsImageProcessingService: Video file details - size: {} bytes, contentType: {}",
                videoFile.getSize(), videoFile.getContentType());

        validateVideoFile(videoFile);

        // Save video to temp file for processing
        Path tempVideoPath = saveToTempFile(videoFile);

        // Generate a shared UUID for all sizes in this video processing operation
        String sharedUuid = UUID.randomUUID().toString().substring(0, 8);
        log.debug("NewsImageProcessingService: Generated shared UUID '{}' for video processing", sharedUuid);

        try {
            // Check if video is valid using FFmpeg
            if (!ffmpegService.isValidVideoFile(tempVideoPath)) {
                log.warn("NewsImageProcessingService: Video file '{}' is not valid or contains no video streams",
                        originalFilename);
                // Generate default thumbnail for invalid video
                ImageVariant defaultThumbnail = generateDefaultVideoThumbnail(originalFilename, sharedUuid);
                return ImageProcessingResponseDto.builder()
                        .originalFilename(originalFilename)
                        .source(ProcessingSource.VIDEO_FRAME)
                        .main(defaultThumbnail)
                        .thumbnail(defaultThumbnail)
                        .card(null)
                        .hero(null)
                        .build();
            }

            // Get video duration to ensure we don't seek beyond the video length
            double duration = ffmpegService.getVideoDuration(tempVideoPath);
            if (duration > 0 && frameTimeSeconds >= duration) {
                frameTimeSeconds = Math.max(0, duration - 1); // Seek to 1 second before end
                log.debug("NewsImageProcessingService: Adjusted seek time to {}s (video duration: {}s)",
                        frameTimeSeconds, duration);
            }

            // ========================================
            // STEP 1: Save actual VIDEO file as "main" media (for playback)
            // ========================================
            // Save video in the SAME folder as images (images/main/) for simplicity
            String videoFilename = generateVideoFilename(originalFilename, sharedUuid);
            Path processedVideoPath = saveVideoToMainFolder(videoFile, videoFilename);

            ImageVariant main = ImageVariant.builder()
                    .filename(videoFilename)
                    .url(mediaUrlService.buildMediaUrl(videoFilename))
                    .filePath(processedVideoPath.toString())
                    .fileSize(Files.size(processedVideoPath))
                    .build();
            log.info("NewsImageProcessingService: Saved video file as main media - filename={}, size={}KB",
                    videoFilename, Files.size(processedVideoPath) / 1024);

            // ========================================
            // STEP 2: Extract JPEG frames for PREVIEW purposes (thumbnail/card/hero)
            // ========================================
            ImageVariant thumbnail = null;
            ImageVariant card = null;
            ImageVariant hero = null;

            if (imageProperties.isNewsSizeEnabled("thumbnail")) {
                ImageProcessingProperties.ImageSizeConfig thumbnailConfig = imageProperties
                        .getNewsSizeConfig("thumbnail");
                thumbnail = extractVideoFrameVariant(tempVideoPath, originalFilename, "thumbnail",
                        thumbnailConfig.getWidth(), thumbnailConfig.getHeight(), frameTimeSeconds, sharedUuid);
            }
            if (imageProperties.isNewsSizeEnabled("card")) {
                ImageProcessingProperties.ImageSizeConfig cardConfig = imageProperties.getNewsSizeConfig("card");
                card = extractVideoFrameVariant(tempVideoPath, originalFilename, "card",
                        cardConfig.getWidth(), cardConfig.getHeight(), frameTimeSeconds, sharedUuid);
            }
            if (imageProperties.isNewsSizeEnabled("hero")) {
                ImageProcessingProperties.ImageSizeConfig heroConfig = imageProperties.getNewsSizeConfig("hero");
                hero = extractVideoFrameVariant(tempVideoPath, originalFilename, "hero",
                        heroConfig.getWidth(), heroConfig.getHeight(), frameTimeSeconds, sharedUuid);
            }

            return ImageProcessingResponseDto.builder()
                    .originalFilename(originalFilename)
                    .source(ProcessingSource.VIDEO_FRAME)
                    .main(main)
                    .thumbnail(thumbnail)
                    .card(card)
                    .hero(hero)
                    .build();

        } catch (Exception e) {
            log.warn(
                    "NewsImageProcessingService: Failed to extract video frames from '{}': {}. Generating default thumbnail.",
                    tempVideoPath, e.getMessage());

            // Generate a default thumbnail for failed video processing
            ImageVariant defaultThumbnail = generateDefaultVideoThumbnail(originalFilename, sharedUuid);

            return ImageProcessingResponseDto.builder()
                    .originalFilename(originalFilename)
                    .source(ProcessingSource.VIDEO_FRAME)
                    .main(defaultThumbnail)
                    .thumbnail(defaultThumbnail)
                    .card(null)
                    .hero(null)
                    .build();
        } finally {
            // Cleanup temp file
            Files.deleteIfExists(tempVideoPath);
        }
    }

    // ========================================
    // Individual Size Generation
    // ========================================

    @Override
    public ImageProcessingResponseDto generateThumbnail(MultipartFile imageFile, String originalFilename)
            throws IOException {
        byte[] imageBytes = imageFile.getInputStream().readAllBytes();
        validateImageBytes(imageBytes, imageFile.getContentType());
        ImageProcessingProperties.ImageSizeConfig thumbnailConfig = imageProperties.getNewsSizeConfig("thumbnail");
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        ImageVariant thumbnail = generateImageVariant(imageBytes, originalFilename, "thumbnail",
                thumbnailConfig.getWidth(), thumbnailConfig.getHeight(), uuid);

        return ImageProcessingResponseDto.builder()
                .originalFilename(originalFilename)
                .source(ProcessingSource.IMAGE_PROCESS)
                .thumbnail(thumbnail)
                .build();
    }

    @Override
    public ImageProcessingResponseDto generateSmall(MultipartFile imageFile, String originalFilename)
            throws IOException {
        byte[] imageBytes = imageFile.getInputStream().readAllBytes();
        validateImageBytes(imageBytes, imageFile.getContentType());
        ImageProcessingProperties.ImageSizeConfig cardConfig = imageProperties.getNewsSizeConfig("card");
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        ImageVariant card = generateImageVariant(imageBytes, originalFilename, "card",
                cardConfig.getWidth(), cardConfig.getHeight(), uuid);

        return ImageProcessingResponseDto.builder()
                .originalFilename(originalFilename)
                .source(ProcessingSource.IMAGE_PROCESS)
                .card(card)
                .build();
    }

    @Override
    public ImageProcessingResponseDto generateMedium(MultipartFile imageFile, String originalFilename)
            throws IOException {
        byte[] imageBytes = imageFile.getInputStream().readAllBytes();
        validateImageBytes(imageBytes, imageFile.getContentType());
        ImageProcessingProperties.ImageSizeConfig heroConfig = imageProperties.getNewsSizeConfig("hero");
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        ImageVariant hero = generateImageVariant(imageBytes, originalFilename, "hero",
                heroConfig.getWidth(), heroConfig.getHeight(), uuid);

        return ImageProcessingResponseDto.builder()
                .originalFilename(originalFilename)
                .source(ProcessingSource.IMAGE_PROCESS)
                .hero(hero)
                .build();
    }

    // ========================================
    // Core Processing Logic
    // ========================================

    /**
     * Generates a single image variant with specified dimensions.
     */
    private ImageVariant generateImageVariant(byte[] imageBytes, String originalFilename,
            String size, int width, int height, String uuid) throws IOException {

        log.debug("NewsImageProcessingService: Generating {} variant ({}x{}) for '{}'", size, width, height,
                originalFilename);

        String filename = generateSizedFilename(originalFilename, size, uuid);
        log.debug("NewsImageProcessingService: Generated filename: {} for size {}", filename, size);

        Path imagePath = saveAndResizeImage(new ByteArrayInputStream(imageBytes), filename, size, width, height);
        log.debug("NewsImageProcessingService: Saved image to: {}", imagePath);

        String url = mediaUrlService.buildImageUrl(size, filename);
        log.debug("NewsImageProcessingService: Generated URL: {}", url);

        return ImageVariant.builder()
                .filename(filename)
                .url(url)
                .filePath(imagePath.toString())
                .fileSize(Files.size(imagePath))
                .contentType(IMAGE_CONTENT_TYPE)
                .width(width)
                .height(height)
                .size(size)
                .build();
    }

    private ImageVariant generateImageVariantFromBufferedImage(BufferedImage bufferedImage, String originalFilename,
            String size, int width, int height, String uuid) throws IOException {

        log.debug("NewsImageProcessingService: Generating {} variant ({}x{}) from BufferedImage for '{}'", size, width,
                height,
                originalFilename);

        String filename = generateSizedFilename(originalFilename, size, uuid);
        log.debug("NewsImageProcessingService: Generated filename: {} for size {}", filename, size);

        Path imagePath = saveAndResizeImageFromBufferedImage(bufferedImage, filename, size, width, height);
        log.debug("NewsImageProcessingService: Saved image to: {}", imagePath);

        String url = mediaUrlService.buildImageUrl(size, filename);
        log.debug("NewsImageProcessingService: Generated URL: {}", url);

        return ImageVariant.builder()
                .filename(filename)
                .url(url)
                .filePath(imagePath.toString())
                .fileSize(Files.size(imagePath))
                .contentType(IMAGE_CONTENT_TYPE)
                .width(width)
                .height(height)
                .size(size)
                .build();
    }

    private ImageVariant generateDefaultVideoThumbnail(String originalFilename, String uuid) throws IOException {
        log.debug("NewsImageProcessingService: Generating default video thumbnail for '{}'", originalFilename);

        // Create a simple black image as default thumbnail
        BufferedImage defaultImage = new BufferedImage(320, 180, BufferedImage.TYPE_INT_RGB);
        // Fill with black
        java.awt.Graphics2D g2d = defaultImage.createGraphics();
        g2d.setColor(java.awt.Color.BLACK);
        g2d.fillRect(0, 0, 320, 180);
        g2d.setColor(java.awt.Color.WHITE);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        java.awt.FontMetrics fm = g2d.getFontMetrics();
        String text = "VIDEO";
        int x = (320 - fm.stringWidth(text)) / 2;
        int y = (180 + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(text, x, y);
        g2d.dispose();

        // Use thumbnail size
        ImageProcessingProperties.ImageSizeConfig thumbnailConfig = imageProperties.getNewsSizeConfig("thumbnail");
        return generateImageVariantFromBufferedImage(defaultImage, originalFilename, "thumbnail",
                thumbnailConfig.getWidth(), thumbnailConfig.getHeight(), uuid);
    }

    /**
     * Extracts a video frame and resizes it to specified dimensions using FFmpeg.
     */
    private ImageVariant extractVideoFrameVariant(Path videoPath, String originalFilename,
            String size, int width, int height, double timeSeconds, String uuid) throws IOException {

        log.debug("NewsImageProcessingService: Extracting video frame for {} variant ({}x{}) at {}s",
                size, width, height, timeSeconds);

        String filename = generateSizedFilename(originalFilename, size, uuid);
        log.debug("NewsImageProcessingService: Generated filename: {} for size {}", filename, size);

        // Use NewsMediaStorageService to ensure folder exists and get path
        mediaStorageService.ensureProcessedImagesFolderExists(size);
        String filePath = mediaPathUtils.resolveProcessedImagePath(processedPath, size, filename);
        Path outputPath = Paths.get(filePath);
        log.debug("NewsImageProcessingService: Output path: {}", outputPath);

        // Extract frame using FFmpeg
        ffmpegService.extractThumbnail(videoPath, outputPath, timeSeconds, width, height);

        String url = mediaUrlService.buildImageUrl(size, filename);
        log.debug("NewsImageProcessingService: Generated URL: {}", url);

        return ImageVariant.builder()
                .filename(filename)
                .url(url)
                .filePath(outputPath.toString())
                .fileSize(Files.size(outputPath))
                .contentType(IMAGE_CONTENT_TYPE)
                .width(width)
                .height(height)
                .size(size)
                .build();
    }

    /**
     * Saves and resizes an image to specified dimensions using configurable
     * settings.
     */
    private Path saveAndResizeImage(InputStream inputStream, String filename, String size, int width, int height)
            throws IOException {
        log.debug("NewsImageProcessingService: Saving and resizing image {} for size {} ({}x{})", filename, size, width,
                height);

        // Use NewsMediaStorageService to ensure folder exists and get path
        log.debug("NewsImageProcessingService: Ensuring folder exists for size {}", size);
        mediaStorageService.ensureProcessedImagesFolderExists(size);
        String filePath = mediaPathUtils.resolveProcessedImagePath(processedPath, size, filename);
        Path outputPath = Paths.get(filePath);
        log.debug("NewsImageProcessingService: Output path: {}", outputPath);

        // Get size-specific configuration
        ImageProcessingProperties.ImageSizeConfig sizeConfig = imageProperties.getNewsSizeConfig(size);
        float quality = sizeConfig != null ? sizeConfig.getQuality() : 0.85f;
        boolean keepAspectRatio = sizeConfig != null ? sizeConfig.isPreserveAspectRatio() : true;
        boolean allowCropping = sizeConfig != null ? sizeConfig.isCropping() : false;

        // Build Thumbnailator with configurable settings
        Thumbnails.Builder<? extends InputStream> thumbnailBuilder = Thumbnails.of(inputStream)
                .size(width, height);

        if (keepAspectRatio) {
            thumbnailBuilder.keepAspectRatio(true);
            if (allowCropping) {
                thumbnailBuilder.crop(net.coobird.thumbnailator.geometry.Positions.CENTER);
            }
        }

        try {
            thumbnailBuilder
                    .outputFormat("jpg")
                    .outputQuality(quality)
                    .toFile(outputPath.toFile());
        } catch (Exception e) {
            log.error("Failed to process image with Thumbnailator: {}", e.getMessage(), e);
            throw new IOException("Image processing failed: " + e.getMessage() +
                    ". The image file may be corrupted or in an unsupported format.", e);
        }

        log.debug(
                "NewsImageProcessingService: Successfully saved resized image to {} (quality: {}, aspectRatio: {}, cropping: {})",
                outputPath, quality, keepAspectRatio, allowCropping);

        return outputPath;
    }

    /**
     * Saves and resizes a BufferedImage to specified dimensions using configurable
     * settings.
     */
    private Path saveAndResizeImageFromBufferedImage(BufferedImage bufferedImage, String filename, String size,
            int width, int height)
            throws IOException {
        log.debug("NewsImageProcessingService: Saving and resizing BufferedImage {} for size {} ({}x{})", filename,
                size, width,
                height);

        // Use NewsMediaStorageService to ensure folder exists and get path
        log.debug("NewsImageProcessingService: Ensuring folder exists for size {}", size);
        mediaStorageService.ensureProcessedImagesFolderExists(size);
        String filePath = mediaPathUtils.resolveProcessedImagePath(processedPath, size, filename);
        Path outputPath = Paths.get(filePath);
        log.debug("NewsImageProcessingService: Output path: {}", outputPath);

        // Get size-specific configuration
        ImageProcessingProperties.ImageSizeConfig sizeConfig = imageProperties.getNewsSizeConfig(size);
        float quality = sizeConfig != null ? sizeConfig.getQuality() : 0.85f;
        boolean keepAspectRatio = sizeConfig != null ? sizeConfig.isPreserveAspectRatio() : true;
        boolean allowCropping = sizeConfig != null ? sizeConfig.isCropping() : false;

        // Build Thumbnailator with configurable settings
        Thumbnails.Builder<? extends BufferedImage> thumbnailBuilder = Thumbnails.of(bufferedImage)
                .size(width, height);

        if (keepAspectRatio) {
            thumbnailBuilder.keepAspectRatio(true);
            if (allowCropping) {
                thumbnailBuilder.crop(net.coobird.thumbnailator.geometry.Positions.CENTER);
            }
        }

        try {
            thumbnailBuilder
                    .outputFormat("jpg")
                    .outputQuality(quality)
                    .toFile(outputPath.toFile());
        } catch (Exception e) {
            log.error("Failed to process BufferedImage with Thumbnailator: {}", e.getMessage(), e);
            throw new IOException("Image processing failed: " + e.getMessage() +
                    ". The image data may be corrupted or in an unsupported format.", e);
        }

        log.debug(
                "NewsImageProcessingService: Successfully saved resized BufferedImage to {} (quality: {}, aspectRatio: {}, cropping: {})",
                outputPath, quality, keepAspectRatio, allowCropping);

        return outputPath;
    }

    /**
     * Saves multipart file to temporary location for processing.
     */
    private Path saveToTempFile(MultipartFile file) throws IOException {
        Path tempFile = Files.createTempFile("video_", "_" + file.getOriginalFilename());
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    // ========================================
    // Validation Methods
    // ========================================

    private void validateImageFile(MultipartFile file) {
        if (!isImage(file.getContentType())) {
            throw new InvalidRequestException("imageFile",
                    String.format(
                            "Invalid image file type: '%s'. Supported formats: JPEG, PNG, GIF, WebP, BMP, AVIF, HEIC/HEIF, JPEG XL",
                            file.getContentType()));
        }

        // Additional validation: try to read the image to ensure it's valid
        try {
            // Create a temporary input stream to validate the image
            try (InputStream tempStream = file.getInputStream()) {
                // Try to read image metadata using ImageIO (built into JDK)
                java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(tempStream);
                if (bufferedImage == null) {
                    throw new InvalidRequestException("imageFile",
                            "Invalid image file: cannot read image data. " +
                                    "The file may be corrupted or not a valid " + file.getContentType() + " image.");
                }
                log.debug("Image validation passed: {}x{} pixels, type={}",
                        bufferedImage.getWidth(), bufferedImage.getHeight(), file.getContentType());
            }
        } catch (InvalidRequestException e) {
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            log.error("Image validation failed for file '{}': {}", file.getOriginalFilename(), e.getMessage());
            throw new InvalidRequestException("imageFile",
                    "Invalid image file: " + e.getMessage() +
                            ". Please ensure the file is a valid image and try again.");
        }
    }

    private void validateImageBytes(byte[] imageBytes, String contentType) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new InvalidRequestException("imageFile", "Image data is required");
        }

        if (!isImage(contentType) && !isModernImageFormat(contentType)) {
            throw new InvalidRequestException("imageFile",
                    String.format(
                            "Invalid image file type: '%s'. Supported formats: JPEG, PNG, GIF, WebP, BMP, AVIF, HEIC/HEIF, JPEG XL",
                            contentType));
        }

        // Additional validation: try to read and process the image using Thumbnailator
        // This is stricter than ImageIO and will catch corrupted images that ImageIO
        // might accept
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            // Try to create a small thumbnail to validate the image can be processed
            BufferedImage testImage = Thumbnails.of(inputStream).size(1, 1).asBufferedImage();
            if (testImage == null) {
                throw new InvalidRequestException("imageFile",
                        "Invalid image file: cannot process image data. " +
                                "The file may be corrupted or not a valid " + contentType + " image.");
            }
            log.debug("Image validation passed: {}x{} pixels, type={}",
                    testImage.getWidth(), testImage.getHeight(), contentType);
        } catch (InvalidRequestException e) {
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            log.error("Image validation failed: {}", e.getMessage());
            throw new InvalidRequestException("imageFile",
                    "Invalid image file: " + e.getMessage() +
                            ". The image file may be corrupted or in an unsupported format.");
        }
    }

    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("videoFile", "Video file is required");
        }

        if (!isVideo(file.getContentType())) {
            throw new InvalidRequestException("videoFile",
                    String.format(
                            "Invalid video file type: '%s'. Supported formats: MP4, MPEG, MOV, AVI, WMV, WebM, OGG",
                            file.getContentType()));
        }

        // Additional validation: Check if the video file can actually be processed
        Path tempFile = null;
        try {
            tempFile = saveToTempFile(file);
            // Use FFmpeg to validate the video file
            if (!ffmpegService.isValidVideoFile(tempFile)) {
                log.warn(
                        "NewsImageProcessingService: Video file validation failed for '{}': Not a valid video file or no video streams found",
                        file.getOriginalFilename());

                // Additional debugging: Check file header
                try {
                    byte[] header = Files.readAllBytes(tempFile).length >= 12 ? Files.readAllBytes(tempFile)
                            : new byte[0];
                    if (header.length >= 12) {
                        String headerHex = String.format("%02X%02X%02X%02X %02X%02X%02X%02X %02X%02X%02X%02X",
                                header[0], header[1], header[2], header[3],
                                header[4], header[5], header[6], header[7],
                                header[8], header[9], header[10], header[11]);
                        log.warn("NewsImageProcessingService: File header (first 12 bytes): {}", headerHex);

                        // Check for common MP4 signatures
                        if (header.length >= 8) {
                            String signature = new String(header, 4, 4, StandardCharsets.US_ASCII);
                            log.warn("NewsImageProcessingService: MP4 box type: {}", signature);
                        }
                    }
                } catch (Exception headerEx) {
                    log.warn("NewsImageProcessingService: Could not read file header: {}", headerEx.getMessage());
                }

                // For now, just log the warning but don't fail - allow processing to continue
                // FFmpeg will handle various video formats and corrupted files
                log.warn(
                        "NewsImageProcessingService: Allowing video file '{}' to proceed despite validation failure",
                        file.getOriginalFilename());
            } else {
                log.debug("NewsImageProcessingService: Video file validation successful for '{}'",
                        file.getOriginalFilename());
            }
        } catch (Exception e) {
            log.error("NewsImageProcessingService: Failed to create temp file for validation: {}", e.getMessage());
            throw new InvalidRequestException("videoFile", "Unable to process video file for validation");
        } finally {
            // Clean up temp file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("NewsImageProcessingService: Failed to delete temp file: {}", e.getMessage());
                }
            }
        }
    }

    // ========================================
    // Utility Methods
    // ========================================

    @Override
    public boolean isImage(String contentType) {
        return IMAGE_TYPES.contains(contentType) || MODERN_IMAGE_TYPES.contains(contentType);
    }

    @Override
    public boolean isVideo(String contentType) {
        return VIDEO_TYPES.contains(contentType);
    }

    /**
     * Check if the content type is a modern image format that requires FFmpeg
     * conversion.
     * These formats (AVIF, HEIC/HEIF, JPEG XL) are not natively supported by Java
     * ImageIO
     * and must be converted to JPEG before processing.
     */
    private boolean isModernImageFormat(String contentType) {
        return MODERN_IMAGE_TYPES.contains(contentType);
    }

    /**
     * Convert a modern image format (AVIF, HEIC/HEIF, JPEG XL) to JPEG using
     * FFmpeg.
     * This method saves the image bytes to a temp file, converts it, and returns
     * the JPEG bytes.
     * 
     * @param imageBytes       The original image bytes in modern format
     * @param originalFilename Original filename for logging
     * @param contentType      The MIME type (e.g., image/avif, image/heic,
     *                         image/jxl)
     * @return JPEG image bytes
     * @throws IOException If conversion fails
     */
    private byte[] convertModernImageToJpeg(byte[] imageBytes, String originalFilename, String contentType)
            throws IOException {
        Path tempInput = null;
        Path tempOutput = null;

        try {
            // Determine file extension from content type
            String extension;
            switch (contentType) {
                case "image/avif" -> extension = ".avif";
                case "image/heic" -> extension = ".heic";
                case "image/heif" -> extension = ".heif";
                case "image/jxl" -> extension = ".jxl";
                default -> throw new IOException("Unsupported modern format: " + contentType);
            }

            // Create temp files for conversion
            tempInput = Files.createTempFile("news-image-input", extension);
            tempOutput = Files.createTempFile("news-image-output", ".jpg");

            // Write input bytes to temp file
            Files.write(tempInput, imageBytes);
            log.debug("Saved modern format image to temp file: {} ({} bytes)", tempInput, imageBytes.length);

            // Convert using FFmpeg (quality=95 for high quality conversion)
            ffmpegService.convertModernImageToJpeg(tempInput, tempOutput, 95);

            // Read converted JPEG bytes
            byte[] jpegBytes = Files.readAllBytes(tempOutput);
            log.info("Converted {} ({} bytes) to JPEG ({} bytes) for file: {}",
                    contentType, imageBytes.length, jpegBytes.length, originalFilename);

            return jpegBytes;

        } catch (Exception e) {
            log.error("Failed to convert modern image format {} for file {}: {}",
                    contentType, originalFilename, e.getMessage());
            throw new InvalidRequestException("imageFile",
                    String.format("Failed to convert %s image: %s. %s",
                            contentType.replace("image/", "").toUpperCase(),
                            e.getMessage(),
                            "Please ensure FFmpeg is installed with modern codec support, or convert the image to JPEG/PNG before uploading."));
        } finally {
            // Clean up temp files
            if (tempInput != null) {
                try {
                    Files.deleteIfExists(tempInput);
                } catch (IOException e) {
                    log.warn("Failed to delete temp input file: {}", e.getMessage());
                }
            }
            if (tempOutput != null) {
                try {
                    Files.deleteIfExists(tempOutput);
                } catch (IOException e) {
                    log.warn("Failed to delete temp output file: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public String sanitizeFilename(String filename) {
        if (filename == null)
            return "unnamed";

        String processed = filename.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        return processed.substring(0, Math.min(processed.length(), MAX_FILENAME_LENGTH - 10)); // Leave room for UUID
    }

    public String generateSizedFilename(String originalFilename, String size, String uuid) {
        if (originalFilename == null)
            originalFilename = "unnamed";
        int dotIdx = originalFilename.lastIndexOf('.');
        String baseName = (dotIdx > 0) ? originalFilename.substring(0, dotIdx) : originalFilename;
        String sanitized = sanitizeFilename(baseName);
        return String.format("%s_%s_%s.jpg", sanitized, uuid, size);
    }

    /**
     * Generate a filename for a video file with UUID
     * Pattern: {sanitized-name}_{uuid}_main.{original-extension}
     */
    private String generateVideoFilename(String originalFilename, String uuid) {
        if (originalFilename == null)
            originalFilename = "unnamed.mp4";

        int dotIdx = originalFilename.lastIndexOf('.');
        String baseName = (dotIdx > 0) ? originalFilename.substring(0, dotIdx) : originalFilename;
        String extension = (dotIdx > 0) ? originalFilename.substring(dotIdx) : ".mp4";

        String sanitized = sanitizeFilename(baseName);
        return String.format("%s_%s_main%s", sanitized, uuid, extension);
    }

    /**
     * Save video file to processed/news/images/main/ folder (same as images)
     * This simplifies the architecture - all main media in one place
     * 
     * @param videoFile the video multipart file
     * @param filename  the generated filename
     * @return Path to the saved video file
     */
    private Path saveVideoToMainFolder(MultipartFile videoFile, String filename) throws IOException {
        // Save video in the SAME folder as main images for unified storage
        String mainFolderPath = mediaStorageService.ensureProcessedImagesFolderExists("main");
        Path videoPath = Paths.get(mainFolderPath, filename);

        try (InputStream inputStream = videoFile.getInputStream()) {
            Files.copy(inputStream, videoPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("NewsImageProcessingService: Saved video to main folder: {}", videoPath);
        }

        return videoPath;
    }

    // ========================================
    // File Management Methods
    // ========================================

    @Override
    public String getImagePath(String filename, String size) {
        return mediaPathUtils.resolveProcessedImagePath(processedPath, size, filename);
    }

    @Override
    public boolean imageExists(String filename, String size) {
        Path imagePath = Paths.get(getImagePath(filename, size));
        return Files.exists(imagePath);
    }

    @Override
    public boolean deleteImages(String baseFilename) {
        boolean allDeleted = true;
        Set<String> enabledSizes = getEnabledSizes();

        log.debug("deleteImages: Starting cleanup for baseFilename='{}', enabledSizes={}", baseFilename, enabledSizes);

        // Extract base filename by removing the size suffix
        // Format: sanitized_uuid_size.jpg -> sanitized_uuid
        String baseName = baseFilename;
        for (String size : enabledSizes) {
            if (baseFilename.endsWith("_" + size + ".jpg")) {
                baseName = baseFilename.substring(0, baseFilename.length() - ("_" + size + ".jpg").length());
                log.debug("deleteImages: Extracted baseName='{}' from size='{}'", baseName, size);
                break;
            }
        }

        // Delete all variants using the extracted base name
        for (String size : enabledSizes) {
            try {
                String filename = baseName + "_" + size + ".jpg";
                log.debug("deleteImages: Attempting to delete filename='{}' for size='{}'", filename, size);
                if (!deleteImage(filename, size)) {
                    log.warn("deleteImages: Failed to delete image filename='{}' for size='{}'", filename, size);
                    allDeleted = false;
                } else {
                    log.debug("deleteImages: Successfully deleted filename='{}' for size='{}'", filename, size);
                }
            } catch (Exception e) {
                log.warn("Failed to delete image for size {}: {}", size, e.getMessage());
                allDeleted = false;
            }
        }

        log.debug("deleteImages: Cleanup complete, allDeleted={}", allDeleted);
        return allDeleted;
    }

    @Override
    public boolean deleteImage(String filename, String size) {
        try {
            Path imagePath = Paths.get(getImagePath(filename, size));
            boolean deleted = Files.deleteIfExists(imagePath);
            if (deleted) {
                log.debug("Deleted image file: {}", imagePath);
            } else {
                log.debug("Image file not found or already deleted: {}", imagePath);
            }
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete image file: {} for size {}", filename, size, e);
            return false;
        }
    }
}