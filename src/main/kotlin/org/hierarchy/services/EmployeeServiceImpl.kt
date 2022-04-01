package org.hierarchy.services

import jakarta.inject.Singleton
import mu.KLogging
import org.hierarchy.Hierarchy
import org.hierarchy.domain.exceptions.InvalidEntryException
import org.hierarchy.domain.ports.`in`.EmployeeService
import org.hierarchy.domain.ports.out.HierarchyRepository
import java.sql.SQLException

@Singleton
class EmployeeServiceImpl(
    private val hierarchyRepository: HierarchyRepository
) : EmployeeService {

    override fun solveHierarchy(hierarchyMapping: Map<String, String>): Map<String, Any> = Hierarchy().let {
        try {
            for ((subordinate, supervisor) in hierarchyMapping) {
                it.addHierarchy(subordinate, supervisor)
            }

            it.buildHierarchy().also { _ -> hierarchyRepository.save(it.getRoot()!!) }
        } catch (invalidEntryException: InvalidEntryException) {
            logger.error(invalidEntryException) { "Entry error when solving hierarchy" }
            throw invalidEntryException
        } catch (sqlException: SQLException) {
            logger.error(sqlException) { "error while storing hierarchy and employees" }
            throw sqlException
        } catch (exception: Exception) {
            logger.error(exception) { "Unknown error while solving hierarchy" }
            throw exception
        }
    }

    override fun getSupervisors(employeeName: String): Map<String, Any> {
        return hierarchyRepository.select(employeeName, supervisorLevel = 2, subordinateLevel = 0).getHierarchyUp()
    }

    companion object : KLogging()
}
