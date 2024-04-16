package com.yoodobuzz.medcalldelivery.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.deliveries.DeliveryActivity


class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification!!.title
        val body = remoteMessage.notification!!.body
        val extraData = remoteMessage.data

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, "TAC")
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.logo)
        val intent = Intent(this, DeliveryActivity::class.java)



        val pendingIntent =
            PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder.setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val id = System.currentTimeMillis().toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("TAC", "demo", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(id, notificationBuilder.build())
    }

}