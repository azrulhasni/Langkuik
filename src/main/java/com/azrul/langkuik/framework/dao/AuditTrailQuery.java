/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.dao;

/**
 *
 * @author azrul
 */
import com.azrul.langkuik.framework.audit.AuditMetadata;
import com.azrul.langkuik.framework.audit.AuditedEntity;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import java.util.GregorianCalendar;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.hibernate.envers.query.AuditQuery;
import com.azrul.langkuik.framework.audit.AuditedField;
import com.azrul.langkuik.framework.standard.Status;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.hibernate.envers.query.order.AuditOrder;
import com.azrul.langkuik.framework.standard.LangkuikExt;

/**
 *
 * @author azrulm
 */
public class AuditTrailQuery<T> implements DAOQuery<AuditedEntity, AuditedEntity>, Serializable {

    private Class<T> classOfEntity;
    private T auditedObject;
    private Optional<LocalDate> startingFrom;

    public AuditTrailQuery(T auditedObject, LocalDate startingFrom) {
        this.classOfEntity = (Class<T>) auditedObject.getClass();
        this.auditedObject = auditedObject;
        this.startingFrom = Optional.ofNullable(startingFrom);
    }

    public AuditTrailQuery(T auditedObject) {
        this.classOfEntity = (Class<T>) auditedObject.getClass();
        this.auditedObject = auditedObject;
        this.startingFrom = Optional.empty();
    }

    @Override
    public Collection<AuditedEntity> doQuery(EntityManager em, Optional<String> orderBy, Optional<Boolean> asc, Optional<Integer> startIndex, Optional<Integer> pageSize, Optional<String> tenantId, Optional<String> worklist) {
        return getAuditData(em, auditedObject, startIndex.orElse(0), pageSize, orderBy, asc, startingFrom, tenantId);
    }

    @Override
    public Long count(EntityManager em, Optional<String> tenantId, Optional<String> worklist) {
        return getAuditDataSize(em, auditedObject, startingFrom, tenantId);
    }

    private Long getAuditDataSize(EntityManager em, T bean, Optional<LocalDate> from, Optional<String> tenantId) {
        AuditReader reader = AuditReaderFactory.get(em);
        AuditQuery auditQuery = reader.createQuery()
                .forRevisionsOfEntity(classOfEntity, false, false)
                .add(AuditEntity.id().eq(EntityUtils.getIdentifierValue(bean, em)));
        from.ifPresent(f -> {
            GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.from(f.atStartOfDay(ZoneId.systemDefault()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
            auditQuery.add(AuditEntity.revisionNumber().le(reader.getRevisionNumberForDate(cal.getTime())));
        });

//        if (LangkuikExt.class.isAssignableFrom(bean.getClass())) {
//            auditQuery.add(
//                    AuditEntity.not(
//                            AuditEntity.or(
//                                    AuditEntity.property("status").eq(Status.DELETED),
//                                    AuditEntity.property("status").eq(Status.PREDRAFT)
//                            )
//                    )
//            );
//        }
        return (Long) auditQuery.addProjection(AuditEntity.id().count()).getSingleResult();
    }

    private List<AuditedEntity> getAuditData(EntityManager em, T bean, Integer startIndex, Optional<Integer> pageSize, Optional<String> orderBy, Optional<Boolean> asc, Optional<LocalDate> from, Optional<String> tenantId) {

        List<AuditedEntity> auditedEntities = new ArrayList<>();
        AuditReader reader = AuditReaderFactory.get(em);
        AuditOrder auditOrder = orderBy.map(o -> {
            if ("operation".equals(o)) {
                return asc.map(a -> {
                    if (a == true) {
                        return AuditEntity.revisionType().asc();
                    } else {
                        return AuditEntity.revisionType().desc();
                    }
                }).orElseGet(() -> {
                    return AuditEntity.revisionType().desc();
                });
            } else if ("revisionNumber".equals(o)) {
                return asc.map(a -> {
                    if (a == true) {
                        return AuditEntity.revisionNumber().asc();
                    } else {
                        return AuditEntity.revisionNumber().desc();
                    }
                }).orElseGet(() -> {
                    return AuditEntity.revisionNumber().desc();
                });
            } else {
                return asc.map(a -> {
                    if (a == true) {
                        return AuditEntity.property(o).asc();
                    } else {
                        return AuditEntity.property(o).desc();
                    }
                }).orElseGet(() -> {
                    return AuditEntity.property(o).desc();
                });
            }
        }).orElseGet(() -> {
            return AuditEntity.revisionNumber().desc();
        });

        AuditQuery auditQuery = reader.createQuery()
                .forRevisionsOfEntity(classOfEntity, false, false)
                .addOrder(auditOrder)
                .add(AuditEntity.id().eq(EntityUtils.getIdentifierValue(bean, em)));

//        AuditQuery auditQuery = orderBy.map(o -> {
//            
//            return asc.map(a -> {
//                if (a == true) {
//                    return reader.createQuery()
//                            .forRevisionsOfEntity(classOfEntity, false, false)
//                            .addOrder(AuditEntity.property(o).asc())
//                            .add(AuditEntity.id().eq(EntityUtils.getIdentifierValue(bean, em)));
//                } else {
//                    return reader.createQuery()
//                            .forRevisionsOfEntity(classOfEntity, false, false)
//                            .addOrder(AuditEntity.property(o).desc())
//                            .add(AuditEntity.id().eq(EntityUtils.getIdentifierValue(bean, em)));
//                }
//            }).orElseGet(() -> {
//                return reader.createQuery()
//                        .forRevisionsOfEntity(classOfEntity, false, false)
//                        .addOrder(AuditEntity.property(o).desc())
//                        .add(AuditEntity.id().eq(EntityUtils.getIdentifierValue(bean, em)));
//            });
        from.ifPresent(f -> {
            GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.from(f.atStartOfDay(ZoneId.systemDefault()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
            auditQuery.add(AuditEntity.revisionNumber().le(reader.getRevisionNumberForDate(cal.getTime())));
        });

        //deal with deleted and predraft
//        if (LangkuikExt.class.isAssignableFrom(bean.getClass())) {
//            auditQuery.add(
//                AuditEntity.not(
//                    AuditEntity.or(
//                        AuditEntity.property("status").eq(Status.DELETED),
//                        AuditEntity.property("status").eq(Status.PREDRAFT)
//                    )
//                )
//            );
//        }

        List<Object[]> revisions = pageSize.map(
                p -> auditQuery.setFirstResult(startIndex).setMaxResults(p/*+1*/).getResultList()
        ).orElseGet(
                () -> auditQuery.setFirstResult(startIndex).getResultList()
        );

////        T previousAuditedBean = null;
        //Collections.reverse(revisions);
        for (Object[] revision : revisions) {

//            try {
            T auditedBean = (T) revision[0];
            AuditMetadata auditMetadata = (AuditMetadata) revision[1];
            RevisionType revisionType = (RevisionType) revision[2];

            AuditedEntity auditedEntity = new AuditedEntity();
            auditedEntity.setObject(auditedBean);

            LocalDateTime modifiedTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(
                    auditMetadata.getTimestamp()),
                    TimeZone.getDefault().toZoneId());

            auditedEntity.setModifiedDate(modifiedTime);
            auditedEntity.setUsername(auditMetadata.getUsername());

            if (revisionType == RevisionType.ADD) {
                auditedEntity.setOperation("Created");
            } else if (revisionType == RevisionType.DEL) {
                auditedEntity.setOperation("Deleted");
            } else {
                auditedEntity.setOperation("Updated");
            }
//                auditedEntity.setModifiedDate(auditMetadata.getRevisionDate());
//                auditedEntity.setRevisionNumber(auditMetadata.getId());
//                auditedEntity.setUserId(auditMetadata.getUpdater());
            Map<String, AuditedField> auditedFields = new HashMap<>();

//                Field[] fields = classOfEntity.getDeclaredFields();
//                for (Field field : fields) {
//                    if (field.isAnnotationPresent(Audited.class)) {
//                        AuditedField auditedField = new AuditedField();
//                        field.setAccessible(true);
//                        auditedField.setWebField(field.getAnnotation(WebField.class));
//                        auditedField.setValue(field.get(auditedBean));
////                            if (previousAuditedBean != null) {
////                                auditedField.setValue(field.get(previousAuditedBean));
////                            }
//                        auditedFields.put(field.getName(), auditedField);
//                    }
//
//                }
//                auditedEntity.setAuditedFields(auditedFields);
//                previousAuditedBean = auditedBean;
            auditedEntities.add(auditedEntity);
//            } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
//                Logger.getLogger(AuditTrailQuery.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
        //Collections.reverse(auditedEntities);
        return auditedEntities;
//        return pageSize
//        if (auditedEntities.size() > pageSize) {
//            return auditedEntities.subList(0, pageS);
//        } else {
//            return auditedEntities;
//        }
    }

    /**
     * @return the startingFrom
     */
    public Optional<LocalDate> getStartingFrom() {
        return startingFrom;
    }

    /**
     * @param startingFrom the startingFrom to set
     */
    public void setStartingFrom(Optional<LocalDate> startingFrom) {
        this.startingFrom = startingFrom;
    }

}
