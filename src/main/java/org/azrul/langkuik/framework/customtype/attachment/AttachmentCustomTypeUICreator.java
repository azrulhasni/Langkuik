/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.customtype.attachment;

import com.vaadin.event.LayoutEvents;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.MultiFileUpload;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadFinishedHandler;
import com.wcs.wcslib.vaadin.widget.multifileupload.ui.UploadStateWindow;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.dao.EntityUtils;
import org.azrul.langkuik.dao.FindRelationParameter;
import org.azrul.langkuik.dao.FindRelationQuery;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.customtype.CustomType;
import org.azrul.langkuik.framework.customtype.CustomTypeUICreator;
import org.azrul.langkuik.framework.webgui.BeanView;

import org.azrul.langkuik.framework.webgui.WebEntityItemContainer;
import org.azrul.langkuik.security.role.FieldState;
import org.azrul.langkuik.security.role.SecurityUtils;

/**
 *
 * @author azrulm
 * @param <P>
 */
public class AttachmentCustomTypeUICreator<P> implements CustomTypeUICreator<P> {

    @Override
    public Component createUIForForm(final P currentBean,
            final Class<? extends CustomType> attachmentClass,
            final String pojoFieldName,
            final BeanView beanView,
            final DataAccessObject<P> conatainerClassDao,
            final DataAccessObject<? extends CustomType> customTypeDao,
            final PageParameter pageParameter,
            final FieldState fieldState,
            final Window window) {
        final FormLayout form = new FormLayout();

        final DataAccessObject<AttachmentCustomType> attachmentDao = ((DataAccessObject<AttachmentCustomType>) customTypeDao);
        FindRelationParameter<P, AttachmentCustomType> findRelationParameter = new FindRelationParameter(
                currentBean, pojoFieldName, null, attachmentClass
        );
        FindRelationQuery<P, AttachmentCustomType> query = new FindRelationQuery(findRelationParameter);
        final Collection<AttachmentCustomType> attachments = attachmentDao.runQuery(query, null, true, 0, Integer.parseInt(pageParameter.getConfig().get("uploadCountLimit")),SecurityUtils.getCurrentTenant());
        //final Collection<AttachmentCustomType> attachments = attachmentDao.find(currentBean, pojoFieldName, null, true, 0, Integer.parseInt(pageParameter.getConfig().get("uploadCountLimit")));

        final WebEntityItemContainer attachmentIC = new WebEntityItemContainer(attachmentClass);
        if (!attachments.isEmpty()) {
            attachmentIC.addAll(attachments);
        }
        final ListSelect attachmentList = new ListSelect("", attachmentIC);
        createAtachmentList(attachmentList, attachments, pageParameter, beanView, form);

        final String relativePath = currentBean.getClass().getCanonicalName()
                + File.separator
                + EntityUtils.getIdentifierValue(currentBean, pageParameter.getEntityManagerFactory());
        final String fullPath = pageParameter.getConfig().get("attachmentRepository")
                + File.separator + relativePath;
        MyUploadHandler<P> uploadFinishHandler = new MyUploadHandler<>(currentBean,
                Integer.parseInt(pageParameter.getConfig().get("uploadCountLimit").trim()),
                pojoFieldName,
                fullPath,
                relativePath,
                attachmentList,
                attachmentIC,
                customTypeDao,
                attachmentClass,
                pageParameter);

        //File upload/download/delete buttons
        HorizontalLayout attachmentButtons = new HorizontalLayout();
        attachmentButtons.setSpacing(true);

        UploadStateWindow uploadStateWindow = new UploadStateWindow();
        MultiFileUpload fileUpload = new MultiFileUpload(uploadFinishHandler, uploadStateWindow);
        uploadFinishHandler.setFileUpload(fileUpload);

        fileUpload.getSmartUpload().setEnabled(true);
        fileUpload.getSmartUpload().setMaxFileSize(Integer.parseInt(pageParameter.getConfig().get("uploadSizeLimit").trim()));
        fileUpload.getSmartUpload().setUploadButtonCaptions(pageParameter.getLocalisedText("fileUpload.button.upload"), pageParameter.getLocalisedText("fileUpload.button.upload"));
        fileUpload.getSmartUpload().setId(pageParameter.getLocalisedText("fileUpload.button.upload"));
        fileUpload.setId(pageParameter.getLocalisedText("fileUpload.button.upload") + "2");

        Button deleteAttachmentBtn = new Button(pageParameter.getLocalisedText("fileUpload.button.delete"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Collection<AttachmentCustomType> attachments = (Collection<AttachmentCustomType>) attachmentList.getValue();
                if (!attachments.isEmpty()) {
                    customTypeDao.unlinkAndDelete(attachments, currentBean, pojoFieldName, pageParameter.getRelationManagerFactory().create(currentBean.getClass(), attachmentClass));
                    //Collection<AttachmentCustomType> a = attachmentDao.find(currentBean, pojoFieldName, null, true, 0, Integer.parseInt(pageParameter.getConfig().get("uploadCountLimit")));
                    FindRelationParameter<P, AttachmentCustomType> findRelationParameter = new FindRelationParameter(
                            currentBean, pojoFieldName, null, attachmentClass
                    );
                    FindRelationQuery<P, AttachmentCustomType> query = new FindRelationQuery(findRelationParameter);
                    Collection<AttachmentCustomType> a = attachmentDao.runQuery(query, null, true, 0, Integer.parseInt(pageParameter.getConfig().get("uploadCountLimit")),SecurityUtils.getCurrentTenant());

                    attachmentIC.removeAllItems();
                    attachmentIC.addAll(a);
                    attachmentIC.refreshItems();
                }
            }
        });
        deleteAttachmentBtn.setId(deleteAttachmentBtn.getCaption());

        Button downloadAttachmentBtn = new Button(pageParameter.getLocalisedText("fileUpload.button.download"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                Collection<AttachmentCustomType> attachments = (Collection<AttachmentCustomType>) attachmentList.getValue();
                if (!attachments.isEmpty()) {
                    AttachmentCustomType attachment = attachments.iterator().next();
                    final String fullPath = pageParameter.getConfig().get("attachmentRepository")
                            + File.separator
                            + attachment.getRelativeLocation()
                            + File.separator
                            + attachment.getFileName();
                    FileResource res = new FileResource(new File(fullPath));
                    beanView.setViewResource("DOWNLOAD", res);
                    ResourceReference rr = ResourceReference.create(res, beanView, "DOWNLOAD");
                    Page.getCurrent().open(rr.getURL(), null);
                }
            }
        });
        downloadAttachmentBtn.setId(downloadAttachmentBtn.getCaption());

        Button closeWindowBtn = new Button(pageParameter.getLocalisedText("dialog.general.button.close"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                //beanView.setCurrentBean(currentBean);
                window.close();
            }
        });
        closeWindowBtn.setId(closeWindowBtn.getCaption());

        if (fieldState.equals(FieldState.EDITABLE)) {
            attachmentButtons.addComponent(fileUpload);
            attachmentButtons.addComponent(deleteAttachmentBtn);
        }

        if (fieldState.equals(FieldState.EDITABLE) || fieldState.equals(FieldState.READ_ONLY)) {
            attachmentButtons.addComponent(downloadAttachmentBtn);
        }

        attachmentButtons.addComponent(closeWindowBtn);

        form.addComponent(attachmentButtons);
        form.setMargin(true);
        //beanView.addComponent(form);
        return form;
    }

    private void createAtachmentList(final ListSelect attachmentList,
            final Collection<? extends CustomType> attachments,
            final PageParameter pageParameter,
            final BeanView view,
            final FormLayout form) {

        attachmentList.setHeight(attachments.size() + 2, Sizeable.Unit.EM);
        attachmentList.setMultiSelect(true);
        VerticalLayout attachmentListCont = new VerticalLayout();
        attachmentListCont.setCaption(pageParameter.getLocalisedText("attachment.caption"));
        attachmentListCont.addComponent(attachmentList);
        attachmentListCont.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {

            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getClickedComponent() == null) {
                    return;
                }
                if (event.getClickedComponent().equals(attachmentList)) {
                    Collection<AttachmentCustomType> attachments = (Collection<AttachmentCustomType>) attachmentList.getValue();
                    if (!attachments.isEmpty()) {
                        AttachmentCustomType attachment = attachments.iterator().next();
                        final String fullPath = pageParameter.getConfig().get("attachmentRepository")
                                + File.separator
                                + attachment.getRelativeLocation()
                                + File.separator
                                + attachment.getFileName();
                        FileResource res = new FileResource(new File(fullPath));
                        view.setViewResource("DOWNLOAD", res);
                        ResourceReference rr = ResourceReference.create(res, view, "DOWNLOAD");

                        Page.getCurrent().open(rr.getURL(), null);
                    }
                }
            }
        });
        form.addComponent(attachmentListCont);
    }

//    @Override
//    public Component createUIForTableRow(C currentBean,
//            Class<? extends CustomType> attachmentClass,
//            String pojoFieldName, BeanView view,
//            DataAccessObject<C> containerClassDao,
//            DataAccessObject<? extends CustomType> customTypeDao,
//            RelationManagerFactory relationManagerFactory,
//            Configuration config,
//            ComponentState componentState) {
//        final FormLayout form = new FormLayout();
//        final DataAccessObject<AttachmentCustomType> attachmentDao = ((DataAccessObject<AttachmentCustomType>) customTypeDao);
//        final Collection<AttachmentCustomType> attachments = attachmentDao.find(currentBean, pojoFieldName, null, true, 0, Integer.parseInt(config.get("uploadCountLimit")));
//
//        final WebEntityItemContainer attachmentIC = new WebEntityItemContainer(attachmentClass);
//        if (!attachments.isEmpty()) {
//            attachmentIC.addAll(attachments);
//        }
//        final ListSelect attachmentList = new ListSelect("", attachmentIC);
//        createAtachmentList(attachmentList, attachments, config, view, form);
//        return form;
//    }
}

class MyUploadHandler<P> implements UploadFinishedHandler {

    private int c;
    private final int limit;
    private final String fullPath;
    private final String relativePath;
    private final WebEntityItemContainer attachmentIC;
    private final Class attachmentClass;
    private final String fieldName;
    private final DataAccessObject customTypeDao;
    private final P currentBean;
    private MultiFileUpload fileUpload = null;
    private final ListSelect attachmentList;
    private PageParameter pageParameter = null;

    public void setFileUpload(MultiFileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }

    public MyUploadHandler(P currentBean,
            int limit,
            String fieldName,
            String fullPath,
            String relativePath,
            ListSelect attachmentList,
            WebEntityItemContainer attachmentIC,
            DataAccessObject customTypeDao,
            Class attachmentClass,
            PageParameter pageParameter) {
        this.currentBean = currentBean;
        this.limit = limit;
        this.fieldName = fieldName;
        this.fullPath = fullPath;
        this.relativePath = relativePath;
        this.attachmentIC = attachmentIC;
        this.customTypeDao = customTypeDao;
        this.attachmentClass = attachmentClass;
        this.attachmentList = attachmentList;
        this.pageParameter = pageParameter;

    }

    @Override
    public void handleFile(InputStream input, String fileName, String mimeType, long length) {
        c++;
        if (c > limit) {
            if (fileUpload != null) {
                fileUpload.setInterruptedMsg(pageParameter.getLocalisedText("attachment.limit.numberOfFiles.message", limit));
                fileUpload.interruptAll();
            }
            return;
        }
        byte[] buffer = new byte[8 * 1024];
        File attachmentFile = new File(fullPath);
        attachmentFile.mkdirs();
        try (OutputStream output = new FileOutputStream(fullPath + File.separator + fileName)) {
            int bytesRead;
            AttachmentCustomType attachment = (AttachmentCustomType) customTypeDao.createAndSave(attachmentClass,SecurityUtils.getCurrentTenant());

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            attachment.setCreationDate(new Date());
            attachment.setFileName(fileName);
            attachment.setRelativeLocation(relativePath);
            attachment.setMimeType(mimeType);
            customTypeDao.saveWithRelation(attachment, currentBean, fieldName, pageParameter.getRelationManagerFactory().create(currentBean.getClass(), attachmentClass));
            //Collection<AttachmentCustomType> attachments = customTypeDao.find(currentBean, fieldName, null, true, 0, limit);
            FindRelationParameter<P, AttachmentCustomType> findRelationParameter = new FindRelationParameter(
                    currentBean, fieldName, null, attachmentClass
            );
            FindRelationQuery<P, AttachmentCustomType> query = new FindRelationQuery(findRelationParameter);
            final Collection<AttachmentCustomType> attachments = customTypeDao.runQuery(query, null, true, 0, Integer.parseInt(pageParameter.getConfig().get("uploadCountLimit")),SecurityUtils.getCurrentTenant());

            if (!attachments.isEmpty()) {
                attachmentIC.addAll(attachments);
                attachmentIC.refreshItems();
            }
            attachmentList.setHeight(attachmentList.size() + 1, Sizeable.Unit.EM);
        } catch (IOException ex) {
            Logger.getLogger(AttachmentCustomTypeUICreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("upload counter:" + c);
    }
}
