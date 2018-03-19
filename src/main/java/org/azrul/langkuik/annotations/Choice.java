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
public @interface Choice {
    String display() default "";
    int value() default -1;
    String textValue() default "";
    
}
