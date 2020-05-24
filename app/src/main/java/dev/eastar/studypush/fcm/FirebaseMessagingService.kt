package dev.eastar.studypush.fcm

import android.log.Log
import com.crashlytics.android.Crashlytics
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dev.eastar.ktx.toTimeText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import smart.base.PP

class FirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("FCM", token)
        PP.pushToken = token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        log(remoteMessage)
        Crashlytics.log(remoteMessage.toString())
        if (remoteMessage.data.isNullOrEmpty())
            return
        PP.isReadedPush = true

        CoroutineScope(Dispatchers.Main).launch {
            val internalMessageId = getInternalMessageId(remoteMessage)
            Crashlytics.log("푸시받음:$internalMessageId[${remoteMessage.data["type"]}]")
            val data = remoteMessage.data
            val push = Gson().fromJson(data["value"], PushModel::class.java)
            NotificationHelper.sendNotification(applicationContext, push)
        }
    }

    private fun getInternalMessageId(remoteMessage: RemoteMessage): String? = remoteMessage.data["messageId"]

    private fun log(remoteMessage: RemoteMessage) {
        with(remoteMessage) {
            Log.e(messageId)                             //, var messageId: String? = null
            Log.e(messageType)                           //, var messageType: String? = null
            Log.e(collapseKey)                           //, var collapseKey: String = ""
            Log.e(notification)                          //, var notification: String? = null
            Log.e(from)                                  //, var from: String? = null
            Log.e(to)                                    //, var to: String? = null
            Log.e(ttl)                                   //, var ttl: String? = null
            Log.e(sentTime.toTimeText)                   //, var sentTime: Date? = null
            Log.e(System.currentTimeMillis().toTimeText) //, var receiveTime: String? = null
            Log.e(priority)                              //, var priority: String? = null
            Log.e(originalPriority)                      //, var originalPriority: String? = null
            Log.e(data)                                  //, var data: String? = null
            Log.e(toIntent())

            Log.flog(messageId)
            Log.flog(messageType)
            Log.flog(collapseKey)
            Log.flog(notification)
            Log.flog(from)
            Log.flog(to)
            Log.flog(data)
            Log.flog(ttl)
            Log.flog(sentTime)
            Log.flog(priority)
            Log.flog(originalPriority)
            Log.flog(toIntent())
        }
    }
}
