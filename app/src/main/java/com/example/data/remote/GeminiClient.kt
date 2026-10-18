package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getApiKey(context: Context): String {
        val prefs = context.getSharedPreferences("abdulbasit_space_prefs", Context.MODE_PRIVATE)
        val customKey = prefs.getString("custom_gemini_api_key", "") ?: ""
        if (customKey.isNotBlank()) return customKey
        
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    fun saveApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences("abdulbasit_space_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("custom_gemini_api_key", key.trim()).apply()
    }

    fun getSelectedModel(context: Context): String {
        val prefs = context.getSharedPreferences("abdulbasit_space_prefs", Context.MODE_PRIVATE)
        return prefs.getString("selected_model", "gemini-3.5-flash") ?: "gemini-3.5-flash"
    }

    fun setSelectedModel(context: Context, model: String) {
        val prefs = context.getSharedPreferences("abdulbasit_space_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_model", model).apply()
    }

    suspend fun generateContent(
        context: Context,
        prompt: String,
        systemInstruction: String? = null,
        imageBase64: String? = null,
        modelOverride: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)
        val model = modelOverride ?: getSelectedModel(context)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                Exception("کلیلێکی دروستی Gemini بەردەست نییە. تکایە لە بەشی 'بەڕێوەبردنی کلیلەکانی AI' کلیلی تایبەتی خۆت دابنێ یان لە کۆنترۆڵ پانێڵ ڕێکیبخە.")
            )
        }

        try {
            val url = "$BASE_URL$model:generateContent?key=$apiKey"
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))

            if (!imageBase64.isNullOrBlank()) {
                val inlineData = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", imageBase64)
                }
                partsArray.put(JSONObject().put("inlineData", inlineData))
            }

            val contentsArray = JSONArray()
            contentsArray.put(JSONObject().put("parts", partsArray))

            val rootJson = JSONObject().apply {
                put("contents", contentsArray)
                if (!systemInstruction.isNullOrBlank()) {
                    val sysParts = JSONArray().put(JSONObject().put("text", systemInstruction))
                    put("systemInstruction", JSONObject().put("parts", sysParts))
                }
            }

            val body = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "API Error: ${response.code} $responseBody")
                val errMsg = try {
                    val errJson = JSONObject(responseBody).optJSONObject("error")
                    errJson?.optString("message") ?: "هەڵەی سێرڤەر: ${response.code}"
                } catch (e: Exception) {
                    "هەڵە لە پەیوەندی بە مۆدێل (${response.code})"
                }
                return@withContext Result.failure(Exception(errMsg))
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val first = candidates.getJSONObject(0)
                val contentObj = first.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    return@withContext Result.success(text)
                }
            }

            Result.failure(Exception("هیچ وەڵامێک لە لایەن مۆدێلەوە وەرنەگیرا."))
        } catch (e: Exception) {
            Log.e(TAG, "Call failed", e)
            Result.failure(e)
        }
    }

    suspend fun testConnection(context: Context, model: String): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext Pair(false, -1L)
        
        val start = System.currentTimeMillis()
        return@withContext try {
            val url = "$BASE_URL$model:generateContent?key=$apiKey"
            val bodyJson = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", "سڵاو")))))
            }
            val body = bodyJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()
            val duration = System.currentTimeMillis() - start
            Pair(response.isSuccessful, duration)
        } catch (e: Exception) {
            Pair(false, -1L)
        }
    }
}
