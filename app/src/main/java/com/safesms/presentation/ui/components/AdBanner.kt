package com.safesms.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.safesms.presentation.ui.theme.Surface as SurfaceColor
import com.safesms.presentation.ui.theme.SurfaceStroke
import com.safesms.util.Constants

/**
 * Banner AdMob con tarjeta contenida y borde suave (igual al mockup).
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier
) {
    var adLoaded by remember { mutableStateOf(false) }
    var adError by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceColor,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, SurfaceStroke)
    ) {
        if (adError != null && !adLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    AdView(ctx).apply {
                        adUnitId = Constants.ADMOB_BANNER_ID
                        setAdSize(AdSize.LARGE_BANNER)

                        adListener = object : com.google.android.gms.ads.AdListener() {
                            override fun onAdLoaded() {
                                adLoaded = true
                                adError = null
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                adError = error.message
                                adLoaded = false
                            }
                        }

                        loadAd(AdRequest.Builder().build())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                update = {}
            )
        }
    }
}
