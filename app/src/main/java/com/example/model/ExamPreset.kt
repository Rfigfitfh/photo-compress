package com.example.model

data class PhotoRequirement(
    val minKb: Int,
    val maxKb: Int,
    val targetKb: Int,
    val widthPx: Int,
    val heightPx: Int,
    val aspectRatio: Float = widthPx.toFloat() / heightPx.toFloat(),
    val description: String,
    val requiresDopStamp: Boolean = false,
    val recommendedBg: String = "White"
)

data class SignatureRequirement(
    val minKb: Int,
    val maxKb: Int,
    val targetKb: Int,
    val widthPx: Int,
    val heightPx: Int,
    val aspectRatio: Float = widthPx.toFloat() / heightPx.toFloat(),
    val description: String,
    val inkColor: String = "Black"
)

data class ThumbRequirement(
    val minKb: Int,
    val maxKb: Int,
    val targetKb: Int,
    val widthPx: Int,
    val heightPx: Int,
    val description: String,
    val hand: String = "Left Thumb"
)

data class ExamPreset(
    val id: String,
    val name: String,
    val shortCode: String,
    val category: String,
    val photo: PhotoRequirement,
    val signature: SignatureRequirement,
    val thumb: ThumbRequirement,
    val tips: List<String>
)

object ExamPresetsRepository {
    val presets: List<ExamPreset> = listOf(
        ExamPreset(
            id = "ssc_cgl",
            name = "SSC (CGL, CHSL, MTS, GD)",
            shortCode = "SSC",
            category = "Central Staff Selection",
            photo = PhotoRequirement(
                minKb = 20,
                maxKb = 50,
                targetKb = 35,
                widthPx = 350,
                heightPx = 450,
                description = "3.5 cm x 4.5 cm, plain white/light background, clear eyes/ears without cap/spectacles.",
                requiresDopStamp = true,
                recommendedBg = "White"
            ),
            signature = SignatureRequirement(
                minKb = 10,
                maxKb = 20,
                targetKb = 15,
                widthPx = 280,
                heightPx = 120,
                description = "4.0 cm x 2.0 cm, black ink on white paper, no blurred strokes.",
                inkColor = "Black"
            ),
            thumb = ThumbRequirement(
                minKb = 10,
                maxKb = 20,
                targetKb = 15,
                widthPx = 240,
                heightPx = 240,
                description = "Left thumb impression on white paper, blue or black ink."
            ),
            tips = listOf(
                "Spectacles & caps lead to automatic application rejection in SSC.",
                "Stamp candidate name and photo capture date (within 3 months) at the bottom.",
                "Signature must be in running handwriting, not ALL CAPS."
            )
        ),
        ExamPreset(
            id = "upsc_cse",
            name = "UPSC (Civil Services, NDA, CDS)",
            shortCode = "UPSC",
            category = "Union Public Service",
            photo = PhotoRequirement(
                minKb = 20,
                maxKb = 300,
                targetKb = 100,
                widthPx = 550,
                heightPx = 550,
                description = "Min 350x350 px, Max 1000x1000 px, 3/4th face coverage, White background.",
                requiresDopStamp = true,
                recommendedBg = "White"
            ),
            signature = SignatureRequirement(
                minKb = 20,
                maxKb = 300,
                targetKb = 80,
                widthPx = 550,
                heightPx = 550,
                description = "Min 350x350 px, Black ink on clean white background.",
                inkColor = "Black"
            ),
            thumb = ThumbRequirement(
                minKb = 20,
                maxKb = 300,
                targetKb = 75,
                widthPx = 400,
                heightPx = 400,
                description = "Clear unsmudged thumb impression on plain paper."
            ),
            tips = listOf(
                "Photo must not be older than 10 days from notification release.",
                "Candidate's Name and Date on which the photograph was taken must be clearly inscribed."
            )
        ),
        ExamPreset(
            id = "ibps_sbi",
            name = "IBPS / SBI (PO, Clerk, SO)",
            shortCode = "IBPS/SBI",
            category = "Banking Recruitment",
            photo = PhotoRequirement(
                minKb = 20,
                maxKb = 50,
                targetKb = 35,
                widthPx = 200,
                heightPx = 230,
                description = "200 x 230 pixels, preferred 4.5 cm x 3.5 cm, light-coloured/white background.",
                requiresDopStamp = false,
                recommendedBg = "White"
            ),
            signature = SignatureRequirement(
                minKb = 10,
                maxKb = 20,
                targetKb = 15,
                widthPx = 140,
                heightPx = 60,
                description = "140 x 60 pixels, black ink pen on white paper. Capital letters not accepted.",
                inkColor = "Black"
            ),
            thumb = ThumbRequirement(
                minKb = 20,
                maxKb = 50,
                targetKb = 35,
                widthPx = 240,
                heightPx = 240,
                description = "240 x 240 pixels (3 cm x 3 cm at 200 DPI), left thumb impression, blue/black ink."
            ),
            tips = listOf(
                "Signature in capital letters will NOT be accepted.",
                "Left thumb impression must show distinct ridges.",
                "Make sure file size is strictly within 20 KB to 50 KB."
            )
        ),
        ExamPreset(
            id = "rrb_railway",
            name = "Railway RRB (ALP, NTPC, Group D)",
            shortCode = "RRB",
            category = "Indian Railways",
            photo = PhotoRequirement(
                minKb = 20,
                maxKb = 50,
                targetKb = 35,
                widthPx = 320,
                heightPx = 400,
                description = "35mm x 45mm, clear color photo, plain white background, sharp focus.",
                requiresDopStamp = false,
                recommendedBg = "White"
            ),
            signature = SignatureRequirement(
                minKb = 10,
                maxKb = 20,
                targetKb = 15,
                widthPx = 200,
                heightPx = 100,
                description = "Black or dark blue ink, strictly in running hand.",
                inkColor = "Black"
            ),
            thumb = ThumbRequirement(
                minKb = 10,
                maxKb = 40,
                targetKb = 25,
                widthPx = 240,
                heightPx = 240,
                description = "Left thumb print, smooth paper, no overlapping."
            ),
            tips = listOf(
                "Photo should have a plain white background.",
                "Front view looking directly into camera with neutral expression."
            )
        ),
        ExamPreset(
            id = "nta_neet_jee",
            name = "NTA (NEET-UG, JEE Main, CUET)",
            shortCode = "NTA",
            category = "National Testing Agency",
            photo = PhotoRequirement(
                minKb = 10,
                maxKb = 200,
                targetKb = 80,
                widthPx = 400,
                heightPx = 500,
                description = "80% face coverage including ears, white background, name and date stamped.",
                requiresDopStamp = true,
                recommendedBg = "White"
            ),
            signature = SignatureRequirement(
                minKb = 4,
                maxKb = 30,
                targetKb = 18,
                widthPx = 300,
                heightPx = 120,
                description = "Running hand signature on white paper with black ink.",
                inkColor = "Black"
            ),
            thumb = ThumbRequirement(
                minKb = 10,
                maxKb = 200,
                targetKb = 50,
                widthPx = 300,
                heightPx = 300,
                description = "Left and Right thumb & fingers impressions on white paper."
            ),
            tips = listOf(
                "80% of image must be candidate's face including ears against pure white background.",
                "Must print candidate's name along with date of taking the photograph."
            )
        ),
        ExamPreset(
            id = "state_psc",
            name = "State PSC & Police (UPPSC, BPSC, MPSC, SI)",
            shortCode = "State PSC",
            category = "State Public Service",
            photo = PhotoRequirement(
                minKb = 20,
                maxKb = 50,
                targetKb = 35,
                widthPx = 350,
                heightPx = 450,
                description = "Light blue or white background, standard 3.5cm x 4.5cm passport dimension.",
                requiresDopStamp = false,
                recommendedBg = "Light Blue"
            ),
            signature = SignatureRequirement(
                minKb = 10,
                maxKb = 20,
                targetKb = 15,
                widthPx = 240,
                heightPx = 100,
                description = "Clear black ink signature on white paper.",
                inkColor = "Black"
            ),
            thumb = ThumbRequirement(
                minKb = 10,
                maxKb = 50,
                targetKb = 25,
                widthPx = 240,
                heightPx = 240,
                description = "Left thumb impression, clean unsmudged ridges."
            ),
            tips = listOf(
                "Light blue or white background is accepted across state portals.",
                "Check whether your state portal asks for combined photo + signature."
            )
        )
    )

    fun getPreset(id: String): ExamPreset {
        return presets.find { it.id == id } ?: presets.first()
    }
}
