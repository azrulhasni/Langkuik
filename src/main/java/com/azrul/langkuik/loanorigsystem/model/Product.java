/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.azrul.langkuik.loanorigsystem.model;

import com.azrul.langkuik.framework.annotation.WebEntity;
import com.azrul.langkuik.framework.annotation.WebField;
import com.azrul.langkuik.custom.lookupchoice.Lookup;
import com.azrul.langkuik.framework.annotation.WebEntityType;
import com.azrul.langkuik.framework.dao.Integer2StringBridge;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.SortableField;

/**
 *
 * @author azrulm
 */
@Entity
//@Table(schema = "BANK1")
@WebEntity(name="Product",type=WebEntityType.REF)
@XmlRootElement
@Indexed
public class Product extends LangkuikExt implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
   
//    @Id
//    @Audited
//    @Column(name = "PRODUCT_ID")
//    @Field(name="Id")
//    @Basic(optional = false)
//    @NumericField(forField = "Id4sort")
//    @SortableField(forField = "Id4sort")
//    @Fields({
//        @Field(name = "Id", analyze = Analyze.YES, bridge = @FieldBridge(impl = LongBridge.class)),
//        @Field(name = "Id4sort", analyze = Analyze.NO)
//    })
//    @WebField(displayName = "Id", order = 1)
//    private Long productId;
    
   
    @Fields({
         @Field(analyze = Analyze.YES, name="Product"),
         @Field(analyze = Analyze.NO, name="Product4Sort")
    })
    @SortableField(forField = "Product4Sort")
    @WebField(displayName = "Product", order = 2)
    @NotEmpty(message="Product cannot be empty")
    @Lookup(entity=ProductListing.class,field = "productName")
    private String product;
    
   

//    @OneToMany(mappedBy = "productId", fetch = FetchType.LAZY)
//    private Collection<Application> applicationCollection;

    public Product() {
    }

    public Product(Long productId) {
        this.id = productId;
    }

//    public Long getProductId() {
//        return productId;
//    }
//
//    public void setProductId(Long productId) {
//        this.productId = productId;
//    }

    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Product)) {
            return false;
        }
        Product other = (Product) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return product+" ["+id+"]";
    }

    /**
     * @return the product
     */
    public String getProduct() {
        return product;
    }

    /**
     * @param product the product to set
     */
    public void setProduct(String product) {
        this.product = product;
    }
    
}
