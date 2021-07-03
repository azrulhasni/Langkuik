/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.azrul.langkuik.loanorigsystem.model;

import com.azrul.langkuik.framework.annotation.WebEntity;
import com.azrul.langkuik.framework.annotation.WebField;
import com.azrul.langkuik.framework.annotation.WebRelation;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.bridge.builtin.LongBridge;

/**
 *
 * @author azrulm
 */
@Entity
//@Table(schema = "BANK1")
@WebEntity(name="Collateral")
@Indexed
public class Collateral extends LangkuikExt implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation

//     @Id
//     @Audited
//    @Basic(optional = false)
//    @Column(name = "COLLATERAL_ID")
//    @NumericField(forField = "Id4sort")
//    @SortableField(forField = "Id4sort")
//    @Fields({
//        @Field(name = "Id", analyze = Analyze.YES, bridge = @FieldBridge(impl = LongBridge.class)),
//        @Field(name = "Id4sort", analyze = Analyze.NO)
//    })
//    @WebField(displayName = "Id", order = 1)
//    private Long collateralId;
    
    

    @Fields({
        @Field(name = "Value", analyze = Analyze.YES, bridge = @FieldBridge(impl = LongBridge.class)),
        @Field(name = "Value4sort", analyze = Analyze.NO)
    })
    @NumericField(forField = "Value4sort")
    @SortableField(forField = "Value4sort")
    @Column(name = "VALUE")
    @WebField(displayName = "Value", order = 2)
    @DecimalMin(value="0", inclusive=false, message="The value must be bigger than 0 ")
    private Long value;
    
    @WebField(displayName = "Require appraisal", order = 6)
    @Column(name = "REQUIRE_APPRAISAL")
    private Boolean requireAppraisal;
    
   
    @WebRelation(name = "Address",order = 1)
    @ManyToOne(fetch=FetchType.EAGER)
    private Address addressCollection;

    public Collateral() {
    }

    public Collateral(Long id) {
        this.id = id;
    }

//    public Long getCollateralId() {
//        return collateralId;
//    }
//
//    public void setCollateralId(Long collateralId) {
//        this.collateralId = collateralId;
//    }

  

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Boolean getRequireAppraisal() {
        return requireAppraisal;
    }

    public void setRequireAppraisal(Boolean requireAppraisal) {
        this.requireAppraisal = requireAppraisal;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Collateral)) {
            return false;
        }
        Collateral other = (Collateral) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (addressCollection!=null){
            return "at "+addressCollection;
        }else{
            return "["+id+"]";
        }
    }

    /**
     * @return the addressCollection
     */
    public Address getAddressCollection() {
        return addressCollection;
    }

    /**
     * @param addressCollection the addressCollection to set
     */
    public void setAddressCollection(Address addressCollection) {
        this.addressCollection = addressCollection;
    }
    
}
