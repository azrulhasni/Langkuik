/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.relationship;

import com.azrul.langkuik.framework.annotation.WebRelation;
import com.azrul.langkuik.custom.VoidCustomComponentRenderer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 *
 * @author azrul
 */
public class RelationContainer {
    private WebRelation webRelation;
    private Field field;
    private Method getter;
    
    

    public RelationContainer(WebRelation webRelation, Field field) {
        this.webRelation = webRelation;
        this.field = field;
    }

    public RelationContainer(WebRelation webRelation, Method getter) {
        this.webRelation = webRelation;
        this.getter = getter;
    }

    /**
     * @return the webRelation
     */
    public WebRelation getWebRelation() {
        return webRelation;
    }

    /**
     * @param webRelation the webRelation to set
     */
    public void setWebRelation(WebRelation webRelation) {
        this.webRelation = webRelation;
    }
    
    public Class getChildType(){
        if (ParameterizedType.class.isAssignableFrom(field.getGenericType().getClass())){
            return (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        }else{
            return field.getType();
        }
    }
    
    public boolean isGenericType(){
        if (ParameterizedType.class.isAssignableFrom(field.getGenericType().getClass())){
            return true;
        }else{
            return false;
        }
    }
    
    public String getRelationshipName(){
            return field.getName();
    }
    
    public boolean isCustomComponent(){
       return webRelation.customComponent()!=VoidCustomComponentRenderer.class;
    }
    
    

    /**
     * @return the field
     */
//    public Field getField() {
//        return field;
//    }

    /**
     * @param field the field to set
     */
    public void setField(Field field) {
        this.field = field;
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
}
