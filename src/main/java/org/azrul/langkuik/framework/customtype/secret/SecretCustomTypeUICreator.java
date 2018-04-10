/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.customtype.secret;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Window;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.customtype.CustomType;
import org.azrul.langkuik.framework.customtype.CustomTypeUICreator;
import org.azrul.langkuik.framework.webgui.BeanView;

import org.azrul.langkuik.security.role.FieldState;
import org.azrul.langkuik.system.security.HashAndSalt;
import org.azrul.langkuik.system.model.user.Secret;
import org.azrul.langkuik.system.model.user.SecretUtils;
import org.azrul.langkuik.system.model.user.UserDAO;

/**
 *
 * @author azrulm
 * @param <P>
 */
public class SecretCustomTypeUICreator<P,W> implements CustomTypeUICreator<P,W> {

    @Override
    public Component createUIForForm(final P currentBean,
            final Class<? extends CustomType> secretClass,
            final String pojoFieldName,
            final BeanView beanView,
            final DataAccessObject<P,W> conatainerClassDao,
            final DataAccessObject<? extends CustomType,W> customTypeDao,
            final PageParameter<W> pageParameter,
            final FieldState fieldState,
            final Window window) {

        final FormLayout form = new FormLayout();
        final PasswordField passwordField1 = new PasswordField(pageParameter.getLocalisedText("password.1stfield.caption"));
        final PasswordField passwordField2 = new PasswordField(pageParameter.getLocalisedText("password.2ndfield.caption"));

        form.addComponent(passwordField1);
        form.addComponent(passwordField2);
        
        Button saveWindowBtn =  new Button(pageParameter.getLocalisedText("password.save.caption"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                try {
                    //beanView.setCurrentBean(currentBean);
                    int e=0;
                    if (passwordField1.getValue()==null){
                        e=1;
                    }
                    if (passwordField2.getValue()==null){
                        e=1;
                    }
                    if (!passwordField1.getValue().equals(passwordField2.getValue())) {
                        e=1;
                    }
                    if (e==0){
                        Field field = currentBean.getClass().getDeclaredField(pojoFieldName);
                        HashAndSalt has = SecretUtils.getHashAndSalt((String) passwordField1.getValue());
                        field.setAccessible(true);
                        Secret secret = new Secret();
                        Secret currentSecret = (Secret) field.get(currentBean);
                        if (currentSecret!=null){
                            secret.setId(currentSecret.getId());
                            secret.setHashedPassword(has.getHashedPassword());
                            secret.setSalt(has.getSalt().toString());
                        }else{
                            UserDAO.assignId(secret);
                            secret.setHashedPassword(has.getHashedPassword());
                            secret.setSalt(has.getSalt().toString());
                        }
                        
                        field.set(currentBean, secret);
                        window.close();
                    }else{
                         Notification.show(pageParameter.getLocalisedText("password.mismatch.warning"), Notification.Type.HUMANIZED_MESSAGE);
                    }
                    
                } catch (NoSuchFieldException ex) {
                    Logger.getLogger(SecretCustomTypeUICreator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(SecretCustomTypeUICreator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(SecretCustomTypeUICreator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(SecretCustomTypeUICreator.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        Button closeWindowBtn = new Button(pageParameter.getLocalisedText("dialog.general.button.close"), new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
              
                        window.close();
                   
                    
               

            }
        });
        saveWindowBtn.setId(saveWindowBtn.getCaption());
        form.addComponent(saveWindowBtn);
        closeWindowBtn.setId(closeWindowBtn.getCaption());
        form.addComponent(closeWindowBtn);
        form.setMargin(true);
        //beanView.addComponent(form);
        return form;

    }

}
