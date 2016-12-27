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
import java.util.Arrays;
import org.azrul.langkuik.annotations.EntityUserMap;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.configs.Configuration;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
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

    public static EntityOperation getEntityRight(Class<?> classOfBean/*,
            Set<String> currentUserRoles*/) {
        EntityOperation entityRight = null;
        if (classOfBean.isAnnotationPresent(WebEntity.class)) {
            EntityUserMap[] entityUserMaps = classOfBean.getAnnotation(WebEntity.class).userMap();
            if (entityUserMaps != null) {
                for (EntityUserMap e : entityUserMaps) {
                    if (hasRole(e.role()) || ("*").equals(e.role())) {
                        entityRight = e.right();
                        break;
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
    public static boolean login(String username, char[] password) {
        Subject currentUser = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        try {
            currentUser.login(token);
            return true;
        } catch (Exception e) {
            return false;
        }
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

    public static String getCurrentUser() {
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
