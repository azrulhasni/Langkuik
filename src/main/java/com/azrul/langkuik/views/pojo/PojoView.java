/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.views.pojo;

import com.azrul.langkuik.framework.annotation.CustomField;
import com.azrul.langkuik.framework.annotation.WebEntity;
import com.azrul.langkuik.framework.dao.DataAccessObject;
import com.azrul.langkuik.framework.dao.EntityUtils;
import com.azrul.langkuik.framework.dao.FindRelationParameter;
import com.azrul.langkuik.framework.dao.FindRelationQuery;
import com.azrul.langkuik.framework.field.FieldUtils;
import com.azrul.langkuik.framework.relationship.RelationUtils;
import com.azrul.langkuik.views.main.MainView;
import com.azrul.langkuik.views.table.TableView;
import com.azrul.langkuik.framework.field.FieldContainer;
import com.azrul.langkuik.framework.dao.Dual;
import com.azrul.langkuik.framework.relationship.RelationContainer;
import com.azrul.langkuik.framework.relationship.RelationMemento;
import com.azrul.langkuik.framework.relationship.RelationType;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.converter.StringToLongConverter;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import java.beans.IntrospectionException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections4.MultiValuedMap;
import java.util.Set;
import com.azrul.langkuik.custom.CustomComponentRenderer;
import com.azrul.langkuik.custom.CustomFieldRenderer;
import com.azrul.langkuik.framework.annotation.WebEntityType;
import com.azrul.langkuik.framework.factory.SpringBeanFactory;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import com.azrul.langkuik.framework.standard.Rules;
import com.azrul.langkuik.views.audit.AuditView;
import com.azrul.langkuik.views.findusage.FindUsageView;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author azrul
 */
//@Component
@Route(value = "PojoView", layout = MainView.class)
public class PojoView<T> extends Div implements AfterNavigationObserver, BeforeLeaveObserver {

    @Value("${application.lgDateFormatLocale}")
    private String dateFormatLocale;

    @Value("${application.lgDateFormat}")
    private String dateFormat;

    @Value("${application.lgFullurl}")
    private String fullUrl;

    @Autowired
    private DataAccessObject dao;

    @Autowired
    ValidatorFactory validatorFactory;

    @Autowired
    PojoTableFactory pojoTableFactory;

    private Binder<T> binder;
    private Dialog dialog;
    private Map<String, RelationMemento> relationMementos;
    private Class<T> currentClass;
    private Boolean discarded = false;
    private Boolean submitted = false;
 
   

    public PojoView() {
        this.relationMementos = new HashMap<>();
    }

    public Boolean isDiscarded() {
        return discarded;
    }

    public void construct(T bean, Dialog dialog) {
        if (bean == null) {
            return;
        }
        Class<T> tclass = (Class<T>) bean.getClass();
        this.binder = new Binder<>(tclass);
        this.setCurrentBean((T) bean);
        this.currentClass = (Class<T>) bean.getClass();
        try {
            FormLayout form = new FormLayout();
            Validator validator = validatorFactory.getValidator();

            MultiValuedMap<Integer, FieldContainer> fieldStore = FieldUtils.getFieldsByOrder(getCurrentClass());
            Map<String, AbstractField> vaadinFieldStore = new HashMap<>();

            //sort fields by order
            List<Integer> orderOfFields = new ArrayList<>(fieldStore.keySet());
            Collections.sort(orderOfFields);
            //Add fields to grid
            for (Integer order : orderOfFields) {
                for (FieldContainer fc : fieldStore.get(order)) {
                    if (fc.getCustomComponent().isPresent()) {
                        break;
                    }

                    //Get date format (for dates)
                    final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

                    //Only display what is visible
                    AbstractField field = null;
                    if (fc.getWebField().visibleInForm() == true) {
                        if (LocalDate.class.equals(fc.getReturnType())) {
                            DatePicker datePicker = new DatePicker(fc.getWebField().displayName());
                            datePicker.setLocale(Locale.forLanguageTag(dateFormatLocale));
                            //datePicker.set
                            field = datePicker;
                            binder.forField(field)
                                    .withValidator(new com.vaadin.flow.data.binder.Validator<LocalDate>() {

                                        @Override
                                        public ValidationResult apply(LocalDate value, ValueContext context) {
                                            String errorMsg = validator.validateProperty(getDraftCurrentBean(), fc.getField().getName())
                                                    .stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
                                            if (errorMsg.isEmpty()) {
                                                return ValidationResult.ok();
                                            } else {
                                                return ValidationResult.error(errorMsg);
                                            }
                                        }
                                    })
                                    .bind(fc.getField().getName());

//                            field = datePicker;
                        } else if (Timestamp.class.equals(fc.getReturnType())) {
                            DatePicker datePicker = new DatePicker(fc.getWebField().displayName());
                            datePicker.setLocale(Locale.forLanguageTag(dateFormatLocale));
                            binder.forField(field)
                                    .withValidator(new com.vaadin.flow.data.binder.Validator<Timestamp>() {

                                        @Override
                                        public ValidationResult apply(Timestamp value, ValueContext context) {
                                            String errorMsg = validator.validateProperty(getDraftCurrentBean(), fc.getField().getName())
                                                    .stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
                                            if (errorMsg.isEmpty()) {
                                                return ValidationResult.ok();
                                            } else {
                                                return ValidationResult.error(errorMsg);
                                            }
                                        }
                                    })
                                    .bind(fc.getField().getName());
                        } else if (Long.class.equals(fc.getReturnType())) {
                            field = new TextField(fc.getWebField().displayName());
                            binder.forField(field)
                                    .withConverter(new StringToLongConverter("Please enter a number"))
                                    .withValidator(new com.vaadin.flow.data.binder.Validator<Long>() {

                                        @Override
                                        public ValidationResult apply(Long value, ValueContext context) {
                                            String errorMsg = validator.validateProperty(getDraftCurrentBean(), fc.getField().getName())
                                                    .stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
                                            if (errorMsg.isEmpty()) {
                                                return ValidationResult.ok();
                                            } else {
                                                return ValidationResult.error(errorMsg);
                                            }
                                        }
                                    })
                                    .withNullRepresentation(0l)
                                    .bind(fc.getField().getName());
//                            field = textField;
                        } else if (Integer.class.equals(fc.getReturnType())) {
                            field = new TextField(fc.getWebField().displayName());
                            binder.forField(field)
                                    .withConverter(new StringToIntegerConverter("Please enter a number"))
                                    .withValidator(new com.vaadin.flow.data.binder.Validator<Integer>() {

                                        @Override
                                        public ValidationResult apply(Integer value, ValueContext context) {
                                            String errorMsg = validator.validateProperty(getDraftCurrentBean(), fc.getField().getName())
                                                    .stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
                                            if (errorMsg.isEmpty()) {
                                                return ValidationResult.ok();
                                            } else {
                                                return ValidationResult.error(errorMsg);
                                            }
                                        }
                                    })
                                    .withNullRepresentation(0)
                                    .bind(fc.getField().getName());
                        } else if (Boolean.class.equals(fc.getReturnType())) {
                            field = new Checkbox(fc.getWebField().displayName());

                            binder.forField(field)
                                    .withValidator(new com.vaadin.flow.data.binder.Validator<Boolean>() {

                                        @Override
                                        public ValidationResult apply(Boolean value, ValueContext context) {
                                            String errorMsg = validator.validateProperty(getDraftCurrentBean(), fc.getField().getName())
                                                    .stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
                                            if (errorMsg.isEmpty()) {
                                                return ValidationResult.ok();
                                            } else {
                                                return ValidationResult.error(errorMsg);
                                            }
                                        }
                                    })
                                    .withNullRepresentation(Boolean.FALSE)
                                    .bind(fc.getField().getName());
                        } else {
                            field = new TextField(fc.getWebField().displayName());
                            binder.forField(field)
                                    .withValidator(new com.vaadin.flow.data.binder.Validator<String>() {

                                        @Override
                                        public ValidationResult apply(String value, ValueContext context) {
                                            String errorMsg = validator.validateProperty(getDraftCurrentBean(), fc.getField().getName())
                                                    .stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
                                            if (errorMsg.isEmpty()) {
                                                return ValidationResult.ok();
                                            } else {
                                                return ValidationResult.error(errorMsg);
                                            }
                                        }
                                    })
                                    .withNullRepresentation("")
                                    .bind(fc.getField().getName());
                        }
                    }
                    if (field != null) {
                        if (fc.isId()) {
                            field.setReadOnly(true);
                        }
                        vaadinFieldStore.put(fc.getField().getName(), field);
                        //form.add(field);
                    }
                }
            }

            for (Integer order : orderOfFields) {
                for (FieldContainer fc : fieldStore.get(order)) {
                    Optional<Class> customRendererClass = fc.getCustomComponent();
                    customRendererClass.ifPresentOrElse(customCompClass -> {
                        CustomField customField = (CustomField) ((Class) customCompClass).getAnnotation(CustomField.class);
                        Class rendererClass = customField.renderer();
                        CustomFieldRenderer renderer = (CustomFieldRenderer) SpringBeanFactory.create(rendererClass);

                        Optional<AbstractField> field = renderer.create(getCurrentBean(), fc.getField().getName(), fc.getWebField().displayName(), vaadinFieldStore);
                        TextField alter = new TextField("Component failed to render");
                        binder.forField(field.orElse(alter)).bind(fc.getField().getName());
                        vaadinFieldStore.put(fc.getField().getName(), field.orElse(alter));
                        form.add(field.orElse(alter));

                    }, () -> {
                        AbstractField field = vaadinFieldStore.get(fc.getField().getName());
                        form.add(field);
                    });

                }
            }

            
            Span errorMessage = new Span();
            HorizontalLayout headerLayout = new HorizontalLayout();
            
            WebEntity webEntity = currentClass.getAnnotation(WebEntity.class);
            if (webEntity.type().equals(WebEntityType.ROOT)){
                headerLayout.add(new Button("Save as draft and back", e -> {
                    BinderValidationStatus<T> validate = binder.validate();
                    if (validate.isOk()) {
                        dialog.close();
                    }
                }));
                headerLayout.add(new Button("Save and submit", e -> {
                    BinderValidationStatus<T> validate = binder.validate();
                    if (validate.isOk()) {
                        submitted = true;
                        dialog.close();
                    }
                }));
            }else{
                headerLayout.add(new Button("Done", e -> {
                    BinderValidationStatus<T> validate = binder.validate();
                    if (validate.isOk()) {
                        dialog.close();
                    }
                }));
            }
             headerLayout.add(new Button("Delete", e -> {
                discarded = true;
                dialog.close();
            }));
            headerLayout.add(new Button("Show audit trail", e -> {
                AuditView auditView = SpringBeanFactory.create(AuditView.class);
                Dialog dialog2 = new Dialog();
                dialog2.add(auditView);
                auditView.construct(getCurrentBean(), dialog2);
                dialog2.setModal(true);
                dialog2.setCloseOnEsc(false);
                dialog2.setCloseOnOutsideClick(false);
                dialog2.open();

            }));

            headerLayout.add(new Button("Find Usage", e -> {
                FindUsageView findUsageView = SpringBeanFactory.create(FindUsageView.class);

                Dialog dialog2 = new Dialog();
                findUsageView.construct(getCurrentBean(), dialog2);
                dialog2.add(findUsageView);
                dialog2.setModal(true);
                dialog2.setCloseOnEsc(false);
                dialog2.setCloseOnOutsideClick(false);
                dialog2.open();
            }));

            VerticalLayout headerAndFormLayout = new VerticalLayout();
            headerAndFormLayout.add(errorMessage);
            headerAndFormLayout.setMargin(false);
            headerAndFormLayout.setPadding(true);
            headerAndFormLayout.add(headerLayout);
            headerAndFormLayout.add(form);
            this.add(headerAndFormLayout);

            MultiValuedMap<Integer, RelationContainer> relationStore = RelationUtils.getRelationsByOrder(getCurrentClass());

            //sort fields by order
            List<Integer> orderOfRelations = new ArrayList<>(relationStore.keySet());
            Collections.sort(orderOfRelations);

            //Add fields to table
            for (Integer order : orderOfRelations) {
                Collection<RelationContainer> rcs = relationStore.asMap().get(order);

                for (RelationContainer rc : rcs) {
                    Optional<WebEntity> childWebEntity = Optional.
                            ofNullable(
                                    (WebEntity) rc.getChildType()
                                            .getAnnotation(WebEntity.class));

                    //deal with standard component
                    if (rc.isCustomComponent() == false) {
                        //pages.put(rc.getRelationshipName(), 1);
                        //Buttons
                        Notification notification = new Notification(
                                "Saving current data as draft", 3000, Notification.Position.TOP_START);
                        HorizontalLayout buttonLayout = new HorizontalLayout();
                        childWebEntity.ifPresent(we -> {
                            if (we.type() == WebEntityType.NOMINAL) {
                                Button btnAddNew = new Button("Add new", e -> {

                                    notification.open();
                                    BinderValidationStatus<T> validate = binder.validate();
                                    if (validate.isOk()) {
                                        dao.save(getCurrentBean());
                                        var childBean = createNew(rc.getChildType(), getCurrentBean());
                                        //save the current objects (as draft) before going to another page
                                        createPojoDialog(getCurrentBean(), rc.getRelationshipName(), childBean);
                                    }

                                });
                                buttonLayout.add(btnAddNew);
                            } else if (we.type() == WebEntityType.REF) {
                                Set<String> roles = VaadinSession.getCurrent().getSession().getAttribute("ROLES") != null
                                        ? (Set<String>) VaadinSession.getCurrent().getSession().getAttribute("ROLES")
                                        : new HashSet<String>();
                                if (roles.contains("REF_ADMIN")) {
                                    Button btnAddNew = new Button("Add new", e -> {

                                        notification.open();
                                        BinderValidationStatus<T> validate = binder.validate();
                                        if (validate.isOk()) {
                                            dao.save(getCurrentBean());
                                            var childBean = createNew(rc.getChildType(), getCurrentBean());
                                            //save the current objects (as draft) before going to another page
                                            createPojoDialog(getCurrentBean(), rc.getRelationshipName(), childBean);
                                        }

                                    });
                                    buttonLayout.add(btnAddNew);
                                }
                            }

                        });
                        childWebEntity.ifPresent(we -> {
                            if (we.type() == WebEntityType.REF) {
                                Button btnLinkExisting = new Button("Link existing", e -> {

                                    notification.open();
                                    BinderValidationStatus<T> validate = binder.validate();
                                    if (validate.isOk()) {
                                        dao.save(getCurrentBean());
                                        createTableDialog(getCurrentBean(), rc.getRelationshipName());
                                    }

                                });
                                buttonLayout.add(btnLinkExisting);
                            }
                        });

                        Button btnDeleteSelected = new Button("Unlink/Delete selected", e -> {
                            String username = (String) VaadinSession.getCurrent().getSession().getAttribute("USERNAME");

                            Grid grid = relationMementos.get(rc.getRelationshipName()).getGrid();
                            Set itemsToBeDeleted = grid.getSelectedItems();
                            FindRelationParameter param = new FindRelationParameter(getCurrentBean(), rc.getRelationshipName());
                            List<String> cannotBeDeleted = new ArrayList<>();
                            dao.unlinkAndDelete(param, itemsToBeDeleted, (p, link, c) -> {
                                LangkuikExt o = (LangkuikExt) c;
                                if (!Rules.canBeDeleted(o, username)) {
                                    cannotBeDeleted.add(o.toString());
                                    return false;
                                } else {
                                    return true;
                                }
                            });

                            StringBuilder textHtml = new StringBuilder("<div><div>The items below cannot be deleted</div> <ul>");

                            for (String item : cannotBeDeleted) {
                                textHtml.append("<li>" + item + "</li>");
                            }
                            textHtml.append("</ul></div>");
                            Html hc = new Html(textHtml.toString());

                            Notification errorNotif = new Notification(hc);
                            errorNotif.setPosition(Position.TOP_START);
                            errorNotif.setDuration(3000);
                            errorNotif.open();

                            pojoTableFactory.redrawTables(getCurrentBean(),
                                    rc.getRelationshipName(),
                                    relationMementos.get(rc.getRelationshipName()));
                        });
                        buttonLayout.add(btnDeleteSelected);

                        Button btnClearSelected = new Button("Clear selection", e -> {
                            relationMementos.get(rc.getRelationshipName()).getGrid().deselectAll();
                        });

                        buttonLayout.add(btnClearSelected);

                        //Build table
                        VerticalLayout layout = new VerticalLayout();
                        pojoTableFactory.createTable(
                                getCurrentBean(),
                                rc.getRelationshipName(),
                                rc.getWebRelation().name(),
                                layout,
                                relationMementos,
                                buttonLayout,
                                e -> {
                                    if (e.getClickCount() == 2) {
                                        LangkuikExt ext = (LangkuikExt) e.getItem();
                                        if (ext.getId() != null) {

                                            BinderValidationStatus<T> validate = binder.validate();
                                            if (validate.isOk()) {
                                                dao.save(getCurrentBean());
                                                createPojoDialog(getCurrentBean(), rc.getRelationshipName(), e.getItem());
                                            }

                                        }
                                    }
                                });
                        //relationMementos.put(rc.getRelationshipName(), relationMemento);
                        this.add(layout);
                    } else { //deal with custom component
                        CustomComponentRenderer renderer = SpringBeanFactory.create(rc.getWebRelation().customComponent());
                        VerticalLayout customComponentLayout = new VerticalLayout();
                        T currentBean = (T) renderer.renderInFormAsDependency(getCurrentBean(),
                                rc.getRelationshipName(),
                                customComponentLayout,
                                relationMementos).orElseThrow();
                        setCurrentBean(currentBean);
                        //relationMementos.put(rc.getRelationshipName(), memento);
                        this.add(customComponentLayout);

                    }
                }
            }
        } catch (IntrospectionException
                | NoSuchFieldException
                | SecurityException
                | IllegalArgumentException ex) {
            Logger.getLogger(PojoView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    /**
     * @return the current
     */
    public T getCurrentBean() {
        //return current;
        return binder.getBean();
    }

    public T getDraftCurrentBean() {
        //return current;
        T nbean = EntityUtils.createNewObject(currentClass);
        binder.writeBeanAsDraft(nbean, true);
        return nbean;
    }

    public Class<T> getCurrentClass() {
        //return current;
        return currentClass;
    }

    /**
     * @param current the current to set
     */
    private void setCurrentBean(T current) {
        //this.current = current;
        binder.setBean(current);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent arg0) {
        if (VaadinSession.getCurrent().getSession().getAttribute("USERNAME") == null) {
            UI.getCurrent().getPage().executeJs("window.open(\"" + fullUrl + "/main\", \"_self\");");
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent arg0) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public <C> C createNew(Class<C> childClass, T parentBean) {

        String username = (String) VaadinSession.getCurrent().getSession().getAttribute("USERNAME");
        String tenant = (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");

        return (C) dao.createAndSave(childClass, Optional.of(tenant), username, Optional.of(EntityUtils.getTranxId(parentBean))).orElseThrow();
        // Dual<T, C> dual = dao.saveAndAssociate(childBean, parentBean, relationName);
        //return dual;

    }

    public <C> void createPojoDialog(T parent, String relationName, C child) {

        Dialog dialog2 = new Dialog();
        PojoView<C> pojoView = SpringBeanFactory.create(PojoView.class);
        pojoView.construct(child, dialog2);
        dialog2.setModal(true);
        dialog2.setCloseOnEsc(false);
        dialog2.setCloseOnOutsideClick(false);
        dialog2.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                if (pojoView.isDiscarded() == false) {
                    Optional<Dual<T, C>> oresult = dao.saveAndAssociate(pojoView.getCurrentBean(), parent, relationName);
                    Dual<T, C> result = oresult.orElseThrow();
                    setCurrentBean(result.getFirst());
                } else {
                    String username = (String) VaadinSession.getCurrent().getSession().getAttribute("USERNAME");
                    Collection<C> itemsToBeDeleted = new ArrayList<>();
                    itemsToBeDeleted.add(pojoView.getCurrentBean());
                    FindRelationParameter param = new FindRelationParameter(getCurrentBean(), relationName);
                    dao.unlinkAndDelete(param, itemsToBeDeleted, (p, link, c) -> {
                        LangkuikExt o = (LangkuikExt) c;
                        if (!Rules.canBeDeleted(o, username)) {
                            return false;
                        } else {
                            return true;
                        }
                    });
                    T tbean = (T) dao.refresh(getCurrentBean());
                    setCurrentBean(tbean);
                }
                pojoTableFactory.redrawTables(getCurrentBean(),
                        relationName, relationMementos.get(relationName));

            }
        });

        //begin:enable scroll
        pojoView.setHeight("100%");
        pojoView.getStyle().set("overflow-y", "auto");
        dialog2.add(pojoView);
        dialog2.setHeightFull();
        //end:enable scroll
        dialog2.open();

        //UI.getCurrent().navigate(PojoView.class);
    }

    public <C> void createTableDialog(T parentBean,
            String relationName) {

        String tenant = (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");

        //Field field = FieldUtils.getField(parentClass, relationName);
        Class<T> parentClass = (Class<T>) parentBean.getClass();
        Class<C> childClass = RelationUtils.getRelationClass(parentClass, relationName);

        FindRelationParameter<T, C> param = new FindRelationParameter(parentBean,
                relationName);
        FindRelationQuery searchQuery = new FindRelationQuery(param);
        Collection exclusion = dao.runQuery(searchQuery, Optional.empty(), Optional.of(Boolean.FALSE), Optional.of(0), Optional.empty(), Optional.of(tenant), Optional.empty());

        RelationType relationType = RelationUtils.getRelationType(parentClass, relationName);
        Dialog dialog2 = new Dialog();
        TableView<C> tableView = SpringBeanFactory.create(TableView.class);

        tableView.construct(childClass,
                TableView.Mode.SELECT,
                Optional.of(relationType),
                dialog2,
                exclusion);
        dialog2.setModal(true);
        dialog2.setCloseOnEsc(false);
        dialog2.setCloseOnOutsideClick(false);
        tableView.setHeight("100%");
        tableView.getStyle().set("overflow-y", "auto");
        tableView.doSearch();
        dialog2.add(tableView);
        dialog2.setHeightFull();
        dialog2.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                Set selections = tableView.getSelected();
                // RelationManager relationManager = relationMgrFactory.create(parentClass, childClass);

                Set<Dual<T, C>> duals = dao.saveAndAssociate(selections, getCurrentBean(), relationName);
                if (duals.isEmpty() == false) {
                    setCurrentBean(duals.iterator().next().getFirst()); //re associate the parent to current object
                }
                pojoTableFactory.redrawTables(parentBean, relationName, relationMementos.get(relationName));

            }
        });
        //end:enable scroll

        dialog2.open();

        //UI.getCurrent().navigate(PojoView.class);
    }

    /**
     * @return the submitted
     */
    public Boolean isSubmitted() {
        return submitted;
    }

    /**
     * @param submitted the submitted to set
     */
    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

}
