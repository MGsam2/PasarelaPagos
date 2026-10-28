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
     A diferencia del Banco B (que usaba un gestor JDBC puro), este gestor (JpaTransactionManager)
     se encarga de las transacciones ACID para las entidades de Hibernate (@Entity).
     Es el motor seguro detrás del PagoService y el portal administrativo.*/
    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            EntityManagerFactory entityManagerFactory) {

        return new JpaTransactionManager(
                entityManagerFactory
        );
    }
}
/*"El MainDataSourceConfig define la conexión troncal de la pasarela. Utiliza la anotación @Primary 
para resolver la ambigüedad en el ecosistema Multi-DataSource. Demuestra una arquitectura híbrida avanzada: 
expone un JdbcTemplate para operaciones DML ultrarrápidas de SQL nativo, y al mismo tiempo configura un 
JpaTransactionManager para administrar de forma segura las entidades del back-office con Hibernate."*/