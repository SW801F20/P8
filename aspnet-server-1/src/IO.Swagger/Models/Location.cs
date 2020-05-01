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
using System.Linq;
using System.IO;
using System.Text;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel.DataAnnotations;
using System.Runtime.Serialization;
using Newtonsoft.Json;
using MongoDB.Bson.Serialization.Attributes;
using MongoDB.Bson;

namespace IO.Swagger.Models
{ 
    /// <summary>
    /// 
    /// </summary>
    [DataContract]
    public partial class Location : IEquatable<Location>
    {
        /// <summary>
        /// Gets or Sets UserRef
        /// </summary>
        [BsonId]
        public ObjectId id { get; set; }

        /// <summary>
        /// Gets or Sets UserRef
        /// </summary>
        [Required]
        [BsonElement("user_ref")]
        [DataMember(Name="user_ref")]
        public string UserRef { get; set; }

        /// <summary>
        /// Gets or Sets Yaw
        /// </summary>
        [Required]
        [BsonElement("yaw")]
        [DataMember(Name="yaw")]
        public float? Yaw { get; set; }

        /// <summary>
        /// Gets or Sets Position
        /// </summary>
        [Required]
        [BsonElement("position")]
        [DataMember(Name="position")]
        public Position Position { get; set; }

        /// <summary>
        /// Gets or Sets Timestamp
        /// </summary>
        [Required]
        [BsonElement("timestamp")]
        [DataMember(Name="timestamp")]
        public DateTime? Timestamp { get; set; }

        /// <summary>
        /// Returns the string presentation of the object
        /// </summary>
        /// <returns>String presentation of the object</returns>
        public override string ToString()
        {
            var sb = new StringBuilder();
            sb.Append("class Location {\n");
            sb.Append("  UserRef: ").Append(UserRef).Append("\n");
            sb.Append("  Yaw: ").Append(Yaw).Append("\n");
            sb.Append("  Position: ").Append(Position).Append("\n");
            sb.Append("  Timestamp: ").Append(Timestamp).Append("\n");
            sb.Append("}\n");
            return sb.ToString();
        }

        /// <summary>
        /// Returns the JSON string presentation of the object
        /// </summary>
        /// <returns>JSON string presentation of the object</returns>
        public string ToJson()
        {
            return JsonConvert.SerializeObject(this, Formatting.Indented);
        }

        /// <summary>
        /// Returns true if objects are equal
        /// </summary>
        /// <param name="obj">Object to be compared</param>
        /// <returns>Boolean</returns>
        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return obj.GetType() == GetType() && Equals((Location)obj);
        }

        /// <summary>
        /// Returns true if Location instances are equal
        /// </summary>
        /// <param name="other">Instance of Location to be compared</param>
        /// <returns>Boolean</returns>
        public bool Equals(Location other)
        {
            if (ReferenceEquals(null, other)) return false;
            if (ReferenceEquals(this, other)) return true;

            return 
                (
                    UserRef == other.UserRef ||
                    UserRef != null &&
                    UserRef.Equals(other.UserRef)
                ) && 
                (
                    Yaw == other.Yaw ||
                    Yaw != null &&
                    Yaw.Equals(other.Yaw)
                ) && 
                (
                    Position == other.Position ||
                    Position != null &&
                    Position.Equals(other.Position)
                ) && 
                (
                    Timestamp == other.Timestamp ||
                    Timestamp != null &&
                    Timestamp.Equals(other.Timestamp)
                );
        }

        /// <summary>
        /// Gets the hash code
        /// </summary>
        /// <returns>Hash code</returns>
        public override int GetHashCode()
        {
            unchecked // Overflow is fine, just wrap
            {
                var hashCode = 41;
                // Suitable nullity checks etc, of course :)
                    if (UserRef != null)
                    hashCode = hashCode * 59 + UserRef.GetHashCode();
                    if (Yaw != null)
                    hashCode = hashCode * 59 + Yaw.GetHashCode();
                    if (Position != null)
                    hashCode = hashCode * 59 + Position.GetHashCode();
                    if (Timestamp != null)
                    hashCode = hashCode * 59 + Timestamp.GetHashCode();
                return hashCode;
            }
        }

        #region Operators
        #pragma warning disable 1591

        public static bool operator ==(Location left, Location right)
        {
            return Equals(left, right);
        }

        public static bool operator !=(Location left, Location right)
        {
            return !Equals(left, right);
        }

        #pragma warning restore 1591
        #endregion Operators
    }
}
