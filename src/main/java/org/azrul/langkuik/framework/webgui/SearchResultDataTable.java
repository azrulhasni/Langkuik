/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.webgui;

import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Notification;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.azrul.langkuik.annotations.Choice;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.configs.Configuration;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.dao.FindAnyEntityQuery;
import org.azrul.langkuik.dao.SearchTerm;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.activechoice.ActiveChoiceEnum;
import org.azrul.langkuik.framework.activechoice.ActiveChoiceTarget;
import org.azrul.langkuik.framework.activechoice.ActiveChoiceUtils;
import org.azrul.langkuik.framework.activechoice.EmptyEnum;
import org.azrul.langkuik.framework.exception.DuplicateDataException;
import org.azrul.langkuik.framework.exception.EntityIsUsedException;
import org.azrul.langkuik.security.role.EntityRight;
import org.azrul.langkuik.security.role.UserSecurityUtils;

/**
 *
 * @author azrulm8
 */
public class SearchResultDataTable<C> extends DataTable<C> {

    public SearchResultDataTable(FindAnyEntityQuery<C> parameter, Class<C> classOfBean, DataAccessObject<C> dao, int noBeansPerPage,/*final Set<String> currentUserRoles,*/ final EntityRight entityRight, final PageParameter pageParameter) {
        this.daoQuery = parameter;
        createSearchPanel(classOfBean, /*currentUserRoles,*/ entityRight, pageParameter);
        createTablePanel(parameter, classOfBean, dao, noBeansPerPage, /*currentUserRoles,*/ entityRight, pageParameter);
    }

    protected void createSearchPanel(final Class<C> classOfBean, /*final Set<String> currentUserRoles,*/ final EntityRight entityRight, final PageParameter pageParameter) throws UnsupportedOperationException, SecurityException, FieldGroup.BindException {
        final BeanFieldGroup fieldGroup = new BeanFieldGroup(classOfBean);
        final FindAnyEntityQuery searchQuery = (FindAnyEntityQuery) daoQuery;

        //collect all activechoices
        Map<com.vaadin.ui.ComboBox, ActiveChoiceTarget> activeChoicesWithFieldAsKey = new HashMap<>();
        Map<String, com.vaadin.ui.ComboBox> activeChoicesFieldWithHierarchyAsKey = new HashMap<>();

        BeanUtils beanUtils = new BeanUtils();
        Map<Integer, DataElementContainer> fieldContainers = beanUtils.getOrderedFieldsByRank(classOfBean);

        final Map<Integer, com.vaadin.ui.Field> searchableFieldsByRank = new TreeMap<>();
        final Map<String, com.vaadin.ui.Field> searchableFieldsByName = new TreeMap<>();

        //Construct search form
        for (DataElementContainer elementContainer : fieldContainers.values()) {
            if (elementContainer instanceof DerivedFieldContainer) {
                continue;
            }
            FieldContainer fieldContainer = (FieldContainer) elementContainer;
            Field pojoField = fieldContainer.getPojoField();
            WebField webField = fieldContainer.getWebField();

            if (pojoField.isAnnotationPresent(org.hibernate.search.annotations.Field.class)) {
                if (webField.choices().length > 0) {
                    //deal with choices

                    com.vaadin.ui.ComboBox searchComboBox = new com.vaadin.ui.ComboBox(webField.name());
                    searchComboBox.setFilteringMode(FilteringMode.STARTSWITH);

                    searchComboBox.setImmediate(true);
                    fieldGroup.bind(searchComboBox, pojoField.getName());
                    for (Choice choice : webField.choices()) {
                        if (choice.value() == -1) {
                            searchComboBox.addItem(choice.textValue());
                            searchComboBox.setItemCaption(choice.textValue(), choice.display());
                        } else {
                            searchComboBox.addItem(choice.value());
                            searchComboBox.setItemCaption(choice.value(), choice.display());
                        }
                    }

                    //allDataSearchForm.addComponent(searchComboBox);
                    searchableFieldsByRank.put(webField.rank(), searchComboBox);
                    searchableFieldsByName.put(pojoField.getName(), searchComboBox);
                } else if (webField.activeChoice().enumTree() != EmptyEnum.class) {
                    //collect active choices - 
                    com.vaadin.ui.ComboBox searchComboBox = new com.vaadin.ui.ComboBox(webField.name());
                    searchComboBox.setFilteringMode(FilteringMode.STARTSWITH);
                    searchComboBox.setImmediate(true);
                    fieldGroup.bind(searchComboBox, pojoField.getName());
                    String hierarchy = webField.activeChoice().hierarchy();
                    Class<ActiveChoiceEnum> enumTree = (Class<ActiveChoiceEnum>) webField.activeChoice().enumTree();
                    ActiveChoiceTarget activeChoiceTarget = ActiveChoiceUtils.build(enumTree, hierarchy);
                    for (String choice : activeChoiceTarget.getSourceChoices()) {
                        searchComboBox.addItem(choice);
                        activeChoicesWithFieldAsKey.put(searchComboBox, activeChoiceTarget);
                        activeChoicesFieldWithHierarchyAsKey.put(hierarchy, searchComboBox);
                    }
                    searchableFieldsByRank.put(webField.rank(), searchComboBox);
                    searchableFieldsByName.put(pojoField.getName(), searchComboBox);

                } else {
                    com.vaadin.ui.Field searchField = fieldGroup.buildAndBind(webField.name(), pojoField.getName());
                    if (pojoField.getType().equals(Date.class)) {
                        DateField dateField = (DateField) searchField;
                        dateField.setDateFormat(pageParameter.getConfig().get("dateFormat"));
                        dateField.setWidth(100f, Sizeable.Unit.PIXELS);
                    }

                    //allDataSearchForm.addComponent(searchField);
                    searchableFieldsByRank.put(webField.rank(), searchField);
                    searchableFieldsByName.put(pojoField.getName(), searchField);
                }
            }

        }

        //build form
        int rowCount = (int) (Math.ceil(searchableFieldsByRank.size() / 2));
        rowCount = rowCount < 1 ? 1 : rowCount;
        GridLayout allDataSearchForm = new GridLayout(2, rowCount);
        allDataSearchForm.setSpacing(true);
        for (com.vaadin.ui.Field searchField : searchableFieldsByRank.values()) {
            allDataSearchForm.addComponent(searchField);
            searchField.setId(searchField.getCaption());
        }

        this.addComponent(allDataSearchForm);

        //deal with active choice
        for (final com.vaadin.ui.ComboBox sourceField : activeChoicesWithFieldAsKey.keySet()) {
            final ActiveChoiceTarget target = activeChoicesWithFieldAsKey.get(sourceField);
            final com.vaadin.ui.ComboBox targetField = activeChoicesFieldWithHierarchyAsKey.get(target.getTargetHierarchy());
            sourceField.addValueChangeListener(new Property.ValueChangeListener() {
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

        //search button
        Button searchBtn = new Button(pageParameter.getLocalisedText("form.general.button.search"), new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                //reset previous searches
                searchQuery.clearSearchTerms();
                //read search terms from form
                try {
                    for (Map.Entry<String, com.vaadin.ui.Field> entry : searchableFieldsByName.entrySet()) {
                        com.vaadin.ui.Field searchField = entry.getValue();
                        if (searchField.getValue() != null) {
                            if (searchField.getValue() instanceof String) {
                                String searchTerm = ((String) searchField.getValue()).trim();
                                if (!"".equals(searchTerm)) {
                                    searchQuery.addSearchTerm(new SearchTerm(entry.getKey(), classOfBean.getDeclaredField(entry.getKey()), searchField.getValue()));
                                }
                            } else {
                                searchQuery.addSearchTerm(new SearchTerm(entry.getKey(), classOfBean.getDeclaredField(entry.getKey()), searchField.getValue()));
                            }
                        }
                    }
                } catch (NoSuchFieldException | SecurityException ex) {
                    Logger.getLogger(SearchResultDataTable.class.getName()).log(Level.SEVERE, null, ex);
                }
                //do query
                Collection<C> allData = dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage, UserSecurityUtils.getCurrentTenant());
                if (allData.isEmpty()) {
                    allData = new ArrayList<>();
                    try {
                        allData.add(dao.createNew(UserSecurityUtils.getCurrentTenant()));
                        bigTotal = dao.countQueryResult(daoQuery, UserSecurityUtils.getCurrentTenant());
                        itemContainer.setBeans(allData);
                        itemContainer.refreshItems();
                        table.setPageLength(itemCountPerPage);
                        table.setPageLength(itemCountPerPage);
                        currentTableIndex = 0;
                        int lastPage = (int) Math.floor(bigTotal / itemCountPerPage);
                        if (bigTotal % itemCountPerPage == 0) {
                            lastPage--;
                        }
                        int currentUpdatedPage = currentTableIndex / itemCountPerPage;
                        pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (currentUpdatedPage + 1), (lastPage + 1)));
                    } catch (DuplicateDataException ex) {
                        Notification.show(pageParameter.getResourceBundle().getString("dialog.duplicateData"), Notification.Type.WARNING_MESSAGE);

                        Logger.getLogger(SearchResultDataTable.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        });
        searchBtn.setId(searchBtn.getCaption());
        this.addComponent(searchBtn);
    }

    public void deleteEntities(Collection<C> currentBeans) throws EntityIsUsedException {

        dao.delete(currentBeans);
        Collection<C> data = dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage, UserSecurityUtils.getCurrentTenant());
        if (data.isEmpty()) {
            data = new ArrayList<>();
            try {
                data.add(dao.createNew(UserSecurityUtils.getCurrentTenant()));
                itemContainer.setBeans(data);
                itemContainer.refreshItems();
                bigTotal = dao.countQueryResult(daoQuery, UserSecurityUtils.getCurrentTenant());

                int lastPage = (int) Math.floor(bigTotal / itemCountPerPage);
                if (bigTotal % itemCountPerPage == 0) {
                    lastPage--;
                }
                int currentUpdatedPage = currentTableIndex / itemCountPerPage;
                pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (currentUpdatedPage + 1), (lastPage + 1)));
                table.setPageLength(itemCountPerPage);
            } catch (DuplicateDataException ex) {
                Notification.show(pageParameter.getResourceBundle().getString("dialog.duplicateData"), Notification.Type.WARNING_MESSAGE);

                Logger.getLogger(SearchResultDataTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
