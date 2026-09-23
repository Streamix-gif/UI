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
        val oled = PrefManager.getVal(PrefName.UseOLED) && isDarkThemeActive(context)
        val useCustom = PrefManager.getVal(PrefName.UseCustomTheme)
        val custom = PrefManager.getVal(PrefName.CustomThemeInt)
        val useMaterial = PrefManager.getVal(PrefName.UseMaterialYou)

        if (useMaterial || useCustom || fromImage != null) {
            val builder = DynamicColorsOptions.Builder()
            if (fromImage != null) builder.setContentBasedSource(fromImage)
            else if (useCustom) builder.setContentBasedSource(custom)
            DynamicColors.applyToActivityIfAvailable(context, builder.build())
        }

        context.setTheme(if (oled) R.style.Theme_Streamix_OLED else R.style.Theme_Streamix)
        context.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        context.window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
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
}
