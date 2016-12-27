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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.persistence.Id;
import org.azrul.langkuik.annotations.Choice;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.dao.DAOQuery;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.dao.EntityUtils;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.security.role.EntityOperation;
import org.azrul.langkuik.security.role.FieldState;
import org.azrul.langkuik.security.role.UserSecurityUtils;

public class DataTable<C> extends VerticalLayout {

    protected int currentTableIndex;
    protected Long bigTotal;
    protected String orderBy;
    protected boolean asc;
    protected Map<String, Field> fieldsToBeDisplayedInTable;
    protected Table table;
    protected int itemCountPerPage;
    protected DataAccessObject<C> dao;
    protected WebEntityItemContainer<C> itemContainer;
    protected Label pageLabel;
    protected DAOQuery daoQuery;
    protected PageParameter pageParameter;
    protected String htmlTableLabel;

    public Map<String, Field> getVisibleFields() {
        return fieldsToBeDisplayedInTable;
    }

    public Collection<C> getTableValues() {
        return (Collection<C>) table.getValue();
    }
    
    public void refresh(){
       itemContainer.setBeans(dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage,UserSecurityUtils.getCurrentTenant()));
       itemContainer.refreshItems();
    }
    
    public boolean isEmpty(){
        return bigTotal==0L;
    }

    public DataTable() {
        this.bigTotal = 0L;
        this.asc = true;
        this.table = new Table(null);
        this.fieldsToBeDisplayedInTable = new HashMap<>();
    }

    protected void createTablePanel(final DAOQuery daoQuery,
            final Class<C> classOfBean,
            final DataAccessObject<C> dao,
            final int noBeansPerPage,
            /*final Set<String> currentUserRoles,*/
            final EntityOperation entityOperation,
            final PageParameter pageParameter,
            final boolean doNotDrawIfEmpty,
            final String htmlTableLabel) {
        this.currentTableIndex = 0;
        this.itemCountPerPage = noBeansPerPage;
        this.dao = dao;
        this.daoQuery = daoQuery;
        this.pageParameter = pageParameter;
        this.htmlTableLabel = htmlTableLabel;

        Collection<C> allData = dao.runQuery(daoQuery, null, true, currentTableIndex, itemCountPerPage,UserSecurityUtils.getCurrentTenant());
        bigTotal = dao.countQueryResult(daoQuery,UserSecurityUtils.getCurrentTenant());

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
                        fieldsToBeDisplayedInTable.put(field.getName(), field);
                    }
                }
            }
            //if we need to still draw when table is empty, we need to still put one element in there
            //Vaadin limitation
            if (allData.isEmpty()) {
                allData.add(dao.createNew(false,UserSecurityUtils.getCurrentTenant()));
            }

            //Put all data of type T into a table to be chosen
            itemContainer = new WebEntityItemContainer<>(classOfBean, allData);

            //iterate through columns
            Map<Double, String> orderByColumns = new TreeMap<>();
            Map<Double, String> columnNames = new TreeMap<>();
            
            Field idField = null;
            //int r = 0;
            for (Field field : fieldsToBeDisplayedInTable.values()) {
                WebField webField = field.getAnnotation(WebField.class);
                if (field.getType().isAssignableFrom(Collection.class)) {//only support simple Java objects
                    continue;
                }
                FieldState fieldState = beanUtils.calculateEffectiveFieldState(field/*, currentUserRoles*/, entityOperation);
                if (fieldState.equals(FieldState.EDITABLE)
                        || fieldState.equals(FieldState.READ_ONLY)) {

                    //r = webField.rank();
                    if (EntityUtils.isManagedEntity(field.getType(), pageParameter.getEntityManagerFactory())) {
                        if (webField.allowNested() == true) {
                            EntityOperation nestedEntityRight = UserSecurityUtils.getEntityRight(field.getType()/*, currentUserRoles*/);
                            if (nestedEntityRight != null) {
                                for (Field nestedField : field.getType().getDeclaredFields()) {
                                    WebField nestedWebField = null;
                                    if (nestedField.isAnnotationPresent(WebField.class)) {
                                        nestedWebField = nestedField.getAnnotation(WebField.class);
                                    } else {
                                        continue;
                                    }
                                    if (nestedWebField.displayInTable()==false){
                                        continue;
                                    }
                                    if (nestedField.getType().isAssignableFrom(Collection.class)) {//only support simple Java objects
                                        continue;
                                    }
                                    if (EntityUtils.isManagedEntity(nestedField.getType(), pageParameter.getEntityManagerFactory())) {
                                        continue; //cannot have nested of nested. Only 1 level of nested is allowed
                                    }

                                    FieldState nestedComponentState = beanUtils.calculateEffectiveFieldState(nestedField/*, currentUserRoles*/, nestedEntityRight);
                                    if (nestedComponentState.equals(FieldState.EDITABLE)
                                            || nestedComponentState.equals(FieldState.READ_ONLY)) {
                                        orderByColumns.put(Double.valueOf((double) webField.rank())
                                                + ((double)nestedWebField.rank() / 10000.0), field.getName() + "." + nestedField.getName());
                                        columnNames.put(Double.valueOf((double) webField.rank())
                                                + ((double)nestedWebField.rank() / 10000.0), webField.nestedFieldPrefix()+" "+nestedWebField.name());
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
            }

            //if orderby == null, use the column with smallest rank (the first) as orderby column
            if (orderBy==null){
                for (Map.Entry<Double,String> o:orderByColumns.entrySet()){
                    if (!o.getValue().contains(".")){
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
            if (this.htmlTableLabel!=null){
                pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (allDataCurrentUpdatedPage + 1), (allDataLastPage + 1)));
                Label htmlLabel = new Label("<ul><li><b>"+this.htmlTableLabel+"</b></li></ul>",ContentMode.HTML);
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
                        itemContainer.setBeans(dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage,UserSecurityUtils.getCurrentTenant()));
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
                        itemContainer.setBeans(dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage,UserSecurityUtils.getCurrentTenant()));
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
                                itemContainer.setBeans(dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage,UserSecurityUtils.getCurrentTenant()));
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
                        itemContainer.setBeans(dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage,UserSecurityUtils.getCurrentTenant()));
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

                            Collection<C> tableData = dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage,UserSecurityUtils.getCurrentTenant());
                            if (tableData.isEmpty()) {
                                tableData.add(dao.createNew(UserSecurityUtils.getCurrentTenant()));
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

            for (Map.Entry<String, Field> entry : fieldsToBeDisplayedInTable.entrySet()) {
                Field field = entry.getValue();
                String fieldName = entry.getKey();
                if (field.getType().equals(Date.class)) {
                    StringToDateConverter stringToDateConv = new StringToDateConverter() {
                        @Override
                        public DateFormat getFormat(Locale locale) {
                            return new SimpleDateFormat(pageParameter.getConfig().get("dateFormat"));
                        }
                    };
                    table.setConverter(fieldName, stringToDateConv);
                } else {
                    if (field.isAnnotationPresent(WebField.class)) {
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
            final EntityOperation entityRight,
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
