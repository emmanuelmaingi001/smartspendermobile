package com.smartspender.app

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationReceiver : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return

        // Target Gmail or Outlook notifications
        if (packageName == "com.google.android.gm" || packageName == "com.microsoft.office.outlook") {
            val extras = sbn.notification.extras
            val text = extras.getCharSequence("android.text")?.toString() ?: ""

            // Check if it's a transaction alert from a bank or M-PESA email
            if (text.contains("KES", ignoreCase = true) || text.contains("Ksh", ignoreCase = true)) {
                val amount = extractAmount(text)

                // Basic logic: if it mentions 'credit' or 'received', it's income
                val type = if (text.contains("Credit", true) || text.contains("Received", true)) {
                    "Income"
                } else {
                    "Expense"
                }

                val updateIntent = Intent("com.smartspender.UPDATE_UI")
                updateIntent.putExtra("amount", amount)
                updateIntent.putExtra("type", "Email: $type")
                sendBroadcast(updateIntent)
            }
        }
    }

    private fun extractAmount(message: String): Double {
        val regex = Regex("(?:KES|Ksh)\\s?([\\d,.]+)")
        val match = regex.find(message)
        return match?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
    }
}