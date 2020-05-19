using IO.Swagger.Models;
using MongoDB.Driver;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace IO.Swagger.DAL
{
    public class UserService
    {
        public User GetUserByName(IMongoDatabase db, string username)
        {
            var userCollection = db.GetCollection<User>("user");
            User user = userCollection.Find((u) => u.Username == username).FirstOrDefault();
            return user;
        }


        public User GetUserByMac(IMongoDatabase db, string mac)
        {
            var userCollection = db.GetCollection<User>("user");
            User user = userCollection.Find((u) => u.MacAddress == mac).FirstOrDefault();
            return user;
        }

        public List<User> GetUsers(IMongoDatabase db)
        {
            var userCollection = db.GetCollection<User>("user");
            List<User> users = userCollection.Find(_ => true).ToList();
            return users;
        }

        public void InsertUser(IMongoDatabase db, User user)
        {
            var userCollection = db.GetCollection<User>("user");
            userCollection.InsertOne(user);
        }
    }
}
