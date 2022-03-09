package com.padi.warehouse

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.padi.warehouse.activities.MainActivity
import com.padi.warehouse.model.Item
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter

class ExpiredItemsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val mBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stock)
            .setContentTitle("Expired Items")
            .setContentText("Some items are expired in your warehouse")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val itemsRef = database.getReference("items").child(user?.uid.toString())

        itemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                // Not used
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val today = now()
                for (itemSnapshot in dataSnapshot.children) {
                    val item = itemSnapshot.getValue(Item::class.java)
                    if (!item?.exp_date.isNullOrEmpty()) {
                        val date = LocalDate.parse(item?.exp_date, dateFormatter)

                        if (date.isBefore(today)) {
                            with(NotificationManagerCompat.from(applicationContext)) {
                                // notificationId is a unique int for each notification that you must define
                                notify(0, mBuilder.build())
                            }
                            break
                        }
                    }
                }
            }
        })

        // Indicate success or failure with your return value:
        return Result.success()
    }

}