package ru.n_aver.billingexample.ui.main

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.SkuDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.n_aver.billingexample.BillingManager
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {
    val availablePurchases = billingManager.skuDetailsStateFlow
    val purchases = billingManager.purchasesStateFlow

    fun startBillingFlow(activity: Activity, skuDetails: SkuDetails) =
        billingManager.launchBillingFlow(activity, skuDetails)
}