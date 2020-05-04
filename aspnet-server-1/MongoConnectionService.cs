public class MongoConnectionService
{
    MongoConnectionService()
    {
        JObject json = JObject.Parse(new StreamReader(@"..\..\mongoAuthentication.json").ReadToEnd());
        var ip = json["ip"];
        var port = json["port"];
        var username = json["username"];
        var password = json["password"];
    }
}
