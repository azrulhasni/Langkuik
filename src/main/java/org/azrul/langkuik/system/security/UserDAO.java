/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.system.security;

import com.vaadin.ui.UI;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.azrul.langkuik.Langkuik;
import org.azrul.langkuik.annotations.AutoIncrementConfig;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.framework.generator.Generator;
import org.azrul.langkuik.framework.relationship.DefaultRelationManagerFactory;
import org.azrul.langkuik.framework.relationship.RelationManagerFactory;

public class UserDAO {

    private static EntityManagerFactory emf;

    public static void setEMF(EntityManagerFactory e) {
        emf = e;
    }

    public static User getUserByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> user = criteria.from(User.class);
        criteria.select(user).where(cb.equal(user.get("email"), email));
         try {
            return em.createQuery(criteria).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static User getUserByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> user = criteria.from(User.class);
        criteria.select(user).where(cb.equal(user.get("username"), username));

        try {
            return em.createQuery(criteria).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static User getUserByID(Integer id) {
        EntityManager em = emf.createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> user = criteria.from(User.class);
        criteria.select(user).where(cb.equal(user.get("id"), id));
         try {
            return em.createQuery(criteria).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static void registerUser(String username, String plainTextPassword) {
        User user = new User();
        user.setUsername(username);

        EntityManager em = emf.createEntityManager();
        try {
            assignId(em, user);
            em.getTransaction().begin();
            generatePassword(user, plainTextPassword, em);
            em.merge(user);
            em.getTransaction().commit();

        } catch (Exception e) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, e);
            em.getTransaction().rollback();
        } finally {

            em.close();
        }
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

    public static void registerAdmin(String username, String plainTextPassword) {
        User user = new User();
        user.setUsername(username);
        UserRole role = new UserRole();
        role.setRoleName("ROLE_ADMIN");
        User existingUser = getUserByUsername(username);

        EntityManager em = emf.createEntityManager();
        try {
            if (existingUser == null) {
                em.getTransaction().begin();

                assignId(em, role);
                em.merge(role);
                em.flush();
                Set<UserRole> roles = new HashSet<>();
                roles.add(role);
                user.setRolesCollection(roles);
                generatePassword(user, plainTextPassword, em);

                assignId(em, user);

                em.merge(user);
                em.getTransaction().commit();
            }

        } catch (Exception e) {
            Logger.getLogger(UserRoleDAO.class.getName()).log(Level.SEVERE, null, e);
            em.getTransaction().rollback();
        } finally {

            em.close();
        }
    }

    private static void generatePassword(User user, String plainTextPassword, EntityManager em) throws Exception {
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();

        // Now hash the plain-text password with the random salt and multiple
        // iterations and then Base64-encode the value (requires less space than Hex):
        String hashedPasswordBase64 = new Sha256Hash(plainTextPassword, salt, 1024).toBase64();
        Secret secret = new Secret();
        assignId(em, secret);
        secret.setHashedPassword(hashedPasswordBase64);
        secret.setSalt(salt.toString());
        user.setPassword(secret);
    }

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.azrul.loanorigsystem_LoanOrigSystem_war_1.0-SNAPSHOT_PU");
        RelationManagerFactory rmf = new DefaultRelationManagerFactory();
        System.out.println(System.getProperty("java.io.tmpdir"));
        setEMF(emf);
        registerUser("user1", "password");
    }
}
