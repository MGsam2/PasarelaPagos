package com.pasarelapagos.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class BancoBDataSourceConfig {

    @Bean(name = "bancoBDataSource")
    public DataSource bancoBDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/banco_b")
                .username("postgres")
                .password("123")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    @Bean(name = "bancoBJdbcTemplate")
    public JdbcTemplate bancoBJdbcTemplate(
            @Qualifier("bancoBDataSource") DataSource dataSource) {

        return new JdbcTemplate(dataSource);
    }
}