package dead.crumbs.data.DAO

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.swagger.client.apis.DeadCrumbsApi
import io.swagger.client.models.Location
import io.swagger.client.models.User
import kotlin.concurrent.thread
//This class should be the one that calls the api
class Location_DAO (val client : DeadCrumbsApi) {

    // Adds a location to the local representation
    // and updates database
    fun postLocation(location: Location) {
        val thread = thread(start = true){
            client.postLocation(body=location)
        }
        thread.join()
    }

    // Returns the first locations that matches the deviceId
    fun getLocation(userName: String) : LiveData<Location>{
        var location: Location? = null
        val thread = thread (start = true){
            location = client.getLocation(userName)
        }
        thread.join()
        if(location == null){
            //throw error here
            return MutableLiveData(location)
        }else{
            return MutableLiveData(location)
        }

    }
}