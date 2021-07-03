/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik;

import com.azrul.langkuik.custom.attachment.Attachments;
import com.azrul.langkuik.framework.dao.AuditTrailQuery;
import com.azrul.langkuik.framework.dao.DataAccessObject;
import com.azrul.langkuik.framework.dao.FindAnyEntityQuery;
import com.azrul.langkuik.framework.audit.AuditedEntity;
import com.azrul.langkuik.framework.exception.DuplicateDataException;
import com.azrul.langkuik.loanorigsystem.model.Applicant;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.azrul.langkuik.loanorigsystem.model.Application;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import com.azrul.langkuik.framework.standard.Status;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

/**
 *
 * @author azrul
 */

@Component
public class TestBed {
     @Autowired
    private DataAccessObject dao;
     
     @Autowired
   private EntityManager em;
 
    @PostConstruct
    public void init() {
//         try {
//             Attachments attachments = (Attachments) dao.createAndSave(Attachments.class, null, "donald.duck", "1");
//             FindAnyEntityQuery query = new FindAnyEntityQuery(Application.class);
//             query.setQueryString("Id:1");
//             Collection<Application> applications = dao.runQuery(query, null, Boolean.FALSE, 0, 3, null,null);
//             Application app = applications.iterator().next();
//             app.setAttachmentCollection(attachments);
//             dao.save(app);
//         } catch (DuplicateDataException ex) {
//             Logger.getLogger(AttachmentTester.class.getName()).log(Level.SEVERE, null, ex);
//         }
    }
    
    @PostConstruct
    public void toTestPreDraftRelationTracker() {
        
    }
    
    @PostConstruct
    public void toTestAuditTrail() {
//        FindAnyEntityQuery query = new FindAnyEntityQuery(Application.class,"yyyy-MM-dd");
//        query.setQueryString(Optional.of("Id:3"));
//        Collection<Application> apps = dao.runQuery(query);
//        
//        AuditTrailQuery audQuery = new AuditTrailQuery(apps.iterator().next());
//        Long count = dao.countQueryResult(audQuery);
//        Collection <AuditedEntity> results = dao.runQuery(audQuery,
//                Optional.of("accountNumber"),
//                Optional.of(Boolean.TRUE),
//                Optional.of(0),
//                Optional.empty(),
//                Optional.of("loanorigsystem"),
//                Optional.empty());
//        for (AuditedEntity ae:results){
//            System.out.println(ae.getModifiedDate().format(
//                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))+ "::::"
//                    +ae.getUserId() + ":::"
//                    + ae.getOperation() + ":::" 
//                    +((Application)ae.getObject()).getAccountNumber());
//                    
//        }
//        int i=0;
    }
    
     @PostConstruct
    public void toTestSwitchToPreDraft() {
//       FindAnyEntityQuery query = new FindAnyEntityQuery(Applicant.class,"yyyy-MM-dd");
//       query.setQueryString(Optional.of("Id:19"));
//       Collection<Applicant> applicants = dao.runQuery(query);
//       Applicant applicant = applicants.iterator().next();
//       applicant.setStatus(Status.PREDRAFT);
//       dao.save(applicant);

//         CriteriaBuilder cb = em.getCriteriaBuilder();
//        javax.persistence.criteria.CriteriaQuery criteria = cb.createQuery(LangkuikExt.class);
//
//        Root root = criteria.from(LangkuikExt.class);
//        
//        javax.persistence.criteria.CriteriaQuery query = criteria.select(root).where(cb.equal(root.get("tranxId"),"﻿d238fb09-2ff3-11eb-8e4a-3db8f5e36b1d"));
//        Collection col = em.createQuery(query).getResultList();
//        for (Object a:col){
//            LangkuikExt o = (LangkuikExt)a;
//            System.out.println("============== ID:"+o.getId()+" class:"+o.getClass().getSimpleName());
//        }
//        int i=0;
    }
}