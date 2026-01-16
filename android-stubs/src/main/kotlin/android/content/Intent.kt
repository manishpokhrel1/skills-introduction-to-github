package android.content

class Intent {
    companion object {
        const val ACTION_CALL = "android.intent.action.CALL"
        const val ACTION_MAIN = "android.intent.action.MAIN"
        const val CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER"
        const val FLAG_ACTIVITY_NEW_TASK = 0x10000000
    }

    constructor()
    constructor(action: String?)
    fun addCategory(cat: String) {}
}
