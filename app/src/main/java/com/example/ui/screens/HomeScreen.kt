package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.UnityBannerAd
import com.example.model.ExamPreset
import com.example.model.ExamPresetsRepository
import com.example.ui.components.StoragePermissionCard
import com.example.ui.theme.AccentBronze
import com.example.ui.theme.AccentBronzeContainer
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamBorder
import com.example.ui.theme.CreamBorderSubtle
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.CreamSurfaceSubtle
import com.example.ui.theme.CreamSurfaceVariant
import com.example.ui.theme.PrimaryEspresso
import com.example.ui.theme.SecondaryTaupe
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activePreset by viewModel.activePreset.collectAsState()
    val savedDocs by viewModel.savedDocuments.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Executive Header Card
            item {
                ProfessionalHeroCard(activePreset = activePreset)
            }

            // Storage Permission Prompt
            item {
                StoragePermissionCard(onPermissionGranted = {})
            }

            // Exam Presets Horizontal Rail
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TARGET EXAMINATION",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentBronzeContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
                        ) {
                            Text(
                                text = "Active: ${activePreset.shortCode}",
                                color = AccentBronze,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ExamPresetsRepository.presets) { preset ->
                            val isSelected = preset.id == activePreset.id
                            ExecutivePresetChip(
                                preset = preset,
                                isSelected = isSelected,
                                onClick = { viewModel.selectPreset(preset) }
                            )
                        }
                    }
                }
            }

            // Official Exam Requirement Matrix
            item {
                OfficialExamMatrixCard(preset = activePreset)
            }

            // Section Label
            item {
                Text(
                    text = "PRECISION TOOLS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 1. Photo Studio
            item {
                ProfessionalToolCard(
                    title = "Photo Studio",
                    description = "Exact KB Compression • Dimension Resizer • White/Blue Background • Name & Date (DOP) Stamp",
                    badge = "${activePreset.photo.minKb}–${activePreset.photo.maxKb} KB",
                    icon = Icons.Default.PhotoCamera,
                    onClick = { viewModel.navigateTo(AppScreen.PHOTO_STUDIO) },
                    testTag = "tool_photo_studio"
                )
            }

            // 2. Signature Studio
            item {
                ProfessionalToolCard(
                    title = "Signature Studio",
                    description = "Paper Shadow Cleaner • Dark Ink Threshold • Digital Touch Drawing Pad",
                    badge = "${activePreset.signature.minKb}–${activePreset.signature.maxKb} KB",
                    icon = Icons.Default.Draw,
                    onClick = { viewModel.navigateTo(AppScreen.SIGNATURE_STUDIO) },
                    testTag = "tool_signature_studio"
                )
            }

            // 3. Thumb Impression Studio
            item {
                ProfessionalToolCard(
                    title = "Thumb Impression Studio",
                    description = "Biometric Friction Ridge Enhancer • 3cm × 3cm Crop • Paper Whitener",
                    badge = "${activePreset.thumb.minKb}–${activePreset.thumb.maxKb} KB",
                    icon = Icons.Default.Fingerprint,
                    onClick = { viewModel.navigateTo(AppScreen.THUMB_STUDIO) },
                    testTag = "tool_thumb_studio"
                )
            }

            // 4. Full Exam Kit (3-in-1)
            item {
                ProfessionalToolCard(
                    title = "3-in-1 Full Exam Kit",
                    description = "Batch process Photo, Signature, and Thumb together with official checklist verification",
                    badge = "Batch Suite",
                    icon = Icons.Default.AutoAwesome,
                    isHighlight = true,
                    onClick = { viewModel.navigateTo(AppScreen.EXAM_KIT) },
                    testTag = "tool_exam_kit"
                )
            }

            // Saved Documents Gallery Link
            item {
                Surface(
                    onClick = { viewModel.navigateTo(AppScreen.SAVED_HUB) },
                    shape = RoundedCornerShape(12.dp),
                    color = CreamSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tool_saved_hub")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CreamSurfaceSubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorderSubtle),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FolderSpecial,
                                    contentDescription = null,
                                    tint = PrimaryEspresso,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exported Document Archive",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${savedDocs.size} ready-to-upload files in device Pictures",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryEspresso
                        ) {
                            Text(
                                text = "${savedDocs.size}",
                                color = CreamBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Bottom Unity Ads Banner with Cream Styling
        UnityBannerAd(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("unity_bottom_banner")
        )
    }
}

@Composable
fun ProfessionalHeroCard(activePreset: ExamPreset) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = PrimaryEspresso,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38322B))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF332B25),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = AccentBronze,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Image Compress Cloud",
                        color = Color(0xFFFAF7F2),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF332B25),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF473C33))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "OFFLINE",
                            color = Color(0xFFEDE4D3),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Professional Document Standardizer for Indian Recruitment Portals",
                color = Color(0xFFF3ECE0),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Ensures zero-rejection compliance across UPSC, SSC, IBPS, RRB & NTA exams with byte-precision binary compression and background whitening.",
                color = Color(0xFFB0A495),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun ExecutivePresetChip(
    preset: ExamPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) PrimaryEspresso else CreamSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PrimaryEspresso else CreamBorder
        ),
        modifier = Modifier.testTag("preset_chip_${preset.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFFFAF7F2),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = preset.name,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFFFAF7F2) else TextPrimary
            )
        }
    }
}

@Composable
fun OfficialExamMatrixCard(preset: ExamPreset) {
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
                    text = "${preset.name} Official Specifications",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = preset.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CreamSurfaceSubtle, RoundedCornerShape(8.dp))
                    .border(1.dp, CreamBorderSubtle, RoundedCornerShape(8.dp))
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "PHOTO", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "${preset.photo.minKb}–${preset.photo.maxKb} KB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "${preset.photo.widthPx}×${preset.photo.heightPx} px", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                }

                Box(modifier = Modifier.width(1.dp).height(32.dp).background(CreamBorder))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "SIGNATURE", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "${preset.signature.minKb}–${preset.signature.maxKb} KB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "${preset.signature.widthPx}×${preset.signature.heightPx} px", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                }

                Box(modifier = Modifier.width(1.dp).height(32.dp).background(CreamBorder))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "THUMB (LTI)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "${preset.thumb.minKb}–${preset.thumb.maxKb} KB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "${preset.thumb.widthPx}×${preset.thumb.heightPx} px", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                }
            }

            if (preset.photo.requiresDopStamp) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFBECEC),
                        modifier = Modifier.size(6.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mandatory: Candidate Name and Date of Photo (DOP) printed at bottom.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8A2727),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ProfessionalToolCard(
    title: String,
    description: String,
    badge: String,
    icon: ImageVector,
    isHighlight: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isHighlight) CreamSurfaceVariant else CreamSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHighlight) AccentBronze.copy(alpha = 0.5f) else CreamBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isHighlight) PrimaryEspresso else CreamSurfaceSubtle,
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorderSubtle),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isHighlight) Color(0xFFFAF7F2) else PrimaryEspresso,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isHighlight) AccentBronzeContainer else CreamSurfaceSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorderSubtle)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighlight) AccentBronze else TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
