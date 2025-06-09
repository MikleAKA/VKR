package com.mikleaka.russianintonation.ui.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mikleaka.russianintonation.data.models.PracticeResult
import com.mikleaka.russianintonation.data.repository.AudioRepository
import com.mikleaka.russianintonation.data.repository.UserProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import com.mikleaka.russianintonation.data.PracticeResultHolder
import com.mikleaka.russianintonation.RussianIntonationApp
import java.io.IOException

/**
 * ViewModel для экрана результатов
 */
class ResultsViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val userProgressRepository = UserProgressRepository(application)
    private val audioRepository = AudioRepository(application)
    
    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    /**
     * Загружает данные для экрана результатов (из Holder'а)
     */
    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        val result = PracticeResultHolder.result
        if (result == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Результат не найден") }
            return
        }

        // Если score >= 80, отмечаем уровень как пройденный
        if (result.score >= 80) {
            userProgressRepository.setLevelPassed(result.levelId)
                        }

        _uiState.update { it.copy(practiceResult = result, isLoading = false) }

        // Очищаем Holder после использования
        // PracticeResultHolder.result = null // Пока не очищаем, т.к. файл может понадобиться для воспроизведения
    }
    
    /**
     * Воспроизводит записанное аудио
     */
    fun playRecordedAudio() {
        val file = PracticeResultHolder.audioFile ?: return

        // Если уже воспроизводится, останавливаем
        if (_uiState.value.isPlayingRecording) {
            stopPlayingRecording()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPlayingRecording = true) }

            audioRepository.playAudio(file).fold(
                onSuccess = {
                    // Наблюдаем за статусом воспроизведения
                    viewModelScope.launch {
                        while (audioRepository.isPlaying()) {
                            kotlinx.coroutines.delay(300)
                        }
                        // Воспроизведение завершилось, обновляем UI
                        _uiState.update { it.copy(isPlayingRecording = false) }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isPlayingRecording = false) }
                    error.printStackTrace()
                }
            )
        }
    }
    
    /**
     * Останавливает воспроизведение аудио
     */
    private fun stopPlayingRecording() {
        _uiState.update { it.copy(isPlayingRecording = false) }
        viewModelScope.launch {
            audioRepository.stopAudio()
        }
    }

    override fun onCleared() {
        PracticeResultHolder.result = null
        PracticeResultHolder.audioFile = null
        viewModelScope.launch {
            audioRepository.stopAudio()
        }
        super.onCleared()
    }
    
    /**
     * Создает фиктивный результат для демонстрации
     */
    private fun createMockResult(): PracticeResult {
        // Генерируем фиктивные точки графика
        val userPoints = generateMockIntonationPoints(20, jitter = 0.2)
        val referencePoints = generateMockIntonationPoints(20, jitter = 0.0)
        
        return PracticeResult(
            id = "mock_result",
            userId = "user1",
            levelId = "level1",
            constructionId = "ik1",
            recordingUrl = "mock_recording.mp3",
            score = 9.2F,
            intonationPoints = userPoints,
            referencePoints = referencePoints,
            timestamp = System.currentTimeMillis(),
            feedback = "Хорошо! Есть небольшие отклонения в интонации, но в целом правильно. Обратите внимание на повышение тона в середине фразы."
        )
    }
    
    /**
     * Генерирует фиктивные точки интонационного графика
     */
    private fun generateMockIntonationPoints(count: Int, jitter: Double): List<com.mikleaka.russianintonation.data.models.IntonationPoint> {
        return (0 until count).map { i ->
            val timeMs = (i * 100).toLong()
            val frequency = 100.0 + Math.sin(i.toDouble() / 2) * 50 + Math.random() * 20 * jitter
            val amplitude = 0.5 + Math.cos(i.toDouble() / 3) * 0.3 + Math.random() * 0.1 * jitter
            com.mikleaka.russianintonation.data.models.IntonationPoint(timeMs, frequency, amplitude)
        }
    }
}

/**
 * Состояние UI экрана результатов
 */
data class ResultsUiState(
    val isLoading: Boolean = false,
    val practiceResult: PracticeResult? = null,
    val isPlayingRecording: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Factory для создания ViewModel с параметрами
 */
class ResultsViewModelFactory() : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResultsViewModel::class.java)) {
            return ResultsViewModel(
                application = RussianIntonationApp.getInstance()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 