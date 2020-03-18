# DefaultApi

All URIs are relative to *url1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteLocation**](DefaultApi.md#deleteLocation) | **DELETE** /Location/{deviceId} | deletes a location
[**getLocation**](DefaultApi.md#getLocation) | **GET** /Location/{deviceId} | returns the location associated with device ID
[**getLocations**](DefaultApi.md#getLocations) | **GET** /Locations | returns all locations as a list
[**postLocation**](DefaultApi.md#postLocation) | **POST** /Location | adds a new location

<a name="deleteLocation"></a>
# **deleteLocation**
> deleteLocation(deviceId)

deletes a location

### Example
```kotlin
// Import classes:
//import io.swagger.client.infrastructure.*
//import io.swagger.client.models.*;

val apiInstance = DefaultApi()
val deviceId : kotlin.Int = 56 // kotlin.Int | The ID of the device.
try {
    apiInstance.deleteLocation(deviceId)
} catch (e: ClientException) {
    println("4xx response calling DefaultApi#deleteLocation")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DefaultApi#deleteLocation")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **deviceId** | **kotlin.Int**| The ID of the device. | [enum: ]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getLocation"></a>
# **getLocation**
> Location getLocation(deviceId)

returns the location associated with device ID

### Example
```kotlin
// Import classes:
//import io.swagger.client.infrastructure.*
//import io.swagger.client.models.*;

val apiInstance = DefaultApi()
val deviceId : kotlin.Int = 56 // kotlin.Int | The ID of the device.
try {
    val result : Location = apiInstance.getLocation(deviceId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DefaultApi#getLocation")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DefaultApi#getLocation")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **deviceId** | **kotlin.Int**| The ID of the device. | [enum: ]

### Return type

[**Location**](Location.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="getLocations"></a>
# **getLocations**
> kotlin.Array&lt;Location&gt; getLocations()

returns all locations as a list

### Example
```kotlin
// Import classes:
//import io.swagger.client.infrastructure.*
//import io.swagger.client.models.*;

val apiInstance = DefaultApi()
try {
    val result : kotlin.Array<Location> = apiInstance.getLocations()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DefaultApi#getLocations")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DefaultApi#getLocations")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.Array&lt;Location&gt;**](Location.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="postLocation"></a>
# **postLocation**
> postLocation(body)

adds a new location

### Example
```kotlin
// Import classes:
//import io.swagger.client.infrastructure.*
//import io.swagger.client.models.*;

val apiInstance = DefaultApi()
val body : Location =  // Location | a JSON object of a location
try {
    apiInstance.postLocation(body)
} catch (e: ClientException) {
    println("4xx response calling DefaultApi#postLocation")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DefaultApi#postLocation")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Location**](Location.md)| a JSON object of a location |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

