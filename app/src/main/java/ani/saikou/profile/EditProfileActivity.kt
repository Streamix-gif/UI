package ani.saikou.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import ani.saikou.databinding.ActivityEditProfileBinding
import ani.saikou.loadImage

class EditProfileActivity : AppCompatActivity() {
    private var selectedAvatar: Uri? = null
    private var selectedBanner: Uri? = null
    private lateinit var binding: ActivityEditProfileBinding

    private val avatarPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        persistUri(uri)
        selectedAvatar = uri
        binding.editAvatar.loadImage(uri.toString())
        binding.avatarChoiceOne.loadImage(uri.toString())
    }

    private val bannerPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        persistUri(uri)
        selectedBanner = uri
        binding.editBanner.loadImage(uri.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentAvatar = AppProfile.avatar(this)
        val currentBanner = AppProfile.banner(this)
        selectedAvatar = currentAvatar
        selectedBanner = currentBanner

        binding.editProfileBack.setOnClickListener { finish() }
        binding.avatarChoiceOne.setOnClickListener { avatarPicker.launch(arrayOf("image/*")) }
        binding.avatarAddCustom.setOnClickListener { avatarPicker.launch(arrayOf("image/*")) }
        binding.editBannerButton.setOnClickListener { bannerPicker.launch(arrayOf("image/*")) }

        binding.editUsername.setText(AppProfile.nickname(this))
        binding.editBio.setText(AppProfile.bio(this))

        currentAvatar?.let {
            binding.editAvatar.loadImage(it.toString())
            binding.avatarChoiceOne.loadImage(it.toString())
        }
        currentBanner?.let { binding.editBanner.loadImage(it.toString()) }

        binding.editProfileSave.setOnClickListener {
            AppProfile.save(
                context = this,
                nickname = binding.editUsername.text.toString(),
                bio = binding.editBio.text.toString(),
                avatar = selectedAvatar,
                banner = selectedBanner
            )
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun persistUri(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Some document providers do not offer persistable permissions.
        }
    }
}
