/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.webgui;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.dao.FindAnyEntityQuery;
import org.azrul.langkuik.dao.HibernateGenericDAO;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.exception.EntityIsUsedException;
import org.azrul.langkuik.framework.webgui.breadcrumb.BreadCrumbBuilder;
import org.azrul.langkuik.framework.webgui.breadcrumb.History;
import org.azrul.langkuik.security.role.EntityOperation;
import org.azrul.langkuik.security.role.SecurityUtils;
import org.vaadin.dialogs.ConfirmDialog;

/**
 *
 * @author azrulm
 * @param <C>
 */
public class SearchResultView<C> extends VerticalView {

    private final Class<C> classOfBean;
    private final int noBeansPerPage;
    private final DataAccessObject<C> dao;
    private final PageParameter pageParameter;
    private SearchResultDataTable<C> dataTable;

    public SearchResultView(Class<C> classOfBean, PageParameter pageParameter) {
        this(classOfBean, 3, pageParameter);
    }

    public SearchResultView(Class<C> classOfBean, int noBeansPerPage,
            PageParameter pageParameter) {
        this.classOfBean = classOfBean;
        this.noBeansPerPage = noBeansPerPage;
        this.pageParameter = pageParameter;
        this.dataTable = null;

        this.dao = new HibernateGenericDAO<>(pageParameter.getEntityManagerFactory(), classOfBean);
    }

    @Override
    public void enter(final ViewChangeListener.ViewChangeEvent vcevent) {
        setCurrentView(vcevent.getViewName());
        this.removeAllComponents();

        BeanUtils beanUtils = new BeanUtils();
        
        //determine user details
        Set<String> currentUserRoles = SecurityUtils.getCurrentUserRoles();
         

        //determine entity rights 
        EntityOperation entityRight = SecurityUtils.getEntityRight(classOfBean, currentUserRoles);
        if (entityRight == null) { //if entityRight=EntityRight.NONE, still allow to go through because field level might be accessible
            //Not accessible
            return;
        }
        
        

        //Build bread crumb
        BreadCrumbBuilder.buildBreadCrumb(vcevent.getNavigator(),
                pageParameter.getBreadcrumb(),
                pageParameter.getHistory());
        FindAnyEntityQuery<C> searchQuery = new FindAnyEntityQuery<>(classOfBean);
        
        //set form
        FormLayout form = new FormLayout();
        
        //add title
        Label title = new Label("<h2><u>"+pageParameter.getLocalisedText("view.searchresult.title",beanUtils.getName(classOfBean))+"</u></h2>",ContentMode.HTML);
        form.addComponent(title);
        
        //create table
        if (dataTable==null){
            //in case of new Object (from menu)
            dataTable = new SearchResultDataTable<>(searchQuery,
                classOfBean,
                dao,
                noBeansPerPage,
                currentUserRoles,
                entityRight,
                pageParameter);
            form.addComponent(dataTable);
        }else{
            //in case of coming back from 'Send Back' button or from Breadcrumb 
            dataTable.refresh();
            form.addComponent(dataTable);
        }

        //Handle navigations and actions
        HorizontalLayout buttonLayout = new HorizontalLayout();

//        Button addNewBtn = new Button("Add new",
//                new Button.ClickListener() {
//                    @Override
//                    public void buttonClick(Button.ClickEvent event
//                    ) {
//                        C currentBean = dao.createAndSave();
//                        BeanView<Object, C> beanView = new BeanView<Object, C>(currentBean,null, pageParameter.getRelationManagerFactory(), pageParameter.getEntityManagerFactory(), pageParameter.getHistory(), pageParameter.getBreadcrumb(), pageParameter.getConfig(), pageParameter.getCustomTypeDaos());
//                        String targetView = "CHOOSE_ONE_TABLE_VIEW_" + UUID.randomUUID().toString();
//                        WebEntity myObject = (WebEntity) currentBean.getClass().getAnnotation(WebEntity.class);
//                        History his = new History(targetView, "Add new " + myObject.name());
//                        pageParameter.getHistory().push(his);
//                        vcevent.getNavigator().addView(targetView, beanView);
//                        vcevent.getNavigator().navigateTo(targetView);
//
//                    }
//                });
//        buttonLayout.addComponent(addNewBtn);
//        addNewBtn.setId(addNewBtn.getCaption());
        Button manageBtn = new Button(pageParameter.getLocalisedText("form.general.button.manage", ""),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        Collection<C> currentBeans = (Collection<C>) dataTable.getTableValues();
                        if (!currentBeans.isEmpty()) {
                            C currentBean = currentBeans.iterator().next();
                            if (currentBean != null) {
                                BeanView<Object, C> beanView = new BeanView<>(currentBean, null, null, pageParameter);
                                String targetView = "CHOOSE_ONE_TABLE_VIEW_" + UUID.randomUUID().toString();
                                WebEntity myObject = (WebEntity) currentBean.getClass().getAnnotation(WebEntity.class);
                                History his = new History(targetView, pageParameter.getLocalisedText("form.general.button.manage", myObject.name()));
                                pageParameter.getHistory().push(his);
                                vcevent.getNavigator().addView(targetView, beanView);
                                vcevent.getNavigator().navigateTo(targetView);
                            }
                        }

                    }
                });
        buttonLayout.addComponent(manageBtn);
        manageBtn.setId(manageBtn.getCaption());

        Button deleteBtn = new Button(pageParameter.getLocalisedText("form.general.button.delete"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        final Collection<C> currentBeans = (Collection<C>) dataTable.getTableValues();
                        if (!currentBeans.isEmpty()) {
                            ConfirmDialog.show(SearchResultView.this.getUI(), pageParameter.getLocalisedText("dialog.delete.header"), pageParameter.getLocalisedText("dialog.delete.confirmText"),
                                    pageParameter.getLocalisedText("dialog.delete.button.ok"), pageParameter.getLocalisedText("dialog.delete.button.cancel"), new ConfirmDialog.Listener() {

                                public void onClose(ConfirmDialog dialog) {
                                    if (dialog.isConfirmed()) {
                                        try{
                                            dataTable.deleteEntities(currentBeans);
                                        }catch (EntityIsUsedException e){
                                             ConfirmDialog.show(SearchResultView.this.getUI(),pageParameter.getLocalisedText("dialog.delete.CannotDeleteBeingUsed"),null);
                                        }
                                    }
                                }
                            });
                        }

                    }
                });
        buttonLayout.addComponent(deleteBtn);
        deleteBtn.setId(deleteBtn.getCaption());

        buttonLayout.setSpacing(true);
        form.addComponent(buttonLayout);

        Button backBtn = new Button(pageParameter.getLocalisedText("form.general.button.back"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        if (!pageParameter.getHistory().isEmpty()) {
                            String currentView = pageParameter.getHistory().pop().getViewHandle();
                            String lastView = pageParameter.getHistory().peek().getViewHandle();
                            vcevent.getNavigator().removeView(currentView);
                            vcevent.getNavigator().navigateTo(lastView);
                        }
                    }
                });
        form.addComponent(backBtn);
        backBtn.setId(backBtn.getCaption());
        this.addComponent(form);
    }
}