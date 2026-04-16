package com.gluedin.sample.gluedin_quick_launch_sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingClient
import com.gluedin.GluedInInitializer
import com.gluedin.callback.AdsStatus
import com.gluedin.callback.GIAdsCallback
import com.gluedin.callback.GIAssetCallback
import com.gluedin.callback.GIInitCallback
import com.gluedin.callback.GIPaymentCallback
import com.gluedin.callback.GISdkCallback
import com.gluedin.callback.PaymentMethod
import com.gluedin.callback.PaymentStatus
import com.gluedin.callback.SDKInitStatus
import com.gluedin.callback.SubscriptionDetails
import com.gluedin.callback.UserAction
import com.gluedin.callback.UserAuthStatus
import com.gluedin.domain.entities.config.ShareData
import com.gluedin.domain.entities.feed.VideoInfo
import com.gluedin.domain.entities.feed.ads.AdsRequestParams
import com.gluedin.domain.entities.feed.ads.BannerAdsType
import com.gluedin.domain.entities.feed.ads.InterstitialAdsType
import com.gluedin.domain.entities.feed.ads.NativeAdsType
import com.gluedin.exception.GluedInSdkException
import com.gluedin.sample.ads.BannerAdLoader
import com.gluedin.sample.ads.InterstitialAdManager
import com.gluedin.sample.ads.NativeAdKotlinFragment
import com.gluedin.sample.ads.RewardedInterstitialManager
import com.gluedin.sample.payment.BillingManager
import com.gluedin.sample.shopify.ShopifyCartManager
import com.gluedin.sample.shopify.ViewCartActivity
import com.gluedin.sample.shopify.WebViewActivity
import com.gluedin.usecase.constants.GluedInConstants
import com.gluedin.view.BannerAdView
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity(){

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
        val gIInitCallBack = object : GIInitCallback {
            override fun onSDKLifecycle(
                mSDKInitStatus: SDKInitStatus, gluedInSdkException: GluedInSdkException?,
            ) {
                try {
                    when (mSDKInitStatus) {
                        SDKInitStatus.SDK_INIT -> {
                            if (mSDKInitStatus.value) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "GluedInSDK Launched Successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                gluedInSdkException?.getErrorMessage().let {
                                    Toast.makeText(
                                        this@MainActivity, it, Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        SDKInitStatus.SDK_EXIT -> {
                            Toast.makeText(this@MainActivity, "SDK_EXIT", Toast.LENGTH_SHORT)
                                .show()
                        }

                        SDKInitStatus.SDK_AUTH -> {
                            Toast.makeText(this@MainActivity, "USER_AUTH", Toast.LENGTH_SHORT)
                                .show()
                        }

                        else -> {}
                    }
                } catch (_: Exception) {
                }
            }
        }

        val gISdkCallBack = object : GISdkCallback {

            override fun onUserAuthStatus(
                userAuthStatus: UserAuthStatus,
                currentVideo: VideoInfo?,
            ) {
                when (userAuthStatus) {
                    UserAuthStatus.USER_LOGIN_REQUIRED -> {
                    }

                    UserAuthStatus.USER_LOGOUT -> {
                        Toast.makeText(this@MainActivity, "user logout", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> {}
                }
            }

            override fun onShareAction(shareData: ShareData) {
                Toast.makeText(
                    this@MainActivity, "Share data: "+shareData.title, Toast.LENGTH_SHORT
                ).show()
            }

            override fun onUserProfileClick(userId: String) {
                Toast.makeText(
                    this@MainActivity, "on User ProfileClick -> $userId", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onRewardClick() = Unit
            override fun onWatchNowAction(deeplink: String) {
                TODO("Not yet implemented")
            }
        }

        val giAdsCallback = object : GIAdsCallback{
            override fun onNativeRequest(
                adsType: NativeAdsType, adsRequestParams: AdsRequestParams
            ): Fragment? {
                when (adsType) {
                    NativeAdsType.AD_MOB_NATIVE, NativeAdsType.GAM_NATIVE -> {
                        return NativeAdKotlinFragment(
                            adsType, adsRequestParams, this@MainActivity.application
                        )
                    }
                }
                return null
            }

            override fun onBannerAdsRequest(
                adsType: BannerAdsType, adsRequestParams: AdsRequestParams, view: BannerAdView?
            ) {
                view?.let {
                    BannerAdLoader(view.context, it, adsType, adsRequestParams).loadAd()
                }

            }

            override fun onInterstitialAdsRequest(
                adsType: InterstitialAdsType, adsRequestParams: AdsRequestParams
            ) {
                InterstitialAdManager.loadAndShow(
                    this@MainActivity, adsRequestParams.adsId
                )
            }

        }

        val gluedInConfigurations = GluedInInitializer.Configurations.Builder()
            .setLogEnabled(true, Log.DEBUG)
            .setHttpLogEnabled(true,3)
            .setSdkCallback(gISdkCallBack)
            .setSdkInitCallback(gIInitCallBack)
            .setGIAdsCallback(giAdsCallback)
            .setGIAssetCallback(giECommerceCallback)
            .setGIPaymentCallback(giPaymentCallBack)
            .setApiAndSecret("put_your_api_key","put_your_secret_key")
            .create()
        gluedInConfigurations.validateAndLaunchGluedInSDK(
            this,
            GluedInConstants.LaunchType.APP,
            intent,
        )
    }

    val giPaymentCallBack = object : GIPaymentCallback {


        override fun onInitiateSeriesPurchase(
            paymentMethod: PaymentMethod,
            inAppSkuId: String?,
            basePlanId: String?,
            offerId: String?,
            purchaseUrl: String?,
            seriesId: String?,
            episodeNumber: Int,
            packageId: String,
            userId: String,
            onNotifyPaymentResult: (status: PaymentStatus, String, String, PaymentMethod) -> Unit
        ) {

            // Handle payments (in-app or subscription)
            if (PaymentMethod.IN_APP_PURCHASE == paymentMethod) {
                /*
                  //TODO:  Enable this method for the actual use case.
                  callBillingManager(
                          inAppSkuId,
                          basePlanId,
                          purchaseUrl,
                          seriesId,
                          packageId,
                          userId,
                          paymentMethod,
                          onNotifyPaymentResult
                      )
                 */

                Toast.makeText(
                    this@MainActivity,
                    "In-app purchases haven’t been configured yet.",
                    Toast.LENGTH_SHORT
                ).show()

            } else if (PaymentMethod.SUBSCRIPTION_PLAN == paymentMethod) {
                /*
                     //TODO: Enable this method for the actual use case.
                       callBillingManagerForSubscription(
                                        inAppSkuId,
                                        basePlanId,
                                        purchaseUrl,
                                        seriesId,
                                        packageId,
                                        userId,
                                        paymentMethod,
                                        onNotifyPaymentResult,
                                    )
                    */
                Toast.makeText(
                    this@MainActivity,
                    "Subscription hasn’t been configured yet.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (PaymentMethod.PAYMENT_GATEWAY == paymentMethod) {
                // Open browser with subscription deeplink
                Toast.makeText(
                    this@MainActivity,
                    "onSelectPaymentMethod seriesId :$seriesId \n paymentUrl : $purchaseUrl \n deeplink : $purchaseUrl",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this@MainActivity, "Other Payment Method", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onRewardedAdRequested(
            adUnitID: String,
            adsType: String,
            seriesId: String?,
            onNotifyAdsResult: (AdsStatus) -> Unit
        ) {
            rewardedInterstitialAd(adUnitID, adsType, seriesId, onNotifyAdsResult)
        }

        override fun onProductDetailsFetched(
            inAppSkuId: List<String>?,
            paymentMethod: PaymentMethod,
            onNotifyPriceResult: (Map<String, Any?>) -> Unit
        ) {
            if (PaymentMethod.IN_APP_PURCHASE == paymentMethod) {
                /* TODO: Enable this method to initiate Purchase from Google Play Store. */

//                 inAppSkuId?.let {
//                       fetchPricePartsForSkus(
//                           inAppSkuId,
//                           BillingClient.ProductType.INAPP,
//                           onNotifyPriceResult
//                       )
//                   }
            } else if (PaymentMethod.SUBSCRIPTION_PLAN == paymentMethod) {
                /* TODO: Enable this method to initiate Purchase from Google Play Store. */

//                inAppSkuId?.let {
//                    fetchPricePartsForSkus(
//                        it,
//                        BillingClient.ProductType.SUBS,
//                        onNotifyPriceResult
//                    )
//                }
            }
        }

        override fun onManageSubscription(
            paymentMethod: PaymentMethod,
            inAppSkuId: String,
            userId: String,
            onNotifyResult: (status: Boolean) -> Unit
        ) {
            BillingManager.openPlayStoreSubscription(this@MainActivity, inAppSkuId, packageName)
        }

        override fun onUpgradeSubscriptionList(
            inAppSkuId: String,
            paymentMethod: PaymentMethod,
            onNotifyPriceResult: (Map<String, SubscriptionDetails?>, String) -> Unit
        ) {
            if (PaymentMethod.SUBSCRIPTION_PLAN == paymentMethod) {
                BillingManager.fetchPricePartsForSkus(
                    activity = this@MainActivity,
                    skuIds = listOf(inAppSkuId),
                    productType = BillingClient.ProductType.SUBS
                ) { resultMap ->
                    BillingManager.getActiveBasePlanId(inAppSkuId) { _ ->

                    }

                }
            }

        }
    }

    private fun rewardedInterstitialAd(
        adId: String,
        platformName: String,
        seriesId: String?,
        onNotifyAdsResult: (AdsStatus) -> Unit
    ) {
        RewardedInterstitialManager.load(context = this, adUnitId = adId, onLoaded = {
            RewardedInterstitialManager.show(
                activity = this,
                adId = adId,
                onReward = { _ -> },
                onClosed = {
                    onNotifyAdsResult.invoke(AdsStatus.AdsSuccess)
                },
                onFailed = { error ->
                    onNotifyAdsResult.invoke(AdsStatus.AdsFailed)
                },
            )
        }, onFailed = { _ ->
            onNotifyAdsResult.invoke(AdsStatus.AdsFailed)
        })
    }

    // Callback for E-Commerce actions
    val giECommerceCallback = object : GIAssetCallback {

        override fun onUserAction(
            context: Context,
            action: UserAction,
            assetId: String?,
            eventRefId: Int?,
            callback: ((Int) -> Unit)?
        ) {
            if (UserAction.ADD_TO_CART == action) {
                ShopifyCartManager.init(this@MainActivity)
                callback?.let {
                    ShopifyCartManager.showProductDetails(
                        context, assetId.toString(), it
                    )
                }

            }
        }

        override fun navigateToCart() {
            val intent = Intent(this@MainActivity, ViewCartActivity::class.java)
            startActivity(intent)
        }

        override fun getCartItemCount(callback: (Int) -> Unit) {
            ShopifyCartManager.init(this@MainActivity)
            ShopifyCartManager.getTotalCartItems() { cartCount ->
                callback(cartCount)
            }
        }

        override fun showOrderHistory() {
            ShopifyCartManager.init(this@MainActivity)
            val orderHistory = ShopifyCartManager.getOderHistoryUrl()
            val intent = Intent(this@MainActivity, WebViewActivity::class.java)
            intent.putExtra("url", orderHistory)
            intent.putExtra("title", "My Orders")
            startActivity(intent)
        }

    }
}
