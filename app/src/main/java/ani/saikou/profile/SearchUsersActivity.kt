package ani.saikou.profile

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SearchUsersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = SocialUi.screen(this, "Search Users", "Find friends by username or ID.")
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 0, 16, 8) }
        body.addView(SocialUi.text(this, "⌕  Hana", 13f).apply { setPadding(16, 14, 16, 14); alpha = .8f })
        listOf("Hana" to "@hana_ka", "Hanami" to "@phanami", "Hana021" to "@hana021", "HanaSky" to "@hanasky", "Hana_chan" to "@hana_chan").forEach { (n, h) ->
            val c = SocialUi.card(this, 14)
            c.addView(SocialUi.row(this, n, h, "Add Friend") {
                startActivity(android.content.Intent(this, UserProfileActivity::class.java).putExtra("user", n))
            })
            body.addView(c, LinearLayout.LayoutParams(-1, 68).apply { topMargin = 7 })
        }
        root.addView(android.widget.ScrollView(this).apply { addView(body) }, LinearLayout.LayoutParams(-1, 0, 1f))
        SocialUi.addBottomNav(this, root)
        setContentView(root)
    }
}