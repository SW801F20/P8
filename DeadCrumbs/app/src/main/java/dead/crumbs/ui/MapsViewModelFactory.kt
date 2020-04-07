package dead.crumbs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dead.crumbs.data.MapsRepository

class MapsViewModelFactory(private val mapsRepository: MapsRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapsViewModel(mapsRepository) as T
    }
}