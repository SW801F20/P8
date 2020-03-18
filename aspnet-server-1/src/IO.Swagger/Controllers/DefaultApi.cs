/*
 * Walking Skeleton API
 *
 * This is the API for the Walking Skeleton.
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
        /// <summary>
        /// deletes a location
        /// </summary>
        /// <param name="deviceId">The ID of the device.</param>
        /// <response code="200">location deleted successfully</response>
        /// <response code="400">deletion of location went wrong</response>
        /// <response code="404">no location associated with devive ID</response>
        [HttpDelete]
        [Route("/Location/{deviceId}")]
        [ValidateModelState]
        [SwaggerOperation("DeleteLocation")]
        public virtual IActionResult DeleteLocation([FromRoute][Required]int? deviceId)
        {
            // TODO: Initiate client elsewhere
            MongoClient dbClient = new MongoClient("mongodb://admin:mongo801@130.225.57.95:27017");
            var db = dbClient.GetDatabase("walkingskeleton");
            var locations = db.GetCollection<BsonDocument>("locations");
            var deleteFilter = Builders<BsonDocument>.Filter.Eq("deviceId", deviceId);
            var deleteResult = locations.DeleteMany(deleteFilter);

            if (deleteResult.DeletedCount == 0) return this.NotFound(deleteResult);
            else if (!deleteResult.IsAcknowledged) return StatusCode(400, deleteResult); //TODO: Find out what it means when IsAcknowledged is false, and don't use StatusCode
            else return this.Ok(deleteResult);
        }


        /// <summary>
        /// returns the location associated with device ID
        /// </summary>
        /// <param name="deviceId">The ID of the device.</param>
        /// <response code="200">a json object of a location</response>
        /// <response code="400">retrival of location went wrong</response> //TODO: Maybe remove this.
        /// <response code="404">no device with that id</response>
        [HttpGet]
        [Route("/Location/{deviceId}")]
        [ValidateModelState]
        [SwaggerOperation("GetLocation")]
        [SwaggerResponse(statusCode: 200, type: typeof(Location), description: "a json object of a location")]
        public virtual IActionResult GetLocation([FromRoute][Required]int? deviceId)
        {
            MongoClient dbClient = new MongoClient("mongodb://admin:mongo801@130.225.57.95:27017");
            var database = dbClient.GetDatabase("walkingskeleton");
            var locationCollection = database.GetCollection<BsonDocument>("locations");
            var filter = Builders<BsonDocument>.Filter.Eq("id", deviceId);
            BsonDocument result = locationCollection.Find(filter).FirstOrDefault();

            if (result != null)
            {            
                Location location = new Location(){
                    Id = result["id"].AsInt32,
                    DeviceId = result["deviceId"].AsInt32,
                    LocationValue = result["locationValue"].AsString
                };  
                
                return new ObjectResult(location);  
            }
            else
            {
                //TODO: use build in methods.
                return StatusCode(404, "No location with that device ID");
            }        
        }

        /// <summary>
        /// returns all locations as a list
        /// </summary>
        /// <response code="200">a JSON array of locations</response>
        /// <response code="404">no locations are in the database</response>
        [HttpGet]
        [Route("/Locations")]
        [ValidateModelState]
        [SwaggerOperation("GetLocations")]
        [SwaggerResponse(statusCode: 200, type: typeof(List<Location>), description: "a JSON array of locations")]
        public virtual IActionResult GetLocations()
        {
            MongoClient dbClient = new MongoClient("mongodb://admin:mongo801@130.225.57.95:27017");
            var database = dbClient.GetDatabase("walkingskeleton");
            var locationCollection = database.GetCollection<BsonDocument>("locations");
            // filter that matches everything.
            var filter = Builders<BsonDocument>.Filter.Empty;
            List<BsonDocument> results = locationCollection.Find(filter).ToList();
            
            if (results.Count > 0)
            {                
                List<Location> lstLocations = new List<Location>();

                foreach(var item in results)
                {                    
                    Location location = new Location(){
                        // Have to convert from BsonValue.
                        Id = item["id"].AsInt32,
                        DeviceId = item["deviceId"].AsInt32,
                        LocationValue = item["locationValue"].AsString
                    };

                    lstLocations.Add(location);

                }
                return new ObjectResult(lstLocations);
            }
            else
            {
                return StatusCode(404, "There are none locations in the database");
            }           
        }

        /// <summary>
        /// adds a new location
        /// </summary>
        /// <param name="body">a json object of a location</param>
        /// <response code="201">location added successfully</response>
        /// <response code="400">location added unsuccessfully</response>
        [HttpPost]
        [Route("/Location")]
        [ValidateModelState]
        [SwaggerOperation("PostLocation")]
        public virtual IActionResult PostLocation([FromBody]Location body)
        {
            // TODO: Initiate client elsewhere
            MongoClient dbClient = new MongoClient("mongodb://admin:mongo801@130.225.57.95:27017");
            var db = dbClient.GetDatabase("walkingskeleton");
            var locations = db.GetCollection<BsonDocument>("locations");
            var locationDocument = new BsonDocument{
                {"id", body.Id},
                {"deviceId", body.DeviceId},
                {"locationValue", body.LocationValue},
			};

            // TODO: Check if it was successful.
            locations.InsertOne(locationDocument);

            // It seems like incorrect input is handled automatically and an error (400) is returned
            return this.Ok("Done");
        }
    }
}
