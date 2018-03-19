/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.azrul.langkuik.security.role.EntityRight;
import org.azrul.langkuik.security.role.FieldRight;

/**
 *
 * @author azrulm
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldUserMap {
    FieldRight right() default FieldRight.INHERITED;
    String role() default "*";
}
