/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.annotations;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.azrul.langkuik.framework.activechoice.ActiveChoiceEnum;
import org.azrul.langkuik.framework.activechoice.EmptyEnum;
/**
 *
 * @author azrulm
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ActiveChoice {
    Class<? extends ActiveChoiceEnum> enumTree() default EmptyEnum.class;
    String hierarchy() default "";
}
