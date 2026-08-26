package com.example.ui

import androidx.annotation.DrawableRes
import com.example.R
import com.example.astronomy.Planet

@get:DrawableRes
val Planet.imageResId: Int
    get() = when (this) {
        Planet.MERCURY -> R.drawable.img_planet_mercury_1786254595592
        Planet.VENUS -> R.drawable.img_planet_venus_1786254605241
        Planet.MARS -> R.drawable.img_planet_mars_1786254615700
        Planet.JUPITER -> R.drawable.img_planet_jupiter_1786254626802
        Planet.SATURN -> R.drawable.img_planet_saturn_1786254636266
        Planet.URANUS -> R.drawable.img_planet_uranus_1786254645944
        Planet.NEPTUNE -> R.drawable.img_planet_neptune_1786254655513
    }
