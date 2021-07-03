/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.loanorigsystem.model;

import com.azrul.langkuik.framework.annotation.WebEntity;
import com.azrul.langkuik.framework.annotation.WebEntityType;
import com.azrul.langkuik.framework.annotation.WebField;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;

/**
 *
 * @author azrul
 */
@Entity
//@Table(schema = "BANK1")
@XmlRootElement
@WebEntity(name="Relationship Manager",type=WebEntityType.REF)
@Indexed
public class RelationshipManager extends LangkuikExt {
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
    @Size(min = 5, max = 50, message="Forename must be at least 5 characters long")
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
    
    
     @Fields({
        @Field(analyze = Analyze.YES,name="Staff_id"),
        @Field(analyze = Analyze.NO,name="Staff_id4sort")
    })
    @SortableField(forField="Staff_id4sort")
    @Audited
    @WebField(displayName="Staff_id",order=3)
    @NotEmpty(message="Staff id cannot be empty")
    @Size(min = 3, max = 50, message="Staff id must be at least 3 characters long")
    private String staffId;

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
     * @return the staffId
     */
    public String getStaffId() {
        return staffId;
    }

    /**
     * @param staffId the staffId to set
     */
    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
}
