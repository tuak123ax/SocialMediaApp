package com.minhtu.firesocialmedia.testutil

import com.minhtu.firesocialmedia.data.remote.service.security.IpInfoRemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.coroutineContext

/**
 * Minimal hand-rolled [io.ktor.client.engine.HttpClientEngine] that always returns the given JSON
 * body. Avoids pulling in the `ktor-client-mock` test artifact (not a declared dependency of this
 * module) while still letting [IpInfoRemoteDataSource] be exercised without a real network call.
 *
 * Runs on [Dispatchers.Unconfined] rather than the real IO dispatcher so it resolves synchronously
 * within whichever (possibly virtual-time) test dispatcher is driving the calling coroutine, and hands
 * back a call context rooted in a fresh, parent-less [Job] rather than the ambient one. Ktor's
 * [io.ktor.client.statement.HttpStatement] cleans up every call by completing and joining that call's
 * job; reusing the ambient job here (which, through `HttpClient`'s own structured-concurrency setup,
 * chains back up to the client's long-lived supervisor job) makes that join wait forever since the
 * supervisor is never meant to complete. A standalone [Job] completes/joins immediately once the
 * response body has been consumed, which is what we want for a fake, single-shot in-memory response.
 */
@OptIn(InternalAPI::class)
private class FakeJsonHttpClientEngine(private val responseJson: String) : HttpClientEngineBase("fake-engine") {
    override val config: HttpClientEngineConfig = HttpClientEngineConfig().apply {
        dispatcher = Dispatchers.Unconfined
    }

    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        return HttpResponseData(
            statusCode = HttpStatusCode.OK,
            requestTime = GMTDate(),
            headers = headersOf(HttpHeaders.ContentType, listOf("application/json")),
            version = HttpProtocolVersion.HTTP_1_1,
            body = ByteReadChannel(responseJson),
            callContext = coroutineContext + Job()
        )
    }
}

/** Builds an [IpInfoRemoteDataSource] backed by a fake in-memory HTTP engine returning [responseJson]. */
fun fakeIpInfoRemoteDataSource(
    responseJson: String = """{"ip":"1.2.3.4","city":"Somewhere","region":"Somewhere","country":"US","loc":"0,0","org":"Test","timezone":"UTC"}"""
): IpInfoRemoteDataSource {
    val client = HttpClient(FakeJsonHttpClientEngine(responseJson)) {
        install(ContentNegotiation) { json() }
    }
    return IpInfoRemoteDataSource(client, "test-token")
}
