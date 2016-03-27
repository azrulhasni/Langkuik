/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.customtype.attachment;

import java.util.Date;
import org.azrul.langkuik.framework.customtype.CustomType;

/**
 *
 * @author azrulm
 */

public interface AttachmentCustomType extends CustomType{

    String getFileName();

    void setFileName(String fileName);

    String getRelativeLocation();

    void setRelativeLocation(String relativeLocation);

    Date getCreationDate();

    void setCreationDate(Date creationDate);
    
    String getMimeType();

    void setMimeType(String mimeType);
    
    @Override
    String toString();
}
