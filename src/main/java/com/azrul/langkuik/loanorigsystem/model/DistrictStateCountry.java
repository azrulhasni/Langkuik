/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.loanorigsystem.model;

import com.azrul.langkuik.framework.standard.LangkuikExt;
import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 *
 * @author azrul
 */
@Entity
@Cacheable 
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
//@Table(schema = "BANK1")
public class DistrictStateCountry extends LangkuikExt {

    @Column(name = "DISTRICT")
    private String district;

    @Column(name = "STATE")
    private String state;

    @Column(name = "COUNTRY")
    private String country;
    
    public DistrictStateCountry(){
        
    }

    public DistrictStateCountry(String district, String state, String country) {
        this.district = district;
        this.state = state;
        this.country = country;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final DistrictStateCountry other = (DistrictStateCountry) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    

    /**
     * @return the district
     */
    public String getDistrict() {
        return district;
    }

    /**
     * @param district the district to set
     */
    public void setDistrict(String district) {
        this.district = district;
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

}
