/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik;

import org.azrul.langkuik.framework.WorkType;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.configs.Configuration;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.dao.EntityUtils;
import org.azrul.langkuik.dao.HibernateGenericDAO;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.customtype.attachment.AttachmentCustomType;
import org.azrul.langkuik.framework.exception.DuplicateDataException;
import org.azrul.langkuik.framework.relationship.RelationManagerFactory;
import org.azrul.langkuik.framework.webgui.BeanView;
import org.azrul.langkuik.framework.webgui.SearchResultView;
import org.azrul.langkuik.framework.webgui.breadcrumb.BreadCrumbBuilder;
import org.azrul.langkuik.framework.webgui.breadcrumb.History;
import org.azrul.langkuik.security.role.EntityRight;
import org.azrul.langkuik.security.role.UserSecurityUtils;
import org.azrul.langkuik.system.model.role.Role;
import org.azrul.langkuik.system.model.role.RoleDAO;
import org.azrul.langkuik.system.model.user.User;
import org.azrul.langkuik.system.model.user.UserDAO;
import org.azrul.langkuik.system.model.user.UserSearchResultView;
import org.azrul.langkuik.system.model.worklist.UserWorklist;
import org.azrul.langkuik.system.model.worklist.UserWorklistDAO;
import org.vaadin.dialogs.ConfirmDialog;

public class Langkuik<W extends Enum> implements Serializable {

    private EntityManagerFactory emf = null;
    private UI ui = null;
    private RelationManagerFactory relationManagerFactory = null;
    private List<Class<?>> customTypeInterfaces = null;
    private AbstractComponentContainer loginPage = null;
    private ResourceBundle textResourceBundle = null;
    private Class<W> worklistType;

    //all parameters
    public Langkuik(final EntityManagerFactory emf,
            final UI ui,
            final RelationManagerFactory relationManagerFactory,
            final List<Class<?>> customTypeInterfaces,
            final AbstractComponentContainer loginPage,
            final ResourceBundle textResourceBundle,
            final Class<W> worklist) {
        this.emf = emf;
        this.ui = ui;
        this.relationManagerFactory = relationManagerFactory;
        this.customTypeInterfaces = customTypeInterfaces;
        this.loginPage = loginPage;
        this.textResourceBundle = textResourceBundle;
        this.worklistType = worklist;
    }

    //default login page (single or multi tenant)
    public Langkuik(final EntityManagerFactory emf,
            final UI ui,
            final RelationManagerFactory relationManagerFactory,
            final List<Class<?>> customTypeInterfaces,
            final ResourceBundle textResourceBundle,
            final Boolean isMultiTenant,
            final Class<W> worklist) {
        this.emf = emf;
        this.ui = ui;
        this.relationManagerFactory = relationManagerFactory;
        this.customTypeInterfaces = customTypeInterfaces;
        if (Boolean.TRUE.equals(isMultiTenant)) {
            this.loginPage = getMultiTenantLoginForm();
        } else {
            this.loginPage = getSingleTenantLoginForm();
        }
        this.textResourceBundle = textResourceBundle;
        this.worklistType = worklist;
    }

    //default login page (single or multi tenant) and default text
    public Langkuik(final EntityManagerFactory emf,
            final UI ui,
            final RelationManagerFactory relationManagerFactory,
            final List<Class<?>> customTypeInterfaces,
            final Boolean isMultiTenant,
            final Class<W> worklist) {
        this.textResourceBundle = ResourceBundle.getBundle("Text", new Locale("en"));

        this.emf = emf;
        this.ui = ui;
        this.relationManagerFactory = relationManagerFactory;
        this.customTypeInterfaces = customTypeInterfaces;
        if (Boolean.TRUE.equals(isMultiTenant)) {
            this.loginPage = getMultiTenantLoginForm();
        } else {
            this.loginPage = getSingleTenantLoginForm();
        }
        this.worklistType = worklist;
    }

    //default login page (single or multi tenant), default text, default customTypeInterface
    public Langkuik(final EntityManagerFactory emf,
            final UI ui,
            final RelationManagerFactory relationManagerFactory,
            final Boolean isMultiTenant,
            final Class<W> worklist) {
        this.textResourceBundle = ResourceBundle.getBundle("Text", new Locale("en"));

        this.emf = emf;
        this.ui = ui;
        this.relationManagerFactory = relationManagerFactory;
        this.customTypeInterfaces = null;
        if (Boolean.TRUE.equals(isMultiTenant)) {
            this.loginPage = getMultiTenantLoginForm();
        } else {
            this.loginPage = getSingleTenantLoginForm();
        }
        this.worklistType = worklist;
    }

    public void init() {
        UserDAO.setEMF(this.emf);
        RoleDAO.setEMF(this.emf);
        UserWorklistDAO.setEMF(this.emf);
        for (W worklist:worklistType.getEnumConstants()){
            UserWorklistDAO.registerWorklist(worklist.toString());
        }
        UserSecurityUtils.init(this.emf);

        ui.setContent(loginPage);

    }

    private FormLayout getSingleTenantLoginForm() {
        final FormLayout loginForm = new FormLayout();
        final TextField username = new TextField(textResourceBundle.getString("login.username"));
        username.setId(username.getCaption());
        final PasswordField password = new PasswordField(textResourceBundle.getString("login.password"));
        password.setId(password.getCaption());
        Button loginBtn = new Button(textResourceBundle.getString("login.loginBtnText"));
        loginBtn.setId(loginBtn.getCaption());
        loginForm.addComponent(username);
        loginForm.addComponent(password);
        loginForm.addComponent(loginBtn);
        loginBtn.addClickListener(getLoginAction(username, password));
        return loginForm;
    }

    private FormLayout getMultiTenantLoginForm() {
        final FormLayout loginForm = new FormLayout();
        final TextField username = new TextField(textResourceBundle.getString("login.username"));
        final TextField tenant = new TextField(textResourceBundle.getString("login.tenant"));
        final PasswordField password = new PasswordField(textResourceBundle.getString("login.password"));
        Button loginBtn = new Button(textResourceBundle.getString("login.loginBtnText"));

        loginForm.addComponent(username);
        loginForm.addComponent(tenant);
        loginForm.addComponent(password);
        loginForm.addComponent(loginBtn);
        loginBtn.addClickListener(getLoginAction(username, tenant, password));
        return loginForm;
    }

    public void massIndex(final EntityManagerFactory emf) {
        HibernateGenericDAO.massIndexDatabaseForSearch(emf);
    }

    public ClickListener getLoginAction(final TextField username, final PasswordField password) {
        return new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (UserSecurityUtils.login(username.getValue(), password.getValue().toCharArray())) {
                    mainApp();
                } else {
                    //authenMsgLbl.setCaption("Login has failed. Please retry");
                    //form.addComponent(authenMsgLbl);
                    Notification.show(textResourceBundle.getString("login.loginErrorText"), Notification.Type.WARNING_MESSAGE);
                }
            }
        };
    }

    public ClickListener getLoginAction(final TextField username, final TextField tenant, final PasswordField password) {
        return new ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (UserSecurityUtils.login(username.getValue() + "/" + tenant.getValue(), password.getValue().toCharArray())) {
                    mainApp();
                } else {
                    //authenMsgLbl.setCaption("Login has failed. Please retry");
                    //form.addComponent(authenMsgLbl);
                    Notification.show(textResourceBundle.getString("login.loginErrorText"), Notification.Type.WARNING_MESSAGE);
                }
            }
        };
    }

    private void mainApp() {

        List<Class<?>> rootClasses = EntityUtils.getAllRootEntities(emf);

        //Manage custom type
        if (customTypeInterfaces == null) {
            customTypeInterfaces = new ArrayList<>();
        }

        //add system level custom type
        customTypeInterfaces.add(AttachmentCustomType.class);
        //create DAOs for custom types
        final List<DataAccessObject<?,W>> customTypeDaos = new ArrayList<>();
        for (Class<?> clazz : customTypeInterfaces) {
            customTypeDaos.add(new HibernateGenericDAO(emf, clazz));
        }

        //Setup page
        VerticalLayout main = new VerticalLayout();
        VerticalLayout content = new VerticalLayout();
        final Navigator navigator = new Navigator(ui, content);
        final HorizontalLayout breadcrumb = new HorizontalLayout();

        MenuBar menubar = new MenuBar();

        menubar.setId("MENUBAR");
        main.addComponent(menubar);
        main.addComponent(breadcrumb);
        main.addComponent(content);

        final Deque<History> history = new ArrayDeque<>();
        final Configuration config = Configuration.getInstance();

        final PageParameter<W> pageParameter = new PageParameter<W>(customTypeDaos,
                emf,
                relationManagerFactory,
                history,
                config,
                breadcrumb,
                textResourceBundle,
                null);

        history.push(new History("START", pageParameter.getLocalisedText("history.start")));
        StartView startView = new StartView(pageParameter);
        navigator.addView("START", startView);
        MenuBar.MenuItem create = menubar.addItem(pageParameter.getLocalisedText("menu.create"), null);
        MenuBar.MenuItem view = menubar.addItem(pageParameter.getLocalisedText("menu.view"), null);
        MenuBar.MenuItem manageCurrentUser = menubar.addItem(pageParameter.getLocalisedText("menu.user"), null);

        for (final Class rootClass : rootClasses) {

            final WebEntity myObject = (WebEntity) rootClass.getAnnotation(WebEntity.class);
            final DataAccessObject<?,W> dao = new HibernateGenericDAO<>(emf, rootClass);

            int state = 0;
            //manage state
            if (UserSecurityUtils.hasRole("ROLE_ADMIN")) {
                state = 1;
                if (rootClass.equals(User.class)) {
                    state = 2;
                }
            } else {
                state = 3;
                if (rootClass.equals(User.class) || rootClass.equals(Role.class) || rootClass.equals(UserWorklist.class)) {
                    state = 4;
                } else {
                    state = 5;
                    if (UserSecurityUtils.getEntityRight(rootClass) == EntityRight.CREATE_UPDATE
                            || UserSecurityUtils.getEntityRight(rootClass) == EntityRight.CREATE_UPDATE_DELETE) {
                        state = 6;
                    } else if (UserSecurityUtils.getEntityRight(rootClass) != EntityRight.RESTRICTED) {
                        state = 8;
                    }

                }
            }
            //apply state
            if (state == 2) {  //user is admin and we dealing with user object
                create.addItem(pageParameter.getLocalisedText("menu.new", myObject.name()), new MenuBar.Command() {
                    @Override
                    public void menuSelected(MenuBar.MenuItem selectedItem) {
                        pageParameter.setRootClass(rootClass); //save the root element
                        pageParameter.setType(WorkType.CREATE_NEW);
                        Object object = null;
                        try {
                            object = dao.createNew(UserSecurityUtils.getCurrentTenant());

                            BeanView<Object, ?, W> createNewView = new BeanView<>(object, null, null, pageParameter);
                            String targetView = "CREATE_NEW_APPLICATION_" + UUID.randomUUID().toString();
                            navigator.addView(targetView, (View) createNewView);
                            history.clear();
                            history.push(new History("START", "Start"));
                            History his = new History(targetView, pageParameter.getLocalisedText("menu.create.new", myObject.name()));
                            history.push(his);
                            navigator.navigateTo(targetView);
                        } catch (DuplicateDataException ex) {
                            Notification.show(pageParameter.getResourceBundle().getString("dialog.duplicateData"), Notification.Type.WARNING_MESSAGE);

                            Logger.getLogger(Langkuik.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
            if (state == 6) { //non admin user with CREATE and UPDATE rights
                create.addItem(pageParameter.getLocalisedText("menu.new", myObject.name()), new MenuBar.Command() {
                    @Override
                    public void menuSelected(MenuBar.MenuItem selectedItem) {
                        pageParameter.setRootClass(rootClass); //save the root element
                        pageParameter.setType(WorkType.CREATE_NEW);
                        Object object = null;
                        try {
                            object = dao.createNew(UserSecurityUtils.getCurrentTenant());
                            BeanView<Object, ?,W> createNewView = new BeanView<>(object, null, null, pageParameter);
                            String targetView = "CREATE_NEW_APPLICATION_" + UUID.randomUUID().toString();
                            navigator.addView(targetView, (View) createNewView);
                            history.clear();
                            history.push(new History("START", "Start"));
                            History his = new History(targetView, pageParameter.getLocalisedText("menu.create.new", myObject.name()));
                            history.push(his);
                            navigator.navigateTo(targetView);
                        } catch (DuplicateDataException ex) {
                            Notification.show(pageParameter.getResourceBundle().getString("dialog.duplicateData"), Notification.Type.WARNING_MESSAGE);

                            Logger.getLogger(Langkuik.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                });
            }
            if (state == 8 || state == 6) { //non admin and vieweing rights
                MenuBar.MenuItem item = view.addItem(pageParameter.getLocalisedText("menu.view.object", myObject.name()), null);
                for (W worklist:worklistType.getEnumConstants()){
                    
                    item.addItem(worklist.toString(), new MenuBar.Command() {
                        @Override
                        public void menuSelected(MenuBar.MenuItem selectedItem) {
                            pageParameter.setRootClass(rootClass);
                            pageParameter.setType(WorkType.EDIT);
                            pageParameter.setWorklistType(worklist);
                            SearchResultView<?,W> seeApplicationView = new SearchResultView<>(rootClass, pageParameter);
                            String targetView = "VIEW_APPLICATION_" + UUID.randomUUID().toString();
                            navigator.addView(targetView, (View) seeApplicationView);
                            history.clear();
                            history.push(new History("START", "Start"));
                            History his = new History(targetView, pageParameter.getLocalisedText("menu.view.object", myObject.name()));
                            history.push(his);
                            navigator.navigateTo(targetView);
                        }
                    });
                }
            }
        }

        manageCurrentUser.addItem(pageParameter.getLocalisedText("menu.user.manage_myself"), new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                pageParameter.setRootClass(User.class);
                pageParameter.setType(WorkType.EDIT);

                User currentUser = UserSecurityUtils.getCurrentUser();
                //CurrentUser currentUser = new CurrentUser(user);
                BeanView<?, User, W> userView = new BeanView<>(currentUser, pageParameter);
                String targetView = "VIEW_USER_" + UUID.randomUUID().toString();
                navigator.addView(targetView, (View) userView);
                history.clear();
                history.push(new History("START", "Start"));
                History his = new History(targetView, pageParameter.getLocalisedText("menu.view.object", pageParameter.getLocalisedText("menu.user.manage")));
                history.push(his);
                navigator.navigateTo(targetView);

            }
        });
        if (UserSecurityUtils.hasRole("ROLE_ADMIN")) {
            manageCurrentUser.addItem(pageParameter.getLocalisedText("menu.user.manage_users"), new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    pageParameter.setRootClass(User.class);
                    pageParameter.setType(WorkType.EDIT);
                    pageParameter.setWorklistType(null);
                    UserSearchResultView<User, W> seeApplicationView = new UserSearchResultView<>(User.class, pageParameter);
                    String targetView = "VIEW_USERS_" + UUID.randomUUID().toString();
                    navigator.addView(targetView, (View) seeApplicationView);
                    history.clear();
                    history.push(new History("START", "Start"));
                    History his = new History(targetView, "Users");
                    history.push(his);
                    navigator.navigateTo(targetView);

                }
            });
            manageCurrentUser.addItem(pageParameter.getLocalisedText("menu.user.manage_worklist_access"), new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    pageParameter.setRootClass(UserWorklist.class);
                    pageParameter.setType(WorkType.EDIT);
                    SearchResultView<UserWorklist, W> seeApplicationView = new SearchResultView<>(UserWorklist.class, pageParameter);
                    String targetView = "VIEW_WORKLIST_ACCESS_" + UUID.randomUUID().toString();
                    navigator.addView(targetView, (View) seeApplicationView);
                    history.clear();
                    history.push(new History("START", "Start"));
                    History his = new History(targetView, "Worklist Access");
                    history.push(his);
                    navigator.navigateTo(targetView);

                }
            });
        }

        menubar.addItem(pageParameter.getLocalisedText("menu.logout"), null).addItem(pageParameter.getLocalisedText("menu.logout"), new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                ConfirmDialog.show(ui, pageParameter.getLocalisedText("dialog.logout.header"), pageParameter.getLocalisedText("dialog.logout.confirmText"),
                        pageParameter.getLocalisedText("dialog.logout.button.ok"),
                        pageParameter.getLocalisedText("dialog.logout.button.cancel"), new ConfirmDialog.Listener() {
                    @Override
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            UserSecurityUtils.logOutUser(ui);

                        }
                    }

                });

            }
        });
        navigator.navigateTo("START");
        ui.setContent(main);
    }

    class StartView extends VerticalLayout implements View {

        private final PageParameter pageParameter;

        public StartView(PageParameter pageParameter) {
            setSizeFull();
            this.pageParameter = pageParameter;
        }

        @Override
        public void enter(ViewChangeListener.ViewChangeEvent vcevent) {
            //creat bread crumb
            BreadCrumbBuilder.buildBreadCrumb(vcevent.getNavigator(), pageParameter.getBreadcrumb(), pageParameter.getHistory());
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            if (!(auth instanceof AnonymousAuthenticationToken)) {
//                UserDetails userDetails = (UserDetails) auth.getPrincipal();
//                Notification.show(pageParameter.getLocalisedText("page.message.welcome",userDetails.getUsername()));
//                for (GrantedAuthority a : userDetails.getAuthorities()) {
//                    System.out.println(a.getAuthority());
//                }
//            }

        }
    }

}
