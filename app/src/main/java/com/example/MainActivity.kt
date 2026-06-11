package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DownloadEntity
import com.example.data.DownloadRepository
import com.example.engine.DownloadEngine
import com.example.service.DownloadService
import kotlinx.coroutines.launch
import java.util.UUID

val BgColor = Color(0xFFFDFBFF)
val TextPrimary = Color(0xFF1B1B1F)
val PrimaryBlue = Color(0xFF3F51B5)
val CardBg = Color(0xFFEAEBF1)
val TextSecondary = Color(0xFF44474E)
val ItemBorder = Color(0xFFC4C6CF)
val MutedText = Color(0xFF74777F)
val DeleteRed = Color(0xFFBA1A1A)
val ActionBtnBg = Color(0xFFF1F0F4)
val ProgressTrack = Color(0xFFE1E2EC)
val NavSelectedBg = Color(0xFFDBE1FF)
val NavSelectedText = Color(0xFF00174B)

class MainActivity : ComponentActivity() {
    private lateinit var repo: DownloadRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repo = DownloadRepository((application as App).db.downloadDao())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        setContent {
            val downloads by repo.observeDownloads().collectAsStateWithLifecycle(initialValue = emptyList())
            var urlInput by remember { mutableStateOf("") }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            fun startDownload() {
                val url = urlInput.trim()
                if (url.isNotEmpty()) {
                    val id = UUID.randomUUID().toString()
                    val fileName = url.substringAfterLast('/').ifEmpty { "file" }
                    val intent = Intent(context, DownloadService::class.java).apply {
                        putExtra("url", url)
                        putExtra("id", id)
                        putExtra("fileName", fileName)
                    }
                    context.startService(intent)
                    urlInput = ""
                }
            }

            fun onAction(entity: DownloadEntity, action: String) {
                when (action) {
                    "pause" -> DownloadEngine.pause(entity.id)
                    "resume" -> DownloadEngine.resume(entity.id)
                    "cancel" -> {
                        DownloadEngine.cancel(entity.id)
                        scope.launch { repo.delete(entity.id) }
                    }
                }
            }

            MaterialTheme(
                colorScheme = lightColorScheme(
                    background = BgColor,
                    surface = BgColor,
                    primary = PrimaryBlue,
                    onPrimary = Color.White,
                    onBackground = TextPrimary,
                    onSurface = TextPrimary
                )
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = BgColor,
                    bottomBar = { BottomNav() }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TopBar()
                        Spacer(modifier = Modifier.height(24.dp))
                        AddDownloadCard(
                            urlInput = urlInput,
                            onUrlChange = { urlInput = it },
                            onStart = { startDownload() }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        DownloadsListTitle(count = downloads.filter { it.progress < 100 }.size)
                        Spacer(modifier = Modifier.height(12.dp))
                        DownloadsList(downloads, ::onAction)
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.tahmil_app_icon_1781207239778),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Text(
                text = "تـحـمـيـل",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
        }
    }
}

@Composable
fun AddDownloadCard(urlInput: String, onUrlChange: (String) -> Unit, onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(CardBg)
            .padding(20.dp)
    ) {
        Text(
            text = "إضافة رابط جديد",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        
        // Custom Input Field matching Tailwind
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            val isHintVisible = urlInput.isEmpty()
            if (isHintVisible) {
                Text(
                    text = "https://example.com/file.zip",
                    color = MutedText,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = urlInput,
                onValueChange = onUrlChange,
                textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary),
                singleLine = true,
                cursorBrush = SolidColor(PrimaryBlue),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("بدء التحميل", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

@Composable
fun DownloadsListTitle(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "التحميلات الجارية",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(NavSelectedBg)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count نشط",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NavSelectedText
            )
        }
    }
}

@Composable
fun DownloadsList(downloads: List<DownloadEntity>, onAction: (DownloadEntity, String) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(downloads, key = { it.id }) { item ->
            DownloadItemCard(item = item, onAction = onAction)
        }
    }
}

@Composable
fun DownloadItemCard(item: DownloadEntity, onAction: (DownloadEntity, String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, ItemBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = item.fileName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val downloadedMb = item.downloaded / (1024 * 1024)
                val totalMb = if (item.total > 0) item.total / (1024 * 1024) else 0
                val sizeText = if (item.total > 0) "$downloadedMb MB من $totalMb MB" else "$downloadedMb MB"
                Text(
                    text = "$sizeText • ${item.status.name}",
                    fontSize = 12.sp,
                    color = MutedText
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (item.progress < 100 && item.status.name != "FAILED") {
                    val isPaused = item.status.name == "PAUSED" // Rough estimate, since engine handles it
                    IconButton(
                        onClick = { onAction(item, if (isPaused) "resume" else "pause") },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ActionBtnBg)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause/Resume",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(
                    onClick = { onAction(item, "cancel") },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ActionBtnBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = DeleteRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        val progressFraction by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (item.total > 0) (item.downloaded.toFloat() / item.total.toFloat()).coerceIn(0f, 1f) else 0f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 200, easing = androidx.compose.animation.core.LinearEasing)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(ProgressTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progressFraction)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
                    .shadow(elevation = 8.dp, ambientColor = PrimaryBlue, spotColor = PrimaryBlue)
            )
        }
    }
}

@Composable
fun BottomNav() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem("الرئيسية", Icons.Default.Home, isSelected = true)
        NavItem("التحميلات", Icons.Default.List, isSelected = false)
        NavItem("الإعدادات", Icons.Default.Settings, isSelected = false)
    }
}

@Composable
fun NavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isSelected) NavSelectedBg else Color.Transparent)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) NavSelectedText else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) NavSelectedText else TextSecondary
        )
    }
}

