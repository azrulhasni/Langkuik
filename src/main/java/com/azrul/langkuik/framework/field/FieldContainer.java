/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.field;

import com.azrul.langkuik.framework.annotation.CustomField;
import com.azrul.langkuik.framework.annotation.WebField;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Id;

/**
 *
 * @author azrul
 */
public class FieldContainer {

    public FieldContainer(WebField webField, Field field, boolean sortable) {
        this.webField = webField;
        this.getter = null;
        this.field = field;
        this.sortable = sortable;
    }

    public FieldContainer(WebField webField, Method getter, boolean sortable) {
        this.webField = webField;
        this.getter = getter;
        this.field = null;
        this.sortable = sortable;
    }

    private WebField webField;
    private Method getter;
    private Field field;
    private boolean sortable;

    public Class<?> getReturnType() {
        if (field == null && getter != null) {
            return getter.getReturnType();
        } else if (field != null && getter == null) {
            return field.getType();
        } else {
            return null;
        }
    }
    
     public boolean isId() {
        if (field == null && getter != null) {
            return getter.isAnnotationPresent(Id.class);
        } else if (field != null && getter == null) {
            return field.isAnnotationPresent(Id.class);
        } else {
            return false;
        }
    }

    public Object getValue(Object t) {
        try {
            if (field == null && getter != null) {
                getter.setAccessible(true);
                return getter.invoke(t, new Object[]{});
            } else if (field != null && getter == null) {
                field.setAccessible(true);
                return field.get(t);
            } else {
                return null;
            }
        } catch (IllegalAccessException ex) {
            Logger.getLogger(FieldContainer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(FieldContainer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(FieldContainer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * @return the webField
     */
    public WebField getWebField() {
        return webField;
    }

    /**
     * @param webField the webField to set
     */
    public void setWebField(WebField webField) {
        this.webField = webField;
    }

    /**
     * @return the getter
     */
    public Method getGetter() {
        return getter;
    }

    /**
     * @param getter the getter to set
     */
    public void setGetter(Method getter) {
        this.getter = getter;
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

    /**
     * @return the sortable
     */
    public boolean isSortable() {
        return sortable;
    }

    /**
     * @param sortable the sortable to set
     */
    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }
    
     public Optional<Class> getCustomComponent(){
       for (Annotation a:field.getAnnotations()){
           Class ca = a.annotationType();
           if (ca.isAnnotationPresent(CustomField.class)){
               return Optional.of(ca);
           }
       }
       return Optional.empty();
    }
}
