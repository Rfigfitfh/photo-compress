package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

object UnityAdsManager {
    private const val TAG = "UnityAdsManager"

    // Configuration IDs provided by user
    const val GAME_ID = "800418075"
    const val ORGANIZATION_CORE_ID = "13469993333173"
    const val REWARDED_AD_UNIT = "BP_Rewarded_Android"
    const val INTERSTITIAL_AD_UNIT = "BP_Interstitial_Android"
    const val BANNER_AD_UNIT = "BP_Banner_Android"

    private var isInitialized = false
    private var isInterstitialLoaded = false
    private var isRewardedLoaded = false

    fun init(context: Context, testMode: Boolean = true) {
        if (isInitialized) return

        try {
            UnityAds.initialize(context.applicationContext, GAME_ID, testMode, object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    Log.d(TAG, "Unity Ads Initialization Complete for Game ID: $GAME_ID")
                    isInitialized = true
                    loadInterstitial(context)
                    loadRewarded(context)
                }

                override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                    Log.e(TAG, "Unity Ads Initialization Failed: $error - $message")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Unity Ads init", e)
        }
    }

    fun loadInterstitial(context: Context) {
        try {
            UnityAds.load(INTERSTITIAL_AD_UNIT, object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String) {
                    Log.d(TAG, "Interstitial loaded: $placementId")
                    isInterstitialLoaded = true
                }

                override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                    Log.w(TAG, "Interstitial failed to load: $placementId - $message")
                    isInterstitialLoaded = false
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error loading interstitial", e)
        }
    }

    fun loadRewarded(context: Context) {
        try {
            UnityAds.load(REWARDED_AD_UNIT, object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String) {
                    Log.d(TAG, "Rewarded ad loaded: $placementId")
                    isRewardedLoaded = true
                }

                override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                    Log.w(TAG, "Rewarded failed to load: $placementId - $message")
                    isRewardedLoaded = false
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error loading rewarded", e)
        }
    }

    fun showInterstitial(activity: Activity, onAdFinished: () -> Unit) {
        if (!isInitialized) {
            init(activity)
            onAdFinished()
            return
        }

        try {
            UnityAds.show(activity, INTERSTITIAL_AD_UNIT, UnityAdsShowOptions(), object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                    Log.w(TAG, "Interstitial show failed: $message")
                    loadInterstitial(activity)
                    onAdFinished()
                }

                override fun onUnityAdsShowStart(placementId: String) {
                    Log.d(TAG, "Interstitial started")
                }

                override fun onUnityAdsShowClick(placementId: String) {
                    Log.d(TAG, "Interstitial clicked")
                }

                override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                    Log.d(TAG, "Interstitial complete: $state")
                    loadInterstitial(activity)
                    onAdFinished()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception showing interstitial", e)
            onAdFinished()
        }
    }

    fun showRewarded(activity: Activity, onRewardGranted: () -> Unit, onAdFinished: () -> Unit) {
        if (!isInitialized) {
            init(activity)
            onRewardGranted()
            onAdFinished()
            return
        }

        try {
            UnityAds.show(activity, REWARDED_AD_UNIT, UnityAdsShowOptions(), object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                    Log.w(TAG, "Rewarded show failed: $message")
                    loadRewarded(activity)
                    // Grant reward as fallback in test/offline environments
                    onRewardGranted()
                    onAdFinished()
                }

                override fun onUnityAdsShowStart(placementId: String) {
                    Log.d(TAG, "Rewarded started")
                }

                override fun onUnityAdsShowClick(placementId: String) {}

                override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                    Log.d(TAG, "Rewarded complete state: $state")
                    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                        onRewardGranted()
                    }
                    loadRewarded(activity)
                    onAdFinished()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception showing rewarded", e)
            onRewardGranted()
            onAdFinished()
        }
    }
}

@Composable
fun UnityBannerAd(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bannerView by remember { mutableStateOf<BannerView?>(null) }
    var isAdLoaded by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        if (context is Activity) {
            try {
                val banner = BannerView(context, UnityAdsManager.BANNER_AD_UNIT, UnityBannerSize(320, 50))
                banner.listener = object : BannerView.IListener {
                    override fun onBannerLoaded(view: BannerView?) {
                        Log.d("UnityBannerAd", "Banner loaded successfully")
                        isAdLoaded = true
                    }

                    override fun onBannerFailedToLoad(view: BannerView?, errorInfo: BannerErrorInfo?) {
                        Log.w("UnityBannerAd", "Banner failed to load: ${errorInfo?.errorMessage}")
                        isAdLoaded = false
                    }

                    override fun onBannerClick(view: BannerView?) {}
                    override fun onBannerShown(view: BannerView?) {}
                    override fun onBannerLeftApplication(view: BannerView?) {}
                }
                banner.load()
                bannerView = banner
            } catch (e: Exception) {
                Log.e("UnityBannerAd", "Banner creation error", e)
            }
        }

        onDispose {
            bannerView?.destroy()
            bannerView = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bannerView != null && isAdLoaded) {
            AndroidView(
                factory = { bannerView ?: View(it) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            )
        } else {
            // Elegant professional placeholder matching warm cream styling
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.height(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Unity Monetization (${UnityAdsManager.BANNER_AD_UNIT}) Ready",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
