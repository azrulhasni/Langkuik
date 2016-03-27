/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.relationship;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import org.azrul.langkuik.framework.relationship.RelationManager;
import org.azrul.langkuik.framework.relationship.RelationManagerFactory;

/**
 *
 * @author azrulm
 */
public class DefaultRelationManagerFactory implements RelationManagerFactory, Serializable {

    public DefaultRelationManagerFactory() {

    }

    @SuppressWarnings("empty-statement")
    public <P, C> RelationManager<P, C> create(Class<P> parentClass,
            Class<C> currentClass) {
        RelationManager<P, C> rm = new RelationManager<P, C>() {

            @Override
            public void link(P parentObject, C currentObject,
                    String parentToCurrentField, EntityManager em) {

                try {
                    if (parentObject == null) {
                        return;
                    }
                    if (currentObject == null) {
                        return;
                    }
                    if (Collection.class.isAssignableFrom(parentObject.getClass().getDeclaredField(parentToCurrentField).getType())) {
                        Field field = parentObject.getClass().getDeclaredField(parentToCurrentField);
                        field.setAccessible(true);
                        Collection collection = (Collection) field.get(parentObject);
                        collection.add(currentObject);
                    } else {
                        Field field = parentObject.getClass().getDeclaredField(parentToCurrentField);
                        field.setAccessible(true);
                        field.set(parentObject, currentObject);
                    }

                } catch (NoSuchFieldException ex) {
                    Logger.getLogger(DefaultRelationManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(DefaultRelationManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(DefaultRelationManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(DefaultRelationManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            @Override
            public void unlink(P parentObject, C currentObjects,
                    String parentToCurrentField, EntityManager em) {
                try {
                    if (parentObject == null) {
                        return;
                    }
                    if (currentObjects == null) {
                        return;
                    }
                    if (Collection.class.isAssignableFrom(parentObject.getClass().getDeclaredField(parentToCurrentField).getType())) {
                        Field field = parentObject.getClass().getDeclaredField(parentToCurrentField);
                        field.setAccessible(true);
                        Collection collection = (Collection) field.get(parentObject);
                        collection.remove(currentObjects);
                    } else {
                        Field field = parentObject.getClass().getDeclaredField(parentToCurrentField);
                        field.setAccessible(true);
                        field.set(parentObject, null);
                    }

                } catch (NoSuchFieldException ex) {
                    Logger.getLogger(DefaultRelationManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(DefaultRelationManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(DefaultRelationManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(DefaultRelationManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

           

        };
        return rm;
    }
}

/* @Override
 public <P, C> RelationManager<P, C> create(Class<P> parentClass, Class<C> currentClass) {
 if (Application.class.equals(parentClass) && Applicants.class.equals(currentClass)) {
 return (RelationManager<P, C>) new RelationManager<Application, Applicants>() {
 @Override
 public void link(Application application, Applicants applicants, String parentToCurrentField, EntityManager em) {
 if (application == null) {
 return;
 }
 if (applicants == null) {
 return;
 }

 applicants.setIdsApplication(application);
 if (application.getApplicantsCollection() == null) {
 application.setApplicantsCollection(new ArrayList<Applicants>());
 }
 application.getApplicantsCollection().add(applicants);
 }

 @Override
 public void unlink(Application application, Applicants applicants,String parentToCurrentField, EntityManager em) {
 if (application == null) {
 return;
 }
 if (applicants == null) {
 return;
 }

 if (application.getApplicantsCollection() != null) {
 application.getApplicantsCollection().remove(applicants);
 }
 applicants.setIdsApplication(null);
 }
 };
 } else if (Application.class.equals(parentClass) && ProductDetails.class.equals(currentClass)) {
 return (RelationManager<P, C>) new RelationManager<Application, ProductDetails>() {
 @Override
 public void link(Application application, ProductDetails productDetails, String parentToCurrentField, EntityManager em) {
 if (application == null) {
 return;
 }
 if (productDetails == null) {
 return;
 }

 application.setIdsProductDetails(productDetails);
 if (productDetails.getApplicationCollection() == null) {
 productDetails.setApplicationCollection(new ArrayList<Application>());
 }
 productDetails.getApplicationCollection().add(application);
 }

 @Override
 public void unlink(Application application, ProductDetails productDetails,String parentToCurrentField, EntityManager em) {
 if (application == null) {
 return;
 }
 if (productDetails == null) {
 return;
 }

 if (productDetails.getApplicationCollection() != null) {
 productDetails.getApplicationCollection().remove(application);
 }
 application.setIdsProductDetails(null);
 }
 };
 } else if (ProductDetails.class.equals(parentClass) && DecisionResults.class.equals(currentClass)) {
 return (RelationManager<P, C>) new RelationManager<ProductDetails, DecisionResults>() {
 @Override
 public void link(ProductDetails productDetails, DecisionResults decisionResults,String parentToCurrentField, EntityManager em) {
 if (decisionResults == null) {
 return;
 }
 if (productDetails == null) {
 return;
 }
 productDetails.setIdsDecisionResults(decisionResults);
 if (decisionResults.getProductDetailsCollection() == null) {
 decisionResults.setProductDetailsCollection(new ArrayList<ProductDetails>());
 }
 decisionResults.getProductDetailsCollection().add(productDetails);
 }

 @Override
 public void unlink(ProductDetails productDetails, DecisionResults decisionResults, String parentToCurrentField,EntityManager em) {
 if (decisionResults == null) {
 return;
 }
 if (productDetails == null) {
 return;
 }
 if (decisionResults.getProductDetailsCollection() != null) {
 decisionResults.getProductDetailsCollection().remove(productDetails);
 }
 productDetails.setIdsDecisionResults(null);
 }
 };
 } else if (Application.class.equals(parentClass) && Attachment.class.equals(currentClass)) {
 return (RelationManager<P, C>) new RelationManager<Application, Attachment>() {
 @Override
 public void link(Application application, Attachment attachment, String parentToCurrentField, EntityManager em) {
 if (application == null) {
 return;
 }
 if (attachment == null) {
 return;
 }
 attachment.setIdsApplication(application);
 if (application.getAttachmentCollection() == null) {
 application.setAttachmentCollection(new ArrayList<Attachment>());
 }
 application.getAttachmentCollection().add(attachment);
 }

 @Override
 public void unlink(Application application, Attachment attachment, String parentToCurrentField,EntityManager em) {
 if (application == null) {
 return;
 }
 if (attachment == null) {
 return;
 }
 if (application.getAttachmentCollection() != null) {
 application.getAttachmentCollection().remove(attachment);
 }
 attachment.setIdsApplication(null);
 }
 };
 } else if (Application.class.equals(parentClass) && Collateral.class.equals(currentClass)) {
 return (RelationManager<P, C>) new RelationManager<Application, Collateral>() {
 @Override

 public void link(Application application, Collateral collateral, String parentToCurrentField,EntityManager em) {

 if (application == null) {
 return;
 }
 if (collateral == null) {
 return;
 }
                   
 //collateral = em.merge(collateral);
                    
                    
 if (collateral.getApplicationCollection() == null) {
 collateral.setApplicationCollection(new HashSet<Application>());
 }
 collateral.getApplicationCollection().add(application);
                    
 //application = em.merge(application);
                    
 if (application.getCollateralCollection() == null) {
 application.setCollateralCollection(new HashSet<Collateral>());
 }
 application.getCollateralCollection().add(collateral);
                   

 }

 @Override
 public void unlink(Application application, Collateral collateral, String parentToCurrentField,EntityManager em) {
 if (application == null) {
 return;
 }
 if (collateral == null) {
 return;
 }
 if (application.getCollateralCollection() != null) {
 application.getCollateralCollection().remove(collateral);
 }
 if (collateral.getApplicationCollection() != null) {
 collateral.getApplicationCollection().remove(application);
 }
 }
 };
 }
 return null;
 }*/
