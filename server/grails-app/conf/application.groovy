// https://github.com/grails/grails-core/issues/10131
import io.swagger.models.*

grails.gorm.default.constraints = {
	'*'(nullable: true)
}

//grails.gorm.default.mapping = {
//	autowire true
//    id generator: 'snowflake'
//	id generator: 'assigned'
//}
//grails.gorm.failOnError = true

//neo4j {
//	type = "embedded"
//	location = "/var/neo4j"
//}

//environments {
//	development {
//		neo4j {
//			url= "bolt://localhost:7687"
//			username= "neo4j"
//			password= "demo"
//		}
//	}
//	test {
//		neo4j {
//			type = "embedded"
//			location = "${myEnv}/data"
//			embedded {
//				options = [
//						'dbms.active_database': "graph_test.db",
//				]
//			}
//		}
//	}
////	production {
////		dataSource{
////			dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
////			username = "nathandunn"
////			driverClassName = "org.postgresql.Driver"
//////            dialect = org.hibernate.dialect.PostgresPlusDialect
////			dialect = "org.bbop.dmc.ImprovedPostgresDialect"
////			url = "jdbc:postgresql://localhost/dmc-production"
////			properties {
////				// See http://grails.org/doc/latest/guide/conf.html#dataSource for documentation
////				jmxEnabled = true
////				initialSize = 5
////				maxActive = 50
////				minIdle = 5
////				maxIdle = 25
////				maxWait = 10000
////				maxAge = 10 * 60000
////				timeBetweenEvictionRunsMillis = 5000
////				minEvictableIdleTimeMillis = 60000
////				validationQuery = "SELECT 1"
////				validationQueryTimeout = 3
////				validationInterval = 15000
////				testOnBorrow = true
////				testWhileIdle = true
////				testOnReturn = false
////				jdbcInterceptors = "ConnectionState"
////				defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
////			}
////		}
////	}
//}

//environments {
//	development {
//		dataSource{
//			dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
//			username = "ndunn"
//			driverClassName = "org.postgresql.Driver"
//
////            dialect = org.hibernate.dialect.PostgresPlusDialect
//			dialect = "org.bbop.dmc.ImprovedPostgresDialect"
//			url = "jdbc:postgresql://localhost/dmc-dev"
//		}
//	}
//	test {
//		dataSource{
//			dbCreate = "create" // one of 'create', 'create-drop', 'update', 'validate', ''
//			username = "ndunn"
//			driverClassName = "org.postgresql.Driver"
////        dialect = org.hibernate.dialect.PostgresPlusDialect
//			dialect = "org.bbop.dmc.ImprovedPostgresDialect"
//			url = "jdbc:postgresql://localhost/dmc-test"
//		}
//	}
//	production {
//		dataSource{
//			dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
//			username = "nathandunn"
//			driverClassName = "org.postgresql.Driver"
////            dialect = org.hibernate.dialect.PostgresPlusDialect
//			dialect = "org.bbop.dmc.ImprovedPostgresDialect"
//			url = "jdbc:postgresql://localhost/dmc-production"
//			properties {
//				// See http://grails.org/doc/latest/guide/conf.html#dataSource for documentation
//				jmxEnabled = true
//				initialSize = 5
//				maxActive = 50
//				minIdle = 5
//				maxIdle = 25
//				maxWait = 10000
//				maxAge = 10 * 60000
//				timeBetweenEvictionRunsMillis = 5000
//				minEvictableIdleTimeMillis = 60000
//				validationQuery = "SELECT 1"
//				validationQueryTimeout = 3
//				validationInterval = 15000
//				testOnBorrow = true
//				testWhileIdle = true
//				testOnReturn = false
//				jdbcInterceptors = "ConnectionState"
//				defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
//			}
//		}
//	}
//}

swagger {
   info {
       description = "Move your app forward with the Swagger API Documentation"
       version = "ttn-swagger-1.0.0"
       title = "Swagger API"
       termsOfServices = "http://swagger.io/"
       contact {
           name = "Contact Us"
           url = "http://swagger.io"
           email = "contact@gmail.com"
       }
       license {
           name = "licence under http://www.tothenew.com/"
           url = "http://www.tothenew.com/"
       }
   }
   schemes = [Scheme.HTTP,Scheme.HTTPS]
   consumes = ["application/json"]
}

// Added by the Spring Security OAuth2 Google Plugin:
grails.plugin.springsecurity.oauth2.domainClass = 'com.insilico.dmc.OAuthID'


