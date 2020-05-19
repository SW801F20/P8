package dead.crumbs.data.DAO
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dead.crumbs.client.apis.DeadCrumbsApi
import io.swagger.client.models.Location

import java.lang.Exception
import kotlin.concurrent.thread

class Location_DAO (val client : DeadCrumbsApi) {

    //Adds a location to the database
    fun postLocation(location: Location) {
        val thread = thread(start = true){
            client.postLocation(body=location)
        }
        thread.join()
    }

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

    // Returns the newest location for a user
    fun getLocation(userName: String) : LiveData<Location> {
        var location: Location? = null
        val thread = thread (start = true){
            location = client.getLocation(userName)
        }
        thread.join()
        if(location == null){
            throw(java.lang.Exception("$userName has no recorded locations."))
        }else{
            return MutableLiveData(location)
        }

    }
}