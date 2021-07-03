/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.loanorigsystem.configuration;

import com.azrul.langkuik.framework.workflow.model.Activity;
import com.azrul.langkuik.framework.workflow.model.Bizprocess;
import com.azrul.langkuik.loanorigsystem.workflow.LoanWorkflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author azrul
 */
@Configuration
public class WorkflowConfig {
    
    @Bean
    public Map<String,Activity> getActivities(Bizprocess bizProcess){
        Map<String,Activity> activities = new HashMap<>();
        for (Activity currentActivity:bizProcess.getWorkflow()){
            activities.put(currentActivity.getId(), currentActivity);
        }
        return activities;
    }
    
    @Bean
    public Bizprocess getBizProcess(){
        try {
            File file = new File(this.getClass().getClassLoader().getResource("workflow.json").getFile());
            ObjectMapper objectMapper = new ObjectMapper();
            Bizprocess bizprocess = objectMapper.readValue(file, Bizprocess.class);
            return bizprocess;
            //System.out.println(bizprocess.getWorkflow().get(0).getId());
        } catch (IOException ex) {
            Logger.getLogger(LoanWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (new Bizprocess()).withName("NONE");
    }
}
