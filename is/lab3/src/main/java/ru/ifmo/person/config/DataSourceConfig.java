package ru.ifmo.person.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

/**
 * Конфигурация Connection Pool с Apache Commons DBCP2 (ЛР3).
 * 
 * Параметры конфигурации пула соединений:
 * - initialSize: 5 - начальное количество соединений при запуске
 * - maxTotal: 20 - максимальное количество активных соединений
 * - maxIdle: 10 - максимальное количество простаивающих соединений
 * - minIdle: 5 - минимальное количество простаивающих соединений
 * - maxWaitMillis: 10000 - максимальное время ожидания соединения (10 сек)
 * - testOnBorrow: true - проверка соединения перед использованием
 * - validationQuery: "SELECT 1" - SQL запрос для проверки соединения
 * - testWhileIdle: true - проверка простаивающих соединений
 * - timeBetweenEvictionRunsMillis: 30000 - интервал проверки (30 сек)
 * - minEvictableIdleTimeMillis: 60000 - минимальное время простоя перед удалением (60 сек)
 * - removeAbandonedOnBorrow: true - удаление забытых соединений
 * - removeAbandonedTimeout: 300 - таймаут для забытых соединений (5 мин)
 */
@ApplicationScoped
public class DataSourceConfig {

    @Produces
    @ApplicationScoped
    public DataSource createDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        
        // Параметры подключения к БД
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = "jdbc:postgresql://pg:5432/studs";
        }
        if (dbUser == null || dbUser.isEmpty()) {
            dbUser = "s409858";
        }
        if (dbPassword == null || dbPassword.isEmpty()) {
            dbPassword = "dmfMuVPpI4Rt35fX";
        }
        
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);
        
        // Параметры пула соединений
        dataSource.setInitialSize(5);
        dataSource.setMaxTotal(20);
        dataSource.setMaxIdle(10);
        dataSource.setMinIdle(5);
        dataSource.setMaxWaitMillis(10000);
        
        // Проверка соединений
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setValidationQueryTimeout(3);
        
        // Удаление неиспользуемых соединений
        dataSource.setTestWhileIdle(true);
        dataSource.setTimeBetweenEvictionRunsMillis(30000);
        dataSource.setMinEvictableIdleTimeMillis(60000);
        
        // Удаление забытых соединений
        dataSource.setRemoveAbandonedOnBorrow(true);
        dataSource.setRemoveAbandonedTimeout(300);
        dataSource.setLogAbandoned(true);
        
        return dataSource;
    }
}
