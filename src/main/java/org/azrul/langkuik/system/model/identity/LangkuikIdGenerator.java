/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.azrul.langkuik.system.model.identity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.azrul.langkuik.framework.generator.Generator;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

/**
 *
 * @author azrulhasni
 */
@Entity
@Indexed
@Table(name = "LANGKUIK_ID_GENERATOR",schema = "SYSTEM")
public class LangkuikIdGenerator implements Generator<Long>,Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Field
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LangkuikIdGenerator)) {
            return false;
        }
        LangkuikIdGenerator other = (LangkuikIdGenerator) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.azrul.langkui.system.model.identity.LangkuikIdGenerator[ id=" + id + " ]";
    }

    @Override
    public Long getValue() {
        return id;
    }
    
}
