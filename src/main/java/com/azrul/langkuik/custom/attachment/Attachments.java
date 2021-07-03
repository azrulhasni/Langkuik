/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.custom.attachment;

import com.azrul.langkuik.framework.annotation.WebRelation;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

/**
 *
 * @author azrul
 */
@Entity
public class Attachments extends LangkuikExt implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public Attachments(){
        
    }

    public Attachments(Long id) {
        this.id = id;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
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
        final Attachments other = (Attachments) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    
    
    @OneToMany
    @JoinColumn(name = "attachments_id")
    @WebRelation(name="Attachments",order=1)
    private Set<Attachment> attachments;

    /**
     * @return the attachments
     */
    public Set<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * @param attachments the attachments to set
     */
    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }
            
            
    
}
