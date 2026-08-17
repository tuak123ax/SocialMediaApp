package com.minhtu.firesocialmedia.domain.usecases.comment

import com.minhtu.firesocialmedia.domain.entity.comment.CommentInstance
import com.minhtu.firesocialmedia.domain.repository.CommentRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeCommentRepository : CommentRepository {
    var comments: List<CommentInstance> = emptyList()
    override suspend fun getAllComments(newsId: String): List<CommentInstance> = comments
}

class GetAllCommentsUseCaseTest {

    @Test
    fun `invoke filters out comments with no message, image or video`() = runTest {
        val repository = FakeCommentRepository().apply {
            comments = listOf(
                CommentInstance(id = "c1", message = "hello"),
                CommentInstance(id = "c2", message = "", image = "", video = ""),
                CommentInstance(id = "c3", image = "pic.png"),
                CommentInstance(id = "c4", video = "clip.mp4")
            )
        }
        val useCase = GetAllCommentsUseCase(repository)

        val result = useCase.invoke("news-1")

        assertEquals(listOf("c1", "c3", "c4"), result.map { it.id })
    }

    @Test
    fun `invoke returns empty list when repository has nothing`() = runTest {
        val repository = FakeCommentRepository().apply { comments = emptyList() }
        val useCase = GetAllCommentsUseCase(repository)

        val result = useCase.invoke("news-1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke drops entirely blank comments`() = runTest {
        val repository = FakeCommentRepository().apply {
            comments = listOf(CommentInstance(id = "c1", message = "   "))
        }
        val useCase = GetAllCommentsUseCase(repository)

        val result = useCase.invoke("news-1")

        assertTrue(result.isEmpty())
    }
}
