package com.gluedin.sample.gluedin_quick_launch_sample

import android.app.Application
import io.flutter.app.FlutterApplication
import io.flutter.view.FlutterMain
import com.gluedin.GluedInInitializer

class Application : FlutterApplication() {

    override fun onCreate() {
        super.onCreate()
        FlutterMain.startInitialization(this)

        GluedInInitializer.initSdk(this)
    }
}