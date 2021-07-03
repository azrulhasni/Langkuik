/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.views.audit;

import com.azrul.langkuik.framework.dao.AuditTrailQuery;
import com.azrul.langkuik.framework.dao.DataAccessObject;
import com.azrul.langkuik.framework.audit.AuditedEntity;
import com.azrul.langkuik.framework.field.FieldContainer;
import com.azrul.langkuik.framework.field.FieldUtils;
import com.azrul.langkuik.views.main.MainView;
import com.azrul.langkuik.views.table.PageNav;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.validation.ValidatorFactory;
import org.apache.commons.collections4.MultiValuedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author azrul
 */
@Route(value = "AuditView", layout = MainView.class)
public class AuditView<T> extends Div {

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
    private DataAccessObject<T> dao;

    private Grid<AuditedEntity> grid;
    private PageNav pageNav;

//    private Button cancel = new Button("Cancel");
//    private Button save = new Button("Save");
    private Integer ELEMENTS_PER_PAGE = 3;
    private Integer maxPageCount = 1;

    private String sortColumn;
    private SortDirection sortDirection;

    private Integer page;
    //private Map<String, Grid.Column<?>> columns;
    private Dialog dialog;
    private T bean;

    public AuditView() {
        this.page = 1;
        //this.columns = new HashMap<>();

    }

    public void construct(T bean, Dialog dialog) {
        AuditTrailQuery audQuery = new AuditTrailQuery(bean);
        dialog.setHeightFull();
        grid = new Grid<>();
        grid.setHeightByRows(true);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();
        grid.setMultiSort(false);
        grid.setPageSize(ELEMENTS_PER_PAGE);

        //Get date format (for dates)
        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        Grid.Column<?> columnModifDate = grid.addColumn(t -> {
            AuditedEntity au = (AuditedEntity) t;
            return au.getModifiedDate().format(DateTimeFormatter.ofPattern(dateTimeFormat));
        }).setHeader("Date-Time")
          .setAutoWidth(true)
          .setSortable(true)
          .setSortProperty("revisionNumber");

        Grid.Column<?> columnUser = grid.addColumn(t -> {
            AuditedEntity au = (AuditedEntity) t;
            return au.getUsername();
        }).setHeader("User")
          .setAutoWidth(true)
          .setSortable(true)
          .setSortProperty("username");

        Grid.Column<?> columnOper = grid.addColumn(t -> {
            AuditedEntity au = (AuditedEntity) t;
            return au.getOperation();
        }).setHeader("Operation")
          .setAutoWidth(true)
          .setSortable(true)
          .setSortProperty("operation");
         
        MultiValuedMap<Integer, FieldContainer> fieldStore = FieldUtils.getFieldsByOrder(bean.getClass(), true);
        List<Integer> orders = new ArrayList<>(fieldStore.keySet());
        //Add fields to grid
        for (Integer order : orders) {
            for (FieldContainer fc : fieldStore.get(order)) {

                //Only display what is visible
                if (fc.getWebField().visibleInTable() == true) {

                    //Add column
                    Grid.Column<?> column = grid.addColumn(t -> {
                        AuditedEntity au = (AuditedEntity) t;
                         if (fc.getValue(au.getObject()) != null) {
                                if (LocalDate.class.equals(fc.getValue(au.getObject()).getClass())) {
                                    Date date = Date.from(((LocalDate)fc.getValue(au.getObject())).atStartOfDay(ZoneId.systemDefault()).toInstant());
                                    return sdf.format(date);
                                } else {
                                    return fc.getValue(au.getObject()).toString(); //apply highlighter here??
                                }
                         }  
                        
                        return "";
                    }
                    ).setHeader(fc.getWebField().displayName())
                            .setSortable(fc.isSortable())
                            .setSortProperty(fc.getField().getName())
                            .setResizable(true);
                }

            }
        }

        //do search
        String tenant = (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");
        DataProvider<AuditedEntity, Void> dataProvider
                = DataProvider.fromCallbacks(// First callback fetches items based on a query
                        query -> {
                            // Query.getOffSet must be call
                            query.getOffset();
                            int offset = (page - 1) * ELEMENTS_PER_PAGE;//query.getOffset();
                            // The number of items to load
                            int limit = query.getLimit();

                            Collection<AuditedEntity> data = new ArrayList<>();
                            if (query.getSortOrders().isEmpty()) {
                                data = dao.runQuery(audQuery,
                                        Optional.empty(),
                                        Optional.of(Boolean.TRUE),
                                        Optional.of(offset),
                                        Optional.of(limit),
                                        Optional.of(tenant),
                                        Optional.empty());
                            } else {
                                QuerySortOrder sort = query.getSortOrders().iterator().next(); //we only support single column sorting
                                sortColumn = sort.getSorted();
                                sortDirection = sort.getDirection();
                                data = dao.runQuery(audQuery,
                                        Optional.of(sort.getSorted()),
                                        Optional.of(SortDirection.ASCENDING.equals(sort.getDirection())),
                                        Optional.of(offset),
                                        Optional.of(limit),
                                        Optional.of(tenant),
                                        Optional.empty());
                            }
                            return data.stream();
                        },
                        // Second callback fetches the number of items
                        // for a query
                        query -> {
                            Long count = dao.countQueryResult(audQuery, Optional.of(tenant), Optional.empty());

                            if (count >= ELEMENTS_PER_PAGE) {
                                Integer finalPage = Long.valueOf(-Math.floorDiv(-count, ELEMENTS_PER_PAGE)).intValue();
                                if (page == (finalPage)) {
                                    int count4ThisPage = Long.valueOf(count % ELEMENTS_PER_PAGE).intValue();
                                    if (count4ThisPage == 0) {
                                        return ELEMENTS_PER_PAGE;
                                    } else {
                                        return count4ThisPage;
                                    }
                                } else {
                                    return ELEMENTS_PER_PAGE;
                                }
                            } else {
                                return count.intValue();
                            }
                        });
        //dataProvider.
        grid.setDataProvider(dataProvider);
        Long count = dao.countQueryResult(audQuery, Optional.of(tenant), Optional.empty());
        maxPageCount = Long.valueOf(-Math.floorDiv(-count, ELEMENTS_PER_PAGE)).intValue();

        //contruct grid navigation
        if (pageNav == null) {
            pageNav = new PageNav();
        }
        pageNav.setWidthFull();
        pageNav.setPage(page, maxPageCount);
        pageNav.getFirstPage().addClickListener(e -> {
            page = 1;
            pageNav.setPage(page, maxPageCount);
            grid.getDataProvider().refreshAll();
        });
        pageNav.getFinalPage().addClickListener(e -> {
            if (page < maxPageCount) {
                page = maxPageCount;
                pageNav.setPage(page, maxPageCount);
                grid.getDataProvider().refreshAll();
            }
        });
        pageNav.getNextPage().addClickListener(e -> {
            if (page < maxPageCount) {
                page++;
                pageNav.setPage(page, maxPageCount);
                grid.getDataProvider().refreshAll();
            }
        });
        pageNav.getLastPage().addClickListener(e -> {
            if (page > 1) {
                page--;
                pageNav.setPage(page, maxPageCount);
                grid.getDataProvider().refreshAll();
            }
        });
        VerticalLayout wrapper = new VerticalLayout();

        //close button
        Button close = new Button("Close", e -> {
            dialog.close();
        });

        wrapper.add(close, pageNav, grid);

        //put all together
        this.add(wrapper);

    }
}
