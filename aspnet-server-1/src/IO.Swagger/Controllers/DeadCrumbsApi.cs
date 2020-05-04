/*
 * DeadCrumbs API
 *
 * This is the API for for the DeadCrumbs application.
 *
 * OpenAPI spec version: 1
 * 
 * Generated by: https://github.com/swagger-api/swagger-codegen.git
 */
using System;
using System.Collections.Generic;
using Microsoft.AspNetCore.Mvc;
using Swashbuckle.AspNetCore.Annotations;
using Swashbuckle.AspNetCore.SwaggerGen;
using Newtonsoft.Json;
using System.ComponentModel.DataAnnotations;
using IO.Swagger.Attributes;

using Microsoft.AspNetCore.Authorization;
using IO.Swagger.Models;
using MongoDB.Driver;
using MongoDB.Bson;
using System.IO;
using Newtonsoft.Json.Linq;
using IO.Swagger.DAL;

namespace IO.Swagger.Controllers
{
    /// <summary>
    /// 
    /// </summary>
    [ApiController]
    public class DeadCrumbsController : ControllerBase
    {
        public DeadCrumbsController(MongoConnectionService mongoConnectionService,
                                    LocationService ls, UserService us)
        {
            db = mongoConnectionService.db;
            this.ls = ls;
            this.us = us;
        }

        private LocationService ls;
        private UserService us;
        private IMongoDatabase db;

        /// <summary>
        /// returns the location associated with username
        /// </summary>
        /// <param name="username">The username of the user.</param>
        /// <response code="200"> Successful request! Returns the newest location represented as JSON</response>
        /// <response code="404"> No locations found!</response>
        [HttpGet]
        [Route("/Location/{username}")]
        [ValidateModelState]
        [SwaggerOperation("GetLocation")]
        [SwaggerResponse(statusCode: 200, type: typeof(Location), description: "a JSON object of a location")]
        public virtual IActionResult GetLocation([FromRoute][Required]string username)
        {
            var newestLoc = ls.GetNewestLocation(db, username);
            if (newestLoc == null)
            {
                return StatusCode(404, $"No locations found!");
            }
           
            var result = new ObjectResult(newestLoc);
            return result;
        }

        /// <summary>
        /// return the user
        /// </summary>
        /// <param name="username">The username of the user.</param>
        /// <response code="200"> Successful request! Returns a user represented as JSON</response>
        /// <response code="404"> No user found called "username"</response>
        [HttpGet]
        [Route("/User/{username}")]
        [ValidateModelState]
        [SwaggerOperation("GetUser")]
        [SwaggerResponse(statusCode: 200, type: typeof(User), description: "a user represented as json")]
        public virtual IActionResult GetUser([FromRoute][Required]string username)
        {
            User user = us.GetUserByName(db, username);

            if (user == null)
            {
                return StatusCode(404, $"No user found called \"{username}\"!");
            }
            var result = new ObjectResult(user);
            return result;
        }

        /// <summary>
        /// returns all user as a list
        /// </summary>
        /// <response code="200">Successful request! Returns a JSON array of users</response>
        /// <response code="404"> No users found</response>
        [HttpGet]
        [Route("/Users")]
        [ValidateModelState]
        [SwaggerOperation("GetUsers")]
        [SwaggerResponse(statusCode: 200, type: typeof(List<User>), description: "a JSON array of users")]
        public virtual IActionResult GetUsers()
        {
            List<User> users = us.GetUsers(db);
            if (users.Count == 0)
            {
                return StatusCode(404, $"No users found!");
            }
            var result = new ObjectResult(users);
            return result;
        }

        /// <summary>
        /// updates location of a user
        /// </summary>
        /// <param name="username"> unique string username>
        /// <param name="orientation"> yaw orientation>
        /// <param name="dist"> distance walked>
        /// <param name="timeStamp"> timestamp of location update>
        /// <response code="201">Successful request! Created new location</response>
        /// <response code="400">Bad request! Location not added</response>
        [HttpPost]
        [Route("/Location/{username}/{orientation}/{dist}/{timeStamp}")]
        [ValidateModelState]
        [SwaggerOperation("PostLocation")]
        [SwaggerResponse(statusCode: 201)]
        public virtual IActionResult UpdateLocation([FromRoute][Required]string username,
            [FromRoute][Required]double orientation,
            [FromRoute][Required]double dist,
            [FromRoute][Required]DateTime timeStamp)
        {
            try
            {
                User user = us.GetUserByName(db, username);

                if (user == null)
                {
                    return StatusCode(400, "Unknown user!");
                }

                Func<double, double> toRadians = (degrees) => { return degrees * Math.PI / 180; };
                Func<double, double> toDegrees = (radians) => { return radians * (180 / Math.PI); };

                var heading = toRadians(orientation);
                var R = 6378.1; //Radius of the Earth
                var kmDist = dist / 1000; //Convert distance from meters to km

                var curLoc = ls.GetNewestLocation(db, username);

                if (curLoc == null)
                {
                    return StatusCode(400, "User has no locations!");
                }
                var lat1 = toRadians(curLoc.Position.Coordinates[0]); //Current lat point converted to radians
                var lng1 = toRadians(curLoc.Position.Coordinates[1]); //Current lat point converted to radians

                //Formula from
                //https://www.movable-type.co.uk/scripts/latlong.html "Destination point given distance and bearing from start point"

                var lat2 = Math.Asin(Math.Sin(lat1) * Math.Cos(kmDist / R) +
                Math.Cos(lat1) * Math.Sin(kmDist / R) * Math.Cos(heading));

                var lng2 = lng1 + Math.Atan2(
                Math.Sin(heading) * Math.Sin(kmDist / R) * Math.Cos(lat1),
                Math.Cos(kmDist / R) - Math.Sin(lat1) * Math.Sin(lat2));

                lat2 = toDegrees(lat2);
                lng2 = toDegrees(lng2);

                var pos = new Position()
                {
                    Type = "Point",
                    Coordinates = new List<double>() { lat2, lng2 }
                };
                var newLoc = new Location(username, orientation, pos, timeStamp);
                ls.InsertLocation(db, newLoc);


                return new ObjectResult(newLoc) { StatusCode = 201 };

            }
            catch (Exception e)
            {
                return StatusCode(400, e.Message);
            }            
        }

        /// <summary>
        /// adds a new location
        /// </summary>
        /// <param name="location">a JSON object of a location</param>
        /// <response code="201">Successful request! Created new location</response>
        /// <response code="400">Bad request! Location not added</response>
        [HttpPost]
        [Route("/Location")]
        [ValidateModelState]
        [SwaggerOperation("PostLocation")]
        [SwaggerResponse(statusCode: 201)]
        public virtual IActionResult PostLocation([FromBody]Location location)
        {
            try
            {
                User user = us.GetUserByName(db, location.UserRef);

                if (user == null)
                {
                    return StatusCode(400, "Location refers to an unknown user!");
                }
                if (location.Position.Coordinates.Count == 2)
                {
                    ls.InsertLocation(db, location);
                }
                else
                {
                    return StatusCode(400, "Coordinates most contain two elements");
                }
               
            }
            catch (Exception e)
            {
                return StatusCode(400, e.Message);
            }

            return StatusCode(201);
        }


        /// <summary>
        /// adds a new user
        /// </summary>
        /// <param name="user">a JSON object of a location</param>
        /// <response code="201">Successful request! Created new user</response>
        /// <response code="400">Bad request! User not added</response>
        [HttpPost]
        [Route("/User")]
        [ValidateModelState]
        [SwaggerOperation("PostUser")]
        [SwaggerResponse(statusCode: 201)]
        public virtual IActionResult PostUser([FromBody]User user)
        {
            try
            {
                User user1 = us.GetUserByName(db, user.Username);
                User user2 = us.GetUserByMac(db, user.MacAddress);
                if (user1 != null)
                {
                    return StatusCode(400, "A user already exists with that username");
                }
                else if (user2 != null)
                {
                    return StatusCode(400, "A user already exists with that mac address");
                }
                else
                {
                    us.InsertUser(db, user);
                }
               
            }
            catch (Exception e)
            {
                return StatusCode(400, e.Message);
            }

            return StatusCode(201);
        }


        /// <summary>
        /// Updates locations of two users
        /// </summary>
        /// <param name="username"> unique string username>
        /// <param name="targetMac"> target bluetooth mac address>
        /// <param name="rssiDist"> proximated rssi distance>
        /// <param name="timeStamp"> timestamp of bluetooth sync>
        /// <response code="201">Successful request! Created new user</response>
        /// <response code="400">Sync failed. User or location does not exist</response>
        [HttpPost]
        [Route("/RSSI/{username}/{targetMac}/{rssiDist}/{timeStamp}")]
        [ValidateModelState]
        [SwaggerOperation("PostBluetoothSync")]
        [SwaggerResponse(statusCode: 201)]
        public virtual IActionResult PostBluetoothSync([FromRoute][Required]string username, 
            [FromRoute][Required]string targetMac, 
            [FromRoute][Required]double rssiDist,
            [FromRoute][Required]DateTime timeStamp)
        {
            const float rssiThreshold = 2;

            User user1 = us.GetUserByName(db, username);
            User user2 = us.GetUserByMac(db, targetMac);

            if(user1 == null || user2 == null) {
                return StatusCode(404);
            }

            var loc1 = ls.GetNewestLocation(db, user1.Username);
            var loc2 = ls.GetNewestLocation(db, user2.Username);

            if (loc1 == null || loc2 == null)
            {
                return StatusCode(404);
            }


            List<Location> res = new List<Location>() { loc1, loc2 }; //Default is the no change to locations res

            if (rssiDist < rssiThreshold)
            {           
                var earthRadiusKm = 6371;
                Func<double, double> toRadians = (degrees) => { return degrees * Math.PI / 180; };
                var dLat = toRadians(loc1.Position.Coordinates[0] - loc2.Position.Coordinates[0]);
                var dLon = toRadians(loc1.Position.Coordinates[1] - loc2.Position.Coordinates[1]);
                var lat1 = toRadians(loc1.Position.Coordinates[0]);
                var lat2 = toRadians(loc2.Position.Coordinates[0]);
                var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                        Math.Sin(dLon / 2) * Math.Sin(dLon / 2) * Math.Cos(lat1) * Math.Cos(lat2);
                var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
                var distanceKM = earthRadiusKm * c;
                var distanceM = distanceKM * 1000;

                if (distanceM > 2) //threshold for sync
                {
                    const double multiplier = 0.5;
                    double latDiff = Math.Abs(loc1.Position.Coordinates[0] - loc2.Position.Coordinates[0]);
                    if (loc1.Position.Coordinates[0] < loc2.Position.Coordinates[0])
                    {
                        loc1.Position.Coordinates[0] += latDiff * multiplier;
                        loc2.Position.Coordinates[0] -= latDiff * multiplier;

                    }
                    else  if (loc1.Position.Coordinates[0] > loc2.Position.Coordinates[0])
                    {
                        loc1.Position.Coordinates[0] -= latDiff * multiplier;
                        loc2.Position.Coordinates[0] += latDiff * multiplier;
                    }

                    double longDiff = Math.Abs(loc1.Position.Coordinates[1] - loc2.Position.Coordinates[1]);

                    if (loc1.Position.Coordinates[1] < loc2.Position.Coordinates[1])
                    {
                        loc1.Position.Coordinates[1] += longDiff * multiplier;
                        loc2.Position.Coordinates[1] -= longDiff * multiplier;
                    }
                    else if (loc1.Position.Coordinates[1] > loc2.Position.Coordinates[1])
                    {
                        loc1.Position.Coordinates[1] -= longDiff * multiplier;
                        loc2.Position.Coordinates[1] += longDiff * multiplier;
                    }
                                        
                    Location newLoc1 = new Location(loc1.UserRef, loc1.Yaw, loc1.Position, timeStamp);
                    Location newLoc2 = new Location(loc2.UserRef, loc2.Yaw, loc2.Position, timeStamp);
                    ls.InsertLocation(db, newLoc1);
                    ls.InsertLocation(db, newLoc2);
                    res = new List<Location>() { newLoc1, newLoc2 }; //New updated locations based on sync
                }
            }

            return new ObjectResult(res) { StatusCode = 201 };
        }
    }
}
