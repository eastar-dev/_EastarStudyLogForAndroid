package dev.eastar.studypush.fcm

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.content.getSystemService
import dev.eastar.studypush.R

class NotificationChannelManager {
    companion object {
        private const val GROUP_ID = "StydyPush"
        private const val GROUP_NAME = "일반"
    }

    @TargetApi(Build.VERSION_CODES.N)
    enum class AppChannel(var groupId: String, var groupName: String, val channelName: String, var importance: Int, var description: String, var lightColor: Int, var lockScreenVisibility: Int) {
        COMMON(GROUP_ID, GROUP_NAME, "일반", NotificationManager.IMPORTANCE_DEFAULT, "일반", Color.BLUE, Notification.VISIBILITY_PUBLIC),
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return

        //default channel check
        val defaultChannel = AppChannel.COMMON//set default channel defind hear
        assert(context.resources.getString(R.string.default_notification_channel_id) == defaultChannel.name)

        //create channel
        context.getSystemService<NotificationManager>()?.let { mn ->
            AppChannel.values().forEach {
                mn.createNotificationChannelGroup(NotificationChannelGroup(it.groupId, it.groupName))

                val channel = NotificationChannel(it.name, it.channelName, it.importance).apply {
                    description = it.description
                    group = it.groupId
                    lightColor = it.lightColor
                    lockscreenVisibility = it.lockScreenVisibility
                }
                mn.createNotificationChannel(channel)
            }
        }
    }

    @Suppress("unused")
    fun deleteNotificationChannel(context: Context, channel: AppChannel) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return
        context.getSystemService<NotificationManager>()?.deleteNotificationChannel(channel.name)
    }
}
