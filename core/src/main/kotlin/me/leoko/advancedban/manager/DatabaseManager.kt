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
            Universal.get().debug(ex.message)
            return
        }

        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT)
        executeStatement(SQLQuery.CREATE_TABLE_PUNISHMENT_HISTORY)
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

    fun executeResultStatement(sql: SQLQuery, vararg parameters: Any?): ResultSet? {
        return executeStatement(sql, true, *parameters)
    }

    private fun executeStatement(sql: SQLQuery, result: Boolean, vararg parameters: Any?): ResultSet? {
        return executeStatement(sql.toString(), result, *parameters)
    }

    @Synchronized
    private fun executeStatement(sql: String, result: Boolean, vararg parameters: Any?): ResultSet? {
        try {
            dataSource!!.connection.use { connection: Connection ->
                connection.prepareStatement(sql).use { statement: PreparedStatement ->
                    for (i in parameters.indices) {
                        statement.setObject(i + 1, parameters[i])
                    }
                    if (result) {
                        val results = createCachedRowSet()
                        results.populate(statement.executeQuery())
                        return results
                    }
                    statement.execute()
                }
            }
        } catch (ex: SQLException) {
            Universal.get().log(
                "An unexpected error has occurred executing an Statement in the database\n" +
                    "Please check the plugins/AdvancedBan/logs/latest.log file and report this error in: https://github.com/DevLeoko/AdvancedBan/issues"
            )
            Universal.get().debug("Query: \n$sql")
            Universal.get().debugSqlException(ex)
        } catch (ex: NullPointerException) {
            Universal.get().log(
                "An unexpected error has occurred connecting to the database\n" +
                    "Check if your MySQL data is correct and if your MySQL-Server is online\n" +
                    "Please check the plugins/AdvancedBan/logs/latest.log file and report this error in: https://github.com/DevLeoko/AdvancedBan/issues"
            )
            Universal.get().debugException(ex)
        }
        return null
    }

    fun isConnectionValid(): Boolean = dataSource?.isRunning == true

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
