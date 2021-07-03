package com.azrul.langkuik.views.main;

import com.azrul.langkuik.framework.annotation.WebEntity;
import com.azrul.langkuik.framework.workflow.Workflow;
import com.azrul.langkuik.framework.workflow.model.Activity;
import com.azrul.langkuik.loanorigsystem.model.Application;
import com.azrul.langkuik.loanorigsystem.model.RelationshipManager;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import com.azrul.langkuik.loanorigsystem.view.ApplicationMDV;
import com.azrul.langkuik.loanorigsystem.view.RelationshipManagerMDV;
import com.azrul.langkuik.views.table.TableView;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteData;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.VaadinSession;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The main view is a top-level placeholder for other views.
 */
//@JsModule("./styles/shared-styles.js")
//@PWA(name = "Langkuik", shortName = "Langkuik", startPath = "main")
@Route("main")
@Theme(value = Lumo.class, variant = Lumo.LIGHT)
//@Theme(value = Material.class)
public class MainView extends AppLayout {

    //private final Tabs menu;
    @Autowired
    private List<Workflow<Application>> workflows;

    public MainView() {

    }

    @PostConstruct
    public void construct() {
        boolean authenticated = getKeycloakPrinciple();
        if (authenticated) {
            setPrimarySection(Section.DRAWER);
            //addToNavbar(new FirstView());
            addToNavbar(true, new DrawerToggle());
            Tabs menu = createMenuTabs();
            addToDrawer(menu);
            RoleContainer r1 = new RoleContainer();
            r1.setName("AAAA");
            r1.getActivities().put("aaa1", "123");
            r1.getActivities().put("aaa2", "1234");
            r1.getActivities().put("aaa3", "12345");
            
            TreeGrid<RoleContainer> grid = new TreeGrid<RoleContainer>();
            //grid.
            
        } else {

        }
    }

    private Tabs createMenuTabs() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        //tabs.add(getAvailableTabs());
        tabs.add(getWorkflowTabs());
        return tabs;
    }

    public boolean getKeycloakPrinciple() {
        if (!SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal().equals("anonymousUser")) {
            KeycloakPrincipal principal
                    = (KeycloakPrincipal) SecurityContextHolder.getContext()
                            .getAuthentication().getPrincipal();

            KeycloakSecurityContext keycloakSecurityContext
                    = principal.getKeycloakSecurityContext();
            Set<String> roles = principal.getKeycloakSecurityContext().getToken().getRealmAccess().getRoles();
            String realm = principal.getKeycloakSecurityContext().getRealm();
            String preferredUsername = keycloakSecurityContext.getIdToken().getPreferredUsername();
            VaadinSession.getCurrent().getSession().setAttribute("ROLES", roles);
            VaadinSession.getCurrent().getSession().setAttribute("USERNAME", preferredUsername);
            VaadinSession.getCurrent().getSession().setAttribute("TENANT", realm);
            for (Map.Entry<String, Object> e : principal.getKeycloakSecurityContext().getToken().getOtherClaims().entrySet()) {
                //System.out.println("data: key:"+e.getKey()+" === value:"+e.getValue());
                VaadinSession.getCurrent().getSession().setAttribute(e.getKey(), e.getValue());
            }
            return true;
        } else {
            return false;
        }
    }

    private Tab[] getWorkflowTabs() {
        Set<String> currentRoles = (Set<String>) VaadinSession.getCurrent().getSession().getAttribute("ROLES");
        final List<Tab> tabs = new ArrayList<>();
        for (Workflow workflow : workflows) {
            Map<String, List<Activity>> roles = workflow.getRoleActivityMap();
            Class appMDVClass = ApplicationMDV.class;

            Map<String, List<Activity>> roleActivity = workflow.getRoleActivityMap();
            for (String currentRole : currentRoles) {
                if (roles.containsKey(currentRole)) {
                    List<Activity> activs = roleActivity.get(currentRole);
                    for (Activity activ : activs) {
                        tabs.add(createTab((Application.class.getAnnotation(WebEntity.class)).name() + "::" + currentRole + "::" + activ.getDescription(), appMDVClass, activ.getId()));
                    }
                }
            }

        }
        return tabs.toArray(new Tab[tabs.size()]);
    }

    private static Tab[] getAvailableTabs() {
        final List<Tab> tabs = new ArrayList<>();

//         Class applicationMDV = new ByteBuddy()
//                .subclass(TypeDescription.Generic.Builder.parameterizedType(TableView.class, Application.class)
//                    .build())
//                .make()
//                .load(TableView.class.getClassLoader())
//                .getLoaded();
        //RouteConfiguration.forSessionScope().setRoute("Application", applicationMDV, MainView.class);
//         tabs.add(createTab("Application",applicationMDV));
//        Class addressMDV = new ByteBuddy()
//                .subclass(TypeDescription.Generic.Builder.parameterizedType(TableView.class, Address.class)
//                    .build())
//                .make()
//                .load(TableView.class.getClassLoader())
//                .getLoaded();
//
//        Class applicantMDV = new ByteBuddy()
//                .subclass(TypeDescription.Generic.Builder.parameterizedType(TableView.class, Applicant.class)
//                    .build())
//                .make()
//                .load(TableView.class.getClassLoader())
//                .getLoaded();
//        RouteConfiguration.forSessionScope().setRoute("address", addressMDV, MainView.class);
//        RouteConfiguration.forSessionScope().setRoute("applicant", applicantMDV, MainView.class);
//
//        for (RouteData routeData : RouteConfiguration.forSessionScope().getAvailableRoutes()) {
//            tabs.add(createTab(routeData.getUrl(), routeData.getNavigationTarget()));
//        }
//        tabs.add(createTab((Address.class.getAnnotation(WebEntity.class)).name(),AddressMDV.class));
//        tabs.add(createTab((Applicant.class.g etAnnotation(WebEntity.class)).name(),ApplicantMDV.class));
        Class appMDVClass = ApplicationMDV.class;
        //RouteConfiguration.forSessionScope().setRoute("application", appMDVClass);
        // RouteConfiguration.forSessionScope().setAnnotatedRoute(appMDVClass);

        //tabs.add(createTab((Application.class.getAnnotation(WebEntity.class)).name(),appMDVClass));
//        tabs.add(createTab((Collateral.class.getAnnotation(WebEntity.class)).name(),CollateralMDV.class));
//        tabs.add(createTab((Product.class.getAnnotation(WebEntity.class)).name(),ProductMDV.class));
//        tabs.add(createTab((RelationshipManager.class.getAnnotation(WebEntity.class)).name(),RelationshipManagerMDV.class));
        return tabs.toArray(new Tab[tabs.size()]);
    }

    private static Tab createTab(String title, Class<? extends Component> viewClass, String activityId) {
        //return createTab(populateLink(new RouterLink(null, viewClass),title));
        QueryParameters params = QueryParameters.simple(Map.of("worklist", activityId));
        
        //RouterLink link = new RouterLink(null, viewClass, new RouteParameters("worklist",activityId));
        RouterLink link = new RouterLink(null, viewClass);
        link.setQueryParameters(params);
        
        
        return createTab(createTab(populateLink(link, title)));
    }

    private static Tab createTab(Component content) {
        final Tab tab = new Tab();
        tab.add(content);
        return tab;
    }

    private static <T extends HasComponents> T populateLink(T a, String title) {
        a.add(title);
        return a;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        //selectTab();
    }

    /*private void selectTab() {
        String target = RouteConfiguration.forSessionScope().getUrl(getContent().getClass());
        Optional<Component> tabToSelect = menu.getChildren().filter(tab -> {
            Component child = tab.getChildren().findFirst().get();
            return child instanceof RouterLink && ((RouterLink) child).getHref().equals(target);
        }).findFirst();
        tabToSelect.ifPresent(tab -> menu.setSelectedTab((Tab) tab));
    }*/
}
