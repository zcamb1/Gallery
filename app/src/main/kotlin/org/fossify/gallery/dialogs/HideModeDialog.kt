package org.fossify.gallery.dialogs

import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.dialogs.RadioGroupDialog
import org.fossify.commons.models.RadioItem
import org.fossify.gallery.R
import org.fossify.gallery.helpers.HIDE_MODE_NORMAL
import org.fossify.gallery.helpers.HIDE_MODE_SECURE

class HideModeDialog(
    val activity: BaseSimpleActivity,
    val callback: (hideMode: Int) -> Unit
) {
    init {
        val items = arrayListOf(
            RadioItem(HIDE_MODE_NORMAL, activity.getString(R.string.hide_normally)),
            RadioItem(HIDE_MODE_SECURE, activity.getString(R.string.hide_in_secure_folder))
        )

        RadioGroupDialog(activity, items, checkedItemId = HIDE_MODE_NORMAL, titleId = R.string.choose_hide_method) {
            callback(it as Int)
        }
    }
} 