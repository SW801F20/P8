package dead.crumbs.ui

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dead.crumbs.data.MapsRepository

class SingletonMapsViewModelFactory(private val mapsRepository: MapsRepository)
    : ViewModelProvider.NewInstanceFactory() {

    var mapsViewModel: ViewModel? = null



    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (mapsViewModel == null)
            mapsViewModel = MapsViewModel(mapsRepository)
        return mapsViewModel as T
    }
}