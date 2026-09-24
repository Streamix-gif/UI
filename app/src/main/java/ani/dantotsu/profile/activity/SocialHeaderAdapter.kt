package ani.dantotsu.profile.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ani.dantotsu.databinding.ItemSocialHeaderBinding

class SocialHeaderAdapter : RecyclerView.Adapter<SocialHeaderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemSocialHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = Unit

    override fun getItemCount(): Int = 1

    class ViewHolder(
        binding: ItemSocialHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root)
}
