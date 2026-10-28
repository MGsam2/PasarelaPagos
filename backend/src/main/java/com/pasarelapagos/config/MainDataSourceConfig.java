package com.pasarelapagos.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

/*CONFIGURACIÓN DE LA BASE DE DATOS PRINCIPAL (MULTI-DATASOURCE):
 Configura la conexión por defecto hacia la base de datos central 'pasarela_pagos'
 y establece los gestores para JPA/Hibernate y JDBC.*/
@Configuration
public class MainDataSourceConfig {

    /*ORIGEN DE DATOS PRINCIPAL:
     Configura el pool de conexiones (HikariCP) hacia la BD central.
     @Primary es crucial: le dice a Spring que si no se especifica una conexión, 
     use esta por defecto, evitando conflictos con la base de datos del Banco B.*/
    @Primary
    @Bean(name = "mainDataSource")
    public DataSource mainDataSource() {

        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/pasarela_pagos")
                .username("postgres")
                .password("123")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    /*JDBCTEMPLATE PRINCIPAL:
     Herramienta inyectada (usualmente en HostBancoA o procesos batch) para ejecutar 
     consultas SQL nativas de altísima velocidad directamente sobre la BD central.*/
    @Bean(name = "mainJdbcTemplate")
    public JdbcTemplate mainJdbcTemplate(
            @Qualifier("mainDataSource")
            DataSource mainDataSource) {

        return new JdbcTemplate(mainDataSource);
    }

    /*GESTOR DE TRANSACCIONES JPA PRINCIPAL:
     Configura el gestor primario para transacciones ACID sobre la base de datos central.
     Soporta tanto operaciones de Hibernate/JPA como operaciones de JDBC sobre mainDataSource.*/
    @Primary
    @Bean(name = {"transactionManager", "mainTransactionManager"})
    public PlatformTransactionManager transactionManager(
            EntityManagerFactory entityManagerFactory) {

        return new JpaTransactionManager(
                entityManagerFactory
        );
    }
}
