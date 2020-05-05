package dead.crumbs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dead.crumbs.data.LocationRepository
import dead.crumbs.data.UserRepository


class LocationViewModelFactory(private val locationRepository: LocationRepository, private val userRepository: UserRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return GPSViewModel(locationRepository, userRepository) as T
    }
}