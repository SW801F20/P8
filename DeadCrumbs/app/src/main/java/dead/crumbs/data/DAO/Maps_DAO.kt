package dead.crumbs.data.DAO

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.swagger.client.apis.DeadCrumbsApi
import io.swagger.client.models.Location
import kotlin.concurrent.thread

class Maps_DAO (val client : DeadCrumbsApi)  {

    //Updates an existing location in the database for a specific user
    //based on an orientation and distance that represent a step.
    //And returns the updated location to the caller.
    fun updateLocation(userName: String, orientation: Double,
                       dist: Double, timeStamp: String) : LiveData<Location> {
        var location: Location? = null
        val thread = thread(start = true){
            location = client.updateLocation(userName, orientation, dist, timeStamp)
        }
        thread.join()
        if (location == null){
            throw Exception("Call to updateLocation failed")
        } else {
            return MutableLiveData(location)
        }
    }
}