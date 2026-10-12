package com.fedo.modelpulse

import android.app.Application
import android.util.Log
import com.fedo.modelpulse.di.dataModule
import com.fedo.modelpulse.di.uiModule
import com.fedo.sdk.Fedo
import com.fedo.sdk.FedoLogHandler
import com.fedo.sdk.FedoLogLevel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ModelPulseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ModelPulseApplication)
            modules(dataModule, uiModule)
        }

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
            logHandler = FedoLogHandler { level, message, throwable ->
                when (level) {
                    FedoLogLevel.INFO,
                    FedoLogLevel.VERBOSE,
                    FedoLogLevel.DEBUG -> Unit
                    FedoLogLevel.WARN -> Log.w("ModelPulse", message, throwable)
                    FedoLogLevel.ERROR -> Log.e("ModelPulse", message, throwable)
                }
            }
        }
    }

    private companion object {
        const val TAG = "ModelPulse"
    }
}
