package dead.crumbs.ui

import dead.crumbs.data.LocationRepository
import androidx.lifecycle.ViewModel
import io.swagger.client.models.Location


class LocationViewModel(private val locationRepository: LocationRepository)
    : ViewModel() {

    fun getLocations() = locationRepository.getLocations()

    fun getLocation(deviceId : Int) = locationRepository.getLocation(deviceId)

    fun addLocation(location: Location) = locationRepository.addLocation(location)

    fun deleteLocation(deviceId: Int) = locationRepository.deleteLocation(deviceId)
}