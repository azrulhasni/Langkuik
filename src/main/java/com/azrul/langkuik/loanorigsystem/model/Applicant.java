/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.azrul.langkuik.loanorigsystem.model;

import com.azrul.langkuik.framework.annotation.WebEntity;
import com.azrul.langkuik.framework.annotation.WebField;
import com.azrul.langkuik.framework.annotation.WebRelation;
import com.azrul.langkuik.framework.dao.Date2LongFieldBridge;
import com.azrul.langkuik.framework.dao.Date2StringFieldBridge;
import com.azrul.langkuik.framework.dao.Integer2StringBridge;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.bridge.builtin.IntegerBridge;

/**
 *
 * @author azrulm
 */
@Entity
//@Table(schema = "BANK1")
@XmlRootElement
@WebEntity(name="Applicant")
@Indexed
public class Applicant extends LangkuikExt implements Serializable {
    private static long serialVersionUID = 1L;

    /**
     * @return the serialVersionUID
     */
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    /**
     * @param aSerialVersionUID the serialVersionUID to set
     */
    public static void setSerialVersionUID(long aSerialVersionUID) {
        serialVersionUID = aSerialVersionUID;
    }
//    @Id
//    @Field(name="Id")
//    @NumericField(forField="Id")
//    @SortableField(forField="Id")
//    @Basic(optional = false)
//    @NotNull
//    @Audited
//    @Column(name = "APPLICANT_ID")
//    @WebField(displayName="Id")
    /*@GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator",
            parameters = {
                    @Parameter(
                            name = "uuid_gen_strategy_class",
                            value = "org.hibernate.id.uuid.CustomVersionOneStrategy"
                    )
            }
    )*/
    
//    @Id
//    @NumericField(forField="Id4sort")   
//    @SortableField(forField="Id4sort")
//    @Fields( {
//        @Field(name="Id",analyze = Analyze.YES, bridge = @FieldBridge(impl = LongBridge.class)),
//        @Field(name = "Id4sort", analyze = Analyze.NO)
//        } ) 
//    @Basic(optional = false)
//    @Column(name = "APPLICANT_ID")
//    @WebField(displayName="Id", order=1)
//    @Audited
//    private Long applicantId;
    
    
    
    //-----------Personal info---------------------------------------------//
    @Fields({
        @Field(analyze = Analyze.YES, name="Forename"),
        @Field(analyze = Analyze.NO, name="Forename4sort")
    })
    @SortableField(forField="Forename4sort")
    @Audited
    @WebField(displayName="Forename",order=2)
    @NotEmpty(message="Forename cannot be empty")
    @Size(min = 3, max = 50, message="Forename must be at least 3 characters long")
    private String forename;  
      
    @Fields({
        @Field(analyze = Analyze.YES,name="Surname"),
        @Field(analyze = Analyze.NO,name="Surname4sort")
    })
    @SortableField(forField="Surname4sort")
    @Audited
    @WebField(displayName="Surname",order=3)
    @NotEmpty(message="Surname cannot be empty")
    @Size(min = 3, max = 50, message="Surname must be at least 3 characters long")
    private String surname;
    
    @Fields( {
        @Field(name="Age",analyze = Analyze.YES, 
                bridge = @FieldBridge(impl= Integer2StringBridge.class)),
        @Field(name = "Age4sort", analyze = Analyze.NO),
        @Field(name = "Age4range", analyze = Analyze.YES)
        } ) 
    @NumericField(forField="Age4range")
    @SortableField(forField="Age4sort")
    @Audited
    @Column(name = "AGE_OF_APPLICANT")
    @WebField(displayName="Age",order=4)
    @DecimalMin(value="18", inclusive=true, message="Age must be at least 18")
    private Integer ageOfApplicant;
     
    @Fields( {
        @Field(name="Date_of_birth",analyze = Analyze.YES, bridge=@FieldBridge(impl=Date2StringFieldBridge.class)),
        @Field(name = "Date_of_birth4sort", analyze = Analyze.NO),
        @Field(name = "Date_of_birth4range", analyze = Analyze.YES, bridge=@FieldBridge(impl=Date2LongFieldBridge.class))
        } ) 
    @SortableField(forField="Date_of_birth4sort")
    @WebField(displayName="Date of birth",order=5)
    //@DateBridge(resolution=Resolution.DAY,encoding = EncodingType.STRING)
    @Audited
    @Column(name = "DATE_OF_BIRTH")
    @NumericField(forField="Date_of_birth4range")
    //@Temporal(TemporalType.TIMESTAMP)
    private LocalDate dateOfBirth;
    
    @Size(max = 1)
    @Audited
    private String gender;
    
    @Fields({
        @Field(analyze = Analyze.YES, name="Email"),
        @Field(analyze = Analyze.NO, name="Email4sort")
    })
    @SortableField(forField="Email4sort")
    @Audited
    @Size(max = 200)
    @Column(name = "EMAIL_ADDRESS")
    @WebField(displayName="Email",order=6)
    @Email(message="The email format must be <name>@<host>.<extension>")
    private String emailAddress;

    
    @Size(max = 1)
    @Column(name = "MARITAL_STATUS")
    @Audited
    private String maritalStatus;
    
    @Column(name = "TIME_AT_CURRENT_ADDRESS_MONTHS")
    @Audited
    private Integer timeAtCurrentAddressMonths;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @WebRelation(name="Current address",order=1)
    private Address currentAddressCollection;
    
    @ManyToOne(fetch=FetchType.LAZY)
    private Address previousAddressCollection;
    
    //-----------Employer info---------------------------------------------//
    
    
    @Size(max = 30)
    @Column(name = "EMPLOYERNAME")
    private String employername;
    
   
    @ManyToOne(fetch=FetchType.LAZY)
    private Address employerAddressCollection;
   
    
   //-----------Financial info---------------------------------------------//
  
    @Fields( {
        @Field(name="Annual_salary",analyze = Analyze.YES, bridge=@FieldBridge(impl=IntegerBridge.class)),
        @Field(name = "Annual_salary4sort", analyze = Analyze.NO)
        } )
    @SortableField(forField="Annual_salary4sort")
    @NumericField(forField="Annual_salary4sort")
    @Column(name = "ANNUAL_SALARY")
    @WebField(displayName="Annual_salary", order=7)
    private Integer annualSalary;
    
 
    
 

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Applicant)) {
            return false;
        }
        Applicant other = (Applicant) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return forename+" "+surname;
    }

    /**
     * @return the currentAddressCollection
     */
    public Address getCurrentAddressCollection() {
        return currentAddressCollection;
    }

    /**
     * @param currentAddressCollection the currentAddressCollection to set
     */
    public void setCurrentAddressCollection(Address currentAddressCollection) {
        this.currentAddressCollection = currentAddressCollection;
    }

    /**
     * @return the previousAddressCollection
     */
    public Address getPreviousAddressCollection() {
        return previousAddressCollection;
    }

    /**
     * @param previousAddressCollection the previousAddressCollection to set
     */
    public void setPreviousAddressCollection(Address previousAddressCollection) {
        this.previousAddressCollection = previousAddressCollection;
    }

//    /**
//     * @return the applicantId
//     */
    
   

//    /**
//     * @param applicantId the applicantId to set
//     */
//    public void setApplicantId(Long applicantId) {
//        this.applicantId = applicantId;
//    }

    /**
     * @return the forename
     */
    public String getForename() {
        return forename;
    }

    /**
     * @param forename the forename to set
     */
    public void setForename(String forename) {
        this.forename = forename;
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * @return the ageOfApplicant
     */
    public Integer getAgeOfApplicant() {
        return ageOfApplicant;
    }

    /**
     * @param ageOfApplicant the ageOfApplicant to set
     */
    public void setAgeOfApplicant(Integer ageOfApplicant) {
        this.ageOfApplicant = ageOfApplicant;
    }

    /**
     * @return the dateOfBirth
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth the dateOfBirth to set
     */
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * @return the emailAddress
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * @param emailAddress the emailAddress to set
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * @return the maritalStatus
     */
    public String getMaritalStatus() {
        return maritalStatus;
    }

    /**
     * @param maritalStatus the maritalStatus to set
     */
    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    /**
     * @return the timeAtCurrentAddressMonths
     */
    public Integer getTimeAtCurrentAddressMonths() {
        return timeAtCurrentAddressMonths;
    }

    /**
     * @param timeAtCurrentAddressMonths the timeAtCurrentAddressMonths to set
     */
    public void setTimeAtCurrentAddressMonths(Integer timeAtCurrentAddressMonths) {
        this.timeAtCurrentAddressMonths = timeAtCurrentAddressMonths;
    }

    /**
     * @return the employername
     */
    public String getEmployername() {
        return employername;
    }

    /**
     * @param employername the employername to set
     */
    public void setEmployername(String employername) {
        this.employername = employername;
    }


    /**
     * @return the annualSalary
     */
    public Integer getAnnualSalary() {
        return annualSalary;
    }

    /**
     * @param annualSalary the annualSalary to set
     */
    public void setAnnualSalary(Integer annualSalary) {
        this.annualSalary = annualSalary;
    }


   

    /**
     * @return the employerAddressCollection
     */
    public Address getEmployerAddressCollection() {
        return employerAddressCollection;
    }

    /**
     * @param employerAddressCollection the employerAddressCollection to set
     */
    public void setEmployerAddressCollection(Address employerAddressCollection) {
        this.employerAddressCollection = employerAddressCollection;
    }

   
    
}
