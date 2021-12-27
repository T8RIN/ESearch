package ru.tech.easysearch.helper.utils.permissions

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.tech.easysearch.R

@TargetApi(Build.VERSION_CODES.M)
object PermissionUtils {

    fun grantPermissionsLoc(activity: Activity) {
        if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.permissionRequest)
                .setMessage(R.string.locationPerm)
                .setPositiveButton(R.string.ok_ok) { _, _ ->
                    activity.requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    )
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    fun grantPermissionsCamera(activity: Activity) {
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.permissionRequest)
                .setMessage(R.string.cameraPerm)
                .setPositiveButton(R.string.ok_ok) { _, _ ->
                    activity.requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        2
                    )
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    fun grantPermissionsMic(activity: Activity) {
        if (activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.permissionRequest)
                .setMessage(R.string.recordPerm)
                .setPositiveButton(R.string.ok_ok) { _, _ ->
                    activity.requestPermissions(
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        3
                    )
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    fun grantPermissionsStorage(activity: Activity) {
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                4
            )
        }
    }

}