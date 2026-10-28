package com.pasarelapagos.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/* GESTOR DE TRANSACCIONES (BANCO B):
 * Administra el inicio, commit y rollback de transacciones ACID en la BD 'banco_b'*/
@Configuration
public class BancoBTransactionConfig {

    /*GESTOR DE TRANSACCIONES JDBC:
     Inyecta "bancoBDataSource" y crea el TransactionManager especifico para Banco B*/
    @Bean
    public DataSourceTransactionManager bancoBTransactionManager(
            @Qualifier("bancoBDataSource") DataSource bancoBDataSource) {

        return new DataSourceTransactionManager(bancoBDataSource);
    }

    /*PLANTILLA TRANSACCIONAL PROGRAMÁTICA:
     Permite ejecutar bloques de codigo JDBC dentro de un contexto transaccional 
     sin depender unicamente de la anotacion @Transactional*/
    @Bean
    public TransactionTemplate bancoBTransactionTemplate(
            @Qualifier("bancoBTransactionManager")
            DataSourceTransactionManager bancoBTransactionManager) {

        return new TransactionTemplate(bancoBTransactionManager);
    }
}
/*El BancoBTransactionConfig provee el DataSourceTransactionManager y TransactionTemplate 
dedicados para Banco B. Garantiza que las operaciones ejecutadas con bancoBJdbcTemplate 
mantengan consistencia ACID mediante commits o rollbacks atómicos sobre la base de datos banco_b*/