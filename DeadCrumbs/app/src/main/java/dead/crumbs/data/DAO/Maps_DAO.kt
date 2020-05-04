package dead.crumbs.data.DAO

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.swagger.client.apis.DeadCrumbsApi
import io.swagger.client.models.Location
import kotlin.concurrent.thread

class Maps_DAO (val client : DeadCrumbsApi)  {

    fun updateLocation(userName: String, orientation: Double, dist: Double, timeStamp: String) : LiveData<Location> {
        var location: Location? = null
        val thread = thread(start = true){
            location = client.updateLocation(userName, orientation, dist, timeStamp)
        }
        thread.join()
        if (location == null){
            //throw error here
            return MutableLiveData(location)
        } else {
            return MutableLiveData(location)
        }
    }
}