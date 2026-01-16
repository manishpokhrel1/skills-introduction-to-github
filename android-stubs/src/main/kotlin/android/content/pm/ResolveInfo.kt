package android.content.pm

class ResolveInfo {
    var activityInfo: ActivityInfo = ActivityInfo()
    fun loadLabel(pm: PackageManager): CharSequence? = null
}

class ActivityInfo {
    var packageName: String = ""
}
