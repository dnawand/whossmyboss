package org.hierarchy.domain

data class Employee(
    val name: String,
    val subordinates: MutableList<Employee> = mutableListOf()
) {

    var supervisor: Employee? = null
        private set

    fun addSupervisor(employee: Employee) {
        this.supervisor = employee

        if (!employee.hasSubordinate(this.name)) {
            employee.addSubordinate(this)
        }
    }

    fun addSubordinate(employee: Employee) {
        employee.supervisor = this

        if (hasSubordinate(employee.name)) {
            return
        }

        this.subordinates.add(employee)
    }

    fun searchDown(employeeName: String): Employee? {
        return searchSubordinates(employeeName, this.subordinates)
    }

    fun searchUp(employeeName: String): Employee? {
        return searchSupervisors(employeeName, this.supervisor)
    }

    fun search(employeeName: String): Employee? =
        searchSubordinates(employeeName, this.subordinates)
            ?: searchSupervisors(employeeName, this.supervisor)

    fun walkSubordinatesTree(f: (Employee, Employee?) -> Unit) {
        if (this.subordinates.isEmpty()) {
            return
        }

        for (subordinate in this.subordinates) {
            f(subordinate, subordinate.supervisor)
            subordinate.walkSubordinatesTree(f)
        }
    }

    fun getHierarchyUp(): MutableMap<String, Any> {
        this.supervisor ?: return this.getHierarchyDown()

        return this.supervisor!!.getHierarchyUp()
    }

    fun getHierarchyDown(): MutableMap<String, Any> {
        return mutableMapOf(
            this.name to this.getSubordinatesHierarchy()
        )
    }

    private fun hasSubordinate(employeeName: String): Boolean =
        this.subordinates.any {
            it.name == employeeName
        }

    private fun searchSubordinates(employeeName: String, subordinates: List<Employee>): Employee? {
        if (subordinates.isEmpty()) {
            return null
        }

        val subordinate = subordinates.first()

        if (subordinate.name == employeeName) {
            return subordinate
        }

        if (subordinate.subordinates.isEmpty()) {
            return null
        }

        return searchSubordinates(employeeName, subordinates.drop(1))
    }

    private fun searchSupervisors(employeeName: String, supervisor: Employee?): Employee? {
        supervisor ?: return null

        if (supervisor.name == employeeName) {
            return supervisor
        }

        return searchSubordinates(employeeName, supervisor.subordinates)
            ?: searchSupervisors(employeeName, supervisor.supervisor)
    }

    private fun getSubordinatesHierarchy(): MutableMap<String, Any> {
        if (this.subordinates.isEmpty()) {
            return mutableMapOf()
        }

        return mutableMapOf<String, Any>()
            .also {
                for (subordinate in this.subordinates) {
                    it[subordinate.name] = subordinate.getSubordinatesHierarchy()
                }
            }
    }
}
