package org.hierarchy.repositories

import jakarta.inject.Singleton
import org.hierarchy.domain.Employee
import org.hierarchy.domain.exceptions.DataNotFoundException
import org.hierarchy.domain.ports.out.HierarchyRepository
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import javax.sql.DataSource

@Singleton
class HierarchyRepositoryImpl(
    private val dataSource: DataSource
) : HierarchyRepository {

    override fun save(employee: Employee): Employee {
        var conn: Connection? = null
        var insertStatement: PreparedStatement? = null

        try {
            conn = this.dataSource.connection
            conn.autoCommit = false

            val insertEmployees = "INSERT INTO employees (name, supervisor_name) VALUES (?, ?);"
            insertStatement = conn.prepareStatement(insertEmployees)
            insertStatement.setString(1, employee.name)
            insertStatement.setString(2, employee.supervisor?.name)
            insertStatement.addBatch()

            employee.walkSubordinatesTree { subordinate, supervisor ->
                insertStatement.setString(1, subordinate.name)
                insertStatement.setString(2, supervisor?.name)
                insertStatement.addBatch()
            }

            insertStatement.executeBatch()
            conn.commit()
        } catch (sqlException: SQLException) {
            throw sqlException
        } finally {
            conn?.close()
            insertStatement?.close()
        }

        return employee
    }

    override fun select(employeeName: String, supervisorLevel: Int, subordinateLevel: Int): Employee {
        val employee = selectEmployee(employeeName)
        val firstLevel = 1

        if (supervisorLevel == 0 && subordinateLevel == 0) {
            return employee
        }

        var conn: Connection? = null
        return try {
            conn = this.dataSource.connection
            conn.autoCommit = false

            employee.apply {
                if (supervisorLevel > 0) {
                    selectSupervisor(
                        conn = conn,
                        currentLevel = firstLevel,
                        maxLevel = supervisorLevel,
                        employee = employee,
                    )
                }
            }.apply {
                if (subordinateLevel > 0) {
                    selectSubordinates(
                        conn = conn,
                        currentLevel = firstLevel,
                        maxLevel = subordinateLevel,
                        employee = this,
                    )
                }
            }
        } catch (sqlException: SQLException) {
            throw sqlException
        } finally {
            conn?.commit()
        }
    }

    private fun selectEmployee(employeeName: String): Employee {
        var conn: Connection? = null
        var selectStatement: PreparedStatement? = null

        return try {
            conn = this.dataSource.connection
            conn.autoCommit = false

            val selectEmployeeQuery = "SELECT name FROM employees WHERE name = ?;"
            selectStatement = conn.prepareStatement(selectEmployeeQuery)
            selectStatement.setString(1, employeeName)
            selectStatement.executeQuery().let {
                if (!it.next()) {
                    throw DataNotFoundException(data = "Employee")
                }

                Employee(name = employeeName)
            }
        } catch (sqlException: SQLException) {
            throw sqlException
        } finally {
            conn?.commit()
            selectStatement?.close()
        }
    }

    private fun selectSupervisor(
        conn: Connection,
        currentLevel: Int,
        maxLevel: Int,
        employee: Employee,
    ): Employee {
        if (currentLevel > maxLevel) {
            return employee
        }

        var selectStatement: PreparedStatement? = null
        return try {
            val selectSupervisorQuery = "SELECT supervisor_name FROM employees WHERE name = ?;"
            selectStatement = conn.prepareStatement(selectSupervisorQuery)
            selectStatement.setString(1, employee.name)
            selectStatement.executeQuery().let {
                if (it.next()) {
                    val supervisorName: String = it.getString("supervisor_name") ?: return employee
                    val supervisor = Employee(name = supervisorName)
                    employee.addSupervisor(supervisor)
                    selectSupervisor(
                        conn = conn,
                        currentLevel = currentLevel + 1,
                        maxLevel = maxLevel,
                        employee = supervisor
                    )
                }

                employee
            }
        } catch (sqlException: SQLException) {
            throw sqlException
        } finally {
            selectStatement?.close()
        }
    }

    private fun selectSubordinates(
        conn: Connection,
        currentLevel: Int,
        maxLevel: Int,
        employee: Employee,
    ): Employee {
        if (currentLevel > maxLevel) {
            return employee
        }

        var selectStatement: PreparedStatement? = null
        return try {
            val subordinates = mutableListOf<Employee>()
            val selectSubordinatesQuery = "SELECT name FROM employees WHERE supervisor_name = ?;"
            selectStatement = conn.prepareStatement(selectSubordinatesQuery)
            selectStatement.setString(1, employee.name)
            selectStatement.executeQuery().let {
                while (it.next()) {
                    val subordinateName: String = it.getString("name")
                    val subordinate = Employee(name = subordinateName)
                    subordinate.addSupervisor(employee)
                    subordinates.add(subordinate)
                }
            }

            if (subordinates.isEmpty()) {
                return employee
            }

            subordinates
                .map { selectSubordinates(conn, currentLevel + 1, maxLevel, it) }
                .forEach { employee.addSubordinate(it) }

            employee
        } catch (sqlException: SQLException) {
            throw sqlException
        } finally {
            selectStatement?.close()
        }
    }
}
