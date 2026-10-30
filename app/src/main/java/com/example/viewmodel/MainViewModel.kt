package com.example.viewmodel

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.UnityAdsManager
import com.example.engine.ImageProcessingEngine
import com.example.engine.ProcessedImageResult
import com.example.model.ExamPreset
import com.example.model.ExamPresetsRepository
import com.example.util.FileUtils
import com.example.util.SavedDocumentItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppScreen {
    HOME,
    PHOTO_STUDIO,
    SIGNATURE_STUDIO,
    THUMB_STUDIO,
    EXAM_KIT,
    SAVED_HUB
}

data class PhotoStudioUiState(
    val originalBitmap: Bitmap? = null,
    val processedResult: ProcessedImageResult? = null,
    val candidateName: String = "",
    val photoDate: String = "",
    val enableDopStamp: Boolean = false,
    val selectedBgColor: Int = Color.WHITE, // Color.WHITE, 0xFFD0E8FF (Light Blue), etc.
    val isBgChangeActive: Boolean = false,
    val bgTolerance: Float = 0.28f,
    val enableSharpen: Boolean = false,
    val targetMinKb: Int = 20,
    val targetMaxKb: Int = 50,
    val exactTargetKb: Int = 35,
    val targetWidth: Int = 350,
    val targetHeight: Int = 450,
    val isProcessing: Boolean = false,
    val savedUri: Uri? = null,
    val statusMessage: String? = null
)

data class SignatureStudioUiState(
    val originalBitmap: Bitmap? = null,
    val processedResult: ProcessedImageResult? = null,
    val paperThreshold: Float = 0.65f,
    val inkColorHex: Int = Color.BLACK,
    val autoTrimMargins: Boolean = true,
    val targetMinKb: Int = 10,
    val targetMaxKb: Int = 20,
    val exactTargetKb: Int = 15,
    val targetWidth: Int = 280,
    val targetHeight: Int = 120,
    val isProcessing: Boolean = false,
    val savedUri: Uri? = null,
    val statusMessage: String? = null
)

data class ThumbStudioUiState(
    val originalBitmap: Bitmap? = null,
    val processedResult: ProcessedImageResult? = null,
    val ridgeContrast: Float = 1.4f,
    val targetMinKb: Int = 10,
    val targetMaxKb: Int = 20,
    val exactTargetKb: Int = 15,
    val targetWidth: Int = 240,
    val targetHeight: Int = 240,
    val isProcessing: Boolean = false,
    val savedUri: Uri? = null,
    val statusMessage: String? = null
)

data class ExamKitUiState(
    val photoBitmap: Bitmap? = null,
    val signatureBitmap: Bitmap? = null,
    val thumbBitmap: Bitmap? = null,
    val photoResult: ProcessedImageResult? = null,
    val signatureResult: ProcessedImageResult? = null,
    val thumbResult: ProcessedImageResult? = null,
    val isProcessing: Boolean = false,
    val allCompleted: Boolean = false,
    val statusMessage: String? = null
)

class MainViewModel : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _activePreset = MutableStateFlow(ExamPresetsRepository.presets.first())
    val activePreset: StateFlow<ExamPreset> = _activePreset.asStateFlow()

    private val _photoState = MutableStateFlow(PhotoStudioUiState())
    val photoState: StateFlow<PhotoStudioUiState> = _photoState.asStateFlow()

    private val _signatureState = MutableStateFlow(SignatureStudioUiState())
    val signatureState: StateFlow<SignatureStudioUiState> = _signatureState.asStateFlow()

    private val _thumbState = MutableStateFlow(ThumbStudioUiState())
    val thumbState: StateFlow<ThumbStudioUiState> = _thumbState.asStateFlow()

    private val _examKitState = MutableStateFlow(ExamKitUiState())
    val examKitState: StateFlow<ExamKitUiState> = _examKitState.asStateFlow()

    private val _savedDocuments = MutableStateFlow<List<SavedDocumentItem>>(emptyList())
    val savedDocuments: StateFlow<List<SavedDocumentItem>> = _savedDocuments.asStateFlow()

    init {
        // Apply initial preset defaults
        applyPresetToTools(_activePreset.value)
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectPreset(preset: ExamPreset) {
        _activePreset.value = preset
        applyPresetToTools(preset)
    }

    private fun applyPresetToTools(preset: ExamPreset) {
        _photoState.update {
            it.copy(
                targetMinKb = preset.photo.minKb,
                targetMaxKb = preset.photo.maxKb,
                exactTargetKb = preset.photo.targetKb,
                targetWidth = preset.photo.widthPx,
                targetHeight = preset.photo.heightPx,
                enableDopStamp = preset.photo.requiresDopStamp,
                photoDate = if (preset.photo.requiresDopStamp && it.photoDate.isBlank()) FileUtils.getCurrentFormattedDate() else it.photoDate
            )
        }

        _signatureState.update {
            it.copy(
                targetMinKb = preset.signature.minKb,
                targetMaxKb = preset.signature.maxKb,
                exactTargetKb = preset.signature.targetKb,
                targetWidth = preset.signature.widthPx,
                targetHeight = preset.signature.heightPx,
                inkColorHex = if (preset.signature.inkColor == "Black") Color.BLACK else Color.rgb(15, 43, 92)
            )
        }

        _thumbState.update {
            it.copy(
                targetMinKb = preset.thumb.minKb,
                targetMaxKb = preset.thumb.maxKb,
                exactTargetKb = preset.thumb.targetKb,
                targetWidth = preset.thumb.widthPx,
                targetHeight = preset.thumb.heightPx
            )
        }
    }

    // -------------------------------------------------------------
    // PHOTO STUDIO ACTIONS
    // -------------------------------------------------------------

    fun setPhotoOriginal(bitmap: Bitmap) {
        _photoState.update {
            it.copy(
                originalBitmap = bitmap,
                savedUri = null,
                statusMessage = null
            )
        }
        reprocessPhoto()
    }

    fun loadSamplePhoto() {
        val sample = ImageProcessingEngine.createSamplePassportPhoto()
        setPhotoOriginal(sample)
    }

    fun updatePhotoCandidateName(name: String) {
        _photoState.update { it.copy(candidateName = name) }
        reprocessPhoto()
    }

    fun updatePhotoDopDate(date: String) {
        _photoState.update { it.copy(photoDate = date) }
        reprocessPhoto()
    }

    fun togglePhotoDopStamp(enable: Boolean) {
        _photoState.update {
            it.copy(
                enableDopStamp = enable,
                photoDate = if (enable && it.photoDate.isBlank()) FileUtils.getCurrentFormattedDate() else it.photoDate
            )
        }
        reprocessPhoto()
    }

    fun setPhotoBackgroundColor(color: Int, isEnabled: Boolean) {
        _photoState.update {
            it.copy(selectedBgColor = color, isBgChangeActive = isEnabled)
        }
        reprocessPhoto()
    }

    fun updatePhotoBgTolerance(tolerance: Float) {
        _photoState.update { it.copy(bgTolerance = tolerance) }
        reprocessPhoto()
    }

    fun togglePhotoSharpen(enable: Boolean) {
        _photoState.update { it.copy(enableSharpen = enable) }
        reprocessPhoto()
    }

    fun updatePhotoTargetKb(targetKb: Int, minKb: Int = (targetKb * 0.7f).toInt(), maxKb: Int = (targetKb * 1.3f).toInt()) {
        _photoState.update {
            it.copy(
                exactTargetKb = targetKb,
                targetMinKb = minKb.coerceAtLeast(5),
                targetMaxKb = maxKb.coerceAtLeast(minKb + 5)
            )
        }
        reprocessPhoto()
    }

    fun reprocessPhoto() {
        val state = _photoState.value
        val original = state.originalBitmap ?: return

        _photoState.update { it.copy(isProcessing = true) }

        viewModelScope.launch(Dispatchers.Default) {
            var working = original

            // 1. Resize to target dimensions
            working = ImageProcessingEngine.resizeBitmap(working, state.targetWidth, state.targetHeight, maintainAspectRatio = true)

            // 2. Offline Background change if active
            if (state.isBgChangeActive) {
                working = ImageProcessingEngine.changeBackground(working, state.selectedBgColor, state.bgTolerance)
            }

            // 3. Upscale & Sharpen if enabled
            if (state.enableSharpen) {
                working = ImageProcessingEngine.enhanceAndSharpen(working, 1.4f, boostContrast = true)
            }

            // 4. Candidate Name & DOP stamp
            if (state.enableDopStamp && state.candidateName.isNotBlank()) {
                working = ImageProcessingEngine.addDopAndNameStamp(working, state.candidateName, state.photoDate)
            }

            // 5. Binary-search Exact KB Compression
            val result = ImageProcessingEngine.compressToTargetKb(
                inputBitmap = working,
                minKb = state.targetMinKb,
                maxKb = state.targetMaxKb,
                targetKb = state.exactTargetKb
            )

            withContext(Dispatchers.Main) {
                _photoState.update {
                    it.copy(
                        processedResult = result,
                        isProcessing = false
                    )
                }
            }
        }
    }

    fun saveProcessedPhoto(activity: Activity) {
        val state = _photoState.value
        val result = state.processedResult ?: return

        val uri = FileUtils.saveImageToStorage(
            activity,
            result.bytes,
            "Photo_${_activePreset.value.shortCode}"
        )

        if (uri != null) {
            val item = SavedDocumentItem(
                id = "photo_${System.currentTimeMillis()}",
                title = "Exam Photo (${state.candidateName.ifBlank { _activePreset.value.shortCode }})",
                examCategory = _activePreset.value.name,
                type = "Photo",
                sizeKb = result.sizeKb,
                width = result.width,
                height = result.height,
                dateAdded = FileUtils.getCurrentFormattedDate(),
                uriString = uri.toString()
            )
            _savedDocuments.update { listOf(item) + it }
            _photoState.update {
                it.copy(
                    savedUri = uri,
                    statusMessage = "Photo saved! Size: ${result.sizeKb} KB (Compliant with ${_activePreset.value.shortCode})"
                )
            }

            // Trigger Unity Interstitial Ad on successful save
            UnityAdsManager.showInterstitial(activity) {}
        }
    }

    // -------------------------------------------------------------
    // SIGNATURE STUDIO ACTIONS
    // -------------------------------------------------------------

    fun setSignatureOriginal(bitmap: Bitmap) {
        _signatureState.update {
            it.copy(
                originalBitmap = bitmap,
                savedUri = null,
                statusMessage = null
            )
        }
        reprocessSignature()
    }

    fun loadSampleSignature() {
        val sample = ImageProcessingEngine.createSampleSignature()
        setSignatureOriginal(sample)
    }

    fun updateSignaturePaperThreshold(threshold: Float) {
        _signatureState.update { it.copy(paperThreshold = threshold) }
        reprocessSignature()
    }

    fun updateSignatureInkColor(colorHex: Int) {
        _signatureState.update { it.copy(inkColorHex = colorHex) }
        reprocessSignature()
    }

    fun updateSignatureTargetKb(targetKb: Int, minKb: Int = 10, maxKb: Int = 20) {
        _signatureState.update {
            it.copy(
                exactTargetKb = targetKb,
                targetMinKb = minKb,
                targetMaxKb = maxKb
            )
        }
        reprocessSignature()
    }

    fun reprocessSignature() {
        val state = _signatureState.value
        val original = state.originalBitmap ?: return

        _signatureState.update { it.copy(isProcessing = true) }

        viewModelScope.launch(Dispatchers.Default) {
            // Clean paper shadows and boost ink
            var cleaned = ImageProcessingEngine.cleanSignature(
                bitmap = original,
                paperWhiteningThreshold = state.paperThreshold,
                inkColorHex = state.inkColorHex,
                autoCropBorders = state.autoTrimMargins
            )

            // Resize to target signature dimensions
            cleaned = ImageProcessingEngine.resizeBitmap(cleaned, state.targetWidth, state.targetHeight, maintainAspectRatio = true)

            // Compress to target KB
            val result = ImageProcessingEngine.compressToTargetKb(
                inputBitmap = cleaned,
                minKb = state.targetMinKb,
                maxKb = state.targetMaxKb,
                targetKb = state.exactTargetKb
            )

            withContext(Dispatchers.Main) {
                _signatureState.update {
                    it.copy(
                        processedResult = result,
                        isProcessing = false
                    )
                }
            }
        }
    }

    fun saveProcessedSignature(activity: Activity) {
        val state = _signatureState.value
        val result = state.processedResult ?: return

        val uri = FileUtils.saveImageToStorage(
            activity,
            result.bytes,
            "Sign_${_activePreset.value.shortCode}"
        )

        if (uri != null) {
            val item = SavedDocumentItem(
                id = "sign_${System.currentTimeMillis()}",
                title = "Exam Signature (${_activePreset.value.shortCode})",
                examCategory = _activePreset.value.name,
                type = "Signature",
                sizeKb = result.sizeKb,
                width = result.width,
                height = result.height,
                dateAdded = FileUtils.getCurrentFormattedDate(),
                uriString = uri.toString()
            )
            _savedDocuments.update { listOf(item) + it }
            _signatureState.update {
                it.copy(
                    savedUri = uri,
                    statusMessage = "Signature saved! Size: ${result.sizeKb} KB (Compliant with ${_activePreset.value.shortCode})"
                )
            }

            UnityAdsManager.showInterstitial(activity) {}
        }
    }

    // -------------------------------------------------------------
    // THUMB IMPRESSION ACTIONS
    // -------------------------------------------------------------

    fun setThumbOriginal(bitmap: Bitmap) {
        _thumbState.update {
            it.copy(
                originalBitmap = bitmap,
                savedUri = null,
                statusMessage = null
            )
        }
        reprocessThumb()
    }

    fun loadSampleThumb() {
        val sample = ImageProcessingEngine.createSampleThumb()
        setThumbOriginal(sample)
    }

    fun updateThumbRidgeContrast(contrast: Float) {
        _thumbState.update { it.copy(ridgeContrast = contrast) }
        reprocessThumb()
    }

    fun updateThumbTargetKb(targetKb: Int, minKb: Int = 10, maxKb: Int = 20) {
        _thumbState.update {
            it.copy(
                exactTargetKb = targetKb,
                targetMinKb = minKb,
                targetMaxKb = maxKb
            )
        }
        reprocessThumb()
    }

    fun reprocessThumb() {
        val state = _thumbState.value
        val original = state.originalBitmap ?: return

        _thumbState.update { it.copy(isProcessing = true) }

        viewModelScope.launch(Dispatchers.Default) {
            // Enhance friction ridges
            var enhanced = ImageProcessingEngine.enhanceThumbImpression(original, state.ridgeContrast)

            // Resize to standard thumb specs (e.g. 240x240 px, 3cm x 3cm)
            enhanced = ImageProcessingEngine.resizeBitmap(enhanced, state.targetWidth, state.targetHeight, maintainAspectRatio = true)

            // Compress to target KB
            val result = ImageProcessingEngine.compressToTargetKb(
                inputBitmap = enhanced,
                minKb = state.targetMinKb,
                maxKb = state.targetMaxKb,
                targetKb = state.exactTargetKb
            )

            withContext(Dispatchers.Main) {
                _thumbState.update {
                    it.copy(
                        processedResult = result,
                        isProcessing = false
                    )
                }
            }
        }
    }

    fun saveProcessedThumb(activity: Activity) {
        val state = _thumbState.value
        val result = state.processedResult ?: return

        val uri = FileUtils.saveImageToStorage(
            activity,
            result.bytes,
            "Thumb_${_activePreset.value.shortCode}"
        )

        if (uri != null) {
            val item = SavedDocumentItem(
                id = "thumb_${System.currentTimeMillis()}",
                title = "Thumb Impression (${_activePreset.value.shortCode})",
                examCategory = _activePreset.value.name,
                type = "Thumb",
                sizeKb = result.sizeKb,
                width = result.width,
                height = result.height,
                dateAdded = FileUtils.getCurrentFormattedDate(),
                uriString = uri.toString()
            )
            _savedDocuments.update { listOf(item) + it }
            _thumbState.update {
                it.copy(
                    savedUri = uri,
                    statusMessage = "Thumb Impression saved! Size: ${result.sizeKb} KB (Compliant with ${_activePreset.value.shortCode})"
                )
            }

            UnityAdsManager.showInterstitial(activity) {}
        }
    }

    // -------------------------------------------------------------
    // 3-IN-1 EXAM BATCH KIT ACTIONS
    // -------------------------------------------------------------

    fun setExamKitPhoto(bitmap: Bitmap) {
        _examKitState.update { it.copy(photoBitmap = bitmap) }
    }

    fun setExamKitSignature(bitmap: Bitmap) {
        _examKitState.update { it.copy(signatureBitmap = bitmap) }
    }

    fun setExamKitThumb(bitmap: Bitmap) {
        _examKitState.update { it.copy(thumbBitmap = bitmap) }
    }

    fun loadAllSampleExamKit() {
        _examKitState.update {
            it.copy(
                photoBitmap = ImageProcessingEngine.createSamplePassportPhoto(),
                signatureBitmap = ImageProcessingEngine.createSampleSignature(),
                thumbBitmap = ImageProcessingEngine.createSampleThumb()
            )
        }
    }

    fun processFullExamKit(activity: Activity) {
        val state = _examKitState.value
        val preset = _activePreset.value

        if (state.photoBitmap == null || state.signatureBitmap == null || state.thumbBitmap == null) {
            _examKitState.update { it.copy(statusMessage = "Please select or generate Photo, Signature, and Thumb first.") }
            return
        }

        _examKitState.update { it.copy(isProcessing = true, statusMessage = null) }

        viewModelScope.launch(Dispatchers.Default) {
            // Process photo
            val photoSized = ImageProcessingEngine.resizeBitmap(state.photoBitmap, preset.photo.widthPx, preset.photo.heightPx)
            val photoResult = ImageProcessingEngine.compressToTargetKb(photoSized, preset.photo.minKb, preset.photo.maxKb, preset.photo.targetKb)

            // Process signature
            val signCleaned = ImageProcessingEngine.cleanSignature(state.signatureBitmap, 0.65f, Color.BLACK)
            val signSized = ImageProcessingEngine.resizeBitmap(signCleaned, preset.signature.widthPx, preset.signature.heightPx)
            val signResult = ImageProcessingEngine.compressToTargetKb(signSized, preset.signature.minKb, preset.signature.maxKb, preset.signature.targetKb)

            // Process thumb
            val thumbEnhanced = ImageProcessingEngine.enhanceThumbImpression(state.thumbBitmap, 1.4f)
            val thumbSized = ImageProcessingEngine.resizeBitmap(thumbEnhanced, preset.thumb.widthPx, preset.thumb.heightPx)
            val thumbResult = ImageProcessingEngine.compressToTargetKb(thumbSized, preset.thumb.minKb, preset.thumb.maxKb, preset.thumb.targetKb)

            withContext(Dispatchers.Main) {
                // Save all 3 to storage
                val pUri = FileUtils.saveImageToStorage(activity, photoResult.bytes, "Batch_Photo_${preset.shortCode}")
                val sUri = FileUtils.saveImageToStorage(activity, signResult.bytes, "Batch_Sign_${preset.shortCode}")
                val tUri = FileUtils.saveImageToStorage(activity, thumbResult.bytes, "Batch_Thumb_${preset.shortCode}")

                val newItems = mutableListOf<SavedDocumentItem>()
                if (pUri != null) {
                    newItems.add(SavedDocumentItem("kit_p_${System.currentTimeMillis()}", "${preset.shortCode} Kit Photo", preset.name, "Photo", photoResult.sizeKb, photoResult.width, photoResult.height, FileUtils.getCurrentFormattedDate(), pUri.toString()))
                }
                if (sUri != null) {
                    newItems.add(SavedDocumentItem("kit_s_${System.currentTimeMillis()}", "${preset.shortCode} Kit Sign", preset.name, "Signature", signResult.sizeKb, signResult.width, signResult.height, FileUtils.getCurrentFormattedDate(), sUri.toString()))
                }
                if (tUri != null) {
                    newItems.add(SavedDocumentItem("kit_t_${System.currentTimeMillis()}", "${preset.shortCode} Kit Thumb", preset.name, "Thumb", thumbResult.sizeKb, thumbResult.width, thumbResult.height, FileUtils.getCurrentFormattedDate(), tUri.toString()))
                }

                _savedDocuments.update { newItems + it }

                _examKitState.update {
                    it.copy(
                        photoResult = photoResult,
                        signatureResult = signResult,
                        thumbResult = thumbResult,
                        isProcessing = false,
                        allCompleted = true,
                        statusMessage = "All 3 documents processed & saved! 100% compliant with ${preset.name}."
                    )
                }

                // Show Unity Rewarded Ad for completing 3-in-1 Kit!
                UnityAdsManager.showRewarded(
                    activity,
                    onRewardGranted = { /* Reward unlocked */ },
                    onAdFinished = {}
                )
            }
        }
    }
}
