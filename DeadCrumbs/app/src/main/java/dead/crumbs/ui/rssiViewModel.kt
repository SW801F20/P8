package dead.crumbs.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.ViewModel
import dead.crumbs.data.RSSI
import dead.crumbs.data.RSSIRepository

class RSSIViewModel(private val rssiRepository: RSSIRepository)
    : ViewModel() {


    fun getRSSIs() = rssiRepository.getRSSIs()

    fun addRSSI(rssi: RSSI) = rssiRepository.addRSSI(rssi)


}