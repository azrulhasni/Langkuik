/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.dao;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.ParameterizedBridge;
import org.hibernate.search.bridge.TwoWayFieldBridge;
import org.hibernate.search.bridge.builtin.impl.DateResolutionUtil;
import org.hibernate.search.bridge.spi.EncodingBridge;
import org.hibernate.search.bridge.spi.IgnoreAnalyzerBridge;
import org.hibernate.search.bridge.spi.NullMarker;
import org.hibernate.search.bridge.util.impl.ToStringNullMarker;
import org.hibernate.search.metadata.NumericFieldSettingsDescriptor.NumericEncodingType;
import org.hibernate.search.util.logging.impl.Log;
import org.hibernate.search.util.logging.impl.LoggerFactory;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 *
 * @author azrul
 */
public class Date2LongFieldBridge implements TwoWayFieldBridge, ParameterizedBridge, IgnoreAnalyzerBridge, EncodingBridge {

	private static final Log LOG = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	public static final TwoWayFieldBridge DATE_YEAR = new Date2LongFieldBridge( Resolution.YEAR );
	public static final TwoWayFieldBridge DATE_MONTH = new Date2LongFieldBridge( Resolution.MONTH );
	public static final TwoWayFieldBridge DATE_DAY = new Date2LongFieldBridge( Resolution.DAY );
	public static final TwoWayFieldBridge DATE_HOUR = new Date2LongFieldBridge( Resolution.HOUR );
	public static final TwoWayFieldBridge DATE_MINUTE = new Date2LongFieldBridge( Resolution.MINUTE );
	public static final TwoWayFieldBridge DATE_SECOND = new Date2LongFieldBridge( Resolution.SECOND );
	public static final TwoWayFieldBridge DATE_MILLISECOND = new Date2LongFieldBridge( Resolution.MILLISECOND );

	private DateTools.Resolution resolution;

	public Date2LongFieldBridge() {
		this( Resolution.MILLISECOND );
	}

	public Date2LongFieldBridge(Resolution resolution) {
		this.resolution = DateResolutionUtil.getLuceneResolution( resolution );
	}

	@Override
	public Object get(String name, Document document) {
		final IndexableField field = document.getField( name );
		if ( field != null ) {
			return new Date( (long) field.numericValue() );
		}
		else {
			return null;
		}
	}

	@Override
	public String objectToString(Object object) {
            LocalDate localdate = (LocalDate) object;
		return object != null ? Long.toString(localdate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()) : null;
	}

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if ( value == null ) {
			return;
		}

		LocalDate localdate = (LocalDate) value;
		long numericDate = DateTools.round(localdate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), resolution );
		luceneOptions.addNumericFieldToDocument( name, numericDate, document );
	}

	@Override
	public void setParameterValues(Map<String, String> parameters) {
		String resolution = parameters.get( "resolution" );
		Resolution hibResolution = Resolution.valueOf( resolution.toUpperCase( Locale.ENGLISH ) );
		this.resolution = DateResolutionUtil.getLuceneResolution( hibResolution );
	}

	public DateTools.Resolution getResolution() {
		return resolution;
	}
	@Override
	public NumericEncodingType getEncodingType() {
		return NumericEncodingType.LONG;
	}

	@Override
	public NullMarker createNullMarker(String indexNullAs) throws IllegalArgumentException {
		try {
			return new ToStringNullMarker( Long.parseLong( indexNullAs ) );
		}
		catch (NumberFormatException e) {
			throw LOG.invalidNullMarkerForLong( e );
		}
	}
}