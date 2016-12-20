/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author azrulm
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface WebField {
    String name();
    boolean allowNested() default false;
    String nestedFieldPrefix() default "";
    String group() default "All";
    Choice[] choices() default {}; 
    ActiveChoice activeChoice() default @ActiveChoice;
     
    int rank() default -1;
    boolean displayInTable() default false;
    AutoIncrementConfig autoIncrement() default @AutoIncrementConfig();
    FieldUserMap[] userMap() default {@FieldUserMap};
    
    boolean required() default false;
    boolean tenantId() default false;
   
}
