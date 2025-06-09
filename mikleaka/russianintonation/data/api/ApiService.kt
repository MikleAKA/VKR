package com.mikleaka.russianintonation.data.api

import com.mikleaka.russianintonation.data.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part

/**
 * Интерфейс для работы с API сервера авторизации
 */
interface ApiService {
    
    /**
     * Регистрация нового пользователя
     */
    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    /**
     * Подтверждение регистрации
     */
    @POST("/verify")
    suspend fun verify(@Body request: VerificationRequest): Response<VerificationResponse>
    
    /**
     * Авторизация пользователя
     */
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    /**
     * Получение данных пользователя
     */
    @GET("/user/profile")
    suspend fun getUserProfile(): Response<UserDto>
    
    /**
     * Смена пароля пользователя
     */
    @POST("/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ChangePasswordResponse>
    
    /**
     * Анализ интонации: отправка аудиофайла и текста фразы
     */
    @Multipart
    @POST("/analyze-intonation")
    suspend fun analyzeIntonation(
        @Part file: MultipartBody.Part,
        @Part("phrase") phrase: RequestBody
    ): Response<AnalyzeResponse>
} 