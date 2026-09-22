package ani.saikou.profile

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class UserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = intent.getStringExtra("user") ?: "Hana"
        val root = SocialUi.screen(this, user, "@"+user.lowercase())
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val hero = SocialUi.card(this, 18)
        hero.setCardBackgroundColor(0xff17213a.toInt())
        val h = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 24, 20, 18) }
        h.addView(SocialUi.avatar(this, user), LinearLayout.LayoutParams(72, 72))
        h.addView(SocialUi.text(this, user, 22f, true).apply { setPadding(0, 10, 0, 0) })
        h.addView(SocialUi.text(this, "Just a random anime lover.", 12f).apply { alpha = .65f })
        h.addView(SocialUi.text(this, "128 Friends      342 Followers      56 Following", 11f, true).apply { setPadding(0, 18, 0, 8) })
        val actions = LinearLayout(this)
        actions.addView(SocialUi.button(this, "Add Friend", true), LinearLayout.LayoutParams(0, 48, 1f))
        actions.addView(SocialUi.button(this, "Message"), LinearLayout.LayoutParams(0, 48, 1f).apply { leftMargin = 8 })
        h.addView(actions)
        hero.addView(h)
        body.addView(hero, LinearLayout.LayoutParams(-1, 250).apply { setMargins(16, 4, 16, 0) })
        body.addView(SocialUi.text(this, "Currently Watching", 17f, true).apply { setPadding(20, 22, 20, 8) })
        body.addView(SocialUi.card(this, 14).apply { addView(SocialUi.row(this@UserProfileActivity, "Frieren", "Episode 20")) }, LinearLayout.LayoutParams(-1, 66).apply { setMargins(16, 0, 16, 0) })
        body.addView(SocialUi.text(this, "Recent Activity", 17f, true).apply { setPadding(20, 22, 20, 8) })
        body.addView(SocialUi.card(this, 14).apply { addView(SocialUi.row(this@UserProfileActivity, user, "created a Watch Together room")) }, LinearLayout.LayoutParams(-1, 66).apply { setMargins(16, 0, 16, 0) })
        root.addView(android.widget.ScrollView(this).apply { addView(body) }, LinearLayout.LayoutParams(-1, 0, 1f))
        SocialUi.addBottomNav(this, root)
        setContentView(root)
    }
}