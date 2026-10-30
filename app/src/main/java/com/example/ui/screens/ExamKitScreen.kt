package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.UnityBannerAd
import com.example.ui.theme.AccentBronze
import com.example.ui.theme.AccentBronzeContainer
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamBorder
import com.example.ui.theme.CreamBorderSubtle
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.CreamSurfaceSubtle
import com.example.ui.theme.CreamSurfaceVariant
import com.example.ui.theme.PrimaryEspresso
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FileUtils
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamKitScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val context = LocalContext.current
    val activePreset by viewModel.activePreset.collectAsState()
    val state by viewModel.examKitState.collectAsState()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bmp = FileUtils.loadBitmapFromUri(context, uri)
            if (bmp != null) viewModel.setExamKitPhoto(bmp)
        }
    }

    val signPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bmp = FileUtils.loadBitmapFromUri(context, uri)
            if (bmp != null) viewModel.setExamKitSignature(bmp)
        }
    }

    val thumbPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bmp = FileUtils.loadBitmapFromUri(context, uri)
            if (bmp != null) viewModel.setExamKitThumb(bmp)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "3-in-1 Full Exam Kit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Text(
                            "Target: ${activePreset.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("kit_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamSurface),
                modifier = Modifier.border(1.dp, CreamBorderSubtle)
            )
        },
        bottomBar = {
            UnityBannerAd(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("kit_bottom_banner")
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Instructions banner in warm cream
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CreamSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentBronzeContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AccentBronze,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Prepare candidate Photo, Signature, and Thumb Impression in a single synchronized batch. All 3 files are verified and saved into device Pictures.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Slot 1: Photo
            ProfessionalKitSlotCard(
                title = "1. Passport Photograph",
                specs = "${activePreset.photo.minKb}–${activePreset.photo.maxKb} KB (${activePreset.photo.widthPx}×${activePreset.photo.heightPx} px)",
                icon = Icons.Default.PhotoCamera,
                hasImage = state.photoBitmap != null,
                bitmap = state.photoBitmap,
                processedResult = state.photoResult,
                onSelectClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                testTag = "slot_photo"
            )

            // Slot 2: Signature
            ProfessionalKitSlotCard(
                title = "2. Candidate Signature",
                specs = "${activePreset.signature.minKb}–${activePreset.signature.maxKb} KB (${activePreset.signature.widthPx}×${activePreset.signature.heightPx} px)",
                icon = Icons.Default.Draw,
                hasImage = state.signatureBitmap != null,
                bitmap = state.signatureBitmap,
                processedResult = state.signatureResult,
                onSelectClick = {
                    signPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                testTag = "slot_signature"
            )

            // Slot 3: Thumb Impression
            ProfessionalKitSlotCard(
                title = "3. Thumb Impression (LTI)",
                specs = "${activePreset.thumb.minKb}–${activePreset.thumb.maxKb} KB (${activePreset.thumb.widthPx}×${activePreset.thumb.heightPx} px)",
                icon = Icons.Default.Fingerprint,
                hasImage = state.thumbBitmap != null,
                bitmap = state.thumbBitmap,
                processedResult = state.thumbResult,
                onSelectClick = {
                    thumbPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                testTag = "slot_thumb"
            )

            // Sample loader
            OutlinedButton(
                onClick = { viewModel.loadAllSampleExamKit() },
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CreamSurface,
                    contentColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("load_sample_kit_button")
            ) {
                Text("Autofill with 3 Sample Documents", fontWeight = FontWeight.Medium, fontSize = 13.sp)
            }

            // Primary Batch Process Button
            Button(
                onClick = {
                    if (context is Activity) {
                        viewModel.processFullExamKit(context)
                    }
                },
                enabled = state.photoBitmap != null && state.signatureBitmap != null && state.thumbBitmap != null && !state.isProcessing,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryEspresso,
                    contentColor = Color(0xFFFAF7F2)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("process_full_kit_button")
            ) {
                if (state.isProcessing) {
                    CircularProgressIndicator(color = CreamBackground, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Processing & Compressing Suite...", fontSize = 14.sp)
                } else {
                    Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Process & Save All 3 for ${activePreset.shortCode}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            if (state.statusMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreenContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.statusMessage!!,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun ProfessionalKitSlotCard(
    title: String,
    specs: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    hasImage: Boolean,
    bitmap: android.graphics.Bitmap?,
    processedResult: com.example.engine.ProcessedImageResult?,
    onSelectClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = CreamSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1EBE1))
                    .border(1.dp, CreamBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = specs,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )

                if (processedResult != null) {
                    Text(
                        text = "✓ Output: ${processedResult.sizeKb} KB (Compliant)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onSelectClick,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasImage) CreamSurfaceSubtle else PrimaryEspresso,
                    contentColor = if (hasImage) TextPrimary else Color(0xFFFAF7F2)
                ),
                border = if (hasImage) androidx.compose.foundation.BorderStroke(1.dp, CreamBorder) else null
            ) {
                Text(if (hasImage) "Change" else "Select", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
