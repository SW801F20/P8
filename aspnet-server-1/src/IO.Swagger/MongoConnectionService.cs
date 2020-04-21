using MongoDB.Driver;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;

namespace IO.Swagger
{
    public class MongoConnectionService
    {
        
        public MongoConnectionService()
        {
            try
            {
                JObject json = JObject.Parse(new StreamReader(@"..\..\mongoConnection.json").ReadToEnd());

                var ip = json["ip"];
                var port = json["port"];
                var username = json["username"];
                var password = json["password"];
                var connectionString = $"mongodb://{username}:{password}@{ip}:{port}";
                var dbClient = new MongoClient(connectionString);
                db = dbClient.GetDatabase("DeadCrumbs");
            }
            catch (Exception)
            {
                throw new Exception("Error in establishing mongo connection based on P8\\aspnet-server1\\mongoConnection.json");
            }
        }
        public IMongoDatabase db { get; set; }

    }
}
