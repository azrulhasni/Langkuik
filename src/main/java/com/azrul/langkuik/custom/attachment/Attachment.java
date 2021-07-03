/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.azrul.langkuik.custom.attachment;

import com.azrul.langkuik.framework.dao.BlobContainer;
import com.azrul.langkuik.framework.annotation.WebField;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import java.io.Serializable;
import java.sql.Blob;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
import org.hibernate.search.bridge.builtin.LongBridge;

/**
 *
 * @author azrulm
 */
@Entity
//@Table(schema = "BANK1")
@Indexed
public class Attachment extends LangkuikExt implements Serializable,BlobContainer {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
//    @Id
//    @Basic(optional = false)
//    @Column(name = "ATTACHMENT_ID")
//    private Long attachmentId;
   

   
    @Column(name = "FILE_NAME")
    @Size(max = 30)
    @Fields({
         @Field(analyze = Analyze.YES, name="File_name"),
         @Field(analyze = Analyze.NO, name="File_name4Sort")
    })
    @SortableField(forField = "File_name4Sort")
    @Audited
    @WebField(displayName="File_name",order=2)
    private String fileName;
    
    @Column(name = "MIME_TYPE")
    private String mimeType;
    
    @Column(name = "RELATIVE_LOCATION")
    private String relativeLocation;
    
    @Column(name = "CREATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @Lob
    @Column(name = "BLOB_DATA")
    @WebField(displayName="File",order=3)
    private Blob blobData;

    public Attachment() {
    }

    public Attachment(Long id) {
        this.id = id;
    }

//    public Long getAttachmentId() {
//        return attachmentId;
//    }
//
//    public void setAttachmentId(Long attachmentId) {
//        this.attachmentId = attachmentId;
//    }

    
    public String getFileName() {
        return fileName;
    }

  
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

  
    public String getRelativeLocation() {
        return relativeLocation;
    }

    
    public void setRelativeLocation(String relativeLocation) {
        this.relativeLocation = relativeLocation;
    }

    
    public Date getCreationDate() {
        return creationDate;
    }

    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }



    
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the attachmentId fields are not set
        if (!(object instanceof Attachment)) {
            return false;
        }
        Attachment other = (Attachment) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    
    public String toString() {
        return fileName;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the blobData
     */
    @Override
    public Blob getBlobData() {
        return blobData;
    }

    /**
     * @param blobData the blobData to set
     */
    public void setBlobData(Blob blobData) {
        this.blobData = blobData;
    }
    
}
