# FFmpeg Static Binaries

This directory should contain static FFmpeg binaries for consistent deployment across different environments.

## Directory Structure

```
src/main/resources/bin/ffmpeg/
├── windows/
│   ├── ffmpeg.exe
│   └── ffprobe.exe (optional)
├── linux/
│   ├── ffmpeg
│   └── ffprobe (optional)
└── macos/
    ├── ffmpeg
    └── ffprobe (optional)
```

## Obtaining Static Binaries

### Windows
1. Download from: https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip
2. Extract `ffmpeg.exe` and `ffprobe.exe` to `windows/` directory

### Linux
1. Download from: https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz
2. Extract `ffmpeg` and `ffprobe` to `linux/` directory
3. Make executable: `chmod +x ffmpeg ffprobe`

### macOS
1. Download from: https://evermeet.cx/ffmpeg/ffmpeg-6.0.7z (or latest)
2. Extract `ffmpeg` to `macos/` directory
3. Make executable: `chmod +x ffmpeg`

## Configuration

The application will automatically detect and use the appropriate binary for the current OS.
If no static binary is found, it will fall back to using `ffmpeg` from the system PATH.

## Application Properties

You can override the FFmpeg path using:

```properties
# Custom FFmpeg executable path
media.ffmpeg.path=/custom/path/to/ffmpeg

# Custom binary directory (relative to classpath)
media.ffmpeg.bin-dir=bin/ffmpeg
```

## Deployment Notes

- Static binaries ensure consistent behavior across different environments
- No external dependencies required
- Binaries are extracted to temporary directory at runtime
- Automatic cleanup on application shutdown</content>
<parameter name="filePath">c:\Users\cmanikan\Documents\MicroServices\PP\TheNews\Java\news-app-msa\src\main\resources\bin\ffmpeg\README.md