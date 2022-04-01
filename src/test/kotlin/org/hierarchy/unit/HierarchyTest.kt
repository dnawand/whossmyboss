package org.hierarchy.unit

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hierarchy.Hierarchy
import org.hierarchy.domain.exceptions.InvalidEntryException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@MicronautTest
class HierarchyTest {

    class TestEntry(
        val description: String = "",
        val input: MutableMap<String, String>,
        val output: Any
    )

    @Test
    fun `should successfully produce hierarchies based on inputs`() {
        val tableTests = listOf(
            TestEntry(
                input = mutableMapOf(
                    "Pete" to "Nick",
                    "Barbara" to "Nick",
                    "Nick" to "Sophie",
                    "Sophie" to "Jonas"
                ),
                output = mutableMapOf(
                    "Jonas" to mutableMapOf<String, Any>(
                        "Sophie" to mutableMapOf<String, Any>(
                            "Nick" to mutableMapOf<String, Any>(
                                "Pete" to emptyMap<Boolean, Boolean>(),
                                "Barbara" to emptyMap<Boolean, Boolean>()
                            )
                        )
                    )
                )
            ),
            TestEntry(
                input = mutableMapOf(
                    "Pete" to "Nick",
                    "Barbara" to "Nick",
                    "Nick" to "Sophie",
                    "Sophie" to "Jonas",
                    "Gilbert" to "Jonas",
                ),
                output = mutableMapOf(
                    "Jonas" to mutableMapOf<String, Any>(
                        "Sophie" to mutableMapOf<String, Any>(
                            "Nick" to mutableMapOf<String, Any>(
                                "Pete" to emptyMap<Boolean, Boolean>(),
                                "Barbara" to emptyMap<Boolean, Boolean>()
                            )
                        ),
                        "Gilbert" to emptyMap<Boolean, Boolean>()
                    )
                )
            )
        )

        for (te in tableTests) {
            val hierarchy = Hierarchy()
            for ((subordinate, supervisor) in te.input) {
                hierarchy.addHierarchy(subordinate, supervisor)
            }
            Assertions.assertEquals(te.output, hierarchy.buildHierarchy())
        }
    }

    @Test
    fun `should throw exception based on the entries`() {
        val tableTests = listOf(
            TestEntry(
                description = "Loop condition",
                input = mutableMapOf(
                    "Pete" to "Nick",
                    "Barbara" to "Nick",
                    "Nick" to "Sophie",
                    "Sophie" to "Jonas",
                    "Jonas" to "Nick",
                ),
                output = InvalidEntryException(
                    entry = "Jonas:Nick",
                    message = "Loop condition found.",
                )
            ),
            TestEntry(
                description = "Same names",
                input = mutableMapOf(
                    "Pete" to "Nick",
                    "Barbara" to "Barbara",
                    "Nick" to "Sophie",
                    "Sophie" to "Jonas",
                ),
                output = InvalidEntryException(
                    entry = "Barbara:Barbara",
                    message = "Supervisor and subordinate names cannot be the same.",
                )
            ),

            TestEntry(
                description = "Multiple roots",
                input = mutableMapOf(
                    "Pete" to "Nick",
                    "Barbara" to "Nick",
                    "Nick" to "Sophie",
                    "Sophie" to "Jonas",
                    "Gilbert" to "Joseph",
                ),
                output = InvalidEntryException(
                    entry = "Jonas,Joseph",
                    message = "Found multiple roots.",
                )
            )
        )

        for (te in tableTests) {
            val hierarchy = Hierarchy()

            val thrown = Assertions.assertThrows(
                InvalidEntryException::class.java, {
                    for ((subordinate, supervisor) in te.input) {
                        hierarchy.addHierarchy(subordinate, supervisor)
                    }

                    hierarchy.buildHierarchy()
                },
                te.description
            )

            (te.output as InvalidEntryException).let {
                Assertions.assertEquals(it.entry, thrown.entry, te.description)
                Assertions.assertEquals(it.message, thrown.message, te.description)
            }
        }
    }
}
