# P8

This GitHub repository contains two applications: an Android application written in Kotlin and an ASP.NET Core REST API. The Prototype folder contains two XD files for our GUI prototype, latest.xd being the newest.

## How To Run

### Android Application
To run the Android application do the following:

1. Open the folder "P8\DeadCrumbs" in Android Studio
2. Insert Google Maps API key in P8/DeadCrumbs/app/src/release/res/values/google_maps_api.xml 
3. Start the application using Android Studio


### ASP.NET Core API
To run the API locally do the following:

1. Insert a MongoDB configuration file named "mongoConnection.json" at P8/aspnet-server-1/ on JSon format that contains the following fields "ip", "port", "username", "password" in order to connect to a MongoDB.
2. Follow the README provided at P8/aspnet-server-1/
