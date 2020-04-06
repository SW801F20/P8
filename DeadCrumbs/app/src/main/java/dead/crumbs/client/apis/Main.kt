package io.swagger.client.apis
import io.swagger.client.infrastructure.ClientException
import io.swagger.client.models.Location

fun main(args: Array<String>) {

    //State of the database is that there are two locations
    //with an id of 1 and an id of 2.
    
    //Make sure to check that the url is correct.
    val client = DefaultApi("http://localhost:50352")

    println("Testing /Locations - GET")
    val allLocations = client.getLocations()

    for (location in allLocations){
        println("Id: " + location.id)
        println("DeviceId: " + location.deviceId)
        println("LocationValue: " + location.locationValue)
    }


    println("\nTesting /Location/{deviceId} - GET")
    val location = client.getLocation(1)

    println("Id: " + location.id)
    println("DeviceId: " + location.deviceId)
    println("LocationValue: " + location.locationValue)


    try {
        println("\nGetting an invalid location:")
        val error = client.getLocation(-1)
    }
    catch (e: ClientException){
        println(e.message)
    }


    println("\nTesting /Location/{deviceId} - DELETE")
    val numOfLocationsBeforeDel = client.getLocations().size
    println("Number of locations before deleting: $numOfLocationsBeforeDel")

    // response is atm just kotlin.Unit which translates to void in java.
    val responseDelete = client.deleteLocation(1)

    val numOfLocationsAfterDel = client.getLocations().size
    println("Number of locations after deleting: $numOfLocationsAfterDel")

    println("\nTesting /Location - POST")
    val numOfLocationsBeforePost = client.getLocations().size
    println("Number of locations before posting: $numOfLocationsBeforePost")

    val newLocation = Location(1, 1, "Some rssi value")
    val responsePost = client.postLocation(body=newLocation)

    val numOfLocationsAfterPost = client.getLocations().size
    println("Number of locations after posting: $numOfLocationsAfterPost")
}
