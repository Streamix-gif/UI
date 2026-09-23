package ani.saikou.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import ani.saikou.settings.saving.PrefManager
import ani.saikou.settings.saving.PrefName
import ani.saikou.themes.ThemeManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch

class DantotsuAppearanceActivity : AppCompatActivity() {

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private val prefs by lazy { getSharedPreferences("streamix_appearance", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager(this).applyTheme()

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(22), dp(16), dp(32))
        }

        content.addView(TextView(this).apply {
            text = "‹"
            textSize = 38f
            gravity = Gravity.CENTER
            setTextColor(resolveColor(com.google.android.material.R.attr.colorPrimary))
            setOnClickListener { finish() }
        }, LinearLayout.LayoutParams(dp(52), dp(52)))

        content.addView(TextView(this).apply {
            text = "Appearance"
            textSize = 30f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        content.addView(TextView(this).apply {
            text = "Customize the look and behavior of AniLab"
            textSize = 14f
            alpha = .58f
            setPadding(0, dp(4), 0, dp(20))
        })

        content.addView(label("THEME"))
        val currentTheme = PrefManager.getVal<String>(PrefName.Theme)
        val themeRow = row("Color theme", currentTheme.replaceFirstChar { it.uppercase() }) {
            val themes = ThemeManager.Theme.entries
            val checked = themes.indexOfFirst { it.theme == PrefManager.getVal<String>(PrefName.Theme) }.coerceAtLeast(0)
            MaterialAlertDialogBuilder(this)
                .setTitle("Color theme")
                .setSingleChoiceItems(
                    themes.map { it.theme.replaceFirstChar { c -> c.uppercase() } }.toTypedArray(),
                    checked
                ) { dialog, which ->
                    PrefManager.setVal(PrefName.Theme, themes[which].theme)
                    dialog.dismiss()
                    recreate()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        content.addView(themeRow)

        content.addView(label("MODE"))
        val mode = prefs.getInt("dark_mode", when {
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES -> 2
            else -> 0
        })
        val modeRow = row("Dark mode", when (mode) { 1 -> "Light"; 2 -> "Dark"; else -> "System" }) {
            val names = arrayOf("System", "Light", "Dark")
            MaterialAlertDialogBuilder(this)
                .setTitle("Dark mode")
                .setSingleChoiceItems(names, mode) { dialog, which ->
                    prefs.edit().putInt("dark_mode", which).apply()
                    AppCompatDelegate.setDefaultNightMode(
                        when (which) {
                            1 -> AppCompatDelegate.MODE_NIGHT_NO
                            2 -> AppCompatDelegate.MODE_NIGHT_YES
                            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        }
                    )
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        content.addView(modeRow)

        content.addView(label("DISPLAY"))
        content.addView(switchRow("OLED theme variant", "Use the OLED variant when available", PrefName.UseOLED))
        content.addView(switchRow("System font", "Use the device system font", PrefName.UseSystemFont))
        content.addView(switchRow("Material You", "Use dynamic system colors where supported", PrefName.UseMaterialYou))

        val scroll = ScrollView(this).apply { addView(content) }
        setContentView(scroll)
    }

    private fun switchRow(title: String, description: String, pref: PrefName): MaterialSwitch {
        return MaterialSwitch(this).apply {
            text = "$title\n$description"
            textSize = 15f
            setPadding(dp(12), dp(10), dp(12), dp(10))
            isChecked = PrefManager.getVal(pref)
            setOnCheckedChangeListener { _, checked ->
                PrefManager.setVal(pref, checked)
                recreate()
            }
        }
    }

    private fun row(title: String, value: String, click: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundResource(android.R.drawable.list_selector_background)
            setOnClickListener { click() }
            addView(TextView(context).apply {
                text = title
                textSize = 16f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            addView(TextView(context).apply {
                text = value
                textSize = 12f
                alpha = .58f
                setPadding(0, dp(3), 0, 0)
            })
        }
    }

    private fun label(text: String) = TextView(this).apply {
        this.text = text
        textSize = 12f
        setTextColor(resolveColor(com.google.android.material.R.attr.colorPrimary))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(dp(8), dp(18), dp(8), dp(8))
    }

    private fun resolveColor(attr: Int): Int {
        val value = android.util.TypedValue()
        theme.resolveAttribute(attr, value, true)
        return if (value.resourceId != 0) {
            androidx.core.content.ContextCompat.getColor(this, value.resourceId)
        } else value.data
    }
}
