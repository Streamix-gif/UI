package ani.dantotsu.media.user

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ani.dantotsu.R
import ani.dantotsu.databinding.DialogLibraryFilterBinding

class LibraryFilterDialogFragment : DialogFragment() {

    private var _binding: DialogLibraryFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogLibraryFilterBinding.inflate(LayoutInflater.from(requireContext()))

        val genres = arguments?.getStringArrayList(ARG_GENRES).orEmpty()
        binding.libraryGenre.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                listOf("All") + genres
            )
        )
        binding.libraryScore.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                listOf("All", "9+", "8+", "7+", "6+")
            )
        )

        binding.sortRecentlyAdded.isChecked = true
        binding.statusAll.isChecked = true

        binding.libraryFilterClose.setOnClickListener { dismiss() }
        binding.libraryFilterReset.setOnClickListener {
            binding.sortRecentlyAdded.isChecked = true
            binding.statusAll.isChecked = true
            binding.libraryGenre.setText("All", false)
            binding.libraryScore.setText("All", false)
        }

        binding.libraryFilterApply.setOnClickListener {
            val sort = when (binding.librarySortGroup.checkedChipId) {
                R.id.sortUpdated -> "updatedAt"
                R.id.sortTitleAsc -> "title"
                R.id.sortTitleDesc -> "title_desc"
                R.id.sortProgress -> "progress"
                R.id.sortScore -> "score"
                else -> null
            }
            val status = when (binding.libraryStatusGroup.checkedChipId) {
                R.id.statusWatching -> "Watching"
                R.id.statusPlanning -> "Planning"
                R.id.statusCompleted -> "Completed"
                R.id.statusPaused -> "Paused"
                R.id.statusDropped -> "Dropped"
                R.id.statusRewatching -> "Rewatching"
                else -> "All"
            }
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply {
                    putString(KEY_SORT, sort)
                    putString(KEY_STATUS, status)
                    putString(KEY_GENRE, binding.libraryGenre.text?.toString().orEmpty())
                    putString(KEY_SCORE, binding.libraryScore.text?.toString().orEmpty())
                }
            )
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_KEY = "library_filter_request"
        const val KEY_SORT = "sort"
        const val KEY_STATUS = "status"
        const val KEY_GENRE = "genre"
        const val KEY_SCORE = "score"
        private const val ARG_GENRES = "genres"

        fun newInstance(genres: List<String>) =
            LibraryFilterDialogFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_GENRES, ArrayList(genres))
                }
            }
    }
}
