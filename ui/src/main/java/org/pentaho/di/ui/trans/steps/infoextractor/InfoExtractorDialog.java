 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 


package org.pentaho.di.ui.trans.steps.infoextractor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.infoextractor.InfoExtractorMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
//import com.xgn.ketl.trans.step.numberextractor.Messages;


public class InfoExtractorDialog extends BaseStepDialog implements StepDialogInterface
{ 
	
	private static Class<?> PKG = InfoExtractorMeta.class;
	private Label wlResultField;
	private Text wResultField;
	private FormData fdlResultField, fdResultField;

	private Label wlKeyword;
	private Text wKeyword;
	private FormData fdlKeyword, fdKeyword;

	private Label wlInfoType;
	private Button wInfoTypeText, wInfoTypeNumber,wInfoTypeDate;
	private FormData fdlInfoType, fdInfoType;

	private Label wlRExp;
	private Text wRExp;
	private FormData fdlRExp, fdRExp;


	private Label wlFromField;
	private CCombo wFromField;
	private FormData fdlFromField, fdFromField;

	private Label wlExtractType;
	private Button wExtractByKeyword, wExtractByRExp;
	private FormData fdlExtractType, fdExtractType;

	private Label        wlContentMark;
//	private Text    	 wContentMark;
//	private FormData     fdlContentMark, fdContentMark;

//	private Label        wlFields;
//	private TableView    wFields;
//	private FormData     fdlFields, fdFields;

	private InfoExtractorMeta input;
	private boolean gotPreviousFields = false;


	public InfoExtractorDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(InfoExtractorMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG,"InfoExtractorDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG,"System.Label.StepName"));
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		wlFromField = new Label(shell, SWT.RIGHT );
		wlFromField.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.FromField.Label" ) );
		props.setLook(wlFromField);
		fdlFromField = new FormData();
		fdlFromField.left = new FormAttachment( 0, 0 );
		fdlFromField.top = new FormAttachment( wStepname, margin );
		fdlFromField.right = new FormAttachment( middle, -margin );
		wlFromField.setLayoutData(fdlFromField);
		wFromField = new CCombo(shell, SWT.BORDER | SWT.READ_ONLY );
		wFromField.setEditable( true );
		props.setLook(wFromField);
		wFromField.addModifyListener( lsMod );
		fdFromField = new FormData();
		fdFromField.left = new FormAttachment( middle, 0 );
		fdFromField.top = new FormAttachment( wStepname, margin );
		fdFromField.right = new FormAttachment( 100, 0 );
		wFromField.setLayoutData(fdFromField);
		wFromField.addFocusListener(new FocusListener() {
			public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
			}
			public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
				setDynamicFilenameField();
			}
		} );

		//Extract Type
		wlExtractType = new Label( shell, SWT.RIGHT );
		wlExtractType.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.ExtractType.Label" ) );
		props.setLook(wlExtractType);
		fdlExtractType = new FormData();
		fdlExtractType.left = new FormAttachment( 0, 0 );
		fdlExtractType.top = new FormAttachment(wFromField, margin );
		fdlExtractType.right = new FormAttachment( middle, -margin );
		wlExtractType.setLayoutData(fdlExtractType);

		wExtractByKeyword = new Button( shell, SWT.RADIO );
		props.setLook(wExtractByKeyword);
		wExtractByKeyword.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.ExtractType.Keyword" )  );
		wExtractByKeyword.setToolTipText( BaseMessages.getString( PKG, "InfoExtractorDialog.ExtractType.Keyword.Tooltip" ) );
		fdExtractType = new FormData();
		fdExtractType.left = new FormAttachment( middle, 0 );
		fdExtractType.top = new FormAttachment(wFromField, margin );
		wExtractByKeyword.setLayoutData(fdExtractType);
		wExtractByKeyword.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				input.setChanged();
			}
		} );

		wExtractByRExp = new Button( shell, SWT.RADIO );
		props.setLook(wExtractByRExp);
		wExtractByRExp.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.ExtractType.RegExp" ) );
		wExtractByRExp.setToolTipText( BaseMessages.getString( PKG, "InfoExtractorDialog.ExtractType.RegExp.Tooltip" ) );
		fdExtractType = new FormData();
		fdExtractType.left = new FormAttachment(wExtractByKeyword, 0 );
		fdExtractType.top = new FormAttachment(wFromField, margin );
		wExtractByRExp.setLayoutData(fdExtractType);
		wExtractByRExp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				input.setChanged();
			}
		} );

//		wExtractByRExp = new Button( shell, SWT.RADIO );
//		props.setLook(wExtractByRExp);
//		wExtractByRExp.setText( "doc/docx" );
//		wExtractByRExp.setToolTipText( BaseMessages.getString( PKG, "InfoExtractorDialog.docFile.Tooltip" ) );
//		fdExtractType = new FormData();
//		fdExtractType.left = new FormAttachment(wExtractByKeyword, 0 );
//		fdExtractType.top = new FormAttachment(wFromField, margin );
//		wExtractByRExp.setLayoutData(fdExtractType);
//		wExtractByRExp.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected( SelectionEvent e ) {
//				input.setChanged();
//			}
//		} );


		//======== Keyword group
		//keyword line
		wlKeyword=new Label(shell, SWT.RIGHT);
		wlKeyword.setText(BaseMessages.getString(PKG,"InfoExtractorDialog.ExtractType.Keyword.Keyword"));
		props.setLook(wlKeyword);
		fdlKeyword=new FormData();
		fdlKeyword.left = new FormAttachment(0, 0);
		fdlKeyword.top  = new FormAttachment(wExtractByRExp, margin);
		fdlKeyword.right= new FormAttachment(middle, -margin);
		wlKeyword.setLayoutData(fdlKeyword);
		wKeyword=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wKeyword);
		wKeyword.addModifyListener(lsMod);
		fdKeyword=new FormData();
		fdKeyword.left = new FormAttachment(middle, 0);
		fdKeyword.top  = new FormAttachment(wExtractByRExp, margin);
		fdKeyword.right= new FormAttachment(100, 0);
		wKeyword.setLayoutData(fdKeyword);

		//Info type line
		wlInfoType = new Label( shell, SWT.RIGHT );
		wlInfoType.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.InfoType.Label" ) );
		props.setLook(wlInfoType);
		fdlInfoType = new FormData();
		fdlInfoType.left = new FormAttachment( 0, 0 );
		fdlInfoType.top = new FormAttachment(wKeyword, margin );
		fdlInfoType.right = new FormAttachment( middle, -margin );
		wlInfoType.setLayoutData(fdlInfoType);

		wInfoTypeText = new Button( shell, SWT.RADIO );
		props.setLook(wInfoTypeText);
		wInfoTypeText.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.InfoType.Text" ) );
		wInfoTypeText.setToolTipText( BaseMessages.getString( PKG, "InfoExtractorDialog.InfoType.Text.Tooltip" ) );
		fdInfoType = new FormData();
		fdInfoType.left = new FormAttachment( middle, 0 );
		fdInfoType.top = new FormAttachment(wKeyword, margin );
		wInfoTypeText.setLayoutData(fdInfoType);
		wInfoTypeText.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				input.setChanged();
			}
		} );

		wInfoTypeNumber = new Button( shell, SWT.RADIO );
		props.setLook(wInfoTypeNumber);
		wInfoTypeNumber.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.InfoType.Number" ) );
		wInfoTypeNumber.setToolTipText( BaseMessages.getString( PKG, "InfoExtractorDialog.InfoType.Number.Tooltip" ) );
		fdInfoType = new FormData();
		fdInfoType.left = new FormAttachment( wInfoTypeText, 0 );
		fdInfoType.top = new FormAttachment(wKeyword, margin );
		wInfoTypeNumber.setLayoutData(fdInfoType);
		wInfoTypeNumber.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				input.setChanged();
			}
		} );

		wInfoTypeDate = new Button( shell, SWT.RADIO );
		props.setLook(wInfoTypeDate);
		wInfoTypeDate.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.InfoType.Date" ) );
		wInfoTypeDate.setToolTipText( BaseMessages.getString( PKG, "InfoExtractorDialog.InfoType.Date.Tooltip" ) );
		fdInfoType = new FormData();
		fdInfoType.left = new FormAttachment( wInfoTypeNumber, 0 );
		fdInfoType.top = new FormAttachment(wKeyword, margin );
		wInfoTypeDate.setLayoutData(fdInfoType);
		wInfoTypeDate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				input.setChanged();
			}
		} );
		//End Keyword line group



		wlResultField =new Label(shell, SWT.RIGHT);
		wlResultField.setText(BaseMessages.getString( PKG, "InfoExtractorDialog.Result.Field" ));
 		props.setLook(wlResultField);
		fdlResultField =new FormData();
		fdlResultField.left = new FormAttachment(0, 0);
		fdlResultField.right= new FormAttachment(middle, -margin);
		fdlResultField.top  = new FormAttachment(wInfoTypeDate, margin);
		wlResultField.setLayoutData(fdlResultField);
		wResultField =new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wResultField);
 		wResultField.addModifyListener(lsMod);
		wResultField.setText(input.getResultField()==null?"result":input.getResultField());
		fdResultField =new FormData();
		fdResultField.left = new FormAttachment(middle, 0);
		fdResultField.top  = new FormAttachment(wInfoTypeDate, margin);
		fdResultField.right= new FormAttachment(100, 0);
		wResultField.setLayoutData(fdResultField);
//
//		wlContentMark=new Label(shell, SWT.RIGHT);
//		wlContentMark.setText("Mark");
// 		props.setLook(wlContentMark);
//		fdlContentMark=new FormData();
//		fdlContentMark.left = new FormAttachment(0, 0);
//		fdlContentMark.right= new FormAttachment(middle, -margin);
//		fdlContentMark.top  = new FormAttachment(wResultField, margin);
//		wlContentMark.setLayoutData(fdlContentMark);
//		wContentMark=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
// 		props.setLook(wContentMark);
// 		wContentMark.addModifyListener(lsMod);
//		fdContentMark=new FormData();
//		fdContentMark.left = new FormAttachment(middle, 0);
//		fdContentMark.top  = new FormAttachment(wResultField, margin);
//		fdContentMark.right= new FormAttachment(100, 0);
//		wContentMark.setLayoutData(fdContentMark);
//
//
//
//		wlFields=new Label(shell, SWT.NONE);
//		wlFields.setText(BaseMessages.getString(PKG,"InfoExtractorDialog.Fields.Label"));
// 		props.setLook(wlFields);
//		fdlFields=new FormData();
//		fdlFields.left = new FormAttachment(0, 0);
//		fdlFields.top  = new FormAttachment(wContentMark, margin);
//		wlFields.setLayoutData(fdlFields);
//
//		final int FieldsCols=8;
//		final int FieldsRows=input.getFieldName().length;
//
//		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
//		colinf[0]=new ColumnInfo(BaseMessages.getString(PKG,"InfoExtractorDialog.FieldName.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,   false);
//		colinf[1]=new ColumnInfo(BaseMessages.getString(PKG,"InfoExtractorDialog.ContentMode.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO,   Rule.getContentModes());
//		colinf[2]=new ColumnInfo(BaseMessages.getString(PKG,"InfoExtractorDialog.KeyWord.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,   false);
//		colinf[3]=new ColumnInfo(BaseMessages.getString(PKG,"InfoExtractorDialog.Before/After.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO, Rule.getPosiitons() );
//		colinf[4]=new ColumnInfo(BaseMessages.getString(PKG,"InfoExtractorDialog.Order.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,false);
//		colinf[5]=new ColumnInfo(BaseMessages.getString(PKG,"InfoExtractorDialog.MinDigitals.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,   false);
//		colinf[6]=new ColumnInfo(BaseMessages.getString(PKG,"InfoExtractorDialog.MaxDigitals.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,   false);
//		colinf[7]=new ColumnInfo(BaseMessages.getString(PKG,"InfoExtractorDialog.DefineDigitals.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,   false);
//
//		wFields=new TableView(transMeta, shell,
//						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
//						      colinf,
//						      FieldsRows,
//						      lsMod,
//							  props
//						      );
//
//		fdFields=new FormData();
//		fdFields.left  = new FormAttachment(0, 0);
//		fdFields.top   = new FormAttachment(wlFields, margin);
//		fdFields.right = new FormAttachment(100, 0);
//		fdFields.bottom= new FormAttachment(100, -50);
//		wFields.setLayoutData(fdFields);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString("System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString("System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, wResultField);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


//		lsResize = new Listener()
//		{
//			public void handleEvent(Event event)
//			{
//				Point size = shell.getSize();
//				wFields.setSize(size.x-10, size.y-50);
//				wFields.table.setSize(size.x-10, size.y-50);
//				wFields.redraw();
//			}
//		};
//		shell.addListener(SWT.Resize, lsResize);
		
		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		//System.out.println("t");
		//log.logDebug(toString(), "getting fields info...");
		this.wResultField.setText(input.getResultField()==null?"":input.getResultField());
//		this.wContentMark.setText(input.getContentMark()==null?"":input.getContentMark());
		
//		for (i=0;i<input.getFieldName().length;i++)
//		{
//			if (input.getFieldName()[i]!=null)
//			{
//				TableItem item = wFields.table.getItem(i);
//				item.setText(1, input.getFieldName()[i]);
//
//				String contentMode = input.getContentMode()[i];
//				String keyword   = input.getKeyWord()[i];
//				String position = input.getPosition()[i];
//                String order = new Integer(input.getOrder()[i]).toString();
//                String minDigitals   = input.getMinLength()[i]<0?"":(""+input.getMinLength()[i]);
//                String maxDigitals   = input.getMaxLength()[i]<0?"":(""+input.getMaxLength()[i]);
//				String definedDigitals   = input.getDefinedDigitals()[i];
//
//				if (contentMode  !=null) item.setText(2, contentMode  ); else item.setText(2, "");
//				if (keyword  !=null) item.setText(3, keyword  ); else item.setText(3, "");
//				if (position!=null) item.setText(4, position); else item.setText(4, "");
//				if (order!=null) item.setText(5, order); else item.setText(5, "");
//				if (minDigitals  !=null) item.setText(6, minDigitals  ); else item.setText(6, "");
//				if (maxDigitals  !=null) item.setText(7, maxDigitals  ); else item.setText(7, "");
//				if (definedDigitals  !=null) item.setText(8, definedDigitals  ); else item.setText(8, "");
//			}
//		}
//
//        wFields.setRowNums();
//        wFields.optWidth(true);
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value
        input.setResultField(this.wResultField.getText());
       // input.setContentMark(this.wContentMark.getText());
        
		int i;
		//Table table = wFields.table;
		
//		int nrfields = wFields.nrNonEmpty();
//
//		input.allocate(nrfields);
//
//		for (i=0;i<nrfields;i++)
//		{
//			TableItem item = wFields.getNonEmpty(i);
//			input.getFieldName()[i]   = item.getText(1);
//			input.getContentMode()[i] = item.getText(2);
//			input.getKeyWord()[i]   = item.getText(4);
//			input.getPosition()[i] = item.getText(5);
//			String order = item.getText(6);
//			String minLength   = item.getText(7);
//			String maxLength   = item.getText(8);
//			input.getDefinedDigitals()[i] = item.getText(9);
//
//			try { input.getOrder()[i]    = Integer.parseInt(order); }
//			  catch(Exception e) { input.getOrder()[i]    = -1; }
//			try { input.getMinLength()[i] = Integer.parseInt(minLength  ); }
//			  catch(Exception e) { input.getMinLength()[i] = -1; }
//			try { input.getMaxLength()[i] = Integer.parseInt(maxLength  ); }
//			  catch(Exception e) { input.getMaxLength()[i] = -1; }
//
//		}
		
		dispose();
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
	private void setDynamicFilenameField() {
		if ( !gotPreviousFields ) {
			try {
				String field = wFromField.getText();
				wFromField.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields( stepname );
				if ( r != null ) {
					wFromField.setItems( r.getFieldNames() );
				}
				if ( field != null ) {
					wFromField.setText( field );
				}
			} catch ( KettleException ke ) {
				new ErrorDialog(
						shell, BaseMessages.getString( PKG, "InfoExtractorDialog.FailedToGetFields.DialogTitle" ),
						BaseMessages.getString( PKG, "InfoExtractorDialog.FailedToGetFields.DialogMessage" ), ke );
			}
			gotPreviousFields = true;
		}
	}
}
