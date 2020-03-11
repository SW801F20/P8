package dead.crumbs.ui

import androidx.lifecycle.ViewModel
import dead.crumbs.data.RSSI
import dead.crumbs.data.RSSIRepository

class QuotesViewModel(private val rssiRepository: RSSIRepository)
    : ViewModel() {

    fun getQuotes() = rssiRepository.getRSSIs()

    fun addQuote(rssi: RSSI) = rssiRepository.addQuote(rssi)
}