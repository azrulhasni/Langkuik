package com.azrul.langkuik.views.table;

//import com.azrul.langkuik.backend.Employee;
import com.azrul.langkuik.framework.field.FieldContainer;
import com.azrul.langkuik.framework.dao.DataAccessObject;
import com.azrul.langkuik.framework.dao.EntityUtils;
import com.azrul.langkuik.framework.dao.FindAnyEntityQuery;
import com.azrul.langkuik.framework.exception.EntityIsUsedException;
import com.azrul.langkuik.framework.factory.SpringBeanFactory;
import com.azrul.langkuik.framework.field.FieldUtils;
import com.azrul.langkuik.framework.relationship.RelationType;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import com.azrul.langkuik.framework.standard.Rules;
import com.azrul.langkuik.framework.standard.Status;
import com.azrul.langkuik.framework.workflow.Workflow;
import com.azrul.langkuik.loanorigsystem.model.Application;
import com.azrul.langkuik.views.main.MainView;
import com.azrul.langkuik.views.pojo.PojoView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;
import com.vaadin.flow.server.VaadinSession;
import java.beans.IntrospectionException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections4.MultiValuedMap;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Value;

//@Route(value = "TableView", layout = MainView.class)
//@CssImport("styles/views/masterdetail/master-detail-view.css")
//@Theme(value = Material.class)
public class TableView<T> extends Div implements AfterNavigationObserver, BeforeLeaveObserver, HasUrlParameter<String>  {

    @Autowired
    private EntityManagerFactory emf;

    @Value("${application.lgDateFormatLocale}")
    private String dateFormatLocale;

    @Value("${application.lgDateFormat}")
    private String dateFormat;

    @Value("${application.lgFullurl}")
    private String fullUrl;

    @Autowired
    ValidatorFactory validatorFactory;

    @Autowired
    private DataAccessObject<T> dao;
    
    @Autowired
    private List<Workflow<Application>> workflows;

    @Override
    public void setParameter(BeforeEvent be, @OptionalParameter String t) {
       System.out.println("param:"+t);
       QueryParameters qp = be.getLocation().getQueryParameters();
       worklist=qp.getParameters().get("worklist").iterator().next();
       
    }


    public enum Mode {
        MAIN, SELECT
    }

    private Mode mode;
    private Grid<T> grid;
    private PageNav pageNav;

//    private Button cancel = new Button("Cancel");
//    private Button save = new Button("Save");
    private Binder<T> binder;
    private Integer ELEMENTS_PER_PAGE = 3;
    private Integer maxPageCount = 1;

    private Integer page;
    private FindAnyEntityQuery<T> searchQuery;
    private String sortColumn;
    private SortDirection sortDirection;
    private Map<String, Column<?>> columns;
    private Class<T> currentClass;
    private Collection<T> exclusion;
    private Dialog dialog;
    private RelationType relationType;
    private String worklist;
    private Boolean ownedByMe;
    

    public TableView() {
        this.page = 1;
        this.columns = new HashMap<>();
        this.exclusion = new ArrayList<>();
        this.relationType = RelationType.NA;
        this.worklist=null;
    }

    

//    public TableView(String worklist, Class<T> tclass, TableView.Mode mode) {
//        this(worklist);
//        this.currentClass = tclass;
//        this.mode = mode;
//    }

    public TableView(Class<T> tclass, TableView.Mode mode) {
        this();
        this.currentClass = tclass;
        this.mode = mode;
    }

    public void construct(Class<T> tclass,
            TableView.Mode mode,
            Optional<RelationType> relationType,
            Dialog dialog,
            Collection<T> exclusion) {
        this.dialog = dialog;
        this.currentClass = tclass;
        this.mode = mode;
        this.exclusion = exclusion;
        this.relationType = relationType.orElse(RelationType.NA);

        construct();
    }

    /* @Override
    public void setParameter(BeforeEvent event,String parameter) {
        //setText(String.format("Hello, %s!", parameter));
       event.getRouteParameters().get("ENTITY").ifPresent(entityName->{ 
           EntityUtils.getManagedEntity(entityName, emf).ifPresent(tclass->{ 
               this.currentClass=(Class<T>) tclass;
               this.mode =  TableView.Mode.MAIN;
               construct();
           });
       });
    } */
    
    
    @PostConstruct //Vaadin only inject after constructor is done
    private void construct() {
        

        if (currentClass == null) {
            //constructing Table (using SpringBeanFactory) in 
            //'PojoView's Link Existing' means calling construct automatically. 
            //This is a problem because currentClass has not been set. 
            //So, if currentClass is null (i.e. we are calling this method in 'PojoView's Link Existing')
            //then, just return.
            return;
        }

//        try {
        setId("master-detail-view");
        //set query
        //Class<T> tclass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        searchQuery = new FindAnyEntityQuery<T>(currentClass, dateFormat, exclusion);

        //date format
        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        // Configure Grid
        grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightByRows(true);
        grid.setMultiSort(false);
        grid.setPageSize(ELEMENTS_PER_PAGE);
        grid.getStyle().set("font-size", "12px");
        if (relationType == RelationType.X_TO_MANY) {
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
        } else {
            grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        }

        MultiValuedMap<Integer, FieldContainer> fieldStore = FieldUtils.getFieldsByOrder(currentClass);
        //sort fields by order
        List<Integer> orders = new ArrayList<>(fieldStore.keySet());
        Collections.sort(orders);

        //Add column for status
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

                //Only display what is visible
                if (fc.getWebField().visibleInTable() == true) {

                    //Add column
                    Column<?> column = grid.addColumn(t -> {
                        if (fc.getValue(t) != null) {
                            if (LocalDate.class.equals(fc.getReturnType())) {
                                Date date = Date.from(((LocalDate) fc.getValue(t)).atStartOfDay(ZoneId.systemDefault()).toInstant());
                                return sdf.format(date);
                            } else {
                                return fc.getValue(t).toString(); //apply highlighter here??
                            }
                        }
                        return "";
                    }
                    ).setHeader(fc.getWebField().displayName())
                            .setSortable(fc.isSortable())
                            .setSortProperty(fc.getField().getName())
                            .setResizable(true);
                    columns.put(fc.getField().getName(), column);
                }

            }
        }

        //add listener if things are clicked in the table
        grid.addItemClickListener(e -> {
            if (e.getClickCount() == 2) {
                if (mode == Mode.MAIN) {
                    Object bean = e.getItem();
                    createPojoDialog(bean);
                }
            }
        });

        //add buttons
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        if (mode.equals(Mode.SELECT)) {
            HorizontalLayout commands = new HorizontalLayout();
            commands.add(new Button("Back", e -> {
                grid.deselectAll();
                if (dialog != null) {
                    dialog.close();
                }
            }));

            commands.add(new Button("Clear selection", e -> {
                grid.deselectAll();
            }));

            commands.add(new Button("Select and back", e -> {
                if (dialog != null) {
                    dialog.close();
                }
            }));

            verticalLayout.add(commands);
        } else {
            HorizontalLayout commands = new HorizontalLayout();

            commands.add(new Button("Clear selection", e -> {
                grid.deselectAll();
            }));

            commands.add(new Button("Delete", e -> {
                String username = (String) VaadinSession.getCurrent().getSession().getAttribute("USERNAME");

                Set itemsToBeDeleted = grid.getSelectedItems();
                List<String> cannotBeDeleted = new ArrayList<>();

                try {
                    dao.delete(itemsToBeDeleted, c -> {
                        LangkuikExt o = (LangkuikExt) c;
                        if (!Rules.canBeDeleted(o, username)) {
                            cannotBeDeleted.add(o.toString());
                            return false;
                        } else {
                            return true;
                        }
                    });
                    redoSearch();
                } catch (EntityIsUsedException ex) {
                    Logger.getLogger(TableView.class.getName()).log(Level.SEVERE, null, ex);
                }

            }));

            verticalLayout.add(commands);
        }
        verticalLayout.add(createGridLayout(currentClass));
        this.add(verticalLayout);
//        } catch (IllegalArgumentException
//                | SecurityException
//                | IntrospectionException
//                | NoSuchFieldException ex) {
//            Logger.getLogger(TableView.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

//    public void setClass(Class<T> tclass) {
//        this.tclass = tclass;
//    }
//    private void createEditorLayout(SplitLayout splitLayout) {
//        Div editorDiv = new Div();
//        editorDiv.setId("editor-layout");
//        FormLayout formLayout = new FormLayout();
//        addFormItem(editorDiv, formLayout, firstname, "First name");
//        addFormItem(editorDiv, formLayout, lastname, "Last name");
//        addFormItem(editorDiv, formLayout, email, "Email");
//        addFormItem(editorDiv, formLayout, password, "Password");
//        createButtonLayout(editorDiv);
//        splitLayout.addToSecondary(editorDiv);
//    }
//    private void createButtonLayout(Div editorDiv) {
//        HorizontalLayout buttonLayout = new HorizontalLayout();
//        buttonLayout.setId("button-layout");
//        buttonLayout.setWidthFull();
//        buttonLayout.setSpacing(true);
//        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
//        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        buttonLayout.add(cancel, save);
//        editorDiv.add(buttonLayout);
//    }
    private Component createGridLayout(Class<T> tclass) {
        Div wrapper = new Div();
        wrapper.setId("wrapper");
        wrapper.setWidthFull();

        final CreateAndSearchPanel searchField = new CreateAndSearchPanel("Search...", (mode == Mode.MAIN));
        searchField.setSearchListener(e -> {
            page = 1;
            if (searchField.getValue().isBlank()) {
                doSearch(Optional.empty());
            } else {
                doSearch(Optional.of(searchField.getValue()));
            }
            pageNav.setPage(page, maxPageCount);
        });
        searchField.setCreateListener(e -> {
            T bean = createNew(tclass);
            createPojoDialog(bean);
        });

        searchField.setWidthFull();
        Div searchWrapper = new Div();
        searchWrapper.setWidthFull();
        searchWrapper.add(searchField);
        searchWrapper.setWidthFull();

        //add page nav
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

        //put all together
        wrapper.add(searchWrapper, pageNav, grid);
        return wrapper;
    }

    public T createNew(Class<T> tclass) {

        String username = (String) VaadinSession.getCurrent().getSession().getAttribute("USERNAME");
        String tenant = (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");
//        T bean = (T)(dao.create(tclass,Optional.of(tenant), username,Optional.empty()).orElseThrow());

        T bean = (T) dao.createAndSave(tclass, Optional.of(tenant), username, Optional.empty()).orElseThrow();
        return bean;

    }

    public void doSearch() {
        doSearch(Optional.empty());
    }

    public void doSearch(Optional<String> queryString) {
        searchQuery.setQueryString(queryString);
        doSearchByQuery(searchQuery);
    }

    public void redoSearch() {
        doSearchByQuery(searchQuery);
    }

    private void doSearchByQuery(FindAnyEntityQuery<T> searchQuery) {
        String tenant = (String) VaadinSession.getCurrent().getSession().getAttribute("TENANT");
        DataProvider<T, Void> dataProvider
                = DataProvider.fromCallbacks(// First callback fetches items based on a query
                        query -> {
                            // Query.getOffSet must be call
                            query.getOffset();
                            int offset = (page - 1) * ELEMENTS_PER_PAGE;//query.getOffset();
                            // The number of items to load
                            int limit = query.getLimit();
                            Collection<T> data = new ArrayList<>();
                            if (query.getSortOrders().isEmpty()) {
                                data = dao.runQuery(searchQuery,
                                        Optional.empty(),
                                        Optional.of(Boolean.TRUE),
                                        Optional.of(offset),
                                        Optional.of(limit),
                                        Optional.of(tenant),
                                        Optional.ofNullable(worklist));
                            } else {
                                QuerySortOrder sort = query.getSortOrders().iterator().next(); //we only support single column sorting
                                sortColumn = sort.getSorted();
                                sortDirection = sort.getDirection();
                                data = dao.runQuery(searchQuery,
                                        Optional.of(sort.getSorted()),
                                        Optional.of(SortDirection.ASCENDING.equals(sort.getDirection())),
                                        Optional.of(offset),
                                        Optional.of(limit),
                                        Optional.of(tenant),
                                        Optional.ofNullable(worklist));
                            }
                            return data.stream();
                        },
                        // Second callback fetches the number of items
                        // for a query
                        query -> {
                            Long count = dao.countQueryResult(searchQuery, Optional.of(tenant), Optional.ofNullable(worklist));

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

        Long count = dao.countQueryResult(searchQuery, Optional.of(tenant), Optional.ofNullable(worklist));
        maxPageCount = Long.valueOf(-Math.floorDiv(-count, ELEMENTS_PER_PAGE)).intValue();
        if (page > maxPageCount) { //this is the case where we deleted enough entities that a 
            //whole page is gone. So maxPageCount is updated but not page. This is where we update Page
            if (maxPageCount==0){
                page = 1;
            }else{
                page = maxPageCount;
            }
        }
        pageNav.setPage(page, maxPageCount);
        
        grid.setDataProvider(dataProvider);
        dataProvider.refreshAll();

    }

//    private void addFormItem(Div wrapper, FormLayout formLayout,
//            AbstractField field, String fieldName) {
//        formLayout.addFormItem(field, fieldName);
//        wrapper.add(formLayout);
//        field.getElement().getClassList().add("full-width");
//    }
//
//    private void populateForm(T value) {
//        // Value can be null as well, that clears the form
//        binder.readBean(value);
//
//        // The password field isn't bound through the binder, so handle that
////        password.setValue("");
//    }
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        System.out.println("Parameters:"+event.getLocation().getSegments());
        //worklist=event.getLocation().getSegments().get(1);
//        if (grid==null){
//            construct();
//        }else{
//            grid.setDataProvider(DataProvider.ofCollection(new ArrayList()));
//        }
        if (VaadinSession.getCurrent().getSession().getAttribute("USERNAME") == null) {
            UI.getCurrent().getPage().executeJs("window.open(\"" + fullUrl + "/main\", \"_self\");");
            return;
        }
        MasterDetailMomento mem = ComponentUtil.getData(this, MasterDetailMomento.class);
        if (mem != null) {
            //dao.massIndex();
            this.page = mem.getPage();
            this.searchQuery = (FindAnyEntityQuery<T>) mem.getSearchQuery();
            this.sortColumn = mem.getSortColumn();
            this.sortDirection = mem.getSortDirection();
        }

        if (pageNav == null) {
            pageNav = new PageNav();
        }
        doSearch();
        pageNav.setPage(page, maxPageCount);

        if (sortColumn != null) {
            setSortColumn(sortColumn, sortDirection);
        }
       
    }

    public void setSortColumn(String columnClassLevelName, SortDirection direction) {
        Column<?> column = columns.get(columnClassLevelName);
        //grid.sort
        GridSortOrderBuilder builder = new GridSortOrderBuilder();
        grid.sort(builder.thenAsc(column).build());
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        //System.out.println("Parameters:"+event.getLocation().getQueryParameters().getParameters());
        MasterDetailMomento mem = new MasterDetailMomento(page, searchQuery, sortColumn, sortDirection);
        ComponentUtil.setData(this, MasterDetailMomento.class, mem);
    }

    public <C> void createPojoDialog(C bean) {
        String tranxId = EntityUtils.getTranxId(bean);
        Dialog dialog2 = new Dialog();
        PojoView<C> pojoView = SpringBeanFactory.create(PojoView.class);
        pojoView.construct(bean, dialog2);
        dialog2.setModal(true);
        dialog2.setCloseOnEsc(false);
        dialog2.setCloseOnOutsideClick(false);

        //begin:enable scroll
        pojoView.setHeight("100%");
        pojoView.getStyle().set("overflow-y", "auto");
        dialog2.add(pojoView);
        dialog2.setHeightFull();
        dialog2.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                if (pojoView.isDiscarded() == false) {
                    T root=(T) pojoView.getCurrentBean();
                    if (pojoView.isSubmitted()){
                    //run workflow
                        for (Workflow workflow:workflows){
                            if (workflow.getRootClass().equals(currentClass.getCanonicalName())){
                               root = (T) workflow.run(root, true);
                            }
                        }
                    }
                    //...then save
                    dao.save(root);
                } else {
                    try {
                        String username = (String) VaadinSession.getCurrent().getSession().getAttribute("USERNAME");
                        dao.delete((T) pojoView.getCurrentBean(), c -> {
                            LangkuikExt o = (LangkuikExt) c;
                            if (!Rules.canBeDeleted(o, username)) {
                                return false;
                            } else {
                                return true;
                            }
                        });
                    } catch (EntityIsUsedException ex) {
                        Logger.getLogger(TableView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                redoSearch();
            }
        });
        //end:enable scroll
        dialog2.open();
        //UI.getCurrent().navigate(PojoView.class);
    }

    public Set<T> getSelected() {
        return grid.getSelectedItems();
    }

    /**
     * @return the worklist
     */
    public String getWorklist() {
        return worklist;
    }

    /**
     * @param worklist the worklist to set
     */
    public void setWorklist(String worklist) {
        this.worklist = worklist;
    }
}
