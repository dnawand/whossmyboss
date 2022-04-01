package org.hierarchy

import org.hierarchy.domain.Employee
import org.hierarchy.domain.exceptions.InvalidEntryException

class Hierarchy {

    private val roots: MutableMap<String, Employee> = mutableMapOf()
    private val employeeReference: MutableMap<String, Employee> = mutableMapOf()

    fun addHierarchy(subordinateName: String, supervisorName: String) {
        if (subordinateName == supervisorName) {
            throw InvalidEntryException(
                entry = "$subordinateName:$supervisorName",
                message = "Supervisor and subordinate names cannot be the same."
            )
        }

        this.employeeReference[supervisorName]
            ?.also { supervisorFound(it, subordinateName) }
            ?: newSupervisor(subordinateName, supervisorName)
    }

    fun buildHierarchy(): Map<String, Any> {
        val rootsCount = this.roots.count()

        if (rootsCount == 0) {
            return mapOf()
        }

        if (rootsCount > 1) {
            throw InvalidEntryException(
                message = "Found multiple roots.",
                entry = this.roots.keys.joinToString(",")
            )
        }

        return this.roots.keys.first().let { this.roots[it]?.getHierarchyDown() } ?: mapOf()
    }

    fun getRoot(): Employee? {
        return this.roots.keys.first().let { this.roots[it] }
    }

    private fun addRoot(employee: Employee) {
        this.roots[employee.name] = employee
    }

    private fun removeRoot(employeeName: String) {
        this.roots.remove(employeeName)
    }

    private fun addReference(employee: Employee) {
        this.employeeReference[employee.name] = employee
    }

    private fun supervisorFound(supervisor: Employee, subordinateName: String) {
        this.employeeReference[subordinateName]?.let { subordinate ->
            if (supervisor.searchUp(subordinateName) != null || subordinate.searchDown(supervisor.name) != null) {
                throw InvalidEntryException(
                    entry = "$subordinateName:${supervisor.name}",
                    message = "Loop condition found."
                )
            }

            if (subordinate.searchDown(supervisor.name) != null) {
                throw InvalidEntryException(
                    entry = "$subordinateName:${supervisor.name}",
                    message = "Loop condition found."
                )
            }

            supervisor.addSubordinate(subordinate)
            removeRoot(subordinateName)
        } ?: Employee(name = subordinateName).also { subordinate ->
            addReference(subordinate)
            supervisor.addSubordinate(subordinate)
        }
    }

    private fun newSupervisor(subordinateName: String, supervisorName: String) {
        Employee(name = supervisorName).also { supervisor ->
            addRoot(supervisor)
            addReference(supervisor)

            this.employeeReference[subordinateName]
                ?.also {
                    it.addSupervisor(supervisor)
                    removeRoot(it.name)
                }
                ?: Employee(name = subordinateName).also { subordinate ->
                    addReference(subordinate)
                    subordinate.addSupervisor(supervisor)
                }
        }
    }
}
