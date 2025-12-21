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
        // ADJUSTED: First truths unlock faster to hook players early
        // With new progression: ~50 SE/min base, players unlock frequently
        private val UNLOCK_THRESHOLDS = mapOf(
            100 to 0L,           // Truth 100 unlocked at start
            99 to 50L,           // ~50 sec of meditation
            98 to 120L,          // ~2 min total
            97 to 200L,          // ~3 min
            96 to 300L,          // ~4 min
            95 to 450L,          // ~5 min
            94 to 650L,          // ~6 min
            93 to 900L,          // ~7 min
            92 to 1_200L,        // ~8 min
            91 to 1_600L,        // ~9 min
            90 to 2_100L,        // ~10 min (first 10 truths in 10 min!)
            89 to 2_700L,        // ~11 min
            88 to 3_400L,        // ~12 min
            87 to 4_200L,        // ~13 min
            86 to 5_200L,        // ~14 min
            85 to 6_400L,        // ~15 min
            84 to 7_800L,        // ~16 min
            83 to 9_500L,        // ~17 min
            82 to 11_500L,       // ~18 min
            81 to 14_000L,       // ~19 min (all Tier 5 in ~20 min)
            // Tier 4 (Rank 61-80) - Medium progression
            80 to 17_000L,
            79 to 21_000L,
            78 to 26_000L,
            77 to 32_000L,
            76 to 40_000L,
            75 to 50_000L,
            74 to 62_000L,
            73 to 77_000L,
            72 to 95_000L,
            71 to 118_000L,
            70 to 145_000L,
            69 to 180_000L,
            68 to 220_000L,
            67 to 275_000L,
            66 to 340_000L,
            65 to 420_000L,
            64 to 520_000L,
            63 to 640_000L,
            62 to 790_000L,
            61 to 980_000L,
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

        /**
         * The 100 Levels of Spiritual Disillusionment
         * Mapped to Truths Unlocked (Rank 100 -> Level 1, Rank 1 -> Level 100)
         */
        val LEVEL_NAMES = mapOf(
            1 to "The Sleeper",
            2 to "Dust Watcher",
            3 to "Silence Cadet",
            4 to "Breath Counter",
            5 to "Mat Dweller",
            6 to "Incense Burner",
            7 to "Gong Striker",
            8 to "Mantra Mumbler",
            9 to "Lotus Sitter",
            10 to "Ego Polisher",
            11 to "Thought Watcher",
            12 to "Shadow Boxer",
            13 to "Dream Walker",
            14 to "Pattern Seeker",
            15 to "Mask Wearer",
            16 to "Mirror Gazer",
            17 to "Silence Hunter",
            18 to "Noise Filter",
            19 to "Draft Blocker",
            20 to "Mist Gatherer",
            21 to "Spark Catcher",
            22 to "Flame Keeper",
            23 to "Light Bender",
            24 to "Time Waster",
            25 to "Void Starer",
            26 to "Self Doubter",
            27 to "Truth Dodger",
            28 to "Bliss Chaser",
            29 to "Comfort Killer",
            30 to "Pain Student",
            31 to "Fear Taster",
            32 to "Edge Walker",
            33 to "Abyss Surfer",
            34 to "Ghost Whisperer",
            35 to "Chain Breaker",
            36 to "Cage Rattler",
            37 to "Lock Picker",
            38 to "Wall Climber",
            39 to "Gate Keeper",
            40 to "Path Finder",
            41 to "Map Burner",
            42 to "Compass Breaker",
            43 to "Star Navigator",
            44 to "Night Warden",
            45 to "Dawn Bringer",
            46 to "Sun Eater",
            47 to "Moon Walker",
            48 to "Tide Turner",
            49 to "Wave Rider",
            50 to "Storm Center",
            51 to "Cloud Piercer",
            52 to "Sky Painter",
            53 to "Wind Speaker",
            54 to "Rain Dancer",
            55 to "Thunder Caller",
            56 to "Lightning Rod",
            57 to "Earth Mover",
            58 to "Stone Singer",
            59 to "River Guide",
            60 to "Ocean Drinker",
            61 to "Fire Walker",
            62 to "Ash Sweeper",
            63 to "Phoenix Feeder",
            64 to "Dragon Tamer",
            65 to "Serpent Charmer",
            66 to "Lion Heart",
            67 to "Eagle Eye",
            68 to "Owl Ear",
            69 to "Wolf Runner",
            70 to "Bear Sleeper",
            71 to "Tree Talker",
            72 to "Root Finder",
            73 to "Leaf Turner",
            74 to "Seed Planter",
            75 to "Forest Ranger",
            76 to "Jungle Guide",
            77 to "Desert Mystic",
            78 to "Oasis Builder",
            79 to "Mirage Maker",
            80 to "Sand Shifter",
            81 to "Mountain Scaler",
            82 to "Peak Viewer",
            83 to "Valley Dweller",
            84 to "Echo Creator",
            85 to "Cave Lighter",
            86 to "Depth Sounder",
            87 to "Core Diver",
            88 to "Magma Surfer",
            89 to "Plate Shifter",
            90 to "World Spinner",
            91 to "Galaxy Viewer",
            92 to "Nebula Drifter",
            93 to "Star Forger",
            94 to "Black Hole Surfer",
            95 to "Event Horizon",
            96 to "Singularity",
            97 to "Quantum Ghost",
            98 to "Reality Hacker",
            99 to "Code Breaker",
            100 to "The Nobody"
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
        val article: String = "", // Expanded mini-article elaborating the truth
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
            "The mind is a storytelling machine that never shuts up, and you believe every lie it tells.",
            """Your mind never stops talking. From the moment you wake up until you fall asleep, it narrates, judges, predicts, and worries. It tells you stories about who you are, what others think of you, what might go wrong tomorrow.

Here is the uncomfortable truth: you have never questioned whether these stories are true. You simply believe them. The mind says "I am not good enough" and you feel inadequate. It says "they are judging me" and you feel shame. It says "something bad will happen" and you feel fear.

But these are just stories. They are not reality. They are interpretations, assumptions, fabrications. The mind is not reporting facts—it is spinning fiction and calling it truth.

Notice this: the storyteller and the one who believes the story are the same entity. The mind creates the drama and then gets lost in it. This is the trap you live in every single day."""),
        
        99 to TruthPunch(99, 5, "The Addiction to Suffering",
            "You are addicted to your own suffering because it is the only thing that makes you feel alive.",
            """Pay attention to how you relate to your problems. Notice how you return to them again and again, like pressing on a bruise. You complain about your suffering, yet something in you clings to it.

Why? Because suffering gives you a sense of identity. Your wounds, your grievances, your struggles—they make you feel like somebody. Without your problems, who would you be? This question terrifies you more than the suffering itself.

Watch how you reject peace when it comes. When things are quiet, you manufacture drama. When solutions appear, you find new problems. The mind is uncomfortable with stillness because stillness threatens its existence.

You are not a victim of suffering. You are its devoted customer, returning daily to purchase more of what hurts you."""),
        
        98 to TruthPunch(98, 5, "The Tantrum of Expectation",
            "Anger is just a tantrum you throw when the world doesn't follow the script you wrote in your head.",
            """Every flash of anger reveals the same pattern: reality did not match your expectation. Someone did not behave as you thought they should. Something did not happen as you planned. Life refused to follow your script.

And so you rage. Like a child whose toy was taken, you throw a tantrum at the universe for not obeying your demands. But consider this: who wrote this script? Who decided how things "should" be?

You did. You created an imaginary version of reality in your mind and then demanded that actual reality conform to it. When it didn't, you called that injustice. But the only injustice is your expectation that the universe should care about your preferences.

Every moment of anger is a moment of insanity—a moment when you genuinely believe the world should rearrange itself to match your imagination."""),
        
        97 to TruthPunch(97, 5, "The Ghost of Tomorrow",
            "Fear is merely the anticipation of pain that hasn't happened yet; you are haunting yourself.",
            """Most of what you fear exists only in your imagination. The disaster you dread, the rejection you anticipate, the failure you expect—none of it is happening now. It lives only in your mental projections of tomorrow.

You are being terrorized by ghosts of your own creation. The mind takes a small possibility and inflates it into a certainty. It rehearses catastrophe until your body responds as if the tragedy is already occurring. Your heart races, your stomach knots, your peace dissolves—all for something that may never happen.

Look at your history of fear. How many of your worst fears actually materialized? And of those that did, how many were as terrible as you imagined? You have spent your life running from shadows.

The future does not exist. Fear is the present moment activity of torturing yourself with imagination."""),
        
        96 to TruthPunch(96, 5, "The Vessel of Noise",
            "You do not have thoughts; thoughts have you. You are a vessel for noise.",
            """You believe you are the one thinking, but observe more carefully. Did you choose your next thought? Can you decide what will arise in your mind in the next moment? You cannot. Thoughts appear uninvited, unbidden, relentlessly.

You are not the thinker. You are the space in which thinking happens. But you have identified so completely with the noise that you believe you are generating it. This is like a radio believing it is creating the music.

The thoughts that fill your mind are not original. They are recycled fears, borrowed opinions, conditioned responses. You are a vessel filled with the noise of your culture, your upbringing, your environment.

What you call "my thoughts" are simply the thoughts that happen to be passing through. You have no more ownership of them than you have of the weather."""),
        
        95 to TruthPunch(95, 5, "The Withdrawal from Stimulation",
            "Boredom is the mind's withdrawal symptom from the drug of constant stimulation.",
            """Notice what happens when nothing is happening. When there is no screen to look at, no conversation to have, no task to complete—what arises? Restlessness. Agitation. An urgent need to find something, anything, to fill the void.

This is boredom, and it is not what you think it is. It is not a lack of interesting things in the world. It is the mind's addiction to stimulation going unsatisfied. You have trained yourself to require constant input, and now you cannot tolerate its absence.

Like any addict denied their substance, you become uncomfortable in the presence of nothing. You reach for your phone not because you need it, but because you cannot bear the emptiness of an un-stimulated moment.

Boredom is not a problem to solve with more stimulation. It is a symptom to observe—the mind revealing its dependency."""),
        
        94 to TruthPunch(94, 5, "The Slave of Preference",
            "You are a slave to your preferences; you like what you were trained to like.",
            """You believe your preferences are yours—unique expressions of your authentic self. You like certain foods, certain music, certain people. These likes feel personal, chosen, real.

But trace any preference back to its origin. Why do you like what you like? Because you were exposed to it. Because your culture praised it. Because people you wanted to please enjoyed it. Your preferences are not discoveries of your true nature—they are the residue of your conditioning.

You did not choose your favorite food as a baby. You did not select your native language. You did not pick your initial values. All of this was installed in you before you had any choice. And now you walk around defending these installations as if they were sacred truths about who you are.

You are not expressing yourself through your preferences. You are expressing your programming."""),
        
        93 to TruthPunch(93, 5, "The Echo Chamber",
            "Your opinions are just regurgitated noise you heard from someone else.",
            """Consider any strongly held opinion you have. Now ask: where did it come from? If you are honest, you will find that you heard it somewhere. From a parent, a teacher, a friend, a book, a screen. You adopted it and called it your own.

The thoughts you are most certain about are precisely the ones you have questioned least. They feel like bedrock truth only because they have been repeated so often—by others, by yourself—that questioning them seems absurd.

But you are not thinking. You are echoing. The voice in your head speaking your opinions is a recording of other voices. You have become a speaker playing back sounds you absorbed, believing you are the original source.

Original thought is rare to the point of being nearly nonexistent. What you call thinking is mostly a remix of borrowed material."""),
        
        92 to TruthPunch(92, 5, "The Twin of Pain",
            "Pleasure is the beginning of pain; you cannot have one without the other.",
            """Every pleasure carries its pain already within it. The delicious meal ends, and you want more. The exciting experience fades, and you miss it. The person you love can be lost.

This is not pessimism—it is observation. Pleasure and pain are not opposites but twins, born together, inseparable. The higher the high, the lower the low. The greater the attachment, the deeper the eventual loss.

You chase pleasure believing it will save you from pain, but this is like running toward a fire to escape the heat. The pleasure itself creates the conditions for suffering. It raises expectations that reality cannot sustain. It creates attachments that will be broken.

True peace is not found in maximizing pleasure but in seeing clearly that pleasure and pain arise together and pass together."""),
        
        91 to TruthPunch(91, 5, "The Waiting Silence",
            "You only listen to reply, never to understand; your silence is just waiting for your turn.",
            """Watch yourself in conversation. While the other person speaks, where is your attention? You are not listening. You are preparing your response. You are waiting for an opening. You are thinking about what you will say next.

Your silence is not receptivity—it is strategy. You appear to be present, but you have already retreated into your own mind, constructing replies, planning arguments, forming judgments.

This is not communication. It is two monologues taking turns. Two people speaking at each other, neither truly hearing, both desperate to be heard. You call this connection, but it is parallel isolation.

Real listening requires the suspension of self. It means temporarily abandoning your agenda, your opinions, your need to respond cleverly. This almost never happens."""),
        90 to TruthPunch(90, 5, "The Booby Prize",
            "Intellectual understanding is the booby prize; knowing 'about' the truth changes nothing.",
            """You have read many books. You can explain concepts eloquently. You understand the ideas, the frameworks, the philosophies. But has any of this knowledge changed you in any fundamental way?

Intellectual understanding is a consolation prize. It gives you the feeling of progress without actual movement. You can describe freedom while remaining imprisoned. You can articulate peace while being consumed by anxiety.

The mind loves to collect understanding because understanding poses no threat to it. You can "understand" that the ego is an illusion while your ego remains fully intact. Knowledge becomes another decoration in your mental museum.

Real change is not intellectual. It does not come from adding more information to your head. It comes from something else entirely—something that cannot be captured in words or concepts."""),
        
        89 to TruthPunch(89, 5, "The Terrifying Emptiness",
            "You are constantly running away from the present moment because it is empty and terrifying.",
            """Notice how rarely you are actually here. Your body sits in this room, but your mind is replaying yesterday or rehearsing tomorrow. You are almost never present to what is happening now.

Why? Because the present moment, stripped of all mental commentary, is empty. There is no story here. There is no past to chew on, no future to worry about. There is just this—and this is nothing special. This emptiness is unbearable to the mind that needs constant content.

So you flee. Into memories, into plans, into fantasies, into your phone. Anything to avoid the simple fact of being here with nothing happening. The entire structure of your mental life is a flight from now.

This moment, right now, is the only thing that exists. And you cannot stand it."""),
        
        88 to TruthPunch(88, 5, "The Lazy Habit",
            "Your 'values' are just habits you are too lazy to question.",
            """You speak of your values as if they were sacred principles you arrived at through careful deliberation. You hold them up as evidence of your character, your depth, your integrity.

But when did you actually choose them? You absorbed them from your environment before you could think critically. Your parents believed certain things. Your culture rewarded certain behaviors. Your peer group enforced certain norms. You swallowed all of this and called it your values.

These values are not principles—they are habits. You hold them not because you have examined them deeply but because examining them would be uncomfortable. It would require admitting that much of what you think you believe was simply installed in you.

True values would require constant questioning. What you have are comfortable assumptions protected by the label of morality."""),
        
        87 to TruthPunch(87, 5, "The Weapon of Guilt",
            "Guilt is a weapon you use to punish yourself so you can feel 'moral' while staying the same.",
            """Guilt feels like a virtue. When you feel guilty about something, you believe this proves you are a good person who knows right from wrong. The guilt itself becomes evidence of your moral character.

But notice what guilt actually does. It punishes you without changing you. You feel bad about your behavior, and this feeling of badness substitutes for actual transformation. You suffer, and the suffering satisfies some internal requirement for justice.

Meanwhile, nothing changes. The behavior continues. The pattern repeats. But you have paid your psychological tax through guilt, so you feel entitled to continue as before. Guilt is not a corrective—it is a bribe you pay yourself to maintain the status quo.

If guilt worked, you would not need to feel it twice about the same thing."""),
        
        86 to TruthPunch(86, 5, "The Biological Robot",
            "You are a biological robot programmed to seek survival and reproduction; there is no ghost in the machine.",
            """Strip away the poetry and the philosophy, and what remains? A biological organism doing what biological organisms do: seeking food, avoiding pain, pursuing pleasure, trying to reproduce. Every noble motivation can be traced to these basic drives.

Your love is chemistry. Your ambition is status-seeking. Your creativity is display behavior. Your spirituality is the organism trying to make sense of its own existence. There is no separate "you" pulling the strings—there are just processes running their course.

This is not cynicism. It is observation. The organism you call "you" was assembled from genetic instructions and environmental inputs. It runs programs installed by evolution over millions of years. What you call free will is the organism's inability to see its own programming.

There is no ghost in the machine. There is only the machine, dreaming that it is more than mechanical."""),
        
        85 to TruthPunch(85, 5, "The Self-Centered Sacrifice",
            "Everything you do is self-centered, even your self-sacrifice and your charity.",
            """When you sacrifice for others, notice the subtle satisfaction. When you give to charity, observe the warm feeling. When you put yourself last, see the secret pride in your humility.

There is no truly selfless act. Even your most generous impulses serve the self. You help others because it makes you feel good about who you are. You sacrifice because the identity of the sacrificer is more valuable to you than what you give up.

This is not an accusation—it is simply how the organism works. The self is always present, always calculating, always extracting some benefit. Even when that benefit is just the pleasure of believing itself to be unselfish.

The only honest position is to admit this. You are not doing good for others. You are doing good for your image of yourself."""),
        
        84 to TruthPunch(84, 5, "The Defended Prison",
            "You defend your limitations because they are familiar and safe.",
            """Watch how you respond when someone threatens your limitations. When someone suggests you could do something you believe you cannot, what arises? Resistance. Argument. Evidence for why you are right to be limited.

You are attached to your cage. It is small, but it is known. Your limitations define you—they tell you who you are and, more importantly, who you are not. If you released these limitations, you would not know where you end and possibilities begin.

So you defend them. You explain why you cannot change. You point to your past as proof of your frozen future. You make your prison comfortable and call it home.

The walls are not keeping the world out. They are keeping you in. And you are the one maintaining them."""),
        
        83 to TruthPunch(83, 5, "The Problem Creator",
            "The mind cannot solve the problems it created; it IS the problem.",
            """You believe your mind is the tool for solving your problems. You think harder, analyze deeper, strategize more cleverly. But have you noticed that the problems never end? For every one you solve, two more appear.

This is because the mind creates problems in order to solve them. Problem-solving is what gives the mind its sense of purpose, its reason for existing. A mind without problems would have nothing to do.

The very act of thinking about a problem reinforces its existence. The more attention you give it, the more real it becomes. The mind does not want resolution—it wants perpetual engagement.

The problem is not out there in your life. The problem is the mental machinery that perceives everything as a problem to be fixed."""),
        
        82 to TruthPunch(82, 5, "The Sleepwalk",
            "You are sleepwalking through a nightmare you call 'normal life.'",
            """Consider what you accept as normal. Waking to an alarm. Spending most of your waking hours doing things you would not choose to do. Waiting for weekends. Waiting for vacations. Waiting for retirement. Waiting for life to start.

This is what you call living. This is what everyone around you calls living. You have normalized a state of low-grade suffering and called it reality. The nightmare is not that life is hard—it is that you do not see what you are doing.

You walk through your days in a trance, following scripts written by others, playing roles nobody assigned. You call this responsibility. You call this maturity. You call this the way things are.

Wake up. Not to a better dream, but to the fact that you are dreaming."""),
        
        81 to TruthPunch(81, 5, "The Portable Prison",
            "There is no escape; wherever you go, you bring your noise with you.",
            """You believe a change in circumstances will bring peace. A new job, a new city, a new relationship. If only the external conditions were different, you would finally be free.

But have you noticed the pattern? You make the change, and for a brief moment there is relief. Then slowly, the same dissatisfaction returns. The same anxiety. The same problems with different faces.

This is because you are the common factor in every situation of your life. Wherever you go, you bring your mind with you. Your habitual thoughts, your automatic reactions, your persistent patterns—they travel in your luggage.

There is no geographical cure for what ails you. There is no circumstantial solution to a problem that exists in consciousness itself."""),
        
        // ===== TIER 4: THE HOLLOW PURSUIT (Ambition and Success) =====
        80 to TruthPunch(80, 4, "The Byproduct Illusion",
            "Success is a byproduct, not a goal; if you are chasing it, you have already missed it.",
            """The people who achieve remarkable things are not chasing success. They are consumed by what they are doing. The success—if it comes—arrives as a side effect, almost unnoticed.

You have it backwards. You want the success so you can feel good, so you can prove something, so you can finally relax. But this orientation guarantees failure because your attention is on the reward, not the work.

The moment you make success your goal, you corrupt the process. Every action becomes a calculation. Every effort becomes a transaction. You lose the very quality that produces excellence: total absorption in what you are doing.

Success cannot be pursued. It must ensue from something else entirely—something that has nothing to do with success at all."""),
        
        79 to TruthPunch(79, 4, "The Beggar's Applause",
            "You want the applause, not the craft; you are a beggar seeking validation from other beggars.",
            """Be honest about what you really want. Is it to do excellent work, or is it to be seen doing excellent work? Is it mastery, or is it recognition? For most, it is the latter disguised as the former.

You perform for an audience that does not care about quality—they care about entertainment. You seek approval from people who themselves are seeking approval. Everyone is begging for validation from other beggars, and no one has anything real to give.

The true artist works in obscurity, not as a sacrifice but because they are too absorbed in the work to need witnesses. The work is enough. But for you, the work is never enough. The work is just a means to get the applause.

If the validation disappeared, would you still do what you do?"""),
        
        78 to TruthPunch(78, 4, "The Consolation Prize",
            "Hard work is the consolation prize for those who lack the talent to be effortless.",
            """We celebrate hard work because it is democratic. Anyone can work hard. It requires no special gift, no rare ability. And so we have made effort itself virtuous, regardless of what it produces.

But look at mastery when it appears. It is effortless. The master makes the difficult look easy. Their excellence flows without visible strain. This is what we call talent—and it is not earned through hard work. It is something else entirely.

Hard work is what you fall back on when you lack this natural alignment between self and skill. You grind because you cannot glide. This is not shameful, but it should not be celebrated either.

The goal is not to work harder. The goal is to find work that does not feel like work at all."""),
        
        77 to TruthPunch(77, 4, "The Acceptable Greed",
            "Ambition is the socially acceptable way to be greedy and dissatisfied.",
            """Society rewards ambition. It praises those who want more, strive for more, achieve more. Ambition is considered a virtue, a sign of character, a marker of potential.

But what is ambition really? It is chronic dissatisfaction. It is the refusal to be content with what is. It is greed wearing respectable clothes.

The ambitious person is never here. They are always reaching for what they do not have. They cannot enjoy their success because their eyes are fixed on the next success. Ambition promises fulfillment tomorrow while stealing peace today.

This is greed with good public relations. You want more money, more status, more power, more recognition—and you call this drive a virtue."""),
        
        76 to TruthPunch(76, 4, "The Comfortable Trap",
            "You are where you are because, at some level, you are okay with being there.",
            """You complain about your situation, but you do not leave. You say you want change, but you take no action. You describe dreams, but you do not move toward them.

The uncomfortable truth is that you have chosen this. Not consciously, not explicitly, but through a thousand small decisions to stay comfortable rather than move into the unknown.

Your current life, with all its problems, offers something your dreams do not: safety. You know how to suffer here. You have mastered this particular form of dissatisfaction. The dream would require becoming someone you do not yet know how to be.

Until the pain of staying exceeds the fear of leaving, you will remain exactly where you are."""),
        
        75 to TruthPunch(75, 4, "The Unserious Question",
            "Asking for advice is an admission that you are not serious about the outcome.",
            """When you ask for advice, what are you really doing? You are transferring responsibility. If the advice works, you can feel validated. If it fails, you can blame the advisor. Either way, you are protected.

Someone who is truly serious does not ask. They research, they experiment, they fail, they adjust. They take full ownership of the outcome. Asking others is a delay tactic—a way to feel like you are doing something while avoiding the risk of actually deciding.

Most advice-seeking is not a search for wisdom. It is a search for permission. You want someone to tell you that what you already want to do is correct. You want validation, not guidance.

If you needed the advice, you would not be ready for it. If you do not need it, why are you asking?"""),
        
        74 to TruthPunch(74, 4, "The Myth of Potential",
            "The world does not care about your potential; it only cares about what you have actually done.",
            """Potential is a comfortable place to hide. As long as you have potential, you can feel special without having to prove anything. You can believe you could succeed without having to risk failure.

But potential is a debt that accrues interest. Every year you do not act, the story you tell yourself becomes less believable. The potential that felt so promising at twenty feels like an excuse at forty.

The world sees only results. Your intentions, your abilities, your unrealized greatness—none of this registers. What have you actually done? What exists in the world because you created it? This is the only measure that matters.

Potential is a promissory note that, unfulfilled, becomes worthless."""),
        
        73 to TruthPunch(73, 4, "The Manufactured Struggle",
            "You create problems to justify your existence; without a struggle, you would have no identity.",
            """Notice how you always have a problem. As soon as one is solved, another appears. Your life is a continuous series of obstacles to overcome, challenges to face, difficulties to navigate.

Is the world really so full of problems, or are you manufacturing them? The mind needs material to work with. An untroubled mind has nothing to do—and what would you be without your troubles?

Your struggles give you an identity. They make you interesting. They justify your complaints, your efforts, your very existence. Without problems, you would have to face the terrifying question: now what?

Peace, if it ever came, would feel like death to the one who knows themselves only through conflict."""),
        
        72 to TruthPunch(72, 4, "The Fear of Quality",
            "Society rewards mediocrity because the masses are terrified of true quality.",
            """True excellence is rare and disturbing. It exposes the mediocrity around it. It makes people uncomfortable because it reveals what is possible—and therefore what they have not achieved.

Society protects itself from this discomfort by celebrating the acceptable. It rewards those who are good enough but not too good. It promotes the competent over the excellent. It prefers the relatable to the remarkable.

The masses do not want greatness—they want comfort. They want to believe that their level is the level, that their normal is normal. True quality threatens this illusion and must be contained, dismissed, or ignored.

If you seek approval from such a society, you must pay the price of mediocrity."""),
        
        71 to TruthPunch(71, 4, "The Leadership Distraction",
            "Becoming a 'leader' is just a way to hide your own inability to follow your own truth.",
            """Notice how much energy goes into leading others. Organizing, motivating, strategizing, directing. It feels important, even noble. But what is it really about?

Leading others is often an escape from leading yourself. It is easier to tell others what to do than to face your own inner chaos. External control substitutes for internal direction. Managing a team becomes a distraction from managing your own life.

The true question is not whether you can lead others but whether you can follow your own truth without anyone watching. Can you direct yourself without an audience? Can you hold yourself accountable when there is no report to file?

Most leaders are followers in disguise—following their need for control, their fear of insignificance."""),
        70 to TruthPunch(70, 4, "The Absence of Hunger",
            "If you need motivation, you are not hungry enough; hunger needs no pep talk.",
            """The person who needs motivation is revealing something important: they do not want what they say they want. True desire does not require external fuel. It consumes you from within.

Watch someone genuinely hungry for something. They do not wait for inspiration. They do not read books on motivation. They do not make vision boards. They simply move—relentlessly, obsessively, without requiring anyone to cheer them on.

Your need for motivation is a symptom of half-hearted desire. You want the results without the genuine pull toward them. You want to want something, which is not the same as wanting it.

When hunger is real, you do not think about whether you feel like eating. You just eat."""),
        
        69 to TruthPunch(69, 4, "The Distraction Factory",
            "Your goals are just distractions to keep you from seeing the pointlessness of your life.",
            """Strip away all your goals. Remove the ambitions, the plans, the projects. What remains? A terrifying emptiness. And so you quickly fill it again with more goals.

Goals give life a sense of direction, of purpose, of meaning. But they are manufactured meaning. You made them up. You decided that achieving X would matter, and then you organized your life around that fiction.

The goals are not problematic because they are goals. They are problematic because they prevent you from ever stopping, from ever asking the deeper question: why am I doing any of this?

Your endless striving is not progress. It is avoidance of the void that waits when you stop striving."""),
        
        68 to TruthPunch(68, 4, "The Safe Surrender",
            "You settle for a salary because you are too afraid to find out what you are actually worth.",
            """A salary is a deal: you trade your time for a predictable amount of money. It seems fair, even generous. But look at what you are really purchasing.

You are buying certainty. You are purchasing the right to never know your true market value. You are avoiding the terror of being directly accountable to results.

The salary insulates you from reality. Someone else takes the risk, absorbs the fluctuations, handles the uncertainty. You are paid to show up, not to produce value. And in exchange for this safety, you accept a permanent cap on your worth.

Your salary is not what you are worth. It is what you are willing to accept to avoid finding out."""),
        
        67 to TruthPunch(67, 4, "The Violence of Comparison",
            "Comparing yourself to others is violence against your own nature.",
            """Every comparison is an act of aggression against yourself. You take your actual life and measure it against someone else's highlight reel. You take your internal experience and compare it to someone else's external appearance. This can only produce suffering.

But the deeper violence is this: comparison assumes you should be someone other than who you are. It denies the validity of your own nature, your own path, your own timing. It says that who you are is not enough.

Why would you want someone else's life? You do not know what it costs them. You do not know what they have sacrificed. You only see the parts they want you to see.

Comparison is the mind refusing to accept reality as it is."""),
        
        66 to TruthPunch(66, 4, "The Performance for Nobody",
            "The 'hustle' is a dance you perform to impress people who do not matter.",
            """Watch the hustle culture. Everyone broadcasting their early mornings, their packed schedules, their exhausting workloads. Who is this performance for?

Not for the work itself—that would not require an audience. Not for genuine achievement—that rarely needs documentation. It is for the approval of strangers, for the envy of peers, for the identity of being a hustler.

The tragic part: the people you are trying to impress are too busy with their own performance to watch yours. Everyone is dancing for an audience that is not looking. Everyone is signaling to people who do not care.

The hustle is not a path to success. It is a public ritual of self-importance."""),
        
        65 to TruthPunch(65, 4, "The Obsession Requirement",
            "True mastery requires the obsession of a maniac, not the 'balance' of a hobbyist.",
            """We celebrate balance as if it were a virtue. Work-life balance. Moderation. Well-roundedness. But look at anyone who has achieved mastery in any field. They are not balanced. They are obsessed.

The master violates all the rules of healthy living. They sacrifice relationships, sleep, comfort, normalcy. They are consumed by their craft to an extent that looks pathological to the balanced observer.

Balance is what average people do. It produces average results. If you want exceptional outcomes, you must become unbalanced—tilted so far in one direction that everything else falls away.

The question is not whether this is healthy. The question is whether you want mastery or comfort."""),
        
        64 to TruthPunch(64, 4, "The Waiting Room",
            "You are not waiting for the right moment; you are waiting for death to save you from having to try.",
            """You say you are waiting for the right time. When circumstances align. When you have enough money, enough skills, enough confidence. You have been waiting for years.

This is not patience. This is fear dressed up as prudence. You are waiting because trying would mean risking failure. And failure would destroy the story you tell yourself about your potential.

Secretly, you are hoping that death arrives before you have to make the attempt. That time runs out before your excuses do. That you can go to your grave still believing you could have succeeded if only you had tried.

The right moment is not coming. There is only this moment, and you are wasting it."""),
        
        63 to TruthPunch(63, 4, "The Fear of Satisfaction",
            "Satisfaction is the death of desire, and you are terrified of being satisfied.",
            """Watch what happens when you get what you want. There is a brief moment of pleasure, and then immediately anxiety arises: what now? What next? What do I pursue now that this is achieved?

You are not capable of resting in satisfaction. It feels like death to you—the death of forward motion, the death of purpose, the death of identity. Without desire, who are you?

So you ensure that satisfaction never fully arrives. You move the goalpost. You focus on what is still missing. You find flaws in what you have achieved. Anything to reignite the hunger.

Your entire life is organized around wanting, and you will do anything to avoid the moment when wanting stops."""),
        
        62 to TruthPunch(62, 4, "The Wealthy Misery",
            "The rich man is just as miserable as the poor man; he just has better toys to distract him.",
            """Money solves money problems. It is excellent for that. But have you noticed that the wealthy are not proportionally happier? Their lives are filled with anxiety, conflict, and dissatisfaction—just luxury versions.

The rich man worries about different things, but he still worries. He still fears, still craves, still suffers. The quality of his distraction has improved, but distraction is all it remains.

This is not an argument against wealth. It is an observation: if you think money will bring peace, you are chasing an illusion. The poor man imagines the rich man is free. The rich man knows he is not.

Suffering is not a function of circumstance. It is a function of mind."""),
        
        61 to TruthPunch(61, 4, "The Vanity of Legacy",
            "Legacy is a vanity project for the dead; you will not be there to enjoy it.",
            """You work to leave a legacy, to be remembered after you are gone. But consider: you will not be there to experience being remembered. You will be dead. What good is the memory of you if you cannot witness it?

Legacy is a consolation prize for mortality. It is the ego's attempt to extend itself beyond death by proxy. It is the fantasy that you will somehow continue through the impact you leave behind.

But you will not. You will end. And the memory of you will fade, distort, eventually disappear entirely. This is not sad—it is simply true.

Working for legacy is working for an audience that will never include you."""),
        
        // ===== TIER 3: THE TRANSACTIONAL HEART (Love and Charity) =====
        60 to TruthPunch(60, 3, "The Mirror of Self",
            "You do not love others; you love how others make you feel about yourself.",
            """Watch closely what happens when you "love" someone. What are you actually experiencing? You are experiencing yourself—the feelings they evoke in you, the image of yourself reflected in their eyes.

When you say you love someone, you are really saying: I love how I feel when I am with you. I love who I am when you look at me. I love the version of myself that exists in your presence.

This is not love—it is sophisticated self-absorption. The other person is a mirror you use to admire yourself. When the mirror stops reflecting what you want to see, suddenly the "love" fades.

Real connection, if it exists at all, would not be about what you get. It would not be about feeling good. It would not be about you at all."""),
        
        59 to TruthPunch(59, 3, "The Empty Pockets",
            "Two beggars pulling at each other's empty pockets—this is what you call a 'relationship.'",
            """Look at what happens in most relationships. Two people, both incomplete, try to extract from each other what they lack in themselves. "I need you" is the hidden foundation of the whole arrangement.

You are a beggar. You approach the relationship wanting to receive—love, security, validation, meaning. The other person is also a beggar, wanting the same. Two empty vessels trying to fill each other.

But neither has anything to give. What passes for giving is really bargaining: I give you this so you will give me that. The entire relationship is a transaction between people who have nothing real to exchange.

When both beggars realize the other's pockets are empty, the relationship collapses."""),
        
        58 to TruthPunch(58, 3, "The Division of Love",
            "Love implies division: the one who loves and the one who is loved. This is conflict, not union.",
            """The word "love" contains a hidden assumption: there is a lover and a beloved. There is you making the effort to love and another receiving your love. Two separate entities reaching toward each other.

But this is not union. This is relationship—and relationship means distance. You cannot relate to something you are one with. The very act of loving confirms separation.

Watch the mechanics of your love. You project onto another. You desire from another. You reach toward another. Every movement reinforces the gap between you.

True merging would end the lover and the beloved. There would be no one left to love and no one left to be loved. What remains would not be called love at all."""),
        
        57 to TruthPunch(57, 3, "The Purchase of Goodness",
            "You give charity only to purchase a flattering image of yourself as a 'good person.'",
            """When you give to charity, observe what happens inside. Watch the warm glow of self-satisfaction. Notice how good you feel about yourself afterward. This is what you were really purchasing.

The money goes to others, but the benefit comes right back to you. You have exchanged dollars for a positive self-image. The poor receive your payment, but you receive something more valuable: the identity of a generous person.

If giving truly cost you something—if it genuinely hurt without any psychological compensation—you would not do it. Every act of generosity is calculated, even when the calculation is unconscious.

You are not giving to help others. You are investing in your own self-esteem."""),
        
        56 to TruthPunch(56, 3, "The Leash of Need",
            "Attachment is not love; it is a leash you place on another to ensure they service your needs.",
            """You say you are attached because you love. But look more closely. Attachment says: I need you. I cannot be okay without you. You must stay because I depend on you.

This is not love—it is ownership. The attachment is a leash that limits the other person's freedom to ensure they remain available to you. Their departing would disturb your peace, so you must keep them close.

Real care would want the other person's growth, even if that growth took them away from you. Real care would release its grip. But attachment tightens its grip because attachment is about your needs, not their wellbeing.

When you are attached, you are using another human being as a utility."""),
        
        55 to TruthPunch(55, 3, "The Fear of Solitude",
            "When you say 'I love you,' you are really saying, 'Do not leave me alone with myself.'",
            """Strip the romance from the declaration and see what remains. "I love you" is often a plea disguised as an offering. What you are really saying is: please stay. Please do not abandon me to my own company.

You are terrified of being alone with yourself. Your own thoughts, your own emptiness, your own unresolved pain—these are unbearable. So you seek constant companionship, calling it love.

The other person becomes a buffer between you and your aloneness. As long as they are there, you do not have to face yourself. Their presence fills the void you cannot bear to confront.

Many relationships are just elaborate avoidance strategies dressed up in romantic language."""),
        
        54 to TruthPunch(54, 3, "The Chain of Responsibility",
            "Responsibility is a social chain forged to keep you enslaved to people you secretly resent.",
            """Consider your responsibilities. Your obligations to family, to society, to those who depend on you. These feel sacred, unquestionable. But have you noticed the resentment underneath?

You did not choose many of these responsibilities. They were imposed on you by circumstance, by expectation, by guilt. And now they bind you to people and situations you would not freely choose.

The resentment is the proof. If these responsibilities were genuinely yours—if they arose from your deepest truth—there would be no resentment. But the chain chafes because it was placed there by others.

You call it duty. You call it love. But in quiet moments, you feel the weight of the servitude."""),
        
        53 to TruthPunch(53, 3, "The Guilt Contract",
            "You owe your children nothing, and they owe you nothing; everything else is a guilt contract.",
            """The relationship between parents and children is drowning in guilt. "I sacrificed for you, so you must take care of me." "I gave you life, so you owe me respect." These are transactions, not love.

Children did not ask to be born. Parents chose to create them. Any debt parents claim is a debt they invented and imposed. Any payment children make is extracted through guilt, not earned through genuine relationship.

Watch how families use guilt as currency. Watch how obligations are weaponized. Watch how love becomes conditional on meeting expectations that were never agreed upon.

A truly free relationship would hold no accounts, demand no repayment, apply no pressure."""),
        
        52 to TruthPunch(52, 3, "The Superior Helper",
            "Helping others is a selfish act designed to assert your superiority and assuage your guilt.",
            """Notice the position you occupy when you help someone. You are above them. They are in need; you have resources. They are weak; you are strong. The helper is always in the superior position.

This is not accidental. You help, in part, because it feels good to be in that position. It confirms your adequacy, your worth, your surplus. The other person's need highlights your having.

And there is guilt too. You have more than you need while others suffer. Helping a little relieves the guilt without requiring you to actually change anything fundamental. It is a psychological tax that keeps the system intact.

Pure help—help without any benefit to the helper—may not exist at all."""),
        
        51 to TruthPunch(51, 3, "The Honest Jealousy",
            "Jealousy is the only honest emotion in your relationships; it reveals your ownership.",
            """We hide jealousy. We consider it ugly, immature, unspiritual. But jealousy is more honest than most of what we show in relationships.

When you feel jealous, you are revealing the truth: you think you own this person. Their attention, their affection, their body, their time—these belong to you, and when someone else threatens to take them, you rage.

This is actually more truthful than the "love" you profess. The love is often performance—an image you maintain. But jealousy comes from the gut. It shows uncensored what you really believe about the other person: that they are yours.

Your jealousy is your honesty breaking through your pretense."""),
        50 to TruthPunch(50, 3, "The Violence of Need",
            "A relationship based on need will always end in violence, either physical or psychological.",
            """When two people need each other, they are tethered by dependency. And dependency breeds fear: the fear of abandonment, the fear of losing what you cannot live without. This fear corrupts everything.

The needy person becomes controlling because they cannot risk losing their supply. They manipulate, guilt, demand, threaten—anything to keep the other person close. The violence may not be physical, but it is violence nonetheless: psychological coercion.

Watch relationships built on need. They oscillate between desperation and resentment. The needy one clings; the needed one suffocates. Both are trapped in a dance that cannot end well.

Only those who do not need each other can be together without violence."""),
        
        49 to TruthPunch(49, 3, "The Human Furniture",
            "You use other people as furniture to decorate the empty room of your life.",
            """Look at the people in your life. What role does each one serve? This friend makes you feel interesting. That colleague makes you feel competent. This partner makes you feel desired.

You have arranged people around you like furniture, each piece placed to fill an aesthetic need. They are not fully human to you—they are props in the story you tell yourself about your life.

When a piece of furniture stops serving its purpose, you replace it. When a person stops reflecting what you want to see, you move on. This is not relationship—it is interior decorating with human beings.

The empty room remains empty. No amount of furniture changes that."""),
        
        48 to TruthPunch(48, 3, "The Disguised Ego",
            "True caring is leaving people alone; your interference is merely ego masquerading as concern.",
            """You want to help. You give advice. You offer solutions. You share wisdom. You believe this makes you caring. But look more closely at what you are actually doing.

Your interference says: I know better than you. Your situation requires my input. You cannot figure this out without me. This is not concern—it is ego disguised as helpfulness.

True caring might simply be witnessing. Being present without fixing. Trusting the other person to find their own way. But this feels too passive to you. The ego wants to do something, to matter, to be the one who helped.

Most unsolicited advice is an ego trip pretending to be compassion."""),
        
        47 to TruthPunch(47, 3, "The Self-Preserving Forgiveness",
            "You forgive others not to heal them, but to stop the pain of holding onto your own hate.",
            """Forgiveness is praised as a gift you give to the one who wronged you. But observe what actually happens when you forgive. Whose burden is lifted?

The pain of holding hatred is yours to bear. The resentment burns in your chest, not theirs. The obsessive thoughts of revenge consume your energy, not theirs. They may have forgotten you entirely while you stew in bitterness.

Forgiveness is self-medication. It is the release of poison you have been drinking while waiting for the other person to die. The forgiveness is for you—it always was.

This does not make forgiveness less valuable. But it exposes the idea that it is selfless as another comfortable lie."""),
        
        46 to TruthPunch(46, 3, "The Unbearable Self",
            "Loneliness is not the absence of others; it is the presence of a self you cannot stand.",
            """You think loneliness means being alone. But you can be surrounded by people and still feel desperately lonely. And you can be entirely alone and feel complete peace.

The loneliness you fear is not about missing others. It is about being left alone with yourself—and finding that company unbearable. Your own thoughts torment you. Your own feelings overwhelm you. Your own presence is not enough.

So you seek distraction in the form of people. Not because you want connection but because you cannot tolerate solitude. Others become an escape from yourself.

True companionship would require first being able to stand your own company."""),
        
        45 to TruthPunch(45, 3, "The Conspiracy of Society",
            "Society is a conspiracy to keep you from realizing you are utterly alone.",
            """Society keeps you busy. There are obligations to meet, roles to play, expectations to fulfill. Family gatherings, work meetings, social events—a constant stream of togetherness.

But strip it all away and what remains? You, alone, with the vast silence of existence. No one can accompany you into your own consciousness. No one will die your death for you. No one truly knows what it is like to be you.

Society is a collective denial of this fundamental solitude. We huddle together, maintaining the illusion of connection, pretending we are not each isolated in our own private universe.

The conspiracy works. Most people never stop long enough to notice their essential aloneness."""),
        
        44 to TruthPunch(44, 3, "The Fear-Based Morality",
            "Social morality is a fear-based construct, not a divine law.",
            """You follow moral rules. You believe them to be right and good. But why do you follow them? Trace the motivation to its source.

You fear punishment—from law, from society, from some imagined cosmic judge. You fear rejection—being cast out if you violate the tribal code. You fear guilt—the internal punishment that enforces compliance.

Where is the love in this? Where is the genuine goodness? Fear dressed in moral clothing is still fear. You are not being good; you are being obedient.

True ethics, if such a thing exists, would arise spontaneously—not from fear of consequences but from seeing clearly what is appropriate. Most morality is just trained behavior."""),
        
        43 to TruthPunch(43, 3, "The Monster in Silence",
            "You seek company because in silence, your own thoughts would eat you alive.",
            """Notice what happens when the noise stops. When there is no one to talk to, nothing to distract you, nowhere to go. What arises?

For most people, it is unbearable. The thoughts that have been suppressed surge forward. The anxieties that were buried demand attention. The emptiness that was masked becomes visible. This is the monster in the silence.

And so you flee. You call someone. You check your phone. You turn on something—anything to break the silence before the monster gets you.

All your social life is, in part, an escape from the horror of your own undistracted mind."""),
        
        42 to TruthPunch(42, 3, "The Covert Demand",
            "Empathy is often just a covert demand for reciprocation.",
            """You listen carefully to someone's troubles. You express understanding. You offer comfort. This feels like empathy—pure, generous care for another person.

But observe more closely. What do you expect in return? You expect to be heard when it is your turn. You expect your empathy to be acknowledged. You expect the relationship to balance out eventually.

This is not empathy—it is investment. You are depositing into an emotional bank account, expecting future withdrawals. The appearance of care masks a hidden transaction.

Genuine empathy—empathy that expects nothing, that would feel no disappointment if unreciprocated—is rare to the point of being almost nonexistent."""),
        
        41 to TruthPunch(41, 3, "The Parallel Truth",
            "The only pure relationship is parallel, where neither intersects or demands from the other.",
            """Consider two people walking side by side in the same direction. They share the journey, but neither is pulling the other. Neither is ahead or behind. Neither needs the other to continue walking.

This is the only relationship that does not inherently contain conflict. When paths cross, demands arise. When needs entangle, resentment follows. When one depends on another, control appears.

But parallel existence asks nothing. It enjoys presence without possession. It shares space without merging. It allows freedom without abandonment.

Most relationships are not parallel. They are intersection—collision points where two separate needs crash into each other."""),
        
        // ===== TIER 2: THE SPIRITUAL SCAM (Seeking and Methods) =====
        40 to TruthPunch(40, 2, "The Prescription Trap",
            "Prescriptions and methods are for those who want to feel they are doing something, not for those who want to arrive.",
            """Methods give you something to do. Steps to follow, practices to maintain, goals to measure against. You feel productive. You feel like you are making progress.

But arrival, if it exists, is not at the end of a process. It is not the result of steps completed. Looking for techniques is a way to stay busy, to maintain the identity of someone making the journey.

The method becomes a destination in itself. You perfect your practice. You refine your technique. You become very good at searching. But you never find, because finding would end the searching.

Those who truly want to arrive do not ask how. They simply stop."""),
        
        39 to TruthPunch(39, 2, "The Sedated Mind",
            "Meditation is merely a way to sedate the mind so it can survive to torture you another day.",
            """Meditation calms you down. It brings temporary relief from the endless noise. For twenty minutes or an hour, the storm subsides.

But then you return to your life, and the storm returns. Tomorrow you will need to meditate again. And the day after. The need never ends because nothing has fundamentally changed.

Meditation is a sedative. It manages symptoms without curing the disease. It makes the unbearable bearable, which allows the underlying condition to persist indefinitely.

You are not healing through meditation. You are stabilizing a chronic condition well enough to continue living with it."""),
        
        38 to TruthPunch(38, 2, "The Endless Practice",
            "If meditation worked, you would have stopped needing to do it years ago.",
            """How long have you been meditating? Months? Years? Decades? And you are still doing it. Still needing it. Still returning to the cushion.

If the medicine worked, you would no longer be sick. If the training succeeded, you would have graduated. If the path led somewhere, you would have arrived. But here you are, still walking.

The endless practice reveals the endless nature of the problem—or perhaps reveals that the practice is not solving what you think it is solving. Either way, the continuation is damning evidence.

At what point do you admit that what you are doing is not working?"""),
        
        37 to TruthPunch(37, 2, "The Beloved Search",
            "You do not want the question to end; you want to keep asking it so you can remain the 'seeker.'",
            """The identity of the seeker is a comfortable one. You are on a journey. You are pursuing truth. You are sophisticated enough to ask the deep questions. This sets you apart from the masses who never inquire.

But you do not actually want to find. Finding would end the seeking. You would lose the identity that has sustained you for years. Who would you be without your questions?

So you keep the search alive. You read another book. You try another technique. You ask another teacher. You stay in perpetual motion toward a destination you secretly hope never to reach.

The seeker is addicted to seeking. The search is the point."""),
        
        36 to TruthPunch(36, 2, "The Blind Guide",
            "Following a guru is the ultimate act of cowardice; it is handing your eyes to a blind man.",
            """A teacher promises to show you the way. They speak with authority, with confidence. They seem to know things you do not know. And so you follow.

But look more closely. This teacher—are they free? Are they beyond suffering? Or are they just articulate about concepts that have not transformed them either? The teaching industry is filled with people teaching what they themselves have not realized.

And even if the teacher were genuine, following them would still be your abdication. You are handing over responsibility for seeing to someone else. You are asking to be led when the whole point is to see for yourself.

A teacher who allows followers is not a teacher worth following."""),
        
        35 to TruthPunch(35, 2, "The Strengthened Ego",
            "Every technique you practice strengthens the very ego you claim you are trying to dissolve.",
            """You practice awareness. You cultivate presence. You train in non-attachment. But who is doing the practicing? Who is cultivating? Who is training?

The one who makes the effort is the ego. And the ego cannot dissolve itself through effort—it can only strengthen itself. Every technique you master becomes another achievement. Every insight becomes another possession. Every spiritual milestone becomes another decoration on the ego's mantelpiece.

The practitioner becomes a spiritual ego—one who identifies as someone who is beyond ego. This is the most stubborn form of ego there is.

The more you practice, the stronger the illusory practitioner becomes."""),
        
        34 to TruthPunch(34, 2, "The Split of Presence",
            "The moment you try to 'be present,' you have split yourself in two and created conflict.",
            """'Be present.' This instruction seems simple enough. Bring your attention to now. Focus on what is happening.

But watch what happens when you try. There is you, trying to be present. And there is you, observing whether you are succeeding. Two entities: the one who should be present and the one checking. This is division, not presence.

True presence is not a state you achieve through effort. It is not something you create by trying. The me that is trying to be present is the very obstacle to presence.

Every attempt to be present confirms your absence."""),
        
        33 to TruthPunch(33, 2, "The Comfort Seeker",
            "Spirituality is just a fancy word for psychological comfort-seeking.",
            """What are you really looking for when you pursue spirituality? Strip away the lofty language. What is the core desire?

You want to feel better. You want peace. You want relief from anxiety, from suffering, from the nagging sense that something is wrong. You want psychological comfort.

There is nothing wrong with this—except the pretense that it is something more elevated. Spirituality dresses up ordinary desire for comfort in cosmic clothing. You are not seeking truth; you are seeking a better feeling state.

If spirituality felt terrible, you would not pursue it. The feeling good is the point."""),
        
        32 to TruthPunch(32, 2, "The Violence of Silence",
            "Trying to quiet the mind is an act of violence against your own biology.",
            """The mind makes noise. This is what minds do. Thinking is as natural to the brain as pumping is to the heart, as breathing is to the lungs.

When you try to quiet the mind, you are fighting biology. You are trying to make an organ stop doing what it evolved to do. This is violence against yourself dressed up as spiritual practice.

Watch the war that happens when you meditate. The mind produces thoughts. You resist. More thoughts come. You resist harder. The battle intensifies. And you call this peace?

Perhaps the issue is not the noise but your war against it."""),
        
        31 to TruthPunch(31, 2, "The Avoidance of Looking",
            "You read books on truth to avoid the terror of looking directly at your own falseness.",
            """How many spiritual books have you read? How many hours have you spent consuming words about truth, about freedom, about enlightenment?

Now ask: what has changed? If the books worked, one would have been enough. Instead, you keep reading. You keep consuming. You keep gathering more concepts, more frameworks, more perspectives.

Reading about truth is safe. Looking at yourself is not. In books, truth is an interesting idea. In direct observation, it is a devastating mirror.

You read to postpone the encounter. Every page is a shield between you and the direct seeing you claim to seek."""),
        30 to TruthPunch(30, 2, "The Artificial Game",
            "Discipline is an artificial game played by those who lack genuine desire.",
            """Discipline is celebrated. Forcing yourself to do what you do not want to do is considered a virtue. But consider what this reveals.

If you needed discipline, you did not truly want it. The desire was weak, so you had to manufacture force. You had to turn it into a game of willpower against resistance.

Watch someone with genuine hunger. They do not speak of discipline. They do not wrestle with resistance. The thing pulls them. They cannot not do it. This is not self-control—it is compulsion, in the most beautiful sense.

Your discipline is evidence of your doubt. If the desire were real, you would not need to force yourself."""),
        
        29 to TruthPunch(29, 2, "The Myth of the Path",
            "The 'path' is a myth sold to you by people who are just as lost as you are.",
            """There is no path. Look around at those selling paths—are they any more arrived than you? They have techniques, systems, stages, and maps. But they are still seeking. Still teaching. Still needing to convince others.

The path is a product. It gives you something to do. It creates dependence on those who claim to know the way. It generates teachers, courses, retreats, and industries—all dependent on your continued belief that you are somewhere other than where you need to be.

If there were a path, someone would have completed it by now and told everyone the shortcut. Instead, the journey continues forever, which is exactly what keeps the industry going.

You are not lost. You are being sold a map."""),
        
        28 to TruthPunch(28, 2, "The Stalling Tactic",
            "There is no 'how'; asking 'how' is a stall tactic to ensure you never have to do it.",
            """How do I become present? How do I let go? How do I find peace? These questions seem sincere, but they are strategies for staying stuck.

Asking 'how' implies that there is a technique you are missing. Once you find it, then you will act. But the technique never quite appears—or when it does, another question follows. How do I do this technique properly?

You already know what to do. Seeing is not complicated. But seeing would change everything, and you are not ready for that. So you ask questions instead of looking.

'How' is a delay disguised as genuine inquiry."""),
        
        27 to TruthPunch(27, 2, "The Destructive Hope",
            "Hope is the most destructive force in the universe; it keeps you focused on a future that will never arrive.",
            """Hope feels positive. It is celebrated as a virtue. But look at what hope actually does.

Hope shifts your attention to the future. It says: things will be better then. Not now, but later. Tomorrow will be different. This keeps you tolerating today because tomorrow is coming.

But tomorrow never comes. When it arrives, it is today again—and hope shifts to the next tomorrow. You live your life in anticipation of a future that never actually materializes.

Hope is how the present is sacrificed on the altar of an imaginary future. It is the drug that makes the unbearable right now tolerable enough to continue."""),
        
        26 to TruthPunch(26, 2, "The Identity of Struggle",
            "You are addicted to the struggle because the arrival would mean the end of your identity.",
            """You are a seeker. You are working on yourself. You are on a journey of growth. This identity has defined you for years.

But what happens if you arrive? Who are you without the struggle? What would you do with yourself if there were nothing left to fix?

The struggle gives you purpose. It gives you something to do. It gives you a story to tell. Arrival would end all of this. You would have to face the emptiness on the other side.

So you remain in struggle. You find new areas to work on. You discover deeper layers of the problem. Anything to avoid the ending of the seeking self."""),
        
        25 to TruthPunch(25, 2, "The Insurance Policy",
            "Religion is the insurance policy you buy because you are afraid of the dark.",
            """Why do people believe? Not because they have seen evidence but because they are afraid. Afraid of death. Afraid of meaninglessness. Afraid of the vast, uncaring universe.

Religion offers insurance. Believe this, follow these rules, and you will be protected. There is a plan. There is meaning. There is something after death.

This is comfort purchased at the price of honesty. You do not believe because it is true—you believe because the alternative is unbearable.

Faith is not a virtue. It is fear management."""),
        
        24 to TruthPunch(24, 2, "The Delay of Method",
            "A method implies time, but truth is immediate; using a method is a way to delay the truth.",
            """Methods require time. Step one, step two, step three. Practice daily for ten years. First concentration, then insight. The method structures your approach across time.

But if truth is about seeing what is, why would it take time? Seeing happens now or not at all. A method that requires time is a method that postpones seeing.

You have adopted the method precisely because it gives you time. It lets you feel like you are progressing while keeping the actual confrontation safely in the future.

Every moment spent in method is a moment spent not looking."""),
        
        23 to TruthPunch(23, 2, "The Feared Destination",
            "The 'journey' does not matter; only the destination matters, and you are terrified to reach it.",
            """'It is about the journey, not the destination.' This is one of the most popular spiritual clichés—and one of the most revealing.

Why would you say this unless the destination frightened you? The destination is what matters. You set out to arrive somewhere. Everything you do is oriented toward getting there.

But arrival would mean the end of you as seeker. And the destination itself—truth, reality, what is—may not be what you hoped for. It may be devastating.

So you celebrate the journey. You make the travel itself the point. Anything to avoid arriving."""),
        
        22 to TruthPunch(22, 2, "The Drowning Chant",
            "You chant mantras to drown out the screaming of your own insecurity.",
            """You chant. You repeat sacred words. The sound fills your mind, leaving no room for thought. You call this spiritual practice.

But what are you running from? Beneath the chant, there is something you do not want to hear. Your mind left to itself would confront you with doubts, fears, and the screaming emptiness of uncertainty.

The mantra is a wall of sound between you and yourself. It is noise used to mask noise. It may bring temporary peace, but it is the peace of avoidance, not resolution.

When the chanting stops, everything you were drowning out is still there."""),
        
        21 to TruthPunch(21, 2, "The Catastrophic Truth",
            "Transformations are catastrophic, not incremental; you cannot 'gradually' wake up.",
            """You imagine awakening as a gradual process. Each day a little better. Each year a little clearer. Slow improvement over time.

But real transformation does not work this way. It is not a gentle ascent—it is a collapse. Everything you believed, everything you built, everything you are comes crashing down at once.

This is why you prefer the gradual story. Gradual feels safe. Gradual keeps you in control. Gradual lets the you who starts the journey be the you who arrives.

Catastrophe does not work that way. The you who exists now will not survive the transformation."""),
        
        // ===== TIER 1: THE ANNIHILATION (Existential Truths) =====
        20 to TruthPunch(20, 1, "The Obstacle of Seeking",
            "There is no self to realize; the one seeking realization is the only obstacle.",
            """You are looking for yourself. You are trying to realize the truth of who you are. But consider the structure of this search.

There is a seeker searching for a self. Two entities: the one looking and the one to be found. But what if the seeker is the only thing in the way? What if the seeking itself is the obstacle?

The self you are looking for does not exist as a separate entity. The entity looking—the seeker—is a phantom created by the search itself. When the seeking stops, what remains?

Perhaps nothing needs to be found. Perhaps the search is the only thing hiding what was never lost."""),
        
        19 to TruthPunch(19, 1, "The Biological Machine",
            "You do not have a life; you are a biological machine processing reactions to stimuli, and you call this 'living.'",
            """What you call life is a series of reactions. Stimuli come in: sights, sounds, events. The organism responds: thoughts arise, emotions occur, actions happen. You call this sequence a life.

But who is living it? There is a body reacting. There is a brain processing. There are automated responses to environmental inputs. Where in all this is the one who lives?

You are not having an experience. Experience is happening, and the observer is a part of the experience, not separate from it. There is no one standing outside the machine, operating it.

What you call 'my life' is the machine's story about itself."""),
        
        18 to TruthPunch(18, 1, "The Fictitious Narrative",
            "The entity you call 'I' is a fictitious narrative created by thought to give continuity to a series of disjointed memories.",
            """There is no continuous self. What you call 'I' is a story invented moment by moment by thought. It takes disjointed memories and weaves them into a narrative called 'me.'

Yesterday's you and today's you are not the same. Every cell, every thought, every moment is different. But the story maintains an illusion of continuity—a persistent character who experiences life.

This character does not exist. It is a fiction maintained by the storytelling function of the mind. Without memory, without the narrative, there would be no sense of being the same person across time.

You are a novel the brain is writing in real time."""),
        
        17 to TruthPunch(17, 1, "The Neurological Defect",
            "Consciousness is not a divine gift; it is a neurological defect that separates man from the perfection of his biological nature.",
            """Animals act. They respond to their environment with elegant simplicity. They do not agonize over decisions. They do not worry about meaning. They do not fear death conceptually.

Then came human consciousness—the capacity to be aware of being aware. And with it came suffering. The ability to imagine futures that do not exist. The ability to regret pasts that cannot be changed. The ability to fear death while still alive.

This is not a gift. It is a defect. It is a glitch in biological programming that creates the very problems it then tries to solve.

You are the only animal troubled by being alive."""),
        
        16 to TruthPunch(16, 1, "The Labeled Pleasure",
            "You are not looking for the truth; you are looking for a permanent state of pleasure that you have labeled 'truth.'",
            """What do you imagine truth will feel like? Peace. Bliss. An end to suffering. Freedom from anxiety. Permanent contentment.

But these are all pleasurable states. What you are chasing is not truth—it is pleasure rebranded. You have taken your desire for good feelings and dressed it in spiritual clothing.

Real truth may be devastating. It may not feel good at all. It may destroy everything you hoped and believed. Are you prepared for that? Or do you want truth only if it comes with guaranteed happiness?

Your spiritual seeking is hedonism wearing a costume."""),
        
        15 to TruthPunch(15, 1, "The Constant Death",
            "Death is not a future event; you die every moment thought ceases, but you frantically resurrect yourself with noise.",
            """Death frightens you because you imagine it as a future event. But you die many times every day. Every time thought ceases—in the gap between thoughts, in deep sleep, in moments of total absorption—you are gone.

Then thought returns, and you resurrect. You re-create the story of yourself. You remember who you are. You come back to life.

The fear of death is the fear of this gap—the fear of the absence of self. You fill every moment with noise, with thought, with activity, to avoid these little deaths that happen constantly.

You are terrified of what you already experience multiple times every day."""),
        
        14 to TruthPunch(14, 1, "The Invented Soul",
            "The soul is a concept invented by the frightened mind to ensure its own continuity after the body rots.",
            """The body will die. This is certain. And the mind, facing this certainty, could not accept total annihilation. So it invented the soul.

The soul is a concept—a belief that something will continue after physical death. It offers continuity to that which desperately wants to persist. It is insurance against oblivion.

But look at the structure. The mind, facing its own ending, creates a concept that guarantees its survival. This is not revelation—it is wishful thinking encoded as belief.

The soul is the mind's refusal to accept its own mortality."""),
        
        13 to TruthPunch(13, 1, "The Painted Ghost",
            "There is no inner 'you' to be improved; you are painting over a ghost.",
            """Self-improvement assumes there is a self to improve. There is the current you, flawed and incomplete, and a future you, better and more realized. The project is to get from one to the other.

But look more closely. Where is this inner self? When you search, you find only thoughts, memories, sensations—all in motion, all changing. There is no fixed core that could be improved.

You are improving a ghost. You are polishing an illusion. Every improvement becomes part of the story the mind tells about itself, but the one who would be improved never actually exists.

Self-improvement is the decorating of nothing."""),
        
        12 to TruthPunch(12, 1, "The Corpse of Childhood",
            "Your 'inner child' is not lost; it is dead, and you are dragging its corpse around to justify your immaturity.",
            """Therapy tells you to heal your inner child. To reconnect with the wounded parts of yourself. To nurture what was neglected long ago.

But that child does not exist. The child you were is gone—died years ago, cell by cell, thought by thought. What remains are memories, patterns, habits. These are not a child. These are the residue of a person who no longer exists.

The 'inner child' is a corpse you drag around to justify your present behavior. It gives you permission to remain immature, to blame the past, to avoid adult responsibility.

You are not healing a child. You are animating a ghost."""),
        
        11 to TruthPunch(11, 1, "The Calamity",
            "Enlightenment is not a state of bliss; it is a calamity that destroys everything you currently value, including you.",
            """You imagine enlightenment as the perfected version of your current self. All your good qualities retained, all your suffering removed. A better you, finally at peace.

But that is not what happens. Enlightenment—if such a thing exists—is catastrophic. It does not improve you; it ends you. Everything you identify with, everything you value, everything that makes you you would be destroyed.

This is why you do not really want it. You want the benefits without the cost. You want redemption without death.

The enlightenment that you would still be around to enjoy is not enlightenment at all."""),
        10 to TruthPunch(10, 1, "The Reaction",
            "You exist only as a reaction; without a stimulus to provoke you, you are nothing.",
            """Take away all stimuli. No sights, no sounds, no sensations, no memories, no thoughts. What remains of you?

You exist only in relationship to something else. You are the reaction to the world, not a thing in yourself. When someone insults you, anger arises. When something pleasant appears, desire arises. You are a response mechanism, nothing more.

Without provocation, there is no you. Without input, there is no output. The self you take to be solid and continuous is actually flickering into existence moment by moment, caused by external triggers.

You are not a noun but a verb—a reacting that is mistaken for a reactor."""),
        
        9 to TruthPunch(9, 1, "The Graveyard Silence",
            "The silence you seek is the silence of the graveyard, yet you are terrified to enter it while alive.",
            """The peace you seek is the absence of yourself. All the noise, all the suffering, all the chaos comes from one source: the self and its endless demands.

True silence is not the quiet of meditation. It is the silence of what you are not there to experience. It is the peace of absence, not the peace of a peaceful person.

This is why you flee from it. You want peace, but you want to be there to enjoy it. You want silence, but you want to hear it. This contradiction ensures you never find what you seek.

The graveyard offers what you want. You simply refuse to enter it while breathing."""),
        
        8 to TruthPunch(8, 1, "The True Freedom",
            "Freedom is not the ability to do what you want; it is the freedom FROM the person you think you are.",
            """You think freedom means options. The ability to choose between this and that. The power to shape your life according to your desires.

But this is freedom within prison walls. The desires themselves are the prison. The person who wants is the prison. As long as there is a you wanting to be free, you remain trapped.

True freedom is not the expansion of the self but the absence of it. Not the fulfillment of desires but the absence of the one who desires.

This is terrifying because it means the free state is one you will not be there to enjoy. Freedom is freedom from you."""),
        
        7 to TruthPunch(7, 1, "The Ego's Trick",
            "There is no observer separate from the observed; the separation is a trick of language to maintain the ego.",
            """Language creates a structure: I see the tree. The sentence implies two things: I (the seer) and the tree (the seen). Subject and object. Observer and observed.

But is there really a seer separate from seeing? When you look at a tree, can you actually find a little observer inside your head doing the looking? Or is there just seeing happening?

The division is a grammatical convention, not a reality. Language tricks you into believing in a self that stands apart from experience. This division is what creates the ego.

There is no observer. There is only observing."""),
        
        6 to TruthPunch(6, 1, "The Stranger's Biography",
            "Your biography is a story you tell yourself to hide the fact that you are a stranger to yourself.",
            """You have a past. You know where you were born, what you did, who you loved. You can tell your story. It gives you an identity—a coherent narrative arc with you as the main character.

But who is this person, really? Do you understand why you do what you do? Do you know where your thoughts come from? Do you comprehend the mystery of your own consciousness?

You are a stranger to yourself, and the biography is a cover story. It gives the illusion of knowing who you are when, in reality, you are as much a mystery to yourself as anyone else.

The story hides the void."""),
        
        5 to TruthPunch(5, 1, "The Ultimate Entertainment",
            "The search for meaning is the ultimate entertainment for a mind terrified of its own emptiness.",
            """You search for meaning. You ask why you are here, what the purpose is, what it all means. This seems like a noble quest.

But consider: why does meaning need to be found? Why is the possibility of meaninglessness so unbearable that you spend your life avoiding it?

The mind cannot tolerate emptiness. It must fill every void with content. And so it invents the greatest game of all: the search for meaning. This search can last a lifetime. It provides endless material for thought, endless hope, endless activity.

The search for meaning is entertainment for a mind that cannot rest."""),
        
        4 to TruthPunch(4, 1, "The Serial Number",
            "You are not a unique individual; you are a culturally manufactured product with a serial number you call a name.",
            """You believe you are special. An individual. A unique expression of something singular. Your name marks your distinct identity in the world.

But look at how you were made. You did not invent your beliefs—they were installed. You did not create your values—they were inherited. Your desires, your fears, your very sense of self—all manufactured by the culture into which you were born.

You are a product. Mass-produced. Different packaging, but the same basic components as everyone else around you. Your name is not who you are; it is your serial number.

Individuality is a mass delusion."""),
        
        3 to TruthPunch(3, 1, "The Natural State",
            "The 'natural state' is not spiritual; it is a biological functioning where the 'you' does not exist to experience it.",
            """There is a natural way of being. Before conditioning, before thought, before the construction of self, the organism functioned. It responded to life without the interference of a 'me.'

This natural state is not a spiritual achievement. It is not enlightenment as you imagine it. It is simply what happens when the self-construct falls away—just biological functioning without a claimer.

The tragedy is that you cannot experience this state. The 'you' that would experience it is the very thing that must be absent for the state to occur.

The natural state is not for you. It is the absence of you."""),
        
        2 to TruthPunch(2, 1, "The Ascent Illusion",
            "Awakening is not a spiritual ascent; it is the realization that there is no one to ascend.",
            """Spirituality is told as a story of ascent. You start low, unenlightened, suffering. You practice, grow, evolve. Eventually, you reach the top: enlightenment, awakening, liberation.

But this story has a fatal flaw: it assumes you will be there at the end. The one who started the climb also arrives at the summit. This is the seduction.

Real awakening—if such a thing exists—is not an ascent. It is the collapse of the one who wanted to ascend. There is no one to climb and no summit to reach.

The spiritual ascent is a story the ego tells itself to stay alive."""),
        
        1 to TruthPunch(1, 1, "The Final Truth",
            "There is nothing inside you but the noise of the world you have swallowed.",
            """You have reached the end. You have heard many truths, each one stripping away another illusion. Now you face the final truth.

There is nothing inside you. No core self. No essential nature. No soul waiting to be discovered. What you call 'you' is the accumulation of everything external that you have absorbed—the ideas, the values, the fears, the desires. You are the noise of the world echoed back.

This is not despair. It is not nihilism. It is simply what is. You are empty, and that emptiness has been filled with everything you have ever encountered.

When you search within, you find only echoes. This is the final truth. And with it, the search ends.""")
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

    /**
     * Get current user level (1-100) based on highest unlocked truth.
     * Rank 100 unlocked (default) = Level 1
     * Rank 1 unlocked = Level 100
     */
    fun getLevel(): Int {
        return 101 - highestUnlockedRank
    }

    /**
     * Get the title for a specific level
     */
    fun getLevelTitle(level: Int): String {
        return LEVEL_NAMES[level] ?: "Seeker"
    }

    /**
     * Get the title for the current user level
     */
    fun getCurrentLevelTitle(): String {
        return getLevelTitle(getLevel())
    }
}

