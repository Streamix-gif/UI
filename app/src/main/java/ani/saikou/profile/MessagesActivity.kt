package ani.saikou.profile

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MessagesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = SocialUi.screen(this, "Messages", "Your conversations.")
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 0, 16, 8) }
        body.addView(SocialUi.text(this, "⌕  Search messages…", 13f).apply { setPadding(16, 14, 16, 14); alpha = .7f })
        listOf("Kael" to "Bro, episode terbaru udah rilis?", "Hana" to "Yuk nonton bareng nanti malam!", "Rynn" to "Gila sih, episode kemarin keren.", "Mizu" to "Siap, nanti aku join.").forEach { (n, s) ->
            val c = SocialUi.card(this, 14)
            c.addView(SocialUi.row(this, n, s, "›") {
                startActivity(android.content.Intent(this, ChatDetailActivity::class.java).putExtra("user", n))
            })
            body.addView(c, LinearLayout.LayoutParams(-1, 68).apply { topMargin = 7 })
        }
        root.addView(android.widget.ScrollView(this).apply { addView(body) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }
}