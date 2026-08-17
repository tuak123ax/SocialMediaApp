package com.minhtu.firesocialmedia.storage.auth



/**
 * Supabase Storage provider.
 *
 * Stored paths are relative keys such as "avatar/uid_123.jpg".
 * This provider converts any stored URL/path into a fully-qualified Supabase URL:
 *
 * - Already a Supabase URL  → passed through unchanged
 * - Old Firebase Storage URL → path is extracted and mapped to Supabase
 * - Relative path            → base URL is prepended
 *
 * All Supabase-specific constants live here – nowhere else.
 */
object SupabaseStorageProvider : StorageProvider {

    internal const val BASE_URL =
        "https://pcklhkkafpomfvhboini.supabase.co/storage/v1/object/public/uploads/"

    const val DEFAULT_AVATAR_URL        = "${BASE_URL}arkavatar.png"
    const val DEFAULT_DECADE_AVATAR_URL = "${BASE_URL}decadeAvatar.png"
    const val DEFAULT_GROUP_AVATAR_URL  = "${BASE_URL}unknownavatar.png"

    override fun resolveUrl(path: String): String {
        if (path.isEmpty()) return path

        return when {
            // Supabase URL with the old defaultavatars/ sub-folder → rewrite to new flat path
            path.contains("supabase.co") && path.contains("/defaultavatars/") ->
                path.replace("/defaultavatars/", "/")

            // Already a Supabase URL → pass through unchanged
            path.contains("supabase.co") -> path

            // Old Firebase Storage URL → extract relative path and remap to Supabase
            path.contains("firebasestorage.googleapis.com") -> {
                val relativePath = try {
                    val encoded = path.substringAfter("/o/").substringBefore("?")
                    urlDecode(encoded)
                } catch (e: Exception) {
                    return path // fallback: keep as-is
                }
                BASE_URL + relativePath
            }

            // Relative path from new Supabase uploads (e.g. "avatar/uid_ts.jpg") → build full URL
            else -> BASE_URL + path
        }
    }

    private fun urlDecode(encoded: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < encoded.length) {
            when {
                encoded[i] == '+' -> {
                    sb.append(' ')
                    i++
                }
                encoded[i] == '%' && i + 2 < encoded.length -> {
                    val hex = encoded.substring(i + 1, i + 3)
                    val code = hex.toIntOrNull(16)
                    if (code != null) {
                        sb.append(code.toChar())
                        i += 3
                    } else {
                        sb.append(encoded[i])
                        i++
                    }
                }
                else -> {
                    sb.append(encoded[i])
                    i++
                }
            }
        }
        return sb.toString()
    }
}
