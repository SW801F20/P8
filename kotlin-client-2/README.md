# io.swagger.client - Kotlin client library for Walking Skeleton API

## Requires

* Kotlin 1.1.2
* Gradle 3.3

## Build

First, create the gradle wrapper script:

```
gradle wrapper
```

Then, run:

```
./gradlew check assemble
```

This runs all tests and packages the library.

## Features/Implementation Notes

* Supports JSON inputs/outputs, File inputs, and Form inputs.
* Supports collection formats for query parameters: csv, tsv, ssv, pipes.
* Some Kotlin and Java types are fully qualified to avoid conflicts with types defined in Swagger definitions.
* Implementation of ApiClient is intended to reduce method counts, specifically to benefit Android targets.

<a name="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to *url1*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*DefaultApi* | [**deleteLocation**](docs/DefaultApi.md#deletelocation) | **DELETE** /Location/{deviceId} | deletes a location
*DefaultApi* | [**getLocation**](docs/DefaultApi.md#getlocation) | **GET** /Location/{deviceId} | returns the location associated with device ID
*DefaultApi* | [**getLocations**](docs/DefaultApi.md#getlocations) | **GET** /Locations | returns all locations as a list
*DefaultApi* | [**postLocation**](docs/DefaultApi.md#postlocation) | **POST** /Location | adds a new location

<a name="documentation-for-models"></a>
## Documentation for Models

 - [io.swagger.client.models.Location](docs/Location.md)

<a name="documentation-for-authorization"></a>
## Documentation for Authorization

All endpoints do not require authorization.
