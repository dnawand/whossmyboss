package org.hierarchy.unit.domain

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hierarchy.domain.Employee
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@MicronautTest
class EmployeeTest {

    @Test
    fun `should successfully return the supervisors hierarchy`() {
        val employee1 = Employee(name = "Nick")
        val employee2 = Employee(name = "Rob")
        val employee3 = Employee(name = "Julia")

        employee1.also { nick ->
            nick.addSubordinate(
                employee3.also { julia ->
                    julia.addSubordinate(employee2)
                }
            )
        }

        Assertions.assertEquals(
            mapOf(
                "Nick" to mapOf<String, Any>(
                    "Julia" to mapOf<String, Any>(
                        "Rob" to mapOf<Boolean, Boolean>()
                    )
                )
            ),
            employee3.getHierarchyUp()
        )
    }
}
