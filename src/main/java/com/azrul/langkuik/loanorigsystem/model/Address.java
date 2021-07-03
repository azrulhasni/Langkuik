/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.azrul.langkuik.loanorigsystem.model;

import com.azrul.langkuik.framework.annotation.WebEntity;
import com.azrul.langkuik.framework.annotation.WebField;
import com.azrul.langkuik.custom.lookupchoice.Lookup;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;

/**
 *
 * @author azrulm
 */

//@Table(schema = "public")
@Entity
@XmlRootElement
@Indexed
@WebEntity(name="Address")
public class Address extends LangkuikExt implements Serializable {
     private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
//    @Id
//    @NumericField(forField="Id4sort")   
//    @SortableField(forField="Id4sort")
//    @Fields( {
//        @Field(name="Id",analyze = Analyze.YES, bridge = @FieldBridge(impl = LongBridge.class)),
//        @Field(name = "Id4sort", analyze = Analyze.NO)
//        } ) 
//    @Basic(optional = false)
//    @Column(name = "ADDRESS_ID")
//    @WebField(displayName="Id",order=1)
//    private Long addressId;
     
   
    @Fields({
        @Field(analyze = Analyze.YES, name="Street_address"),
        @Field(analyze = Analyze.NO, name="Street_address4sort")
    })
    @SortableField(forField="Street_address4sort")
    @Size(max = 100)
    @Column(name = "STREET_ADDRESS")
    @WebField(displayName="Street address",order=2)
    @NotEmpty(message = "Street address cannot be empty")
    @Size(min = 5, max = 255, message="Street address must be more than characters")
    private String streetAddress;

    @Override
    public String toString() {
        return "streetAddress=" + streetAddress + ':';
    }
    
 
    @Fields({
        @Field(analyze = Analyze.YES, name="City"),
        @Field(analyze = Analyze.NO, name="City4sort")
    })
    @SortableField(forField="City4sort")
    @Size(max = 50)
    @Column(name = "CITY")
    @WebField(displayName="City",order=5)
    @Size(min = 2, max = 255, message="City must be more than characters")
    @NotEmpty(message = "City cannot be empty")
     @Lookup(entity=DistrictStateCountry.class,field = "district", filterBy="state")
    private String city;

    @Fields({
        @Field(analyze = Analyze.YES, name="Country"),
        @Field(analyze = Analyze.NO, name="Country4sort")
    })
    @SortableField(forField="Country4sort")
    @Size(max = 50)
    @Column(name = "COUNTRY")
    @WebField(displayName="Country",order=3)
    @Size(min = 2, max = 255, message="Country must be more than characters")
    @NotEmpty(message = "Country cannot be empty")
    @Lookup(entity=DistrictStateCountry.class,field = "country")
    private String country;
    
    @Fields({
        @Field(analyze = Analyze.YES, name="State"),
        @Field(analyze = Analyze.NO, name="State4sort")
    })
    @SortableField(forField="State4sort")
    @Size(max = 50)
    @Column(name = "STATE")
    @WebField(displayName="State",order=4)
    @Size(min = 2, max = 255, message="State must be more than characters")
    @NotEmpty(message = "Country cannot be empty")
     @Lookup(entity=DistrictStateCountry.class,field = "state", filterBy = "country")
    private String state;
    
    @Fields({
        @Field(analyze = Analyze.YES, name="Postal_code"),
        @Field(analyze = Analyze.NO, name="Postal_code4sort")
    })
    @SortableField(forField="Postal_code4sort")
    @Size(max = 50)
    @Column(name = "POSTALCODE")
    @WebField(displayName="Postal code", order=6)
    @Size(min = 5, max = 5, message="Postal code must be 5 character long")
    @NotEmpty(message = "Postal code cannot be empty")
    private String postalcode;

    
    @Fields({
        @Field(analyze = Analyze.YES, name="Phone_number"),
        @Field(analyze = Analyze.NO, name="Phone_number4sort")
    })
    @SortableField(forField="Phone_number4sort")
    @Size(max = 50)
    @Column(name = "Phone_number")
    @WebField(displayName="Phone number", order=7)
    private String phoneNumber;
    /**
     * @return the streetAddress
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * @param streetAddress the streetAddress to set
     */
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the postalcode
     */
    public String getPostalcode() {
        return postalcode;
    }

    /**
     * @param postalcode the postalcode to set
     */
    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

//    /**
//     * @return the addressId
//     */
//    public Long getAddressId() {
//        return addressId;
//    }
//
//    /**
//     * @param addressId the addressId to set
//     */
//    public void setAddressId(Long addressId) {
//        this.addressId = addressId;
//    }

    /**
     * @return the phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber the phoneNumber to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    
}
