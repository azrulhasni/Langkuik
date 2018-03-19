/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.system.security;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import org.azrul.langkuik.dao.HibernateGenericDAO;

/**
 *
 * @author Azrul
 */
public class UserRoleDAO {

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
        CriteriaBuilder cb = em.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<UserRole> criteria = cb.createQuery(UserRole.class);
        Root<UserRole> role = criteria.from(UserRole.class);
        criteria.select(role).where(cb.equal(role.get("roleName"), roleName));
        return !em.createQuery(criteria).getResultList().isEmpty();
    }
    
    

    public static UserRole insert(UserRole r) {
        EntityManager em = emf.createEntityManager();

        UserRole savedObject = null;
        try {
            em.getTransaction().begin();
            savedObject = em.merge(r);
            em.getTransaction().commit();
            return savedObject;
        } catch (Exception e) {
            Logger.getLogger(UserRoleDAO.class.getName()).log(Level.SEVERE, null, e);
            em.getTransaction().rollback();
        } finally {

            em.close();
        }
        return null;
    }

}
