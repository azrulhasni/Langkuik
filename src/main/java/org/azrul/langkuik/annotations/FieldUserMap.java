/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.azrul.langkuik.security.role.EntityOperation;
import org.azrul.langkuik.security.role.FieldOperation;

/**
 *
 * @author azrulm
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldUserMap {
    FieldOperation right() default FieldOperation.INHERITED;
    String role() default "*";
}
