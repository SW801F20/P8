package dead.crumbs.data.DAO

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dead.crumbs.data.RSSIDist

class RSSI_DAO {
    // A fake database table
    private val rssiList = mutableListOf<RSSIDist>()
    // MutableLiveData is from the Architecture Components Library
    // LiveData can be observed for changes
    private val rssis = MutableLiveData<List<RSSIDist>>()

    init {
        // Immediately connect the now empty quoteList
        // to the MutableLiveData which can be observed
        rssis.value = rssiList
    }

    fun add_rssi(rssi_Dist_val: RSSIDist) {
        rssiList.add(rssi_Dist_val)
        // After adding a rssi to the "database",
        // update the value of MutableLiveData
        // which will notify its active observers
        rssis.value = rssiList
    }

    // Casting MutableLiveData to LiveData because its value
    // shouldn't be changed from other classes
    fun getRSSIs() = rssis as LiveData<List<RSSIDist>>
}