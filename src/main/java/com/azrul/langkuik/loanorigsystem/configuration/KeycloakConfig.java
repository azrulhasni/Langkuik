/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.loanorigsystem.configuration;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 
 *
 * @author azrul
 */
@Configuration
public class KeycloakConfig {

   @Bean
   public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
       return new KeycloakSpringBootConfigResolver();
   }
}