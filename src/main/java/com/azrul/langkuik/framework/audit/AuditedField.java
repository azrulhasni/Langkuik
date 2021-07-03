/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.audit;

import com.azrul.langkuik.framework.annotation.WebField;
import java.io.Serializable;

/**
 *
 * @author azrul
 */
public class AuditedField<T> implements Serializable{
    private WebField field;
    private T value;

    /**
     * @return the field
     */
    public WebField getWebField() {
        return field;
    }

    /**
     * @param field the field to set
     */
    public void setWebField(WebField field) {
        this.field = field;
    }

    /**
     * @return the value
     */
    public T getValue() {
        return value;
    }

    /**
     * @param value the oldValue to set
     */
    public void setValue(T value) {
        this.value = value;
    }

    
}
