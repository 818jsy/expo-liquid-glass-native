package expo.modules.liquidglassnative

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.facebook.react.bridge.ReactContext

@Composable
internal fun BackdropDemoScaffold(
    backdrop: LayerBackdrop,
    backgroundImageUri: String?,
    useRealtimeCapture: Boolean = false,
    renderBackgroundContent: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    var painter: Painter? by remember { mutableStateOf(null) }
    
    LaunchedEffect(backgroundImageUri, useRealtimeCapture) {
        val imageUri = backgroundImageUri
        
        android.util.Log.d("BackdropDemoScaffold", "Loading image: $imageUri, useRealtimeCapture: $useRealtimeCapture")
        
        if (imageUri != null && !useRealtimeCapture) {
            try {
                val imageBitmap = withContext(Dispatchers.IO) {
                    when {
                        imageUri.startsWith("asset://") -> {
                            // React Native asset 경로 처리
                            // Expo에서는 assets가 번들에 포함되어 있으므로 여러 경로 시도
                            val assetPath = imageUri.replace("asset://", "").replace("assets/", "")
                            android.util.Log.d("BackdropDemoScaffold", "Loading asset: $assetPath")
                            
                            // 여러 가능한 경로 시도
                            val pathsToTry = listOf(
                                assetPath,
                                "assets/$assetPath",
                                "src/main/assets/$assetPath",
                                "src/main/assets/assets/$assetPath",
                                "bundle-assets/$assetPath",
                                "bundle-assets/assets/$assetPath"
                            )
                            
                            // find를 사용하여 첫 번째 성공한 경로의 bitmap 반환
                            pathsToTry.firstNotNullOfOrNull { path ->
                                try {
                                    context.assets.open(path).use { stream ->
                                        val bitmap = BitmapFactory.decodeStream(stream)
                                        if (bitmap != null) {
                                            android.util.Log.d("BackdropDemoScaffold", "Successfully loaded asset from: $path")
                                        }
                                        bitmap
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.d("BackdropDemoScaffold", "Failed to load from path: $path - ${e.message}")
                                    null
                                }
                            } ?: run {
                                android.util.Log.e("BackdropDemoScaffold", "Failed to load asset from all paths: $assetPath")
                                null
                            }
                        }
                        imageUri.startsWith("file://") -> {
                            val uri = Uri.parse(imageUri)
                            val path = uri.path
                            if (path != null) {
                                val file = java.io.File(path)
                                if (file.exists()) {
                                    BitmapFactory.decodeFile(file.absolutePath)
                                } else {
                                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                        BitmapFactory.decodeStream(inputStream)
                                    }
                                }
                            } else {
                                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                    BitmapFactory.decodeStream(inputStream)
                                }
                            }
                        }
                        imageUri.startsWith("http://") || imageUri.startsWith("https://") -> {
                            try {
                                val url = java.net.URL(imageUri)
                                url.openConnection().getInputStream().use { stream ->
                                    BitmapFactory.decodeStream(stream)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("BackdropDemoScaffold", "Failed to load HTTP image: $imageUri", e)
                                null
                            }
                        }
                        imageUri.startsWith("content://") -> {
                            val uri = Uri.parse(imageUri)
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                BitmapFactory.decodeStream(inputStream)
                            }
                        }
                        else -> {
                            val uri = Uri.parse(imageUri)
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                BitmapFactory.decodeStream(inputStream)
                            }
                        }
                    }
                }
                
                if (imageBitmap != null) {
                    android.util.Log.d("BackdropDemoScaffold", "Successfully loaded image: $imageUri")
                    painter = BitmapPainter(imageBitmap.asImageBitmap())
                } else {
                    android.util.Log.w("BackdropDemoScaffold", "Failed to decode image bitmap: $imageUri")
                    painter = null
                }
            } catch (e: Exception) {
                android.util.Log.e("BackdropDemoScaffold", "Failed to load image: $imageUri", e)
                painter = null
            }
        } else {
            painter = null
        }
    }
    
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 실시간 캡처를 사용하는 경우
        if (useRealtimeCapture) {
            android.util.Log.d("BackdropDemoScaffold", "Using realtime capture, renderBackgroundContent: $renderBackgroundContent")
            if (renderBackgroundContent) {
                // 실시간 캡처 + 배경 콘텐츠 렌더링
                painter?.let { currentPainter ->
                    Image(
                        currentPainter,
                        null,
                        Modifier
                            .fillMaxSize()
                            .layerBackdrop(backdrop),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    BasicText(
                        "Liquid Glass Buttons",
                        Modifier.padding(bottom = 8.dp),
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    BasicText(
                        "These buttons demonstrate the liquid glass effect with real-time backdrop capture. Scroll to see the effect on different backgrounds.",
                        Modifier.padding(bottom = 30.dp),
                        style = TextStyle(
                            color = Color(0xFF333333),
                            fontSize = 16.sp
                        )
                    )
                    
                    repeat(10) { index ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(bottom = 20.dp)
                                .background(
                                    Color(0x4DFFFFFF),
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                BasicText(
                                    "Content Item ${index + 1}",
                                    style = TextStyle(
                                        color = Color.Black,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(Modifier.height(8.dp))
                                BasicText(
                                    "This is scrollable content that will be captured by the liquid glass effect.",
                                    style = TextStyle(
                                        color = Color(0xFF333333),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }
                }
                } else {
                // 실시간 캡처만 (배경 이미지 없이 현재 화면 캡처)
                android.util.Log.d("BackdropDemoScaffold", "Using realtime capture only (no background image)")
                android.util.Log.d("BackdropDemoScaffold", "Applying layerBackdrop modifier to Box")
                Box(
                    Modifier
                        .fillMaxSize()
                        .layerBackdrop(backdrop)
                ) {
                    android.util.Log.d("BackdropDemoScaffold", "Box with layerBackdrop rendered")
                    // 디버깅: 캡처가 안 되면 이 텍스트가 보임
                    // BasicText(
                    //     "Backdrop Capture",
                    //     Modifier.align(Alignment.Center),
                    //     style = TextStyle(color = Color.Red, fontSize = 12.sp)
                    // )
                }
            }
        } else if (backgroundImageUri != null) {
            // 이미지 배경 표시 (실시간 캡처 사용 안 함)
            painter?.let { currentPainter ->
                Image(
                    currentPainter,
                    null,
                    Modifier
                        .layerBackdrop(backdrop)
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        content()
    }
}

