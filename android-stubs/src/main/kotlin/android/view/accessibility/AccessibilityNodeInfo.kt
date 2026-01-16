package android.view.accessibility

class AccessibilityNodeInfo {
    var text: CharSequence? = null
    val childCount: Int = 0
    fun getChild(i: Int): AccessibilityNodeInfo? = null
}

class AccessibilityEvent {
    val text: MutableList<CharSequence> = mutableListOf()
}
