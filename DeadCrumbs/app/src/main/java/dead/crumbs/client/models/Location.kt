/**
 * DeadCrumbs API
 * This is the API for for the DeadCrumbs application.
 *
 * OpenAPI spec version: 1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
package io.swagger.client.models

import io.swagger.client.models.Position

/**
 * 
 * @param userRef 
 * @param yaw 
 * @param position 
 * @param timestamp 
 */
data class Location (
    val userRef: kotlin.String,
    val yaw: kotlin.Double,
    val position: Position,
    val timestamp: kotlin.String

) {
}