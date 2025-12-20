package com.thirdeyetimer.app.domain

import android.content.Context
import android.content.SharedPreferences

/**
 * TruthPunchManager
 * 
 * Manages the hierarchical "Truth Punch" system - 100 philosophical truths
 * organized into 5 tiers of increasing brutality.
 * 
 * This is the "Anti-Idle" core: players unlock harsh philosophical truths
 * through meditation progress, creating a unique engagement loop where
 * the "reward" is disillusionment rather than empowerment.
 * 
 * Tier 5 (Rank 81-100): Operational Lies - Society and Behavior
 * Tier 4 (Rank 61-80): Hollow Pursuit - Ambition and Success
 * Tier 3 (Rank 41-60): Transactional Heart - Love and Charity
 * Tier 2 (Rank 21-40): Spiritual Scam - Seeking and Methods
 * Tier 1 (Rank 1-20): Annihilation - Existential Truths
 */
class TruthPunchManager(context: Context) {
    
    private val PREFS_NAME = "TruthPunchPrefs"
    private val KEY_HIGHEST_UNLOCK = "highest_truth_unlocked"
    private val KEY_SEEN_TRUTHS = "seen_truths"
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        // Spiritual Ego thresholds for unlocking truths (exponential scaling)
        // First truths are easy, later ones require massive meditation time
        private val UNLOCK_THRESHOLDS = mapOf(
            100 to 0L,           // Truth 100 unlocked at start
            99 to 100L,
            98 to 250L,
            97 to 500L,
            96 to 750L,
            95 to 1_000L,
            94 to 1_500L,
            93 to 2_000L,
            92 to 3_000L,
            91 to 4_000L,
            90 to 5_000L,
            89 to 6_500L,
            88 to 8_000L,
            87 to 10_000L,
            86 to 12_500L,
            85 to 15_000L,
            84 to 18_000L,
            83 to 22_000L,
            82 to 27_000L,
            81 to 33_000L,
            // Tier 4 (Rank 61-80)
            80 to 40_000L,
            79 to 48_000L,
            78 to 57_000L,
            77 to 67_000L,
            76 to 78_000L,
            75 to 90_000L,
            74 to 105_000L,
            73 to 120_000L,
            72 to 140_000L,
            71 to 160_000L,
            70 to 185_000L,
            69 to 210_000L,
            68 to 240_000L,
            67 to 275_000L,
            66 to 315_000L,
            65 to 360_000L,
            64 to 410_000L,
            63 to 470_000L,
            62 to 540_000L,
            61 to 620_000L,
            // Tier 3 (Rank 41-60)
            60 to 710_000L,
            59 to 810_000L,
            58 to 930_000L,
            57 to 1_070_000L,
            56 to 1_230_000L,
            55 to 1_410_000L,
            54 to 1_620_000L,
            53 to 1_860_000L,
            52 to 2_140_000L,
            51 to 2_460_000L,
            50 to 2_830_000L,
            49 to 3_250_000L,
            48 to 3_740_000L,
            47 to 4_300_000L,
            46 to 4_950_000L,
            45 to 5_690_000L,
            44 to 6_540_000L,
            43 to 7_520_000L,
            42 to 8_650_000L,
            41 to 9_950_000L,
            // Tier 2 (Rank 21-40)
            40 to 11_500_000L,
            39 to 13_200_000L,
            38 to 15_200_000L,
            37 to 17_500_000L,
            36 to 20_100_000L,
            35 to 23_100_000L,
            34 to 26_600_000L,
            33 to 30_600_000L,
            32 to 35_200_000L,
            31 to 40_500_000L,
            30 to 46_600_000L,
            29 to 53_600_000L,
            28 to 61_600_000L,
            27 to 70_900_000L,
            26 to 81_500_000L,
            25 to 93_700_000L,
            24 to 107_800_000L,
            23 to 124_000_000L,
            22 to 142_600_000L,
            21 to 164_000_000L,
            // Tier 1 (Rank 1-20) - The Ultimate Truths
            20 to 188_600_000L,
            19 to 216_900_000L,
            18 to 249_400_000L,
            17 to 286_800_000L,
            16 to 329_800_000L,
            15 to 379_300_000L,
            14 to 436_200_000L,
            13 to 501_600_000L,
            12 to 576_800_000L,
            11 to 663_300_000L,
            10 to 762_800_000L,
            9 to 877_200_000L,
            8 to 1_008_800_000L,
            7 to 1_160_100_000L,
            6 to 1_334_100_000L,
            5 to 1_534_200_000L,
            4 to 1_764_300_000L,
            3 to 2_028_900_000L,
            2 to 2_333_300_000L,
            1 to 2_683_300_000L  // The Final Truth
        )
    }
    
    /**
     * Data class representing a single Truth Punch
     */
    data class TruthPunch(
        val rank: Int,           // 1-100, lower = more brutal
        val tier: Int,           // 1-5, lower = more existential
        val title: String,       // Ominous preview title
        val truth: String,       // The actual truth punch text
        val isUnlocked: Boolean = false,
        val isSeen: Boolean = false
    )
    
    // Highest truth rank unlocked (100 = first, 1 = last)
    var highestUnlockedRank: Int
        get() = prefs.getInt(KEY_HIGHEST_UNLOCK, 100)
        private set(value) = prefs.edit().putInt(KEY_HIGHEST_UNLOCK, value).apply()
    
    // Set of seen truth ranks
    private var seenTruths: MutableSet<Int>
        get() {
            val str = prefs.getString(KEY_SEEN_TRUTHS, "") ?: ""
            return if (str.isEmpty()) mutableSetOf() 
                   else str.split(",").mapNotNull { it.toIntOrNull() }.toMutableSet()
        }
        set(value) {
            prefs.edit().putString(KEY_SEEN_TRUTHS, value.joinToString(",")).apply()
        }
    
    /**
     * All 100 Truth Punches organized by rank
     * Lower rank = more brutal/existential
     */
    private val allTruths: Map<Int, TruthPunch> = mapOf(
        // ===== TIER 5: THE OPERATIONAL LIES (Society and Behavior) =====
        100 to TruthPunch(100, 5, "The Storytelling Machine",
            "The mind is a storytelling machine that never shuts up, and you believe every lie it tells."),
        99 to TruthPunch(99, 5, "The Addiction to Suffering",
            "You are addicted to your own suffering because it is the only thing that makes you feel alive."),
        98 to TruthPunch(98, 5, "The Tantrum of Expectation",
            "Anger is just a tantrum you throw when the world doesn't follow the script you wrote in your head."),
        97 to TruthPunch(97, 5, "The Ghost of Tomorrow",
            "Fear is merely the anticipation of pain that hasn't happened yet; you are haunting yourself."),
        96 to TruthPunch(96, 5, "The Vessel of Noise",
            "You do not have thoughts; thoughts have you. You are a vessel for noise."),
        95 to TruthPunch(95, 5, "The Withdrawal from Stimulation",
            "Boredom is the mind's withdrawal symptom from the drug of constant stimulation."),
        94 to TruthPunch(94, 5, "The Slave of Preference",
            "You are a slave to your preferences; you like what you were trained to like."),
        93 to TruthPunch(93, 5, "The Echo Chamber",
            "Your opinions are just regurgitated noise you heard from someone else."),
        92 to TruthPunch(92, 5, "The Twin of Pain",
            "Pleasure is the beginning of pain; you cannot have one without the other."),
        91 to TruthPunch(91, 5, "The Waiting Silence",
            "You only listen to reply, never to understand; your silence is just waiting for your turn."),
        90 to TruthPunch(90, 5, "The Booby Prize",
            "Intellectual understanding is the booby prize; knowing 'about' the truth changes nothing."),
        89 to TruthPunch(89, 5, "The Terrifying Emptiness",
            "You are constantly running away from the present moment because it is empty and terrifying."),
        88 to TruthPunch(88, 5, "The Lazy Habit",
            "Your 'values' are just habits you are too lazy to question."),
        87 to TruthPunch(87, 5, "The Weapon of Guilt",
            "Guilt is a weapon you use to punish yourself so you can feel 'moral' while staying the same."),
        86 to TruthPunch(86, 5, "The Biological Robot",
            "You are a biological robot programmed to seek survival and reproduction; there is no ghost in the machine."),
        85 to TruthPunch(85, 5, "The Self-Centered Sacrifice",
            "Everything you do is self-centered, even your self-sacrifice and your charity."),
        84 to TruthPunch(84, 5, "The Defended Prison",
            "You defend your limitations because they are familiar and safe."),
        83 to TruthPunch(83, 5, "The Problem Creator",
            "The mind cannot solve the problems it created; it IS the problem."),
        82 to TruthPunch(82, 5, "The Sleepwalk",
            "You are sleepwalking through a nightmare you call 'normal life.'"),
        81 to TruthPunch(81, 5, "The Portable Prison",
            "There is no escape; wherever you go, you bring your noise with you."),
        
        // ===== TIER 4: THE HOLLOW PURSUIT (Ambition and Success) =====
        80 to TruthPunch(80, 4, "The Byproduct Illusion",
            "Success is a byproduct, not a goal; if you are chasing it, you have already missed it."),
        79 to TruthPunch(79, 4, "The Beggar's Applause",
            "You want the applause, not the craft; you are a beggar seeking validation from other beggars."),
        78 to TruthPunch(78, 4, "The Consolation Prize",
            "Hard work is the consolation prize for those who lack the talent to be effortless."),
        77 to TruthPunch(77, 4, "The Acceptable Greed",
            "Ambition is the socially acceptable way to be greedy and dissatisfied."),
        76 to TruthPunch(76, 4, "The Comfortable Trap",
            "You are where you are because, at some level, you are okay with being there."),
        75 to TruthPunch(75, 4, "The Unserious Question",
            "Asking for advice is an admission that you are not serious about the outcome."),
        74 to TruthPunch(74, 4, "The Myth of Potential",
            "The world does not care about your potential; it only cares about what you have actually done."),
        73 to TruthPunch(73, 4, "The Manufactured Struggle",
            "You create problems to justify your existence; without a struggle, you would have no identity."),
        72 to TruthPunch(72, 4, "The Fear of Quality",
            "Society rewards mediocrity because the masses are terrified of true quality."),
        71 to TruthPunch(71, 4, "The Leadership Distraction",
            "Becoming a 'leader' is just a way to hide your own inability to follow your own truth."),
        70 to TruthPunch(70, 4, "The Absence of Hunger",
            "If you need motivation, you are not hungry enough; hunger needs no pep talk."),
        69 to TruthPunch(69, 4, "The Distraction Factory",
            "Your goals are just distractions to keep you from seeing the pointlessness of your life."),
        68 to TruthPunch(68, 4, "The Safe Surrender",
            "You settle for a salary because you are too afraid to find out what you are actually worth."),
        67 to TruthPunch(67, 4, "The Violence of Comparison",
            "Comparing yourself to others is violence against your own nature."),
        66 to TruthPunch(66, 4, "The Performance for Nobody",
            "The 'hustle' is a dance you perform to impress people who do not matter."),
        65 to TruthPunch(65, 4, "The Obsession Requirement",
            "True mastery requires the obsession of a maniac, not the 'balance' of a hobbyist."),
        64 to TruthPunch(64, 4, "The Waiting Room",
            "You are not waiting for the right moment; you are waiting for death to save you from having to try."),
        63 to TruthPunch(63, 4, "The Fear of Satisfaction",
            "Satisfaction is the death of desire, and you are terrified of being satisfied."),
        62 to TruthPunch(62, 4, "The Wealthy Misery",
            "The rich man is just as miserable as the poor man; he just has better toys to distract him."),
        61 to TruthPunch(61, 4, "The Vanity of Legacy",
            "Legacy is a vanity project for the dead; you will not be there to enjoy it."),
        
        // ===== TIER 3: THE TRANSACTIONAL HEART (Love and Charity) =====
        60 to TruthPunch(60, 3, "The Mirror of Self",
            "You do not love others; you love how others make you feel about yourself."),
        59 to TruthPunch(59, 3, "The Empty Pockets",
            "Two beggars pulling at each other's empty pockets—this is what you call a 'relationship.'"),
        58 to TruthPunch(58, 3, "The Division of Love",
            "Love implies division: the one who loves and the one who is loved. This is conflict, not union."),
        57 to TruthPunch(57, 3, "The Purchase of Goodness",
            "You give charity only to purchase a flattering image of yourself as a 'good person.'"),
        56 to TruthPunch(56, 3, "The Leash of Need",
            "Attachment is not love; it is a leash you place on another to ensure they service your needs."),
        55 to TruthPunch(55, 3, "The Fear of Solitude",
            "When you say 'I love you,' you are really saying, 'Do not leave me alone with myself.'"),
        54 to TruthPunch(54, 3, "The Chain of Responsibility",
            "Responsibility is a social chain forged to keep you enslaved to people you secretly resent."),
        53 to TruthPunch(53, 3, "The Guilt Contract",
            "You owe your children nothing, and they owe you nothing; everything else is a guilt contract."),
        52 to TruthPunch(52, 3, "The Superior Helper",
            "Helping others is a selfish act designed to assert your superiority and assuage your guilt."),
        51 to TruthPunch(51, 3, "The Honest Jealousy",
            "Jealousy is the only honest emotion in your relationships; it reveals your ownership."),
        50 to TruthPunch(50, 3, "The Violence of Need",
            "A relationship based on need will always end in violence, either physical or psychological."),
        49 to TruthPunch(49, 3, "The Human Furniture",
            "You use other people as furniture to decorate the empty room of your life."),
        48 to TruthPunch(48, 3, "The Disguised Ego",
            "True caring is leaving people alone; your interference is merely ego masquerading as concern."),
        47 to TruthPunch(47, 3, "The Self-Preserving Forgiveness",
            "You forgive others not to heal them, but to stop the pain of holding onto your own hate."),
        46 to TruthPunch(46, 3, "The Unbearable Self",
            "Loneliness is not the absence of others; it is the presence of a self you cannot stand."),
        45 to TruthPunch(45, 3, "The Conspiracy of Society",
            "Society is a conspiracy to keep you from realizing you are utterly alone."),
        44 to TruthPunch(44, 3, "The Fear-Based Morality",
            "Social morality is a fear-based construct, not a divine law."),
        43 to TruthPunch(43, 3, "The Monster in Silence",
            "You seek company because in silence, your own thoughts would eat you alive."),
        42 to TruthPunch(42, 3, "The Covert Demand",
            "Empathy is often just a covert demand for reciprocation."),
        41 to TruthPunch(41, 3, "The Parallel Truth",
            "The only pure relationship is parallel, where neither intersects or demands from the other."),
        
        // ===== TIER 2: THE SPIRITUAL SCAM (Seeking and Methods) =====
        40 to TruthPunch(40, 2, "The Prescription Trap",
            "Prescriptions and methods are for those who want to feel they are doing something, not for those who want to arrive."),
        39 to TruthPunch(39, 2, "The Sedated Mind",
            "Meditation is merely a way to sedate the mind so it can survive to torture you another day."),
        38 to TruthPunch(38, 2, "The Endless Practice",
            "If meditation worked, you would have stopped needing to do it years ago."),
        37 to TruthPunch(37, 2, "The Beloved Search",
            "You do not want the question to end; you want to keep asking it so you can remain the 'seeker.'"),
        36 to TruthPunch(36, 2, "The Blind Guide",
            "Following a guru is the ultimate act of cowardice; it is handing your eyes to a blind man."),
        35 to TruthPunch(35, 2, "The Strengthened Ego",
            "Every technique you practice strengthens the very ego you claim you are trying to dissolve."),
        34 to TruthPunch(34, 2, "The Split of Presence",
            "The moment you try to 'be present,' you have split yourself in two and created conflict."),
        33 to TruthPunch(33, 2, "The Comfort Seeker",
            "Spirituality is just a fancy word for psychological comfort-seeking."),
        32 to TruthPunch(32, 2, "The Violence of Silence",
            "Trying to quiet the mind is an act of violence against your own biology."),
        31 to TruthPunch(31, 2, "The Avoidance of Looking",
            "You read books on truth to avoid the terror of looking directly at your own falseness."),
        30 to TruthPunch(30, 2, "The Artificial Game",
            "Discipline is an artificial game played by those who lack genuine desire."),
        29 to TruthPunch(29, 2, "The Myth of the Path",
            "The 'path' is a myth sold to you by people who are just as lost as you are."),
        28 to TruthPunch(28, 2, "The Stalling Tactic",
            "There is no 'how'; asking 'how' is a stall tactic to ensure you never have to do it."),
        27 to TruthPunch(27, 2, "The Destructive Hope",
            "Hope is the most destructive force in the universe; it keeps you focused on a future that will never arrive."),
        26 to TruthPunch(26, 2, "The Identity of Struggle",
            "You are addicted to the struggle because the arrival would mean the end of your identity."),
        25 to TruthPunch(25, 2, "The Insurance Policy",
            "Religion is the insurance policy you buy because you are afraid of the dark."),
        24 to TruthPunch(24, 2, "The Delay of Method",
            "A method implies time, but truth is immediate; using a method is a way to delay the truth."),
        23 to TruthPunch(23, 2, "The Feared Destination",
            "The 'journey' does not matter; only the destination matters, and you are terrified to reach it."),
        22 to TruthPunch(22, 2, "The Drowning Chant",
            "You chant mantras to drown out the screaming of your own insecurity."),
        21 to TruthPunch(21, 2, "The Catastrophic Truth",
            "Transformations are catastrophic, not incremental; you cannot 'gradually' wake up."),
        
        // ===== TIER 1: THE ANNIHILATION (Existential Truths) =====
        20 to TruthPunch(20, 1, "The Obstacle of Seeking",
            "There is no self to realize; the one seeking realization is the only obstacle."),
        19 to TruthPunch(19, 1, "The Biological Machine",
            "You do not have a life; you are a biological machine processing reactions to stimuli, and you call this 'living.'"),
        18 to TruthPunch(18, 1, "The Fictitious Narrative",
            "The entity you call 'I' is a fictitious narrative created by thought to give continuity to a series of disjointed memories."),
        17 to TruthPunch(17, 1, "The Neurological Defect",
            "Consciousness is not a divine gift; it is a neurological defect that separates man from the perfection of his biological nature."),
        16 to TruthPunch(16, 1, "The Labeled Pleasure",
            "You are not looking for the truth; you are looking for a permanent state of pleasure that you have labeled 'truth.'"),
        15 to TruthPunch(15, 1, "The Constant Death",
            "Death is not a future event; you die every moment thought ceases, but you frantically resurrect yourself with noise."),
        14 to TruthPunch(14, 1, "The Invented Soul",
            "The soul is a concept invented by the frightened mind to ensure its own continuity after the body rots."),
        13 to TruthPunch(13, 1, "The Painted Ghost",
            "There is no inner 'you' to be improved; you are painting over a ghost."),
        12 to TruthPunch(12, 1, "The Corpse of Childhood",
            "Your 'inner child' is not lost; it is dead, and you are dragging its corpse around to justify your immaturity."),
        11 to TruthPunch(11, 1, "The Calamity",
            "Enlightenment is not a state of bliss; it is a calamity that destroys everything you currently value, including you."),
        10 to TruthPunch(10, 1, "The Reaction",
            "You exist only as a reaction; without a stimulus to provoke you, you are nothing."),
        9 to TruthPunch(9, 1, "The Graveyard Silence",
            "The silence you seek is the silence of the graveyard, yet you are terrified to enter it while alive."),
        8 to TruthPunch(8, 1, "The True Freedom",
            "Freedom is not the ability to do what you want; it is the freedom FROM the person you think you are."),
        7 to TruthPunch(7, 1, "The Ego's Trick",
            "There is no observer separate from the observed; the separation is a trick of language to maintain the ego."),
        6 to TruthPunch(6, 1, "The Stranger's Biography",
            "Your biography is a story you tell yourself to hide the fact that you are a stranger to yourself."),
        5 to TruthPunch(5, 1, "The Ultimate Entertainment",
            "The search for meaning is the ultimate entertainment for a mind terrified of its own emptiness."),
        4 to TruthPunch(4, 1, "The Serial Number",
            "You are not a unique individual; you are a culturally manufactured product with a serial number you call a name."),
        3 to TruthPunch(3, 1, "The Natural State",
            "The 'natural state' is not spiritual; it is a biological functioning where the 'you' does not exist to experience it."),
        2 to TruthPunch(2, 1, "The Ascent Illusion",
            "Awakening is not a spiritual ascent; it is the realization that there is no one to ascend."),
        1 to TruthPunch(1, 1, "The Final Truth",
            "There is nothing inside you but the noise of the world you have swallowed.")
    )
    
    /**
     * Check and unlock truths based on current lifetime Spiritual Ego
     * Returns list of newly unlocked truths
     */
    fun checkUnlocks(lifetimeSpiritualEgo: Long): List<TruthPunch> {
        val newlyUnlocked = mutableListOf<TruthPunch>()
        
        for ((rank, threshold) in UNLOCK_THRESHOLDS) {
            if (lifetimeSpiritualEgo >= threshold && rank < highestUnlockedRank) {
                // This rank is now unlocked!
                highestUnlockedRank = rank
                allTruths[rank]?.let { truth ->
                    newlyUnlocked.add(truth.copy(isUnlocked = true))
                }
            }
        }
        
        return newlyUnlocked.sortedByDescending { it.rank }
    }
    
    /**
     * Get all truths with their current unlock/seen status
     */
    fun getAllTruths(): List<TruthPunch> {
        val seen = seenTruths
        return allTruths.values.map { truth ->
            truth.copy(
                isUnlocked = truth.rank >= highestUnlockedRank,
                isSeen = seen.contains(truth.rank)
            )
        }.sortedByDescending { it.rank }  // Show easiest first (100 -> 1)
    }
    
    /**
     * Get only unlocked truths
     */
    fun getUnlockedTruths(): List<TruthPunch> {
        return getAllTruths().filter { it.isUnlocked }
    }
    
    /**
     * Get the next truth to be unlocked
     */
    fun getNextTruth(): TruthPunch? {
        val nextRank = highestUnlockedRank - 1
        return if (nextRank >= 1) allTruths[nextRank] else null
    }
    
    /**
     * Get Spiritual Ego required for next unlock
     */
    fun getNextUnlockThreshold(): Long {
        val nextRank = highestUnlockedRank - 1
        return if (nextRank >= 1) UNLOCK_THRESHOLDS[nextRank] ?: 0L else Long.MAX_VALUE
    }
    
    /**
     * Mark a truth as seen (dismisses "NEW" badge)
     */
    fun markAsSeen(rank: Int) {
        val seen = seenTruths
        seen.add(rank)
        seenTruths = seen
    }
    
    /**
     * Get count of unseen unlocked truths (for notification badge)
     */
    fun getUnseenCount(): Int {
        val seen = seenTruths
        return getAllTruths().count { it.isUnlocked && !seen.contains(it.rank) }
    }
    
    /**
     * Get a specific truth by rank
     */
    fun getTruth(rank: Int): TruthPunch? {
        return allTruths[rank]?.copy(
            isUnlocked = rank >= highestUnlockedRank,
            isSeen = seenTruths.contains(rank)
        )
    }
    
    /**
     * Get tier name for display
     */
    fun getTierName(tier: Int): String {
        return when (tier) {
            5 -> "The Operational Lies"
            4 -> "The Hollow Pursuit"
            3 -> "The Transactional Heart"
            2 -> "The Spiritual Scam"
            1 -> "The Annihilation"
            else -> "Unknown Tier"
        }
    }
    
    /**
     * Get tier subtitle for display
     */
    fun getTierSubtitle(tier: Int): String {
        return when (tier) {
            5 -> "Society and Behavior"
            4 -> "Ambition and Success"
            3 -> "Love and Charity"
            2 -> "Seeking and Methods"
            1 -> "Existential Truths"
            else -> ""
        }
    }
    
    /**
     * Get progress percentage for current tier
     */
    fun getTierProgress(tier: Int): Float {
        val tierTruths = getAllTruths().filter { it.tier == tier }
        val unlockedCount = tierTruths.count { it.isUnlocked }
        return if (tierTruths.isNotEmpty()) unlockedCount.toFloat() / tierTruths.size else 0f
    }
    
    /**
     * Get total unlock progress (0-100)
     */
    fun getOverallProgress(): Float {
        val unlockedCount = getAllTruths().count { it.isUnlocked }
        return (unlockedCount.toFloat() / 100f) * 100f
    }
}

