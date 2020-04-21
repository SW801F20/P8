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

namespace IO.Swagger.Controllers
{ 
    /// <summary>
    /// 
    /// </summary>
    [ApiController]
    public class DefaultApiController : ControllerBase
    {
        public DefaultApiController()
        {
            dbClient = new MongoClient("mongodb://admin:mongo801@130.225.57.95:27017");
            db = dbClient.GetDatabase("DeadCrumbs");
        }
        private MongoClient dbClient;
        private IMongoDatabase db;

        /// <summary>
        /// returns the location associated with username
        /// </summary>
        /// <param name="username">The username of the user.</param>
        /// <response code="200">a JSON object of a location</response>
        /// <response code="400">retrival of location went wrong</response>
        /// <response code="404">no device with that id</response>
        [HttpGet]
        [Route("/Location/{username}")]
        [ValidateModelState]
        [SwaggerOperation("GetLocation")]
        [SwaggerResponse(statusCode: 200, type: typeof(Location), description: "a JSON object of a location")]
        public virtual IActionResult GetLocation([FromRoute][Required]string username)
        {
            var locationCollection = db.GetCollection<Location>("location");
            List<Location> locations = locationCollection.Find((u) => u.UserRef == username).ToList();
            if (locations.Count == 0)
            {
                return StatusCode(404, $"No locations found!");
            }
            locations.Sort((l1, l2) => l2.Timestamp.Value.CompareTo(l1.Timestamp.Value));
            Location newestLoc = locations[0];

            var result = new ObjectResult(newestLoc);
            return result;
        }

        /// <summary>
        /// return the user
        /// </summary>
        /// <param name="username">The username of the user.</param>
        /// <response code="200">a user represented as json</response>
        [HttpGet]
        [Route("/User/{username}")]
        [ValidateModelState]
        [SwaggerOperation("GetUser")]
        [SwaggerResponse(statusCode: 200, type: typeof(User), description: "a user represented as json")]
        public virtual IActionResult GetUser([FromRoute][Required]string username)
        {
            var userCollection = db.GetCollection<User>("user");
            User user;

            user = userCollection.Find((u) => u.Username == username).FirstOrDefault();
            
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
        /// <response code="200">a JSON array of users</response>
        [HttpGet]
        [Route("/Users")]
        [ValidateModelState]
        [SwaggerOperation("GetUsers")]
        [SwaggerResponse(statusCode: 200, type: typeof(List<User>), description: "a JSON array of users")]
        public virtual IActionResult GetUsers()
        {
            var userCollection = db.GetCollection<User>("user");
            List<User> users = userCollection.Find(_ => true).ToList();
            if (users.Count==0)
            {
                return StatusCode(404, $"No users found!");
            }
            var result = new ObjectResult(users);
            return result;
        }

        /// <summary>
        /// adds a new location
        /// </summary>
        /// <param name="location">a JSON object of a location</param>
        /// <response code="201">location added successfully</response>
        /// <response code="400">location added unsuccessfully</response>
        [HttpPost]
        [Route("/Location")]
        [ValidateModelState]
        [SwaggerOperation("PostLocation")]
        public virtual IActionResult PostLocation([FromBody]Location location)
        {
            try
            {
                var locationCollection = db.GetCollection<Location>("location");
                locationCollection.InsertOne(location);
            }
            catch (Exception e)
            {

                return StatusCode(400, e.Message);
            }
            
           
            return StatusCode(201);
        }
    }
}