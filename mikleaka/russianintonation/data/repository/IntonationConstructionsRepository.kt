package com.mikleaka.russianintonation.data.repository

import android.content.Context
import com.mikleaka.russianintonation.data.models.Difficulty
import com.mikleaka.russianintonation.data.models.IntonationConstruction
import com.mikleaka.russianintonation.data.models.Level
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Репозиторий для работы с интонационными конструкциями
 */
class IntonationConstructionsRepository(private val context: Context) {
    
    /**
     * Получает список всех интонационных конструкций
     */
    fun getIntonationConstructions(): Flow<List<IntonationConstruction>> = flow {
        val constructions = listOf(
            IntonationConstruction(
                id = "ik1",
                title = "ИК-1",
                description = "Интонационная конструкция завершенности. Используется в повествовательных предложениях и специальных вопросах.",
                imageUrl = null,
                levels = listOf(
                    Level(
                        id = "ik1_level1",
                        title = "Это мой дом",
                        description = "Простое предложение с ИК-1",
                        phrase = "Это мой дом",
                        referenceAudioUrl = "ik1/Its_my_house/Its_my_house44.wav",
                        difficulty = Difficulty.EASY
                    ),
                    Level(
                        id = "ik1_level2",
                        title = "Скоро наступит зима",
                        description = "Предложение с ИК-1 в конце",
                        phrase = "Скоро наступит зима",
                        referenceAudioUrl = "ik1/Winter_is_coming_soon/Winter_is_coming_soon44.wav",
                        difficulty = Difficulty.MEDIUM
                    ),
                    Level(
                        id = "ik1_level3",
                        title = "Москва - столица России",
                        description = "Повествовательное предложение с ИК-1",
                        phrase = "Москва - столица России",
                        referenceAudioUrl = "ik1/Moscow_is_the_capital_of_Russia/Moscow_is_the_capital_of_Russia45.wav",
                        difficulty = Difficulty.HARD
                    )
                )
            ),
            IntonationConstruction(
                id = "ik2",
                title = "ИК-2",
                description = "Интонационная конструкция для волеизъявления. Используется в императивах и общих вопросах с вопросительным словом.",
                imageUrl = null,
                levels = listOf(
                    Level(
                        id = "ik2_level1",
                        title = "Кто пришёл?",
                        description = "Вопросительное предложение с ИК-2",
                        phrase = "Кто пришёл?",
                        referenceAudioUrl = "ik2/Whos_here/Whos_here44.wav",
                        difficulty = Difficulty.EASY
                    ),
                    Level(
                        id = "ik2_level2",
                        title = "Где ты был?",
                        description = "Вопрос с вопросительным словом",
                        phrase = "Где ты был?",
                        referenceAudioUrl = "ik2/Where_were_you/Where_were_you43.wav",
                        difficulty = Difficulty.MEDIUM
                    ),
                    Level(
                        id = "ik2_level3",
                        title = "Как тебя зовут?",
                        description = "Вопрос с вопросительным словом",
                        phrase = "Как тебя зовут?",
                        referenceAudioUrl = "ik2/Whats_your_name/Whats_your_name43.wav",
                        difficulty = Difficulty.HARD
                    )
                )
            )
        )
        
        emit(constructions)
    }
    
    /**
     * Получает конкретную интонационную конструкцию по идентификатору
     */
    fun getIntonationConstructionById(id: String): Flow<IntonationConstruction?> = flow {
        val constructions = getIntonationConstructions().collect { constructions ->
            val construction = constructions.find { it.id == id }
            emit(construction)
        }
    }
    
    /**
     * Получает полный путь к аудио-файлу
     */
    fun getAudioFilePath(relativePath: String): String {
        return "file:///android_asset/$relativePath"
    }
} 