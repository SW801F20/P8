package dead.crumbs.ui

import androidx.lifecycle.ViewModel
import dead.crumbs.data.RSSIRepository
import dead.crumbs.data.UserRepository
import io.swagger.client.models.Location
import java.util.*

class RSSIViewModel(private val rssiRepository: RSSIRepository, private val userRepository: UserRepository)
    : ViewModel() {

    fun getMacs():MutableList<String>{
        var users = userRepository.getUsers()
        var mac_adresses = mutableListOf<String>();
        for(user in users.value!!){
            mac_adresses.add(user.mac_address)
        }
        return mac_adresses
    }

    //See description in RSSI_DAO for a description of functionality.
    fun bluetoothSync(my_username : String, target_mac : String, distance : Double): Array<Location> {
        val currYear = Calendar.getInstance().get(Calendar.YEAR).toString().padStart(4,'0')
        val currMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString().padStart(2,'0')
        val currDate = Calendar.getInstance().get(Calendar.DATE).toString().padStart(2,'0')
        val currHour = Calendar.getInstance().get(Calendar.HOUR).toString().padStart(2,'0')
        val currMinute = Calendar.getInstance().get(Calendar.MINUTE).toString().padStart(2,'0')
        val currSecond = Calendar.getInstance().get(Calendar.SECOND).toString().padStart(2,'0')
        val dateTimeString = currYear + "-" + currMonth + "-" + currDate+ "T" + currHour + ":" + currMinute + ":" + currSecond
        return rssiRepository.bluetoothSync(my_username, target_mac, distance, dateTimeString)
    }


}