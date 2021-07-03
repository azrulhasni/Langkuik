/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.loanorigsystem.model;

import com.azrul.langkuik.framework.annotation.WebEntity;
import com.azrul.langkuik.framework.annotation.WebField;
import com.azrul.langkuik.framework.annotation.WebRelation;
import com.azrul.langkuik.custom.attachment.AttachmentRenderer;
import com.azrul.langkuik.custom.attachment.Attachments;
import com.azrul.langkuik.framework.annotation.WebEntityType;
import com.azrul.langkuik.framework.dao.Date2StringFieldBridge;
import com.azrul.langkuik.framework.dao.Integer2StringBridge;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import com.azrul.langkuik.loanorigsystem.workflow.LoanWorkflow;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
//import org.azrul.langkuik.framework.Work;
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
@Entity()
//@Table(schema = "BANK1")
@XmlRootElement
@WebEntity(name="Application",type=WebEntityType.ROOT, workflow=LoanWorkflow.class)
@Indexed
public class Application extends LangkuikExt /*extends Work<WorklistStatus>*/ implements Serializable {

    

    public Application() {
        //this.status=WorklistStatus.Init;
    }

    private static final long serialVersionUID = 1L;

//    @Id
//    @NumericField(forField = "Id4sort")
//    @SortableField(forField = "Id4sort")
//    @Fields({
//        @Field(name = "Id", analyze = Analyze.YES, bridge = @FieldBridge(impl = LongBridge.class)),
//        @Field(name = "Id4sort", analyze = Analyze.NO)
//    })
//    @Basic(optional = false)
//    @Column(name = "APPLICATION_ID")
//    @WebField(displayName = "Id", order = 1)
//    @Audited
//    private Long applicationId;
    @Column(name = "ACCEPTED_DATE")
    @Audited
    @Fields({
        @Field(name = "Accepted_date", analyze = Analyze.YES, bridge = @FieldBridge(impl = Date2StringFieldBridge.class)),
        @Field(name = "Accepted_date4sort", analyze = Analyze.NO),
        @Field(name = "Accepted_date4range", analyze = Analyze.YES)
    })
    @NumericField(forField="Accepted_date4range")
    @SortableField(forField = "Accepted_date4sort")
    @WebField(displayName = "Accepted date", order = 200)
    private LocalDate acceptedDate;

    @Size(max = 16)
    @Column(name = "ACCOUNT_NUMBER")
    @Audited
    @Fields({
        @Field(analyze = Analyze.YES, name = "Account_number"),
        @Field(analyze = Analyze.NO, name = "Account_number4sort")
    })
    @SortableField(forField = "Account_number4sort")
    @WebField(displayName = "Account number", isReadOnly = true, order = 300)
    @NotEmpty(message="Account number cannot be empty")
    @Size(min = 8, max=10, message="Account number must be more than 8 characters")
    private String accountNumber;

    @Column(name = "APPLICATION_DATE")
    @Fields({
        @Field(name = "Application_date", analyze = Analyze.YES, bridge = @FieldBridge(impl = Date2StringFieldBridge.class)),
        @Field(name = "Application_date4sort", analyze = Analyze.NO),
        @Field(name = "Application_date4range", analyze = Analyze.NO)
    })
    @NumericField(forField="Application_date4range")
    @SortableField(forField = "Application_date4sort")
    @Audited
    @WebField(displayName = "Application date", order = 400)
    private LocalDate applicationDate;

    @Size(max = 14)
    @Column(name = "APPLICATION_NUMBER")
    @Fields({
        @Field(analyze = Analyze.YES, name = "Application_number"),
        @Field(analyze = Analyze.NO, name = "Application_number4sort")
    })
    @SortableField(forField = "Application_number4sort")
    @Audited
    @WebField(displayName = "Application number", order = 500)
    @NotEmpty(message="Application number cannot be empty")
    private String applicationNumber;

    @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @WebRelation(name = "Product", order = 550)
    private Product productId;
    
    @JoinColumn(name = "RELATION_MANAGER_ID", referencedColumnName = "ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @WebRelation(name = "Relationship manager", order = 600)
    private RelationshipManager relationManager;

    @ManyToMany(/*mappedBy = "applicationId", */fetch = FetchType.LAZY)
    @JoinTable(
            //schema = "BANK1",
            name = "APPLICATIONS_APPLICANTS",
            
            joinColumns = {
                @JoinColumn(name = "APPLICATION_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {
                @JoinColumn(name = "APPLICANT_ID", referencedColumnName = "ID")})
    @WebRelation(name = "Applicants", order = 700)
    private Set<Applicant> applicantsCollection;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    /*@JoinTable(
            //schema = "BANK1",
            name = "APPLICATION_COLLATERAL",
            joinColumns = {
                @JoinColumn(name = "APPLICATION_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {
                @JoinColumn(name = "COLLATERAL_ID", referencedColumnName = "ID")})*/
    @WebRelation(name = "Collaterals", order = 800)
    private Set<Collateral> collateralCollection;

   
    
     @Column(name = "ADVANCE")
    @Fields( {
        @Field(name="Advance",analyze=Analyze.YES,
                bridge = @FieldBridge(impl = Integer2StringBridge.class)),   
        @Field(name = "Advance4sort", analyze = Analyze.NO),
        @Field(name = "Advance4range", analyze = Analyze.YES)
        } ) 
    @NumericField(forField="Advance4range")
    @SortableField(forField="Advance4sort")
    @Audited
    @WebField(displayName="Advance",order=900)
    @Min(value=1,message="Advance cannot be zero")
    @NotNull(message="Advance cannot be zero")
    private Integer advance;
    
    @Column(name = "REQUESTED_LOAN_AMOUNT")
    @Fields( {
        @Field(name="Requested_Loan_Ammount",analyze=Analyze.YES,
          bridge = @FieldBridge(impl = Integer2StringBridge.class)),
        @Field(name = "Requested_Loan_Ammount4sort", analyze = Analyze.NO),
        @Field(name="Requested_Loan_Ammount4range",analyze=Analyze.YES)
        } ) 
    @NumericField(forField="Requested_Loan_Ammount4range")
    @SortableField(forField="Requested_Loan_Ammount4sort")
    @Audited
    @WebField(displayName="Requested Loan Ammount",order=1000)
    @Min(value=1,message="Loan ammount cannot be zero")
    @NotNull(message="Loan ammount cannot be zero")
    private Integer requestedLoanAmount;  
     
    @Column(name = "REQUESTED_LOAN_TERM")
    @Fields( {
        @Field(name="Requested_Loan_Term",analyze=Analyze.YES,
                bridge = @FieldBridge(impl= Integer2StringBridge.class)),
        @Field(name = "Requested_Loan_Term4sort", analyze = Analyze.NO),
        @Field(name="Requested_Loan_Term4range",analyze=Analyze.YES)
    } ) 
    @NumericField(forField="Requested_Loan_Term4range")
    @SortableField(forField="Requested_Loan_Term4sort")
    @Audited
    @WebField(displayName="Requested Loan Term",order=1100)
    @Min(value=1,message="Loan term cannot be zero")
    @NotNull(message="Loan ammount cannot be zero")
    private Integer requestedLoanTerm;
    
    @WebField(displayName = "Approved", order = 1200)
    @Column(name = "APPROVED")
    private Boolean approved;
    
    @OneToOne(/*mappedBy = "applicationId", */fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "attachmentCollection_id", referencedColumnName = "id")    
    @WebRelation(name = "Attachments", order = 1300, customComponent = AttachmentRenderer.class)
    private Attachments attachmentCollection;

    public Application(Long id) {
        this.id = id;
        this.approved=false;
    }

//    public Long getApplicationId() {
//        return applicationId;
//    }
//
//    public void setApplicationId(Long applicationId) {
//        this.applicationId = applicationId;
//    }
    public LocalDate getAcceptedDate() {
        return acceptedDate;
    }

    public void setAcceptedDate(LocalDate acceptedDate) {
        this.acceptedDate = acceptedDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

//    public Integer getApplicationStatus() {
//        return applicationStatus;
//    }
//
//    public void setApplicationStatus(Integer applicationStatus) {
//        this.applicationStatus = applicationStatus;
//    }
    public Product getProductId() {
        if (productId == null) {
            productId = new Product();
        }
        return productId;
    }

    public void setProductId(Product productId) {
        this.productId = productId;
    }

    @XmlTransient
    public Set<Applicant> getApplicantsCollection() {
        if (applicantsCollection == null) {
            applicantsCollection = new HashSet<Applicant>();
        }
        return applicantsCollection;
    }

    public void setApplicantsCollection(Set<Applicant> applicantsCollection) {
        this.applicantsCollection = applicantsCollection;
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
        if (!(object instanceof Application)) {
            return false;
        }
        Application other = (Application) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "["+ id + " ]";
    }

    @PrePersist
    void prePersist() {
        updateDates();
        //assignApplicationNumber();
    }

    void updateDates() {
        if (applicationDate == null) {
            GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
            cal.setTime(new Date());
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);

            applicationDate = cal.getTime().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
    }

    @PostPersist
    void assignApplicationNumber() {
        DecimalFormat df = new DecimalFormat("############");
        setApplicationNumber(df.format(id));
    }

    /*@WebOp(name = "Submit", onEvent = EventType.OnSubmit, userMap = @OpUserMap(role = "*", right = OpRight.EXECUTE))
    public Work.Result doSubmit() {
        Work.Result res = new Work.Result();
        if (getApplicationStatus() == 5) {
            if (this.getStatus().equals(WorklistStatus.Init)) {
                res.setMessage("OK");
                res.setNextWorklist(WorklistStatus.Underwriting);
            } else if (this.getStatus().equals(WorklistStatus.Underwriting)) {
                res.setMessage("OK");
                res.setNextWorklist(WorklistStatus.Approved);
            }
        } else {
            res.setMessage("NOT OK");
            res.setNextWorklist(this.getStatus());
        }
        return res;        
    }*/
    

    /**
     * @return the collateralCollection
     */
    public Set<Collateral> getCollateralCollection() {
        return collateralCollection;
    }

    /**
     * @param collaterals the collateralCollection to set
     */
    public void setCollateralCollection(Set<Collateral> collateralCollection) {
        this.collateralCollection = collateralCollection;
    }
    
    public Integer getAdvance() {
        return advance;
    }

    public void setAdvance(Integer advance) {
        this.advance = advance;
    }


    public Integer getRequestedLoanAmount() {
        return requestedLoanAmount;
    }

    public void setRequestedLoanAmount(Integer requestedLoanAmount) {
        this.requestedLoanAmount = requestedLoanAmount;
    }

    public Integer getRequestedLoanTerm() {
        return requestedLoanTerm;
    }

    public void setRequestedLoanTerm(Integer requestedLoanTerm) {
        this.requestedLoanTerm = requestedLoanTerm;
    }

    /**
     * @return the attachmentCollection
     */
    public Attachments getAttachmentCollection() {
        return attachmentCollection;
    }

    /**
     * @param attachmentCollection the attachmentCollection to set
     */
    public void setAttachmentCollection(Attachments attachmentCollection) {
        this.attachmentCollection = attachmentCollection;
    }

    /**
     * @return the relationManager
     */
    public RelationshipManager getRelationManager() {
        return relationManager;
    }

    /**
     * @param relationManager the relationManager to set
     */
    public void setRelationManager(RelationshipManager relationManager) {
        this.relationManager = relationManager;
    }

    /**
     * @return the approved
     */
    public Boolean isApproved() {
        return getApproved();
    }

    /**
     * @param approved the approved to set
     */
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    /**
     * @return the approved
     */
    public Boolean getApproved() {
        return approved;
    }

}
