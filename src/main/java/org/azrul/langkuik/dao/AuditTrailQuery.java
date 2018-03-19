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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.framework.audit.AuditedEntity;
import org.azrul.langkuik.framework.audit.AuditedField;
import org.azrul.langkuik.security.role.EntityRight;
import org.azrul.langkuik.system.model.audit.AuditMetadata;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import java.util.Date;
import java.util.GregorianCalendar;
import org.hibernate.envers.query.AuditQuery;

/**
 *
 * @author azrulm
 */
public class AuditTrailQuery<T> implements DAOQuery<AuditedEntity, AuditedEntity>, Serializable{

    private Class<T> classOfEntity;
    private T auditedObject;
    private EntityManagerFactory emf;
    private Date startingFrom;

    public AuditTrailQuery(Class<T> classOfEntity, T auditedObject, Date startingFrom) {
        this.classOfEntity = classOfEntity;
        this.auditedObject = auditedObject;
        this.startingFrom = startingFrom;
    }

    @Override
    public Collection<AuditedEntity> doQuery(EntityManagerFactory emf, String orderBy, boolean asc, int startIndex, int offset,String tenantId) {
        this.emf = emf;
        return getAuditData(auditedObject, startIndex, offset, null, startingFrom, tenantId);
    }

    @Override
    public Long count(EntityManagerFactory emf, String tenantId) {
        return getAuditDataSize(emf, auditedObject, startingFrom,tenantId);
    }

    public Long getAuditDataSize(EntityManagerFactory emf, T bean, Date from,String tenantId) {
        AuditReader reader = AuditReaderFactory.get(emf.createEntityManager());
        AuditQuery auditQuery = reader.createQuery()
                .forRevisionsOfEntity(classOfEntity, false, false)
                .add(AuditEntity.id().eq(EntityUtils.getIdentifierValue(bean, emf)));
        if (from != null) {
            GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
            cal.setTime(from);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            auditQuery.add(AuditEntity.revisionNumber().le(reader.getRevisionNumberForDate(cal.getTime())));
        }
        return (Long) auditQuery.addProjection(AuditEntity.id().count()).getSingleResult();
    }

    public List<AuditedEntity> getAuditData(T bean, int startIndex, int offset, List<String> targetFields, Date from, String tenantId) {

        List<AuditedEntity> auditedEntities = new ArrayList<>();
        AuditReader reader = AuditReaderFactory.get(emf.createEntityManager());
        AuditQuery auditQuery = reader.createQuery()
                .forRevisionsOfEntity(classOfEntity, false, false)
                .addOrder(AuditEntity.revisionNumber().desc())
                .add(AuditEntity.id().eq(EntityUtils.getIdentifierValue(bean, emf)));

        if (from != null) {
            GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
            cal.setTime(from);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            auditQuery.add(AuditEntity.revisionNumber().le(reader.getRevisionNumberForDate(cal.getTime())));
        }

        List<Object[]> revisions = auditQuery.setFirstResult(startIndex)
                .setMaxResults(offset/*+1*/)
                .getResultList();

////        T previousAuditedBean = null;
        Collections.reverse(revisions);
        for (Object[] revision : revisions) {
            try {
                T auditedBean = (T) revision[0];
                AuditMetadata auditMetadata = (AuditMetadata) revision[1];
                RevisionType revisionType = (RevisionType) revision[2];

                AuditedEntity auditedEntity = new AuditedEntity();
                auditedEntity.setObject(auditedBean);
                
                if (revisionType == RevisionType.ADD) {
                    auditedEntity.setOperation(EntityRight.CREATE_UPDATE_DELETE);
                } else if (revisionType == RevisionType.DEL) {
                    auditedEntity.setOperation(EntityRight.DELETE);
                } else {
                    auditedEntity.setOperation(EntityRight.UPDATE);
                }
                auditedEntity.setModifiedDate(auditMetadata.getRevisionDate());
                auditedEntity.setRevisionNumber(auditMetadata.getId());
                auditedEntity.setUserId(auditMetadata.getUpdater());
                ArrayList<AuditedField> auditedFields = new ArrayList<>();

                Field[] fields = classOfEntity.getDeclaredFields();
                for (Field field : fields) {
                    if (targetFields == null || targetFields.isEmpty() || targetFields.contains(field.getName())) {
                        if (field.isAnnotationPresent(Audited.class)) {
                            AuditedField auditedField = new AuditedField();
                            field.setAccessible(true);
                            auditedField.setWebField(field.getAnnotation(WebField.class));
                            auditedField.setValue(field.get(auditedBean));
//                            if (previousAuditedBean != null) {
//                                auditedField.setValue(field.get(previousAuditedBean));
//                            }
                            auditedFields.add(auditedField);
                        }
                    }
                }
                auditedEntity.setAuditedFields(auditedFields);
//                previousAuditedBean = auditedBean;
                auditedEntities.add(auditedEntity);
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Collections.reverse(auditedEntities);
        if (auditedEntities.size() > offset) {
            return auditedEntities.subList(0, offset);
        } else {
            return auditedEntities;
        }
    }

    /**
     * @return the startingFrom
     */
    public Date getStartingFrom() {
        return startingFrom;
    }

    /**
     * @param startingFrom the startingFrom to set
     */
    public void setStartingFrom(Date startingFrom) {
        this.startingFrom = startingFrom;
    }

}
