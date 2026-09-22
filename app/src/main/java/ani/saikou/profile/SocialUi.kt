package ani.saikou.profile

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton

object SocialUi {
    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    fun text(context: Context, value: String, size: Float, bold: Boolean = false): TextView =
        TextView(context).apply {
            text = value
            textSize = size
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.WHITE)
        }

    fun card(context: Context, radius: Int = 18): MaterialCardView =
        MaterialCardView(context).apply {
            setCardBackgroundColor(Color.rgb(18, 24, 42))
            strokeWidth = dp(context, 1)
            strokeColor = Color.rgb(78, 73, 150)
            setRadius(dp(context, radius).toFloat())
        }

    fun button(context: Context, label: String, primary: Boolean = false): MaterialButton =
        MaterialButton(context).apply {
            text = label
            isAllCaps = false
            setCornerRadius(dp(context, 14))
            minHeight = dp(context, 48)
            if (primary) {
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(125, 77, 235))
                setTextColor(Color.WHITE)
            }
        }

    fun avatar(context: Context, label: String): TextView =
        TextView(context).apply {
            text = label.take(1).uppercase()
            gravity = Gravity.CENTER
            textSize = 16f
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(70, 72, 100))
            }
        }

    fun screen(activity: AppCompatActivity, title: String, subtitle: String? = null): LinearLayout {
        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(7, 12, 24))
        }
        val top = LinearLayout(activity).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(activity, 12), dp(activity, 14), dp(activity, 12), dp(activity, 8))
        }
        val back = text(activity, "‹", 34f)
        top.addView(back, LinearLayout.LayoutParams(dp(activity, 42), dp(activity, 48)))
        val titles = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        titles.addView(text(activity, title, 21f, true))
        if (!subtitle.isNullOrBlank()) titles.addView(text(activity, subtitle, 12f).apply { alpha = .65f })
        top.addView(titles, LinearLayout.LayoutParams(0, -2, 1f))
        back.setOnClickListener { activity.finish() }
        root.addView(top)
        return root
    }

    fun row(context: Context, name: String, status: String, action: String? = null, onClick: (() -> Unit)? = null): LinearLayout {
        val row = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 12), dp(context, 8), dp(context, 8), dp(context, 8))
            if (onClick != null) setOnClickListener { onClick() }
        }
        row.addView(avatar(context, name), LinearLayout.LayoutParams(dp(context, 42), dp(context, 42)))
        val info = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 10), 0, dp(context, 6), 0)
        }
        info.addView(text(context, name, 14f, true))
        info.addView(text(context, status, 11f).apply { alpha = .62f })
        row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
        if (action != null) row.addView(button(context, action, true), LinearLayout.LayoutParams(dp(context, 92), dp(context, 44)))
        return row
    }
}