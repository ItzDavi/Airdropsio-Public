package com.example.airdropsio

import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class NotificationService : FirebaseMessagingService() {
    //Notifications service to customize notification from Firebase Cloud Messaging for the custom Node.js notification server
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        notify(remoteMessage.notification!!.title, remoteMessage.notification!!.body)
    }

    //Function to create the notification
    private fun notify(title: String?, message: String?) {
        //Build the notification
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "0000")
            .setSmallIcon(R.drawable.airdropsio_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)

        //Send the notification
        val managerCompat = NotificationManagerCompat.from(this)
        managerCompat.notify(123, builder.build())
    }
}