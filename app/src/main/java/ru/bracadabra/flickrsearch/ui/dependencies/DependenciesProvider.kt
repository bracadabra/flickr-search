package ru.bracadabra.flickrsearch.ui.dependencies

import androidx.fragment.app.Fragment

private const val HOLDER_TAG = "holder_tag"

object DependenciesProvider {

    fun with(fragment: Fragment): FlickrSearchDependencies {
        val fragmentManager = fragment.requireFragmentManager()
        var holder = fragmentManager.findFragmentByTag(HOLDER_TAG) as HolderFragment?
        if (holder == null) {
            holder = HolderFragment()
            fragmentManager
                .beginTransaction()
                .add(holder, HOLDER_TAG)
                .commit()
        }

        return holder.dependencies
    }

    class HolderFragment : Fragment() {

        init {
            retainInstance = true
        }

        val dependencies = FlickrSearchDependencies()

    }

}