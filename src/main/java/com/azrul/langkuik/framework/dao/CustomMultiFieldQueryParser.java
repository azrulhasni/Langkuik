/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.dao;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.NumericRangeQuery;

/**
 *
 * @author azrul
 */
public class CustomMultiFieldQueryParser extends MultiFieldQueryParser {

    private String dateFormat = null;

    private Class tclass;

    public CustomMultiFieldQueryParser(String[] fields, String dateFormat, Analyzer analyzer, Class tclass) {
        super(fields, analyzer);
        this.tclass = tclass;
        this.dateFormat = dateFormat;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected org.apache.lucene.search.Query getRangeQuery(String baseFieldName, String min, String max, boolean minInclusive,
            boolean maxInclusive) throws ParseException {

        Field field = EntityUtils.getFieldBySearchFieldName(tclass, baseFieldName);
        if (!(field.getType().equals(String.class)/*||field.getType().equals(LocalDate.class)*/)) {
            String fieldName = baseFieldName + "4range";
            if (Long.class.equals(field.getType())) {
                return NumericRangeQuery.newLongRange(fieldName, Long.parseLong(min), Long.parseLong(max), minInclusive, maxInclusive);
            } else if (Integer.class.equals(field.getType())) {
                return NumericRangeQuery.newIntRange(fieldName, Integer.parseInt(min), Integer.parseInt(max), minInclusive, maxInclusive);
            } else if (Double.class.equals(field.getType())) {
                return NumericRangeQuery.newDoubleRange(fieldName, Double.parseDouble(min), Double.parseDouble(max), minInclusive, maxInclusive);
            } else if (BigDecimal.class.equals(field.getType())) {
                return NumericRangeQuery.newDoubleRange(fieldName, (new BigDecimal(min)).doubleValue(), (new BigDecimal(max)).doubleValue(), minInclusive, maxInclusive);
            }else if (LocalDate.class.equals(field.getType())) {
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                try {
                    return NumericRangeQuery.newLongRange(fieldName, Long.valueOf(sdf.parse(min).getTime()), Long.valueOf(sdf.parse(max).getTime()), minInclusive, maxInclusive);
                } catch (java.text.ParseException ex) {
                    Logger.getLogger(CustomMultiFieldQueryParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
//        if(tclass.getD) {
//            return NumericRangeQuery.newLongRange(fieldName, Long.parseLong(min), Long.parseLong(max), minInclusive, maxInclusive);
//        }
        return super.getRangeQuery(baseFieldName, min, max, minInclusive, maxInclusive);
    }
}
