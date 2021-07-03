/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.views.findusage;

import com.azrul.langkuik.framework.annotation.WebEntity;
import com.azrul.langkuik.framework.annotation.WebRelation;
import com.azrul.langkuik.custom.attachment.Attachment;
import com.azrul.langkuik.framework.dao.DataAccessObject;
import com.azrul.langkuik.framework.dao.Dual;
import com.azrul.langkuik.framework.dao.FindUsageQuery;
import com.azrul.langkuik.framework.factory.SpringBeanFactory;
import com.azrul.langkuik.framework.field.FieldContainer;
import com.azrul.langkuik.framework.field.FieldUtils;
import com.azrul.langkuik.framework.relationship.RelationMemento;
import com.azrul.langkuik.framework.relationship.RelationUtils;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import com.azrul.langkuik.framework.standard.Status;
import com.azrul.langkuik.views.main.MainView;
import com.azrul.langkuik.views.pojo.PojoConst;
import static com.azrul.langkuik.views.pojo.PojoConst.ELEMENTS_PER_PAGE;
import com.azrul.langkuik.views.pojo.PojoView;
import com.azrul.langkuik.views.table.PageNav;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.lang.reflect.Field;
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
import java.util.Optional;
import java.util.stream.Stream;
import javax.persistence.EntityManagerFactory;
import javax.validation.ValidatorFactory;
import org.apache.commons.collections4.MultiValuedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author azrul
 */
@Route(value = "FindUsageView", layout = MainView.class)
public class FindUsageView<T> extends Div {

    @Value("${application.lgDateFormatLocale}")
    private String dateFormatLocale;

    @Value("${application.lgDateFormat}")
    private String dateFormat;

    @Value("${application.lgDateTimeFormat}")
    private String dateTimeFormat;

    @Value("${application.lgFullurl}")
    private String fullUrl;

    @Autowired
    ValidatorFactory validatorFactory;

    @Autowired
    private DataAccessObject dao;

    @Autowired
    private EntityManagerFactory emf;

    private Map<String, RelationMemento> relationMementos = new HashMap<>();

    public FindUsageView() {
    }

    public void construct(T current, Dialog dialog) {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.add(new Button("Back", e -> {
                dialog.close();
            }));
        this.add(headerLayout);
        Collection<Dual<Class<?>, Field>> parentClasses = RelationUtils.getAllDependingClass(current.getClass(), emf);
        for (Dual<Class<?>, Field> parentClass : parentClasses) {
            WebEntity webEntity = parentClass.getFirst().getAnnotation(WebEntity.class);
            if (webEntity != null) {
                HorizontalLayout buttonLayout = new HorizontalLayout();
                Button btnClearSelected = new Button("Clear selection", e -> {
                    relationMementos.get(parentClass.getSecond()).getGrid().deselectAll();
                });
                buttonLayout.add(btnClearSelected);
                
                WebRelation webRelation = parentClass.getSecond().getAnnotation(WebRelation.class);
                if (webRelation==null){
                    continue;
                }
               
                
                VerticalLayout verticalLayout = new VerticalLayout();
                //verticalLayout.add(new Span(webEntity.name()+ "::" + webRelation.name()));
                createTable(current, 
                        parentClass.getSecond().getName(),
                        parentClass.getFirst(),
                        webEntity.name()+ "::" + webRelation.name(),
                        verticalLayout,
                        relationMementos, 
                        buttonLayout, e -> {
                                    if (e.getClickCount() == 2) {
                                        LangkuikExt ext = (LangkuikExt) e.getItem();
                                        if (ext.getId() != null) {
                                            createPojoDialog(e.getItem(), parentClass.getSecond().getName(), current,relationMementos.get(parentClass.getSecond().getName()));
                                           

                                        }
                                    }
                                },
                        ELEMENTS_PER_PAGE);
                this.add(verticalLayout);
            }
        }

    }

    private <P> void createPojoDialog(P parent, String relationName, T child, RelationMemento memento) {

        Dialog dialog2 = new Dialog();
        PojoView<P> pojoView = SpringBeanFactory.create(PojoView.class);
        pojoView.construct(parent, dialog2);
        dialog2.setModal(true);
        dialog2.setCloseOnEsc(false);
        dialog2.setCloseOnOutsideClick(false);
        dialog2.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                if (pojoView.getCurrentBean() != null) {
                    Optional<P> oresult = dao.save(parent);
                    P result = oresult.orElseThrow();
                    redrawTables(child,
                            relationName, parent.getClass(), memento);
                }

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

    private <P, T> void createTable(T currentBean,
            String parentToCurrentRelation,
            Class<P> parentClass,
            String humanReadableRelationshipName,
            VerticalLayout layout,
            Map<String, RelationMemento> relationMementos,
            Component buttonLayout,
            ComponentEventListener<ItemClickEvent<P>> rowItemClickListener,
            Integer heightInPixel) {

        String tenant = (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");
        
        Map<String, Grid.Column<?>> columns = new HashMap<>();

        FindUsageQuery findUsageQuery = new FindUsageQuery(currentBean,
                parentToCurrentRelation,
                parentClass);

        Long count = dao.countQueryResult(findUsageQuery, Optional.of(tenant), Optional.empty());
        int maxPageCount = Long.valueOf(-Math.floorDiv(-count, PojoConst.ELEMENTS_PER_PAGE)).intValue();

        Grid<P> grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        //grid.setHeight(heightInPixel.toString() + "px");
        grid.setHeightByRows(true);
        grid.setMultiSort(false);
        grid.setPageSize(PojoConst.ELEMENTS_PER_PAGE);

        DataProvider dataProvider = null;

        MultiValuedMap<Integer, FieldContainer> fieldStore = FieldUtils.getFieldsByOrder(parentClass);

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
        PageNav pageNav = createPageNav(parentToCurrentRelation, maxPageCount, grid, relationMementos);
        pageNav.setWidthFull();

        //At this stage, if no relation mementos is present represeting this relationship in the relationMementos map, add one
        if (relationMementos.get(parentToCurrentRelation) == null) {
            RelationMemento memento = new RelationMemento(grid, pageNav, 1, maxPageCount);
            relationMementos.put(parentToCurrentRelation, memento);
        }

        //Add data or not
        if (count > 0) {
            dataProvider = createDataProvider(findUsageQuery,
                    relationMementos.get(parentToCurrentRelation));
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

    private <T, C> DataProvider<C, Void> createDataProvider(FindUsageQuery usageQuery,
            RelationMemento relationMemento) {
        String tenant = (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");
        DataProvider<C, Void> dataProvider
                = DataProvider.fromCallbacks(// Firstly, callback fetches items based on a query
                        (var query) -> {
                            // Query.getOffSet must be call
                            query.getOffset();
                            Integer offset = 0;
                            if (relationMemento != null) {
                                offset = (relationMemento.getPage() - 1) * PojoConst.ELEMENTS_PER_PAGE;
                            }// The number of items to load
                            Integer limit = query.getLimit();
                            Collection<C> data = new ArrayList<>();
                            if (query.getSortOrders().isEmpty()) {
                                data = dao.runQuery(usageQuery, 
                                        Optional.empty(), 
                                        Optional.of(Boolean.FALSE), 
                                        Optional.of(offset), 
                                        Optional.of(limit), 
                                        Optional.of(tenant), 
                                        Optional.empty());
                            } else {
                                QuerySortOrder sort = query.getSortOrders().iterator().next(); //we only support single column sorting
                                //sortColumn = sort.getSorted();
                                //sortDirection = sort.getDirection();
                                data = dao.runQuery(usageQuery, 
                                        Optional.of(sort.getSorted()),
                                        Optional.of(SortDirection.ASCENDING.equals(sort.getDirection())), 
                                        Optional.of(offset), 
                                        Optional.of(limit), 
                                        Optional.of(tenant), 
                                        Optional.empty());
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
                            Long count = dao.countQueryResult(usageQuery,
                                    Optional.of(tenant), 
                                    Optional.empty());
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

    public <P, T> void redrawTables(T currentBean,
            String parentToCurrentRelation,
            Class<P> parentClass,
            RelationMemento relationMemento) {
        String tenant = (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");
        FindUsageQuery findUsageQuery = new FindUsageQuery(currentBean,
                parentToCurrentRelation,
                parentClass);

        Long count = dao.countQueryResult(findUsageQuery, Optional.of(tenant), Optional.empty());
        int maxPageCount = Long.valueOf(-Math.floorDiv(-count, PojoConst.ELEMENTS_PER_PAGE)).intValue();

        Grid grid = relationMemento.getGrid();
        if (grid != null) {
            grid.setDataProvider(createDataProvider(findUsageQuery, relationMemento));
        }
        relationMemento.setMaxPageCount(maxPageCount);
        redrawPageNav(maxPageCount, relationMemento);

    }

    private <C> void redrawPageNav(int maxPageCount, RelationMemento relationMemento) {
        PageNav pageNav = relationMemento.getPageNav();
        pageNav.setPage(relationMemento.getPage(), maxPageCount);

    }
}
