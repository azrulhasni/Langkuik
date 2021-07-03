/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.views.pojo.configuration;

import com.azrul.langkuik.views.pojo.PojoTableFactory;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author azrul
 */
@Configuration
public class PojoConfig {
    @Bean
   public ValidatorFactory validatorFactory(){
       return Validation.buildDefaultValidatorFactory();
   }
   
   @Bean
   public PojoTableFactory pojoTableFactory(){
       return new PojoTableFactory();
   }
}
