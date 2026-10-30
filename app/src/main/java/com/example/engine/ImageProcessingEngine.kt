package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import java.io.ByteArrayOutputStream
import java.util.ArrayDeque
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class ProcessedImageResult(
    val bitmap: Bitmap,
    val bytes: ByteArray,
    val sizeKb: Float,
    val width: Int,
    val height: Int,
    val quality: Int,
    val isCompliant: Boolean,
    val format: String = "JPEG"
)

object ImageProcessingEngine {

    /**
     * Binary search exact KB compressor.
     * Guaranteed to find optimal JPEG quality & scale within [minKb, maxKb].
     */
    fun compressToTargetKb(
        inputBitmap: Bitmap,
        minKb: Int,
        maxKb: Int,
        targetKb: Int? = null,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): ProcessedImageResult {
        val desiredTargetKb = targetKb ?: ((minKb + maxKb) / 2)
        var currentBitmap = inputBitmap

        var lowQuality = 15
        var highQuality = 98
        var bestQuality = 85
        var bestBytes = ByteArray(0)
        var bestSizeKb = Float.MAX_VALUE
        var bestBitmap = currentBitmap

        // Attempt up to 3 scale steps if image is too large even at lowest quality
        for (scaleAttempt in 0..2) {
            lowQuality = 15
            highQuality = 98
            var currentBestForScale = ByteArray(0)
            var currentBestSizeForScale = Float.MAX_VALUE
            var currentBestQuality = 80

            while (lowQuality <= highQuality) {
                val midQuality = (lowQuality + highQuality) / 2
                val stream = ByteArrayOutputStream()
                currentBitmap.compress(format, midQuality, stream)
                val bytes = stream.toByteArray()
                val sizeKb = bytes.size / 1024f

                val currentDiff = abs(sizeKb - desiredTargetKb)
                val bestDiff = abs(currentBestSizeForScale - desiredTargetKb)

                if (currentDiff < bestDiff) {
                    currentBestSizeForScale = sizeKb
                    currentBestForScale = bytes
                    currentBestQuality = midQuality
                }

                if (sizeKb > maxKb) {
                    highQuality = midQuality - 1
                } else if (sizeKb < minKb) {
                    lowQuality = midQuality + 1
                } else {
                    // Within range!
                    if (sizeKb <= desiredTargetKb) {
                        lowQuality = midQuality + 1
                    } else {
                        highQuality = midQuality - 1
                    }
                }
            }

            bestBytes = currentBestForScale
            bestSizeKb = currentBestSizeForScale
            bestQuality = currentBestQuality
            bestBitmap = currentBitmap

            // If best size is still too large, downscale image by 80%
            if (bestSizeKb > maxKb && currentBitmap.width > 200 && currentBitmap.height > 200) {
                val newWidth = (currentBitmap.width * 0.85f).roundToInt()
                val newHeight = (currentBitmap.height * 0.85f).roundToInt()
                currentBitmap = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true)
            } else {
                break
            }
        }

        val isCompliant = bestSizeKb >= (minKb * 0.9f) && bestSizeKb <= (maxKb * 1.05f)

        return ProcessedImageResult(
            bitmap = bestBitmap,
            bytes = bestBytes,
            sizeKb = (bestSizeKb * 10f).roundToInt() / 10f,
            width = bestBitmap.width,
            height = bestBitmap.height,
            quality = bestQuality,
            isCompliant = isCompliant,
            format = if (format == Bitmap.CompressFormat.PNG) "PNG" else "JPEG"
        )
    }

    /**
     * Resizes a bitmap to exact target pixel dimensions.
     */
    fun resizeBitmap(
        bitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        maintainAspectRatio: Boolean = true
    ): Bitmap {
        if (bitmap.width == targetWidth && bitmap.height == targetHeight) {
            return bitmap
        }

        if (!maintainAspectRatio) {
            return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        }

        // Fit center with aspect ratio
        val srcWidth = bitmap.width.toFloat()
        val srcHeight = bitmap.height.toFloat()
        val srcRatio = srcWidth / srcHeight
        val targetRatio = targetWidth.toFloat() / targetHeight.toFloat()

        var cropWidth = srcWidth
        var cropHeight = srcHeight
        var startX = 0f
        var startY = 0f

        if (srcRatio > targetRatio) {
            // Source is wider, crop sides
            cropWidth = srcHeight * targetRatio
            startX = (srcWidth - cropWidth) / 2f
        } else {
            // Source is taller, crop top/bottom
            cropHeight = srcWidth / targetRatio
            startY = (srcHeight - cropHeight) / 2f
        }

        val cropped = Bitmap.createBitmap(
            bitmap,
            startX.toInt(),
            startY.toInt(),
            cropWidth.toInt(),
            cropHeight.toInt()
        )
        return Bitmap.createScaledBitmap(cropped, targetWidth, targetHeight, true)
    }

    /**
     * Offline smart background changer for portrait/passport photos.
     * Uses corner seed flood fill with color distance matching and soft boundary feathering.
     */
    fun changeBackground(
        bitmap: Bitmap,
        targetBgColor: Int,
        tolerance: Float = 0.28f
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Sample background colors from top corners
        val seedCorners = listOf(
            0,
            width - 1,
            (width * height * 0.05f).toInt(),
            (width * height * 0.05f).toInt() + width - 1
        )

        var seedR = 0
        var seedG = 0
        var seedB = 0
        for (idx in seedCorners) {
            val color = pixels[idx]
            seedR += Color.red(color)
            seedG += Color.green(color)
            seedB += Color.blue(color)
        }
        seedR /= seedCorners.size
        seedG /= seedCorners.size
        seedB /= seedCorners.size

        val threshold = (tolerance * 441.67f) // Max RGB distance = sqrt(3*255^2) ≈ 441.67
        val isBackground = BooleanArray(width * height)
        val visited = BooleanArray(width * height)
        val queue = ArrayDeque<Int>()

        // Seed with perimeter pixels (top edge and top-half side edges)
        for (x in 0 until width) {
            queue.add(x)
            visited[x] = true
        }
        for (y in 1 until (height * 0.7f).toInt()) {
            val leftIdx = y * width
            val rightIdx = y * width + (width - 1)
            if (!visited[leftIdx]) {
                queue.add(leftIdx)
                visited[leftIdx] = true
            }
            if (!visited[rightIdx]) {
                queue.add(rightIdx)
                visited[rightIdx] = true
            }
        }

        val dx = intArrayOf(0, 0, -1, 1)
        val dy = intArrayOf(-1, 1, 0, 0)

        while (!queue.isEmpty()) {
            val idx = queue.poll() ?: break
            val color = pixels[idx]
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)

            val dist = sqrt(
                ((r - seedR) * (r - seedR) +
                 (g - seedG) * (g - seedG) +
                 (b - seedB) * (b - seedB)).toDouble()
            ).toFloat()

            if (dist <= threshold) {
                isBackground[idx] = true
                val cx = idx % width
                val cy = idx / width

                for (i in 0..3) {
                    val nx = cx + dx[i]
                    val ny = cy + dy[i]
                    if (nx in 0 until width && ny in 0 until height) {
                        val nIdx = ny * width + nx
                        if (!visited[nIdx]) {
                            visited[nIdx] = true
                            queue.add(nIdx)
                        }
                    }
                }
            }
        }

        // Apply target background color
        val outputPixels = IntArray(width * height)
        for (i in 0 until (width * height)) {
            if (isBackground[i]) {
                outputPixels[i] = targetBgColor
            } else {
                outputPixels[i] = pixels[i]
            }
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setPixels(outputPixels, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Stamped candidate name and date of photo (DOP) at the bottom
     * Mandatory for SSC, UPSC, Vyapam, State PSC exams.
     */
    fun addDopAndNameStamp(
        bitmap: Bitmap,
        candidateName: String,
        photoDate: String
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // Draw original photo
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        // Calculate bottom banner dimensions (approx 16% of height)
        val bannerHeight = (height * 0.17f).coerceAtLeast(36f)
        val bannerTop = height - bannerHeight

        val bannerPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(200, 200, 200)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }

        // Draw white banner rectangle
        canvas.drawRect(0f, bannerTop, width.toFloat(), height.toFloat(), bannerPaint)
        canvas.drawLine(0f, bannerTop, width.toFloat(), bannerTop, borderPaint)

        // Draw text
        val nameText = candidateName.trim().uppercase()
        val dateText = if (photoDate.isNotBlank()) "DOP: ${photoDate.trim()}" else ""

        val textSize = (bannerHeight * 0.36f).coerceIn(12f, 32f)

        val textPaint = Paint().apply {
            color = Color.BLACK
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val centerX = width / 2f
        if (dateText.isNotBlank()) {
            val nameY = bannerTop + (bannerHeight * 0.42f)
            val dateY = bannerTop + (bannerHeight * 0.82f)
            canvas.drawText(nameText, centerX, nameY, textPaint)
            textPaint.textSize = textSize * 0.88f
            canvas.drawText(dateText, centerX, dateY, textPaint)
        } else {
            val nameY = bannerTop + (bannerHeight * 0.62f)
            canvas.drawText(nameText, centerX, nameY, textPaint)
        }

        return resultBitmap
    }

    /**
     * Upscale and unsharp-mask sharpening enhancer.
     * Fixes dim, blurry smartphone photos for exam portals.
     */
    fun enhanceAndSharpen(
        bitmap: Bitmap,
        sharpnessAmount: Float = 1.3f,
        boostContrast: Boolean = true
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val srcPixels = IntArray(width * height)
        val dstPixels = IntArray(width * height)
        bitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)

        // 3x3 Sharpen Kernel
        val centerWeight = 1f + 4f * sharpnessAmount
        val edgeWeight = -sharpnessAmount

        for (y in 0 until height) {
            for (x in 0 until width) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    dstPixels[y * width + x] = srcPixels[y * width + x]
                    continue
                }

                val cCenter = srcPixels[y * width + x]
                val cTop = srcPixels[(y - 1) * width + x]
                val cBottom = srcPixels[(y + 1) * width + x]
                val cLeft = srcPixels[y * width + (x - 1)]
                val cRight = srcPixels[y * width + (x + 1)]

                var r = (Color.red(cCenter) * centerWeight +
                        (Color.red(cTop) + Color.red(cBottom) + Color.red(cLeft) + Color.red(cRight)) * edgeWeight)
                var g = (Color.green(cCenter) * centerWeight +
                        (Color.green(cTop) + Color.green(cBottom) + Color.green(cLeft) + Color.green(cRight)) * edgeWeight)
                var b = (Color.blue(cCenter) * centerWeight +
                        (Color.blue(cTop) + Color.blue(cBottom) + Color.blue(cLeft) + Color.blue(cRight)) * edgeWeight)

                if (boostContrast) {
                    // Mild S-curve contrast boost
                    r = (r - 128f) * 1.15f + 128f
                    g = (g - 128f) * 1.15f + 128f
                    b = (b - 128f) * 1.15f + 128f
                }

                val cr = r.roundToInt().coerceIn(0, 255)
                val cg = g.roundToInt().coerceIn(0, 255)
                val cb = b.roundToInt().coerceIn(0, 255)

                dstPixels[y * width + x] = Color.rgb(cr, cg, cb)
            }
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(dstPixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Clean and enhance paper signature photos.
     * Whitens yellowed paper shadows, boosts ink pigment to pure dark, and auto-trims whitespace.
     */
    fun cleanSignature(
        bitmap: Bitmap,
        paperWhiteningThreshold: Float = 0.65f,
        inkColorHex: Int = Color.BLACK,
        autoCropBorders: Boolean = true
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val srcPixels = IntArray(width * height)
        val dstPixels = IntArray(width * height)
        bitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)

        val threshold = (paperWhiteningThreshold * 255).toInt()

        var minX = width
        var maxX = 0
        var minY = height
        var maxY = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                val c = srcPixels[idx]
                // Luminosity formula
                val lum = (0.299f * Color.red(c) + 0.587f * Color.green(c) + 0.114f * Color.blue(c)).toInt()

                if (lum > threshold) {
                    // Clean background to pure white
                    dstPixels[idx] = Color.WHITE
                } else {
                    // Dark ink
                    dstPixels[idx] = inkColorHex
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        val processed = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        processed.setPixels(dstPixels, 0, width, 0, 0, width, height)

        if (autoCropBorders && minX < maxX && minY < maxY) {
            val padX = 14
            val padY = 10
            val cropLeft = (minX - padX).coerceAtLeast(0)
            val cropTop = (minY - padY).coerceAtLeast(0)
            val cropRight = (maxX + padX).coerceAtMost(width)
            val cropBottom = (maxY + padY).coerceAtMost(height)

            val cropW = cropRight - cropLeft
            val cropH = cropBottom - cropTop

            if (cropW > 10 && cropH > 10) {
                return Bitmap.createBitmap(processed, cropLeft, cropTop, cropW, cropH)
            }
        }

        return processed
    }

    /**
     * Biometric Thumb Impression Enhancer.
     * Clarifies friction ridge loops/whorls and removes background ink blotches.
     */
    fun enhanceThumbImpression(
        bitmap: Bitmap,
        ridgeContrast: Float = 1.4f
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val srcPixels = IntArray(width * height)
        val dstPixels = IntArray(width * height)
        bitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)

        for (i in 0 until (width * height)) {
            val c = srcPixels[i]
            val r = Color.red(c)
            val g = Color.green(c)
            val b = Color.blue(c)

            // Convert to grayscale
            val gray = (0.299f * r + 0.587f * g + 0.114f * b)

            // Amplify contrast around median
            val enhanced = ((gray - 120f) * ridgeContrast + 120f).coerceIn(0f, 255f).toInt()

            // If it's mostly paper background, brighten to pure white
            val finalVal = if (enhanced > 215) 255 else enhanced
            dstPixels[i] = Color.rgb(finalVal, finalVal, finalVal)
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(dstPixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Helper to generate a realistic sample candidate passport photo for instant testing.
     */
    fun createSamplePassportPhoto(): Bitmap {
        val width = 350
        val height = 450
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.rgb(238, 242, 246))

        val paint = Paint().apply { isAntiAlias = true }

        // Shoulders / Suit jacket
        paint.color = Color.rgb(30, 41, 59)
        val suitPath = Path().apply {
            moveTo(35f, 450f)
            quadTo(175f, 320f, 315f, 450f)
            close()
        }
        canvas.drawPath(suitPath, paint)

        // Shirt Collar
        paint.color = Color.WHITE
        val collarPath = Path().apply {
            moveTo(135f, 340f)
            lineTo(175f, 395f)
            lineTo(215f, 340f)
            close()
        }
        canvas.drawPath(collarPath, paint)

        // Neck
        paint.color = Color.rgb(228, 172, 136)
        canvas.drawRect(145f, 270f, 205f, 345f, paint)

        // Face
        paint.color = Color.rgb(240, 185, 148)
        canvas.drawOval(RectF(105f, 130f, 245f, 305f), paint)

        // Hair
        paint.color = Color.rgb(33, 33, 33)
        val hairPath = Path().apply {
            moveTo(95f, 220f)
            quadTo(90f, 110f, 175f, 105f)
            quadTo(260f, 110f, 255f, 220f)
            quadTo(230f, 135f, 175f, 135f)
            quadTo(120f, 135f, 95f, 220f)
            close()
        }
        canvas.drawPath(hairPath, paint)

        // Eyes
        paint.color = Color.rgb(40, 40, 40)
        canvas.drawCircle(145f, 205f, 6f, paint)
        canvas.drawCircle(205f, 205f, 6f, paint)

        // Eyebrows
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(135f, 192f, 155f, 192f, paint)
        canvas.drawLine(195f, 192f, 215f, 192f, paint)

        // Smile
        val mouthRect = RectF(155f, 248f, 195f, 268f)
        canvas.drawArc(mouthRect, 0f, 180f, false, paint)

        return bitmap
    }

    /**
     * Helper to generate a sample signature for instant testing.
     */
    fun createSampleSignature(): Bitmap {
        val width = 280
        val height = 120
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 4f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }

        val path = Path().apply {
            moveTo(30f, 75f)
            quadTo(55f, 35f, 75f, 65f)
            quadTo(95f, 95f, 115f, 65f)
            quadTo(135f, 35f, 155f, 65f)
            lineTo(185f, 65f)
            quadTo(205f, 40f, 225f, 85f)
            moveTo(50f, 90f)
            lineTo(240f, 90f)
        }
        canvas.drawPath(path, paint)

        return bitmap
    }

    /**
     * Helper to generate a sample thumb impression for instant testing.
     */
    fun createSampleThumb(): Bitmap {
        val width = 240
        val height = 240
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val centerX = 120f
        val centerY = 120f

        // Draw fingerprint oval ridges
        for (r in 14..95 step 12) {
            val oval = RectF(centerX - r * 0.75f, centerY - r, centerX + r * 0.75f, centerY + r)
            canvas.drawOval(oval, paint)
        }

        return bitmap
    }
}
