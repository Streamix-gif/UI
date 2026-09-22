package ani.saikou.profile

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class FriendsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = SocialUi.screen(this, "Friends", "Find friends and see who is online.")
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 0, 16, 8) }
        body.addView(SocialUi.text(this, "⌕  Search users (username or ID)", 13f).apply { setPadding(16, 14, 16, 14); alpha = .7f })
        body.addView(SocialUi.text(this, "My Friends     Requests     Search", 13f, true).apply { setPadding(8, 18, 8, 10) })
        listOf("Shin" to "Watching One Piece E1150", "Kael" to "Online", "Rynn" to "In a Watch Room", "Mizu" to "Online", "Hana" to "Online", "Zed" to "Offline", "Lily" to "Offline").forEach { (n, s) ->
            val c = SocialUi.card(this, 14)
            c.addView(SocialUi.row(this, n, s, "Chat") {
                startActivity(android.content.Intent(this, ChatDetailActivity::class.java).putExtra("user", n))
            })
            body.addView(c, LinearLayout.LayoutParams(-1, 64).apply { topMargin = 6 })
        }
        root.addView(android.widget.ScrollView(this).apply { addView(body) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }
}