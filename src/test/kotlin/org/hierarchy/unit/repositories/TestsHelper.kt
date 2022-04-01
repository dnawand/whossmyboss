package org.hierarchy.unit.repositories

import jakarta.inject.Singleton
import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource

@Singleton
class TestsHelper(
    private val dataSource: DataSource,
) {

    fun executeQuery(conn: Connection, statement: String, vararg args: Any): ResultSet {
        return try {
            val preparedStatement = conn.prepareStatement(statement)

            for (i in args.withIndex()) {
                preparedStatement.setObject(i.index + 1, i.value)
            }
            preparedStatement.executeQuery()
        } catch (exception: Exception) {
            throw exception
        }
    }

    fun executeStatement(conn: Connection, statement: String, vararg args: Any): Boolean {
        return try {
            val preparedStatement = conn.prepareStatement(statement)
            for (i in args.withIndex()) {
                preparedStatement.setObject(i.index + 1, i.value)
            }
            preparedStatement.execute()
        } catch (exception: Exception) {
            throw exception
        }
    }

    fun truncateTables() {
        var conn: Connection? = null
        try {
            conn = dataSource.connection
            executeStatement(conn, "SET REFERENTIAL_INTEGRITY FALSE;")
            executeStatement(conn, "TRUNCATE TABLE employees;")
            executeStatement(conn, "SET REFERENTIAL_INTEGRITY TRUE;")
        } finally {
            conn?.close()
        }
    }
}
