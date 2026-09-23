package ani.saikou.themes

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.view.View
import android.view.Window
import android.view.WindowManager
import ani.saikou.R
import ani.saikou.settings.saving.PrefManager
import ani.saikou.settings.saving.PrefName
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

class ThemeManager(private val context: Activity) {

    fun applyTheme(fromImage: Bitmap? = null) {
        val useOLED = PrefManager.getVal(PrefName.UseOLED) && isDarkThemeActive(context)

        if (fromImage != null) {
            val options = DynamicColorsOptions.Builder().apply {
                if (fromImage != null) setContentBasedSource(fromImage)
                if (useOLED) setThemeOverlay(R.style.AppTheme_Streamix_Amoled)
                if (PrefManager.getVal(PrefName.UseSystemFont)) {
                    setThemeOverlay(R.style.ThemeOverlay_Streamix_SystemFont)
                }
            }.build()
            DynamicColors.applyToActivityIfAvailable(context, options)
        }

        val theme = Theme.fromString(PrefManager.getVal(PrefName.Theme))
        val themeId = when (theme) {
            Theme.BLUE -> if (useOLED) R.style.Theme_Streamix_BlueOLED else R.style.Theme_Streamix_Blue
            Theme.GREEN -> if (useOLED) R.style.Theme_Streamix_GreenOLED else R.style.Theme_Streamix_Green
            Theme.PURPLE -> if (useOLED) R.style.Theme_Streamix_PurpleOLED else R.style.Theme_Streamix_Purple
            Theme.PINK -> if (useOLED) R.style.Theme_Streamix_PinkOLED else R.style.Theme_Streamix_Pink
            Theme.ORIAX -> if (useOLED) R.style.Theme_Streamix_OriaxOLED else R.style.Theme_Streamix_Oriax
            Theme.SAIKOU -> if (useOLED) R.style.Theme_Streamix_SaikouOLED else R.style.Theme_Streamix_Saikou
            Theme.RED -> if (useOLED) R.style.Theme_Streamix_RedOLED else R.style.Theme_Streamix_Red
            Theme.LAVENDER -> if (useOLED) R.style.Theme_Streamix_LavenderOLED else R.style.Theme_Streamix_Lavender
            Theme.OCEAN -> if (useOLED) R.style.Theme_Streamix_OceanOLED else R.style.Theme_Streamix_Ocean
            Theme.MONOCHROME -> if (useOLED) R.style.Theme_Streamix_MonochromeOLED else R.style.Theme_Streamix_Monochrome
        }

        val window = context.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        context.setTheme(themeId)

        if (PrefManager.getVal(PrefName.UseSystemFont)) {
            context.theme.applyStyle(R.style.ThemeOverlay_Streamix_SystemFont, true)
        }
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
    }

    fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win: Window = activity.window
        val params = win.attributes
        params.flags = if (on) params.flags or bits else params.flags and bits.inv()
        win.attributes = params
    }

    private fun isDarkThemeActive(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    enum class Theme(val theme: String) {
            BLUE("BLUE"),
            GREEN("GREEN"),
            PURPLE("PURPLE"),
            PINK("PINK"),
            ORIAX("ORIAX"),
            SAIKOU("SAIKOU"),
            RED("RED"),
            LAVENDER("LAVENDER"),
            OCEAN("OCEAN"),
            MONOCHROME("MONOCHROME (BETA)");

        companion object {
            fun fromString(value: String): Theme =
                entries.find { it.theme == value } ?: PURPLE
        }
    }
}