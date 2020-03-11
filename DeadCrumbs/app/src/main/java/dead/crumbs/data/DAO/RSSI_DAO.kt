package dead.crumbs.data.DAO

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dead.crumbs.data.RSSI

class RSSI_DAO {
    // A fake database table
    private val rssiList = mutableListOf<RSSI>()
    // MutableLiveData is from the Architecture Components Library
    // LiveData can be observed for changes
    private val rssis = MutableLiveData<List<RSSI>>()

    init {
        // Immediately connect the now empty quoteList
        // to the MutableLiveData which can be observed
        rssis.value = rssiList
    }

    fun add_rssi(rssi_val: RSSI) {
        rssiList.add(rssi_val)
        // After adding a rssi to the "database",
        // update the value of MutableLiveData
        // which will notify its active observers
        rssis.value = rssiList
    }

    // Casting MutableLiveData to LiveData because its value
    // shouldn't be changed from other classes
    fun getRSSIs() = rssis as LiveData<List<RSSI>>
}