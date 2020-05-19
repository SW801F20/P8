using IO.Swagger.Models;
using MongoDB.Driver;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace IO.Swagger.DAL
{
    public class LocationService
    {
        public Location GetNewestLocation(IMongoDatabase db, string username)
        {
            var locationCollection = db.GetCollection<Location>("location");
            List<Location> locations = locationCollection.Find((u) => u.UserRef == username).ToList();
            if (locations.Count == 0)
            {
                return null;
            }

            //We want the newest - determined by timestamp
            locations.Sort((loc1, loc2) => loc2.Timestamp.CompareTo(loc1.Timestamp));
            return locations[0]; //newest location
        }

        public void InsertLocation(IMongoDatabase db, Location location )
        {
            var locationCollection = db.GetCollection<Location>("location");
            locationCollection.InsertOne(location);
        }

    }
}
