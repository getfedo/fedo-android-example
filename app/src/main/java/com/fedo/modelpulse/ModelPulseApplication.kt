package com.fedo.modelpulse

import android.app.Application
import android.util.Log
import com.fedo.sdk.Fedo

class ModelPulseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (!FedoIntegration.isConfigured) {
            // Never log the key itself, only whether one was found.
            Log.i(
                TAG,
                "No FEDO_API_KEY found, skipping Fedo setup. Add one to local.properties " +
                    "to enable the feedback surfaces; see local.properties.example.",
            )
            return
        }

        Fedo.initialize(this, BuildConfig.FEDO_API_KEY) {
            debug = BuildConfig.DEBUG
        }
    }

    private companion object {
        const val TAG = "ModelPulse"
    }
}
