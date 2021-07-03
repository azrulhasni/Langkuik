/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.custom.lookupchoice;

import com.azrul.langkuik.custom.CustomFieldRenderer;
import com.azrul.langkuik.framework.dao.DataAccessObject;
import com.azrul.langkuik.framework.dao.Dual;
import com.azrul.langkuik.framework.dao.LookupQuery;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.ListDataProvider;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author azrul
 * @param <P>
 */
public class LookupRenderer<P, L> implements CustomFieldRenderer<P, L> {

    @Autowired
    DataAccessObject dao;

    @Override
    public Optional<AbstractField> create(P bean,
            final String fieldName, String displayName, Map<String, AbstractField> fieldsInForm) {
        //by default load all
        try {
            Lookup lookup = bean.getClass().getDeclaredField(fieldName).getAnnotation(Lookup.class);
            Class<L> lookupEntityClass = lookup.entity();
            LookupQuery<L, String> defLookupQuery = new LookupQuery<>(lookupEntityClass,
                            lookup.field());
            defLookupQuery = new LookupQuery<>(lookupEntityClass,
                            lookup.field());
            
//            Collection<Dual<AbstractField,Lookup>> fieldsFilteredByThis = new ArrayList<>();
//            for (Field f:bean.getClass().getDeclaredFields()){
//                Lookup sLookup = f.getAnnotation(Lookup.class);
//                if (sLookup!=null){
//                    if (f.getName().equals(sLookup.filterBy())){
//                        fieldsFilteredByThis.add(new Dual(fieldsInForm.get(f.getName()),sLookup));
//                    }
//                }
//            }
            
            ComboBox comboBox = new ComboBox(displayName);
            comboBox.setDataProvider(new ListDataProvider(dao.runQuery(defLookupQuery)));

            comboBox.addFocusListener(e -> {

                LookupQuery<L, String> lookupQuery = null;
                if ("".equals(lookup.filterBy())) {
                    lookupQuery = new LookupQuery<>(lookupEntityClass,
                            lookup.field());
                } else {
                    if (fieldsInForm.get(lookup.filterBy())==null){
                        lookupQuery = new LookupQuery<>(lookupEntityClass,
                            lookup.field()); 
                    }else{
                        lookupQuery = new LookupQuery<>(lookupEntityClass,
                                lookup.field(), lookup.filterBy(),
                                (String) fieldsInForm.get(lookup.filterBy()).getValue()); 
                    }
                }
                comboBox.setDataProvider(new ListDataProvider(dao.runQuery(lookupQuery)));
            });
            
            comboBox.addValueChangeListener(e->{
                if (!"".equals(lookup.filterBy())){
                    LookupQuery<L, String> mlookupQuery = new LookupQuery<>(
                            lookupEntityClass,
                            lookup.filterBy(),
                            lookup.field(), 
                            (String)e.getValue());

                    String chosen = (String) dao.runQuery(mlookupQuery).iterator().next();
                    ComboBox mCombo = (ComboBox) fieldsInForm.get(lookup.filterBy());
                    mCombo.setValue(chosen);
                }
            });

            return Optional.of(comboBox);
        } catch (NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(LookupRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Optional.empty();
    }

}
