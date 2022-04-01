package org.hierarchy.domain.exceptions

data class InvalidEntryException(
    override val message: String?,
    val entry: String
) : Exception(message)
