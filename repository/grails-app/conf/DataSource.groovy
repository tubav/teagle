dataSource {
    pooled = true
    driverClassName = "org.hsqldb.jdbcDriver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
    development {//database only in memory
        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop','update'
            url = "jdbc:hsqldb:mem:devDB"
        }
    }
    test {
        dataSource {//database persistent in a file
            dbCreate = "update"
            //url = "jdbc:hsqldb:mem:testDb"
            //url = "jdbc:hsqldb:file:prodDb"
            url = "jdbc:h2:mem:devDb"
        }
    }
//    production {
//        dataSource {
//            dbCreate = "update"
//            url = "jdbc:hsqldb:file:prodDb;shutdown=true"
//            url = "jdbc:hsqldb:file:prodDb"
//        }
//    }
    production {//mysql database
        dataSource {
            pooled = true
            dbCreate = "update"
            url = "jdbc:mysql://localhost:3306/repository"
            driverClassName = "com.mysql.jdbc.Driver"
            dialect = org.hibernate.dialect.MySQLInnoDBDialect
            username = "root"
            password = "*4root#"
            properties {
                maxActive = 50
                maxIdle = 15
                minIdle = 5
                initialSize = 5
                minEvictableIdleTimeMillis = 1800000
                timeBetweenEvictionRunsMillis = 1800000
                maxWait = 10000
            }
        }
    }
}
