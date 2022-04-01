package org.hierarchy.domain.ports.out

import org.hierarchy.domain.Employee

interface HierarchyRepository {
    fun save(employee: Employee): Employee
    fun select(employeeName: String, supervisorLevel: Int, subordinateLevel: Int): Employee
}
