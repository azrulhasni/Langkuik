/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.framework.webgui;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.azrul.langkuik.annotations.FieldUserMap;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.annotations.DerivedField;

/**
 *
 * @author azrulm
 */
public class FieldContainer implements DataElementContainer{
    private WebField webField;
    private Field pojoField;


    public FieldContainer(WebField myField,Field pojoField){
        this.webField = myField;
        this.pojoField = pojoField;
    }
    
  
    
   
    /**
     * @return the myField
     */
    public WebField getWebField() {
        return webField;
    }

    /**
     * @param myField the myField to set
     */
    public void setWebField(WebField myField) {
        this.webField = myField;
    }

    /**
     * @return the field
     */
    public Field getPojoField() {
        return pojoField;
    }

    /**
     * @param field the field to set
     */
    public void setPojoField(Field field) {
        this.pojoField = field;
    }

      public FieldUserMap[] getFieldUserMaps(){
         return webField.userMap();
     }

    
}
