package com.azrul.langkuik.framework.audit;

import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author azrul
 */
@Entity
@Table(name = "REVINFO")
@RevisionEntity(LangkuikRevisionEntityListener.class)
public class AuditMetadata extends DefaultRevisionEntity {

    private static long serialVersionUID = 1L;

    

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "DATETIME")
    private Timestamp dateTime;

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getId());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AuditMetadata other = (AuditMetadata) obj;
        if (!Objects.equals(this.getId(), other.getId())) {
            return false;
        }
        return true;
    }

   

    /**
     * @return the dateTime
     */
    public Timestamp getDateTime() {
        return dateTime;
    }

    /**
     * @param dateTime the dateTime to set
     */
    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

//    /**
//     * @return the revisionId
//     */
//    public Long getRevisionId() {
//        return revisionId;
//    }
//
//    /**
//     * @param revisionId the revisionId to set
//     */
//    public void setRevisionId(Long revisionId) {
//        this.revisionId = revisionId;
//    }
}
