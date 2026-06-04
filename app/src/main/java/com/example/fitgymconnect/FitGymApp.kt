package com.example.fitgymconnect

import android.app.Application
import com.example.fitgymconnect.utils.Constants
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FitGymApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PaymentConfiguration.init(applicationContext, Constants.STRIPE_PUBLISHABLE_KEY)
    }
}
