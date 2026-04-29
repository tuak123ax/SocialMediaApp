package com.minhtu.firesocialmedia.storage

/**
 * Firebase Storage provider (stub – ready to implement).
 *
 * Firebase stores files under gs://<bucket>/<path> and exposes them via
 * signed download URLs that are already full "https://" URLs.  Because the
 * app persists those full URLs (not relative paths), resolveUrl() is a no-op
 * here – every stored value already starts with "http".
 *
 * If you switch to storing only the relative storage path instead of the
 * full download URL, replace the body of resolveUrl() with the logic that
 * fetches / constructs the signed URL.
 *
 * To activate:
 *   StorageProvider.current = FirebaseStorageProvider
 */
object FirebaseStorageProvider : StorageProvider {

    // Firebase download URLs are full https:// URLs – nothing to prepend.
    override fun resolveUrl(path: String): String = path
}
