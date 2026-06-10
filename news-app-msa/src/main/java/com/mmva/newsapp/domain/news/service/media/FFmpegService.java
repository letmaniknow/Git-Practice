package com.mmva.newsapp.domain.news.service.media;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.TimeUnit;

/**
 * Service for executing FFmpeg commands to process video files.
 * Supports both system-installed FFmpeg and bundled static binaries.
 */
@Slf4j
@Service
public class FFmpegService {

    private static final int TIMEOUT_SECONDS = 30;

    @Value("${media.ffmpeg.path:#{null}}")
    private String customFFmpegPath;

    @Value("${media.ffmpeg.bin-dir:bin/ffmpeg}")
    private String binDir;

    private Path extractedFFmpegPath;
    private boolean useSystemFFmpeg = true;

    // FFmpeg capabilities (detected at startup)
    private boolean supportsAvif = false;
    private boolean supportsHeif = false;
    private boolean supportsJpegXl = false;
    private boolean supportsAv1 = false;

    @PostConstruct
    public void initialize() {
        log.info("FFmpegService: Starting initialization...");

        if (customFFmpegPath != null && !customFFmpegPath.trim().isEmpty()) {
            log.info("FFmpegService: Custom FFmpeg path configured: {}", customFFmpegPath);
            Path customPath = Path.of(customFFmpegPath);
            if (Files.exists(customPath) && Files.isExecutable(customPath)) {
                extractedFFmpegPath = customPath;
                useSystemFFmpeg = false;
                log.info("Using custom FFmpeg path: {}", customPath);
                checkFFmpegCapabilities();
                return;
            } else {
                log.warn("Custom FFmpeg path '{}' is not valid, falling back to auto-detection", customFFmpegPath);
            }
        }

        // Try to load static binary from resources
        log.info("FFmpegService: Attempting to load static binary from resources...");
        try {
            Path binaryPath = loadStaticBinary();
            if (binaryPath != null) {
                extractedFFmpegPath = binaryPath;
                useSystemFFmpeg = false;
                log.info("Using static FFmpeg binary: {}", binaryPath);
            } else {
                log.warn("No static FFmpeg binary found, will use system PATH");
            }
        } catch (Exception e) {
            log.error("Failed to load static FFmpeg binary: {}", e.getMessage(), e);
            log.info("Falling back to system FFmpeg");
        }

        log.info("FFmpegService: Initialization complete. Using system FFmpeg: {}", useSystemFFmpeg);

        // Check what codecs/formats are available
        checkFFmpegCapabilities();
    }

    @PreDestroy
    public void cleanup() {
        if (extractedFFmpegPath != null && !useSystemFFmpeg) {
            try {
                // Only delete if it's in temp directory (not a custom path)
                if (extractedFFmpegPath.getParent().toString().contains("temp") ||
                        extractedFFmpegPath.getParent().toString().contains("tmp")) {
                    Files.deleteIfExists(extractedFFmpegPath);
                    log.debug("Cleaned up extracted FFmpeg binary: {}", extractedFFmpegPath);
                }
            } catch (Exception e) {
                log.warn("Failed to cleanup FFmpeg binary: {}", e.getMessage());
            }
        }
    }

    private Path loadStaticBinary() throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        log.debug("FFmpegService: Detected OS: {}", osName);

        String binaryName;
        String resourcePath;

        if (osName.contains("windows")) {
            binaryName = "ffmpeg.exe";
            resourcePath = binDir + "/windows/" + binaryName;
        } else if (osName.contains("linux")) {
            binaryName = "ffmpeg";
            resourcePath = binDir + "/linux/" + binaryName;
        } else if (osName.contains("mac")) {
            binaryName = "ffmpeg";
            resourcePath = binDir + "/macos/" + binaryName;
        } else {
            log.warn("Unsupported OS for static binaries: {}", osName);
            return null;
        }

        log.debug("FFmpegService: Looking for binary at resource path: {}", resourcePath);

        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            log.warn("Static FFmpeg binary not found at: {}", resourcePath);
            return null;
        }

        log.debug("FFmpegService: Found binary resource, extracting to temp directory...");

        // Extract to temp directory
        Path tempDir = Files.createTempDirectory("ffmpeg-static-");
        Path extractedPath = tempDir.resolve(binaryName);

        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, extractedPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Make executable on Unix-like systems
        if (!osName.contains("windows")) {
            try {
                Files.setPosixFilePermissions(extractedPath,
                        PosixFilePermissions.fromString("rwxr-xr-x"));
            } catch (Exception e) {
                log.warn("Failed to set executable permissions on FFmpeg binary: {}", e.getMessage());
            }
        }

        log.info("Extracted FFmpeg binary to: {}", extractedPath);
        return extractedPath;
    }

    private String getFFmpegCommand() {
        if (extractedFFmpegPath != null && !useSystemFFmpeg) {
            log.debug("FFmpegService: Using extracted binary: {}", extractedFFmpegPath);
            return extractedFFmpegPath.toString();
        }
        log.debug("FFmpegService: Using system FFmpeg");
        return "ffmpeg";
    }

    /**
     * Checks FFmpeg capabilities (supported codecs and formats).
     * This runs at startup to detect what modern formats are available.
     */
    private void checkFFmpegCapabilities() {
        log.info("FFmpegService: Checking capabilities...");
        try {
            String[] command = { getFFmpegCommand(), "-codecs" };
            ProcessResult result = executeCommandWithOutput(command, "capabilities check");

            String output = result.getOutput() + result.getErrorOutput();

            // Check for modern format support
            supportsAvif = output.toLowerCase().contains("avif") ||
                    output.toLowerCase().contains("av1 image");
            supportsHeif = output.toLowerCase().contains("heif") ||
                    output.toLowerCase().contains("heic");
            supportsJpegXl = output.toLowerCase().contains("jpegxl") ||
                    output.toLowerCase().contains("jpeg xl");
            supportsAv1 = output.toLowerCase().contains("av1") &&
                    output.toLowerCase().contains("video");

            log.info("FFmpeg Capabilities:");
            log.info("  - AVIF (images):  {}", supportsAvif ? "✓ Supported" : "✗ Not supported");
            log.info("  - HEIF/HEIC:      {}", supportsHeif ? "✓ Supported" : "✗ Not supported");
            log.info("  - JPEG XL:        {}", supportsJpegXl ? "✓ Supported" : "✗ Not supported");
            log.info("  - AV1 (video):    {}", supportsAv1 ? "✓ Supported" : "✗ Not supported");

            if (!supportsAvif && !supportsHeif) {
                log.warn("FFmpeg does not support modern image formats (AVIF/HEIF).");
                log.warn("To enable support:");
                log.warn("  Windows: choco install ffmpeg-full");
                log.warn("  Ubuntu:  sudo apt install ffmpeg libavif-dev libheif-dev");
                log.warn("  macOS:   brew install ffmpeg");
            }

        } catch (Exception e) {
            log.warn("Failed to check FFmpeg capabilities: {}", e.getMessage());
            log.info("Assuming basic codec support only");
        }
    }

    /**
     * Check if FFmpeg supports AVIF format.
     */
    public boolean supportsAvif() {
        return supportsAvif;
    }

    /**
     * Check if FFmpeg supports HEIF/HEIC format.
     */
    public boolean supportsHeif() {
        return supportsHeif;
    }

    /**
     * Check if FFmpeg supports JPEG XL format.
     */
    public boolean supportsJpegXl() {
        return supportsJpegXl;
    }

    /**
     * Check if FFmpeg supports AV1 video codec.
     */
    public boolean supportsAv1() {
        return supportsAv1;
    }

    /**
     * Converts a modern image format (AVIF, HEIF/HEIC, JPEG XL) to JPEG.
     * This method checks if FFmpeg supports the format before attempting
     * conversion.
     * 
     * @param inputPath  Path to the input image file (AVIF/HEIF/HEIC/JPEG XL)
     * @param outputPath Path where to save the converted JPEG file
     * @param quality    JPEG quality (1-100, recommended 90-95 for high quality)
     * @throws IOException If conversion fails or format is not supported
     */
    public void convertModernImageToJpeg(Path inputPath, Path outputPath, int quality) throws IOException {
        if (quality < 1 || quality > 100) {
            throw new IllegalArgumentException("JPEG quality must be between 1 and 100");
        }

        // Detect format from file extension
        String filename = inputPath.getFileName().toString().toLowerCase();
        String format;
        boolean supported;

        if (filename.endsWith(".avif")) {
            format = "AVIF";
            supported = supportsAvif;
        } else if (filename.endsWith(".heic") || filename.endsWith(".heif")) {
            format = "HEIF/HEIC";
            supported = supportsHeif;
        } else if (filename.endsWith(".jxl")) {
            format = "JPEG XL";
            supported = supportsJpegXl;
        } else {
            throw new IOException("Unsupported image format: " + filename
                    + ". Only AVIF, HEIF/HEIC, and JPEG XL are supported for conversion.");
        }

        // Check if FFmpeg supports this format
        if (!supported) {
            String errorMsg = String.format(
                    "FFmpeg does not support %s format. To enable support:\n" +
                            "  Windows: choco install ffmpeg-full\n" +
                            "  Ubuntu:  sudo apt install ffmpeg %s\n" +
                            "  macOS:   brew install ffmpeg",
                    format,
                    format.equals("AVIF") ? "libavif-dev" : format.equals("HEIF/HEIC") ? "libheif-dev" : "libjxl-dev");
            throw new IOException(errorMsg);
        }

        log.info("Converting {} image to JPEG: {} -> {}", format, inputPath.getFileName(), outputPath.getFileName());

        // FFmpeg command to convert modern format to JPEG
        // -i input: input file
        // -q:v quality: JPEG quality (2=high, 31=low; we map 1-100 to 31-2)
        // -y: overwrite output file
        int ffmpegQuality = 31 - ((quality - 1) * 29 / 99); // Map 1-100 to 31-2 (inverted scale)

        String[] command = {
                getFFmpegCommand(),
                "-i", inputPath.toString(),
                "-q:v", String.valueOf(ffmpegQuality),
                "-y",
                outputPath.toString()
        };

        executeCommand(command, "image conversion");

        log.info("Successfully converted {} to JPEG: {} (quality={})", format, outputPath.getFileName(), quality);
    }

    /**
     * Extracts a thumbnail from a video file at the specified time.
     *
     * @param videoPath   Path to the video file
     * @param outputPath  Path where to save the thumbnail
     * @param timeSeconds Time in seconds to seek to
     * @param width       Thumbnail width
     * @param height      Thumbnail height
     * @throws IOException If FFmpeg execution fails
     */
    /**
     * Extracts a thumbnail from a video file at the specified time with
     * professional scaling.
     *
     * This method handles all possible video sizes and aspect ratios using a
     * "cover" strategy:
     * - Videos wider than target (16:9) → Scale to target height, crop sides
     * - Videos taller than target (9:16 vertical) → Scale to target width, crop
     * top/bottom
     * - Videos with matching aspect ratio (4:3) → Scale proportionally, no cropping
     * - Square videos (1:1) → Scale to fit, crop to target aspect ratio
     * - Very small videos → Scale up proportionally then crop
     * - Very large videos → Scale down proportionally then crop
     *
     * @param videoPath   Path to the video file
     * @param outputPath  Path where to save the thumbnail
     * @param timeSeconds Time in seconds to seek to
     * @param width       Thumbnail width
     * @param height      Thumbnail height
     * @throws IOException If FFmpeg execution fails
     */
    public void extractThumbnail(Path videoPath, Path outputPath, double timeSeconds, int width, int height)
            throws IOException {
        // Input validation
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Thumbnail dimensions must be positive: " + width + "x" + height);
        }
        if (timeSeconds < 0) {
            timeSeconds = 0; // Default to start of video
        }

        // Professional thumbnail scaling using "cover" strategy (like CSS object-fit:
        // cover)
        // This handles all video aspect ratios correctly:
        // - 16:9 videos (widescreen) → crop sides
        // - 4:3 videos (standard) → fit perfectly
        // - 1:1 videos (square) → crop to fit
        // - 9:16 videos (vertical) → crop top/bottom
        // - Ultra-wide, portrait, etc. → all handled properly

        // Use FFmpeg's built-in cover scaling: scale to increase aspect ratio, then
        // center crop
        String scaleFilter = String.format(
                "scale=%d:%d:force_original_aspect_ratio=increase,crop=%d:%d:(ow-iw)/2:(oh-ih)/2",
                width, height, width, height);

        String[] command = {
                getFFmpegCommand(),
                "-y", // Overwrite output files
                "-ss", String.valueOf(timeSeconds), // Seek to position
                "-i", videoPath.toString(), // Input file
                "-vframes", "1", // Extract 1 frame
                "-vf", scaleFilter, // Scale to cover area, then center crop
                "-q:v", "2", // Quality setting
                outputPath.toString() // Output file
        };

        executeCommand(command, "thumbnail extraction");
    }

    /**
     * Gets video duration in seconds.
     *
     * @param videoPath Path to the video file
     * @return Duration in seconds, or -1 if unable to determine
     */
    public double getVideoDuration(Path videoPath) {
        try {
            String[] command = {
                    getFFmpegCommand(),
                    "-i", videoPath.toString(),
                    "-f", "null", "-"
            };

            ProcessResult result = executeCommandWithOutput(command, "duration probe");

            // Parse duration from stderr (FFmpeg outputs info to stderr)
            String output = result.getErrorOutput();
            // Look for "Duration: HH:MM:SS.ss" pattern
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Duration: (\\d+):(\\d+):(\\d+\\.\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(output);

            if (matcher.find()) {
                int hours = Integer.parseInt(matcher.group(1));
                int minutes = Integer.parseInt(matcher.group(2));
                double seconds = Double.parseDouble(matcher.group(3));

                return hours * 3600 + minutes * 60 + seconds;
            }
        } catch (Exception e) {
            log.warn("Failed to get video duration: {}", e.getMessage());
        }
        return -1.0;
    }

    /**
     * Checks if a video file is valid and contains video streams.
     *
     * @param videoPath Path to the video file
     * @return true if valid video file with video streams
     */
    public boolean isValidVideoFile(Path videoPath) {
        log.debug("FFmpegService: Validating video file: {}", videoPath);
        try {
            String[] command = {
                    getFFmpegCommand(),
                    "-i", videoPath.toString(),
                    "-f", "null", "-"
            };

            log.debug("FFmpegService: Running validation command: {}", String.join(" ", command));

            ProcessResult result = executeCommandWithOutput(command, "format probe");

            // Check if output contains video stream information
            String output = result.getErrorOutput();
            boolean hasVideo = output.contains("Video:") || output.contains("Stream #");
            log.debug("FFmpegService: Validation result - hasVideo: {}, exitCode: {}", hasVideo, result.getExitCode());

            return hasVideo;

        } catch (Exception e) {
            log.warn("Failed to validate video file: {}", e.getMessage());
            return false;
        }
    }

    private void executeCommand(String[] command, String operation) throws IOException {
        ProcessResult result = executeCommandWithOutput(command, operation);
        if (result.getExitCode() != 0) {
            throw new IOException(String.format("FFmpeg %s failed with exit code %d: %s",
                    operation, result.getExitCode(), result.getErrorOutput()));
        }
    }

    private ProcessResult executeCommandWithOutput(String[] command, String operation) throws IOException {
        log.debug("Executing FFmpeg command: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false); // Keep stdout and stderr separate

        Process process = pb.start();

        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        // Use threads to read output to prevent blocking
        Thread stdoutReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line).append("\n");
                }
            } catch (IOException e) {
                log.debug("Error reading stdout: {}", e.getMessage());
            }
        });

        Thread stderrReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderr.append(line).append("\n");
                }
            } catch (IOException e) {
                log.debug("Error reading stderr: {}", e.getMessage());
            }
        });

        stdoutReader.start();
        stderrReader.start();

        try {
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                log.warn("FFmpeg {} timed out after {} seconds, destroying process", operation, TIMEOUT_SECONDS);
                process.destroyForcibly();
                // Wait a bit more for the process to be destroyed
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
                throw new IOException(
                        String.format("FFmpeg %s timed out after %d seconds", operation, TIMEOUT_SECONDS));
            }

            // Wait for readers to finish
            stdoutReader.join(1000);
            stderrReader.join(1000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new IOException(String.format("FFmpeg %s interrupted: %s", operation, e.getMessage()));
        }

        int exitCode = process.exitValue();
        log.debug("FFmpeg {} completed with exit code: {}", operation, exitCode);

        return new ProcessResult(exitCode, stdout.toString(), stderr.toString());
    }

    private static class ProcessResult {
        private final int exitCode;
        private final String output;
        private final String errorOutput;

        public ProcessResult(int exitCode, String output, String errorOutput) {
            this.exitCode = exitCode;
            this.output = output;
            this.errorOutput = errorOutput;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }

        public String getErrorOutput() {
            return errorOutput;
        }
    }
}