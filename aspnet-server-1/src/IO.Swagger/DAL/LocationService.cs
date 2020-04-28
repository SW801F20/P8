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
            locations.Sort((loc1, loc2) => loc2.Timestamp.Value.CompareTo(loc1.Timestamp.Value));
            return locations[0]; //newest location
        }

    }
}
