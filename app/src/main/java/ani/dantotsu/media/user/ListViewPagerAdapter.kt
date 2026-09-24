package ani.dantotsu.media.user

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ani.dantotsu.media.CalendarSchedulePageFragment

class ListViewPagerAdapter(
    private val size: Int,
    private val calendar: Boolean,
    fragment: FragmentActivity
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = size
    override fun createFragment(position: Int): Fragment =
        if (calendar) {
            CalendarSchedulePageFragment.newInstance(position)
        } else {
            ListFragment.newInstance(position, false)
        }
}
