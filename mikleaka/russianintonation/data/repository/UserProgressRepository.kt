package com.mikleaka.russianintonation.data.repository

import android.content.Context
import android.content.SharedPreferences

class UserProgressRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_progress", Context.MODE_PRIVATE)
    private val KEY_PASSED_LEVELS = "passed_levels"

    fun isLevelPassed(levelId: String): Boolean {
        return getPassedLevels().contains(levelId)
    }

    fun setLevelPassed(levelId: String) {
        val set = getPassedLevels().toMutableSet()
        set.add(levelId)
        prefs.edit().putStringSet(KEY_PASSED_LEVELS, set).apply()
    }

    fun getPassedLevels(): Set<String> {
        return prefs.getStringSet(KEY_PASSED_LEVELS, emptySet()) ?: emptySet()
    }

    fun getPassedCountForConstruction(levelIds: List<String>): Int {
        val passed = getPassedLevels()
        return levelIds.count { passed.contains(it) }
    }
} 