package dead.crumbs.ui

import androidx.lifecycle.ViewModel
import dead.crumbs.data.RSSIDist
import dead.crumbs.data.RSSIRepository

class RSSIViewModel(private val rssiRepository: RSSIRepository)
    : ViewModel() {

    
    fun getRSSIs() = rssiRepository.getRSSIs()

    fun addRSSI(rssiDist: RSSIDist) = rssiRepository.addRSSI(rssiDist)


}