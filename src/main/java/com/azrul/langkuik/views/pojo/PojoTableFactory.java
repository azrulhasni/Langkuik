package com.azrul.langkuik.views.pojo;

import com.azrul.langkuik.custom.attachment.Attachment;
import com.azrul.langkuik.framework.dao.DataAccessObject;
import com.azrul.langkuik.framework.dao.FindRelationParameter;
import com.azrul.langkuik.framework.dao.FindRelationQuery;
import com.azrul.langkuik.framework.field.FieldContainer;
import com.azrul.langkuik.framework.field.FieldUtils;
import com.azrul.langkuik.framework.relationship.RelationMemento;
import com.azrul.langkuik.framework.relationship.RelationUtils;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import com.azrul.langkuik.framework.standard.Status;
import com.azrul.langkuik.views.table.PageNav;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.beans.IntrospectionException;
import java.sql.Blob;
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
import java.util.stream.Stream;
import org.apache.commons.collections4.MultiValuedMap;
import java.util.Optional;
import javax.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class PojoTableFactory {
    
    @Autowired
    private DataAccessObject dao;

    @Autowired
    ValidatorFactory validatorFactory;
    
    @Value("${application.lgDateFormat}")
    private String dateFormat;
    
    @Value("${application.lgFullurl}")
    private String fullUrl;
    
    public PojoTableFactory(){
        
    }

    public <T, C> void createTable(T parentBean,/*getCurrentBean()*/
            String relationName,
            String humanReadableRelationshipName,
            VerticalLayout layout,
            Map<String, RelationMemento> relationMementos,
            Component buttonLayout,
            ComponentEventListener<ItemClickEvent<C>> rowItemClickListener) {
        
        createTable(parentBean,
                relationName,
                humanReadableRelationshipName,
                layout,
                relationMementos,
                buttonLayout,
                rowItemClickListener,
                150);
    }

    public <T, C> void createTable(T parentBean,
            String relationName,
            String humanReadableRelationshipName,
            VerticalLayout layout,
            Map<String, RelationMemento> relationMementos,
            Component buttonLayout,
            ComponentEventListener<ItemClickEvent<C>> rowItemClickListener,
            Integer heightInPixel) {
        
        String tenant = (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");
        Class<T> parentClass = (Class<T>) parentBean.getClass();
        Class<C> childClass = RelationUtils.getRelationClass(parentClass, relationName);
        Map<String, Grid.Column<?>> columns = new HashMap<>();
        FindRelationParameter<T, C> param = new FindRelationParameter(parentBean,
                relationName);
        FindRelationQuery searchQuery = new FindRelationQuery(param);

        Long count = dao.countQueryResult(searchQuery, Optional.of(tenant),Optional.empty());
        int maxPageCount = Long.valueOf(-Math.floorDiv(-count, PojoConst.ELEMENTS_PER_PAGE)).intValue();


            Grid<C> grid = new Grid<>();
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
            grid.setHeight(heightInPixel.toString() + "px");
            grid.setMultiSort(false);
            grid.setPageSize(PojoConst.ELEMENTS_PER_PAGE);
        
            DataProvider dataProvider = null;

            MultiValuedMap<Integer, FieldContainer> fieldStore = FieldUtils.getFieldsByOrder(childClass);

            //sort fields by order
            List<Integer> orders = new ArrayList<>(fieldStore.keySet());
            Collections.sort(orders);

            //Add status icons
            grid.addComponentColumn(source -> {
                if (LangkuikExt.class.isAssignableFrom(source.getClass())) {
                    Icon icon = null;
                    LangkuikExt ext = (LangkuikExt) source;
                    if (Status.DRAFT.equals(ext.getStatus())) {
                        icon = new Icon(VaadinIcon.EDIT);
                    } else if (Status.IN_PROGRESS.equals(ext.getStatus())) {
                        icon = new Icon(VaadinIcon.ARROW_FORWARD);
                    } else {
                        icon = new Icon(VaadinIcon.CHECK_SQUARE_O);
                    }
                    icon.setSize("0.8em");
                    Div div = new Div();
                    div.add(icon);
                    div.setTitle(ext.getStatus().toString());
                    return div;

                } else {
                    return new Label("");
                }
            }).setFrozen(true).setAutoWidth(true);

            //Add fields to grid
            for (Integer order : orders) {
                for (FieldContainer fc : fieldStore.get(order)) {

                    //Get date format (for dates)
                    final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

                    //Only display what is visible
                    if (fc.getWebField().visibleInTable() == true) {
                        if (Blob.class.equals(fc.getReturnType())) {
                            //Handle attachment
                            Grid.Column<?> column = grid.addComponentColumn(source -> {
                                Attachment attachment = (Attachment) source;
                                if (attachment.getFileName() == null) {
                                    return new Label();
                                }
                                Anchor download = new Anchor(new StreamResource(attachment.getFileName(), dao.getInputStreamFactory(attachment)), "");
                                download.getElement().setAttribute("download", true);
                                download.add(new Button(new Icon(VaadinIcon.DOWNLOAD_ALT)));
                                return download;
                            }
                            );
                            columns.put(fc.getField().getName(), column);

                        } else {
                            //Add column
                            Grid.Column<?> column = grid.addColumn(source -> {
                                if (fc.getValue(source) != null) {
                                    if (LocalDate.class.equals(fc.getReturnType())) {
                                        Date date = Date.from(((LocalDate) fc.getValue(source)).atStartOfDay(ZoneId.systemDefault()).toInstant());
                                        return sdf.format(date);
                                    } else {
                                        return fc.getValue(source).toString();
                                    }
                                }
                                return "";
                                //}
                            }).setHeader(fc.getWebField().displayName())
                                    .setSortable(fc.isSortable())
                                    .setSortProperty(fc.getField().getName());
                            columns.put(fc.getField().getName(), column);
                        }
                    }
                }
            }

            //make grid clickable
            grid.addItemClickListener(rowItemClickListener);

            //Page navigation
            PageNav pageNav = createPageNav(relationName, maxPageCount, grid, relationMementos);
            pageNav.setWidthFull();

            //At this stage, if no relation mementos is present represeting this relationship in the relationMementos map, add one
            if (relationMementos.get(relationName) == null) {
                RelationMemento memento = new RelationMemento(grid, pageNav, 1, maxPageCount);
                relationMementos.put(relationName, memento);
            }

            //Add data or not
            if (count > 0) {
                dataProvider = createDataProvider(searchQuery,
                        relationMementos.get(relationName));
                grid.setDataProvider(dataProvider);
            }

            //Put all together
            Div wrapper = new Div();
            wrapper.setId("wrapper");
            //Style set = wrapper.getStyle().set("border","1px solid red");
            wrapper.setWidthFull();
            wrapper.add(new Label(humanReadableRelationshipName), buttonLayout, pageNav, grid);
            layout.add(wrapper);

    }

    public <T, C> DataProvider<C, Void> createDataProvider(FindRelationQuery searchQuery,
            RelationMemento relationMemento) {
        String tenant = (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");
        DataProvider<C, Void> dataProvider
                = DataProvider.fromCallbacks(// Firstly, callback fetches items based on a query
                        (var query) -> {
                            // Query.getOffSet must be call
                            query.getOffset();
                            Integer offset = 0;
                            if (relationMemento != null) {
                                offset = (relationMemento.getPage() - 1) * PojoConst.ELEMENTS_PER_PAGE;//query.getOffset();
                            }// The number of items to load
                            Integer limit = query.getLimit();
                            Collection<C> data = new ArrayList<>();
                            if (query.getSortOrders().isEmpty()) {
                                data = dao.runQuery(searchQuery,Optional.empty(), Optional.of(Boolean.FALSE), Optional.of(offset), Optional.of(limit), Optional.of(tenant), Optional.empty());
                            } else {
                                QuerySortOrder sort = query.getSortOrders().iterator().next(); //we only support single column sorting
                                //sortColumn = sort.getSorted();
                                //sortDirection = sort.getDirection();
                                data = dao.runQuery(searchQuery, Optional.of(sort.getSorted()),
                                        Optional.of(SortDirection.ASCENDING.equals(sort.getDirection())), Optional.of(offset) , Optional.of(limit), Optional.of(tenant), Optional.empty());
                            }
                            if (data.isEmpty()) {
                                return Stream.<C>empty();
                            } else {
                                return data.stream();
                            }
                        },
                        // Secondly, callback fetches the number of items
                        // for a query
                        query -> {
                            Long count = dao.countQueryResult(searchQuery, Optional.of(tenant),Optional.empty());
                            Integer result = count.intValue();
                            if (count >= PojoConst.ELEMENTS_PER_PAGE) {
                                Integer finalPage = Long.valueOf(-Math.floorDiv(-count, PojoConst.ELEMENTS_PER_PAGE)).intValue();
                                Integer currentPage = 0;
                                if (relationMemento != null) {
                                    currentPage = relationMemento.getPage();
                                }
                                if (currentPage.equals(finalPage)) {
                                    Integer count4ThisPage = Long.valueOf(count % PojoConst.ELEMENTS_PER_PAGE).intValue();
                                    if (count4ThisPage.equals(0)) {
                                        result = PojoConst.ELEMENTS_PER_PAGE;
                                    } else {
                                        result = count4ThisPage;
                                    }
                                } else {
                                    result = PojoConst.ELEMENTS_PER_PAGE;
                                }
                            } else {
                                result = count.intValue();
                            }
                            return result;
                            //return result + (result - result%PojoConst.ELEMENTS_PER_PAGE);
                        });
        return dataProvider;
    }

    private <C> PageNav createPageNav(String relationName,
            int maxPageCount, 
            Grid<C> grid, 
            Map<String, RelationMemento> relationMementos) {
        
        PageNav pageNav = new PageNav();
        pageNav.setPage(1, maxPageCount);
        pageNav.getFirstPage().addClickListener(e -> {
            relationMementos.get(relationName).setPage(1);
            pageNav.setPage(relationMementos.get(relationName).getPage(), maxPageCount);
            grid.getDataProvider().refreshAll();
        });
        pageNav.getFinalPage().addClickListener(e -> {
            if (relationMementos.get(relationName).getPage() < relationMementos.get(relationName).getMaxPageCount()) {
                relationMementos.get(relationName).setPage(relationMementos.get(relationName).getMaxPageCount());
                pageNav.setPage(relationMementos.get(relationName).getPage(), relationMementos.get(relationName).getMaxPageCount());
                grid.getDataProvider().refreshAll();
            }
        });
        pageNav.getNextPage().addClickListener(e -> {
            if (relationMementos.get(relationName).getPage() < relationMementos.get(relationName).getMaxPageCount()) {
                relationMementos.get(relationName).setPage(relationMementos.get(relationName).getPage() + 1);
                pageNav.setPage(relationMementos.get(relationName).getPage(), relationMementos.get(relationName).getMaxPageCount());
                grid.getDataProvider().refreshAll();
            }
        });
        pageNav.getLastPage().addClickListener(e -> {
            if (relationMementos.get(relationName).getPage() > 1) {
                relationMementos.get(relationName).setPage(relationMementos.get(relationName).getPage() - 1);
                pageNav.setPage(relationMementos.get(relationName).getPage(), relationMementos.get(relationName).getMaxPageCount());
                grid.getDataProvider().refreshAll();
            }
        });
        return pageNav;
    }

    public  <T, C> void redrawTables(T parentBean, String relationName, RelationMemento relationMemento) {
        String tenant = (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");
        FindRelationParameter<T, C> param = new FindRelationParameter(parentBean, relationName);
        FindRelationQuery searchQuery = new FindRelationQuery(param);

        Long count = dao.countQueryResult(searchQuery,Optional.of(tenant), Optional.empty());
        int maxPageCount = Long.valueOf(-Math.floorDiv(-count, PojoConst.ELEMENTS_PER_PAGE)).intValue();

        Grid grid = relationMemento.getGrid();
        if (grid != null) {
            grid.setDataProvider(createDataProvider(searchQuery, relationMemento));
        }
        relationMemento.setMaxPageCount(maxPageCount);
        redrawPageNav(maxPageCount, relationMemento);

    }

    private <C> void redrawPageNav(int maxPageCount, RelationMemento relationMemento) {
        PageNav pageNav = relationMemento.getPageNav();
        pageNav.setPage(relationMemento.getPage(), maxPageCount);

    }
}