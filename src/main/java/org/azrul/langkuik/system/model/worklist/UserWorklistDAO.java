/*
 * Copyright 2018 Azrul.
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
package org.azrul.langkuik.system.model.worklist;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import org.azrul.langkuik.annotations.AutoIncrementConfig;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.framework.generator.Generator;
import org.azrul.langkuik.system.model.user.User;
import org.azrul.langkuik.system.model.user.UserDAO;

/**
 *
 * @author Azrul
 */
public class UserWorklistDAO {
    
    private static EntityManagerFactory emf;

    public static void setEMF(EntityManagerFactory e) {
        emf = e;
    }

    private static void assignId(EntityManager em, Object object) throws IllegalArgumentException, InstantiationException, IllegalAccessException, SecurityException, InvocationTargetException, NoSuchMethodException {
        for (Field field : object.getClass().getDeclaredFields()) {

            if (field.getAnnotation(WebField.class) != null) {
                AutoIncrementConfig autoIncConf = field.getAnnotation(WebField.class).autoIncrement();

                Class genClass = autoIncConf.generator();
                if (genClass != null && genClass.isInterface() == false) {
                    Generator generator = (Generator) genClass.getConstructor().newInstance(new Object[]{});

                    generator = em.merge(generator);
                    em.flush();
                    field.setAccessible(true);
                    field.set(object, generator.getValue());

                }
            }
        }
    }

    public static UserWorklist getWorklistByWorklistName(String worklistName) {
        EntityManager em = emf.createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<UserWorklist> criteria = cb.createQuery(UserWorklist.class);
        Root<UserWorklist> worklist = criteria.from(UserWorklist.class);
        criteria.select(worklist).where(cb.equal(worklist.get("worklistName"), worklistName));

        try {
            return em.createQuery(criteria).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

   

    public static void registerWorklist(String worklistName) {
//        User user = new User();
//        user.setUsername(username);
//
//        EntityManager em = emf.createEntityManager();
//        try {
//            assignId(em, user);
//            em.getTransaction().begin();
//            generatePassword(user, plainTextPassword, em);
//            em.merge(user);
//            em.getTransaction().commit();
//
//        } catch (Exception e) {
//            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, e);
//            em.getTransaction().rollback();
//        } finally {
//
//            em.close();
//        }
    }
}