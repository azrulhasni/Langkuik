/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.custom.lookupchoice;

import com.azrul.langkuik.framework.annotation.CustomField;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 *
 * @author azrul
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@CustomField(renderer=LookupRenderer.class)
public @interface Lookup  {
    Class entity();
    String field();
    String filterBy() default "";
    String matching() default "";
}
