/*
 * Copyright 2014 azrulm.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.azrul.langkuik.security.role;

import com.vaadin.ui.UI;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.ManagedType;
import org.azrul.langkuik.annotations.EntityUserMap;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.configs.Configuration;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.azrul.langkuik.annotations.FieldUserMap;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.system.model.role.Role;
import org.azrul.langkuik.system.model.role.RoleDAO;
import org.azrul.langkuik.system.model.user.User;
import org.azrul.langkuik.system.model.user.UserDAO;
//import org.springframework.security.authentication.AnonymousAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

/**
 *
 * @author azrulm
 */
public class UserSecurityUtils {
    
    public static List<String> roles = new ArrayList<>();
    
    public static void init(EntityManagerFactory emf) {
        Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory();
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
        List<String> roles = extractRoles(emf);
        RoleDAO.insert(roles);
        //storeRolesIntoDB(roles);
        roles.addAll(roles);
        
    }
    
    public static List<String> getRoles() {
        return roles;
    }
    
    private static void storeRolesIntoDB(List<String> roles){
        roles.forEach(RoleDAO::registerRole);
    }
    
    
    
   private static List<String> extractRoles(EntityManagerFactory emf) {
        List<String> roles = new ArrayList();
        Set<String> roleSet = new HashSet();
        List<Class<?>> classes = new ArrayList<>();
        for (ManagedType<?> entity : emf.getMetamodel().getManagedTypes()) {
            Class<?> clazz = entity.getJavaType();
            if (clazz == null) {
                continue;
            }
            if (clazz.getAnnotation(WebEntity.class) == null) {
                continue;
            }
            
            if (clazz.isAnnotationPresent(WebEntity.class)) {
                WebEntity we = clazz.getAnnotation(WebEntity.class);
                for (EntityUserMap eum : we.userMap()) {
                    roleSet.add(eum.role());
                }
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    
                    if (field.isAnnotationPresent(WebField.class)) {
                        WebField wf = field.getAnnotation(WebField.class);
                        for (FieldUserMap fum:wf.userMap()){
                            roleSet.add(fum.role());
                        }
                    }
                }
            }
        }
        roles.addAll(roleSet);
        return roles;
    }
    
    public static EntityRight getEntityRight(Class<?> classOfBean/*,
            Set<String> currentUserRoles*/) {
        EntityRight entityRight = null;
        if (classOfBean.isAnnotationPresent(WebEntity.class)) {
            EntityUserMap[] entityUserMaps = classOfBean.getAnnotation(WebEntity.class).userMap();
            if (entityUserMaps != null) {
                for (EntityUserMap e : entityUserMaps) {
                    if (hasRole(e.role())) {
                        entityRight = e.right();
                        break;
                    }
                }
                if (entityRight==null){
                    for (EntityUserMap e : entityUserMaps) {
                        if (("*").equals(e.role())) {
                            entityRight = e.right();
                            break;
                        }
                    }
                }
            }
        }
        return entityRight;
    }
    
    public static boolean hasRole(String role) {
        Subject currentUser = SecurityUtils.getSubject();
        return currentUser.hasRole(role);
    }
    
    public static boolean hasAllRoles(String... roles) {
        Subject currentUser = SecurityUtils.getSubject();
        return currentUser.hasAllRoles(Arrays.asList(roles));
    }

//    public static Set<String> getCurrentUserRoles() {
//        //determine user details
////        UserDetails userDetails = null;
////        Set<String> currentUserRoles = new HashSet<>();
////        
////        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////        if (!(auth instanceof AnonymousAuthenticationToken)) {
////            userDetails = (UserDetails) auth.getPrincipal();
////        } else {
////            //return;
////        }
////        for (GrantedAuthority grantedAuth : userDetails.getAuthorities()) {
////            currentUserRoles.add(grantedAuth.getAuthority());
////        }
////        return currentUserRoles;
//        Subject currentUser = SecurityUtils.getSubject();
//        currentUser.
//    return null;
//    }
//    public static boolean login(String username, char[] password) {
//        Subject currentUser = SecurityUtils.getSubject();
//        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
//        try {
//            currentUser.login(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
    public static boolean login(String username, char[] password) {
        // get the currently executing user:
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        
        if (!currentUser.isAuthenticated()) {
            //collect user principals and credentials in a gui specific manner
            //such as username/password html form, X509 certificate, OpenID, etc.
            //We'll use the username/password example here since it is the most common.
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            //this is all you have to do to support 'remember me' (no config - built in!):
            //token.setRememberMe(rememberMe);

            try {
                currentUser.login(token);
                //System.out.println("User [" + currentUser.getPrincipal().toString() + "] logged in successfully.");

                // save current username in the session, so we have access to our User model
                currentUser.getSession().setAttribute("username", username);
                return true;
            } catch (UnknownAccountException uae) {
                System.out.println("There is no user with username of "
                        + token.getPrincipal());
            } catch (IncorrectCredentialsException ice) {
                System.out.println("Password for account " + token.getPrincipal()
                        + " was incorrect!");
            } catch (LockedAccountException lae) {
                System.out.println("The account for username " + token.getPrincipal()
                        + " is locked.  "
                        + "Please contact your administrator to unlock it.");
            }
        } else {
            return true; // already logged in
        }
        
        return false;
    }
    
    public static boolean authenticated() {
        Subject currentUser = SecurityUtils.getSubject();
        return currentUser.isAuthenticated();
    }
    
    public static boolean isCurrentUserAuditViewer(Configuration config) {
        //Set<String> roles = getCurrentUserRoles();
        String auditViewerRole = config.get("auditViewerRole");
        return SecurityUtils.getSubject().hasRole(auditViewerRole);
        //if (roles.contains(auditViewerRole)) return true; else return false;
    }
    
    public static User getCurrentUser() {
        String username = getCurrentUsername();
        return UserDAO.getUserByUsername(username);
    }
    
    public static String getCurrentUsername() {
        //determine user details
//        UserDetails userDetails = null;
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (!(auth instanceof AnonymousAuthenticationToken)) {
//            userDetails = (UserDetails) auth.getPrincipal();
//        } else {
//            return null;
//        }
//        if (userDetails.getUsername().contains("/")){
//            String[] userPlusTenant = userDetails.getUsername().split("/");
//            return userPlusTenant[0];
//        }else{
//            return userDetails.getUsername();
//        }
        String principal = (String) SecurityUtils.getSubject().getPrincipal();
        if (principal==null){
            return "NON_USER";
        }
        if (principal.contains("/")) {
            String[] userPlusTenant = principal.split("/");
            return userPlusTenant[0];
        } else {
            return principal;
        }
    }
    
    public static String getCurrentTenant() {
        //determine user details
//        UserDetails userDetails = null;
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (!(auth instanceof AnonymousAuthenticationToken)) {
//            userDetails = (UserDetails) auth.getPrincipal();
//        } else {
//            return null;
//        }
//        if (userDetails.getUsername().contains("/")){
//            String[] userPlusTenant = userDetails.getUsername().split("/");
//            return userPlusTenant[1];
//        }else{
//            return null;
//        }
        String principal = (String) SecurityUtils.getSubject().getPrincipal();
        if (principal.contains("/")) {
            String[] userPlusTenant = principal.split("/");
            return userPlusTenant[1];
        } else {
            return null;
        }
    }
    
    public static void logOutUser(UI ui) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        SecurityContextLogoutHandler ctxLogOut = new SecurityContextLogoutHandler();
//        ctxLogOut.logout(req, resp, auth);
        SecurityUtils.getSubject().logout();
        ui.getSession().close();
        ui.getPage().setLocation("");
//        try {
//            
//        } catch (ServletException ex) {
//            Logger.getLogger(UserSecurityUtils.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(UserSecurityUtils.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
