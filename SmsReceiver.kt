package com.smartspender.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import java.util.regex.Pattern

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<*>
                for (pdu in pdus) {
                    val message = SmsMessage.createFromPdu(pdu as ByteArray)
                    val body = message.displayMessageBody

                    // Filter for M-PESA
                    if (body.contains("MPESA", ignoreCase = true) || body.contains("confirmed", ignoreCase = true)) {
                        // PASS CONTEXT HERE so the function can send the broadcast
                        parseMpesaMessage(body, context)
                    }
                }
            }
        }
    }

    private fun parseMpesaMessage(message: String, context: Context) {
        try {
            // Regex to find Amount (e.g., Ksh1,250.00)
            val amountPattern = Pattern.compile("Ksh([\\d,]+\\.\\d{2})")
            val amountMatcher = amountPattern.matcher(message)

            if (amountMatcher.find()) {
                val amountStr = amountMatcher.group(1)?.replace(",", "") ?: "0.00"
                val amount = amountStr.toFloat()

                // Determine the category for your Pie Chart
                val category = when {
                    message.contains("received", ignoreCase = true) -> "Money Received"
                    message.contains("Paybill", ignoreCase = true) -> "Paybill"
                    message.contains("Buy Goods", ignoreCase = true) -> "Pay to Till"
                    message.contains("Pochi", ignoreCase = true) -> "Pochi"
                    message.contains("sent to", ignoreCase = true) -> "Send Money"
                    else -> "Other"
                }

                Log.d("SmartSpender", "SUCCESS: Found $category of Ksh $amount")

                // Send data to MainActivity
                val updateIntent = Intent("com.smartspender.UPDATE_CHART")
                updateIntent.putExtra("amount", amount)
                updateIntent.putExtra("category", category)
                updateIntent.putExtra("isIncome", category == "Money Received")
                context.sendBroadcast(updateIntent)
            }
        } catch (e: Exception) {
            Log.e("SmartSpender", "Error parsing M-PESA: ${e.message}")
        }
    }
}