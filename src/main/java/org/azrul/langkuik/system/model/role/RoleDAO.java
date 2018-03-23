/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.system.model.role;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import org.azrul.langkuik.annotations.AutoIncrementConfig;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.dao.HibernateGenericDAO;
import org.azrul.langkuik.framework.generator.Generator;
import org.hibernate.exception.ConstraintViolationException;

/**
 *
 * @author Azrul
 */
public class RoleDAO {

    private static EntityManagerFactory emf;

    public static void setEMF(EntityManagerFactory e) {
        emf = e;
    }

//  public static List<UserRole> getUserRolesByID(Integer userID) {
//   EntityManager em = emf.createEntityManager();
//    CriteriaBuilder cb = em.getCriteriaBuilder();
//    javax.persistence.criteria.CriteriaQuery<UserRole> criteria = cb.createQuery(UserRole.class);
//    Root<UserRole> role = criteria.from(UserRole.class);
//    criteria.select(role).where(cb.equal(role.get("userID"), userID));
//    return em.createQuery(criteria).getResultList();
//  }
    public static boolean isRoleExist(String roleName) {
        EntityManager em = emf.createEntityManager();
        return isRoleExist(em, roleName);
    }

    public static boolean isRoleExist(EntityManager em, String roleName) {
       Role role = getRole(em, roleName);
       if (role==null) return true; else return false;
    }
    
    public static Role getRole(EntityManager em, String roleName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<Role> criteria = cb.createQuery(Role.class);
        Root<Role> role = criteria.from(Role.class);
        try{
            criteria.select(role).where(cb.equal(role.get("roleName"), roleName));
        return em.createQuery(criteria).getSingleResult();
        }catch(NoResultException e ){
            return null;
        }
    }

    public static List<Role> insert(List<String> roleNames) {
        EntityManager em = emf.createEntityManager();

        List<Role> roles = new ArrayList<>();
        try {
            em.getTransaction().begin();
            for (String roleName : roleNames) {
                Role role = getRole(em,roleName);
                if (role==null){
                    insert(em, roleName);
                }
                roles.add(role);
                
            }
            em.getTransaction().commit();
            return roles;
        } catch (Exception e) {
            Logger.getLogger(RoleDAO.class.getName()).log(Level.SEVERE, null, e);
            em.getTransaction().rollback();
        } finally {

            em.close();
        }
        return null;
    }

    public static Role insert(String roleName) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            Role role = insert(em, roleName);
            em.getTransaction().commit();
            return role;
        } catch (Exception e) {
            Logger.getLogger(RoleDAO.class.getName()).log(Level.SEVERE, null, e);
            em.getTransaction().rollback();
        } finally {

            em.close();
        }
        return null;
    }

    public static Role insert(EntityManager em, String roleName) {
        Role savedObject = null;
        try {
            savedObject = new Role();
            savedObject.setRoleName(roleName);
            assignId(em, savedObject);

            savedObject = em.merge(savedObject);

            return savedObject;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(RoleDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(RoleDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(RoleDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(RoleDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(RoleDAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(RoleDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return savedObject;
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

    public static void registerRole(String roleName) {
        Role role = new Role();
        role.setRoleName(roleName);
       
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            assignId(em, role);
           

            em.merge(role);
            em.getTransaction().commit();

        } catch (Exception e) {
            Logger.getLogger(RoleDAO.class.getName()).log(Level.SEVERE, null, e);
            em.getTransaction().rollback();
        } finally {

            em.close();
        }
    }

}
