/*
 * Copyright 2014 azrulhasni.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.azrul.langkuik.framework.webgui;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.azrul.langkuik.annotations.EntityUserMap;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.dao.EntityUtils;
import org.azrul.langkuik.dao.FindUsageQuery;
import org.azrul.langkuik.dao.HibernateGenericDAO;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.webgui.breadcrumb.BreadCrumbBuilder;
import org.azrul.langkuik.framework.webgui.breadcrumb.History;
import org.azrul.langkuik.security.role.EntityOperation;
import org.azrul.langkuik.security.role.SecurityUtils;

/**
 *
 * @author azrulhasni
 * @param <C>
 */
public class FindUsageView<C> extends VerticalView {

    private final DataAccessObject dao;
    private final PageParameter pageParameter;
    private final C currentBean;
    private final Map<String, FindUsageDataTable> dataTables;

    public FindUsageView(PageParameter pageParameter, C currentBean) {
        this.currentBean = currentBean;
        this.pageParameter = pageParameter;
        this.dao = new HibernateGenericDAO<>(pageParameter.getEntityManagerFactory(), (Class<C>) this.currentBean.getClass());
        dataTables = new HashMap<>();
    }

    @Override
    public void enter(final ViewChangeListener.ViewChangeEvent vcevent) {
        setCurrentView(vcevent.getViewName());
        //reset form
        this.removeAllComponents();

        final Set<String> currentUserRoles = SecurityUtils.getCurrentUserRoles();
        EntityOperation entityRight = determineEntityRight(currentBean.getClass(), currentUserRoles);
        if (entityRight == null) { //if entityRight=EntityRight.NONE, still allow to go through because field level might be accessible
            //Not accessible
            return;
        }

        //create bean utils
        final BeanUtils beanUtils = new BeanUtils();

        //rebuild pageParameter.getBreadcrumb()
        BreadCrumbBuilder.buildBreadCrumb(vcevent.getNavigator(),
                pageParameter.getBreadcrumb(),
                pageParameter.getHistory());

        //rebuild components
        if (currentBean == null) {
            return;
        }
        VerticalLayout layout = new VerticalLayout();
        Label title = new Label("<h2><u>"+pageParameter.getLocalisedText("view.findUsage.title",
                    beanUtils.getName(currentBean.getClass()))+"</u></h2>",
                ContentMode.HTML);
        layout.addComponent(title);

        List<Class<?>> entityClasses = EntityUtils.getAllEntities(pageParameter.getEntityManagerFactory());
        for (final Class<?> entityClass : entityClasses) {
            String label = entityClass.getSimpleName();
            WebEntity webEntity = entityClass.getAnnotation(WebEntity.class);
            if (webEntity == null) {
                continue;
            }
            FindUsageQuery query = new FindUsageQuery(pageParameter.getEntityManagerFactory(),
             currentBean,
             entityClass);
            EntityOperation usageEntityRight = SecurityUtils.getEntityRight(entityClass, currentUserRoles);
           
            if (dataTables.containsKey(entityClass.getCanonicalName())==false){
                FindUsageDataTable dataTable = new FindUsageDataTable();
                dataTable.createTablePanel(query, entityClass, dao, Integer.parseInt(pageParameter.getConfig().get("uploadCountLimit")), currentUserRoles, usageEntityRight, pageParameter, false, label);
                layout.addComponent(dataTable);
                dataTables.put(entityClass.getCanonicalName(),dataTable);
            
            }else{
                FindUsageDataTable dataTable = dataTables.get(entityClass.getCanonicalName());
                dataTable.refresh();
                layout.addComponent(dataTable);
                
            }
            if (dataTables.get(entityClass.getCanonicalName()).isEmpty() == false) {
                Button manageBtn = new Button(pageParameter.getLocalisedText("form.general.button.manage", ""),
                        new Button.ClickListener() {
                            @Override
                            public void buttonClick(
                                    Button.ClickEvent event) {
                                        Collection<C> currentBeans = (Collection<C>)dataTables.get(entityClass.getCanonicalName()).getTableValues();
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
                layout.addComponent(manageBtn);
                manageBtn.setId(manageBtn.getCaption());
            }
        }

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
        layout.addComponent(backBtn);
        this.addComponent(layout);
        backBtn.setId(backBtn.getCaption());
    }

    private EntityOperation determineEntityRight(final Class aclass,
            final Set<String> currentUserRoles) {
        //determine entity rights
        EntityOperation entityRight = null;
        EntityUserMap[] entityUserMaps = ((WebEntity) aclass.getAnnotation(WebEntity.class)).userMap();
        for (EntityUserMap e : entityUserMaps) {
            if (currentUserRoles.contains(e.role()) || ("*").equals(e.role())) {
                entityRight = e.right();
                break;
            }
        }
        return entityRight;
    }

}
