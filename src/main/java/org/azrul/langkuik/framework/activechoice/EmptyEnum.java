/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.framework.activechoice;

/**
 *
 * @author azrulm
 */
public enum EmptyEnum implements ActiveChoiceEnum<EmptyEnum>{
    EMPTY
    ;

    @Override
    public EmptyEnum getParent() {
       return EMPTY;
    }
    
}
