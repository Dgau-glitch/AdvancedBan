package me.leoko.advancedban.manager

import com.zaxxer.hikari.HikariDataSource
import me.leoko.advancedban.Universal
import me.leoko.advancedban.utils.DynamicDataSource
import me.leoko.advancedban.utils.SQLQuery
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import javax.sql.rowset.CachedRowSet
import javax.sql.rowset.RowSetFactory
import javax.sql.rowset.RowSetProvider

class DatabaseManager {
    private var dataSource: HikariDataSource? = null
    var isUseMySQL: Boolean = false
        private set

    private var factory: RowSetFactory? = null

    fun setup(useMySQLServer: Boolean) {
        isUseMySQL = useMySQLServer
        try {
            dataSource = DynamicDataSource(isUseMySQL).generateDataSource()
        } catch (ex: ClassNotFoundException) {
            Universal.get().log("§cERROR: Failed to configure data source!")
            Universal.get().debug(ex.message ?: "unknown error")
            return
        }

        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT)
        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT_HISTORY)
        migrateReasonColumns()
    }

    fun shutdown() {
        if (!isUseMySQL) {
            try {
                dataSource!!.connection.use { connection ->
                    connection.prepareStatement("SHUTDOWN").use { statement -> statement.execute() }
                }
            } catch (exc: SQLException) {
                Universal.get().log("An unexpected error has occurred turning off the database")
                Universal.get().debugException(exc)
            } catch (exc: NullPointerException) {
                Universal.get().log("An unexpected error has occurred turning off the database")
                Universal.get().debugException(exc)
            }
        }
        dataSource?.close()
    }

    @Throws(SQLException::class)
    private fun createCachedRowSet(): CachedRowSet {
        if (factory == null) factory = RowSetProvider.newFactory()
        return factory!!.createCachedRowSet()
    }

    fun executeStatement(sql: SQLQuery, vararg parameters: Any?) {
        executeStatement(sql, false, *parameters)
    }

    fun executeStatementSucceeded(sql: SQLQuery, vararg parameters: Any?): Boolean =
        executeStatementInternal(sql.toString(), false, reportErrors = true, *parameters).success

    fun executeResultStatement(sql: SQLQuery, vararg parameters: Any?): ResultSet? {
        return executeStatement(sql, true, *parameters)
    }

    private fun executeStatement(sql: SQLQuery, result: Boolean, vararg parameters: Any?): ResultSet? =
        executeStatementInternal(sql.toString(), result, reportErrors = true, *parameters).resultSet

    @Synchronized
    private fun executeStatementInternal(sql: String, result: Boolean, reportErrors: Boolean, vararg parameters: Any?): StatementResult {
        try {
            dataSource!!.connection.use { connection: Connection ->
                connection.prepareStatement(sql).use { statement: PreparedStatement ->
                    for (i in parameters.indices) {
                        statement.setObject(i + 1, parameters[i])
                    }
                    if (result) {
                        val results = createCachedRowSet()
                        results.populate(statement.executeQuery())
                        return StatementResult(results, true)
                    }
                    statement.execute()
                    return StatementResult(null, true)
                }
            }
        } catch (ex: SQLException) {
            if (reportErrors) {
                Universal.get().log(
                    "An unexpected error has occurred executing an Statement in the database\n" +
                        "Please check the plugins/AdvancedBan/logs/latest.log file and report this error in: https://github.com/DevLeoko/AdvancedBan/issues"
                )
                Universal.get().debug("Query: \n$sql")
                Universal.get().debugSqlException(ex)
            }
        } catch (ex: NullPointerException) {
            if (reportErrors) {
                Universal.get().log(
                    "An unexpected error has occurred connecting to the database\n" +
                        "Check if your MySQL data is correct and if your MySQL-Server is online\n" +
                        "Please check the plugins/AdvancedBan/logs/latest.log file and report this error in: https://github.com/DevLeoko/AdvancedBan/issues"
                )
                Universal.get().debugException(ex)
            }
        }
        return StatementResult(null, false)
    }

    private fun migrateReasonColumns() {
        executeStatementInternal(SQLQuery.ALTER_PUNISHMENT_REASON_COLUMN.toString(), false, reportErrors = false)
        executeStatementInternal(SQLQuery.ALTER_PUNISHMENT_HISTORY_REASON_COLUMN.toString(), false, reportErrors = false)
    }

    fun isConnectionValid(): Boolean = dataSource?.isRunning == true

    private data class StatementResult(val resultSet: ResultSet?, val success: Boolean)

    companion object {
        @Volatile
        private var instance: DatabaseManager? = null

        @JvmStatic
        @Synchronized
        fun get(): DatabaseManager {
            if (instance == null) instance = DatabaseManager()
            return instance!!
        }
    }
}
