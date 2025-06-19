package org.fossify.gallery.helpers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.fossify.commons.extensions.getFilenameFromPath
import org.fossify.commons.extensions.getParentPath
import org.fossify.commons.helpers.NOMEDIA
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.gallery.extensions.config
import java.io.File
import android.util.Log

class SecureHideManager(private val context: Context) {
    private val config = context.config
    private val secureFolderPath get() = config.secureHideFolderPath
    private val hiddenFilesPath get() = "$secureFolderPath/hidden_files"

    data class HiddenFileMetadata(
        val originalPath: String,
        val hiddenPath: String,
        val hiddenDate: Long,
        val originalName: String,
        val fileSize: Long
    )

    private fun getMetadataFile() = File(secureFolderPath, "file_metadata.json")

    fun setupSecureFolder() {
        Log.d("SecureHideManager", "Setting up secure folder at: $secureFolderPath")
        
        val secureFolder = File(secureFolderPath)
        if (!secureFolder.exists()) {
            val created = secureFolder.mkdirs()
            Log.d("SecureHideManager", "Created secure folder: $created")
        }
        
        val hiddenFilesFolder = File(hiddenFilesPath)
        if (!hiddenFilesFolder.exists()) {
            val created = hiddenFilesFolder.mkdirs()
            Log.d("SecureHideManager", "Created hidden files folder: $created")
        }
        
        // Create .nomedia to hide entire secure folder from media scanner
        val noMediaFile = File(secureFolder, NOMEDIA)
        if (!noMediaFile.exists()) {
            val created = noMediaFile.createNewFile()
            Log.d("SecureHideManager", "Created .nomedia file: $created")
        }
    }

    fun hideFileInSecureFolder(originalPath: String, callback: (success: Boolean) -> Unit) {
        ensureBackgroundThread {
            try {
                Log.d("SecureHideManager", "Hiding file: $originalPath")
                setupSecureFolder()
                
                val originalFile = File(originalPath)
                if (!originalFile.exists()) {
                    Log.e("SecureHideManager", "Original file does not exist: $originalPath")
                    callback(false)
                    return@ensureBackgroundThread
                }
                
                val timestamp = System.currentTimeMillis()
                val hiddenName = "${timestamp}_${originalFile.name}"
                val hiddenPath = "$hiddenFilesPath/$hiddenName"
                val hiddenFile = File(hiddenPath)
                
                Log.d("SecureHideManager", "Moving to: $hiddenPath")
                
                // Copy file to secure location
                originalFile.copyTo(hiddenFile, overwrite = false)
                
                if (hiddenFile.exists()) {
                    Log.d("SecureHideManager", "File copied successfully, size: ${hiddenFile.length()}")
                    
                    // Delete original file
                    val deleted = originalFile.delete()
                    Log.d("SecureHideManager", "Original file deleted: $deleted")
                    
                    // Save metadata
                    saveHiddenMetadata(originalPath, hiddenPath, originalFile.name, hiddenFile.length())
                    
                    callback(true)
                } else {
                    Log.e("SecureHideManager", "Failed to copy file to secure location")
                    callback(false)
                }
            } catch (e: Exception) {
                Log.e("SecureHideManager", "Error hiding file: ${e.message}", e)
                callback(false)
            }
        }
    }

    private fun saveHiddenMetadata(originalPath: String, hiddenPath: String, originalName: String, fileSize: Long) {
        try {
            val currentMetadata = loadMetadata().toMutableList()
            
            val metadata = HiddenFileMetadata(
                originalPath = originalPath,
                hiddenPath = hiddenPath,
                hiddenDate = System.currentTimeMillis(),
                originalName = originalName,
                fileSize = fileSize
            )
            
            currentMetadata.add(metadata)
            
            val json = Gson().toJson(currentMetadata)
            getMetadataFile().writeText(json)
            
            Log.d("SecureHideManager", "Metadata saved. Total files: ${currentMetadata.size}")
        } catch (e: Exception) {
            Log.e("SecureHideManager", "Error saving metadata: ${e.message}", e)
        }
    }

    private fun loadMetadata(): List<HiddenFileMetadata> {
        val metadataFile = getMetadataFile()
        Log.d("SecureHideManager", "Loading metadata from: ${metadataFile.absolutePath}")
        
        if (!metadataFile.exists()) {
            Log.d("SecureHideManager", "Metadata file does not exist")
            return emptyList()
        }
        
        return try {
            val json = metadataFile.readText()
            Log.d("SecureHideManager", "Metadata JSON: $json")
            
            val type = object : TypeToken<List<HiddenFileMetadata>>() {}.type
            val result = Gson().fromJson<List<HiddenFileMetadata>>(json, type) ?: emptyList()
            
            Log.d("SecureHideManager", "Loaded ${result.size} metadata entries")
            return result
        } catch (e: Exception) {
            Log.e("SecureHideManager", "Error loading metadata: ${e.message}", e)
            emptyList()
        }
    }

    fun getHiddenFiles(): List<HiddenFileMetadata> {
        return loadMetadata()
    }

    fun restoreFileFromSecureFolder(metadata: HiddenFileMetadata, callback: (success: Boolean) -> Unit) {
        ensureBackgroundThread {
            try {
                val hiddenFile = File(metadata.hiddenPath)
                val originalFile = File(metadata.originalPath)
                
                if (!hiddenFile.exists()) {
                    callback(false)
                    return@ensureBackgroundThread
                }
                
                // Ensure parent directory exists
                originalFile.parentFile?.mkdirs()
                
                // Copy file back to original location
                hiddenFile.copyTo(originalFile, overwrite = false)
                
                if (originalFile.exists()) {
                    // Delete from secure folder
                    hiddenFile.delete()
                    
                    // Remove from metadata
                    removeHiddenMetadata(metadata)
                    
                    callback(true)
                } else {
                    callback(false)
                }
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    private fun removeHiddenMetadata(toRemove: HiddenFileMetadata) {
        val currentMetadata = loadMetadata().toMutableList()
        currentMetadata.removeAll { it.hiddenPath == toRemove.hiddenPath }
        
        val json = Gson().toJson(currentMetadata)
        getMetadataFile().writeText(json)
    }
} 