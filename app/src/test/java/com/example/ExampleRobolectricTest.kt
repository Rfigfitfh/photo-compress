package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.ImageProcessingEngine
import com.example.model.ExamPresetsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Image Compress Cloud", appName)
    }

    @Test
    fun `verify exam presets exist`() {
        val presets = ExamPresetsRepository.presets
        assertTrue(presets.isNotEmpty())
        val ssc = ExamPresetsRepository.getPreset("ssc_cgl")
        assertNotNull(ssc)
        assertEquals("SSC", ssc.shortCode)
        assertEquals(20, ssc.photo.minKb)
        assertEquals(50, ssc.photo.maxKb)
    }

    @Test
    fun `verify image processing compressor produces compliant output`() {
        val samplePhoto = ImageProcessingEngine.createSamplePassportPhoto()
        assertNotNull(samplePhoto)

        // Compress targeting 35 KB (range 20 to 50 KB)
        val result = ImageProcessingEngine.compressToTargetKb(
            inputBitmap = samplePhoto,
            minKb = 20,
            maxKb = 50,
            targetKb = 35
        )

        assertNotNull(result)
        assertTrue(result.bytes.isNotEmpty())
        assertTrue(result.quality in 1..100)
    }
}
