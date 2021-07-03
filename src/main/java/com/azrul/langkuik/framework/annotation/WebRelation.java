/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.azrul.langkuik.custom.CustomComponentRenderer;
import com.azrul.langkuik.custom.VoidCustomComponentRenderer;

/**
 *
 * @author azrul
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebRelation {
    String name();
    int order() default 0;
    Class<? extends CustomComponentRenderer> customComponent() default VoidCustomComponentRenderer.class;
    
}
