/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.webgui;

import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.StringToDateConverter;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Id;
import org.azrul.langkuik.annotations.Choice;
import org.azrul.langkuik.annotations.DerivedField;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.dao.DAOQuery;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.dao.EntityUtils;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.exception.DuplicateDataException;
import org.azrul.langkuik.security.role.EntityRight;
import org.azrul.langkuik.security.role.FieldState;
import org.azrul.langkuik.security.role.UserSecurityUtils;

public class DataTable<C> extends VerticalLayout {

    protected int currentTableIndex;
    protected Long bigTotal;
    protected String orderBy;
    protected boolean asc;
    protected Map<String, DataElementContainer> fieldsToBeDisplayedInTable;
    protected Table table;
    protected int itemCountPerPage;
    protected DataAccessObject<C> dao;
    protected WebEntityItemContainer<C> itemContainer;
    protected Label pageLabel;
    protected DAOQuery daoQuery;
    protected PageParameter pageParameter;
    protected String htmlTableLabel;

    public Map<String, DataElementContainer> getVisibleFields() {
        return fieldsToBeDisplayedInTable;
    }

    public Collection<C> getTableValues() {
        Collection<C> data = (Collection<C>) table.getValue();
        List<C> listData = new ArrayList(data);
        table.setValue(null);
        //this.table.select(null); //force vaadin to reset collection of selected fields
        Collections.reverse(listData);
        return listData;
    }

    public void refresh() {
        itemContainer.setBeans(dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage, UserSecurityUtils.getCurrentTenant()));
        itemContainer.refreshItems();
    }

    public boolean isEmpty() {
        return bigTotal == 0L;
    }

    public DataTable() {
        this.bigTotal = 0L;
        this.asc = true;
        this.table = new Table(null);
        this.table.setMultiSelect(false);
        this.table.setNullSelectionAllowed(true); //help with vaadin bug where reselecting second time will retain old data

        this.fieldsToBeDisplayedInTable = new HashMap<>();
    }

    protected void createTablePanel(final DAOQuery daoQuery,
            final Class<C> classOfBean,
            final DataAccessObject<C> dao,
            final int noBeansPerPage,
            /*final Set<String> currentUserRoles,*/
            final EntityRight entityOperation,
            final PageParameter pageParameter,
            final boolean doNotDrawIfEmpty,
            final String htmlTableLabel) {
        this.currentTableIndex = 0;
        this.itemCountPerPage = noBeansPerPage;
        this.dao = dao;
        this.daoQuery = daoQuery;
        this.pageParameter = pageParameter;
        this.htmlTableLabel = htmlTableLabel;

        Collection<C> allData = dao.runQuery(daoQuery, null, true, currentTableIndex, itemCountPerPage, UserSecurityUtils.getCurrentTenant());
        bigTotal = dao.countQueryResult(daoQuery, UserSecurityUtils.getCurrentTenant());

        boolean draw = true;
        if (doNotDrawIfEmpty == false && allData.isEmpty() == true) {
            draw = false;
        }

        if (draw == true) {
            BeanUtils beanUtils = new BeanUtils();

            //Page label
            pageLabel = new Label();

            //Collect all visible and searchable fields
            for (Field field : classOfBean.getDeclaredFields()) {
                if (field.isAnnotationPresent(WebField.class)) {
                    WebField webField = (WebField) field.getAnnotation(WebField.class);
                    if (webField.displayInTable() == true) {
                        fieldsToBeDisplayedInTable.put(field.getName(), new FieldContainer(webField, field));
                    }
                }
            }
            for (Method method : classOfBean.getDeclaredMethods()) {
                if (method.isAnnotationPresent(DerivedField.class)) {
                    DerivedField derivedField = (DerivedField) method.getAnnotation(DerivedField.class);
                    if (derivedField.displayInTable() == true) {
                        fieldsToBeDisplayedInTable.put(derivedField.name(), new DerivedFieldContainer(derivedField, method));
                    }
                }
            }

            //if we need to still draw when table is empty, we need to still put one element in there
            //Vaadin limitation
            if (allData.isEmpty()) {
                try {
                    allData.add(dao.createNew(false, UserSecurityUtils.getCurrentTenant()));
                } catch (DuplicateDataException ex) {
                    Notification.show(pageParameter.getResourceBundle().getString("dialog.duplicateData"), Notification.Type.WARNING_MESSAGE);

                }
            }

            //Put all data of type T into a table to be chosen
            itemContainer = new WebEntityItemContainer<>(classOfBean, allData);

            //iterate through columns
            Map<Double, String> orderByColumns = new TreeMap<>();
            Map<Double, String> columnNames = new TreeMap<>();

            Field idField = null;
            //int r = 0;

            //manage column names
            for (DataElementContainer fieldContainer : fieldsToBeDisplayedInTable.values()) {
                if (fieldContainer instanceof FieldContainer) {
                    Field field = ((FieldContainer) fieldContainer).getPojoField();
                    WebField webField = field.getAnnotation(WebField.class);
                    if (field.getType().isAssignableFrom(Collection.class)) {//only support simple Java objects
                        continue;
                    }
                    FieldState fieldState = beanUtils.calculateEffectiveFieldState(webField.userMap(), entityOperation);
                    if (fieldState.equals(FieldState.EDITABLE)
                            || fieldState.equals(FieldState.READ_ONLY)) {

                        //r = webField.rank();
                        if (EntityUtils.isManagedEntity(field.getType(), pageParameter.getEntityManagerFactory())) {
                            if (webField.allowNested() == true) {
                                EntityRight nestedEntityRight = UserSecurityUtils.getEntityRight(field.getType()/*, currentUserRoles*/);
                                if (nestedEntityRight != null) {
                                    for (Field nestedField : field.getType().getDeclaredFields()) {
                                        WebField nestedWebField = null;
                                        if (nestedField.isAnnotationPresent(WebField.class)) {
                                            nestedWebField = nestedField.getAnnotation(WebField.class);
                                        } else {
                                            continue;
                                        }
                                        if (nestedWebField.displayInTable() == false) {
                                            continue;
                                        }
                                        if (nestedField.getType().isAssignableFrom(Collection.class)) {//only support simple Java objects
                                            continue;
                                        }
                                        if (EntityUtils.isManagedEntity(nestedField.getType(), pageParameter.getEntityManagerFactory())) {
                                            continue; //cannot have nested of nested. Only 1 level of nested is allowed
                                        }

                                        FieldState nestedComponentState = beanUtils.calculateEffectiveFieldState(nestedWebField.userMap()/*, currentUserRoles*/, nestedEntityRight);
                                        if (nestedComponentState.equals(FieldState.EDITABLE)
                                                || nestedComponentState.equals(FieldState.READ_ONLY)) {
                                            orderByColumns.put(Double.valueOf((double) webField.rank())
                                                    + ((double) nestedWebField.rank() / 10000.0), field.getName() + "." + nestedField.getName());
                                            columnNames.put(Double.valueOf((double) webField.rank())
                                                    + ((double) nestedWebField.rank() / 10000.0), webField.nestedFieldPrefix() + " " + nestedWebField.name());
                                        }
                                    }
                                }
                            }
                        } else {
                            orderByColumns.put(Double.valueOf((double) webField.rank()), field.getName());
                            columnNames.put(Double.valueOf((double) webField.rank()), webField.name());

                            if (field.isAnnotationPresent(Id.class)) {
                                idField = field;
                            }
                        }
                    }
                } else if (fieldContainer instanceof DerivedFieldContainer) {
                    Method method = ((DerivedFieldContainer) fieldContainer).getPojoMethod();
                    DerivedField derivedField = method.getAnnotation(DerivedField.class);

                    FieldState fieldState = beanUtils.calculateEffectiveFieldState(derivedField.userMap(), entityOperation);
                    if (fieldState.equals(FieldState.EDITABLE)
                            || fieldState.equals(FieldState.READ_ONLY)) {
                        orderByColumns.put(Double.valueOf((double) derivedField.rank()), derivedField.name());
                        columnNames.put(Double.valueOf((double) derivedField.rank()), derivedField.name());
                        table.addGeneratedColumn(derivedField.name(), new Table.ColumnGenerator() {
                            public Component generateCell(Table source, Object itemId, Object columnId) {
                                try {
                                    return new Label((String) method.invoke(itemId));
                                } catch (IllegalAccessException ex) {
                                    Logger.getLogger(DataTable.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (IllegalArgumentException ex) {
                                    Logger.getLogger(DataTable.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (InvocationTargetException ex) {
                                    Logger.getLogger(DataTable.class.getName()).log(Level.SEVERE, null, ex);
                                } 
                                return new Label();
                                

                            }
                        });
                    }
                }
            }

            //if orderby == null, use the column with smallest rank (the first) as orderby column
            if (orderBy == null) {
                for (Map.Entry<Double, String> o : orderByColumns.entrySet()) {
                    if (!o.getValue().contains(".")) {
                        orderBy = orderByColumns.entrySet().iterator().next().getValue();
                        break;
                    }
                }
            }

            //Construct table for choice data
            table.setContainerDataSource(itemContainer);

            //set ID converter
            if (idField != null) {
                if (Number.class.isAssignableFrom(idField.getType())) {
                    table.setConverter(idField.getName(), new NumberBasedIDConverter((Class<Number>) idField.getType()));
                }
            }

            //table add derived data if any
            itemContainer.refreshItems();

            table.setHeight(100f, Sizeable.Unit.PERCENTAGE);
            table.setVisibleColumns(orderByColumns.values().toArray());
            table.setColumnHeaders(columnNames.values().toArray(new String[]{""}));
            table.setImmediate(true);
            table.setSelectable(true);
            table.setMultiSelect(true);
            table.setPageLength(itemCountPerPage);

            //create navigation button
            int allDataLastPage = (int) Math.floor(bigTotal / itemCountPerPage);
            if (bigTotal % itemCountPerPage == 0) {
                allDataLastPage--;
            }
            int allDataCurrentUpdatedPage = currentTableIndex / itemCountPerPage;

            //add title to table
            if (this.htmlTableLabel != null) {
                pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (allDataCurrentUpdatedPage + 1), (allDataLastPage + 1)));
                Label htmlLabel = new Label("<ul><li><b>" + this.htmlTableLabel + "</b></li></ul>", ContentMode.HTML);
                this.addComponent(htmlLabel);
            }

            //put table in form
            this.addComponent(table);
            HorizontalLayout allDataTableNav = new HorizontalLayout();

            Button firstPageBtn = new Button("<<", new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event
                ) {
                    int lastPage = (int) Math.floor(bigTotal / itemCountPerPage);
                    if (bigTotal % itemCountPerPage == 0) {
                        lastPage--;
                    }

                    if (currentTableIndex > 0) {
                        currentTableIndex = 0;
                        itemContainer.setBeans(dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage, UserSecurityUtils.getCurrentTenant()));
                        itemContainer.refreshItems();
                        table.setPageLength(itemCountPerPage);
                    }
                    int currentUpdatedPage = currentTableIndex / itemCountPerPage;
                    pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (currentUpdatedPage + 1), (lastPage + 1)));
                }
            }
            );
            allDataTableNav.addComponent(firstPageBtn);

            Button leftPageBtn = new Button("<", new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event
                ) {
                    int lastPage = (int) Math.floor(bigTotal / itemCountPerPage);
                    if (bigTotal % itemCountPerPage == 0) {
                        lastPage--;
                    }
                    if (currentTableIndex > 0) {
                        currentTableIndex -= itemCountPerPage;
                        itemContainer.setBeans(dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage, UserSecurityUtils.getCurrentTenant()));
                        itemContainer.refreshItems();
                        table.setPageLength(itemCountPerPage);
                    }
                    int currentUpdatedPage = currentTableIndex / itemCountPerPage;
                    pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (currentUpdatedPage + 1), (lastPage + 1)));
                }
            }
            );
            allDataTableNav.addComponent(leftPageBtn);
            allDataTableNav.addComponent(pageLabel);

            Button rightPageBtn
                    = new Button(">", new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event
                        ) {
                            int lastPage = (int) Math.floor(bigTotal / itemCountPerPage);
                            if (bigTotal % itemCountPerPage == 0) {
                                lastPage--;
                            }
                            int currentPage = currentTableIndex / itemCountPerPage;
                            if (currentPage < lastPage) {
                                currentTableIndex += itemCountPerPage;
                                itemContainer.setBeans(dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage, UserSecurityUtils.getCurrentTenant()));
                                itemContainer.refreshItems();
                                table.setPageLength(itemCountPerPage);
                            }
                            int currentUpdatedPage = currentTableIndex / itemCountPerPage;
                            pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (currentUpdatedPage + 1), (lastPage + 1)));
                        }
                    }
                    );
            allDataTableNav.addComponent(rightPageBtn);

            Button lastPageBtn = new Button(">>", new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event
                ) {
                    int lastPage = (int) Math.floor(bigTotal / itemCountPerPage);
                    if (bigTotal % itemCountPerPage == 0) {
                        lastPage--;
                    }
                    int currentPage = currentTableIndex / itemCountPerPage;
                    if (currentPage < lastPage) {
                        currentTableIndex = lastPage * itemCountPerPage;
                        itemContainer.setBeans(dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage, UserSecurityUtils.getCurrentTenant()));
                        itemContainer.refreshItems();
                        table.setPageLength(itemCountPerPage);
                    }
                    pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (lastPage + 1), (lastPage + 1)));
                }
            }
            );
            allDataTableNav.addComponent(lastPageBtn);

            this.addComponent(allDataTableNav);

            table.addHeaderClickListener(new Table.HeaderClickListener() {

                @Override
                public void headerClick(Table.HeaderClickEvent event
                ) {

                    String column = (String) event.getPropertyId();
                    if (orderBy.equals(column)) {
                        asc = !asc;
                    }
                    orderBy = column;

                    Collection<C> tableData = dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage, UserSecurityUtils.getCurrentTenant());
                    if (tableData.isEmpty()) {
                        try {
                            tableData.add(dao.createNew(UserSecurityUtils.getCurrentTenant()));
                        } catch (DuplicateDataException ex) {
                            Notification.show(pageParameter.getResourceBundle().getString("dialog.duplicateData"), Notification.Type.WARNING_MESSAGE);

                        }
                    }
                    itemContainer.setBeans(tableData);
                    itemContainer.refreshItems();
                    table.setPageLength(itemCountPerPage);
                    //totalTableData = dao.countSearch(null, classOfBean);
                }
            }
            );

            // Disable the default sorting behavior
            table.setSortEnabled(false);

            //Implement formatter
            for (Map.Entry<String, DataElementContainer> entry : fieldsToBeDisplayedInTable.entrySet()) {
                if (entry.getValue() instanceof FieldContainer) {
                    FieldContainer fieldContainer = (FieldContainer) entry.getValue();
                    Field field = fieldContainer.getPojoField();
                    String fieldName = entry.getKey();
                    if (field.getType().equals(Date.class)) {
                        StringToDateConverter stringToDateConv = new StringToDateConverter() {
                            @Override
                            public DateFormat getFormat(Locale locale) {
                                return new SimpleDateFormat(pageParameter.getConfig().get("dateFormat"));
                            }
                        };
                        table.setConverter(fieldName, stringToDateConv);
                    } else if (field.isAnnotationPresent(WebField.class)) {
                        final WebField myf = field.getAnnotation(WebField.class);
                        if (myf.choices().length > 0) {
                            final Map<Object, Object> modelToPresentationMap = new HashMap<>();
                            final Map<Object, Object> presentationToModelMap = new HashMap<>();
                            for (Choice choice : myf.choices()) {
                                if (choice.value() == -1) {
                                    modelToPresentationMap.put(choice.textValue(), choice.display());
                                    presentationToModelMap.put(choice.display(), choice.textValue());
                                } else {
                                    modelToPresentationMap.put(choice.value(), choice.display());
                                    presentationToModelMap.put(choice.display(), choice.value());
                                }
                            }

                            Converter<String, Object> choicesConverter = new Converter<String, Object>() {

                                @Override
                                public Object convertToModel(String value, Class<? extends Object> targetType, Locale locale) throws Converter.ConversionException {
                                    return presentationToModelMap.get(value);
                                }

                                @Override
                                public String convertToPresentation(Object value, Class<? extends String> targetType, Locale locale) throws Converter.ConversionException {
                                    return (String) modelToPresentationMap.get(value);
                                }

                                @Override
                                public Class<Object> getModelType() {
                                    return (Class<Object>) (new Object()).getClass();
                                }

                                @Override
                                public Class<String> getPresentationType() {
                                    return (Class<String>) "".getClass();
                                }
                            };
                            table.setConverter(fieldName, choicesConverter);
                        }
                    }
                }
            }
        }
    }

    protected void createTablePanel(final DAOQuery parameter,
            final Class<C> classOfBean,
            final DataAccessObject<C> dao,
            final int noBeansPerPage,
            /*final Set<String> currentUserRoles,*/
            final EntityRight entityRight,
            final PageParameter pageParameter) {
        createTablePanel(parameter,
                classOfBean,
                dao,
                noBeansPerPage,
                /*currentUserRoles,*/
                entityRight,
                pageParameter,
                true,
                null);
    }

}
