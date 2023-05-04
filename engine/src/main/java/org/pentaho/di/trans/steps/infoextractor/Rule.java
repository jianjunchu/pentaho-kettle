package org.pentaho.di.trans.steps.infoextractor;

import org.pentaho.di.i18n.BaseMessages;

public class Rule {
	private static Class<?> PKG = InfoExtractorMeta.class;
	private  String[] definedDigitals;
	
	private  String keyWord;
	

	private  long order;
	private  int minDigitals;

	private String contentMark;
	
	public static int CONTENT_MODE_DIGITAL = 1;
	public static int CONTENT_MODE_TEXT = 2;
	public static int CONTENT_MODE_EMAIL = 3;
	public static int CONTENT_MODE_PHONE = 4;
	public static String[] CONTENT_MODE_DESC = new String[]{BaseMessages.getString(PKG,"InfoExtractorDialog.CONTENT_MODE_DESC.NUMBER"),BaseMessages.getString(PKG,"InfoExtractorDialog.CONTENT_MODE_DESC.TEXT"),BaseMessages.getString(PKG,"InfoExtractorDialog.CONTENT_MODE_DESC.EMAIL"),BaseMessages.getString(PKG,"InfoExtractorDialog.CONTENT_MODE_DESC.PHONE")};
	private  int contentMode;
	
	public static int SEARCH_MODE_ALL = 1;
	public static int SEARCH_MODE_FIRST = 2;
	public static String[] SEARCH_MODE_DESC = new String[]{BaseMessages.getString(PKG,"InfoExtractorDialog.SEARCH_MODE_DESC.ALL"),BaseMessages.getString(PKG,"InfoExtractorDialog.SEARCH_MODE_DESC.FIRST")};
	private  int searchMode;
	
	public static int POSITION_AFTER = 1;
	public static int POSITION_BEFORE = 2;
	public static String[] POSITION_MODE_DESC = new String[]{BaseMessages.getString(PKG,"InfoExtractorDialog.POSITION_MODE_DESC.AFTER"),BaseMessages.getString(PKG,"InfoExtractorDialog.POSITION_MODE_DESC.BEFORE")};
	private  int position;
	
	private long idField;
	private long idMeasure;
	
	public String[] getDefinedDigitals() {
		return definedDigitals;
	}
	public void setDefinedDigitals(String[] definedDigitals) {
		this.definedDigitals = definedDigitals;
	}
	public String getKeyWord() {
		return keyWord;
	}
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public long getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public int getMinDigitals() {
		return minDigitals;
	}
	public void setMinDigitals(int minDigitals) {
		this.minDigitals = minDigitals;
	}
	public String getContentMark() {
		return contentMark;
	}
	public void setContentMark(String contentMark) {
		this.contentMark = contentMark;
	}
	public int getContentMode() {
		return contentMode;
	}
	public void setContentMode(int contentMode) {
		this.contentMode = contentMode;
	}
	public int getSearchMode() {
		return searchMode;
	}
	public void setSearchMode(int searchMode) {
		this.searchMode = searchMode;
	}
	
	public long getIdMeasure() {
		return idMeasure;
	}
	
	public void setIdMeasure(int idMeasure) {
		this.idMeasure = idMeasure;
	}
	public long getIdField() {
		return idField;
	}
	public void setIdField(int idField) {
		this.idField = idField;
	}
	public static String[] getPosiitons() {
		return null;
	}
	public static String[] getContentModes() {
		// TODO Auto-generated method stub
		return CONTENT_MODE_DESC;
	}
	
	public static int getContentMode(String contentModeName)
	{
		if(contentModeName.endsWith(BaseMessages.getString(PKG,"InfoExtractorDialog.CONTENT_MODE_DESC.NUMBER")))
			return CONTENT_MODE_DIGITAL;
		else if(contentModeName.endsWith(BaseMessages.getString(PKG,"InfoExtractorDialog.CONTENT_MODE_DESC.TEXT")))
			return CONTENT_MODE_TEXT;
		else if(contentModeName.endsWith(BaseMessages.getString(PKG,"InfoExtractorDialog.CONTENT_MODE_DESC.EMAIL")))
			return CONTENT_MODE_EMAIL;
		else if(contentModeName.endsWith(BaseMessages.getString(PKG,"InfoExtractorDialog.CONTENT_MODE_DESC.PHONE")))
			return CONTENT_MODE_PHONE;
		else
			return -1;
		
	}
}
