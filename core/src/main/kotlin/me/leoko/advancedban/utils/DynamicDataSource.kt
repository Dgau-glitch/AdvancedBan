package me.leoko.advancedban.utils

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.leoko.advancedban.Universal

class DynamicDataSource @Throws(ClassNotFoundException::class) constructor(preferMySQL: Boolean) {
    private val config = HikariConfig()

    init {
        val mi = Universal.get().methods
        if (preferMySQL) {
            val ip = mi.getString(mi.mySQLFile, "MySQL.IP", "Unknown")
            val dbName = mi.getString(mi.mySQLFile, "MySQL.DB-Name", "Unknown")
            val usrName = mi.getString(mi.mySQLFile, "MySQL.Username", "Unknown")
            val password = mi.getString(mi.mySQLFile, "MySQL.Password", "Unknown")
            val properties = mi.getString(mi.mySQLFile, "MySQL.Properties", "verifyServerCertificate=false&useSSL=false&useUnicode=true&characterEncoding=utf8")
            val port = mi.getInteger(mi.mySQLFile, "MySQL.Port", 3306)

            Class.forName("com.mysql.jdbc.Driver")
            config.jdbcUrl = "jdbc:mysql://$ip:$port/$dbName?$properties"
            config.username = usrName
            config.password = password
        } else {
            val driverClassName = "org.hsqldb.jdbc.JDBCDriver"
            Class.forName(driverClassName)
            config.driverClassName = driverClassName
            config.jdbcUrl = "jdbc:hsqldb:file:${mi.dataFolder.path}/data/storage;hsqldb.lock_file=false"
            config.username = "SA"
            config.password = ""
        }
    }

    fun generateDataSource(): HikariDataSource = HikariDataSource(config)
}
