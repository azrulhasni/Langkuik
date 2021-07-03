/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik;

import com.azrul.langkuik.framework.dao.DataAccessObject;
import com.azrul.langkuik.framework.dao.LookupQuery;
import com.azrul.langkuik.loanorigsystem.model.DistrictStateCountry;
import java.util.Collection;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author azrul
 */
@Component
public class CachePreLoader {
    @Autowired
    DataAccessObject dao;
    
    @PostConstruct
    void preload(){
        LookupQuery query = new LookupQuery(DistrictStateCountry.class);
        Collection data = dao.runQuery(query);
//        int size = CacheManager.ALL_CACHE_MANAGERS.get(0)
//            .getCache("com.azrul.langkuik.loanorigsystem.model.DistrictStateCountry").getSize();
//        System.out.println("################ CACHE SIZE = "+size);
    }
}
