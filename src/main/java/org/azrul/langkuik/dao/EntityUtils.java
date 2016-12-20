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
package org.azrul.langkuik.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.annotations.WebField;

/**
 *
 * @author azrulm
 */
public class EntityUtils implements Serializable {

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

    public static boolean isManagedEntity(Class<?> targetClass, EntityManagerFactory emf) {
        for (ManagedType<?> entity : emf.getMetamodel().getManagedTypes()) {
            if (entity.getJavaType() == null) {
                continue;
            }
            if (entity.getJavaType().equals(targetClass)) {
                return true;
            }
        }
        return false;
    }

    public static List<Class<?>> getAllRootEntities(EntityManagerFactory emf) {
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
    }

    public static boolean isClassRoot(Class targetClass) {
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
    }

    public static <C> Object getIdentifierValue(C entity, EntityManagerFactory emf) {
        Object id = emf.getPersistenceUnitUtil().getIdentifier(entity);
        return id;
    }

    public static String getIdentifierFieldName(Class<?> entityClass, EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            Metamodel metamodel = em.getMetamodel();
            EntityType entity = metamodel.entity(entityClass);
            Set<SingularAttribute> singularAttributes = entity.getSingularAttributes();
            for (SingularAttribute singularAttribute : singularAttributes) {
                if (singularAttribute.isId()) {
                    return singularAttribute.getName();
                }
            }
        } finally {
            em.close();
        }
        return null;
    }

    public static Field getTenantFieldName(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.getAnnotation(WebField.class) != null) {
                if (field.getAnnotation(WebField.class).tenantId() == true) {
                    return field;
                }
            }
        }

        return null;
    }

}
