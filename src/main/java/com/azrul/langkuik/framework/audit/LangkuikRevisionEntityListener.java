/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.audit;

import com.vaadin.flow.server.VaadinSession;
import java.sql.Timestamp;
import org.hibernate.envers.RevisionListener;

/**
 *
 * @author azrul
 */
public class LangkuikRevisionEntityListener implements RevisionListener{

    @Override
    public void newRevision(Object o) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session!=null) {
            String username = (String)session.getSession().getAttribute("USERNAME");
            AuditMetadata re = (AuditMetadata)o;
            re.setUsername(username);
            re.setDateTime(new Timestamp(System.currentTimeMillis()));
        }
        
    }
    
}
