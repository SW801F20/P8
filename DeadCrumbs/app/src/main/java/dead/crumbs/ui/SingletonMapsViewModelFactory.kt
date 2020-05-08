package dead.crumbs.ui

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dead.crumbs.data.LocationRepository
import dead.crumbs.data.UserRepository

class SingletonMapsViewModelFactory(private val locationRepository: LocationRepository,
                                    private val userRepository: UserRepository)
    : ViewModelProvider.NewInstanceFactory() {

    var mapsViewModel: ViewModel? = null



    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (mapsViewModel == null)
            mapsViewModel = MapsViewModel(locationRepository, userRepository)
        return mapsViewModel as T
    }
}