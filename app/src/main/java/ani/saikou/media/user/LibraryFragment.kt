package ani.saikou.media.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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

        val openLibraryFilters = View.OnClickListener {
            LibraryFilterDialogFragment
                .newInstance(model.getAllGenres())
                .show(parentFragmentManager, "library_filters")
        }

        binding.filter.setOnClickListener(openLibraryFilters)
        binding.listSort.setOnClickListener(openLibraryFilters)

        parentFragmentManager.setFragmentResultListener(
            LibraryFilterDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            val sort = result.getString(LibraryFilterDialogFragment.KEY_SORT)
            val status = result.getString(LibraryFilterDialogFragment.KEY_STATUS, "All")
            val genre = result.getString(LibraryFilterDialogFragment.KEY_GENRE, "All")
            val score = result.getString(LibraryFilterDialogFragment.KEY_SCORE, "All")
                .removeSuffix("+")
                .toIntOrNull() ?: 0

            binding.listProgressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                if (sort != null) {
                    withContext(Dispatchers.IO) {
                        model.loadLists(true, Anilist.userid ?: return@withContext, sort)
                    }
                }
                model.applyLibraryFilters(genre, score)
                binding.listProgressBar.visibility = View.GONE

                val lists = model.getLists().value ?: return@launch
                if (status != "All") {
                    val index = lists.keys.indexOfFirst { key ->
                        key.replace("-", "").replace(" ", "")
                            .equals(status.replace("-", "").replace(" ", ""), ignoreCase = true)
                    }
                    if (index >= 0) binding.listViewPager.setCurrentItem(index, true)
                } else {
                    binding.listViewPager.setCurrentItem(0, true)
                }
            }
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