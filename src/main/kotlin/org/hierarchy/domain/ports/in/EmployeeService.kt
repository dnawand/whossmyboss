package org.hierarchy.domain.ports.`in`

interface EmployeeService {
    fun solveHierarchy(hierarchyMapping: Map<String, String>): Map<String, Any>
    fun getSupervisors(employeeName: String): Map<String, Any>
}
