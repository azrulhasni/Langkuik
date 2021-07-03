/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.audit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.hibernate.search.annotations.SortableField;

/**
 *
 * @author azrul
 */
public class AuditedEntity<T> {

    private Number revisionNumber;
    private T object;

    private LocalDateTime modifiedDate;
    private String username;
    private String operation;
    //private Map<String,AuditedField<?>> auditedFields;

    /**
     * @return the revisionNumber
     */
    public Number getRevisionNumber() {
        return revisionNumber;
    }

    /**
     * @param revisionNumber the revisionNumber to set
     */
    public void setRevisionNumber(Number revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    /**
     * @return the object
     */
    public T getObject() {
        return object;
    }

    /**
     * @param object the object to set
     */
    public void setObject(T object) {
        this.object = object;
    }

    /**
     * @return the modifiedDate
     */
    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    /**
     * @param modifiedDate the modifiedDate to set
     */
    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    

    /**
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @param operation the operation to set
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

//    public void setOperation(RevisionType revisionType) {
//        if (revisionType == RevisionType.ADD) {
//            operation = EntityRight.CREATE_UPDATE;
//        } else if (revisionType == RevisionType.DEL) {
//            operation = EntityRight.DELETE;
//        } else {
//            operation = EntityRight.UPDATE;
//        }
//    }

    /**
     * @return the auditFields
     */
//    public Map<String,AuditedField<?>> getAuditedFields() {
//        return auditedFields;
//    }
//
//    /**
//     * @param auditFields the auditFields to set
//     */
//    public void setAuditedFields(Map<String,AuditedField<?>> auditFields) {
//        this.auditedFields = auditFields;
//    }
//    
//    public AuditedField getAuditedField(String fieldName) {
//        return auditedFields.get(fieldName);
//    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

}

