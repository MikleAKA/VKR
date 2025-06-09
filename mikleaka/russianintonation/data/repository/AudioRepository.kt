package com.mikleaka.russianintonation.data.repository

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.mikleaka.russianintonation.data.models.IntonationPoint
import com.mikleaka.russianintonation.data.models.PracticeResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.floor
import com.mikleaka.russianintonation.data.api.ApiClient
import com.mikleaka.russianintonation.data.models.AnalyzeResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Репозиторий для работы с аудио (запись, воспроизведение, анализ)
 */
class AudioRepository(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordingFile: File? = null
    private var isRecording = false
    private var isPlaying = false

    /**
     * Начинает запись аудио
     * @return File объект файла записи
     */
    suspend fun startRecording(): Result<File> = withContext(Dispatchers.IO) {
        if (isRecording) {
            return@withContext Result.failure(IOException("Запись уже идет"))
        }

        try {
            val outputDir = context.cacheDir
            val outputFile = File(outputDir, "recording_${UUID.randomUUID()}.m4a")
            recordingFile = outputFile

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile.absolutePath)
                
                try {
                    prepare()
                    start()
                    isRecording = true
                    Result.success(outputFile)
                } catch (e: IOException) {
                    Result.failure(e)
                }
            }
            
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Останавливает запись аудио
     * @return путь к записанному файлу
     */
    suspend fun stopRecording(): Result<File> = withContext(Dispatchers.IO) {
        if (!isRecording) {
            return@withContext Result.failure(IOException("Запись не была начата"))
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            val file = recordingFile ?: return@withContext Result.failure(IOException("Файл записи не найден"))
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Воспроизводит аудиофайл
     * @param file файл для воспроизведения
     */
    suspend fun playAudio(file: File): Result<Unit> = withContext(Dispatchers.IO) {
        if (isPlaying) {
            stopAudio()
        }

        try {
            mediaPlayer = createNewMediaPlayer()
            
            mediaPlayer?.apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    this@AudioRepository.isPlaying = false
                    Log.d("AudioRepository", "Воспроизведение аудио завершено по событию onCompletion")
                    release()
                    this@AudioRepository.mediaPlayer = null
                }
                start()
                this@AudioRepository.isPlaying = true
                Log.d("AudioRepository", "Воспроизведение аудио начато: ${file.absolutePath}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AudioRepository", "Ошибка при воспроизведении аудио", e)
            this@AudioRepository.isPlaying = false
            this@AudioRepository.mediaPlayer = null
            Result.failure(e)
        }
    }
    
    /**
     * Воспроизводит аудиофайл из assets
     * @param assetPath путь к файлу в assets
     */
    suspend fun playAudioFromAssets(assetPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (isPlaying) {
            stopAudio()
        }
        
        try {
            var afd: AssetFileDescriptor? = null
            try {
                Log.d("AudioRepository", "Попытка воспроизведения аудио из assets: $assetPath")
                afd = context.assets.openFd(assetPath)
                
                mediaPlayer = createNewMediaPlayer()
                
                mediaPlayer?.apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    prepare()
                    setOnCompletionListener {
                        this@AudioRepository.isPlaying = false
                        Log.d("AudioRepository", "Воспроизведение аудио из assets завершено по событию onCompletion: $assetPath")
                        release()
                        this@AudioRepository.mediaPlayer = null
                    }
                    start()
                    this@AudioRepository.isPlaying = true
                    Log.d("AudioRepository", "Воспроизведение аудио из assets начато: $assetPath")
                }
                afd.close()
                Result.success(Unit)
            } catch (e: IOException) {
                Log.e("AudioRepository", "Ошибка при воспроизведении аудио из assets: $assetPath", e)
                afd?.close()
                this@AudioRepository.isPlaying = false
                this@AudioRepository.mediaPlayer = null
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e("AudioRepository", "Ошибка при воспроизведении аудио из assets", e)
            this@AudioRepository.isPlaying = false
            this@AudioRepository.mediaPlayer = null
            Result.failure(e)
        }
    }

    /**
     * Останавливает воспроизведение аудио
     */
    suspend fun stopAudio(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val player = mediaPlayer
            
            mediaPlayer = null
            isPlaying = false
            
            player?.apply {
                try {
                    if (isPlaying) {
                        stop()
                    }
                } catch (e: Exception) {
                    Log.e("AudioRepository", "Ошибка при остановке воспроизведения", e)
                }
                
                try {
                    release()
                } catch (e: Exception) {
                    Log.e("AudioRepository", "Ошибка при освобождении ресурсов MediaPlayer", e)
                }
            }
            
            Log.d("AudioRepository", "Воспроизведение аудио остановлено")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AudioRepository", "Критическая ошибка при остановке воспроизведения аудио", e)
            mediaPlayer = null
            isPlaying = false
            Result.failure(e)
        }
    }
    
    /**
     * Проверяет, воспроизводится ли аудио в данный момент
     */
    fun isPlaying(): Boolean {
        return isPlaying
    }

    /**
     * Анализирует записанную интонацию и сравнивает с эталонной (реальный запрос к серверу)
     * @param recordingFile файл записи
     * @param phrase текст фразы
     * @return результат анализа
     */
    fun analyzeIntonation(
        recordingFile: File,
        phrase: String
    ): Flow<Result<PracticeResult>> = flow {
        try {
            val requestFile = recordingFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", recordingFile.name, requestFile)
            val phraseBody = phrase.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = ApiClient.apiService.analyzeIntonation(body, phraseBody)

            if (response.isSuccessful) {
                val analyzeResponse = response.body()
                if (analyzeResponse != null) {
                    // Преобразуем com.mikleaka.russianintonation.data.models.AnalyzeResponse в PracticeResult
                    val scoreFloat = (floor(analyzeResponse.score * 100) / 100).toFloat()
                    val practiceResult = PracticeResult(
                        id = "res_${UUID.randomUUID()}",
                        userId = "local_user", // Заглушка, т.к. сервер не возвращает
                        levelId = "level_1", // Заглушка
                        constructionId = "ik_1", // Заглушка
                        recordingUrl = "", // Заглушка
                        score = scoreFloat,
                        intonationPoints = emptyList(), // Больше не используется
                        referencePoints = emptyList(), // Больше не используется
                        timestamp = System.currentTimeMillis(),
                        feedback = "Ваша оценка: ${"%.2f".format(scoreFloat)}", // Простое сообщение
                        graphUrl = analyzeResponse.graphUrl
                    )
                    emit(Result.success(practiceResult))
                } else {
                    emit(Result.failure(Exception("Empty response body")))
                }
            } else {
                val code = response.code()
                val errorMsg = response.errorBody()?.string() ?: "Ошибка анализа интонации"
                emit(Result.failure(Exception("HTTP_$code:$errorMsg")))
            }
        } catch (e: Exception) {
            Log.e("AudioRepository", "Ошибка при анализе интонации", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Создает новый экземпляр MediaPlayer
     */
    private fun createNewMediaPlayer(): MediaPlayer {
        return MediaPlayer().apply {
            setOnErrorListener { _, what, extra ->
                Log.e("AudioRepository", "MediaPlayer ошибка: what=$what, extra=$extra")
                this@AudioRepository.isPlaying = false
                this@AudioRepository.mediaPlayer = null
                true
            }
        }
    }
}