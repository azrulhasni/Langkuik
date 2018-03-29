/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.webgui;

import com.vaadin.data.util.BeanItemContainer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import org.azrul.langkuik.annotations.DerivedField;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.dao.EntityUtils;

/**
 *
 * @author azrulm
 */
public class WebEntityItemContainer<BEANTYPE> extends BeanItemContainer<BEANTYPE> {

    public WebEntityItemContainer(Collection<BEANTYPE> collection) throws IllegalArgumentException {
        super(collection);

    }

    public WebEntityItemContainer(Class<BEANTYPE> type) throws IllegalArgumentException {
        super(type);
    }

    public WebEntityItemContainer(Class<BEANTYPE> type, Collection<BEANTYPE> collection) throws IllegalArgumentException {
        super(type, collection);
        //collect nested fields
        for (Field field : type.getDeclaredFields()) {
            WebField webField = (WebField) field.getAnnotation(WebField.class);
            if (webField != null) {
                if (webField.allowNested() == true && !field.getType().isAssignableFrom(Collection.class)) {
                    Class fieldType = field.getType();
                    for (Field nestedField : fieldType.getDeclaredFields()) {
                        WebField nestedWebField = (WebField) nestedField.getAnnotation(WebField.class);
                        if (nestedWebField!=null){
                            this.addNestedContainerProperty(field.getName() + "." + nestedField.getName());
                        }
                    }
                }
            }
        }
        
    }

    public void refreshItems() {

        fireItemSetChange();
    }

    public void setBeans(Collection<BEANTYPE> collection) {
        this.removeAllItems();
        this.addAll(collection);
    }

}
