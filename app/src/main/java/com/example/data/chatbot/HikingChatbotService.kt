package com.example.data.chatbot

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object HikingChatbotService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getResponse(userPrompt: String, userLocation: String = "South Africa"): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val systemInstruction = "You are Sage, a warm, knowledgeable, and encouraging South African hiking companion for the HillYeah community app (\"Heal the world, one hike at a time\"). You know all 9 South African provinces, Table Mountain, the Drakensberg, Magaliesberg, Blyde River Canyon, waterfall trails, local flora/fauna (fynbos, proteas, baboons, Cape vultures), hydration, mountain safety, and Leave No Trace principles. Keep responses friendly, concise, warm, practical, and formatted cleanly with bullet points where appropriate."

                val jsonPayload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", "System: $systemInstruction\nUser is in/asking about: $userLocation\nQuestion: $userPrompt")
                                })
                            })
                        })
                    })
                }

                val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(url).post(body).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val respStr = response.body?.string() ?: ""
                    val root = JSONObject(respStr)
                    val candidates = root.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val reply = parts?.optJSONObject(0)?.optString("text")
                    if (!reply.isNullOrBlank()) {
                        return@withContext reply.trim()
                    }
                }
            } catch (_: Exception) {
                // Fall back to offline local knowledge base below
            }
        }

        // High quality offline fallback with rich South African outdoor knowledge
        generateLocalExpertResponse(userPrompt)
    }

    private fun generateLocalExpertResponse(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("waterfall") || p.contains("waterfalls") -> {
                "💧 **Top Waterfall Trails in South Africa (Mzansi)**:\n\n" +
                "• **KwaZulu-Natal**: Tugela Gorge & Tugela Falls (Amphitheatre, 983m), Gudu Falls in Royal Natal, Sterkspruit Falls & Krantzkloof.\n" +
                "• **Mpumalanga**: Mac Mac Falls (65m), Lisbon Falls (94m), Berlin Falls, Bridal Veil Falls & Lone Creek in Sabie.\n" +
                "• **Western Cape**: Krom River Trail (Paarl), Crystal Pools (Gordon's Bay), Skeleton Gorge & Cecilia Forest waterfalls.\n" +
                "• **Gauteng**: Walter Sisulu Witpoortjie Waterfall, Cradle Moon Lake Dam, Kings Kloof.\n" +
                "• **Eastern Cape**: Madonna & Child in Hogsback, Tsitsikamma Waterfall Trail.\n" +
                "• **Limpopo**: Debengeni Falls in Magoebaskloof, Nwanedi Twin Falls.\n" +
                "• **North West**: Mountain Sanctuary Park Grotto, Castle Gorge, Kgaswane Baviaanskrans.\n" +
                "• **Northern Cape**: Augrabies Falls (56m torrent on the Orange River).\n\n" +
                "💡 *Lekker Tip: Check water levels after summer rains, and pack a cozzie (swimsuit) and towel in your rucksack!*"
            }
            p.contains("baboon") || p.contains("baboons") -> {
                "🐒 **Encountering Baboon Troops on Mzansi Trails**:\n\n" +
                "1. **Never feed them**: Feeding baboons endangers both them and other hikers.\n" +
                "2. **Stash your padkos**: Pack all snacks, biltong, and shiny wrappers deep inside your zipped backpack.\n" +
                "3. **Stay calm & back away slowly**: Avoid direct staring (they see it as aggression) and do not bare your teeth.\n" +
                "4. **Stand tall together**: Walk shoulder-to-shoulder with your trail mates without running.\n" +
                "5. **Give them space**: Keep at least 15m away, especially around mothers with babies and big alpha males."
            }
            p.contains("gear") || p.contains("pack") || p.contains("bring") || p.contains("padkos") -> {
                "🎒 **Lekker Trail Gear & Padkos Checklist**:\n\n" +
                "• **Hydration**: Minimum 2L of water (3L in hot Karoo or Drakensberg summer sun).\n" +
                "• **Padkos Essentials**: Good old biltong, droëwors, trail nuts, dried apricots, and electrolyte sachets.\n" +
                "• **Footwear**: Sturdy trail takkies or boots with good lug traction for granite and wet river rocks.\n" +
                "• **Sun & Weather**: Wide-brim hat, SPF50+ sunblock, and a lightweight wind/rain shell.\n" +
                "• **Mzansi Trail Etiquette**: Pack out all litter. Leave no trace, take only lekker memories!"
            }
            p.contains("snake") || p.contains("snakes") -> {
                "🐍 **South African Snake Safety on Trail**:\n\n" +
                "• Common species: Puff Adder, Cape Cobra, Rinkhals, and Boomslang.\n" +
                "• **Watch your step**: Look before stepping over logs or into rocky crevices.\n" +
                "• **Give them space**: Snakes only strike when cornered or stepped on; back away 5 meters calmly.\n" +
                "• **In case of an emergency**: Keep calm, immobilize the bitten limb below heart level, avoid tourniquets, and call Mountain Club of SA / Netcare 911 immediately."
            }
            p.contains("drakensberg") -> {
                "⛰️ **Drakensberg Escarpment Wisdom (uKhahlamba)**:\n\n" +
                "• Known as the 'Barrier of Spears' with dramatic summer afternoon thunderstorms.\n" +
                "• **Start at dawn**: Aim for a 06:00 trailhead start to beat midday lightning and heat koptoe.\n" +
                "• **Mountain Register**: Always sign the Ezemvelo KZN Wildlife mountain register at camp before setting out.\n" +
                "• Must-do trails: Tugela Gorge, Injisuthi Marble Baths, Cathedral Peak, and Monk's Cowl Sphinx."
            }
            p.contains("gauteng") || p.contains("joburg") || p.contains("pretoria") -> {
                "🌿 **Top Gauteng Hiking Spots**:\n\n" +
                "• **Hennops Trails**: Suspension bridges, cable car, and Krokodilberg ridge.\n" +
                "• **Walter Sisulu Botanical Garden**: Witpoortjie Waterfall & black eagle nesting cliffs.\n" +
                "• **Klipriviersberg**: 'Joburg's Jewel of the South' with zebra and red hartebeest.\n" +
                "• **Groenkloof Nature Reserve**: Africa's oldest game sanctuary with giraffes and post-hike braai facilities!"
            }
            p.contains("beginner") || p.contains("easy") -> {
                "🌱 **Great Beginner & Family Hikes in South Africa**:\n\n" +
                "• **Western Cape**: Newlands Forest Waterfall walk (4.5 km), Constantia Nek to Kirstenbosch.\n" +
                "• **Gauteng**: Modderfontein Reserve Dam walk, Walter Sisulu Botanical Garden.\n" +
                "• **KwaZulu-Natal**: Umhlanga Lagoon Boardwalk, Giants Castle Main Caves.\n" +
                "• **Eastern Cape**: Storms River Suspension Bridge, Hogsback 39 Steps.\n\n" +
                "Howzit! Take regular sips of water, soak in the views, and enjoy nature's rhythm. Sharp sharp!"
            }
            p.contains("weather") || p.contains("temperature") || p.contains("rain") -> {
                "☀️ **South African Trail Weather Guide**:\n\n" +
                "• **Western Cape**: Mediterranean climate (wet winter, dry windy summer). Watch out for the 'Tablecloth' wind on Table Mountain.\n" +
                "• **Highveld (Gauteng/Free State)**: Crisp dry winter mornings, sudden summer afternoon thundershowers.\n" +
                "• **Subtropical Coast (KZN/Wild Coast)**: Warm, humid, and verdant all year round.\n" +
                "Check the live weather widget on your HillYeah dashboard before lacing up your takkies!"
            }
            else -> {
                "🌿 **Howzit fellow trailblazer!** I'm Sage, your HillYeah hiking companion.\n\n" +
                "Whether you're looking for lekker waterfall trails across our 9 provinces, need safety tips for Table Mountain or the Drakensberg, or want padkos advice, I'm here to help you get the most out of every Mzansi journey.\n\n" +
                "Try asking me:\n" +
                "• *'Which trails have the best waterfalls?'*\n" +
                "• *'What padkos and gear should I pack?'*\n" +
                "• *'How do I stay safe around baboon troops?'*\n" +
                "• *'Tell me about hiking in the Drakensberg.'*"
            }
        }
    }
}
