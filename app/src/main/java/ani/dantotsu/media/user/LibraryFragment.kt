package ani.dantotsu.media.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Menu
import android.view.inputmethod.InputMethodManager
import android.content.Context
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import ani.dantotsu.R
import ani.dantotsu.Refresh
import ani.dantotsu.databinding.ActivityListBinding
import ani.dantotsu.settings.saving.PrefManager
import ani.dantotsu.settings.saving.PrefName
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment() {
    private var _binding: ActivityListBinding? = null
    private val binding get() = _binding!!
    private val model: ListViewModel by viewModels()
    private var selectedTabIdx = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listed.visibility = View.GONE
        binding.listTitle.text = getString(R.string.library)
        binding.listTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { selectedTabIdx = tab?.position ?: 0 }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        model.getLists().observe(viewLifecycleOwner) {
            val defaultKeys = listOf("Reading","Watching","Completed","Paused","Dropped","Planning","Favourites","Rewatching","Rereading","All")
            val userKeys = resources.getStringArray(R.array.keys)
            if (it != null) {
                binding.listProgressBar.visibility = View.GONE
                binding.listViewPager.adapter = ListViewPagerAdapter(it.size, false, requireActivity())
                val keys = it.keys.toList().map { key -> userKeys.getOrNull(defaultKeys.indexOf(key)) ?: key }
                val values = it.values.toList()
                TabLayoutMediator(binding.listTabLayout, binding.listViewPager) { tab, position ->
                    tab.text = "${keys[position]} (${values[position].size})"
                }.attach()
                if (it.isNotEmpty()) binding.listViewPager.setCurrentItem(selectedTabIdx.coerceIn(0, it.size - 1), false)
            }
        }

        val live = Refresh.activity.getOrPut(hashCode()) { androidx.lifecycle.MutableLiveData(true) }
        live.observe(viewLifecycleOwner) {
            if (it) {
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val userId = ani.dantotsu.connections.anilist.Anilist.userid ?: 0
                        model.loadLists(true, userId)
                    }
                    live.postValue(false)
                }
            }
        }

        if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) binding.listSort.visibility = View.GONE
        binding.listSort.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            popup.setOnMenuItemClickListener { item ->
                val sort = when (item.itemId) {
                    R.id.score -> "score"
                    R.id.title -> "title"
                    R.id.updated -> "updatedAt"
                    R.id.release -> "release"
                    else -> null
                }
                PrefManager.setVal(PrefName.AnimeListSortOrder, sort ?: "")
                binding.listProgressBar.visibility = View.VISIBLE
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    model.loadLists(true, ani.dantotsu.connections.anilist.Anilist.userid ?: 0, sort)
                }
                true
            }
            popup.inflate(R.menu.list_sort_menu)
            popup.show()
        }
        binding.filter.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            popup.menu.add(Menu.NONE, 0, Menu.NONE, "All")
            model.getAllGenres().forEachIndexed { index, genre -> popup.menu.add(1, index + 1, Menu.NONE, genre) }
            model.getAllTags().forEachIndexed { index, tag -> popup.menu.add(2, index + 10000, Menu.NONE, tag) }
            popup.setOnMenuItemClickListener { item ->
                when (item.groupId) {
                    0 -> model.unfilterLists()
                    1 -> model.filterLists(item.title.toString())
                    2 -> model.filterListsByTag(item.title.toString())
                }
                true
            }
            popup.show()
        }
        binding.random.setOnClickListener {
            val current = binding.listTabLayout.selectedTabPosition
            (requireActivity().supportFragmentManager.findFragmentByTag("f$current") as? ListFragment)?.randomOptionClick()
        }
        binding.search.setOnClickListener {
            val visible = binding.searchView.isVisible
            binding.searchView.visibility = if (visible) View.GONE else View.VISIBLE
            if (!visible) {
                binding.searchViewText.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.searchViewText, InputMethodManager.SHOW_IMPLICIT)
            } else {
                binding.searchViewText.text.clear()
                model.unfilterLists()
            }
        }
        binding.searchViewText.addTextChangedListener { model.searchLists(binding.searchViewText.text.toString()) }
    }

    override fun onDestroyView() {
        Refresh.activity.remove(hashCode())
        binding.listViewPager.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
