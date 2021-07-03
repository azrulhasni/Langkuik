/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.standard;

import com.azrul.langkuik.framework.annotation.WebField;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.bridge.builtin.EnumBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

/**
 *
 * @author azrul
 */
@Entity
@Audited
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
//@MappedSuperclass
@XmlRootElement
//@Indexed
public abstract class LangkuikExt {
    
    

    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    @WebField(displayName = "Id", order = 1)
    @NumericField(forField="Id4range")
    @Audited
    @SortableField(forField = "Id4sort")
    @Fields({
        @Field(name = "Id", analyze = Analyze.YES, bridge = @FieldBridge(impl = LongBridge.class)),
        @Field(name = "Id4sort", analyze = Analyze.NO),
        @Field(name = "Id4range", analyze = Analyze.YES)
    })
    protected Long id;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(name = "TRANX_ID")
    private String tranxId;
    
   
    @Field(analyze = Analyze.YES, name="tenantId")
    @Column(name = "TENANT_ID")
    protected String tenantId;
    
    @Field(analyze = Analyze.YES, name="creatorId")
    @Column(name = "CREATOR_ID")
    protected String creatorId;
    
    @Audited
    @Field(analyze = Analyze.YES, name="worklist")
    @Column(name = "WORKLIST")
    @SortableField
    @WebField(displayName = "Id", order = 2, visibleInTable = false, visibleInForm = false)
    protected String worklist;
    
    @Audited
    @Field(analyze = Analyze.YES, name="ownerId")
    @Column(name = "OWNER_ID")
    @SortableField
    @WebField(displayName = "Id", order = 3, visibleInTable = false, visibleInForm = false)
    protected String ownerId;
    
    @Audited
    @Column(name = "STATUS")
    @Field(name = "status", index=Index.YES, bridge=@FieldBridge(impl = EnumBridge.class))
    @SortableField
    @WebField(displayName = "Id", order = 4, visibleInTable = false, visibleInForm = false)
    private Status status;
    
    
    protected LangkuikExt(){
        worklist=null;
        status=Status.PREDRAFT;
    }

    /**
     * @return the tenantId
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId the tenantId to set
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * @return the creatorId
     */
    public String getCreatorId() {
        return creatorId;
    }

    /**
     * @param creatorId the creatorId to set
     */
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * @return the worklist
     */
    public Optional<String> getWorklist() {
        return Optional.ofNullable(worklist);
    }

    /**
     * @param worklist the worklist to set
     */
    public void setWorklist(String worklist) {
        this.worklist = worklist;
    }

    /**
     * @return the ownerId
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * @param ownerId the ownerId to set
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

   

    /**
     * @return the tranxId
     */
    public String getTranxId() {
        return tranxId;
    }

    /**
     * @param tranxId the tranxId to set
     */
    public void setTranxId(String tranxId) {
        if (tranxId!=null){
            this.tranxId = tranxId;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.id);
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
        final LangkuikExt other = (LangkuikExt) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LangkuikExt{" + "id=" + id + '}';
    }
    
    
}
