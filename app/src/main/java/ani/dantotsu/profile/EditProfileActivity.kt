package ani.dantotsu.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ani.dantotsu.databinding.ActivityEditProfileBinding
import ani.dantotsu.initActivity
import ani.dantotsu.themes.ThemeManager

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager(this).applyTheme()
        initActivity(this)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editProfileBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
