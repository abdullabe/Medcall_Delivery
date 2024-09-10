package com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.ColorRes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.yoodobuzz.medcalldelivery.R
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.sqrt

class Extension {
}
fun GoogleMap.setCustomMapStyle(context: Context, styleResId: Int) {
    try {
        val styleOptions = MapStyleOptions.loadRawResourceStyle(context, styleResId)
        this.setMapStyle(styleOptions)
    } catch (e: Resources.NotFoundException) {
        Log.e("Custom Style", "Can't find style. Error: ", e)
    }
}
fun GoogleMap.zoomArea(list: List<LatLng>, padding : Int = 225) {
    if (list.isEmpty()) return
    val boundsBuilder = LatLngBounds.Builder()
    for (latLngPoint in list) boundsBuilder.include(latLngPoint)
    val latLngBounds = boundsBuilder.build()
    moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding))
}
fun GoogleMap.setStartingZoomArea(startLatLng: LatLng, endLatLng: LatLng) {
    val builder = LatLngBounds.Builder()
    builder.include(startLatLng)
    builder.include(endLatLng)
    val bounds = builder.build()
    val padding = 10
    this.setLatLngBoundsForCameraTarget(bounds)
    this.moveCamera(
        CameraUpdateFactory.newLatLngBounds(
            bounds,
            displayWidth(),
            displayHeight(),
            padding
        )
    )
    this.setMinZoomPreference(this.cameraPosition.zoom)
}

fun GoogleMap.changeCameraPosition(latLng: LatLng, zoom: Float = 15f) {
    moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
}

fun GoogleMap.drawPolyline(shape: String, @ColorRes colorId: Int, with: Float = 10f) {
    if (shape.isEmpty()) return
    shape.let {
        val polyline = PolylineOptions().addAll(PolyUtil.decode(shape)).width(with).geodesic(true)
            .color(colorId)
        addPolyline(polyline)
    }
}

fun GoogleMap.addMarkerExt(position: LatLng,context: Context) {
    // Custom delivery point icon
//    val deliveryIcon = BitmapDescriptorFactory.fromResource(R.drawable.pin)
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pin)
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 140, 140, false)
    val deliveryIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap) // Correct method to use
    addMarker(
        MarkerOptions()
            .position(position)
            .icon(deliveryIcon) // Use the custom delivery point icon here
    )
}


fun displayWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

fun displayHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels

}
fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371e3 // Radius of Earth in meters
    val φ1 = Math.toRadians(lat1)
    val φ2 = Math.toRadians(lat2)
    val Δφ = Math.toRadians(lat2 - lat1)
    val Δλ = Math.toRadians(lon2 - lon1)

    val a = sin(Δφ / 2) * sin(Δφ / 2) +
            cos(φ1) * cos(φ2) *
            sin(Δλ / 2) * sin(Δλ / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c // Distance in meters
}
