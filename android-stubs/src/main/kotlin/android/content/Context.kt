package android.content

open class Context {
    open fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        return object : SharedPreferences {
            override fun getBoolean(key: String, defValue: Boolean): Boolean = defValue
        }
    }

    open fun startActivity(intent: Intent) {}
}

interface SharedPreferences {
    fun getBoolean(key: String, defValue: Boolean): Boolean
}
