/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.field;

import com.azrul.langkuik.framework.annotation.WebField;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.SortableField;

/**
 *
 * @author azrul
 */
public class FieldUtils {

    public static Map<String, Field> getAllFields(Class<?> type) {
        Map<String, Field> fields = new HashMap<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            for (Field field : Arrays.asList(c.getDeclaredFields())) {
                fields.put(field.getName(), field);
            }
        }
        return fields;
    }

    public static boolean classContainsField(Class<?> type, String fieldName) {
        Field field = getField(type, fieldName);
        if (field == null) {
            return false;
        } else {
            return true;
        }
    }

    public static Field getField(Class<?> type, String fieldName) {
        try {
            if (type == null) {
                return null;
            }
            Field field = type.getDeclaredField(fieldName);
            return field;
        } catch (NoSuchFieldException ex) {
            try {
                Class<?> sup = type.getSuperclass();
                return sup.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ex1) {
                Logger.getLogger(FieldUtils.class.getName()).log(Level.SEVERE, null, ex1);
            } catch (SecurityException ex1) {
                Logger.getLogger(FieldUtils.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (SecurityException ex) {
            Logger.getLogger(FieldUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public static <T> MultiValuedMap<Integer, FieldContainer> getFieldsByOrder(Class<T> tclass) {
        return getFieldsByOrder(tclass, false);
    }

    public static <T> MultiValuedMap<Integer, FieldContainer> getFieldsByOrder(Class<T> tclass, boolean onlyAuditedFields) {
        //Field store
        MultiValuedMap<Integer, FieldContainer> fieldStore = new ArrayListValuedHashMap<>();
        try {

            BeanInfo beanInfo = Introspector.getBeanInfo(tclass);
            for (java.beans.PropertyDescriptor propertyDescriptor
                    : beanInfo.getPropertyDescriptors()) {
                if ("class".equals(propertyDescriptor.getName())) {
                    continue;
                }
                if ("accessible".equals(propertyDescriptor.getName())) {
                    continue;
                }
                if ("annotatedType".equals(propertyDescriptor.getName())) {
                    continue;
                }
                if ("empty".equals(propertyDescriptor.getName())) {
                    continue;
                }

                if (onlyAuditedFields == true) {
                    if (!FieldUtils.getField(tclass, propertyDescriptor.getName()).isAnnotationPresent(Audited.class)
                            && !propertyDescriptor.getReadMethod().isAnnotationPresent(Audited.class)) {

                        continue;
                    }
                }

                //Get webfield annotation values
                WebField webField = null;
                //System.out.println("Fieldname:"+propertyDescriptor.getReadMethod().getName());
                if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getReadMethod().isAnnotationPresent(WebField.class)) {
                    webField = propertyDescriptor.getReadMethod().getAnnotation(WebField.class);

                } else if (FieldUtils.getField(tclass, propertyDescriptor.getName()).isAnnotationPresent(WebField.class)) {
                    webField = FieldUtils.getField(tclass, propertyDescriptor.getName()).getAnnotation(WebField.class);
                }

                //Get sortable
                boolean isFieldSortable = false;
                if (propertyDescriptor.getReadMethod().isAnnotationPresent(SortableField.class)
                        || FieldUtils.getField(tclass, propertyDescriptor.getName()).isAnnotationPresent(SortableField.class)) {
                    isFieldSortable = true;
                }

                //Add columns
                if (webField != null) {
                    //Only display what is visible
                    if (webField.visibleInTable() == true) {
                        if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getReadMethod().isAnnotationPresent(WebField.class)) {
                            Method getter = propertyDescriptor.getReadMethod();
                            //store field to be sorted
                            FieldContainer fc = new FieldContainer(webField, getter, isFieldSortable);
                            fieldStore.put(webField.order(), fc);

                        } else if (FieldUtils.getField(tclass, propertyDescriptor.getName()).isAnnotationPresent(WebField.class)) {
                            Field field = FieldUtils.getField(tclass, propertyDescriptor.getName());
                            //store field to be sorted
                            FieldContainer fc = new FieldContainer(webField, field, isFieldSortable);
                            fieldStore.put(webField.order(), fc);
                        }
                    }
                }
            }

        } catch (IntrospectionException ex) {
            Logger.getLogger(FieldUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fieldStore;
    }
}
