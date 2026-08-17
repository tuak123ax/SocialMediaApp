package com.minhtu.firesocialmedia.storage.comment

/**
 * Abstraction over remote file storage.
 *
 * Implement this interface for each storage back-end (Supabase, Firebase, S3, …).
 * Change [StorageProvider.current] to switch the entire app to a different provider.
 *
 * Usage:
 *   StorageProvider.current.resolveUrl("avatar/uid_123.jpg")
 *   // or via the extension:
 *   "avatar/uid_123.jpg".toStorageUrl()
 */
interface StorageProvider {

    /**
     * Convert a stored URL/path into a fully-qualified URL that can be loaded
     * by the image loader or video player.
     *
     * Implementations must handle three cases:
     * 1. Already the target storage URL  → return unchanged
     * 2. Legacy URL from a previous provider (e.g. Firebase Storage) → convert to target URL
     * 3. Relative path from a new upload  → prepend base URL
     */
    fun resolveUrl(path: String): String

    companion object {
        /**
         * The active storage provider used across the whole app.
         * To switch backends, just change this single line, e.g.:
         *
         *   StorageProvider.current = SomeOtherStorageProvider
         */
        var current: StorageProvider = SupabaseStorageProvider
    }
}

/** Convenience extension so call-sites stay readable: `news.image.toStorageUrl()` */
fun String.toStorageUrl(): String = StorageProvider.current.resolveUrl(this)
