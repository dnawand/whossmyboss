package org.hierarchy.web

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.micronaut.ktor.KtorRoutingBuilder
import jakarta.inject.Singleton
import org.hierarchy.domain.exceptions.DataNotFoundException
import org.hierarchy.domain.exceptions.InvalidEntryException
import org.hierarchy.domain.ports.`in`.EmployeeService

@Singleton
class HierarchyRoute(
    private val employeeService: EmployeeService
) : KtorRoutingBuilder({
    authenticate("auth-basic") {
        post("/hierarchy") {
            try {
                val hierarchyMapping = call.receive<Map<String, String>>()

                call.respond(status = HttpStatusCode.Created, employeeService.solveHierarchy(hierarchyMapping))
            } catch (invalidEntryException: InvalidEntryException) {
                call.respond(status = HttpStatusCode.BadRequest, ExceptionHelper.toObjectResponse(invalidEntryException))
            } catch (exception: Exception) {
                call.respondText("Internal server error", status = HttpStatusCode.InternalServerError)
            }
        }

        get("/hierarchy/{name}") {
            val employeeName = call.parameters["name"]
                ?: return@get call.respondText("Bad request", status = HttpStatusCode.BadRequest)

            try {
                call.respond(employeeService.getSupervisors(employeeName))
            } catch (dataNotFoundException: DataNotFoundException) {
                call.respondText("Not found", status = HttpStatusCode.NotFound)
            } catch (exception: Exception) {
                call.respondText("Internal server error", status = HttpStatusCode.InternalServerError)
            }
        }
    }
})
