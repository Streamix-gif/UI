package ani.saikou

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class NavigationPlaceholderFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TextView(requireContext()).apply {
            text = requireArguments().getString(ARG_TITLE).orEmpty()
            gravity = Gravity.CENTER
            textSize = 20f
        }
    }

    companion object {
        private const val ARG_TITLE = "title"

        fun newInstance(title: String) = NavigationPlaceholderFragment().apply {
            arguments = Bundle().apply { putString(ARG_TITLE, title) }
        }
    }
}
