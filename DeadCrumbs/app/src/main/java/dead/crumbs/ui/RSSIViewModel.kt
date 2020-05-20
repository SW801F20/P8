package dead.crumbs.ui

import androidx.lifecycle.ViewModel
import dead.crumbs.data.RSSIRepository
import dead.crumbs.data.UserRepository
import dead.crumbs.utilities.UtilFunctions
import io.swagger.client.models.Location

class RSSIViewModel(private val rssiRepository: RSSIRepository, private val userRepository: UserRepository)
    : ViewModel() {

    //Returns all the bluetooth mac addresse stored in the db
    fun getMacs():MutableList<String>{
        val users = userRepository.getUsers()
        val mac_adresses = mutableListOf<String>();
        for(user in users.value!!){
            mac_adresses.add(user.mac_address)
        }
        return mac_adresses
    }

    //Bluetooth synchronization between my_username and target device with target bluetooth mac address
    fun bluetoothSync(my_username : String, target_mac : String, distance : Double): Array<Location> {
        val dateTimeString = UtilFunctions.getDatetime()
        return rssiRepository.bluetoothSync(my_username, target_mac, distance, dateTimeString)
    }
}