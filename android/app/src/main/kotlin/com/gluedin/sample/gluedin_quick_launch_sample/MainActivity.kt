package com.gluedin.sample.gluedin_quick_launch_sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.gluedin.GluedInInitializer
import com.app.usecase.constants.GluedInConstants
import com.gluedin.config.login.GluedInSDKCallBack
import com.gluedin.config.login.GluedInSdkException
import com.gluedin.domain.entities.feed.VideoInfo
import com.gluedin.domain.entities.config.ShareData
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MethodChannel(flutterEngine?.dartExecutor?.binaryMessenger!!, "com.gluedin.io/gluedinsdk")
            .setMethodCallHandler { methodCall, result ->
                methodCall.method?.let {
                   if (it.contentEquals("launchGluedInApp")) {
                        launchGluedInApp()
                    }
                }
            }
    }

    private fun launchGluedInApp() {
        val callback = object : GluedInSDKCallBack {
            override fun onSdkInitSuccess(
                isSuccess: Boolean,
                gluedInSdkException: GluedInSdkException?
            ) {

                if (isSuccess) {
                    Toast.makeText(
                        this@MainActivity,
                        "GluedInSDK Launched Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    gluedInSdkException?.getErrorMessage().let {
                        Toast.makeText(
                            this@MainActivity,
                            it,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }

            override fun onAppAuthSuccess(
                isSuccess: Boolean,
                gluedInSdkException: GluedInSdkException?
            ) = Unit

            override fun onLoginRegistrationRequired(
                currentVideo: VideoInfo?,
                isLoginAction: Boolean
            )= Unit

            override fun onSdkLogout() = Unit

            override fun onSdkExit() = Unit

            override fun onGluedInShareAction(shareData: ShareData) = Unit

            override fun onProductCTAClicked(assetId: String, eventRefId: Int) = Unit

            override fun onUserProfileClick(userId: String)= Unit

        }

        val gluedInConfigurations = GluedInInitializer.Configurations.Builder()
            .setLogEnabled(true, Log.DEBUG)
            .setSdkCallback(callback)
            .setApiKey("put_your_api_key")
            .setSecretKey("put_your_secret_key")
            .setHttpLogEnabled(true, 3)
            .setDeeplinkAuthority("sample.com")
            .setFeedType(GluedInInitializer.Configurations.FeedType.VERTICAL)

            .create()
        val localIntent = Intent()
        gluedInConfigurations.validateAndLaunchGluedInSDK(
            this,
            GluedInConstants.LaunchType.APP,
            localIntent
        )

    }
}
