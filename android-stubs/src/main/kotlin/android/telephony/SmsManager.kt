package android.telephony

class SmsManager {
    companion object {
        fun getDefault(): SmsManager = SmsManager()
    }

    fun sendTextMessage(destAddress: String?, scAddress: String?, text: String?, sentIntent: Any?, deliveryIntent: Any?) {}
}
