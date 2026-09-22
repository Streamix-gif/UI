package ani.saikou.profile

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ChatDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = intent.getStringExtra("user") ?: "Kael"
        val root = SocialUi.screen(this, user, "Online")
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(12, 0, 12, 8) }
        val area = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        listOf("Bro, episode terbaru udah rilis?", "Iya, gila sih keren banget.", "Nanti malam nonton bareng?", "Boleh, aku buat room ya.", "Gas!").forEachIndexed { idx, message ->
            val t = SocialUi.text(this, message, 13f).apply {
                setPadding(14, 11, 14, 11)
                setBackgroundColor(if (idx % 2 == 0) 0xff182239.toInt() else 0xff6540c8.toInt())
                gravity = if (idx % 2 == 0) Gravity.START else Gravity.END
            }
            area.addView(t, LinearLayout.LayoutParams(-1, 48).apply { topMargin = 6 })
        }
        body.addView(area, LinearLayout.LayoutParams(-1, 0, 1f))
        body.addView(SocialUi.button(this, "👥  Invite to Watch Together"), LinearLayout.LayoutParams(-1, 48))
        val input = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        input.addView(SocialUi.text(this, "Type a message…", 12f).apply { setPadding(14, 0, 0, 0) }, LinearLayout.LayoutParams(0, 48, 1f))
        input.addView(SocialUi.button(this, "➤", true), LinearLayout.LayoutParams(60, 48))
        body.addView(input)
        root.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))
        SocialUi.addBottomNav(this, root)
        setContentView(root)
    }
}