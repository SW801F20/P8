/**
 * DeadCrumbs API
 * This is the API for for the DeadCrumbs application.
 *
 * OpenAPI spec version: 1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
package io.swagger.client.apis

import io.swagger.client.models.Location
import io.swagger.client.models.User

import io.swagger.client.infrastructure.*

class DeadCrumbsApi(basePath: kotlin.String = "/") : ApiClient(basePath) {

    /**
     * returns the location associated with username
     * 
     * @param username The username of the user. 
     * @return Location
     */
    @Suppress("UNCHECKED_CAST")
    fun getLocation(username: kotlin.String): Location {
        
        val localVariableConfig = RequestConfig(
                RequestMethod.GET,
                "/Location/{username}".replace("{" + "username" + "}", "$username")
        )
        val response = request<Location>(
                localVariableConfig
        )

        return when (response.responseType) {
            ResponseType.Success -> (response as Success<*>).data as Location
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
    /**
     * return the user
     * 
     * @param username The username of the user. 
     * @return User
     */
    @Suppress("UNCHECKED_CAST")
    fun getUser(username: kotlin.String): User {
        
        val localVariableConfig = RequestConfig(
                RequestMethod.GET,
                "/User/{username}".replace("{" + "username" + "}", "$username")
        )
        val response = request<User>(
                localVariableConfig
        )

        return when (response.responseType) {
            ResponseType.Success -> (response as Success<*>).data as User
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
    /**
     * returns all user as a list
     * 
     * @return kotlin.Array<User>
     */
    @Suppress("UNCHECKED_CAST")
    fun getUsers(): kotlin.Array<User> {
        
        val localVariableConfig = RequestConfig(
                RequestMethod.GET,
                "/Users"
        )
        val response = request<kotlin.Array<User>>(
                localVariableConfig
        )

        return when (response.responseType) {
            ResponseType.Success -> (response as Success<*>).data as kotlin.Array<User>
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
    /**
     * updates locations based on bluetooth sync
     * 
     * @param username The username of the user. 
     * @param targetMac The mac address of another user. 
     * @param rssiDist distance estimated from RSSI 
     * @param timeStamp The time. 
     * @return kotlin.Array<Location>
     */
    @Suppress("UNCHECKED_CAST")
    fun postBluetoothSync(username: kotlin.String, targetMac: kotlin.String, rssiDist: kotlin.Double, timeStamp: kotlin.String): kotlin.Array<Location> {
        
        val localVariableConfig = RequestConfig(
                RequestMethod.POST,
                "/RSSI/{username}/{targetMac}/{rssiDist}/{timeStamp}".replace("{" + "username" + "}", "$username").replace("{" + "targetMac" + "}", "$targetMac").replace("{" + "rssiDist" + "}", "$rssiDist").replace("{" + "timeStamp" + "}", "$timeStamp")
        )
        val response = request<kotlin.Array<Location>>(
                localVariableConfig
        )

        return when (response.responseType) {
            ResponseType.Success -> (response as Success<*>).data as kotlin.Array<Location>
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
    /**
     * adds a new location
     * 
     * @param body a JSON object of a location 
     * @return void
     */
    fun postLocation(body: Location): Unit {
        val localVariableBody: kotlin.Any? = body
        
        val localVariableConfig = RequestConfig(
                RequestMethod.POST,
                "/Location"
        )
        val response = request<Any?>(
                localVariableConfig
        )

        return when (response.responseType) {
            ResponseType.Success -> Unit
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
    /**
     * adds a new user
     * 
     * @param body a JSON object of a user 
     * @return void
     */
    fun postUser(body: User): Unit {
        val localVariableBody: kotlin.Any? = body
        
        val localVariableConfig = RequestConfig(
                RequestMethod.POST,
                "/User"
        )
        val response = request<Any?>(
                localVariableConfig
        )

        return when (response.responseType) {
            ResponseType.Success -> Unit
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
    /**
     * updates location of a user
     * 
     * @param username The username of the user. 
     * @param orientation orientation of user 
     * @param dist step length 
     * @param timeStamp The time. 
     * @return Location
     */
    @Suppress("UNCHECKED_CAST")
    fun updateLocation(username: kotlin.String, orientation: kotlin.Double, dist: kotlin.Double, timeStamp: kotlin.String): Location {
        
        val localVariableConfig = RequestConfig(
                RequestMethod.POST,
                "/Location/{username}/{orientation}/{dist}/{timeStamp}".replace("{" + "username" + "}", "$username").replace("{" + "orientation" + "}", "$orientation").replace("{" + "dist" + "}", "$dist").replace("{" + "timeStamp" + "}", "$timeStamp")
        )
        val response = request<Location>(
                localVariableConfig
        )

        return when (response.responseType) {
            ResponseType.Success -> (response as Success<*>).data as Location
            ResponseType.Informational -> TODO()
            ResponseType.Redirection -> TODO()
            ResponseType.ClientError -> throw ClientException((response as ClientError<*>).body as? String ?: "Client error")
            ResponseType.ServerError -> throw ServerException((response as ServerError<*>).message ?: "Server error")
        }
    }
}
