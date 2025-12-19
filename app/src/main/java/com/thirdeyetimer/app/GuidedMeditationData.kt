package com.thirdeyetimer.app

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/**
 * Data class representing a meditation item
 */
data class MeditationItem(
    val id: Int,
    val title: String,
    val resourceId: Int,
    val icon: ImageVector,
    val description: String,
    val duration: String,
    val difficulty: String = "",
    val category: MeditationCategory,
    val isFeatured: Boolean = false
)

/**
 * Enum representing meditation categories
 */
enum class MeditationCategory {
    BASIC,
    BODY_SCAN,
    LOVING_KINDNESS,
    BUDDHIST,
    HINDU,
    NATURE,
    CHAKRA,
    SPECIALIZED,
    EATING_MOVEMENT
}

/**
 * Helper class to organize and access meditation data
 */
class GuidedMeditationData {
    companion object {
        /**
         * Maps a category to its display name, icon and description
         */
        val CATEGORY_INFO = mapOf(
            MeditationCategory.BASIC to Triple("Basic Meditation Techniques", Icons.Filled.SelfImprovement, "Fundamental practices for beginners and seasoned meditators"),
            MeditationCategory.BODY_SCAN to Triple("Body Scan Meditations", Icons.Filled.AccessibilityNew, "Cultivate awareness of physical sensations in the body"),
            MeditationCategory.LOVING_KINDNESS to Triple("Loving Kindness & Compassion", Icons.Filled.Favorite, "Develop compassion for yourself and others"),
            MeditationCategory.BUDDHIST to Triple("Buddhist Practices", Icons.Filled.Spa, "Traditional Buddhist meditation techniques"),
            MeditationCategory.HINDU to Triple("Hindu & Yogic Practices", Icons.Filled.LightMode, "Meditations from Hindu and yogic traditions"),
            MeditationCategory.NATURE to Triple("Nature & Environment", Icons.Filled.Landscape, "Connect with the natural world through meditation"),
            MeditationCategory.CHAKRA to Triple("Chakra Meditations", Icons.Filled.FilterVintage, "Balance and activate your energy centers"),
            MeditationCategory.SPECIALIZED to Triple("Specialized Practices", Icons.Filled.Bolt, "Targeted meditations for specific purposes"),
            MeditationCategory.EATING_MOVEMENT to Triple("Eating & Movement", Icons.Filled.DirectionsWalk, "Mindful practices for eating and physical activities")
        )

        /**
         * Creates a list of recommended featured meditations
         */
        fun getFeaturedMeditations(): List<MeditationItem> {
            return ALL_MEDITATIONS.filter { it.isFeatured }
        }

        /**
         * Gets meditations by category
         */
        fun getMeditationsByCategory(category: MeditationCategory): List<MeditationItem> {
            return ALL_MEDITATIONS.filter { it.category == category }
        }

        /**
         * Gets a meditation by its resource ID
         */
        fun getMeditationByResourceId(resourceId: Int): MeditationItem? {
            return ALL_MEDITATIONS.find { it.resourceId == resourceId }
        }

        /**
         * Gets a meditation by its position in the original guided meditation array
         */
        fun getMeditationByPosition(position: Int): MeditationItem? {
            return if (position >= 0 && position < ALL_MEDITATIONS.size) {
                ALL_MEDITATIONS[position]
            } else {
                null
            }
        }

        /**
         * The complete list of all meditations
         */
        val ALL_MEDITATIONS = listOf(
            // Complete Silence
            MeditationItem(
                id = 0,
                title = "Complete Silence",
                resourceId = 0,
                icon = Icons.Filled.PlayCircle,
                description = "Meditate in complete silence",
                duration = "Variable",
                category = MeditationCategory.BASIC,
                isFeatured = true
            ),

            // Basic Meditation Techniques
            MeditationItem(
                id = 1,
                title = "Acceptance Meditation",
                resourceId = R.raw.acceptance_meditation,
                icon = Icons.Filled.PlayCircle,
                description = "Learn to accept thoughts and sensations",
                duration = "15 min",
                category = MeditationCategory.BASIC,
                isFeatured = true
            ),
            MeditationItem(
                id = 2,
                title = "Acceptance",
                resourceId = R.raw.acceptance,
                icon = Icons.Filled.PlayCircle,
                description = "Accept what is present in your experience",
                duration = "10 min",
                category = MeditationCategory.BASIC
            ),
            MeditationItem(
                id = 3,
                title = "Anapanasati",
                resourceId = R.raw.anapanasati,
                icon = Icons.Filled.PlayCircle,
                description = "Traditional Buddhist breath awareness",
                duration = "20 min",
                category = MeditationCategory.BASIC,
                isFeatured = true
            ),
            MeditationItem(
                id = 4,
                title = "Breath Counting",
                resourceId = R.raw.breath_counting,
                icon = Icons.Filled.PlayCircle,
                description = "Focus on counting your breaths",
                duration = "12 min",
                category = MeditationCategory.BASIC
            ),
            MeditationItem(
                id = 5,
                title = "Buddhist 1 - Breath Anapanasati",
                resourceId = R.raw.buddhist_1_breath_anapanasati,
                icon = Icons.Filled.PlayCircle,
                description = "Traditional Buddhist breath meditation",
                duration = "15 min",
                category = MeditationCategory.BASIC
            ),
            MeditationItem(
                id = 6,
                title = "Choiceless Awareness",
                resourceId = R.raw.choiceless_awareness,
                icon = Icons.Filled.PlayCircle,
                description = "Open awareness to whatever arises",
                duration = "15 min",
                category = MeditationCategory.BASIC
            ),
            MeditationItem(
                id = 7,
                title = "Mindfulness Breathing",
                resourceId = R.raw.mindfulness_breathing,
                icon = Icons.Filled.PlayCircle,
                description = "Gentle awareness of the breath",
                duration = "10 min",
                category = MeditationCategory.BASIC,
                isFeatured = true
            ),
            MeditationItem(
                id = 8,
                title = "Open Awareness",
                resourceId = R.raw.open_awareness,
                icon = Icons.Filled.PlayCircle,
                description = "Expand your awareness without focus",
                duration = "15 min",
                category = MeditationCategory.BASIC
            ),

            // Body Scan Techniques
            MeditationItem(
                id = 9,
                title = "Body Scan Meditation",
                resourceId = R.raw.body_scan_meditation,
                icon = Icons.Filled.PlayCircle,
                description = "Complete body awareness practice",
                duration = "20 min",
                category = MeditationCategory.BODY_SCAN,
                isFeatured = true
            ),
            MeditationItem(
                id = 10,
                title = "Body Scan Bottom Up",
                resourceId = R.raw.body_scan_bottom_up,
                icon = Icons.Filled.PlayCircle,
                description = "Scan from feet to head",
                duration = "15 min",
                category = MeditationCategory.BODY_SCAN
            ),
            MeditationItem(
                id = 11,
                title = "Body Scan Top Down",
                resourceId = R.raw.body_scan_top_down,
                icon = Icons.Filled.PlayCircle,
                description = "Scan from head to feet",
                duration = "15 min",
                category = MeditationCategory.BODY_SCAN
            ),
            MeditationItem(
                id = 12,
                title = "Body Scan Left Right",
                resourceId = R.raw.body_scan_left_right,
                icon = Icons.Filled.PlayCircle,
                description = "Scan from left to right",
                duration = "15 min",
                category = MeditationCategory.BODY_SCAN
            ),
            MeditationItem(
                id = 13,
                title = "Body Scan Front Back",
                resourceId = R.raw.body_scan_front_back,
                icon = Icons.Filled.PlayCircle,
                description = "Scan from front to back",
                duration = "15 min",
                category = MeditationCategory.BODY_SCAN
            ),

            // Loving Kindness & Compassion
            MeditationItem(
                id = 14,
                title = "Loving Kindness Meditation",
                resourceId = R.raw.loving_kindness_meditation,
                icon = Icons.Filled.PlayCircle,
                description = "Cultivate universal loving kindness",
                duration = "15 min",
                category = MeditationCategory.LOVING_KINDNESS,
                isFeatured = true
            ),
            MeditationItem(
                id = 15,
                title = "Loving Kindness",
                resourceId = R.raw.loving_kindness,
                icon = Icons.Filled.PlayCircle,
                description = "Develop compassion for all beings",
                duration = "12 min",
                category = MeditationCategory.LOVING_KINDNESS
            ),
            MeditationItem(
                id = 16,
                title = "Buddhist 2 - Loving Kindness Metta",
                resourceId = R.raw.buddhist_2_loving_kindness_metta,
                icon = Icons.Filled.PlayCircle,
                description = "Traditional Buddhist metta practice",
                duration = "15 min",
                category = MeditationCategory.LOVING_KINDNESS
            ),
            MeditationItem(
                id = 17,
                title = "Compassion Meditation",
                resourceId = R.raw.compassion_meditation,
                icon = Icons.Filled.PlayCircle,
                description = "Developing deep compassion",
                duration = "15 min",
                category = MeditationCategory.LOVING_KINDNESS
            ),
            MeditationItem(
                id = 18,
                title = "Compassion",
                resourceId = R.raw.compassion,
                icon = Icons.Filled.PlayCircle,
                description = "Connect with compassion for self and others",
                duration = "10 min",
                category = MeditationCategory.LOVING_KINDNESS
            ),
            MeditationItem(
                id = 19,
                title = "Buddhist 6 - Compassion Tonglen",
                resourceId = R.raw.buddhist_6_compassion_tonglen,
                icon = Icons.Filled.PlayCircle,
                description = "Traditional tonglen practice",
                duration = "15 min",
                category = MeditationCategory.LOVING_KINDNESS
            ),
            MeditationItem(
                id = 20,
                title = "Metta Benefactor",
                resourceId = R.raw.metta_benefactor,
                icon = Icons.Filled.PlayCircle,
                description = "Loving kindness for those who help us",
                duration = "10 min",
                category = MeditationCategory.LOVING_KINDNESS
            ),
            MeditationItem(
                id = 21,
                title = "Metta Difficult",
                resourceId = R.raw.metta_difficult,
                icon = Icons.Filled.PlayCircle,
                description = "Loving kindness for difficult people",
                duration = "10 min",
                category = MeditationCategory.LOVING_KINDNESS
            ),
            MeditationItem(
                id = 22,
                title = "Metta Neutral",
                resourceId = R.raw.metta_neutral,
                icon = Icons.Filled.PlayCircle,
                description = "Loving kindness for neutral people",
                duration = "10 min",
                category = MeditationCategory.LOVING_KINDNESS
            ),
            MeditationItem(
                id = 23,
                title = "Metta Self",
                resourceId = R.raw.metta_self,
                icon = Icons.Filled.PlayCircle,
                description = "Loving kindness for yourself",
                duration = "10 min",
                category = MeditationCategory.LOVING_KINDNESS
            ),

            // Buddhist Practices (add more items for each category...)
            MeditationItem(
                id = 24,
                title = "Buddhist 3 - Body Scan Four Elements",
                resourceId = R.raw.buddhist_3_body_scan_four_elements,
                icon = Icons.Filled.PlayCircle,
                description = "Traditional four elements meditation",
                duration = "15 min",
                category = MeditationCategory.BUDDHIST
            ),
            MeditationItem(
                id = 25,
                title = "Buddhist 4 - Open Awareness",
                resourceId = R.raw.buddhist_4_open_awareness,
                icon = Icons.Filled.PlayCircle,
                description = "Open awareness practice",
                duration = "15 min",
                category = MeditationCategory.BUDDHIST
            ),
            MeditationItem(
                id = 26,
                title = "Buddhist 5 - Walking Gatha",
                resourceId = R.raw.buddhist_5_walking_gatha,
                icon = Icons.Filled.PlayCircle,
                description = "Walking meditation with verses",
                duration = "15 min",
                category = MeditationCategory.BUDDHIST
            ),
            MeditationItem(
                id = 27,
                title = "Buddhist 7 - Refuge Three Jewels",
                resourceId = R.raw.buddhist_7_refuge_three_jewels,
                icon = Icons.Filled.PlayCircle,
                description = "Taking refuge in Buddha, Dharma, Sangha",
                duration = "15 min",
                category = MeditationCategory.BUDDHIST
            ),
            MeditationItem(
                id = 28,
                title = "Eightfold Path",
                resourceId = R.raw.eightfold_path,
                icon = Icons.Filled.PlayCircle,
                description = "Contemplation of the Eightfold Path",
                duration = "15 min",
                category = MeditationCategory.BUDDHIST
            ),
            MeditationItem(
                id = 29,
                title = "Four Foundations",
                resourceId = R.raw.four_foundations,
                icon = Icons.Filled.PlayCircle,
                description = "Four foundations of mindfulness",
                duration = "15 min",
                category = MeditationCategory.BUDDHIST,
                isFeatured = true
            ),
            MeditationItem(
                id = 30,
                title = "Four Immeasurables",
                resourceId = R.raw.four_immeasurables,
                icon = Icons.Filled.PlayCircle,
                description = "Loving-kindness, compassion, joy, equanimity",
                duration = "15 min",
                category = MeditationCategory.BUDDHIST
            ),

            // Hindu Practices
            MeditationItem(
                id = 31,
                title = "Hindu 1 - Mantra Presence",
                resourceId = R.raw.hindu_1_mantra_presence,
                icon = Icons.Filled.PlayCircle,
                description = "Mantra meditation for presence",
                duration = "15 min",
                category = MeditationCategory.HINDU
            ),
            MeditationItem(
                id = 32,
                title = "Hindu 2 - Prana Body Scan",
                resourceId = R.raw.hindu_2_prana_body_scan,
                icon = Icons.Filled.PlayCircle,
                description = "Energetic body scan practice",
                duration = "15 min",
                category = MeditationCategory.HINDU
            ),
            MeditationItem(
                id = 33,
                title = "Hindu 3 - Om Resonance",
                resourceId = R.raw.hindu_3_om_resonance,
                icon = Icons.Filled.PlayCircle,
                description = "Resonating with the sound of Om",
                duration = "15 min",
                category = MeditationCategory.HINDU,
                isFeatured = true
            ),
            MeditationItem(
                id = 34,
                title = "Hindu 4 - Nature Dhyana",
                resourceId = R.raw.hindu_4_nature_dhyana,
                icon = Icons.Filled.PlayCircle,
                description = "Nature meditation from Hindu tradition",
                duration = "15 min",
                category = MeditationCategory.HINDU
            ),
            MeditationItem(
                id = 35,
                title = "Kundalini",
                resourceId = R.raw.kundalini,
                icon = Icons.Filled.PlayCircle,
                description = "Awakening the kundalini energy",
                duration = "15 min",
                category = MeditationCategory.HINDU
            ),
            MeditationItem(
                id = 36,
                title = "Mantra",
                resourceId = R.raw.mantra,
                icon = Icons.Filled.PlayCircle,
                description = "Sacred sound repetition practice",
                duration = "15 min",
                category = MeditationCategory.HINDU
            ),

            // You can add more items for the remaining categories...
            // This is a representative sample - the full list would include all meditations

            // Nature & Environment (sample)
            MeditationItem(
                id = 37,
                title = "Mountain Meditation",
                resourceId = R.raw.mountain_meditation,
                icon = Icons.Filled.PlayCircle,
                description = "Embody the stability of a mountain",
                duration = "15 min",
                category = MeditationCategory.NATURE,
                isFeatured = true
            ),
            MeditationItem(
                id = 38,
                title = "Ocean Meditation",
                resourceId = R.raw.ocean_meditation,
                icon = Icons.Filled.PlayCircle,
                description = "Connect with the vast ocean",
                duration = "15 min",
                category = MeditationCategory.NATURE
            ),

            // Chakra Meditations (sample)
            MeditationItem(
                id = 39,
                title = "Chakra Third Eye",
                resourceId = R.raw.chakra_third_eye,
                icon = Icons.Filled.PlayCircle,
                description = "Activate your intuition center",
                duration = "15 min",
                category = MeditationCategory.CHAKRA,
                isFeatured = true
            ),
            MeditationItem(
                id = 40,
                title = "Chakra Heart",
                resourceId = R.raw.chakra_heart,
                icon = Icons.Filled.PlayCircle,
                description = "Open and balance your heart center",
                duration = "15 min",
                category = MeditationCategory.CHAKRA
            ),

            // Specialized Practices (sample)
            MeditationItem(
                id = 41,
                title = "Forgiveness",
                resourceId = R.raw.forgiveness,
                icon = Icons.Filled.PlayCircle,
                description = "Practice letting go of grudges",
                duration = "15 min",
                category = MeditationCategory.SPECIALIZED
            ),
            MeditationItem(
                id = 42,
                title = "Gratitude Meditation",
                resourceId = R.raw.gratitude_meditation,
                icon = Icons.Filled.PlayCircle,
                description = "Cultivate appreciation for life",
                duration = "12 min",
                category = MeditationCategory.SPECIALIZED,
                isFeatured = true
            ),

            // Eating & Movement (sample)
            MeditationItem(
                id = 43,
                title = "Mindful Eating Meditation",
                resourceId = R.raw.mindful_eating_meditation,
                icon = Icons.Filled.PlayCircle,
                description = "Eat with full awareness",
                duration = "10 min",
                category = MeditationCategory.EATING_MOVEMENT
            ),
            MeditationItem(
                id = 44,
                title = "Walking Meditation",
                resourceId = R.raw.walking_meditation,
                icon = Icons.Filled.PlayCircle,
                description = "Practice mindfulness while walking",
                duration = "15 min",
                category = MeditationCategory.EATING_MOVEMENT,
                isFeatured = true
            )
        )
    }
}
