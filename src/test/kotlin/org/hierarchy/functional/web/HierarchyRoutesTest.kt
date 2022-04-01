package org.hierarchy.functional.web

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.hierarchy.unit.repositories.TestsHelper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@MicronautTest
class HierarchyRoutesTest(
    private val testsHelper: TestsHelper
) {

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    private val user = "user"
    private val password = "password"

    @AfterEach
    fun truncateTables() {
        testsHelper.truncateTables()
    }

    @Test
    fun `should return 201 after posting a hierarchy mapping without errors`() {
        val body = """
            {
                "Pete": "Nick",
                "Barbara": "Nick",
                "Nick": "Sophie",
                "Sophie": "Jonas"
            }
        """.trimIndent()

        val response = client.toBlocking().exchange(
            HttpRequest.POST("/hierarchy", body).basicAuth(user, password),
            Argument.of(Map::class.java),
        )

        Assertions.assertEquals(HttpStatus.CREATED, response.status)
        Assertions.assertEquals(
            """
            |{Jonas={Sophie={Nick={Pete={}, Barbara={}}}}}
            """.trimMargin(),
            response.body().toString()
        )
    }

    @Test
    fun `should return 400 when posting a hierarchy mapping with loop condition`() {
        val body = """
            {
                "Pete": "Nick",
                "Barbara": "Nick",
                "Nick": "Sophie",
                "Sophie": "Jonas",
                "Jonas": "Nick"
            }
        """.trimIndent()

        val thrown = Assertions.assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().exchange(
                HttpRequest.POST("/hierarchy", body).basicAuth(user, password),
                String::class.java,
            )
        }

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, thrown.status)
        Assertions.assertEquals(
            """
            |{
            |  "error" : "Loop condition found.",
            |  "data" : "Jonas:Nick"
            |}
            """.trimMargin(),
            thrown.response?.body().toString()
        )
    }

    @Test
    fun `should return 400 when posting a hierarchy mapping with same-name entries`() {
        val body = """
            {
                "Pete": "Pete",
                "Barbara": "Nick",
                "Nick": "Sophie",
                "Sophie": "Jonas",
                "Jonas": "Nick"
            }
        """.trimIndent()

        val thrown = Assertions.assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().exchange(
                HttpRequest.POST("/hierarchy", body).basicAuth(user, password),
                String::class.java,
            )
        }

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, thrown.status)
        Assertions.assertEquals(
            """
            |{
            |  "error" : "Supervisor and subordinate names cannot be the same.",
            |  "data" : "Pete:Pete"
            |}
            """.trimMargin(),
            thrown.response?.body().toString()
        )
    }

    @Test
    fun `should return 400 when posting a hierarchy mapping with multiple roots`() {
        val body = """
            {
                "Pete": "Nick",
                "Barbara": "Nick",
                "Nick": "Sophie",
                "Sophie": "Jonas",
                "Gilbert": "Joseph"
            }
        """.trimIndent()

        val thrown = Assertions.assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().exchange(
                HttpRequest.POST("/hierarchy", body).basicAuth(user, password),
                String::class.java,
            )
        }

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, thrown.status)
        Assertions.assertEquals(
            """
            |{
            |  "error" : "Found multiple roots.",
            |  "data" : "Jonas,Joseph"
            |}
            """.trimMargin(),
            thrown.response?.body().toString()
        )
    }

    @Test
    fun `should return 404 when requesting supervisor for an unknown employee`() {
        val thrown = Assertions.assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().exchange(
                HttpRequest.GET<Any>("/hierarchy/User").basicAuth(user, password),
                Any::class.java,
            )
        }

        Assertions.assertEquals(HttpStatus.NOT_FOUND, thrown.status)
    }
}
