/*
 To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.webgui;

import org.azrul.langkuik.dao.DataAccessObject;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.dao.HibernateGenericDAO;
import org.azrul.langkuik.framework.webgui.breadcrumb.BreadCrumbBuilder;
import org.azrul.langkuik.dao.FindAnyEntityQuery;
import org.azrul.langkuik.dao.FindRelationParameter;
import org.azrul.langkuik.dao.FindRelationQuery;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.webgui.breadcrumb.History;
import org.azrul.langkuik.security.role.RelationState;
import org.azrul.langkuik.security.role.EntityRight;
import org.azrul.langkuik.security.role.UserSecurityUtils;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author azrulm
 * @param <P>
 * @param <C>
 */
public class RelationView<P, C> extends VerticalView {

    private P parentBean;
    private String parentToBeanField;
    private RelationState parentToBeanRelationState;
    private final Class<C> classOfBean;
    private final int noBeansPerPage;
    private final DataAccessObject<C> dao;
    private ChoiceType choiceType;
    private PageParameter pageParameter;
    //private SearchResultDataTable<C> allDataTableLayout;
    private RelationDataTable<P, C> relationTableLayout;

    public RelationView(P parentBean,
            String parentToBeanField,
            RelationState parentToBeanFieldState,
            Class<C> classOfBean,
            ChoiceType choiceType,
            PageParameter pageParameter) {
        this(parentBean,
                parentToBeanField,
                parentToBeanFieldState,
                classOfBean,
                choiceType,
                3,
                pageParameter);
    }

    public RelationView(P parentBean,
            String parentToBeanField,
            RelationState parentToBeanFieldState,
            Class<C> classOfBean,
            ChoiceType choiceType,
            int noBeansPerPage,
            PageParameter pageParameter) {
        this.parentBean = parentBean;
        this.parentToBeanField = parentToBeanField;
        this.classOfBean = classOfBean;
        this.noBeansPerPage = noBeansPerPage;
        this.dao = new HibernateGenericDAO<>(pageParameter.getEntityManagerFactory(), classOfBean);

        this.choiceType = choiceType;
        this.parentToBeanRelationState = parentToBeanFieldState;
        this.pageParameter = pageParameter;
        //this.allDataTableLayout = null;
        this.relationTableLayout = null;
    }

    public void enter(final ViewChangeListener.ViewChangeEvent vcevent) {
        setCurrentView(vcevent.getViewName());
        this.removeAllComponents();

        //get user roles
        //final Set<String> currentUserRoles = UserSecurityUtils.getCurrentUserRoles();

        //determine entity rights 
        final EntityRight entityRight = UserSecurityUtils.getEntityRight(classOfBean/*, currentUserRoles*/);
        if (entityRight == null) { //if entityRight=EntityRight.NONE, still allow to go through because field level might be accessible
            //Not accessible
            return;
        }

        //create bean utils
        BeanUtils beanUtils = new BeanUtils();

        //creat bread crumb
        BreadCrumbBuilder.buildBreadCrumb(vcevent.getNavigator(),
                pageParameter.getBreadcrumb(),
                pageParameter.getHistory());

        //create form
        final FormLayout form = new FormLayout();
        Label title = new Label("<h2><u>" + pageParameter.getLocalisedText("view.relation.title",
                beanUtils.getName(classOfBean),
                beanUtils.getName(parentBean.getClass())) + "</u></h2>",
                ContentMode.HTML);
        form.addComponent(title);

        //Do query and build table  
        final FindAnyEntityQuery<C> searchQuery = new FindAnyEntityQuery<>(
                classOfBean);
        FindRelationParameter<P, C> findRelationParameter = new FindRelationParameter<>(
                parentBean,
                parentToBeanField,
                pageParameter.getRelationManagerFactory().create((Class<P>) parentBean.getClass(), classOfBean),
                classOfBean);
        FindRelationQuery<P, C> entityCollectionQuery = new FindRelationQuery<>(
                findRelationParameter
        );

        // pageParameter.getRelationManagerFactory().create((Class<P>) parentBean.getClass(), classOfBean)
        //create table
        //create table
        if (relationTableLayout == null) {
            //in case of new Object (from menu)
            relationTableLayout = new RelationDataTable<>(
                    entityCollectionQuery,
                    findRelationParameter,
                    classOfBean,
                    dao,
                    noBeansPerPage,
                    //currentUserRoles,
                    entityRight,
                    pageParameter);

            form.addComponent(relationTableLayout);
        } else {
            //in case of coming back from 'Send Back' button or from Breadcrumb 
            relationTableLayout.refresh();
            form.addComponent(relationTableLayout);
        }

        if (parentToBeanRelationState.equals(RelationState.EDIT_RELATION)) {
            Button associateExistingBtn = new Button(pageParameter.getLocalisedText("form.general.button.associate"), new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    final SearchResultDataTable<C> allDataTableLayout = new SearchResultDataTable<>(searchQuery,
                                classOfBean,
                                dao,
                                noBeansPerPage,
                                //currentUserRoles,
                                entityRight,
                                pageParameter);
                        form.addComponent(allDataTableLayout);
                    

                    //handle associate existing data
                    final Window associateChildrenWindow = new Window(pageParameter.getLocalisedText("form.associate.window"));
                    associateChildrenWindow.setId(associateChildrenWindow.getCaption());
                    associateChildrenWindow.setContent(allDataTableLayout);
                    associateChildrenWindow.setModal(true);

                    HorizontalLayout popupButtonLayout = new HorizontalLayout();

                    Button associateToCurrentBtn = new Button(pageParameter.getLocalisedText("form.associate.button.associate"), new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            Collection<C> allDataList = allDataTableLayout.getTableValues();
                            relationTableLayout.associateEntities(allDataList, choiceType);
                            associateChildrenWindow.close();
                        }
                    });
                    associateToCurrentBtn.setId(associateToCurrentBtn.getCaption());
                    popupButtonLayout.addComponent(associateToCurrentBtn);

                    Button closeBtn = new Button(pageParameter.getLocalisedText("dialog.general.button.close"), new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            associateChildrenWindow.close();
                        }
                    });
                    popupButtonLayout.addComponent(closeBtn);
                    closeBtn.setId(closeBtn.getCaption());

                    popupButtonLayout.setSpacing(true);
                    MarginInfo marginInfo = new MarginInfo(true);

                    allDataTableLayout.setMargin(marginInfo);
                    allDataTableLayout.addComponent(popupButtonLayout);
                    RelationView.this.getUI().addWindow(associateChildrenWindow);
                }
            });
            form.addComponent(associateExistingBtn);
            associateExistingBtn.setId(associateExistingBtn.getCaption());
            Button dissociateExistingBtn = new Button(pageParameter.getLocalisedText("form.general.button.dissociate"), new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    final Collection<C> currentBeans = (Collection<C>) relationTableLayout.getTableValues();
                    if (!currentBeans.isEmpty()) {
                        ConfirmDialog.show(RelationView.this.getUI(), pageParameter.getLocalisedText("dialog.dissociate.header"), pageParameter.getLocalisedText("dialog.delete.confirmText"),
                                pageParameter.getLocalisedText("dialog.dissociate.button.ok"), pageParameter.getLocalisedText("dialog.delete.button.cancel"), new ConfirmDialog.Listener() {
                                    @Override
                                    public void onClose(ConfirmDialog dialog) {
                                        if (dialog.isConfirmed()) {
                                            RelationView.this.parentBean = relationTableLayout.dissociateEntities(currentBeans);
                                        }
                                    }
                                });
                    }
                }
            });
            form.addComponent(dissociateExistingBtn);
            dissociateExistingBtn.setId(dissociateExistingBtn.getCaption());
        }
        form.addComponent(relationTableLayout);

        //Navigation and actions
        HorizontalLayout buttonLayout = new HorizontalLayout();
        if (parentToBeanRelationState.equals(RelationState.CREATE_ADD_DELETE_CHILDREN) 
                || parentToBeanRelationState.equals(RelationState.CREATE_ADD_CHILDREN)) {

            if (beanUtils.isCreatable(classOfBean/*, currentUserRoles*/)) {
                Button addNewBtn = new Button(pageParameter.getLocalisedText("form.general.button.addNew"),
                        new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent event
                            ) {
                                C currentBean = dao.createNew(UserSecurityUtils.getCurrentTenant());//dao.createAndSave(parentBean, parentToBeanField, pageParameter.getRelationManagerFactory().create((Class<P>) parentBean.getClass(), classOfBean));
                                BeanView<P, C> beanView = new BeanView<P, C>(currentBean, parentBean, parentToBeanField, pageParameter);
                                String targetView = "CHOOSE_ONE_TABLE_VIEW_" + UUID.randomUUID().toString();
                                WebEntity myObject = (WebEntity) currentBean.getClass().getAnnotation(WebEntity.class);
                                History his = new History(targetView, pageParameter.getLocalisedText("history.button.addNewObject", myObject.name()));
                                pageParameter.getHistory().push(his);
                                vcevent.getNavigator().addView(targetView, beanView);
                                vcevent.getNavigator().navigateTo(targetView);

                            }
                        });
                buttonLayout.addComponent(addNewBtn);
                addNewBtn.setId(addNewBtn.getCaption());
            }
        }

        if (parentToBeanRelationState.equals(RelationState.EDIT_CHILDREN)
                || parentToBeanRelationState.equals(RelationState.CREATE_ADD_DELETE_CHILDREN)
                || parentToBeanRelationState.equals(RelationState.CREATE_ADD_CHILDREN)) {

            if (beanUtils.isEditable(classOfBean/*, currentUserRoles*/) || beanUtils.isViewable(classOfBean/*, currentUserRoles*/)) {
                Button manageBtn = new Button(pageParameter.getLocalisedText("form.general.button.manage", ""),
                        new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent event) {
                                C currentBean = relationTableLayout.getTableValues().iterator().next();
                                if (currentBean != null) {
                                    BeanView<P, C> beanView = new BeanView<P, C>(currentBean, parentBean, parentToBeanField, pageParameter);
                                    String targetView = "CHOOSE_ONE_TABLE_VIEW_" + UUID.randomUUID().toString();
                                    WebEntity myObject = (WebEntity) currentBean.getClass().getAnnotation(WebEntity.class);
                                    History his = new History(targetView, pageParameter.getLocalisedText("form.general.button.manage", myObject.name()));
                                    pageParameter.getHistory().push(his);
                                    vcevent.getNavigator().addView(targetView, beanView);
                                    vcevent.getNavigator().navigateTo(targetView);
                                }
                            }
                        });
                buttonLayout.addComponent(manageBtn);
                manageBtn.setId(manageBtn.getCaption());
            }
        }

        if (parentToBeanRelationState.equals(RelationState.CREATE_ADD_DELETE_CHILDREN)
                || parentToBeanRelationState.equals(RelationState.DELETE_CHILDREN)) {

            if (beanUtils.isCreatable(classOfBean/*, currentUserRoles*/)) {
                Button deleteBtn = new Button(pageParameter.getLocalisedText("form.general.button.delete"),
                        new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent event) {
                                final Collection<C> currentBeans = (Collection<C>) relationTableLayout.getTableValues();
                                if (!currentBeans.isEmpty()) {
                                    ConfirmDialog.show(RelationView.this.getUI(), pageParameter.getLocalisedText("dialog.delete.header"), pageParameter.getLocalisedText("dialog.delete.confirmText"),
                                            pageParameter.getLocalisedText("dialog.delete.button.ok"), pageParameter.getLocalisedText("dialog.delete.button.cancel"), new ConfirmDialog.Listener() {
                                        @Override
                                        public void onClose(ConfirmDialog dialog) {
                                            if (dialog.isConfirmed()) {
                                                RelationView.this.parentBean = relationTableLayout.deleteEntities(currentBeans);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                buttonLayout.addComponent(deleteBtn);
                deleteBtn.setId(deleteBtn.getCaption());
            }
        }

        Button saveAndBackBtn = new Button(pageParameter.getLocalisedText("form.general.button.saveAndBack"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        //parentDao.save(parentBean);
                        if (!pageParameter.getHistory().isEmpty()) {
                            String currentView = pageParameter.getHistory().pop().getViewHandle();
                            String lastView = pageParameter.getHistory().peek().getViewHandle();
                            vcevent.getNavigator().removeView(currentView);
                            vcevent.getNavigator().navigateTo(lastView);
                        }
                    }
                });
        buttonLayout.addComponent(saveAndBackBtn);
        saveAndBackBtn.setId(saveAndBackBtn.getCaption());

        buttonLayout.setSpacing(true);
        form.addComponent(buttonLayout);
        this.addComponent(form);
    }
}
