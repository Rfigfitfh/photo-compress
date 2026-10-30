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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.UnityBannerAd
import com.example.ui.theme.AccentBronze
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamBorder
import com.example.ui.theme.CreamBorderSubtle
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.CreamSurfaceSubtle
import com.example.ui.theme.CreamSurfaceVariant
import com.example.ui.theme.PrimaryEspresso
import com.example.ui.theme.SecondaryTaupe
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
fun PhotoStudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val context = LocalContext.current
    val activePreset by viewModel.activePreset.collectAsState()
    val state by viewModel.photoState.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = FileUtils.loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                viewModel.setPhotoOriginal(bitmap)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Photo Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Text(
                            "${activePreset.name} (${activePreset.photo.widthPx}×${activePreset.photo.heightPx} px)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("photo_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamSurface
                ),
                modifier = Modifier.border(0.dp, Color.Transparent).border(1.dp, CreamBorderSubtle)
            )
        },
        bottomBar = {
            UnityBannerAd(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("photo_bottom_banner")
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
            // Passport Photo Preview Viewport
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("photo_preview_container"),
                shape = RoundedCornerShape(12.dp),
                color = CreamSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val displayBitmap = state.processedResult?.bitmap ?: state.originalBitmap

                    // Viewport Frame
                    Box(
                        modifier = Modifier
                            .size(width = 200.dp, height = 240.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0EBE1))
                            .border(1.5.dp, CreamBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (displayBitmap != null) {
                            Image(
                                bitmap = displayBitmap.asImageBitmap(),
                                contentDescription = "Exam Photo Preview",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No Photo Loaded",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (state.isProcessing) {
                            Surface(
                                color = PrimaryEspresso.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        color = CreamBackground,
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Precision Compliance Pill
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

                    // Selection Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
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
                                .testTag("pick_photo_button")
                        ) {
                            Text("Import Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.loadSamplePhoto() },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CreamSurfaceSubtle,
                                contentColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("load_sample_photo_button")
                        ) {
                            Text("Use Sample", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // TOOL 1: Exact KB Compressor
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = PrimaryEspresso,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Exact File Size Compressor",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CreamSurfaceSubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorderSubtle)
                        ) {
                            Text(
                                text = "Target: ${state.exactTargetKb} KB",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryEspresso,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Official limit for ${activePreset.shortCode}: ${state.targetMinKb} KB to ${state.targetMaxKb} KB",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    )

                    Slider(
                        value = state.exactTargetKb.toFloat(),
                        onValueChange = { viewModel.updatePhotoTargetKb(it.toInt()) },
                        valueRange = state.targetMinKb.toFloat().coerceAtLeast(10f)..state.targetMaxKb.toFloat().coerceAtLeast(40f),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryEspresso,
                            activeTrackColor = PrimaryEspresso,
                            inactiveTrackColor = CreamBorder
                        ),
                        modifier = Modifier.testTag("photo_kb_slider")
                    )
                }
            }

            // TOOL 2: Offline Background Changer
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                tint = PrimaryEspresso,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Offline Background Changer",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Whitens yellowish or shadowy wall backgrounds",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = state.isBgChangeActive,
                            onCheckedChange = { viewModel.setPhotoBackgroundColor(state.selectedBgColor, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryEspresso,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = CreamBorder
                            ),
                            modifier = Modifier.testTag("bg_change_switch")
                        )
                    }

                    if (state.isBgChangeActive) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Standard Background Tone:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pure White
                            ProfessionalColorChip(
                                label = "Pure White",
                                color = Color.White,
                                isSelected = state.selectedBgColor == Color.White.toArgb(),
                                onClick = { viewModel.setPhotoBackgroundColor(Color.White.toArgb(), true) }
                            )

                            // Light Sky Blue
                            ProfessionalColorChip(
                                label = "Sky Blue",
                                color = Color(0xFFD0E8FF),
                                isSelected = state.selectedBgColor == Color(0xFFD0E8FF).toArgb(),
                                onClick = { viewModel.setPhotoBackgroundColor(Color(0xFFD0E8FF).toArgb(), true) }
                            )

                            // Warm Off-White Cream
                            ProfessionalColorChip(
                                label = "Off-White",
                                color = Color(0xFFF8F5EE),
                                isSelected = state.selectedBgColor == Color(0xFFF8F5EE).toArgb(),
                                onClick = { viewModel.setPhotoBackgroundColor(Color(0xFFF8F5EE).toArgb(), true) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Matte Sensitivity: ${(state.bgTolerance * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )

                        Slider(
                            value = state.bgTolerance,
                            onValueChange = { viewModel.updatePhotoBgTolerance(it) },
                            valueRange = 0.15f..0.45f,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryEspresso,
                                activeTrackColor = PrimaryEspresso,
                                inactiveTrackColor = CreamBorder
                            )
                        )
                    }
                }
            }

            // TOOL 3: Candidate Name & DOP Stamp
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = null,
                                tint = PrimaryEspresso,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Name & Date of Photo (DOP) Stamp",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Mandatory for SSC CGL, CHSL, MTS & UPSC",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (activePreset.photo.requiresDopStamp) Color(0xFF8A2727) else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = state.enableDopStamp,
                            onCheckedChange = { viewModel.togglePhotoDopStamp(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryEspresso,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = CreamBorder
                            ),
                            modifier = Modifier.testTag("dop_stamp_switch")
                        )
                    }

                    if (state.enableDopStamp) {
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.candidateName,
                            onValueChange = { viewModel.updatePhotoCandidateName(it) },
                            label = { Text("Candidate Full Name (in CAPITAL)", fontSize = 12.sp) },
                            placeholder = { Text("e.g. AMIT SHARMA") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEspresso,
                                unfocusedBorderColor = CreamBorder,
                                focusedContainerColor = CreamSurface,
                                unfocusedContainerColor = CreamSurfaceSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("candidate_name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.photoDate,
                            onValueChange = { viewModel.updatePhotoDopDate(it) },
                            label = { Text("Date of Photo Taken (DD/MM/YYYY)", fontSize = 12.sp) },
                            placeholder = { Text("e.g. 24/10/2026") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEspresso,
                                unfocusedBorderColor = CreamBorder,
                                focusedContainerColor = CreamSurface,
                                unfocusedContainerColor = CreamSurfaceSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dop_date_input"),
                            singleLine = true
                        )
                    }
                }
            }

            // TOOL 4: Clarity & Unsharp Mask Sharpening
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CreamSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = PrimaryEspresso,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Photo Sharpen & Clarity Filter",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Removes softness from dim webcam / mobile cameras",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = state.enableSharpen,
                        onCheckedChange = { viewModel.togglePhotoSharpen(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryEspresso,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CreamBorder
                        ),
                        modifier = Modifier.testTag("sharpen_switch")
                    )
                }
            }

            // Export Action Button
            Button(
                onClick = {
                    if (context is Activity) {
                        viewModel.saveProcessedPhoto(context)
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
                    .testTag("save_photo_button")
            ) {
                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Compliant Photo to Pictures",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (state.savedUri != null) {
                OutlinedButton(
                    onClick = {
                        FileUtils.shareImage(context, state.savedUri!!, "Exam Compliant Photo")
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
                        .testTag("share_photo_button")
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

@Composable
fun ProfessionalColorChip(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) CreamSurfaceVariant else CreamSurface,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) PrimaryEspresso else CreamBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}
