package org.hierarchy.services

import jakarta.inject.Singleton
import org.hierarchy.Hierarchy
import org.hierarchy.domain.ports.`in`.EmployeeService
import org.hierarchy.domain.ports.out.HierarchyRepository

@Singleton
class EmployeeServiceImpl(
    private val hierarchyRepository: HierarchyRepository
) : EmployeeService {

    override fun solveHierarchy(hierarchyMapping: Map<String, String>): Map<String, Any> = Hierarchy().let {
        for ((subordinate, supervisor) in hierarchyMapping) {
            it.addHierarchy(subordinate, supervisor)
        }

        it.buildHierarchy().also { _ -> hierarchyRepository.save(it.getRoot()!!) }
    }

    override fun getSupervisors(employeeName: String): Map<String, Any> {
        return hierarchyRepository.select(employeeName, supervisorLevel = 2, subordinateLevel = 0).getHierarchyUp()
    }
}
