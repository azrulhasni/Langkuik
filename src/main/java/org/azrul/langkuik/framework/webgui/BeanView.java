/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.webgui;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.converter.AbstractStringToNumberConverter;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.UserError;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.azrul.langkuik.annotations.Choice;
import org.azrul.langkuik.annotations.EntityUserMap;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.dao.HibernateGenericDAO;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.activechoice.ActiveChoiceEnum;
import org.azrul.langkuik.framework.activechoice.ActiveChoiceTarget;
import org.azrul.langkuik.framework.activechoice.ActiveChoiceUtils;
import org.azrul.langkuik.framework.activechoice.EmptyEnum;
import org.azrul.langkuik.framework.customtype.CustomType;
import org.azrul.langkuik.framework.customtype.attachment.AttachmentCustomTypeUICreator;
import org.azrul.langkuik.framework.webgui.breadcrumb.BreadCrumbBuilder;
import org.azrul.langkuik.framework.webgui.breadcrumb.History;
import org.azrul.langkuik.security.role.FieldState;
import org.azrul.langkuik.security.role.EntityOperation;
import org.azrul.langkuik.security.role.RelationState;
import org.azrul.langkuik.dao.EntityUtils;
import org.azrul.langkuik.framework.audit.AuditedEntity;
import org.azrul.langkuik.framework.audit.AuditedField;
import org.azrul.langkuik.security.role.SecurityUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;

public class BeanView<P, C> extends VerticalView {

    private C currentBean;
    private final P parentBean;
    private final String parentToCurrentBeanField;
    private final DataAccessObject<C> dao;
    private final PageParameter pageParameter;
    private int tabPage;

    public BeanView(C currentBean, P parentBean, String parentToCurrentBeanField,
            PageParameter pageParameter) {
        this.currentBean = currentBean;
        this.pageParameter = pageParameter;
        this.dao = new HibernateGenericDAO<>(pageParameter.getEntityManagerFactory(), (Class<C>) currentBean.getClass());
        this.parentBean = parentBean;
        this.parentToCurrentBeanField = parentToCurrentBeanField;
        this.tabPage = 0;
        // this.parentBean = parentBean;
    }

    public void setViewResource(String key, Resource resource) {
        this.setResource(key, resource);

    }

    public Resource getViewResource(String key) {
        return this.getResource(key);
    }
    
    public void setCurrentBean(C currentBean){
        this.currentBean = currentBean;
    }

    @Override
    public void enter(final ViewChangeListener.ViewChangeEvent vcevent) {
        int tempPageNo = tabPage; //save page no
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

        //refresh current item
        C newBean = dao.refresh(currentBean);
        if (newBean != null) {
            currentBean = newBean;
        }

        final BeanFieldGroup fieldGroup = new BeanFieldGroup(currentBean.getClass());
        fieldGroup.setItemDataSource(currentBean);
        final FormLayout form = new FormLayout();
        //add title
        Label title = new Label("<h2><u>" + pageParameter.getLocalisedText("view.bean.title",
                beanUtils.getName(currentBean.getClass())) + "</u></h2>",
                ContentMode.HTML);
        form.addComponent(title);

        //draw form
        Map<String, Map<Integer, FieldContainer>> groups = beanUtils.createGroupsFromBean(currentBean.getClass());
        //Map<Integer, RelationContainer> relations = beanUtils.getOrderedRelationsByRank(currentBean.getClass());
        //render form according to tab
        if (groups.size() == 1) {
            createForm(entityRight,
                    currentUserRoles,
                    groups,
                    fieldGroup,
                    //relations,
                    pageParameter.getCustomTypeDaos(),
                    vcevent.getNavigator(),
                    form);
        } else {
            TabSheet tabSheet = new TabSheet();
           
            tabSheet.addListener(new  TabSheet.SelectedTabChangeListener(){
                @Override
                public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                   FormLayout currentComponent = (FormLayout) event.getTabSheet().getSelectedTab(); 
                   tabPage =  event.getTabSheet().getTabPosition(event.getTabSheet().getTab(currentComponent));
                }
            });
            for (String group : groups.keySet()) {
                if (("All").equals(group)) {
                    createForm(entityRight,
                            currentUserRoles,
                            groups,
                            group,
                            fieldGroup,
                            //relations,
                            pageParameter.getCustomTypeDaos(),
                            vcevent.getNavigator(),
                            form);
                } else {
                    FormLayout tab = new FormLayout();
                    createForm(entityRight,
                            currentUserRoles,
                            groups,
                            group,
                            fieldGroup,
                            //relations,
                            pageParameter.getCustomTypeDaos(),
                            vcevent.getNavigator(),
                            tab);
                    tabSheet.addTab(tab, group);

                }
            }
            tabSheet.setSelectedTab(tempPageNo);
            form.addComponent(tabSheet);
        }

        //Navigation and actions
        HorizontalLayout navButtons = new HorizontalLayout();
        navButtons.setSpacing(true);

        Button saveAndBackBtn = new Button(pageParameter.getLocalisedText("form.general.button.saveAndBack"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        try {
                            fieldGroup.commit();
                            currentBean = (C) fieldGroup.getItemDataSource().getBean();
                            currentBean = saveBean(currentBean, parentBean, beanUtils, currentUserRoles);
                            if (!pageParameter.getHistory().isEmpty()) {
                                String currentView = pageParameter.getHistory().pop().getViewHandle();
                                String lastView = pageParameter.getHistory().peek().getViewHandle();
                                vcevent.getNavigator().removeView(currentView);
                                vcevent.getNavigator().navigateTo(lastView);
                            }
                        } catch (FieldGroup.CommitException ex) {
                            handleFieldsError(fieldGroup);
                        }
                    }

                });
        navButtons.addComponent(saveAndBackBtn);
        saveAndBackBtn.setId(saveAndBackBtn.getCaption());

        //find usage button
        if (currentBean != null) {
            if (EntityUtils.isClassRoot(currentBean.getClass())) {
                Button findUsageBtn = new Button(pageParameter.getLocalisedText("form.general.button.findUsage"),
                        new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent event) {
                                try {
                                    fieldGroup.commit();
                                    currentBean = (C) fieldGroup.getItemDataSource().getBean();
                                    currentBean = saveBean(currentBean, parentBean, beanUtils, currentUserRoles);
                                    FindUsageView<C> findUsageDataTableView = new FindUsageView<>(pageParameter, currentBean);
                                    String targetView = "FIND_USAGE_TABLE_VIEW_" + UUID.randomUUID().toString();
                                    WebEntity myObject = (WebEntity) currentBean.getClass().getAnnotation(WebEntity.class);
                                    History his = new History(targetView, pageParameter.getLocalisedText("form.general.button.findUsage", myObject.name()));
                                    pageParameter.getHistory().push(his);
                                    vcevent.getNavigator().addView(targetView, findUsageDataTableView);
                                    vcevent.getNavigator().navigateTo(targetView);
                                } catch (FieldGroup.CommitException ex) {
                                    handleFieldsError(fieldGroup);
                                }
                            }
                        });
                navButtons.addComponent(findUsageBtn);
                findUsageBtn.setId(findUsageBtn.getCaption());
            }
        }

        //Audit trail
        if (currentBean != null && dao.isAuditable(currentBean.getClass()) && 
                SecurityUtils.isCurrentUserAuditViewer(pageParameter.getConfig())==true) {
            
            Button auditTrailBtn = new Button(pageParameter.getLocalisedText("form.general.button.auditTrail"),
                    new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            try {
                                fieldGroup.commit();
                                currentBean = (C) fieldGroup.getItemDataSource().getBean();
                                currentBean = saveBean(currentBean, parentBean, beanUtils, currentUserRoles);
                                
                                AuditTrailView auditTrailView = new AuditTrailView(currentBean,currentBean.getClass(),3,pageParameter);
                                String targetView = "AUDIT_VIEW_" + UUID.randomUUID().toString();
                                History his = new History(targetView, pageParameter.getLocalisedText("view.auditttrail.title",beanUtils.getName(dao.getType())));
                                pageParameter.getHistory().push(his);
                                vcevent.getNavigator().addView(targetView,auditTrailView);
                                vcevent.getNavigator().navigateTo(targetView);

                            } catch (CommitException ex) {
                                Logger.getLogger(BeanView.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
            navButtons.addComponent(auditTrailBtn);
            auditTrailBtn.setId(auditTrailBtn.getCaption());
        }

        form.addComponent(navButtons);
        form.setMargin(new MarginInfo(true));
        this.addComponent(form);
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

    private void createForm(EntityOperation entityRight,
            final Set<String> currentUserRoles,
            final Map<String, Map<Integer, FieldContainer>> groups,
            final BeanFieldGroup fieldGroup,
            //final Map<Integer, RelationContainer> relations,
            final List<DataAccessObject<?>> customTypeDaos, Navigator nav,
            final FormLayout form) throws FieldGroup.BindException, UnsupportedOperationException {
        createForm(entityRight, currentUserRoles, groups, null, fieldGroup, customTypeDaos, nav, form);
    }

    private void createForm(EntityOperation entityRight,
            final Set<String> currentUserRoles,
            final Map<String, Map<Integer, FieldContainer>> groups,
            String group,
            final BeanFieldGroup fieldGroup,
            //final Map<Integer, RelationContainer> relations,
            final List<DataAccessObject<?>> customTypeDaos, final Navigator nav,
            final FormLayout form) throws FieldGroup.BindException, UnsupportedOperationException {
        //create bean utils
        final BeanUtils beanUtils = new BeanUtils();

        //select which group we want
        Map<Integer, FieldContainer> fieldContainerMap = null;
        if (group == null) {
            fieldContainerMap = groups.entrySet().iterator().next().getValue();
        } else {
            fieldContainerMap = groups.get(group);
        }

        //collect all activechoices
        Map<com.vaadin.ui.ComboBox, ActiveChoiceTarget> activeChoicesWithFieldAsKey = new HashMap<>();
        Map<String, com.vaadin.ui.ComboBox> activeChoicesFieldWithHierarchyAsKey = new HashMap<>();

        //collect all cutsom types
        List<Class> customTypes = new ArrayList<>();
        for (DataAccessObject<?> ctDao : customTypeDaos) {
            customTypes.add(ctDao.getType());
        }
        //deal with every field
        everyField:
        for (Map.Entry<Integer, FieldContainer> entry : fieldContainerMap.entrySet()) {
            final FieldContainer fieldContainer = entry.getValue();
            final FieldState effectiveFieldState = beanUtils.calculateEffectiveFieldState(fieldContainer.getPojoField(), currentUserRoles, entityRight);

            //Create form
            if (FieldState.INVISIBLE.equals(effectiveFieldState)) {
                continue everyField; //Continue with next field
            }

            //deal with normal form element
            com.vaadin.ui.Field uifield = null;
            //deal with plain choices
            if (fieldContainer.getWebField().choices().length > 0) {
                //deal with choices
                com.vaadin.ui.ComboBox formComboBox = new com.vaadin.ui.ComboBox(fieldContainer.getWebField().name());
                formComboBox.setImmediate(true);
                formComboBox.setFilteringMode(FilteringMode.STARTSWITH);
                fieldGroup.bind(formComboBox, fieldContainer.getPojoField().getName());
                for (Choice choice : fieldContainer.getWebField().choices()) {
                    if (choice.value() == -1) {
                        formComboBox.addItem(choice.textValue());
                        formComboBox.setItemCaption(choice.textValue(), choice.display());
                    } else {
                        formComboBox.addItem(choice.value());
                        formComboBox.setItemCaption(choice.value(), choice.display());
                    }
                }
                form.addComponent(formComboBox);
                uifield = formComboBox;
                //deal with active choices
            } else if (fieldContainer.getWebField().activeChoice().enumTree() != EmptyEnum.class) {
                //collect active choices - 
                com.vaadin.ui.ComboBox formComboBox = new com.vaadin.ui.ComboBox(fieldContainer.getWebField().name());
                formComboBox.setImmediate(true);
                formComboBox.setFilteringMode(FilteringMode.STARTSWITH);

                fieldGroup.bind(formComboBox, fieldContainer.getPojoField().getName());
                String hierarchy = fieldContainer.getWebField().activeChoice().hierarchy();
                Class<ActiveChoiceEnum> enumTree = (Class<ActiveChoiceEnum>) fieldContainer.getWebField().activeChoice().enumTree();
                ActiveChoiceTarget activeChoiceTarget = ActiveChoiceUtils.build(enumTree, hierarchy);
                for (String choice : activeChoiceTarget.getSourceChoices()) {
                    formComboBox.addItem(choice);
                    activeChoicesWithFieldAsKey.put(formComboBox, activeChoiceTarget);
                    activeChoicesFieldWithHierarchyAsKey.put(hierarchy, formComboBox);
                }

                form.addComponent(formComboBox);
                uifield = formComboBox;

                //deal with Id
            } else if (fieldContainer.getPojoField().isAnnotationPresent(Id.class)) {
                com.vaadin.ui.Field formField = fieldGroup.buildAndBind(fieldContainer.getWebField().name(), fieldContainer.getPojoField().getName());
                if (Number.class.isAssignableFrom(fieldContainer.getPojoField().getType())) {
                    ((TextField) formField).setConverter(new NumberBasedIDConverter((Class<Number>) fieldContainer.getPojoField().getType()));
                }

                form.addComponent(formField);
                uifield = formField;
            } else if (fieldContainer.getPojoField().isAnnotationPresent(OneToMany.class)
                    || fieldContainer.getPojoField().isAnnotationPresent(ManyToOne.class)
                    || fieldContainer.getPojoField().isAnnotationPresent(ManyToMany.class)) {

                //special relationship: deal with custom type form element
                int state = 0;
                Class classOfField = null;
                while (true) {
                    if (state == 0) {
                        if (Collection.class.isAssignableFrom(fieldContainer.getPojoField().getType())) {
                            classOfField = (Class) ((ParameterizedType) fieldContainer.getPojoField().getGenericType()).getActualTypeArguments()[0];
                            state = 1;
                        } else {
                            classOfField = fieldContainer.getPojoField().getType();
                            state = 3;
                            break;
                        }
                    }
                    if (state == 1) {
                        if (CustomType.class.isAssignableFrom(classOfField)) {

                            state = 2;
                            break;
                        } else {

                            state = 3;
                            break;
                        }
                    }
                }
                EntityOperation rightOfField = determineEntityRight(classOfField, currentUserRoles);
                if (rightOfField == null) { //if entityRight=EntityRight.NONE, still allow to go through because field level might be accessible
                    //Not accessible
                    rightOfField = EntityOperation.RESTRICTED;
                }
                if (state == 2) { //Custom type
                    Button openCustom = new Button(pageParameter.getLocalisedText("form.general.button.manage", fieldContainer.getWebField().name()), new Button.ClickListener() {
                        @Override
                        public void buttonClick(ClickEvent event) {
                            try {
                                fieldGroup.commit();
                                C cBean = (C) fieldGroup.getItemDataSource().getBean();
                                currentBean = saveBean(cBean,parentBean,beanUtils,currentUserRoles);
                                
//                                 fieldGroup.commit();
//                                    currentBean = (C) fieldGroup.getItemDataSource().getBean();
//                                    fieldGroup.commit();
//                                    currentBean = (C) fieldGroup.getItemDataSource().getBean();
//                                    currentBean = saveBean(currentBean,
//                                            parentBean,
//                                            beanUtils,
//                                            currentUserRoles);
                                fieldGroup.setItemDataSource(currentBean);
                                //field class
                                Class iclassOfField = (Class) ((ParameterizedType) fieldContainer.getPojoField().getGenericType()).getActualTypeArguments()[0];

                                //find a custom type dao
                                DataAccessObject<? extends CustomType> chosenCTDao = null;
                                for (DataAccessObject cdao : customTypeDaos) {
                                    if (cdao.getType().isAssignableFrom(iclassOfField)) {
                                        chosenCTDao = cdao;
                                        break;
                                    }
                                }

                                //deal with windows
                                final Window window = new Window();
                                final AttachmentCustomTypeUICreator<C> attachmentCustomTypeUICreator = new AttachmentCustomTypeUICreator();
                                Component customTypeComponent = attachmentCustomTypeUICreator.createUIForForm(currentBean,
                                        iclassOfField,
                                        fieldContainer.getPojoField().getName(),
                                        BeanView.this,
                                        dao,
                                        chosenCTDao,
                                        pageParameter,
                                        effectiveFieldState,
                                        window);
                                customTypeComponent.setCaption(pageParameter.getLocalisedText("form.general.button.manage", fieldContainer.getWebField().name()));
                                customTypeComponent.setId(customTypeComponent.getCaption());
                           
                                window.setCaption(customTypeComponent.getCaption());
                                window.setId(window.getCaption());
                                window.setContent(customTypeComponent);
                                window.addCloseListener(new Window.CloseListener() {
                                    @Override
                                    public void windowClose(Window.CloseEvent e) {
                                       currentBean = dao.refresh(currentBean);
                                       fieldGroup.setItemDataSource(currentBean);
                                       int ijk=0;
                                    }
                                });
                                window.setModal(true);
                                

                                BeanView.this.getUI().addWindow(window);
                            } catch (CommitException ex) {
                                handleFieldsError(fieldGroup);
                            }
                        }
                    });
                    openCustom.setId(openCustom.getCaption());
                    form.addComponent(openCustom);

                } else { 
                    processRelationship(fieldContainer, beanUtils, currentUserRoles, entityRight, rightOfField, fieldGroup, nav, form);
                }

            } else {
                com.vaadin.ui.Field formField = fieldGroup.buildAndBind(fieldContainer.getWebField().name(), fieldContainer.getPojoField().getName());
                if (fieldContainer.getPojoField().getType().equals(Date.class)) {
                    //deal with date
                    DateField dateField = (DateField) formField;
                    dateField.setDateFormat(pageParameter.getConfig().get("dateFormat"));
                    dateField.setWidth(100f, Unit.PIXELS);
                } else if (fieldContainer.getPojoField().getType().equals(BigDecimal.class)) {
                    TextField bdField = (TextField) formField;
                    bdField.setConverter(new StringToBigDecimalConverter());
                }

                form.addComponent(formField);
                uifield = formField;
            }

            if (uifield != null) {
                //deal with read only
                if (FieldState.READ_ONLY.equals(effectiveFieldState)) {
                    uifield.setReadOnly(true);
                } else {
                    uifield.setReadOnly(false);
                    if (fieldContainer.getWebField().required() == true) {
                        uifield.setRequired(true);
                    }
                }

                //set null presentation
                if (uifield instanceof AbstractTextField) {
                    AbstractTextField textField = (AbstractTextField) uifield;
                    textField.setNullRepresentation("");
                }

                //set debug id
                uifield.setId(uifield.getCaption());
            }
        }

        //deal with active choice
        for (final com.vaadin.ui.ComboBox sourceField : activeChoicesWithFieldAsKey.keySet()) {
            final ActiveChoiceTarget target = activeChoicesWithFieldAsKey.get(sourceField);
            final com.vaadin.ui.ComboBox targetField = activeChoicesFieldWithHierarchyAsKey.get(target.getTargetHierarchy());
            sourceField.addValueChangeListener(new ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    List<String> targetValues = target.getTargets().get(sourceField.getValue());
                    if (targetValues != null && !targetValues.isEmpty() && targetField != null) {
                        targetField.removeAllItems();
                        for (String targetValue : targetValues) {
                            targetField.addItem(targetValue);
                        }
                    }
                }
            });

        }
    }

    public void processRelationship(final FieldContainer fieldContainer, final BeanUtils beanUtils, final Set<String> currentUserRoles, EntityOperation entityRight, EntityOperation rightOfField, final BeanFieldGroup fieldGroup, final Navigator nav, final FormLayout form) throws SecurityException {
        //relationship
        try {
            fieldContainer.getPojoField().setAccessible(true);
            final WebField relation = fieldContainer.getPojoField().getAnnotation(WebField.class);
            
            Button relationshipButton = null;
            final RelationState effectiveRelationState = beanUtils.calculateEffectiveRelationState(fieldContainer.getPojoField(), currentUserRoles, entityRight, rightOfField);
            if (fieldContainer.getPojoField().isAnnotationPresent(OneToMany.class)) {
                relationshipButton = new Button(pageParameter.getLocalisedText("form.general.button.manage", relation.name()),
                        new Button.ClickListener() {
                            @Override
                            public void buttonClick(
                                    Button.ClickEvent event) {
                                try {
                                    fieldGroup.commit();
                                    currentBean = (C) fieldGroup.getItemDataSource().getBean();
                                    currentBean = saveBean(currentBean,
                                            parentBean,
                                            beanUtils,
                                            currentUserRoles);
                                    
                                    Class classOfField = (Class) ((ParameterizedType) fieldContainer.getPojoField().getGenericType()).getActualTypeArguments()[0];
                                    RelationView view = new RelationView(currentBean,
                                            fieldContainer.getPojoField().getName(),
                                            effectiveRelationState,
                                            classOfField,
                                            ChoiceType.CHOOSE_MANY,
                                            pageParameter);
                                    String targetView = "BEAN_VIEW_" + UUID.randomUUID().toString();
                                    History his = new History(targetView, pageParameter.getLocalisedText("form.general.button.manage", relation.name()));
                                    pageParameter.getHistory().push(his);
                                    nav.addView(targetView, view);
                                    nav.navigateTo(targetView);
                                } catch (CommitException ex) {
                                    handleFieldsError(fieldGroup);
                                }
                            }
                        });
                
            } else if (fieldContainer.getPojoField().isAnnotationPresent(ManyToOne.class)) {
                relationshipButton = new Button(pageParameter.getLocalisedText("form.general.button.manage", relation.name()),
                        new Button.ClickListener() {
                            @Override
                            public void buttonClick(
                                    Button.ClickEvent event) {
                                try {
                                    fieldGroup.commit();
                                    currentBean = (C) fieldGroup.getItemDataSource().getBean();
                                    fieldGroup.commit();
                                    currentBean = (C) fieldGroup.getItemDataSource().getBean();
                                    currentBean = saveBean(currentBean,
                                            parentBean,
                                            beanUtils,
                                            currentUserRoles);
                                    
                                    RelationView view = new RelationView(currentBean,
                                            fieldContainer.getPojoField().getName(),
                                            effectiveRelationState,
                                            fieldContainer.getPojoField().getType(),
                                            ChoiceType.CHOOSE_ONE,
                                            pageParameter);
                                    String targetView = "BEAN_VIEW_" + UUID.randomUUID().toString();
                                    History his = new History(targetView, pageParameter.getLocalisedText("form.general.button.manage", relation.name()));
                                    pageParameter.getHistory().push(his);
                                    nav.addView(targetView, view);
                                    nav.navigateTo(targetView);
                                } catch (CommitException ex) {
                                    handleFieldsError(fieldGroup);
                                }
                            }
                        });
            } else if (fieldContainer.getPojoField().isAnnotationPresent(ManyToMany.class)) {
                relationshipButton = new Button(pageParameter.getLocalisedText("form.general.button.manage", relation.name()),
                        new Button.ClickListener() {
                            @Override
                            public void buttonClick(
                                    Button.ClickEvent event) {
                                try {
                                    fieldGroup.commit();
                                    currentBean = (C) fieldGroup.getItemDataSource().getBean();
                                    fieldGroup.commit();
                                    currentBean = (C) fieldGroup.getItemDataSource().getBean();
                                    currentBean = saveBean(currentBean,
                                            parentBean,
                                            beanUtils,
                                            currentUserRoles);
                                    
                                    Class classOfField = (Class) ((ParameterizedType) fieldContainer.getPojoField().getGenericType()).getActualTypeArguments()[0];
                                    RelationView view = new RelationView(currentBean,
                                            fieldContainer.getPojoField().getName(),
                                            effectiveRelationState,
                                            classOfField,
                                            ChoiceType.CHOOSE_MANY,
                                            pageParameter);
                                    
                                    String targetView = "BEAN_VIEW_" + UUID.randomUUID().toString();
                                    History his = new History(targetView, pageParameter.getLocalisedText("form.general.button.manage", relation.name()));
                                    pageParameter.getHistory().push(his);
                                    nav.addView(targetView, view);
                                    nav.navigateTo(targetView);
                                } catch (CommitException ex) {
                                    handleFieldsError(fieldGroup);
                                }
                            }
                        });
            }
            relationshipButton.setId(relationshipButton.getCaption());
            form.addComponent(relationshipButton);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(BeanView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private C saveBean(C bean, P pbean, BeanUtils beanUtils,
            Set<String> currentUserRoles) {
        if (beanUtils.isEditable(bean.getClass(), currentUserRoles) || beanUtils.isCreatable(bean.getClass(), currentUserRoles)) {
            if (pbean == null) {
                return dao.save(bean);
            } else {
                return dao.saveAndAssociate(bean, pbean, parentToCurrentBeanField, pageParameter.getRelationManagerFactory().create((Class<P>) pbean.getClass(), (Class<C>) bean.getClass()));
            }
        } else {
            return null;
        }
    }

    private List<com.vaadin.ui.Field<?>> validateFields(BeanFieldGroup fg) {
        List<com.vaadin.ui.Field<?>> invFields = new ArrayList<>();
        Collection<com.vaadin.ui.Field<?>> fields = fg.getFields();
        for (com.vaadin.ui.Field f : fields) {
            try {
                f.validate();
            } catch (InvalidValueException ivex) {
                invFields.add(f);

            }
        }
        return invFields;
    }

    private void handleFieldsError(BeanFieldGroup fieldGroup) {
        List<com.vaadin.ui.Field<?>> invFields = validateFields(fieldGroup);
        if (invFields.size() > 0) {
            invFields.iterator().next().focus();
            for (com.vaadin.ui.Field<?> invf : invFields) {
                if (invf instanceof AbstractComponent) {
                    ((AbstractComponent) invf).setComponentError(new UserError(pageParameter.getLocalisedText("error.message.display.invalidValue")));
                }
            }
        }
        Notification.show(pageParameter.getLocalisedText("error.message.display.invalidValue"), Notification.Type.HUMANIZED_MESSAGE);
    }
}

class StringToBigDecimalConverter extends AbstractStringToNumberConverter<BigDecimal> {

    @Override
    protected NumberFormat getFormat(Locale locale) {
        NumberFormat numberFormat = super.getFormat(locale);
        if (numberFormat instanceof DecimalFormat) {
            ((DecimalFormat) numberFormat).setParseBigDecimal(true);
        }

        return numberFormat;
    }

    @Override
    public BigDecimal convertToModel(String value,
            Class<? extends BigDecimal> targetType, Locale locale)
            throws com.vaadin.data.util.converter.Converter.ConversionException {
        return (BigDecimal) convertToNumber(value, BigDecimal.class, locale);
    }

    @Override
    public Class<BigDecimal> getModelType() {
        return BigDecimal.class;
    }
}
