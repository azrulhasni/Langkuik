/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Comparator;
import org.azrul.langkuik.system.choices.SystemData;

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
    
    //choices
    Choice[] choices() default {}; 
    ActiveChoice activeChoice() default @ActiveChoice;
    SystemData systemChoice() default SystemData.NONE;
    
    
    //rank of field 
    int rank() default -1;
    boolean displayInTable() default false;
    AutoIncrementConfig autoIncrement() default @AutoIncrementConfig();
    FieldUserMap[] userMap() default {@FieldUserMap};
    
    //constraints
    boolean required() default false;

    
    //multi tenancy
    boolean tenantId() default false;
   
}
