package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.repository.HomeCommentDbRepository
import com.minhtu.firesocialmedia.domain.repository.HomeCommentRepository
import com.minhtu.firesocialmedia.home.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.home.entity.comment.base.BaseNewsInstance
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeCommentUseCasesTest {

    private class FakeCommentDbRepository(
        val saveCommentResult: Boolean = true,
        val saveSubCommentResult: Boolean = true,
        val saveLikedResult: Boolean = true
    ) : HomeCommentDbRepository {
        var deletedComment: Pair<String, BaseNewsInstance>? = null
        var deletedSubComment: Triple<String, String, BaseNewsInstance>? = null
        var updatedCommentCount: Pair<String, Int>? = null
        var updatedReplyCount: Pair<String, Int>? = null
        var updatedLikeCount: Triple<String, String, Int>? = null
        var updatedSubLikeCount: List<Any>? = null

        override suspend fun saveCommentToDatabase(selectedNewId: String, commentId: String, instance: CommentInstance): Boolean = saveCommentResult
        override suspend fun saveSubCommentToDatabase(id: String, selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance): Boolean = saveSubCommentResult
        override suspend fun deleteCommentFromDatabase(selectedNewId: String, instance: BaseNewsInstance) {
            deletedComment = selectedNewId to instance
        }
        override suspend fun deleteSubCommentFromDatabase(selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance) {
            deletedSubComment = Triple(selectedNewId, parentCommentId, instance)
        }
        override suspend fun updateCommentCountForNewInDatabase(id: String, value: Int) {
            updatedCommentCount = id to value
        }
        override suspend fun updateReplyCountForCommentInDatabase(id: String, currentCommentId: String, value: Int) {
            updatedReplyCount = currentCommentId to value
        }
        override suspend fun updateLikeCountForCommentInDatabase(selectedNewId: String, likedComment: String, value: Int) {
            updatedLikeCount = Triple(selectedNewId, likedComment, value)
        }
        override suspend fun updateLikeCountForSubCommentInDatabase(selectedNewId: String, likedComment: String, parentCommentId: String, value: Int) {
            updatedSubLikeCount = listOf(selectedNewId, likedComment, parentCommentId, value)
        }
        override suspend fun saveLikedComments(id: String, value: HashMap<String, Int>): Boolean = saveLikedResult
        override suspend fun syncComments(): Boolean = true
        override suspend fun clearComments() {}
    }

    private class FakeCommentRepository(val comments: List<CommentInstance>) : HomeCommentRepository {
        override suspend fun getAllComments(newsId: String): List<CommentInstance> = comments
    }

    @Test
    fun `HomeDeleteCommentFromDatabaseUseCase delegates to repository`() = runTest {
        val repo = FakeCommentDbRepository()
        val comment = CommentInstance(id = "c1")
        HomeDeleteCommentFromDatabaseUseCase(repo).invoke("news1", comment)
        assertEquals("news1" to comment, repo.deletedComment)
    }

    @Test
    fun `HomeDeleteSubCommentFromDatabaseUseCase delegates to repository`() = runTest {
        val repo = FakeCommentDbRepository()
        val comment = CommentInstance(id = "sub1")
        HomeDeleteSubCommentFromDatabaseUseCase(repo).invoke("news1", "parent1", comment)
        assertEquals(Triple("news1", "parent1", comment), repo.deletedSubComment)
    }

    @Test
    fun `HomeGetAllCommentsUseCase filters out blank comments`() = runTest {
        val validComment = CommentInstance(id = "c1", message = "hello")
        val blankComment = CommentInstance(id = "c2", message = "", image = "", video = "")
        val withImage = CommentInstance(id = "c3", message = "", image = "img.png", video = "")
        val repo = FakeCommentRepository(listOf(validComment, blankComment, withImage))

        val result = HomeGetAllCommentsUseCase(repo).invoke("news1")

        assertEquals(listOf("c1", "c3"), result.map { it.id })
    }

    @Test
    fun `HomeSaveCommentToDatabaseUseCase delegates to repository`() = runTest {
        val repo = FakeCommentDbRepository(saveCommentResult = true)
        val result = HomeSaveCommentToDatabaseUseCase(repo).invoke("news1", "c1", CommentInstance(id = "c1"))
        assertTrue(result)
    }

    @Test
    fun `HomeSaveLikedCommentsUseCase delegates to repository`() = runTest {
        val repo = FakeCommentDbRepository(saveLikedResult = false)
        val result = HomeSaveLikedCommentsUseCase(repo).invoke("u1", hashMapOf("c1" to 1))
        assertFalse(result)
    }

    @Test
    fun `HomeSaveSubCommentToDatabaseUseCase delegates to repository`() = runTest {
        val repo = FakeCommentDbRepository(saveSubCommentResult = true)
        val comment = CommentInstance(id = "sub1")
        val result = HomeSaveSubCommentToDatabaseUseCase(repo).invoke("id1", "news1", "parent1", comment)
        assertTrue(result)
    }

    @Test
    fun `HomeUpdateCommentCountForNewUseCase delegates to repository`() = runTest {
        val repo = FakeCommentDbRepository()
        HomeUpdateCommentCountForNewUseCase(repo).invoke("news1", 5)
        assertEquals("news1" to 5, repo.updatedCommentCount)
    }

    @Test
    fun `HomeUpdateLikeCountForCommentUseCase delegates to repository`() = runTest {
        val repo = FakeCommentDbRepository()
        HomeUpdateLikeCountForCommentUseCase(repo).invoke("news1", "c1", 3)
        assertEquals(Triple("news1", "c1", 3), repo.updatedLikeCount)
    }

    @Test
    fun `HomeUpdateLikeCountForSubCommentUseCase delegates to repository`() = runTest {
        val repo = FakeCommentDbRepository()
        HomeUpdateLikeCountForSubCommentUseCase(repo).invoke("news1", "sub1", "parent1", 2)
        assertEquals(listOf("news1", "sub1", "parent1", 2), repo.updatedSubLikeCount)
    }

    @Test
    fun `HomeUpdateReplyCountForCommentUseCase delegates to repository`() = runTest {
        val repo = FakeCommentDbRepository()
        HomeUpdateReplyCountForCommentUseCase(repo).invoke("news1", "c1", 7)
        assertEquals("c1" to 7, repo.updatedReplyCount)
    }
}
