--- FILE: project_documentation/1_OVERVIEW/app_architecture.md ---

# App Architecture - Fossify Gallery

## Overview

Fossify Gallery is an open-source photo and video management application for Android, built with Activity-based architecture with focus on **privacy**, **performance** and **user experience**. The application uses 100% Kotlin and follows modern Android best practices.

## High-Level Architecture

```mermaid
graph TB
    subgraph "UI Layer"
        A["Activities<br/>(MainActivity, MediaActivity,<br/>ViewPagerActivity...)"] --> B["Fragments<br/>(PhotoFragment,<br/>VideoFragment)"]
        A --> C["Adapters<br/>(DirectoryAdapter,<br/>MediaAdapter)"]
        C --> D["Custom Views<br/>(from Fossify Commons)"]
    end
    
    subgraph "Business Logic"
        E["Helpers<br/>(MediaFetcher, Config,<br/>MediaStoreHelper)"] --> F["Extensions<br/>(Context, Activity,<br/>String extensions)"]
        E --> G["Utils<br/>(File operations,<br/>Image processing)"]
    end
    
    subgraph "Data Layer"
        H["Models<br/>(Medium, Directory,<br/>Favorite)"] --> I["Room Database<br/>(GalleryDatabase)"]
        H --> J["Data Sources<br/>(MediaStore API,<br/>SharedPreferences)"]
    end
    
    subgraph "External Dependencies"
        K["Fossify Commons<br/>(Base classes, theming)"] --> L["Android Framework<br/>(MediaStore, Storage)"]
        K --> M["Third-party Libraries<br/>(Room, ExoPlayer)"]
    end
    
    A --> E
    E --> H
    H --> K
```

## Component Breakdown

### 1. UI Layer (Presentation)

- **Activities**: 18 main activities handling different screens
  - `MainActivity`: Main screen displaying directories
  - `MediaActivity`: Display media in directory
  - `ViewPagerActivity`: View photos/videos fullscreen
  - `EditActivity`: Photo editing
  - `VideoPlayerActivity`: Video playback
  - `SettingsActivity`: App settings

- **Adapters**: Manage data display in RecyclerView
  - `DirectoriesAdapter`: Directory grid display
  - `MediaAdapter`: Media files display  
  - `MyPagerAdapter`: ViewPager management

- **Custom Views**: Inherit from Fossify Commons
  - Uses ViewBinding for type-safe view access
  - Material Design with Dynamic Theming

### 2. Business Logic Layer

- **Helpers**: Contains main application logic
  - `MediaFetcher`: Collect media from MediaStore
  - `Config`: SharedPreferences wrapper
  - `MediaStoreHelper`: MediaStore API operations
  - `DBHelper`: Database operations

- **Extensions**: Extend functionality for existing classes
  - Extension functions for Context, Activity, etc.

### 3. Data Layer

#### Data Models
- `Medium`: Entity for media files
- `Directory`: Entity for directories
- `Favorite`: Entity for favorites
- `Widget`: Entity for widgets

#### Core Models
- `Medium`: Represents media file (photo/video)
- `Directory`: Represents directory
- `AlbumCover`: Cover for album

## Architecture Flow

```mermaid
sequenceDiagram
    participant User
    participant MainActivity
    participant MediaFetcher
    participant Database
    participant MediaStore
    
    User->>MainActivity: "Open app"
    MainActivity->>MediaFetcher: "getCachedDirectories()"
    MediaFetcher->>Database: "Query cached data"
    Database-->>MediaFetcher: "Return directories"
    MediaFetcher-->>MainActivity: "Return directories"
    MainActivity->>DirectoryAdapter: "Update adapter"
    DirectoryAdapter-->>User: "Show directories (fast)"
    
    Note over MainActivity: "Background refresh"
    MainActivity->>MediaFetcher: "getDirectoriesAsync()"
    MediaFetcher->>MediaStore: "Query fresh data"
    MediaStore-->>MediaFetcher: "Return fresh directories"
    MediaFetcher-->>MainActivity: "Return updated directories"
    MainActivity->>DirectoryAdapter: "Notify data changed"
    DirectoryAdapter-->>User: "Update UI (fresh data)"
```

## Key Architecture Principles

### **1. Separation of Concerns**
- UI logic completely separated from business logic
- Data access encapsulated in helper classes
- Each Activity has clear responsibility

### **2. Single Responsibility**
- Each class has one main function
- Dependencies are managed centralized
- Clear interfaces between layers

### **3. Performance First**
- Lazy loading for media files
- Background threading for heavy operations
- Caching strategy with Room Database
- RecyclerView optimization with ViewHolder pattern

### **4. Privacy & Security**
- App lock with biometrics
- Hidden folder protection
- No analytics or tracking
- Local data storage (no cloud sync)

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | 100% Kotlin |
| UI Framework | Android Views + ViewBinding |
| Database | Room Database |
| Architecture | Activity-based |
| DI | Manual Dependency Injection |
| Threading | AsyncTask + Thread |
| Image Loading | Custom implementation |
| Video Playback | ExoPlayer |
| Testing | JUnit + Espresso |

## Project Structure

```
app/src/main/kotlin/org/fossify/gallery/
├── activities/         # UI Controllers
├── adapters/          # RecyclerView Adapters  
├── asynctasks/        # Background Tasks
├── databases/         # Room Database
├── dialogs/          # Custom Dialogs
├── extensions/       # Extension Functions
├── fragments/        # UI Fragments
├── helpers/          # Business Logic
├── interfaces/       # Contracts & Callbacks
├── models/           # Data Models
└── views/            # Custom Views
```

--- END FILE: project_documentation/1_OVERVIEW/app_architecture.md --- 