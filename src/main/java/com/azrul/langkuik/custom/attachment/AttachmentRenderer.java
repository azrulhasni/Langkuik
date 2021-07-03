/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.custom.attachment;

import com.azrul.langkuik.framework.dao.DataAccessObject;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.azrul.langkuik.custom.CustomComponentRenderer;
import com.azrul.langkuik.framework.dao.Dual;
import com.azrul.langkuik.framework.dao.FindRelationParameter;
import com.azrul.langkuik.framework.dao.FindRelationQuery;
import com.azrul.langkuik.framework.relationship.RelationMemento;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import com.azrul.langkuik.views.pojo.PojoTableFactory;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.VaadinSession;
import elemental.json.Json;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.serial.SerialBlob;
import javax.validation.ValidatorFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author azrul
 */
public class AttachmentRenderer<P> implements CustomComponentRenderer<P> {
    private static final int LIMIT=3;
    
     @Value("${application.lgDateFormatLocale}")
    private String dateFormatLocale;
    
    @Value("${application.lgDateFormat}")
    private String dateFormat;

    @Autowired
    private DataAccessObject dao;

    @Autowired
    ValidatorFactory validatorFactory;
    
    @Autowired
    PojoTableFactory pojoTableFactory;

  

    @Override
    public Optional<P> renderInFormAsDependency(P parent, 
            String relationName,
            VerticalLayout layout,
            Map<String, RelationMemento> relationMementos) {
        
        final String username = (String) VaadinSession.getCurrent().getSession().getAttribute("USERNAME");
        final String tenant = (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");
        final LangkuikExt ext = (LangkuikExt) parent;
        final Span spanErrorMsg = new Span();
        
        this.dateFormat = dateFormat;

        VerticalLayout uploadLayout = new VerticalLayout();
        FindRelationQuery query = new FindRelationQuery(new FindRelationParameter(parent, relationName));
        Collection<Attachments> attchs = dao.runQuery(query, Optional.empty(), Optional.of(Boolean.FALSE), Optional.of(0), Optional.of(LIMIT), Optional.of(tenant), Optional.empty());
        Attachments atts = null;
        if (attchs.isEmpty()) {

            Optional<Dual<P, Attachments>> oDualAttachments = dao.createAssociateAndSave(Attachments.class, parent, relationName, Optional.of(tenant), username);
            
            Dual<P, Attachments> dualAttachments = oDualAttachments.orElseThrow();
            atts = dualAttachments.getSecond();
            parent = dualAttachments.getFirst();

        } else {
            atts = attchs.iterator().next();
        }
        final Attachments attachments = atts;

        if (attachments != null) {
            LangkuikMultiFileBuffer buffer = new LangkuikMultiFileBuffer();

            final Upload upload = new Upload(buffer);
            upload.setMaxFileSize(2_000_000);
            upload.setMaxFiles(1); //upload 1 at a time
            upload.setAutoUpload(true);
            upload.setWidthFull();

            uploadLayout.add(spanErrorMsg);
            uploadLayout.add(upload);

            pojoTableFactory.createTable(
                    attachments,
                    "attachments",
                    "Attachment",
                    layout,
                    relationMementos,
                    uploadLayout,
                    e -> {
                    },
                    200);

            upload.addSucceededListener(event -> {

                try {
                    spanErrorMsg.setText("");
                    Optional<Attachment> oattachment = dao.createAndSave(Attachment.class, Optional.of(tenant), username, Optional.of(ext.getTranxId()));
                    Attachment attachment = oattachment.orElseThrow();
                    attachment.setFileName(event.getFileName());
                    attachment.setMimeType(event.getMIMEType());

                    Blob blob = new SerialBlob(IOUtils.toByteArray(buffer.getInputStream(event.getFileName())));
                    attachment.setBlobData(blob);
                    dao.saveAndAssociate(attachment, attachments, "attachments");

                    //relationMementos.get("attachments").getGrid().getDataProvider().refreshAll();
                    pojoTableFactory.redrawTables(attachments, "attachments", relationMementos.get("attachments"));
                    upload.getElement().setPropertyJson("files", Json.createArray()); //clear upload
                } catch (IOException | SQLException ex) {
                    Logger.getLogger(AttachmentRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }

            });

            upload.addFileRejectedListener(e -> {
                //e.getSource().
                spanErrorMsg.setText(e.getErrorMessage());
                //errorMsgs.add(e.getErrorMessage());
            });
            upload.addFailedListener(e -> {
                spanErrorMsg.add("Error uploading the file:" + e.getFileName());
            });
        }
        return Optional.of(parent);
    }

  

}
