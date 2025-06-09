package com.mikleaka.russianintonation.data

import com.mikleaka.russianintonation.data.models.PracticeResult
import java.io.File

/**
 * Временное хранилище для передачи результата практики между экранами.
 * Это простое решение для избежания передачи сложных объектов через навигацию.
 */
object PracticeResultHolder {
    var result: PracticeResult? = null
    var audioFile: File? = null
} 