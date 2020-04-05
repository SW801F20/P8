package dead.crumbs.data.DAO

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.swagger.client.apis.DefaultApi
import io.swagger.client.models.Location
import kotlin.concurrent.thread

class Location_DAO {
    private val locationList = mutableListOf<Location>()
    // MutableLiveData is from the Architecture Components Library
    // LiveData can be observed for changes
    private val locations = MutableLiveData<List<Location>>()

    // Since access is done from an emulator, we cannot use localhost
    // instead we have to use 10.0.2.2, also, it assumes API is running on port 8080
    val client = DefaultApi("http://10.0.2.2:8080")

    init {
        //start by retrieving everything from the database
        var all_locations = arrayOf<Location>()

        //operations over the internet should happen on a thread
        val thread = thread(start = true){
            all_locations = client.getLocations()
        }
        //Wait for the action to complete
        thread.join()
        locationList.addAll(all_locations)
        locations.value = locationList
    }

    // Deletes all locations that matches the deviceId
    fun deleteLocation(deviceId : Int){
        val thread = thread(start = true){
            client.deleteLocation(deviceId)
        }
        thread.join()
        val locs = locations.value?.filter { it.deviceId != deviceId }
        locations.value = locs
    }

    // Add a locations to the local representations
    // and updates database
    fun addLocations(location: Location) {
        locationList.add(location)
        val thread = thread(start = true){
            client.postLocation(body=location)

        }
        thread.join()
        locations.value = locationList
    }

    // Returns all locations
    fun getLocations() : LiveData<List<Location>>{
        updateLocalData()
        return locations
    }


    // Returns the first locations that matches the deviceId
    fun getLocation(deviceId: Int) : LiveData<Location>{
        updateLocalData()
        val locationLst = locations.value
        if (locationLst != null) {
            for (location in locationLst){
                if (location.deviceId == deviceId){
                    return MutableLiveData(location)
                }
            }
        }
        //Maybe not the correct way to do things.
        return MutableLiveData(Location(0,0, "Error"))
    }

    // Used to re-read the data from the database.
    private fun updateLocalData(){
        var alllocations = arrayOf<Location>()
        val thread = thread(start = true){
            alllocations = client.getLocations()
        }
        thread.join()
        locations.value = alllocations.toList()
    }
}