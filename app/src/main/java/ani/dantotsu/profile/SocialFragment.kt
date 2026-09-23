package ani.dantotsu.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import ani.dantotsu.R
import ani.dantotsu.databinding.FragmentSocialBinding
import ani.dantotsu.profile.activity.ActivityFragment
import ani.dantotsu.profile.activity.ActivityFragment.Companion.ActivityType

class SocialFragment : Fragment() {
    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showFeed(ActivityType.USER)
        binding.socialMyActivity.setOnClickListener { showFeed(ActivityType.USER) }
        binding.socialGlobal.setOnClickListener { showFeed(ActivityType.GLOBAL) }
        binding.socialRefresh.setOnRefreshListener { showFeed(ActivityType.USER) }
    }

    private fun showFeed(type: ActivityType) {
        childFragmentManager.commit {
            replace(R.id.socialFeedContainer, ActivityFragment.newInstance(type))
        }
        binding.socialRefresh.isRefreshing = false
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}