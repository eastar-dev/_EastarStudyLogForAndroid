@file:Suppress("LocalVariableName", "SpellCheckingInspection")

package dev.eastar.studypush.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.log.Log
import androidx.core.app.NotificationCompat
import dev.eastar.ktx.appName
import dev.eastar.ktx.toIntent
import dev.eastar.studypush.R
import java.net.URISyntaxException
import java.util.concurrent.atomic.AtomicInteger

object NotificationHelper {
    fun sendNotification(context: Context, push: PushModel) {
        val builder = NotificationCompat.Builder(context, context.getString(R.string.default_notification_channel_id))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSmallIcon(dev.eastar.base.R.drawable.ic_stat_name)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.app_icon_round))
            .setAutoCancel(true)
            .setContentTitle(context.appName)
            .setContentText(push.alert)
            .setWhen(System.currentTimeMillis())

        setContentIntent(context, builder, push)

        setBigText(context, builder, push)

        push.action?.let {
            for (action in it) {
                if (action?.text.isNullOrBlank()) continue
                if (action?.link.isNullOrBlank()) continue
                builder.addAction(0, action?.text, PendingIntent.getActivity(context, 0, action?.link.toIntent(), PendingIntent.FLAG_UPDATE_CURRENT))
            }
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = NotificationID.id
        nm.notify(id /* ID of notification */, builder.build())
    }

    fun setContentIntent(context: Context, builder: NotificationCompat.Builder, push: PushModel) {
        try {
            val pi = PendingIntent.getActivity(
                context,
                0,
                Intent.parseUri(push.alert_link, Intent.URI_INTENT_SCHEME),
                PendingIntent.FLAG_ONE_SHOT
            )
            builder.setContentIntent(pi)
        } catch (e: URISyntaxException) {
            Log.printStackTrace(e)
        }
    }

    private fun setBigText(context: Context, builder: NotificationCompat.Builder, push: PushModel) {
        val style = NotificationCompat.BigTextStyle()
            .setBigContentTitle(push.title ?: context.appName)
            .bigText(push.alert ?: push.content)
        builder.setStyle(style)
    }


    //private fun setBigPictureStyle(context: Context, builder: NotificationCompat.Builder, push: PushModel) {
    //    val style = NotificationCompat.BigPictureStyle()
    //        .bigPicture(push._bitmap)
    //        .setBigContentTitle(push.title)
    //        .setSummaryText(push.content)
    //    builder.setStyle(style)
    //}

    object NotificationID {
        private val c = AtomicInteger(4000)

        val id: Int
            get() = c.incrementAndGet()
    }


}
