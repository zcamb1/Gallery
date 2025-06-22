--- FILE: project_documentation/1_OVERVIEW/main_user_flows.md ---

# Fossify Gallery - Main User Flows

## Overview

This document describes the main user interaction flows with Fossify Gallery, from app startup to complex tasks like photo editing and folder management.

## 1. App Startup

```mermaid
graph TD
    A[App Launch] --> B[SplashActivity]
    B --> C[MainActivity]
    C --> D{Storage Permission?}
    D -->|No| E[Request Permission]
    E --> F{Grant permissions?}
    F -->|Yes| G[Scan Media Files]
    F -->|No| H[Show Empty State]
    D -->|Yes| G
    G --> I[Show Directory Grid]
    H --> J[Show Permission Explanation]
    J --> E
    
    subgraph "Background Process"
        K[MediaFetcher]
        L[MediaStore Query]
        M[Cache in Database]
    end
    
    G --> K
    K --> L
    L --> M
    M --> I
```

### Startup Flow Details

1. **SplashActivity** displays briefly
2. **MainActivity** checks storage access permissions
3. If permissions missing → show permission request dialog
4. **MediaFetcher** scans media files from MediaStore
5. Display grid of directories with thumbnails

## 2. Media Browsing and Viewing

```mermaid
sequenceDiagram
    participant User
    participant MainActivity
    participant MediaActivity
    participant ViewPagerActivity
    
    User->>MainActivity: "Tap folder"
    MainActivity->>MediaActivity: "Navigate with folder path"
    MediaActivity-->>User: "Show media grid"
    
    User->>MediaActivity: "Tap photo/video"
    MediaActivity->>ViewPagerActivity: "Navigate with media index"
    ViewPagerActivity-->>User: "Fullscreen view"
    
    Note over ViewPagerActivity: "Supports gestures:<br/>- Pinch to zoom<br/>- Swipe for next/prev<br/>- Double tap to zoom"
```

### Supported Media Types

| Type | Features |
|------|----------|
| **Photos** | Zoom, rotate, EXIF data viewing |
| **Videos** | Play/pause, seek, volume control |
| **GIFs** | Auto-play, Frame control |
| **RAW** | Basic viewing, EXIF data |

## 3. File and Folder Management

```mermaid
graph TD
    A[Select Files] --> B[Long Press]
    B --> C{Choose action}
    C --> D[Delete]
    C --> E[Move]
    C --> F[Copy]
    C --> G[Share]
    C --> H[Set as]
    
    D --> I[Move to Recycle Bin]
    E --> J[Choose destination]
    F --> K[Choose destination]
    
    subgraph "Bulk Actions"
        L[Select Multiple]
        M[Select All]
        N[Apply to Selected]
    end
```

### File Management Actions

#### **Move/Copy Operations**
1. Select files (long press)
2. Choose "Move" or "Copy"
3. Navigate to destination folder
4. Confirm operation

#### **Delete Operations**
- **Soft Delete**: Move to recycle bin (can restore)
- **Permanent Delete**: Remove from storage completely

## 4. Photo Editing

```mermaid
graph TD
    A[Photo Selected] --> B[EditActivity]
    B --> C[Load Image]
    C --> D{Choose edit type}
    D --> E[Filters]
    D --> F[Adjustments]
    D --> G[Crop/Rotate]
    D --> H[Draw/Text]
    
    E --> I[Apply filter preset"]
    F --> J[Brightness, Contrast, Saturation]
    G --> K[Crop to aspect ratio]
    H --> L[Add drawings/annotations]
    
    I --> M[Preview Changes]
    J --> M
    K --> M
    L --> M
    
    M --> N{Save changes?}
    N -->|Yes| O[Save to Gallery]
    N -->|No| P[Discard Changes]
```

### Editing Features

| Category | Tools Available |
|----------|----------------|
| **Filters** | Vintage, B&W, Sepia, Blur, Sharpen |
| **Adjustments** | Brightness, Contrast, Saturation, Vibrance |
| **Crop** | Free crop, Aspect ratios, Manual coordinates |
| **Rotation** | 90° increments, Free rotation |
| **Draw** | Brush tool, Color picker, Opacity control |
| **Text** | Add text overlay, Font selection |

## 5. Settings and Customization

```mermaid
graph TD
    A[Settings Activity] --> B[General Settings]
    A --> C[Display Settings]
    A --> D[Privacy Settings]
    A --> E[Advanced Settings]
    
    B --> F[Default folder, File filters, Sorting]
    C --> G[Theme, Grid columns, Thumbnails]
    D --> H[App lock, Hidden folders, Security]
    E --> I[Cache management, Performance]
    
    subgraph "Theme Customization"
        J[Light Theme]
        K[Dark Theme]
        L[System Default]
        M[Custom Colors]
    end
    
    C --> J
    C --> K
    C --> L
    C --> M
```

### Settings Categories

- **General**: Default directory, file type filters
- **Display**: Grid size, thumbnail style and spacing
- **Privacy**: Hidden folder access, app security
- **Advanced**: Performance settings, cache management
- **Security**: App lock with PIN/Pattern/Biometrics

### Performance Options
- **Load Quality**: Thumbnail vs full resolution
- **Animation**: Transition effects on/off
- **Memory**: Cache size configuration
- **File loading priority**: Speed vs Quality
- **Background refresh**: Auto-sync frequency
- **Thumbnail caching strategy**: Database vs file system

## 6. Search and Discovery

```mermaid
sequenceDiagram
    participant User
    participant SearchActivity
    participant MediaFetcher
    participant Database
    
    User->>SearchActivity: "Enter search term"
    SearchActivity->>MediaFetcher: "Search request"
    MediaFetcher->>Database: "Query by filename"
    Database-->>MediaFetcher: "Return matches"
    MediaFetcher-->>SearchActivity: "Results list"
    SearchActivity-->>User: "Display results"
    
    Note over SearchActivity: "Search within:<br/>- File names<br/>- Folder paths<br/>- EXIF data<br/>- Date ranges"
```

### Search Capabilities

| Search Type | Examples |
|-------------|----------|
| **Filename** | "vacation", "IMG_2023" |
| **Date range** | "2023", "December", "last week" |
| **File type** | Photos only, Videos only, RAW files |
| **Location** | EXIF GPS data (if available) |
| **EXIF data** | Camera model, settings |

## 7. Slideshow and Presentation

```mermaid
graph TD
    A[Slideshow Mode] --> B[Select Photos]
    B --> C[Configure Settings]
    C --> D[Start Slideshow]
    
    C --> E[Transition speed]
    C --> F[Random order]
    C --> G[Loop playback]
    C --> H[Transition effects]
    
    D --> I[Fullscreen Display]
    I --> J{User interaction}
    J -->|Tap| K[Pause/Resume]
    J -->|Swipe| L[Manual navigation]
    J -->|Back| M[Exit slideshow]
```

### Slideshow Features
- **Timer**: 1-30 seconds per photo
- **Transitions**: Fade, slide, zoom effects
- **Order**: Sequential or random
- **Loop**: Continuous playback
- **Interaction**: Pause, skip, exit anytime

## 8. Widgets and Shortcuts

### Home Screen Widget
- Display random photos from selected folder
- Click widget → open app directly
- Configurable refresh interval

### App Shortcuts
- **Camera**: Open camera directly
- **Last photo**: Jump to recent photo
- **Favorites**: Quick access to starred items

---
**End of Main User Flows Documentation**
