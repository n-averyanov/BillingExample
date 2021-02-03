package ru.n_aver.billingexample

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import ru.n_aver.billingexample.models.Purchases
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : DefaultLifecycleObserver, BillingClientStateListener, PurchasesUpdatedListener {

    @Volatile
    lateinit var billingClient: BillingClient

    private lateinit var billingClientScope: CoroutineScope
    val purchasesStateFlow = MutableStateFlow<List<Purchase>>(listOf())
    val skuDetailsStateFlow = MutableStateFlow<List<SkuDetails>>(listOf())

    override fun onBillingSetupFinished(p0: BillingResult) {
        Log.i("BillingClient", "Successful connection to google play")
        querySkuDetails()
    }

    private fun querySkuDetails() {
        if (!::billingClient.isInitialized || !billingClient.isReady) {
            return
        }
        val inAppRequest = SkuDetailsParams
            .newBuilder()
            .setType(BillingClient.SkuType.INAPP)
            .setSkusList(Purchases.purchases)
            .build()

        billingClient.querySkuDetailsAsync(
            inAppRequest
        ) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.i("BillingManager", skuDetailsList?.toString() ?: "Epmty sku")
                skuDetailsStateFlow.value = skuDetailsList ?: listOf()
            } else {
                Log.i("BillingManager", billingResult.responseCode.toString())
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        billingClientScope.cancel()
        billingClientScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        billingClient.startConnection(this)
    }

    @ExperimentalCoroutinesApi
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchasesStateFlow.value = purchases ?: listOf()
        }
    }

    @MainThread
    fun launchBillingFlow(
        activity: Activity,
        skuDetails: SkuDetails
    ) = billingClient.launchBillingFlow(
        activity,
        BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
    )

    override fun onCreate(owner: LifecycleOwner) {
        billingClient = BillingClient
            .newBuilder(context)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        if (!billingClient.isReady) {
            billingClientScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            billingClient.startConnection(this)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (billingClient.isReady) {
            billingClientScope.cancel()
            billingClient.endConnection()
        }
    }
}