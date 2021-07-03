/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.annotation;

import com.azrul.langkuik.framework.workflow.VoidWorkflow;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.azrul.langkuik.framework.workflow.Workflow;

/**
 *
 * @author azrul
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebEntity {
    String name();
    WebEntityType type() default WebEntityType.NOMINAL;
   
    Class<? extends Workflow> workflow() default VoidWorkflow.class;
}

