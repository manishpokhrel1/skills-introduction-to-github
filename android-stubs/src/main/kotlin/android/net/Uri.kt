package android.net

class Uri(private val s: String) {
    companion object {
        fun parse(s: String): Uri = Uri(s)
    }
    override fun toString(): String = s
}
