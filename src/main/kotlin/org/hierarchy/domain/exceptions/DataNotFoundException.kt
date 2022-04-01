package org.hierarchy.domain.exceptions

data class DataNotFoundException(
    override val message: String? = "Data not found.",
    val data: String
) : Exception(message)
