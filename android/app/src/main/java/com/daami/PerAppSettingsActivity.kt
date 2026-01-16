package com.daami

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch

class PerAppSettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("dami_prefs", MODE_PRIVATE)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val header = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val enabledCountView = android.widget.TextView(this).apply {
            text = "Enabled: 0"
            setPadding(8, 8, 8, 8)
        }
        val refreshBtn = Button(this).apply { text = "Refresh" }
        topRow.addView(enabledCountView)
        topRow.addView(refreshBtn)

        // Search box to filter apps by name or package
        val searchBox = android.widget.EditText(this).apply {
            hint = "Search apps..."
            setPadding(8, 8, 8, 8)
        }

        header.addView(topRow)
        header.addView(searchBox)
        layout.addView(header)

        // Query launchable apps only
        val pm = packageManager
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        val apps = pm.queryIntentActivities(intent, 0)

        // Provide quick actions
        val enableAll = Button(this).apply {
            text = "Enable auto-read for all"
            setOnClickListener {
                for (ri in apps) {
                    val pkg = ri.activityInfo.packageName
                    prefs.edit().putBoolean("auto_read_app_$pkg", true).apply()
                }
            }
        }

        val disableAll = Button(this).apply {
            text = "Disable auto-read for all"
            setOnClickListener {
                for (ri in apps) {
                    val pkg = ri.activityInfo.packageName
                    prefs.edit().putBoolean("auto_read_app_$pkg", false).apply()
                }
            }
        }

        layout.addView(enableAll)
        layout.addView(disableAll)

        // Container for app items so we can refresh easily
        val itemsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        layout.addView(itemsContainer)

        fun populateItems(query: String? = null) {
            itemsContainer.removeAllViews()
            var enabledCount = 0
            val q = query?.trim()?.lowercase()
            for (ri in apps) {
                val pkg = ri.activityInfo.packageName
                val label = ri.loadLabel(pm)?.toString() ?: pkg
                val key = "auto_read_app_$pkg"
                val checked = prefs.getBoolean(key, false)
                // filter by query
                if (q != null && q.isNotEmpty()) {
                    val combined = (label + " " + pkg).lowercase()
                    if (!combined.contains(q)) continue
                }
                if (checked) enabledCount++
                val sw = Switch(this).apply {
                    text = label
                    isChecked = checked
                    setOnCheckedChangeListener { _, checked ->
                        prefs.edit().putBoolean(key, checked).apply()
                        // update count immediately
                        val current = enabledCountView.text.toString().removePrefix("Enabled: ").toIntOrNull() ?: 0
                        enabledCountView.text = "Enabled: ${if (checked) current + 1 else maxOf(0, current - 1)}"
                    }
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                itemsContainer.addView(sw)
            }
            enabledCountView.text = "Enabled: $enabledCount"
        }

        refreshBtn.setOnClickListener { populateItems(searchBox.text.toString()) }

        // wire search box to filter as user types
        searchBox.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                populateItems(s?.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // initial population
        populateItems()

        val done = Button(this).apply { text = "Done"; setOnClickListener { finish() } }
        layout.addView(done)

        scroll.addView(layout)
        setContentView(scroll)
    }
}
