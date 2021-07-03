/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.loanorigsystem.configuration;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author azrul
 */
@Configuration
@EnableTransactionManagement
public class PersistenceJPAConfig {
    
    @Value("${application.lgDatabaseDriverClassName}")
    private String dbDriverClassName;
      
    @Value("${application.lgDatabaseUsername}")
    private String dbUsername;
        
    @Value("${application.lgDatabasePassword}")
    private String dbPassword;
          
    @Value("${application.lgJdbcURL}")
    private String dbJdbcURL;
    
    @Value("${application.lgHibernateSearchDirectoryProvider}")
    private String hibernateSearchDirectoryProvider;
            
    @Value("${application.lgHibernateSearchIndexBase}")
    private String hibernateSearchIndexBase;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em
                = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan(new String[]{
            "com.azrul.langkuik.loanorigsystem.model",
            "com.azrul.langkuik.framework.standard",
            "com.azrul.langkuik.custom.attachment",
            "com.azrul.langkuik.custom.lookupchoice",
            "com.azrul.langkuik.framework.audit"
        });

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());
        
        return em;
    }

    // ...
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

//        dataSource.setDriverClassName("org.postgresql.Driver");
//        dataSource.setUsername("LoanOrigSystem");
//        dataSource.setPassword("1qazZAQ!");
//        dataSource.setUrl("jdbc:postgresql://localhost/LoanOrigSystem3");
        dataSource.setDriverClassName(dbDriverClassName);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        dataSource.setUrl(dbJdbcURL);

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());

        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    Properties additionalProperties() {
        Properties properties = new Properties();
        //properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
        properties.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        //properties.setProperty("hibernate.hbm2ddl.auto", "create");
        properties.setProperty("hibernate.search.default.directory_provider", hibernateSearchDirectoryProvider);
        properties.setProperty("hibernate.search.default.indexBase", hibernateSearchIndexBase);
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.current_session_context_class", "thread");
        properties.setProperty("hibernate.search.default.exclusive_index_use", "false");
        properties.setProperty("org.hibernate.envers.global_with_modified_flag ", "true");
        properties.setProperty("javax.persistence.validation.mode","none");
        properties.setProperty("org.hibernate.envers.audit_strategy", "org.hibernate.envers.strategy.ValidityAuditStrategy");
        properties.setProperty("org.hibernate.envers.audit_strategy", "org.hibernate.envers.strategy.DefaultAuditStrategy");
        properties.setProperty("hibernate.cache.use_second_level_cache","true");
        properties.setProperty("hibernate.cache.use_query_cache","true");
        properties.setProperty("hibernate.cache.region.factory_class","org.hibernate.cache.ehcache.EhCacheRegionFactory");

        return properties;
    }

}
