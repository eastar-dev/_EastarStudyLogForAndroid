package dev.eastar.studypush.fcm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.net.URISyntaxException

data class PushModel(
    val dt: Long = System.currentTimeMillis(),
    val alert: String?,
    val alert_link: String?,
    val title: String?,
    val content: String?,
    val content_link: String?,
    val img: String?,
    val img_link: String?,
    val action: List<Action?>?,
    val button: List<Button>?
) {
    data class Action(
        val img: String?,
        val link: String?,
        val text: String?
    )

    data class Button(
        val text: String?,
        val link: String?
    )
}
