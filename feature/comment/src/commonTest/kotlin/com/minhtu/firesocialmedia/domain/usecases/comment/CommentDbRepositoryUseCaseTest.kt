package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.base.BaseNewsInstance
import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.CommentDbRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Shared fake used by the tests for the small, single-purpose use cases
 * that delegate directly to [CommentDbRepository].
 */
private class FakeCommentDbRepository : CommentDbRepository {
    var saveCommentResult = true
    var saveSubCommentResult = true
    var saveLikedCommentsResult = true

    var savedComment: Triple<String, String, CommentInstance>? = null
    var savedSubComment: Triple<String, String, BaseNewsInstance>? = null
    var deletedComment: Pair<String, BaseNewsInstance>? = null
    var deletedSubComment: Triple<String, String, BaseNewsInstance>? = null
    var commentCountUpdate: Pair<String, Int>? = null
    var replyCountUpdate: Triple<String, String, Int>? = null
    var likeCountUpdate: Triple<String, String, Int>? = null
    var subLikeCountUpdate: List<Any>? = null
    var savedLikedComments: Pair<String, HashMap<String, Int>>? = null

    override suspend fun saveCommentToDatabase(selectedNewId: String, commentId: String, instance: CommentInstance): Boolean {
        savedComment = Triple(selectedNewId, commentId, instance)
        return saveCommentResult
    }

    override suspend fun saveSubCommentToDatabase(id: String, selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance): Boolean {
        savedSubComment = Triple(id, parentCommentId, instance)
        return saveSubCommentResult
    }

    override suspend fun deleteCommentFromDatabase(selectedNewId: String, instance: BaseNewsInstance) {
        deletedComment = selectedNewId to instance
    }

    override suspend fun deleteSubCommentFromDatabase(selectedNewId: String, parentCommentId: String, instance: BaseNewsInstance) {
        deletedSubComment = Triple(selectedNewId, parentCommentId, instance)
    }

    override suspend fun updateCommentCountForNewInDatabase(id: String, value: Int) {
        commentCountUpdate = id to value
    }

    override suspend fun updateReplyCountForCommentInDatabase(id: String, currentCommentId: String, value: Int) {
        replyCountUpdate = Triple(id, currentCommentId, value)
    }

    override suspend fun updateLikeCountForCommentInDatabase(selectedNewId: String, likedComment: String, value: Int) {
        likeCountUpdate = Triple(selectedNewId, likedComment, value)
    }

    override suspend fun updateLikeCountForSubCommentInDatabase(selectedNewId: String, likedComment: String, parentCommentId: String, value: Int) {
        subLikeCountUpdate = listOf(selectedNewId, likedComment, parentCommentId, value)
    }

    override suspend fun saveLikedComments(id: String, map: HashMap<String, Int>): Boolean {
        savedLikedComments = id to map
        return saveLikedCommentsResult
    }

    override suspend fun syncComments(): Boolean = true
    override suspend fun clearComments() {}
}

class SaveCommentToDatabaseUseCaseTest {
    @Test
    fun `invoke delegates to repository and returns its result`() = runTest {
        val repository = FakeCommentDbRepository().apply { saveCommentResult = true }
        val useCase = SaveCommentToDatabaseUseCase(repository)
        val comment = CommentInstance(id = "c1", message = "hi")

        val result = useCase.invoke("news-1", "c1", comment)

        assertTrue(result)
        assertEquals(Triple("news-1", "c1", comment), repository.savedComment)
    }

    @Test
    fun `invoke surfaces repository failure`() = runTest {
        val repository = FakeCommentDbRepository().apply { saveCommentResult = false }
        val useCase = SaveCommentToDatabaseUseCase(repository)

        val result = useCase.invoke("news-1", "c1", CommentInstance(id = "c1"))

        assertFalse(result)
    }
}

class SaveSubCommentToDatabaseUseCaseTest {
    @Test
    fun `invoke delegates to repository and returns its result`() = runTest {
        val repository = FakeCommentDbRepository().apply { saveSubCommentResult = true }
        val useCase = SaveSubCommentToDatabaseUseCase(repository)
        val reply = CommentInstance(id = "r1", message = "reply")

        val result = useCase.invoke("r1", "news-1", "c1", reply)

        assertTrue(result)
        assertEquals(Triple("r1", "c1", reply as BaseNewsInstance), repository.savedSubComment)
    }

    @Test
    fun `invoke surfaces repository failure`() = runTest {
        val repository = FakeCommentDbRepository().apply { saveSubCommentResult = false }
        val useCase = SaveSubCommentToDatabaseUseCase(repository)

        val result = useCase.invoke("r1", "news-1", "c1", CommentInstance(id = "r1"))

        assertFalse(result)
    }
}

class DeleteCommentFromDatabaseUseCaseTest {
    @Test
    fun `invoke delegates deletion to repository`() = runTest {
        val repository = FakeCommentDbRepository()
        val useCase = DeleteCommentFromDatabaseUseCase(repository)
        val comment = CommentInstance(id = "c1")

        useCase.invoke("news-1", comment)

        assertEquals("news-1" to (comment as BaseNewsInstance), repository.deletedComment)
    }
}

class DeleteSubCommentFromDatabaseUseCaseTest {
    @Test
    fun `invoke delegates deletion to repository with parent id`() = runTest {
        val repository = FakeCommentDbRepository()
        val useCase = DeleteSubCommentFromDatabaseUseCase(repository)
        val reply = CommentInstance(id = "r1")

        useCase.invoke("news-1", "c1", reply)

        assertEquals(Triple("news-1", "c1", reply as BaseNewsInstance), repository.deletedSubComment)
    }
}

class UpdateCommentCountForNewUseCaseTest {
    @Test
    fun `invoke updates comment count for the given news id`() = runTest {
        val repository = FakeCommentDbRepository()
        val useCase = UpdateCommentCountForNewUseCase(repository)

        useCase.invoke("news-1", 5)

        assertEquals("news-1" to 5, repository.commentCountUpdate)
    }
}

class UpdateReplyCountForCommentUseCaseTest {
    @Test
    fun `invoke updates reply count for a comment`() = runTest {
        val repository = FakeCommentDbRepository()
        val useCase = UpdateReplyCountForCommentUseCase(repository)

        useCase.invoke("news-1", "c1", 2)

        assertEquals(Triple("news-1", "c1", 2), repository.replyCountUpdate)
    }
}

class UpdateLikeCountForCommentUseCaseTest {
    @Test
    fun `invoke updates like count for a comment`() = runTest {
        val repository = FakeCommentDbRepository()
        val useCase = UpdateLikeCountForCommentUseCase(repository)

        useCase.invoke("news-1", "c1", 3)

        assertEquals(Triple("news-1", "c1", 3), repository.likeCountUpdate)
    }
}

class UpdateLikeCountForSubCommentUseCaseTest {
    @Test
    fun `invoke updates like count for a sub comment`() = runTest {
        val repository = FakeCommentDbRepository()
        val useCase = UpdateLikeCountForSubCommentUseCase(repository)

        useCase.invoke("news-1", "r1", "c1", 4)

        assertEquals(listOf("news-1", "r1", "c1", 4), repository.subLikeCountUpdate)
    }
}

class SaveLikedCommentsUseCaseTest {
    @Test
    fun `invoke saves liked comments map and returns result`() = runTest {
        val repository = FakeCommentDbRepository().apply { saveLikedCommentsResult = true }
        val useCase = SaveLikedCommentsUseCase(repository)
        val likes = hashMapOf("c1" to 1)

        val result = useCase.invoke("user-1", likes)

        assertTrue(result)
        assertEquals("user-1" to likes, repository.savedLikedComments)
    }

    @Test
    fun `invoke surfaces repository failure`() = runTest {
        val repository = FakeCommentDbRepository().apply { saveLikedCommentsResult = false }
        val useCase = SaveLikedCommentsUseCase(repository)

        val result = useCase.invoke("user-1", hashMapOf())

        assertFalse(result)
    }
}
