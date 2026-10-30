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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamBorder
import com.example.ui.theme.CreamBorderSubtle
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.CreamSurfaceSubtle
import com.example.ui.theme.PrimaryEspresso
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.util.FileUtils
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThumbImpressionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val context = LocalContext.current
    val activePreset by viewModel.activePreset.collectAsState()
    val state by viewModel.thumbState.collectAsState()

    val thumbPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = FileUtils.loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                viewModel.setThumbOriginal(bitmap)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Thumb Impression Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Text(
                            "${activePreset.name} (${activePreset.thumb.widthPx}×${activePreset.thumb.heightPx} px)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("thumb_back_button")
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
                    .testTag("thumb_bottom_banner")
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
            // Thumb Preview & Biometric Guide
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("thumb_preview_container"),
                shape = RoundedCornerShape(12.dp),
                color = CreamSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val displayBitmap = state.processedResult?.bitmap ?: state.originalBitmap

                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.5.dp, CreamBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (displayBitmap != null) {
                            Image(
                                bitmap = displayBitmap.asImageBitmap(),
                                contentDescription = "Thumb Impression Preview",
                                modifier = Modifier.fillMaxSize().padding(10.dp)
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(50.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No Impression Loaded",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Circular 3cm guide overlay
                        Box(
                            modifier = Modifier
                                .size(155.dp)
                                .clip(CircleShape)
                                .border(1.dp, PrimaryEspresso.copy(alpha = 0.2f), CircleShape)
                        )

                        if (state.isProcessing) {
                            Surface(
                                color = PrimaryEspresso.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        color = CreamBackground,
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Compliance Status Badge
                    if (state.processedResult != null) {
                        val res = state.processedResult!!
                        val isOk = res.sizeKb >= state.targetMinKb && res.sizeKb <= state.targetMaxKb

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOk) SuccessGreenContainer else WarningAmberContainer,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isOk) SuccessGreen.copy(alpha = 0.3f) else WarningAmber.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isOk) SuccessGreen else WarningAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${res.sizeKb} KB  •  ${res.width}×${res.height} px  •  ${if (isOk) "EXAM COMPLIANT" else "ADJUSTING"}",
                                    color = if (isOk) SuccessGreen else WarningAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.4.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pick & Sample Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                thumbPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryEspresso,
                                contentColor = Color(0xFFFAF7F2)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("pick_thumb_button")
                        ) {
                            Text("Import Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.loadSampleThumb() },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CreamSurfaceSubtle,
                                contentColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("load_sample_thumb_button")
                        ) {
                            Text("Use Sample", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // TOOL 1: Biometric Friction Ridge Sharpener
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CreamSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Biometric Ridge Sharpener",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Amplifies fingerprint loops, arches and whorls while clearing ink smudge halos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    )

                    Slider(
                        value = state.ridgeContrast,
                        onValueChange = { viewModel.updateThumbRidgeContrast(it) },
                        valueRange = 1.0f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryEspresso,
                            activeTrackColor = PrimaryEspresso,
                            inactiveTrackColor = CreamBorder
                        ),
                        modifier = Modifier.testTag("thumb_ridge_slider")
                    )
                }
            }

            // TOOL 2: Exact Target KB Compressor
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CreamSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Size: ${state.exactTargetKb} KB",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryEspresso
                        )
                        Text(
                            text = "Req: ${state.targetMinKb}–${state.targetMaxKb} KB",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }

                    Slider(
                        value = state.exactTargetKb.toFloat(),
                        onValueChange = { viewModel.updateThumbTargetKb(it.toInt(), state.targetMinKb, state.targetMaxKb) },
                        valueRange = state.targetMinKb.toFloat().coerceAtLeast(5f)..state.targetMaxKb.toFloat().coerceAtLeast(30f),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryEspresso,
                            activeTrackColor = PrimaryEspresso,
                            inactiveTrackColor = CreamBorder
                        ),
                        modifier = Modifier.testTag("thumb_kb_slider")
                    )
                }
            }

            // Save Thumb Impression Button
            Button(
                onClick = {
                    if (context is Activity) {
                        viewModel.saveProcessedThumb(context)
                    }
                },
                enabled = state.processedResult != null,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryEspresso,
                    contentColor = Color(0xFFFAF7F2)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_thumb_button")
            ) {
                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Compliant Thumb to Pictures",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (state.savedUri != null) {
                OutlinedButton(
                    onClick = {
                        FileUtils.shareImage(context, state.savedUri!!, "Exam Compliant Thumb")
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CreamSurface,
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("share_thumb_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share / Send to Upload Device")
                }
            }

            if (state.statusMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreenContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.statusMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
