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
import org.eclipse.swt.widgets.*;
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

	Group group0, group1,group2;

	private Label wlContentField;
	private CCombo wContentField;
	private FormData fdlContentField, fdContentField;

	private Label wlExtractType;
	private Button wExtractByKeyword, wExtractByRExp;
	private FormData fdlExtractType, fdExtractType;

	private Label wlPreProcessType;
	private Button wPreProcessRemoveHtml, wPreProcessRemoveBlank,wPreProcessRemoveControlChar, wPreProcessRemoveCRLF;
	private FormData fdlPreProcessType, fdPreProcessType;

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

		wlContentField = new Label(shell, SWT.RIGHT );
		wlContentField.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.FromField.Label" ) );
		props.setLook(wlContentField);
		fdlContentField = new FormData();
		fdlContentField.left = new FormAttachment( 0, 0 );
		fdlContentField.top = new FormAttachment( wStepname, margin );
		fdlContentField.right = new FormAttachment( middle, -margin );
		wlContentField.setLayoutData(fdlContentField);
		wContentField = new CCombo(shell, SWT.BORDER | SWT.READ_ONLY );
		wContentField.setEditable( true );
		props.setLook(wContentField);
		wContentField.addModifyListener( lsMod );
		fdContentField = new FormData();
		fdContentField.left = new FormAttachment( middle, 0 );
		fdContentField.top = new FormAttachment( wStepname, margin );
		fdContentField.right = new FormAttachment( 100, 0 );
		wContentField.setLayoutData(fdContentField);
		wContentField.addFocusListener(new FocusListener() {
			public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
			}
			public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
				setDynamicFilenameField();
			}
		} );


		group0 = new Group(shell, SWT.NONE);
		group0.setText(BaseMessages.getString( PKG, "InfoExtractorDialog.Group.PreProcess" ));
		props.setLook(group0);
		FormLayout group0Layout = new FormLayout();
		group0Layout.marginWidth = 10;
		group0Layout.marginHeight = 10;
		group0.setLayout( group0Layout );

		FormData fd0 = new FormData();
		fd0.left = new FormAttachment( 0, margin );
		fd0.top = new FormAttachment(wContentField, margin );
		fd0.right = new FormAttachment( 100, -margin );
		group0.setLayoutData(fd0);

		//Data PreProcess Type
		wlPreProcessType = new Label( group0, SWT.RIGHT );
		wlPreProcessType.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.ProcessType.Label" ) );
		props.setLook(wlPreProcessType);
		fdlPreProcessType = new FormData();
		fdlPreProcessType.left = new FormAttachment( 0, 0 );
		fdlPreProcessType.top = new FormAttachment(wContentField, margin );
		fdlPreProcessType.right = new FormAttachment( middle, -margin );
		wlPreProcessType.setLayoutData(fdlPreProcessType);

		wPreProcessRemoveHtml = new Button( group0, SWT.CHECK );
		props.setLook(wPreProcessRemoveHtml);
		wPreProcessRemoveHtml.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.ProcessType.removeHtml" )  );
		wPreProcessRemoveHtml.setToolTipText( BaseMessages.getString( PKG, "InfoExtractorDialog.ProcessType.removeHtml.Tooltip" ) );
		fdPreProcessType = new FormData();
		fdPreProcessType.left = new FormAttachment( middle, 0 );
		fdPreProcessType.top = new FormAttachment(wContentField, margin );
		wPreProcessRemoveHtml.setLayoutData(fdPreProcessType);
		wPreProcessRemoveHtml.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				input.setChanged();
			}
		} );

		wPreProcessRemoveBlank = new Button( group0, SWT.CHECK );
		props.setLook(wPreProcessRemoveBlank);
		wPreProcessRemoveBlank.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.ProcessType.removeBlank" ) );
		wPreProcessRemoveBlank.setToolTipText( BaseMessages.getString( PKG, "InfoExtractorDialog.ProcessType.removeBlank.Tooltip" ) );
		fdPreProcessType = new FormData();
		fdPreProcessType.left = new FormAttachment(wPreProcessRemoveHtml, 0 );
		fdPreProcessType.top = new FormAttachment(wContentField, margin );
		wPreProcessRemoveBlank.setLayoutData(fdPreProcessType);
		wPreProcessRemoveBlank.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				input.setChanged();
				}
		} );


		wPreProcessRemoveControlChar = new Button( group0, SWT.CHECK );
		props.setLook(wPreProcessRemoveControlChar);
		wPreProcessRemoveControlChar.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.ProcessType.removeContralChar" ) );
		wPreProcessRemoveControlChar.setToolTipText( BaseMessages.getString( PKG, "InfoExtractorDialog.ProcessType.removeContralChar.Tooltip" ) );
		fdPreProcessType = new FormData();
		fdPreProcessType.left = new FormAttachment(wPreProcessRemoveBlank, 0 );
		fdPreProcessType.top = new FormAttachment(wContentField, margin );
		wPreProcessRemoveControlChar.setLayoutData(fdPreProcessType);
		wPreProcessRemoveControlChar.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				input.setChanged();
			}
		} );

		wPreProcessRemoveCRLF = new Button( group0, SWT.CHECK );
		props.setLook(wPreProcessRemoveCRLF);
		wPreProcessRemoveCRLF.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.ProcessType.removeCRLFChar" ) );
		wPreProcessRemoveCRLF.setToolTipText( BaseMessages.getString( PKG, "InfoExtractorDialog.ProcessType.removeCRLFChar.Tooltip" ) );
		fdPreProcessType = new FormData();
		fdPreProcessType.left = new FormAttachment(wPreProcessRemoveControlChar, 0 );
		fdPreProcessType.top = new FormAttachment(wContentField, margin );
		wPreProcessRemoveCRLF.setLayoutData(fdPreProcessType);
		wPreProcessRemoveCRLF.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				input.setChanged();
			}
		} );


		group1 = new Group(shell, SWT.NONE);
		group1.setText("信息提取方式");
		props.setLook(group1);
		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;
		group1.setLayout( groupLayout );

		FormData fd = new FormData();
		fd.left = new FormAttachment( 0, margin );
		fd.top = new FormAttachment(group0, margin );
		fd.right = new FormAttachment( 100, -margin );
		group1.setLayoutData(fd);

		//Extract Type
		wlExtractType = new Label( group1, SWT.RIGHT );
		wlExtractType.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.ExtractType.Label" ) );
		props.setLook(wlExtractType);
		fdlExtractType = new FormData();
		fdlExtractType.left = new FormAttachment( 0, 0 );
		fdlExtractType.top = new FormAttachment(group0, margin );
		fdlExtractType.right = new FormAttachment( middle, -margin );
		wlExtractType.setLayoutData(fdlExtractType);

		wExtractByKeyword = new Button( group1, SWT.RADIO );
		props.setLook(wExtractByKeyword);
		wExtractByKeyword.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.ExtractType.Keyword" )  );
		wExtractByKeyword.setToolTipText( BaseMessages.getString( PKG, "InfoExtractorDialog.ExtractType.Keyword.Tooltip" ) );
		fdExtractType = new FormData();
		fdExtractType.left = new FormAttachment( middle, 0 );
		fdExtractType.top = new FormAttachment(group0, margin );
		wExtractByKeyword.setLayoutData(fdExtractType);
		wExtractByKeyword.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				input.setChanged();
				group2.setEnabled(true);
				wInfoTypeText.setEnabled(wExtractByKeyword.getSelection());
				wInfoTypeNumber.setEnabled(wExtractByKeyword.getSelection());
				wInfoTypeDate.setEnabled(wExtractByKeyword.getSelection());
				wlInfoType.setEnabled(wExtractByKeyword.getSelection());
				wlKeyword.setText(wExtractByKeyword.getSelection()?BaseMessages.getString(PKG,"InfoExtractorDialog.ExtractType.Keyword.Keyword"):BaseMessages.getString(PKG,"InfoExtractorDialog.ExtractType.RegExp.RegExp"));
				if(!wInfoTypeText.getSelection() && !wInfoTypeNumber.getSelection() && !wInfoTypeDate.getSelection())
					wInfoTypeText.setSelection(true);
			}
		} );

		wExtractByRExp = new Button( group1, SWT.RADIO );
		props.setLook(wExtractByRExp);
		wExtractByRExp.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.ExtractType.RegExp" ) );
		wExtractByRExp.setToolTipText( BaseMessages.getString( PKG, "InfoExtractorDialog.ExtractType.RegExp.Tooltip" ) );
		fdExtractType = new FormData();
		fdExtractType.left = new FormAttachment(wExtractByKeyword, 0 );
		fdExtractType.top = new FormAttachment(wContentField, margin );
		wExtractByRExp.setLayoutData(fdExtractType);
		wExtractByRExp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				input.setChanged();
				wInfoTypeText.setEnabled(wExtractByKeyword.getSelection());
				wInfoTypeNumber.setEnabled(wExtractByKeyword.getSelection());
				wInfoTypeDate.setEnabled(wExtractByKeyword.getSelection());
				wlInfoType.setEnabled(wExtractByKeyword.getSelection());
				wlKeyword.setText(wExtractByRExp.getSelection()?BaseMessages.getString(PKG,"InfoExtractorDialog.ExtractType.RegExp.RegExp"):BaseMessages.getString(PKG,"InfoExtractorDialog.ExtractType.Keyword.Keyword"));
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
		//fdlKeyword.top  = new FormAttachment(wExtractByRExp, margin);
		fdlKeyword.top  = new FormAttachment(group1, margin);
		fdlKeyword.right= new FormAttachment(middle, -margin);
		wlKeyword.setLayoutData(fdlKeyword);
		wKeyword=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wKeyword);
		wKeyword.addModifyListener(lsMod);
		fdKeyword=new FormData();
		fdKeyword.left = new FormAttachment(middle, 0);
		//fdKeyword.top  = new FormAttachment(wExtractByRExp, margin);
		fdKeyword.top  = new FormAttachment(group1, margin);
		fdKeyword.right= new FormAttachment(100, 0);
		wKeyword.setLayoutData(fdKeyword);

		group2 = new Group(shell, SWT.NONE);
		group2.setText("信息类型");
		props.setLook(group2);
		FormLayout groupLayout2 = new FormLayout();
		groupLayout2.marginWidth = 10;
		groupLayout2.marginHeight = 10;
		group2.setLayout( groupLayout2 );

		FormData fd2 = new FormData();
		fd2.left = new FormAttachment( 0, margin );
		fd2.top = new FormAttachment(wKeyword, margin );
		fd2.right = new FormAttachment( 100, -margin );
		group2.setLayoutData(fd2);

		//Info type line
		wlInfoType = new Label( group2, SWT.RIGHT );
		wlInfoType.setText( BaseMessages.getString( PKG, "InfoExtractorDialog.InfoType.Label" ) );
		props.setLook(wlInfoType);
		fdlInfoType = new FormData();
		fdlInfoType.left = new FormAttachment( 0, 0 );
		fdlInfoType.top = new FormAttachment(wKeyword, margin );
		fdlInfoType.right = new FormAttachment( middle, -margin );
		wlInfoType.setLayoutData(fdlInfoType);

		wInfoTypeText = new Button( group2, SWT.RADIO );
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

		wInfoTypeNumber = new Button( group2, SWT.RADIO );
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

		wInfoTypeDate = new Button( group2, SWT.RADIO );
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
		fdlResultField.top  = new FormAttachment(group2, margin);
		wlResultField.setLayoutData(fdlResultField);
		wResultField =new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wResultField);
 		wResultField.addModifyListener(lsMod);
		wResultField.setText(input.getResultField()==null?"result":input.getResultField());
		fdResultField =new FormData();
		fdResultField.left = new FormAttachment(middle, 0);
		fdResultField.top  = new FormAttachment(group2, margin);
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
		if(input.getExtractType()==InfoExtractorMeta.EXTRACT_TYPE_KEYWORD) {
			wKeyword.setText(input.getKeyWord());
			wExtractByKeyword.setSelection(true);
			if (input.getInfoType() == InfoExtractorMeta.INFO_TYPE_TEXT)
				this.wInfoTypeText.setSelection(true);
			else if (input.getInfoType() == InfoExtractorMeta.INFO_TYPE_NUMBER)
				this.wInfoTypeNumber.setSelection(true);
			else if (input.getInfoType() == InfoExtractorMeta.INFO_TYPE_DATE)
				this.wInfoTypeDate.setSelection(true);
		}else if(input.getExtractType()==InfoExtractorMeta.EXTRACT_TYPE_REGEXP) {
				wKeyword.setText(input.getRegularExpression());
				wExtractByRExp.setSelection(true);
				wInfoTypeText.setEnabled(false);
				wInfoTypeNumber.setEnabled(false);
				wInfoTypeDate.setEnabled(false);
				wlInfoType.setEnabled(false);
				wlKeyword.setText(BaseMessages.getString(PKG,"InfoExtractorDialog.ExtractType.RegExp.RegExp"));
				group2.setEnabled(false);
		}else {
			wKeyword.setText("");
			wExtractByKeyword.setSelection(true);
			wInfoTypeText.setSelection(true);
		}
		this.wContentField.setText(input.getContentField()==null?"":input.getContentField());
		this.wPreProcessRemoveControlChar.setSelection(input.isRemoveControlChars());
		this.wPreProcessRemoveBlank.setSelection(input.isRemoveBlank());
		this.wPreProcessRemoveCRLF.setSelection(input.isRemoveCRLF());
		this.wPreProcessRemoveHtml.setSelection(input.isRemoveHtml());
		this.wResultField.setText(input.getResultField()==null?"result":input.getResultField());

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
		input.setContentField(wContentField.getText());
		input.setRemoveControlChars(wPreProcessRemoveControlChar.getSelection());
		input.setRemoveHtml(wPreProcessRemoveHtml.getSelection());
		input.setRemoveCRLF(wPreProcessRemoveCRLF.getSelection());
		input.setRemoveBlank(wPreProcessRemoveBlank.getSelection());
		if(wExtractByKeyword.getSelection()) {
			input.setExtractType(InfoExtractorMeta.EXTRACT_TYPE_KEYWORD);
			input.setKeyWord(wKeyword.getText());
			if(wInfoTypeText.getSelection())
				input.setInfoType(InfoExtractorMeta.INFO_TYPE_TEXT);
			else if(wInfoTypeNumber.getSelection())
				input.setInfoType(InfoExtractorMeta.INFO_TYPE_NUMBER);
			else if(wInfoTypeDate.getSelection())
				input.setInfoType(InfoExtractorMeta.INFO_TYPE_DATE);
		}
		else {
			input.setExtractType(InfoExtractorMeta.EXTRACT_TYPE_REGEXP);
			input.setRegularExpression(wKeyword.getText());
		}
       // input.setContentMark(this.wContentMark.getText());
		input.setResultField(this.wResultField.getText());
		dispose();
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
	private void setDynamicFilenameField() {
		if ( !gotPreviousFields ) {
			try {
				String field = wContentField.getText();
				wContentField.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields( stepname );
				if ( r != null ) {
					wContentField.setItems( r.getFieldNames() );
				}
				if ( field != null ) {
					wContentField.setText( field );
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
