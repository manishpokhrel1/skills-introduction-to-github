package com.daami

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("dami_prefs", MODE_PRIVATE)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val autoRead = Switch(this).apply {
            text = "Auto screen read"
            isChecked = prefs.getBoolean("auto_read_enabled", false)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("auto_read_enabled", checked).apply() }
        }

        val verbosity = Spinner(this).apply {
            val options = listOf("Short summary", "Detailed summary")
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, options)
            val cur = prefs.getString("auto_read_verbosity", "Short summary")
            setSelection(options.indexOf(cur))
            setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                    prefs.edit().putString("auto_read_verbosity", options[position]).apply()
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            })
        }

        val openAcc = Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        val perApp = Button(this).apply {
            text = "Manage per-app auto-read"
            setOnClickListener {
                val intent = Intent(this@SettingsActivity, PerAppSettingsActivity::class.java)
                startActivity(intent)
            }
        }

        val done = Button(this).apply {
            text = "Done"
            setOnClickListener { finish() }
        }

        layout.addView(autoRead)
        layout.addView(verbosity)
        layout.addView(perApp)
        layout.addView(openAcc)
        layout.addView(done)

        setContentView(layout)
    }
}
