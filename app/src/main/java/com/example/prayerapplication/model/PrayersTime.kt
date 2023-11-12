package com.example.prayerapplication.model

import androidx.annotation.Keep

@Keep
data class PrayersTime(
    val id: Int,
    val name: String,
    val hours: Int,
    val minutes: Int
)