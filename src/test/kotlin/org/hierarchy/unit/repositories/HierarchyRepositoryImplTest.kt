package org.hierarchy.unit.repositories

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hierarchy.domain.Employee
import org.hierarchy.domain.exceptions.DataNotFoundException
import org.hierarchy.domain.ports.out.HierarchyRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.sql.Connection
import javax.sql.DataSource

@MicronautTest
class HierarchyRepositoryImplTest(
    private val hierarchyRepository: HierarchyRepository,
    private val dataSource: DataSource,
    private val testsHelper: TestsHelper
) {

    @AfterEach
    fun truncateTables() {
        testsHelper.truncateTables()
    }

    @Test
    fun `should successfully store an employee hierarchy`() {
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

        hierarchyRepository.save(employee1)

        var conn: Connection? = null
        val subordinateNames = try {
            conn = dataSource.connection

            testsHelper.executeQuery(
                conn,
                "SELECT name FROM employees WHERE supervisor_name in (?, ?);",
                employee1.name,
                employee3.name,
            ).let {
                mutableListOf<String>().also { names ->
                    while (it.next()) {
                        names.add(it.getString("name"))
                    }
                }
            }
        } catch (exception: Exception) {
            throw exception
        } finally {
            conn?.close()
        }

        Assertions.assertTrue(subordinateNames.contains(employee2.name))
        Assertions.assertTrue(subordinateNames.contains(employee3.name))
    }

    @Test
    fun `should return a EmployeeHierarchy after storing an employee hierarchy`() {
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

        hierarchyRepository.save(employee1)

        hierarchyRepository.select(employee2.name, 2, 0).also {
            Assertions.assertEquals(employee2.name, it.name)
            Assertions.assertNotNull(it.supervisor)
            Assertions.assertEquals(employee3.name, it.supervisor?.name)
            Assertions.assertNotNull(it.supervisor?.supervisor)
            Assertions.assertEquals(employee1.name, it.supervisor?.supervisor?.name)
        }
    }

    @Test
    fun `should return only the specified supervisor level of hierarchy`() {
        val employee1 = Employee(name = "Julia")
        val employee2 = Employee(name = "Nick")
        val employee3 = Employee(name = "Rob")
        val employee4 = Employee(name = "Lucas")

        employee1.also { julia ->
            julia.addSubordinate(
                employee2.also { nick ->
                    nick.addSubordinate(
                        employee3.also { rob ->
                            rob.addSubordinate(employee4)
                        }
                    )
                }
            )
        }

        hierarchyRepository.save(employee1)

        hierarchyRepository.select(employee4.name, 2, 0).also {
            Assertions.assertEquals(employee4.name, it.name)
            Assertions.assertNotNull(it.supervisor)
            Assertions.assertEquals(employee3.name, it.supervisor?.name)
            Assertions.assertNotNull(it.supervisor?.supervisor)
            Assertions.assertEquals(employee2.name, it.supervisor?.supervisor?.name)
            Assertions.assertNull(it.supervisor?.supervisor?.supervisor)
        }
    }

    @Test
    fun `should return only the specified supervisor and subordinate levels of hierarchy`() {
        val employee1 = Employee(name = "Julia")
        val employee2 = Employee(name = "Nick")
        val employee3 = Employee(name = "Rob")
        val employee4 = Employee(name = "Lucas")
        val employee5 = Employee(name = "Patrik")
        val employee6 = Employee(name = "Joseph")
        val employee7 = Employee(name = "Robert")
        employee1.also { julia ->
            julia.addSubordinate(
                employee2.also { nick ->
                    nick.addSubordinate(
                        employee3.also { rob ->
                            rob.addSubordinate(employee4)
                            rob.addSubordinate(
                                employee5.also { patrik ->
                                    patrik.addSubordinate(
                                        employee6.also { joseph ->
                                            joseph.addSubordinate(employee7)
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            )
        }

        hierarchyRepository.save(employee1)
        hierarchyRepository.select(employee3.name, 1, 2).also {
            Assertions.assertEquals(employee3.name, it.name)

            // supervisor level 1
            Assertions.assertNotNull(it.supervisor)
            Assertions.assertEquals(employee2.name, it.supervisor?.name)

            // supervisor level 2
            Assertions.assertNull(it.supervisor?.supervisor)

            // subordinate level 1
            Assertions.assertEquals(2, it.subordinates.size)
            Assertions.assertTrue(it.subordinates.any { s -> s.name == employee4.name })
            Assertions.assertTrue(it.subordinates.any { s -> s.name == employee5.name })

            // subordinate level 2
            Assertions.assertTrue(
                it.subordinates.first { s -> s.name == employee4.name }.subordinates.isEmpty(),
            )
            Assertions.assertFalse(
                it.subordinates.first { s -> s.name == employee5.name }.subordinates.isEmpty(),
            )
            Assertions.assertTrue(
                it.subordinates.first { s -> s.name == employee5.name }
                    .subordinates.first().name == employee6.name,
            )
            Assertions.assertTrue(
                it.subordinates.first { s -> s.name == employee5.name }
                    .subordinates.first()
                    .subordinates.isEmpty(),
            )
        }
    }

    @Test
    fun `should throw DataNotFoundException if a user is not found`() {
        val nonExistentEmployeeName = "Patrik"

        val thrown = Assertions.assertThrows(DataNotFoundException::class.java) {
            hierarchyRepository.select(nonExistentEmployeeName, 1, 2)
        }

        Assertions.assertEquals("Employee", thrown.data)
    }
}
