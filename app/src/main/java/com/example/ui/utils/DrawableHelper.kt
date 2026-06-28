package com.example.ui.utils

import android.content.Context
import com.example.R

object DrawableHelper {
    fun getDrawableIdByName(name: String): Int {
        return when (name) {
            "img_hero_dune" -> R.drawable.img_hero_dune_1782494377779
            "img_poster_cyberpunk" -> R.drawable.img_poster_cyberpunk_1782494394074
            "img_poster_fantasy" -> R.drawable.img_poster_fantasy_1782494405615
            "img_profile_avatar" -> R.drawable.img_profile_avatar_1782494416410
            "img_natok_banner" -> R.drawable.img_natok_banner_1782589071496
            "img_natok_comedy_banner" -> R.drawable.img_natok_comedy_banner_1782636977780
            "img_action_banner" -> R.drawable.img_action_banner_1782589087194
            "img_app_icon_cinema" -> R.drawable.img_app_icon_cinema_1782585891873
            else -> R.drawable.img_poster_cyberpunk_1782494394074 // default fallback
        }
    }
}
