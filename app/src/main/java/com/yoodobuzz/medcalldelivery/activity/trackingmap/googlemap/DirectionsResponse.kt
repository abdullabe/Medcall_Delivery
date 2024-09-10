package com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap

data class DirectionsResponse(

val routes: List<Route>
)

data class Route(
    val legs: List<Leg>
)

data class Leg(
    val duration: Duration
)

data class Duration(
    val text: String, // e.g., "10 mins"
    val value: Int    // Duration in seconds
)

