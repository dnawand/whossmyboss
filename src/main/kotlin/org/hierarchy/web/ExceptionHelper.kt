package org.hierarchy.web

import org.hierarchy.domain.exceptions.InvalidEntryException

object ExceptionHelper {

    data class ErrorResponse(
        val error: String,
        val data: String
    )

    fun toObjectResponse(invalidEntryException: InvalidEntryException) = ErrorResponse(
        error = invalidEntryException.message ?: "",
        data = invalidEntryException.entry,
    )
}
