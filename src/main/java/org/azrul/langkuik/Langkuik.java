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
import com.vaadin.server.VaadinService;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.configs.Configuration;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.dao.EntityUtils;
import org.azrul.langkuik.dao.HibernateGenericDAO;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.customtype.attachment.AttachmentCustomType;
import org.azrul.langkuik.framework.relationship.RelationManagerFactory;
import org.azrul.langkuik.framework.webgui.BeanView;
import org.azrul.langkuik.framework.webgui.SearchResultView;
import org.azrul.langkuik.framework.webgui.breadcrumb.BreadCrumbBuilder;
import org.azrul.langkuik.framework.webgui.breadcrumb.History;
import org.azrul.langkuik.security.role.SecurityUtils;
import org.vaadin.dialogs.ConfirmDialog;

public class Langkuik implements Serializable{
    
     public void initLangkuik(final EntityManagerFactory emf, 
             final UI ui, 
             final RelationManagerFactory relationManagerFactory) {
         List<Class<?>> customTypeInterfaces = new ArrayList<>();
         initLangkuik(emf,ui,relationManagerFactory,customTypeInterfaces);
     }
     
     public void massIndex(final EntityManagerFactory emf){
         HibernateGenericDAO.massIndexDatabaseForSearch(emf);
     }
     
  

    public void initLangkuik(final EntityManagerFactory emf, 
            final UI ui, 
            final RelationManagerFactory relationManagerFactory, 
            List<Class<?>> customTypeInterfaces) {
        
        
        List<Class<?>> rootClasses = EntityUtils.getAllRootEntities(emf);

      
        
        //Manage custom type
        if (customTypeInterfaces ==null){
            customTypeInterfaces = new ArrayList<>();
        }
        
        //add system level custom type
        customTypeInterfaces.add(AttachmentCustomType.class);
        //create DAOs for custom types
        final List<DataAccessObject<?>> customTypeDaos = new ArrayList<>();
        for (Class<?> clazz:customTypeInterfaces){
            customTypeDaos.add(new HibernateGenericDAO(emf,clazz));
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

        final PageParameter pageParameter = new PageParameter(customTypeDaos,
                emf, 
                relationManagerFactory, 
                history, 
                config, 
                breadcrumb);
        
        history.push(new History("START", pageParameter.getLocalisedText("history.start")));
        StartView startView = new StartView(pageParameter);
        navigator.addView("START", startView);
        MenuBar.MenuItem create = menubar.addItem(pageParameter.getLocalisedText("menu.create"), null);
        MenuBar.MenuItem view = menubar.addItem(pageParameter.getLocalisedText("menu.view"), null);
        
        
        
        
        for (final Class rootClass : rootClasses) {
            final WebEntity myObject = (WebEntity) rootClass.getAnnotation(WebEntity.class);
            final DataAccessObject<?> dao = new HibernateGenericDAO<>(emf, rootClass);
            create.addItem(pageParameter.getLocalisedText("menu.new", myObject.name()), new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    pageParameter.setRootClass(rootClass); //save the root element
                    pageParameter.setType(WorkType.CREATE_NEW);
                    Object object = dao.createNew(SecurityUtils.getCurrentTenant());
                    BeanView<Object, ?> createNewView = new BeanView<>(object, null,null,pageParameter );
                    String targetView = "CREATE_NEW_APPLICATION_" + UUID.randomUUID().toString();
                    navigator.addView(targetView,(View)createNewView);
                    history.clear();
                    history.push(new History("START", "Start"));
                    History his = new History(targetView,pageParameter.getLocalisedText("menu.create.new", myObject.name()));
                    history.push(his);
                    navigator.navigateTo(targetView);
                }
            });
            view.addItem(pageParameter.getLocalisedText("menu.view.object",myObject.name()), new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    pageParameter.setRootClass(rootClass);
                    pageParameter.setType(WorkType.EDIT);
                    SearchResultView<?> seeApplicationView = new SearchResultView<>(rootClass, pageParameter);
                    String targetView = "VIEW_APPLICATION_" + UUID.randomUUID().toString();
                    navigator.addView(targetView, (View)seeApplicationView);
                    history.clear();
                    history.push(new History("START", "Start"));
                    History his = new History(targetView, pageParameter.getLocalisedText("menu.view.object",myObject.name()));
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
                                    HttpServletRequest req = (HttpServletRequest) VaadinService.getCurrentRequest();
                                    HttpServletResponse resp = (HttpServletResponse) VaadinService.getCurrentResponse();
                                    SecurityUtils.logOutUser(req, resp);
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
            this.pageParameter=pageParameter;
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