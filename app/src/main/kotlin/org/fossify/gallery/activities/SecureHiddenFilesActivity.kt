package org.fossify.gallery.activities

import android.os.Bundle
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.gallery.R
import org.fossify.gallery.adapters.SecureHiddenFilesAdapter
import org.fossify.gallery.databinding.ActivitySecureHiddenFilesBinding
import org.fossify.gallery.helpers.SecureHideManager
import org.fossify.commons.dialogs.ConfirmationAdvancedDialog
import org.fossify.gallery.R as GalleryR
import org.fossify.gallery.extensions.config
import android.util.Log
import org.fossify.commons.extensions.handleHiddenFolderPasswordProtection
import androidx.recyclerview.widget.LinearLayoutManager

class SecureHiddenFilesActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = arrayListOf(
        GalleryR.mipmap.ic_launcher_red,
        GalleryR.mipmap.ic_launcher_pink,
        GalleryR.mipmap.ic_launcher_purple,
        GalleryR.mipmap.ic_launcher_deep_purple,
        GalleryR.mipmap.ic_launcher_indigo,
        GalleryR.mipmap.ic_launcher_blue,
        GalleryR.mipmap.ic_launcher_light_blue,
        GalleryR.mipmap.ic_launcher_cyan,
        GalleryR.mipmap.ic_launcher_teal,
        GalleryR.mipmap.ic_launcher_amber,
        GalleryR.mipmap.ic_launcher_orange,
        GalleryR.mipmap.ic_launcher_deep_orange,
        GalleryR.mipmap.ic_launcher_brown,
        GalleryR.mipmap.ic_launcher_blue_grey,
        GalleryR.mipmap.ic_launcher_grey_black,
        GalleryR.mipmap.ic_launcher
    )

    override fun getAppLauncherName() = getString(GalleryR.string.app_launcher_name)
    override fun getRepositoryName() = null
    private lateinit var binding: ActivitySecureHiddenFilesBinding
    private lateinit var secureHideManager: SecureHideManager
    private var hiddenFiles = ArrayList<SecureHideManager.HiddenFileMetadata>()

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        
        // Always require password protection for secure hidden files
        handleHiddenFolderPasswordProtection {
            setupActivity()
        }
    }

    private fun setupActivity() {
        binding = ActivitySecureHiddenFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        
        secureHideManager = SecureHideManager(this)
        loadHiddenFiles()
    }

    override fun onResume() {
        super.onResume()
        
        // Only setup if binding is initialized (after password authentication)
        if (::binding.isInitialized) {
            setupToolbar()
            loadHiddenFiles()
        }
    }

    private fun setupToolbar() {
        binding.secureHiddenFilesToolbar.apply {
            setNavigationOnClickListener { finish() }
        }
        
        updateMaterialActivityViews(
            binding.secureHiddenFilesCoordinator,
            binding.secureHiddenFilesNestedScrollview,
            useTransparentNavigation = true,
            useTopSearchMenu = false
        )
    }

    private fun loadHiddenFiles() {
        ensureBackgroundThread {
            Log.d("SecureHideManager", "[Activity] Loading hidden files...")
            Log.d("SecureHideManager", "[Activity] Secure folder path: ${config.secureHideFolderPath}")
            
            hiddenFiles = ArrayList(secureHideManager.getHiddenFiles())
            
            Log.d("SecureHideManager", "[Activity] Found ${hiddenFiles.size} hidden files")
            for (file in hiddenFiles) {
                Log.d("SecureHideManager", "[Activity] File: ${file.originalName} -> ${file.hiddenPath}")
            }
            
            runOnUiThread {
                setupRecyclerView()
                updatePlaceholderVisibility()
            }
        }
    }

    private fun setupRecyclerView() {
        Log.d("SecureHideManager", "[Activity] Setting up RecyclerView with ${hiddenFiles.size} files")
        for (file in hiddenFiles) {
            Log.d("SecureHideManager", "[Activity] File to show: ${file.originalName}")
        }
        
        // Check RecyclerView state
        Log.d("SecureHideManager", "[Activity] RecyclerView: ${binding.secureHiddenFilesList}")
        Log.d("SecureHideManager", "[Activity] RecyclerView LayoutManager before: ${binding.secureHiddenFilesList.layoutManager}")
        
        // 🔧 FIX: Set LayoutManager explicitly
        if (binding.secureHiddenFilesList.layoutManager == null) {
            binding.secureHiddenFilesList.layoutManager = LinearLayoutManager(this)
            Log.d("SecureHideManager", "[Activity] LayoutManager set to LinearLayoutManager")
        }
        
        val adapter = SecureHiddenFilesAdapter(
            this,
            hiddenFiles,
            binding.secureHiddenFilesList
        ) { hiddenFile ->
            // Handle item click - could open file in viewer or show details
            showHiddenFileDetails(hiddenFile)
        }
        
        Log.d("SecureHideManager", "[Activity] Adapter created: $adapter")
        
        binding.secureHiddenFilesList.adapter = adapter
        Log.d("SecureHideManager", "[Activity] Adapter set with ${adapter.itemCount} items")
        
        // Check LayoutManager after adapter is set
        Log.d("SecureHideManager", "[Activity] RecyclerView LayoutManager after: ${binding.secureHiddenFilesList.layoutManager}")
        Log.d("SecureHideManager", "[Activity] RecyclerView adapter: ${binding.secureHiddenFilesList.adapter}")
        Log.d("SecureHideManager", "[Activity] RecyclerView childCount: ${binding.secureHiddenFilesList.childCount}")
        
        // Force layout
        binding.secureHiddenFilesList.post {
            Log.d("SecureHideManager", "[Activity] Post layout - childCount: ${binding.secureHiddenFilesList.childCount}")
            binding.secureHiddenFilesList.requestLayout()
        }
    }

    private fun updatePlaceholderVisibility() {
        val isEmpty = hiddenFiles.isEmpty()
        Log.d("SecureHideManager", "[Activity] Updating placeholder visibility. isEmpty: $isEmpty, files count: ${hiddenFiles.size}")
        
        binding.secureHiddenFilesPlaceholder.beVisibleIf(isEmpty)
        binding.secureHiddenFilesList.beVisibleIf(!isEmpty)
        
        Log.d("SecureHideManager", "[Activity] Placeholder visible: ${binding.secureHiddenFilesPlaceholder.isVisible()}")
        Log.d("SecureHideManager", "[Activity] RecyclerView visible: ${binding.secureHiddenFilesList.isVisible()}")
    }

    private fun showHiddenFileDetails(hiddenFile: SecureHideManager.HiddenFileMetadata) {
        val details = buildString {
            append("Original name: ${hiddenFile.originalName}\n")
            append("Original path: ${hiddenFile.originalPath}\n") 
            append("Hidden since: ${hiddenFile.hiddenDate}\n")
            append("Size: ${hiddenFile.fileSize} bytes")
        }
        
        ConfirmationAdvancedDialog(this, details, 0, org.fossify.commons.R.string.ok, 0, false) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear any cached thumbnails
        binding.secureHiddenFilesList.adapter = null
    }
} 