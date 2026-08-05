package com.example.util

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale
import kotlin.math.max

class LicenseManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "tailor_book_license_prefs"
        private const val KEY_INSTALL_TIME = "key_install_time"
        private const val KEY_IS_ACTIVATED = "key_is_activated"
        private const val KEY_ACTIVATION_KEY = "key_activation_key"
        private const val KEY_SHOP_ID = "key_shop_id"
        private const val KEY_EASYPAISA_NO = "key_easypaisa_no"
        private const val KEY_EASYPAISA_NAME = "key_easypaisa_name"

        // Free trial period: 7 days in milliseconds
        private const val TRIAL_DURATION_MS = 7 * 24 * 60 * 60 * 1000L

        // Universal Master Activation Passcodes for owner unlock (10 professional keys)
        private val MASTER_KEYS = listOf(
            "TAILOR-786",
            "TAILOR-2026",
            "PAID123",
            "TB-PRO-8899",
            "TAILOR-PREMIUM-2026",
            "MASTER-SUIT-786",
            "EASYPAISA-PAID-99",
            "TB-VIP-7860",
            "PAK-TAILOR-PRO",
            "SILAI-MASTER-2026"
        )
    }

    init {
        // Initialize install timestamp if first run
        if (!prefs.contains(KEY_INSTALL_TIME)) {
            val now = System.currentTimeMillis()
            val randomShopNum = (10000..99999).random()
            val shopId = "TLR-$randomShopNum"
            prefs.edit()
                .putLong(KEY_INSTALL_TIME, now)
                .putString(KEY_SHOP_ID, shopId)
                .putString(KEY_EASYPAISA_NO, "03109674455")
                .putString(KEY_EASYPAISA_NAME, "Sahibzada Abdullah Ahmad")
                .apply()
        }
    }

    fun getInstallTime(): Long {
        return prefs.getLong(KEY_INSTALL_TIME, System.currentTimeMillis())
    }

    fun getShopId(): String {
        return prefs.getString(KEY_SHOP_ID, "TLR-78600") ?: "TLR-78600"
    }

    fun isActivated(): Boolean {
        return prefs.getBoolean(KEY_IS_ACTIVATED, false)
    }

    /**
     * Calculates remaining trial days (from 7 down to 0).
     */
    fun getRemainingTrialDays(): Int {
        if (isActivated()) return 7
        val installTime = getInstallTime()
        val currentTime = System.currentTimeMillis()
        val elapsedMs = currentTime - installTime
        val remainingMs = TRIAL_DURATION_MS - elapsedMs
        if (remainingMs <= 0) return 0

        val daysLeft = (remainingMs / (24 * 60 * 60 * 1000L)).toInt() + 1
        return daysLeft.coerceIn(0, 7)
    }

    fun isTrialActive(): Boolean {
        return getRemainingTrialDays() > 0
    }

    fun isAppUnlocked(): Boolean {
        return isActivated() || isTrialActive()
    }

    fun getEasyPaisaNumber(): String {
        return prefs.getString(KEY_EASYPAISA_NO, "03109674455") ?: "03109674455"
    }

    fun getEasyPaisaName(): String {
        return prefs.getString(KEY_EASYPAISA_NAME, "Sahibzada Abdullah Ahmad")
            ?: "Sahibzada Abdullah Ahmad"
    }

    fun setEasyPaisaDetails(number: String, name: String) {
        prefs.edit()
            .putString(KEY_EASYPAISA_NO, number.trim())
            .putString(KEY_EASYPAISA_NAME, name.trim())
            .apply()
    }

    /**
     * Generate shop-specific expected key based on Shop ID.
     * E.g. Shop ID "TLR-59281" -> "TB-59281-786" or similar formula.
     */
    fun generateExpectedKey(shopId: String): String {
        val digits = shopId.filter { it.isDigit() }
        val num = digits.toIntOrNull() ?: 12345
        val code = (num * 3 + 786) % 100000
        return "TB-$digits-$code"
    }

    /**
     * Verifies the activation key strictly. Returns true if valid and activates app.
     */
    fun verifyAndActivate(enteredKey: String): Boolean {
        val cleaned = enteredKey.trim().uppercase(Locale.getDefault())
        if (cleaned.isBlank()) return false

        val currentShopId = getShopId()
        val expectedShopKey = generateExpectedKey(currentShopId).uppercase(Locale.getDefault())

        // Strict verification: only MASTER_KEYS or exact shop key
        val isValid = MASTER_KEYS.contains(cleaned) || cleaned == expectedShopKey

        if (isValid) {
            prefs.edit()
                .putBoolean(KEY_IS_ACTIVATED, true)
                .putString(KEY_ACTIVATION_KEY, cleaned)
                .apply()
            return true
        }
        return false
    }

    /**
     * Resets activation for testing / debug if needed.
     */
    fun deactivateForTesting() {
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVATED, false)
            .remove(KEY_ACTIVATION_KEY)
            .apply()
    }
}
