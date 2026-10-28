package com.pasarelapagos.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/*CONFIGURACIÓN MULTI-DATASOURCE (BANCO B):
 Declara de forma explícita la conexión secundaria hacia la base de datos 'banco_b'*/
@Configuration
public class BancoBDataSourceConfig {

    /*ORIGEN DE DATOS SEGUNDARIO (bancoBDataSource):
     Configura el pool de conexiones JDBC directo hacia la BD de Banco B*/
    @Bean(name = "bancoBDataSource")
    public DataSource bancoBDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/banco_b")
                .username("postgres")
                .password("123")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    /*JDBCTEMPLATE EXCLUSIVO PARA BANCO B:
     Inyecta el DataSource secundario usando @Qualifier para evitar ambigüedades con la BD principal*/
    @Bean(name = "bancoBJdbcTemplate")
    public JdbcTemplate bancoBJdbcTemplate(
            @Qualifier("bancoBDataSource") DataSource dataSource) {

        return new JdbcTemplate(dataSource);
    }
}
/*El BancoBDataSourceConfig implementa el patrón Multi-DataSource en Spring. 
Instancia un DataSource y un JdbcTemplate dedicados para la base de datos aislada banco_b, 
permitiendo que el HostBancoB ejecute débitos e inserciones sin interferir
 con la base de datos principal de la pasarela */