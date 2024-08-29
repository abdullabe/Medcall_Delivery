package com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class PlaceItem(
    private val _position: LatLng,
    private val _title: String,
    private val _snippet: String
) : ClusterItem {

    override fun getPosition(): LatLng {
        return _position
    }

    override fun getTitle(): String {
        return _title
    }

    override fun getSnippet(): String {
        return _snippet
    }
}