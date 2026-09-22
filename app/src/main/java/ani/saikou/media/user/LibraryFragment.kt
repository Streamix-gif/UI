package ani.saikou.media.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import ani.saikou.connections.anilist.Anilist
import ani.saikou.media.MediaDetailsActivity
import ani.saikou.databinding.FragmentLibraryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

class LibraryFragment : Fragment() {
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private val model: ListViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Anilist.userid == null) {
            binding.listProgressBar.visibility = View.GONE
            return
        }

        binding.search.setOnClickListener {
            val visible = binding.searchView.visibility == View.VISIBLE
            binding.searchView.visibility = if (visible) View.GONE else View.VISIBLE
            if (!visible) {
                binding.searchViewText.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.searchViewText, InputMethodManager.SHOW_IMPLICIT)
            } else {
                binding.searchViewText.text?.clear()
                model.unfilterLists()
            }
        }

        binding.searchViewText.setOnEditorActionListener { _, _, _ ->
            model.searchLists(binding.searchViewText.text?.toString().orEmpty())
            false
        }

        binding.filter.setOnClickListener {
            val popup = PopupMenu(requireContext(), binding.filter)
            popup.menu.add(0, 0, 0, "All")
            model.getAllGenres().forEachIndexed { index, genre ->
                popup.menu.add(1, index + 1, index + 1, genre)
            }
            model.getAllTags().forEachIndexed { index, tag ->
                popup.menu.add(2, index + 10000, index + 10000, tag)
            }
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

        binding.listSort.setOnClickListener {
            val popup = PopupMenu(requireContext(), binding.listSort)
            popup.menu.add(0, 0, 0, "Score")
            popup.menu.add(0, 1, 1, "Title")
            popup.menu.add(0, 2, 2, "Updated")
            popup.menu.add(0, 3, 3, "Release")
            popup.setOnMenuItemClickListener { item ->
                val sort = when (item.itemId) {
                    0 -> "score"
                    1 -> "title"
                    2 -> "updatedAt"
                    3 -> "release"
                    else -> null
                }
                binding.listProgressBar.visibility = View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        model.loadLists(true, Anilist.userid ?: return@withContext, sort)
                    }
                }
                true
            }
            popup.show()
        }

        binding.random.setOnClickListener {
            val media = model.getLists().value?.values?.flatten()?.randomOrNull() ?: return@setOnClickListener
            startActivity(
                Intent(requireContext(), MediaDetailsActivity::class.java)
                    .putExtra("media", media as Serializable)
            )
        }

        model.getLists().observe(viewLifecycleOwner) { lists ->
            if (lists == null) return@observe
            binding.listProgressBar.visibility = View.GONE
            binding.listViewPager.adapter = ListViewPagerAdapter(lists.size, false, requireActivity())
            val keys = lists.keys.toList()
            val values = lists.values.toList()
            TabLayoutMediator(binding.listTabLayout, binding.listViewPager) { tab, position ->
                tab.text = keys[position] + " (" + values[position].size + ")"
            }.attach()
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                model.loadLists(true, Anilist.userid ?: return@withContext)
            }
        }
    }

    override fun onDestroyView() {
        binding.listViewPager.adapter = null
        _binding = null
        super.onDestroyView()
    }
}