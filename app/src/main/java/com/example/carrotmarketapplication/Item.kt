package com.example.carrotmarketapplication

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Item(
    val position: Int,
    val icon: Int,
    val title: String,
    val description: String,
    val seller: String,
    val price: Int,
    val address: String,
    val likeNum: Int,
    val chatNum: Int,
    var heartStatus: Boolean
):Parcelable
