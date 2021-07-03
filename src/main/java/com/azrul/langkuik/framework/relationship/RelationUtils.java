/*
 * Copyright 2014 azrulm.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.azrul.langkuik.framework.relationship;

import com.azrul.langkuik.framework.annotation.WebRelation;
import com.azrul.langkuik.framework.dao.Dual;
import static com.azrul.langkuik.framework.dao.EntityUtils.getAllEntities;
import com.azrul.langkuik.framework.field.FieldUtils;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

/**
 *
 * @author azrulm
 */
public class RelationUtils {

    public static RelationType getRelationType(Field field) {
        if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
            return RelationType.X_To_ONE;
        }
        if (field.isAnnotationPresent(ManyToMany.class) || field.isAnnotationPresent(OneToMany.class)) {
            return RelationType.X_TO_MANY;
        }
        return RelationType.NA;
    }

    public static RelationType getRelationType(Class parentClass, String relationName) {
        Field field = FieldUtils.getField(parentClass, relationName);
        if (field != null) {
            return getRelationType(field);
        }
        return RelationType.NA;
    }

    public static Class getRelationClass(Class parentClass, String relationName) {
        Field field = FieldUtils.getField(parentClass, relationName);
        if (field == null) {
            return null;
        }
        if (Collection.class.isAssignableFrom(field.getType())) {
            return (Class) ((ParameterizedType) FieldUtils.getField(parentClass, relationName).getGenericType()).getActualTypeArguments()[0];
        } else {
            return FieldUtils.getField(parentClass, relationName).getType();
        }
    }

    public static List<Field> getParentChildFields(Class parentClass, Class currentClass) {
        List<Field> fields = new ArrayList<>();
        if (parentClass == null) {
            return fields;
        }
        if (currentClass == null) {
            return fields;
        }
        try {
            for (Field parentField : FieldUtils.getAllFields(parentClass).values()) {
                parentField.setAccessible(true);

                if (Collection.class.isAssignableFrom(parentField.getType())) {
                    Class fieldClass = (Class) ((ParameterizedType) parentField.getGenericType()).getActualTypeArguments()[0];
                    if (fieldClass.equals(currentClass)) {
                        fields.add(parentField);
                    }
                } else {
                    if (parentField.getType().equals(currentClass)) {
                        fields.add(parentField);
                    }
                }
            }
            return fields;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(DefaultRelationManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fields;

    }

    public static <T> MultiValuedMap<Integer, RelationContainer> getRelationsByOrder(Class<T> tclass) throws IntrospectionException, NoSuchFieldException {
        //Field store
        MultiValuedMap<Integer, RelationContainer> fieldStore = new ArrayListValuedHashMap<>();
        //grid.
        BeanInfo beanInfo = Introspector.getBeanInfo(tclass);
        for (java.beans.PropertyDescriptor propertyDescriptor
                : beanInfo.getPropertyDescriptors()) {
            if (escapeNonFields(propertyDescriptor)) {
                continue;
            }

            //Get webfield annotation values
            WebRelation webRelation = null;
            // tclass,propertyDescriptor.getName()).setAccessible(true);
            if (propertyDescriptor.getReadMethod().isAnnotationPresent(WebRelation.class)) {
                webRelation = propertyDescriptor.getReadMethod().getAnnotation(WebRelation.class);
            } else if (FieldUtils.getField(tclass, propertyDescriptor.getName()).isAnnotationPresent(WebRelation.class)) {
                webRelation = FieldUtils.getField(tclass, propertyDescriptor.getName()).getAnnotation(WebRelation.class);
            }

            //Add columns
            if (webRelation != null) {
                if (propertyDescriptor.getReadMethod().isAnnotationPresent(WebRelation.class)) {
                    Method getter = propertyDescriptor.getReadMethod();
                    //store field to be sorted
                    RelationContainer rc = new RelationContainer(webRelation, getter);
                    fieldStore.put(webRelation.order(), rc);
                } else if (FieldUtils.getField(tclass, propertyDescriptor.getName()).isAnnotationPresent(WebRelation.class)) {
                    Field field = FieldUtils.getField(tclass, propertyDescriptor.getName());
                    //store field to be sorted
                    RelationContainer rc = new RelationContainer(webRelation, field);
                    fieldStore.put(webRelation.order(), rc);
                }

            }
        }
        return fieldStore;
    }

    private static boolean escapeNonFields(PropertyDescriptor propertyDescriptor) {
        if ("class".equals(propertyDescriptor.getName())) {
            return true;
        }
        if ("accessible".equals(propertyDescriptor.getName())) {
            return true;
        }
        if ("annotatedType".equals(propertyDescriptor.getName())) {
            return true;
        }
        if ("empty".equals(propertyDescriptor.getName())) {
            return true;
        }
        return false;
    }

    public static <T> Collection<Dual<Class<?>,Field>> getAllDependingClass(Class<T> child, EntityManagerFactory emf) {
        Collection<Dual<Class<?>, Field>> results = new ArrayList<>();
        List<Class<?>> allEntities = getAllEntities(emf);
        for (Class<?> pclass : allEntities) {
            for (Field pfield : pclass.getDeclaredFields()) {
                if (Collection.class.isAssignableFrom(pfield.getType())) {
                    Class<?> pt = (Class) ((ParameterizedType) pfield.getGenericType()).getActualTypeArguments()[0];
                    if (pt.equals(child)) {
                        Dual<Class<?>, Field> dual = new Dual(pclass, pfield);
                        results.add(dual);
                    }
                } else {
                    if (pfield.getType().equals(child)) {
                        Dual<Class<?>, Field> dual = new Dual(pclass, pfield);
                        results.add(dual);
                    }
                }
            }
        }

        return results;
    }
}
