/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.dao;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 *
 * @author azrulm
 */
public final class SearchTerm implements Serializable{
    private String fieldName;
    private Object value;
    private Field field;

    
    public SearchTerm(){
        
    }
    
    public SearchTerm(String fieldName, Field field,Object value){
        setField(field);
        setFieldName(fieldName);
        setValue(value);
    }
    /**
     * @return the fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * @return the field
     */
    public Field getField() {
        return field;
    }

    /**
     * @param field the field to set
     */
    public void setField(Field field) {
        this.field = field;
    }
}
