package dead.crumbs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dead.crumbs.data.RSSIRepository

class RSSIViewModelFactory(private val rssiRepository: RSSIRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return QuotesViewModel(rssiRepository) as T
    }
}