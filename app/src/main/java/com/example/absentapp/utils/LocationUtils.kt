package com.example.absentapp.utils

fun LocationUtils(
    userLat: Double,
    userLng: Double,
    targetLat: Double,
    targetLng: Double
): Float {
    val result = FloatArray(1)
    android.location.Location.distanceBetween(
        userLat, userLng,
        targetLat, targetLng,
        result
    )
    return result[0]
}