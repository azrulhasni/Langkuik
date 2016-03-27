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

package org.azrul.langkuik.framework.audit;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import org.azrul.langkuik.security.role.EntityOperation;

/**
 *
 * @author azrulm
 */
public class AuditedEntity<T> {
    private Number revisionNumber;
    private T object;
    
    private Date modifiedDate;
    private String userId;
    private EntityOperation operation;
    private List<AuditedField<?>> auditedFields;

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
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /**
     * @param modifiedDate the modifiedDate to set
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the operation
     */
    public EntityOperation getOperation() {
        return operation;
    }

    /**
     * @param operation the operation to set
     */
    public void setOperation(EntityOperation operation) {
        this.operation = operation;
    }

    /**
     * @return the auditFields
     */
    public List<AuditedField<?>> getAuditedFields() {
        return auditedFields;
    }

    /**
     * @param auditFields the auditFields to set
     */
    public void setAuditedFields(List<AuditedField<?>> auditFields) {
        this.auditedFields = auditFields;
    }

    
}
