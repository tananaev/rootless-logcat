package com.tananaev.logcat

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class GoogleActivity : MainActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        firebaseAnalytics = Firebase.analytics
        MobileAds.initialize(this) {}
        setContentView(R.layout.activity_google)
        super.onCreate(savedInstanceState)
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (adapter.itemCount > 0) {
                    findViewById<AdView>(R.id.ad_view).loadAd(AdRequest.Builder().build())
                    adapter.unregisterAdapterDataObserver(this)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        handleRating()
    }

    @Suppress("DEPRECATION")
    private fun handleRating() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (!preferences.getBoolean("ratingShown", false)) {
            val openTimes = preferences.getInt("openTimes", 0) + 1
            preferences.edit().putInt("openTimes", openTimes).apply()
            if (openTimes >= 5) {
                val reviewManager = ReviewManagerFactory.create(this)
                reviewManager.requestReviewFlow().addOnCompleteListener { infoTask ->
                    if (infoTask.isSuccessful) {
                        val flow = reviewManager.launchReviewFlow(this, infoTask.result)
                        flow.addOnCompleteListener {
                            preferences.edit().putBoolean("ratingShown", true).apply()
                        }
                    }
                }
            }
        }
    }
}
