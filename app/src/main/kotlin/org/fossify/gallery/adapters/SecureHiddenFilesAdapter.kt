package org.fossify.gallery.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.adapters.MyRecyclerViewAdapter
import org.fossify.commons.extensions.*
import org.fossify.commons.views.MyRecyclerView
import org.fossify.gallery.R
import org.fossify.gallery.databinding.ItemSecureHiddenFileBinding
import org.fossify.gallery.helpers.SecureHideManager
import java.io.File
import android.util.Log
import org.fossify.commons.helpers.ensureBackgroundThread

class SecureHiddenFilesAdapter(
    activity: BaseSimpleActivity,
    var hiddenFiles: ArrayList<SecureHideManager.HiddenFileMetadata>,
    recyclerView: MyRecyclerView,
    private val onItemClick: (SecureHideManager.HiddenFileMetadata) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, {}) {

    private val secureHideManager = SecureHideManager(activity)

    init {
        Log.d("SecureHideManager", "[Adapter] Constructor called with ${hiddenFiles.size} files")
        setupDragListener(false)
    }

    override fun getActionMenuId() = 0

    override fun prepareActionMode(menu: android.view.Menu) {}

    override fun actionItemPressed(id: Int) {}

    override fun getSelectableItemCount() = hiddenFiles.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = hiddenFiles.getOrNull(position)?.hiddenPath?.hashCode()

    override fun getItemKeyPosition(key: Int) = hiddenFiles.indexOfFirst { it.hiddenPath.hashCode() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("SecureHideManager", "[Adapter] onCreateViewHolder called")
        val holder = createViewHolder(ItemSecureHiddenFileBinding.inflate(layoutInflater, parent, false).root)
        Log.d("SecureHideManager", "[Adapter] ViewHolder created: $holder")
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hiddenFile = hiddenFiles.getOrNull(position) ?: return
        Log.d("SecureHideManager", "[Adapter] Binding view for position $position: ${hiddenFile.originalName}")
        
        holder.bindView(hiddenFile, true, false) { itemView, _ ->
            setupView(itemView, hiddenFile)
        }
        bindViewHolder(holder)
        
        Log.d("SecureHideManager", "[Adapter] View bound for position $position")
    }

    override fun getItemCount() = hiddenFiles.size

    private fun setupView(view: View, hiddenFile: SecureHideManager.HiddenFileMetadata) {
        Log.d("SecureHideManager", "[Adapter] Setting up view for: ${hiddenFile.originalName}")
        
        val binding = ItemSecureHiddenFileBinding.bind(view)
        
        binding.apply {
            secureHiddenFileName.text = hiddenFile.originalName
            secureHiddenFileDetails.text = "Original: ${hiddenFile.originalPath.getParentPath()}"
            
            // Load thumbnail
            val hiddenFilePath = hiddenFile.hiddenPath
            val file = File(hiddenFilePath)
            
            if (file.exists()) {
                try {
                    if (hiddenFile.originalName.isImageFast() || hiddenFile.originalName.isVideoFast()) {
                        Glide.with(activity)
                            .load(file)
                            .centerCrop()
                            .into(secureHiddenFileThumbnail)
                    } else {
                        secureHiddenFileThumbnail.setImageResource(R.drawable.ic_files_vector)
                    }
                } catch (e: Exception) {
                    secureHiddenFileThumbnail.setImageResource(R.drawable.ic_files_vector)
                }
            } else {
                secureHiddenFileThumbnail.setImageResource(R.drawable.ic_files_vector)
            }
            
            // Setup restore button
            secureHiddenFileRestore.setOnClickListener {
                restoreFile(hiddenFile)
            }
            
            // Setup item click
            secureHiddenFileHolder.setOnClickListener {
                onItemClick(hiddenFile)
            }
        }
    }

    private fun restoreFile(hiddenFile: SecureHideManager.HiddenFileMetadata) {
        Log.d("SecureHideManager", "[Adapter] Restoring file: ${hiddenFile.originalName}")
        
        secureHideManager.restoreFileFromSecureFolder(hiddenFile) { success ->
            activity.runOnUiThread {
                if (success) {
                    Log.d("SecureHideManager", "[Adapter] File restored successfully: ${hiddenFile.originalPath}")
                    
                    // Remove from adapter list
                    hiddenFiles.remove(hiddenFile)
                    notifyDataSetChanged()
                    
                    // Rescan the restored file so it appears in main gallery
                    activity.rescanPaths(arrayListOf(hiddenFile.originalPath)) {
                        Log.d("SecureHideManager", "[Adapter] File rescanned: ${hiddenFile.originalPath}")
                    }
                    
                    activity.toast(R.string.file_restored_successfully)
                } else {
                    Log.e("SecureHideManager", "[Adapter] Failed to restore file: ${hiddenFile.originalName}")
                    activity.toast(org.fossify.commons.R.string.unknown_error_occurred)
                }
            }
        }
    }

    fun updateHiddenFiles(newHiddenFiles: ArrayList<SecureHideManager.HiddenFileMetadata>) {
        val diffCallback = HiddenFilesDiffCallback(hiddenFiles, newHiddenFiles)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        hiddenFiles = newHiddenFiles
        diffResult.dispatchUpdatesTo(this)
    }

    private class HiddenFilesDiffCallback(
        private val oldList: List<SecureHideManager.HiddenFileMetadata>,
        private val newList: List<SecureHideManager.HiddenFileMetadata>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].hiddenPath == newList[newItemPosition].hiddenPath
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
} 