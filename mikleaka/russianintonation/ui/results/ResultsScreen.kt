package com.mikleaka.russianintonation.ui.results

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikleaka.russianintonation.data.models.IntonationPoint
import com.mikleaka.russianintonation.ui.components.LoadingDialog
import com.mikleaka.russianintonation.ui.components.RiButton
import com.mikleaka.russianintonation.ui.components.RiTopAppBarWithProfile
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.ui.platform.LocalContext
import android.app.Application

/**
 * Экран результатов практики
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val viewModel: ResultsViewModel = viewModel(factory = ResultsViewModelFactory())
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            RiTopAppBarWithProfile(
                title = "Результаты",
                onBackClick = onNavigateBack,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingDialog()
        } else {
            uiState.practiceResult?.let { result ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // График с сервера
                    result.graphUrl?.let { url ->
                        Text(
                            text = result.feedback,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getScoreDescription(result.score),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            AsyncImage(
                                model = "http://10.0.2.2:8080$url",
                                contentDescription = "График интонации с сервера",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Кнопки действий
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RiButton(
                            text = "Повторить",
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f)
                        )
                        
                        RiButton(
                            text = "На главную",
                            onClick = onNavigateToHome,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Воспроизведение записи
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.playRecordedAudio() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPlayingRecording)
                                        Icons.Default.Stop
                                    else
                                        Icons.Default.PlayArrow,
                                    contentDescription = if (uiState.isPlayingRecording)
                                        "Остановить воспроизведение"
                                    else
                                        "Прослушать запись",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            Text(
                                text = if (uiState.isPlayingRecording)
                                    "Воспроизведение записи..."
                                else
                                    "Прослушать вашу запись",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Возвращает цвет в зависимости от оценки
 */
@Composable
fun getScoreColor(score: Float): Color {
    return when {
        score >= 80 -> Color(0xFF4CAF50) // Зеленый
        score >= 65 -> Color(0xFF8BC34A) // Светло-зеленый
        score >= 50 -> Color(0xFFFFC107) // Желтый
        else -> Color(0xFFFF5722) // Оранжевый
    }
}

/**
 * Возвращает текстовое описание оценки
 */
fun getScoreDescription(score: Float): String {
    return when {
        score >= 80 -> "Отлично! Ваша интонация очень близка к эталонной."
        score >= 65 -> "Хорошо! Есть небольшие отклонения в интонации, но в целом правильно."
        score >= 50 -> "Неплохо, но требуется дополнительная практика."
        else -> "Нужна дополнительная практика. Постарайтесь четче следовать образцу интонации."
    }
}
