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
import com.yoodobuzz.medcalldelivery.activity.splash.SplashActivity
import com.yoodobuzz.medcalldelivery.activity.trackingmap.DeliveredActivity
import com.yoodobuzz.medcalldelivery.activity.trackingmap.DeliveryOneActivity
import com.yoodobuzz.medcalldelivery.activity.trackingmap.DeliveryTwoActivity


class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
        val page =remoteMessage.data["page"]
        println("### page : ${page}")

        val intent: Intent
        // Create an explicit intent for an Activity in your app
        intent = Intent(this, SplashActivity::class.java)
        intent.putExtra("page",page)


        if (page == "1") {
            Intent(this, DeliveryOneActivity::class.java)
        }else if(page=="2"){
           Intent(
                this, DeliveryTwoActivity::class.java
            )

        }else if(page=="3"){
            Intent(this, DeliveryTwoActivity::class.java)

        }else if(page=="4"){
            Intent(this, DeliveredActivity::class.java)

        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)



        // Create a notification channel if necessary
        val channelId = "default_channel_id"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // For Android O and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Default Channel"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}