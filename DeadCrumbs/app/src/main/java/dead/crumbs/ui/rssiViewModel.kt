package dead.crumbs.ui

import androidx.lifecycle.ViewModel
import dead.crumbs.data.BluetoothRSSI
import dead.crumbs.data.RSSIRepository

class RSSIViewModel(private val rssiRepository: RSSIRepository)
    : ViewModel() {

    fun getRSSIs() = rssiRepository.getRSSIs()

    fun addRSSI(rssi: BluetoothRSSI) = rssiRepository.addRSSI(rssi)
}