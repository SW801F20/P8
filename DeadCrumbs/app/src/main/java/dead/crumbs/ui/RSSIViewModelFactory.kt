package dead.crumbs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dead.crumbs.data.RSSIRepository
import dead.crumbs.data.UserRepository

class RSSIViewModelFactory(private val rssiRepository: RSSIRepository, private val userRepository: UserRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RSSIViewModel(rssiRepository, userRepository) as T
    }
}