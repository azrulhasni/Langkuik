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

/**
 *
 * @author azrul
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebField {
    String displayName() default ""; 
    boolean visibleInTable() default true;
    boolean visibleInForm() default true;
    boolean isReadOnly() default false;
    //String dateFormat() default "yyyy-MM-dd";
    int order() default 0;
}
