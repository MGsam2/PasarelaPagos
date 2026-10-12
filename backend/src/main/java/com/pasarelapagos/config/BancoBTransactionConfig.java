package com.pasarelapagos.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class BancoBTransactionConfig {

    @Bean
    public DataSourceTransactionManager bancoBTransactionManager(
            @Qualifier("bancoBDataSource") DataSource bancoBDataSource) {

        return new DataSourceTransactionManager(bancoBDataSource);
    }

    @Bean
    public TransactionTemplate bancoBTransactionTemplate(
            @Qualifier("bancoBTransactionManager")
            DataSourceTransactionManager bancoBTransactionManager) {

        return new TransactionTemplate(bancoBTransactionManager);
    }
}