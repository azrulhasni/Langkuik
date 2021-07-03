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
package com.azrul.langkuik.framework.dao;

import com.azrul.langkuik.framework.field.FieldUtils;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import com.fasterxml.uuid.Generators;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import org.hibernate.search.annotations.Analyze;

/**
 *
 * @author azrulm
 */
public class EntityUtils implements Serializable {
    
    public static Field getFieldBySearchFieldName(Class searchClass,String srchFieldName) {
        //Query based on free text + lucene query language
//            try {
        List<String> searchFields = new ArrayList<>();
        for (Field f : FieldUtils.getAllFields(searchClass).values()) {
            if (f.isAnnotationPresent(org.hibernate.search.annotations.Field.class)
                    || f.isAnnotationPresent(org.hibernate.search.annotations.DocumentId.class)) {
                org.hibernate.search.annotations.Field mfield = f.getAnnotation(org.hibernate.search.annotations.Field.class);
                if (Analyze.YES.equals(mfield.analyze())) {
                    String searchFieldName = mfield.name();
                    if (searchFieldName.equals(srchFieldName)) {
                        return f;
                    }
                }
            } else if (f.isAnnotationPresent(org.hibernate.search.annotations.Fields.class)) {
                org.hibernate.search.annotations.Field[] mfields = f.getAnnotation(org.hibernate.search.annotations.Fields.class).value();
                for (org.hibernate.search.annotations.Field mfield : mfields) {
                    if (Analyze.YES.equals(mfield.analyze())) {
                        String searchFieldName = mfield.name();
                        if (searchFieldName.equals(srchFieldName)) {
                            return f;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static List<String> getSearchFields(Class searchClass) {
        //Query based on free text + lucene query language
//            try {
        List<String> searchFields = new ArrayList<>();
        for (Field f : FieldUtils.getAllFields(searchClass).values()) {
            if (f.isAnnotationPresent(org.hibernate.search.annotations.Field.class)
                    || f.isAnnotationPresent(org.hibernate.search.annotations.DocumentId.class)) {
                org.hibernate.search.annotations.Field mfield = f.getAnnotation(org.hibernate.search.annotations.Field.class);
                if (Analyze.YES.equals(mfield.analyze())) {
                    String searchFieldName = mfield.name();
                    if (searchFieldName != null) {
                        if (!searchFieldName.contains("4range")){
                            searchFields.add(searchFieldName);
                        }
                    }
                }
            } else if (f.isAnnotationPresent(org.hibernate.search.annotations.Fields.class)) {
                org.hibernate.search.annotations.Field[] mfields = f.getAnnotation(org.hibernate.search.annotations.Fields.class).value();
                for (org.hibernate.search.annotations.Field mfield : mfields) {
                    if (Analyze.YES.equals(mfield.analyze())) {
                        String searchFieldName = mfield.name();
                        if (searchFieldName != null) {
                           if (!searchFieldName.contains("4range")){
                                searchFields.add(searchFieldName);
                            }
                        }
                    }
                }
            }
        }
        return searchFields;
    }

    public static List<Class<?>> getAllEntities(EntityManagerFactory emf) {
        List<Class<?>> managedClasses = new ArrayList<>();
        for (ManagedType<?> entity : emf.getMetamodel().getManagedTypes()) {
            Class<?> clazz = entity.getJavaType();
            if (clazz == null) {
                continue;
            }
            managedClasses.add(clazz);
        }
        return managedClasses;
    }

    public static boolean isManagedEntity(Class<?> targetClass, EntityManager em) {
        for (ManagedType<?> entity : em.getEntityManagerFactory().getMetamodel().getManagedTypes()) {
            if (entity.getJavaType() == null) {
                continue;
            }
            if (entity.getJavaType().equals(targetClass)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<Class<?>> getManagedEntity(String className, EntityManagerFactory emf) {
        for (ManagedType<?> entity : emf.getMetamodel().getManagedTypes()) {
            if (entity.getJavaType() == null) {
                continue;
            }
            if (entity.getJavaType().getName().equals(className)) {
                return Optional.of(entity.getJavaType());
            }
        }
        return Optional.empty();
    }
    /*public static List<Class<?>> getAllRootEntities(EntityManagerFactory emf) {
        List<Class<?>> rootClasses = new ArrayList<>();
        for (ManagedType<?> entity : emf.getMetamodel().getManagedTypes()) {
            Class<?> clazz = entity.getJavaType();
            if (clazz == null) {
                continue;
            }
            if (clazz.getAnnotation(WebEntity.class) == null) {
                continue;
            }
            if (clazz.getAnnotation(WebEntity.class).isRoot() == true) {
                rootClasses.add(clazz);
            }
        }
        return rootClasses;
    }*/
 /*public static boolean isClassRoot(Class targetClass) {
        if (targetClass.isAnnotationPresent(WebEntity.class)) {
            WebEntity webEntity = (WebEntity) targetClass.getAnnotation(WebEntity.class);
            if (webEntity != null) {
                return webEntity.isRoot();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }*/
//    public static <C> Object getIdentifierValue(C entity, EntityManagerFactory emf) {
//        Object id = emf.getPersistenceUnitUtil().getIdentifier(entity);
//        return id;
//    }

    public static <C> Object getIdentifierValue(C entity, EntityManager em) {
        Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
        return id;
    }

//    public static String getIdentifierFieldName(Class<?> entityClass, EntityManagerFactory emf) {
//        EntityManager em = emf.createEntityManager();
//        try {
//            Metamodel metamodel = em.getMetamodel();
//            EntityType entity = metamodel.entity(entityClass);
//            Set<SingularAttribute> singularAttributes = entity.getSingularAttributes();
//            for (SingularAttribute singularAttribute : singularAttributes) {
//                if (singularAttribute.isId()) {
//                    return singularAttribute.getName();
//                }
//            }
//        } finally {
//            em.close();
//        }
//        return null;
//    }

    public static <T> String getIdentifierFieldName(Class<T> entityClass, EntityManager em) {

        Metamodel metamodel = em.getMetamodel();
        EntityType entity = metamodel.entity(entityClass);
        Set<SingularAttribute> singularAttributes = entity.getSingularAttributes();
        for (SingularAttribute singularAttribute : singularAttributes) {
            if (singularAttribute.isId()) {
                return singularAttribute.getName();
            }
        }

        return null;
    }

    public static <T> Optional<String> getIdentifierSearchFieldName4Sorting(Class<T> entityClass, EntityManager em) {

        Metamodel metamodel = em.getMetamodel();
        EntityType entity = metamodel.entity(entityClass);
        Set<SingularAttribute> singularAttributes = entity.getSingularAttributes();
        for (SingularAttribute singularAttribute : singularAttributes) {
            if (singularAttribute.isId()) {
                try {
                    return classFieldToSearchField4Sorting(entityClass, singularAttribute.getName());
                } catch (SecurityException ex) {
                    Logger.getLogger(EntityUtils.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        return Optional.empty();
    }

    public static <T> Optional<String> classFieldToSearchField4Sorting(Class<T> entityClass, String searchFieldName)  {
        Field field = FieldUtils.getField(entityClass, searchFieldName);
        if (field.isAnnotationPresent(org.hibernate.search.annotations.Field.class)) {
            org.hibernate.search.annotations.Field searchField = field.getAnnotation(org.hibernate.search.annotations.Field.class);
            return Optional.of(searchField.name());
        } else if (field.isAnnotationPresent(org.hibernate.search.annotations.Fields.class)) {
            org.hibernate.search.annotations.Field[] searchFields = field.getAnnotation(org.hibernate.search.annotations.Fields.class).value();
            for (org.hibernate.search.annotations.Field searchField : searchFields) {
                if (Analyze.NO.equals(searchField.analyze())) {
                    return Optional.of(searchField.name());
                }
            }
            return Optional.empty();
        } else {
            return Optional.empty();
        }
    }

   
    
    public static <T> T createNewObject(Class<T> tclass){
      try {
                return tclass.getConstructor(new Class[]{}).newInstance(new Object[]{});
            } catch (InstantiationException 
                    | IllegalAccessException 
                    | IllegalArgumentException 
                    | InvocationTargetException 
                    | NoSuchMethodException 
                    | SecurityException ex) {
                Logger.getLogger(EntityUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        return null;
    }

    public static <T> String getTranxId(T bean) {
        if (bean == null) {
            return null;
        }
        if (LangkuikExt.class.isAssignableFrom(bean.getClass())) {
            LangkuikExt l = (LangkuikExt) bean;
            return l.getTranxId();
        }
        return null;
    }

    public static <T> T setTranxId(T bean) {
        String tranxId = Generators.timeBasedGenerator().generate().toString();
        return setTranxId(bean, tranxId);
    }

    public static <T> T setTranxId(T bean, String tranxId) {
        if (LangkuikExt.class.isAssignableFrom(bean.getClass())) {
            LangkuikExt l = (LangkuikExt) bean;
            if (l.getTranxId() == null) {
                l.setTranxId(tranxId);
                return (T) l;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
   
}
