/*******************************************************************************
 *
 * Auphi Data Integration Platform(Kettle Platform)
 * Copyright (C) 2011-2017 by Auphi BI : http://www.doetl.com
 * Support：support@pentahochina.com
 *
 *******************************************************************************
 *
 * Licensed under the LGPL License, Version 3.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    https://opensource.org/licenses/LGPL-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.aofei.kettleplugin.DB2Load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DB2DatabaseMeta;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;


/**
*/
public class DB2LoadDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = DB2LoadMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CCombo       wConnection;
	
	//Database alias
	private Label				wlDB2DatabaseName;
	private TextVar				wDB2DatabaseName;
	private FormData			fdlDB2DatabaseName, fdDB2DatabaseName;	
	
    private Label        		wlSchema;
    private TextVar      		wSchema;
    private FormData     		fdlSchema, fdSchema;
    private FormData			fdbSchema;
    private Button				wbSchema;

	
	private Label        		wlTable;
	private Button       		wbTable;
	private TextVar      		wTable;
	private FormData     		fdlTable, fdbTable, fdTable;

	private Label				wlSqlldr;
	private Button				wbSqlldr;
	private TextVar				wSqlldr;
	private FormData			fdlSqlldr, fdbSqlldr, fdSqlldr;
	
	private Label               wlLoadAction;
	private CCombo              wLoadAction;
	private FormData            fdlLoadAction, fdLoadAction;


	private Label               wlTruncateAction;
	private CCombo              wTruncateAction;
	private FormData            fdlTruncateAction, fdTruncateAction;


	//Add BY Tony 2018
	private Label        		wlTruncateSQL;
	private StyledTextComp 		wTruncateSQL;
	private FormData     		fdlTruncateSQL, fdTruncateSQL;



	private Label				wlReturn;
	private TableView			wReturn;
	private FormData			fdlReturn, fdReturn;

	private Label				wlDataFile;
	private Button				wbDataFile;
	private TextVar				wDataFile;
	private FormData			fdlDataFile, fdbDataFile, fdDataFile;	

	private Label				wlLogFile;
	private Button				wbLogFile;
	private TextVar				wLogFile;
	private FormData			fdlLogFile, fdbLogFile, fdLogFile;	

	private Label				wlBadFile;
	private Button				wbBadFile;
	private TextVar				wBadFile;
	private FormData			fdlBadFile, fdbBadFile, fdBadFile;		

	private Label				wlDiscardFile;
	private Button				wbDiscardFile;
	private TextVar				wDiscardFile;
	private FormData			fdlDiscardFile, fdbDiscardFile, fdDiscardFile;		

   private Label               wlEncoding;
   private TextVar               wEncoding;
   private FormData            fdlEncoding, fdEncoding;
   
	private Label         wlBeforeSQL;
	private TextVar       wBeforeSQL;
	private FormData      fdlBeforeSQL, fdBeforeSQL;
	
	private Label         wlAfterSQL;
	private TextVar       wAfterSQL;
	private FormData      fdlAfterSQL, fdAfterSQL;
	
	private Button				wGetLU;
	private FormData			fdGetLU;
	private Listener			lsGetLU;
	
	private DB2LoadMeta	input;
	
   private Map<String, Integer> inputFields;
   
	private ColumnInfo[] ciReturn ;
	
	/**
	 * List of ColumnInfo that should have the field names of the selected database table
	 */
	private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();
	

   private static final String[] ALL_FILETYPES = new String[] {
       	BaseMessages.getString(PKG, "DB2LoadDialog.Filetype.All") };


	public DB2LoadDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input = (DB2LoadMeta) in;
       inputFields =new HashMap<String, Integer>();
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
		SelectionListener lsSelection = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e) 
			{
				input.setChanged();
				setTableFieldCombo();
			}
		};
		ModifyListener lsTableMod = new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				input.setChanged();
				setTableFieldCombo();
			}
		};
		changed = input.hasChanged();
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		shell.setLayout(new FillLayout());
		shell.setText(BaseMessages.getString(PKG, "DB2LoadDialog.Shell.Title")); //$NON-NLS-1$

	    ScrolledComposite sComp = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.H_SCROLL );
	    sComp.setLayout(new FillLayout());
       
       Composite comp = new Composite(sComp, SWT.NONE );
       props.setLook(comp);

       FormLayout fileLayout = new FormLayout();
       fileLayout.marginWidth  = 3;
       fileLayout.marginHeight = 3;
       comp.setLayout(fileLayout);


		// Stepname line
		wlStepname = new Label(comp, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "DB2LoadDialog.Stepname.Label")); //$NON-NLS-1$
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		
		wStepname = new Text(comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// Connection line
		wConnection = addConnectionLine(comp, wStepname, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1)  {
			wConnection.select(0);
		}
		wConnection.addModifyListener(lsMod);
		wConnection.addModifyListener(new ModifyListener() { 
			public void modifyText(ModifyEvent event) { 
				//setFlags(); 
				}});
		wConnection.addSelectionListener(lsSelection);
		
		// Schema line...
        wlSchema=new Label(comp, SWT.RIGHT);
        wlSchema.setText(BaseMessages.getString(PKG, "DB2LoadDialog.TargetSchema.Label")); //$NON-NLS-1$
        props.setLook(wlSchema);
        fdlSchema=new FormData();
        fdlSchema.left = new FormAttachment(0, 0);
        fdlSchema.right= new FormAttachment(middle, -margin);
        fdlSchema.top  = new FormAttachment(wConnection, margin*2);
        wlSchema.setLayoutData(fdlSchema);

		wbSchema=new Button(comp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbSchema);
 		wbSchema.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
 		fdbSchema=new FormData();
 		fdbSchema.top  = new FormAttachment(wConnection, 2*margin);
 		fdbSchema.right= new FormAttachment(100, 0);
		wbSchema.setLayoutData(fdbSchema);
        
        wSchema=new TextVar(transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSchema);
        wSchema.addModifyListener(lsTableMod);
        fdSchema=new FormData();
        fdSchema.left = new FormAttachment(middle, 0);
        fdSchema.top  = new FormAttachment(wConnection, margin*2);
        fdSchema.right= new FormAttachment(wbSchema, -margin);
        wSchema.setLayoutData(fdSchema);

		// Table line...
		wlTable=new Label(comp, SWT.RIGHT);
		wlTable.setText(BaseMessages.getString(PKG, "DB2LoadDialog.TargetTable.Label"));
 		props.setLook(wlTable);
		fdlTable=new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right= new FormAttachment(middle, -margin);
		fdlTable.top  = new FormAttachment(wbSchema, margin);
		wlTable.setLayoutData(fdlTable);

		wbTable=new Button(comp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbTable=new FormData();
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wbSchema, margin);
		wbTable.setLayoutData(fdbTable);

		wTable=new TextVar(transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsTableMod);
		fdTable=new FormData();
		fdTable.top  = new FormAttachment(wbSchema, margin);
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.right= new FormAttachment(wbTable, -margin);
		wTable.setLayoutData(fdTable);
		
       // Database alias line...
	   wlDB2DatabaseName=new Label(comp, SWT.RIGHT);
	   wlDB2DatabaseName.setText(BaseMessages.getString(PKG, "DB2LoadDialog.TargetDatabase.Label")); //$NON-NLS-1$
       props.setLook(wlDB2DatabaseName);
       fdlDB2DatabaseName=new FormData();
       fdlDB2DatabaseName.left = new FormAttachment(0, 0);
       fdlDB2DatabaseName.right= new FormAttachment(middle, -margin);
       fdlDB2DatabaseName.top  = new FormAttachment(wTable, margin*2);
       wlDB2DatabaseName.setLayoutData(fdlDB2DatabaseName);
       
       wDB2DatabaseName = new TextVar(transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
       props.setLook(wDB2DatabaseName);
       wDB2DatabaseName.addModifyListener(lsMod);
	   fdDB2DatabaseName = new FormData();
	   fdDB2DatabaseName.left = new FormAttachment(middle, 0);
	   fdDB2DatabaseName.top = new FormAttachment(wTable, margin);
	   fdDB2DatabaseName.right = new FormAttachment(100, 0);
	   wDB2DatabaseName.setLayoutData(fdDB2DatabaseName);

	   
		// Sqlldr line...
		wlSqlldr = new Label(comp, SWT.RIGHT);
		wlSqlldr.setText(BaseMessages.getString(PKG, "DB2LoadDialog.Sqlldr.Label")); //$NON-NLS-1$
		props.setLook(wlSqlldr);
		fdlSqlldr = new FormData();
		fdlSqlldr.left = new FormAttachment(0, 0);
		fdlSqlldr.right = new FormAttachment(middle, -margin);
		fdlSqlldr.top = new FormAttachment(wDB2DatabaseName, margin);
		wlSqlldr.setLayoutData(fdlSqlldr);
		
		wbSqlldr = new Button(comp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbSqlldr);
		wbSqlldr.setText(BaseMessages.getString(PKG, "DB2LoadDialog.Browse.Button")); //$NON-NLS-1$
		fdbSqlldr = new FormData();
		fdbSqlldr.right = new FormAttachment(100, 0);
		fdbSqlldr.top = new FormAttachment(wDB2DatabaseName, margin);
		wbSqlldr.setLayoutData(fdbSqlldr);
		wSqlldr = new TextVar(transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSqlldr);
		wSqlldr.addModifyListener(lsMod);
		fdSqlldr = new FormData();
		fdSqlldr.left = new FormAttachment(middle, 0);
		fdSqlldr.top = new FormAttachment(wDB2DatabaseName, margin);
		fdSqlldr.right = new FormAttachment(wbSqlldr, -margin);
		wSqlldr.setLayoutData(fdSqlldr);
				
		// Load Action line
		wlLoadAction = new Label(comp, SWT.RIGHT);
		wlLoadAction.setText(BaseMessages.getString(PKG, "DB2LoadDialog.LoadAction.Label"));
		props.setLook(wlLoadAction);
		fdlLoadAction = new FormData();
		fdlLoadAction.left = new FormAttachment(0, 0);
		fdlLoadAction.right = new FormAttachment(middle, -margin);
		fdlLoadAction.top = new FormAttachment(wSqlldr, margin);
		wlLoadAction.setLayoutData(fdlLoadAction);
		wLoadAction = new CCombo(comp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wLoadAction.add(BaseMessages.getString(PKG, "DB2LoadDialog.AppendLoadAction.Label"));
		wLoadAction.add(BaseMessages.getString(PKG, "DB2LoadDialog.ReplaceLoadAction.Label"));
		wLoadAction.add(BaseMessages.getString(PKG, "DB2LoadDialog.TruncateLoadAction.Label"));
		
		wLoadAction.select(0); // +1: starts at -1
		SelectionAdapter lsSelMod = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				input.setChanged();
				setFlagsTruncateOption();
			}
		};
		wLoadAction.addSelectionListener(lsSelMod);
		
		props.setLook(wLoadAction);
		fdLoadAction= new FormData();
		fdLoadAction.left = new FormAttachment(middle, 0);
		fdLoadAction.top = new FormAttachment(wSqlldr, margin);
		fdLoadAction.right = new FormAttachment(100, 0);
		wLoadAction.setLayoutData(fdLoadAction);
		
		fdLoadAction = new FormData();
		fdLoadAction.left = new FormAttachment(middle, 0);
		fdLoadAction.top = new FormAttachment(wSqlldr, margin);
		fdLoadAction.right = new FormAttachment(100, 0);
		wLoadAction.setLayoutData(fdLoadAction);


		// Truncate Action line
		wlTruncateAction = new Label(comp, SWT.RIGHT);
		wlTruncateAction.setText(BaseMessages.getString(PKG, "DB2LoadDialog.TruncateAction.Label"));
		props.setLook(wlTruncateAction);
		fdlTruncateAction = new FormData();
		fdlTruncateAction.left = new FormAttachment(0, 0);
		fdlTruncateAction.right = new FormAttachment(middle, -margin);
		fdlTruncateAction.top = new FormAttachment(wLoadAction, margin);
		wlTruncateAction.setLayoutData(fdlTruncateAction);
		wTruncateAction = new CCombo(comp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);


		wTruncateAction.add(DB2LoadMeta.TRUNCATE_ACTION_DEFAULT);
		wTruncateAction.add(DB2LoadMeta.TRUNCATE_ACTION_TRUNCATE);
		wTruncateAction.add(DB2LoadMeta.TRUNCATE_ACTION_DELETE);
		wTruncateAction.add(DB2LoadMeta.TRUNCATE_ACTION_CUSTOM);

		wTruncateAction.select(0); // +1: starts at -1
		SelectionAdapter taSelMod = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				input.setChanged();
				setFlagsTruncateSQLOption();
			}
		};
		wTruncateAction.addSelectionListener(taSelMod);

		props.setLook(wTruncateAction);
		fdTruncateAction= new FormData();
		fdTruncateAction.left = new FormAttachment(middle, 0);
		fdTruncateAction.top = new FormAttachment(wLoadAction, margin);
		fdTruncateAction.right = new FormAttachment(100, 0);
		wTruncateAction.setLayoutData(fdTruncateAction);

		fdTruncateAction = new FormData();
		fdTruncateAction.left = new FormAttachment(middle, 0);
		fdTruncateAction.top = new FormAttachment(wLoadAction, margin);
		fdTruncateAction.right = new FormAttachment(100, 0);
		wTruncateAction.setLayoutData(fdTruncateAction);


		// Truncate  SQL ...
		wlTruncateSQL=new Label(comp, SWT.RIGHT);
		wlTruncateSQL.setText(BaseMessages.getString(PKG, "DB2LoadDialog.TruncateSQL.Label"));
		props.setLook(wlTruncateSQL);
		fdlTruncateSQL=new FormData();
		fdlTruncateSQL.left = new FormAttachment(0, 0);
		fdlTruncateSQL.right= new FormAttachment(middle, -margin);
		fdlTruncateSQL.top  = new FormAttachment(wTruncateAction, margin);
		wlTruncateSQL.setLayoutData(fdlTruncateSQL);
		wTruncateSQL=new StyledTextComp(transMeta, comp, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
		props.setLook(wTruncateSQL);
		wTruncateSQL.addModifyListener(lsMod);
		fdTruncateSQL=new FormData();
		fdTruncateSQL.left = new FormAttachment(middle, 0);
		fdTruncateSQL.top  = new FormAttachment(wTruncateAction, margin);
		fdTruncateSQL.right= new FormAttachment(100, 0);
		wTruncateSQL.setLayoutData(fdTruncateSQL);





		// Data file line
		wlDataFile = new Label(comp, SWT.RIGHT);
		wlDataFile.setText(BaseMessages.getString(PKG, "DB2LoadDialog.DataFile.Label")); //$NON-NLS-1$
		props.setLook(wlDataFile);
		fdlDataFile = new FormData();
		fdlDataFile.left = new FormAttachment(0, 0);
		fdlDataFile.top = new FormAttachment(wTruncateSQL, margin);
		fdlDataFile.right = new FormAttachment(middle, -margin);
		wlDataFile.setLayoutData(fdlDataFile);
		wbDataFile = new Button(comp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbDataFile);
		wbDataFile.setText(BaseMessages.getString(PKG, "DB2LoadDialog.Browse.Button")); //$NON-NLS-1$
		fdbDataFile = new FormData();
		fdbDataFile.right = new FormAttachment(100, 0);
		fdbDataFile.top = new FormAttachment(wTruncateSQL, margin);
		wbDataFile.setLayoutData(fdbDataFile);	
		wDataFile = new TextVar(transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wDataFile);
		wDataFile.addModifyListener(lsMod);
		fdDataFile = new FormData();
		fdDataFile.left = new FormAttachment(middle, 0);
		fdDataFile.top = new FormAttachment(wTruncateSQL, margin);
		fdDataFile.right = new FormAttachment(wbDataFile, -margin);
		wDataFile.setLayoutData(fdDataFile);
		
		// Log file line
		wlLogFile = new Label(comp, SWT.RIGHT);
		wlLogFile.setText(BaseMessages.getString(PKG, "DB2LoadDialog.LogFile.Label")); //$NON-NLS-1$
		props.setLook(wlLogFile);
		fdlLogFile = new FormData();
		fdlLogFile.left = new FormAttachment(0, 0);
		fdlLogFile.top = new FormAttachment(wDataFile, margin);
		fdlLogFile.right = new FormAttachment(middle, -margin);
		wlLogFile.setLayoutData(fdlLogFile);
		wbLogFile = new Button(comp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbLogFile);
		wbLogFile.setText(BaseMessages.getString(PKG, "DB2LoadDialog.Browse.Button")); //$NON-NLS-1$
		fdbLogFile = new FormData();
		fdbLogFile.right = new FormAttachment(100, 0);
		fdbLogFile.top = new FormAttachment(wDataFile, margin);
		wbLogFile.setLayoutData(fdbLogFile);
		wLogFile = new TextVar(transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wLogFile);
		wLogFile.addModifyListener(lsMod);
		fdLogFile = new FormData();
		fdLogFile.left = new FormAttachment(middle, 0);
		fdLogFile.top = new FormAttachment(wDataFile, margin);
		fdLogFile.right = new FormAttachment(wbLogFile, -margin);
		wLogFile.setLayoutData(fdLogFile);		

		// Bad file line
		wlBadFile = new Label(comp, SWT.RIGHT);
		wlBadFile.setText(BaseMessages.getString(PKG, "DB2LoadDialog.BadFile.Label")); //$NON-NLS-1$
		props.setLook(wlBadFile);
		fdlBadFile = new FormData();
		fdlBadFile.left = new FormAttachment(0, 0);
		fdlBadFile.top = new FormAttachment(wLogFile, margin);
		fdlBadFile.right = new FormAttachment(middle, -margin);
		wlBadFile.setLayoutData(fdlBadFile);
		wbBadFile = new Button(comp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbBadFile);
		wbBadFile.setText(BaseMessages.getString(PKG, "DB2LoadDialog.Browse.Button")); //$NON-NLS-1$
		fdbBadFile = new FormData();
		fdbBadFile.right = new FormAttachment(100, 0);
		fdbBadFile.top = new FormAttachment(wLogFile, margin);
		wbBadFile.setLayoutData(fdbBadFile);		
		wBadFile = new TextVar(transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wBadFile);
		wBadFile.addModifyListener(lsMod);
		fdBadFile = new FormData();
		fdBadFile.left = new FormAttachment(middle, 0);
		fdBadFile.top = new FormAttachment(wLogFile, margin);
		fdBadFile.right = new FormAttachment(wbBadFile, -margin);
		wBadFile.setLayoutData(fdBadFile);		
		
		// Discard file line
		wlDiscardFile = new Label(comp, SWT.RIGHT);
		wlDiscardFile.setText(BaseMessages.getString(PKG, "DB2LoadDialog.DiscardFile.Label")); //$NON-NLS-1$
		props.setLook(wlDiscardFile);
		fdlDiscardFile = new FormData();
		fdlDiscardFile.left = new FormAttachment(0, 0);
		fdlDiscardFile.top = new FormAttachment(wBadFile, margin);
		fdlDiscardFile.right = new FormAttachment(middle, -margin);
		wlDiscardFile.setLayoutData(fdlDiscardFile);
		wbDiscardFile = new Button(comp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbDiscardFile);
		wbDiscardFile.setText(BaseMessages.getString(PKG, "DB2LoadDialog.Browse.Button")); //$NON-NLS-1$
		fdbDiscardFile = new FormData();
		fdbDiscardFile.right = new FormAttachment(100, 0);
		fdbDiscardFile.top = new FormAttachment(wBadFile, margin);
		wbDiscardFile.setLayoutData(fdbDiscardFile);
		wDiscardFile = new TextVar(transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wDiscardFile);
		wDiscardFile.addModifyListener(lsMod);
		fdDiscardFile = new FormData();
		fdDiscardFile.left = new FormAttachment(middle, 0);
		fdDiscardFile.top = new FormAttachment(wBadFile, margin);
		fdDiscardFile.right = new FormAttachment(wbDiscardFile, -margin);
		wDiscardFile.setLayoutData(fdDiscardFile);			
		
		//codePage
       wlEncoding=new Label(comp, SWT.RIGHT);
       wlEncoding.setText(BaseMessages.getString(PKG, "DB2LoadDialog.Encoding.Label"));
       props.setLook(wlEncoding);
       fdlEncoding=new FormData();
       fdlEncoding.left  = new FormAttachment(0, 0);
       fdlEncoding.top   = new FormAttachment(wDiscardFile, margin);
       fdlEncoding.right = new FormAttachment(middle, -margin);
       wlEncoding.setLayoutData(fdlEncoding);
       wEncoding=new TextVar(transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
       wEncoding.addModifyListener(lsMod);
       props.setLook(wEncoding);
       fdEncoding=new FormData();
       fdEncoding.left = new FormAttachment(middle, 0);
       fdEncoding.top  = new FormAttachment(wDiscardFile, margin);
       fdEncoding.right= new FormAttachment(100, 0);        
       wEncoding.setLayoutData(fdEncoding);
              
		wlBeforeSQL=new Label(comp, SWT.RIGHT);
		wlBeforeSQL.setText(BaseMessages.getString(PKG, "DB2LoadDialog.BeforeSQL.Label"));
 		props.setLook(wlBeforeSQL);
		fdlBeforeSQL=new FormData();
		fdlBeforeSQL.left  = new FormAttachment(0, 0);
		fdlBeforeSQL.top   = new FormAttachment(wEncoding, margin);
		fdlBeforeSQL.right = new FormAttachment(middle, -margin);
		wlBeforeSQL.setLayoutData(fdlBeforeSQL);
		wBeforeSQL=new TextVar(transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wBeforeSQL);
		fdBeforeSQL=new FormData();
		fdBeforeSQL.left  = new FormAttachment(middle, 0);
		fdBeforeSQL.top   = new FormAttachment(wEncoding, margin);
		fdBeforeSQL.right = new FormAttachment(100, 0);
		wBeforeSQL.setLayoutData(fdBeforeSQL);
		wBeforeSQL.addSelectionListener(new SelectionAdapter() 
	    {
	        public void widgetSelected(SelectionEvent e) 
	        {
			    input.setChanged();
	    	}
       }
   );
		wlAfterSQL=new Label(comp, SWT.RIGHT);
		wlAfterSQL.setText(BaseMessages.getString(PKG, "DB2LoadDialog.AfterSQL.Label"));
 		props.setLook(wlAfterSQL);
		fdlAfterSQL=new FormData();
		fdlAfterSQL.left  = new FormAttachment(0, 0);
		fdlAfterSQL.top   = new FormAttachment(wBeforeSQL, margin);
		fdlAfterSQL.right = new FormAttachment(middle, -margin);
		wlAfterSQL.setLayoutData(fdlAfterSQL);
		wAfterSQL=new TextVar(transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wAfterSQL);
		fdAfterSQL=new FormData();
		fdAfterSQL.left  = new FormAttachment(middle, 0);
		fdAfterSQL.top   = new FormAttachment(wBeforeSQL, margin);
		fdAfterSQL.right = new FormAttachment(100, 0);
		wAfterSQL.setLayoutData(fdAfterSQL);
		wAfterSQL.addSelectionListener(new SelectionAdapter() 
	    {
	        public void widgetSelected(SelectionEvent e) 
	        {
			    input.setChanged();
	    	}
       }
   );
		
		// THE BUTTONS
		wOK = new Button(comp, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wSQL = new Button(comp, SWT.PUSH);
		wSQL.setText(BaseMessages.getString(PKG, "DB2LoadDialog.SQL.Button")); //$NON-NLS-1$
		wCancel = new Button(comp, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel , wSQL }, margin, null);

		// The field Table
		wlReturn = new Label(comp, SWT.NONE);
		wlReturn.setText(BaseMessages.getString(PKG, "DB2LoadDialog.Fields.Label")); //$NON-NLS-1$
		props.setLook(wlReturn);
		fdlReturn = new FormData();
		fdlReturn.left = new FormAttachment(0, 0);
		fdlReturn.top = new FormAttachment(wAfterSQL, margin);
		wlReturn.setLayoutData(fdlReturn);

		int UpInsCols = 3;
		int UpInsRows = (input.getFieldTable() != null ? input.getFieldTable().length : 10);

		ciReturn = new ColumnInfo[UpInsCols];
		ciReturn[0] = new ColumnInfo(BaseMessages.getString(PKG, "DB2LoadDialog.ColumnInfo.TableField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[1] = new ColumnInfo(BaseMessages.getString(PKG, "DB2LoadDialog.ColumnInfo.StreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[2] = new ColumnInfo(BaseMessages.getString(PKG, "DB2LoadDialog.ColumnInfo.DateMask"), ColumnInfo.COLUMN_TYPE_CCOMBO, 
				                     new String[] {"",                //$NON-NLS-1$
			                                       BaseMessages.getString(PKG, "DB2LoadDialog.DateMask.Label"),
	                                        	   BaseMessages.getString(PKG, "DB2LoadDialog.DateTimeMask.Label")}, true); 
		tableFieldColumns.add(ciReturn[0]);
		wReturn = new TableView(transMeta, comp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciReturn, UpInsRows, lsMod, props);

		wGetLU = new Button(comp, SWT.PUSH);
		wGetLU.setText(BaseMessages.getString(PKG, "DB2LoadDialog.GetFields.Label")); //$NON-NLS-1$
		fdGetLU = new FormData();
		fdGetLU.top   = new FormAttachment(wlReturn, margin);
		fdGetLU.right = new FormAttachment(100, 0);
		wGetLU.setLayoutData(fdGetLU);
		
		fdReturn = new FormData();
		fdReturn.left = new FormAttachment(0, 0);
		fdReturn.top = new FormAttachment(wlReturn, margin);
		fdReturn.right = new FormAttachment(wGetLU, -margin);
		fdReturn.bottom = new FormAttachment(wOK, -2*margin);
		wReturn.setLayoutData(fdReturn);
		
       FormData fdComp = new FormData();
       fdComp.left  = new FormAttachment(0, 0);
       fdComp.top   = new FormAttachment(0, 0);
       fdComp.right = new FormAttachment(100, 0);
       fdComp.bottom= new FormAttachment(100, 0);
       comp.setLayoutData(fdComp);

       comp.pack();
       Rectangle bounds = comp.getBounds();

       sComp.setContent(comp);
       sComp.setExpandHorizontal(true);
       sComp.setExpandVertical(true);
       sComp.setMinWidth(bounds.width);
       sComp.setMinHeight(bounds.height);

	    // 
       // Search the fields in the background
       //
       
       final Runnable runnable = new Runnable()
       {
           public void run()
           {
               StepMeta stepMeta = transMeta.findStep(stepname);
               if (stepMeta!=null)
               {
                   try
                   {
                   	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                       
                       // Remember these fields...
                       for (int i=0;i<row.size();i++)
                       {
                       	inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                       }
                       
                       setComboBoxes(); 
                   }
                   catch(KettleException e)
                   {
                       logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                   }
               }
           }
       };
       new Thread(runnable).start();

       wbTable.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					getTableName();
				}
			}
		);
		wbSchema.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					getSchemaNames();
				}
			}
		);
		wbSqlldr.addSelectionListener(new SelectionAdapter()
       {
           public void widgetSelected(SelectionEvent e)
           {
               FileDialog dialog = new FileDialog(shell, SWT.OPEN);
               dialog.setFilterExtensions(new String[] { "*" });
               if (wSqlldr.getText() != null)
               {
                   dialog.setFileName(wSqlldr.getText());
               }
               dialog.setFilterNames(ALL_FILETYPES);
               if (dialog.open() != null)
               {
               	wSqlldr.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                     + dialog.getFileName());
               }
           }
       });
			
		wbDataFile.addSelectionListener(new SelectionAdapter()
       {
           public void widgetSelected(SelectionEvent e)
           {
               FileDialog dialog = new FileDialog(shell, SWT.OPEN);
               dialog.setFilterExtensions(new String[] { "*" });
               if (wDataFile.getText() != null)
               {
                   dialog.setFileName(wDataFile.getText());
               }                
               dialog.setFilterNames(ALL_FILETYPES);
               if (dialog.open() != null)
               {
               	wDataFile.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                     + dialog.getFileName());
               }
           }
       });	

		wbLogFile.addSelectionListener(new SelectionAdapter()
       {
           public void widgetSelected(SelectionEvent e)
           {
               FileDialog dialog = new FileDialog(shell, SWT.OPEN);
               dialog.setFilterExtensions(new String[] { "*" });
               if (wLogFile.getText() != null)
               {
                   dialog.setFileName(wLogFile.getText());
               }                
               dialog.setFilterNames(ALL_FILETYPES);
               if (dialog.open() != null)
               {
               	wLogFile.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                     + dialog.getFileName());
               }
           }
       });
		
		wbBadFile.addSelectionListener(new SelectionAdapter()
       {
           public void widgetSelected(SelectionEvent e)
           {
               FileDialog dialog = new FileDialog(shell, SWT.OPEN);
               dialog.setFilterExtensions(new String[] { "*" });
               if (wBadFile.getText() != null)
               {
                   dialog.setFileName(wBadFile.getText());
               }                
               dialog.setFilterNames(ALL_FILETYPES);
               if (dialog.open() != null)
               {
               	wBadFile.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                     + dialog.getFileName());
               }
           }
       });	
		
		wbDiscardFile.addSelectionListener(new SelectionAdapter()
       {
           public void widgetSelected(SelectionEvent e)
           {
               FileDialog dialog = new FileDialog(shell, SWT.OPEN);
               dialog.setFilterExtensions(new String[] { "*" });
               if (wDiscardFile.getText() != null)
               {
                   dialog.setFileName(wDiscardFile.getText());
               }                
               dialog.setFilterNames(ALL_FILETYPES);
               if (dialog.open() != null)
               {
               	wDiscardFile.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                     + dialog.getFileName());
               }
           }
       });			
		
		// Add listeners
		lsOK = new Listener()
		{
			public void handleEvent(Event e)
			{
				ok();
			}
		};
		lsGetLU = new Listener()
		{
			public void handleEvent(Event e)
			{
				getUpdate();
			}
		};
		lsSQL = new Listener()
		{
			public void handleEvent(Event e)
			{
				create();
			}
		};
		lsCancel = new Listener()
		{
			public void handleEvent(Event e)
			{
				cancel();
			}
		};

		wOK.addListener(SWT.Selection, lsOK);
		wGetLU.addListener(SWT.Selection, lsGetLU);
		wSQL.addListener(SWT.Selection, lsSQL);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wStepname.addSelectionListener(lsDef);
       wSchema.addSelectionListener(lsDef);
       wTable.addSelectionListener(lsDef);
       wDataFile.addSelectionListener(lsDef);
       wLogFile.addSelectionListener(lsDef);
       wBadFile.addSelectionListener(lsDef);
       wDiscardFile.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent e)
			{
				cancel();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		//setTableFieldCombo();
		input.setChanged(changed);
		checkPriviledges();
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}
	protected void setComboBoxes()
   {
       // Something was changed in the row.
       //
		final Map<String, Integer> fields = new HashMap<String, Integer>();
       
       // Add the currentMeta fields...
       fields.putAll(inputFields);
       
       Set<String> keySet = fields.keySet();
       List<String> entries = new ArrayList<String>(keySet);
       
       String[] fieldNames= (String[]) entries.toArray(new String[entries.size()]);
       Const.sortStrings(fieldNames);
       // return fields
       ciReturn[1].setComboValues(fieldNames);
       
   }
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		int i;
		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "DB2LoadDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		if (input.getFieldTable() != null)
			for (i = 0; i < input.getFieldTable().length; i++)
			{
				TableItem item = wReturn.table.getItem(i);
				if (input.getFieldTable()[i] != null)
					item.setText(1, input.getFieldTable()[i]);
				if (input.getFieldStream()[i] != null)
					item.setText(2, input.getFieldStream()[i]);
				String dateMask = input.getDateMask()[i];
				if (dateMask!=null) {
					if ( DB2LoadMeta.DATE_MASK_DATE.equals(dateMask) )
					{
					    item.setText(3,BaseMessages.getString(PKG, "DB2LoadDialog.DateMask.Label"));
					}
					else if ( DB2LoadMeta.DATE_MASK_DATETIME.equals(dateMask)) 
					{
						item.setText(3,BaseMessages.getString(PKG, "DB2LoadDialog.DateTimeMask.Label"));
					}
					else 
					{
						item.setText(3,"");
					}					
				} 
				else {
					item.setText(3,"");
				}
			}
			if (input.getDatabaseMeta() != null) wConnection.setText(input.getDatabaseMeta().getName());
			if (input.getDatabaseName() != null) wDB2DatabaseName.setText(input.getDatabaseName());
	        if (input.getSchemaName() != null) wSchema.setText(input.getSchemaName());
			if (input.getTableName() != null) wTable.setText(input.getTableName());
			if (input.getDB2ClientDir() != null) wSqlldr.setText(input.getDB2ClientDir());
			if (input.getDataFile() != null) wDataFile.setText(input.getDataFile());
			if (input.getLogFile() != null) wLogFile.setText(input.getLogFile());
			if (input.getBadFile() != null) wBadFile.setText(input.getBadFile());
			if (input.getDiscardFile() != null) wDiscardFile.setText(input.getDiscardFile());	
			if (input.getBeforSQL() != null) wBeforeSQL.setText(input.getBeforSQL());
			if (input.getAfterSQL() != null) wAfterSQL.setText(input.getAfterSQL());
			if (input.getPageCode() != null) wEncoding.setText(input.getPageCode());
		
			String action = input.getLoadAction();
			if ( DB2LoadMeta.ACTION_APPEND.equals(action))
			{
				wLoadAction.select(0);
			}
			else if ( DB2LoadMeta.ACTION_REPLACE.equals(action))
			{
				wLoadAction.select(1);
			}
			else if ( DB2LoadMeta.ACTION_TRUNCATE.equals(action))
			{
				wLoadAction.select(2);
			}
			else
			{
				if(log.isDebug()) logDebug("Internal error: load_action set to default 'append'"); //$NON-NLS-1$
				wLoadAction.select(0);
			}

		String truncateAction = input.getTruncateAction();
		if ( DB2LoadMeta.TRUNCATE_ACTION_DEFAULT.equals(truncateAction))
		{
			wTruncateAction.select(0);
		}
		else if ( DB2LoadMeta.TRUNCATE_ACTION_TRUNCATE.equals(truncateAction))
		{
			wTruncateAction.select(1);
		}
		else if ( DB2LoadMeta.TRUNCATE_ACTION_DELETE.equals(truncateAction))
		{
			wTruncateAction.select(2);
		}
		else if ( DB2LoadMeta.TRUNCATE_ACTION_CUSTOM.equals(truncateAction))
		{
			wTruncateAction.select(3);
		}
		else
		{
			if(log.isDebug()) logDebug("Internal error: load_action set to default 'append'"); //$NON-NLS-1$
			wTruncateAction.select(0);
		}
		wTruncateSQL.setText(input.getTruncateSQL()==null?"":input.getTruncateSQL());
		
		
			wStepname.selectAll();
			wReturn.setRowNums();
			wReturn.optWidth(true);
			setFlagsTruncateOption();
			setFlagsTruncateSQLOption();
	}
	
	private void cancel()
	{
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void getInfo(DB2LoadMeta inf)
	{
		int nrfields = wReturn.nrNonEmpty();

		inf.allocate(nrfields);

		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "DB2LoadDialog.Log.FoundFields", "" + nrfields)); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < nrfields; i++)
		{
			TableItem item = wReturn.getNonEmpty(i);
			inf.getFieldTable()[i] = item.getText(1);
			inf.getFieldStream()[i] = item.getText(2);
			if ( BaseMessages.getString(PKG, "DB2LoadDialog.DateMask.Label").equals(item.getText(3)) )
			    inf.getDateMask()[i] = DB2LoadMeta.DATE_MASK_DATE;
			else if ( BaseMessages.getString(PKG, "DB2LoadDialog.DateTimeMask.Label").equals(item.getText(3)) )
				inf.getDateMask()[i] = DB2LoadMeta.DATE_MASK_DATETIME;
			else inf.getDateMask()[i] = "";
		}
		inf.setDatabaseMeta(  transMeta.findDatabase(wConnection.getText()) );
		inf.setSchemaName( wSchema.getText() );
		inf.setTableName( wTable.getText() );
		inf.setDatabaseName(wDB2DatabaseName.getText());
		inf.setDB2ClientDir( wSqlldr.getText() );
		inf.setDataFile( wDataFile.getText() );
		inf.setLogFile( wLogFile.getText() );
		inf.setBadFile( wBadFile.getText() );
		inf.setDiscardFile( wDiscardFile.getText() );
		inf.setPageCode( wEncoding.getText() );
		inf.setBeforSQL( wBeforeSQL.getText() );
		inf.setAfterSQL( wAfterSQL.getText() );
		
		/*
		 * Set the loadaction 
		 */
		String action = wLoadAction.getText();
		if ( BaseMessages.getString(PKG, "DB2LoadDialog.AppendLoadAction.Label").equals(action) ) 
		{
			inf.setLoadAction(DB2LoadMeta.ACTION_APPEND);
		}
		else if ( BaseMessages.getString(PKG, "DB2LoadDialog.ReplaceLoadAction.Label").equals(action) )
		{
			inf.setLoadAction(DB2LoadMeta.ACTION_REPLACE);
		}
		else if ( BaseMessages.getString(PKG, "DB2LoadDialog.TruncateLoadAction.Label").equals(action) )
		{
			inf.setLoadAction(DB2LoadMeta.ACTION_TRUNCATE);
		}
		else
		{
			if(log.isDebug()) logDebug("Internal error: load_action set to default 'append', value found '" + action + "'."); //$NON-NLS-1$
			inf.setLoadAction(DB2LoadMeta.ACTION_APPEND);	
		}

		String truncateActionText = wTruncateAction.getText();
		if ( DB2LoadMeta.TRUNCATE_ACTION_DEFAULT.equals(truncateActionText) )
		{
			inf.setTruncateAction(DB2LoadMeta.TRUNCATE_ACTION_DEFAULT);
		}
		else if ( DB2LoadMeta.TRUNCATE_ACTION_TRUNCATE.equals(truncateActionText) )
		{
			inf.setTruncateAction(DB2LoadMeta.TRUNCATE_ACTION_TRUNCATE);
		}
		else if ( DB2LoadMeta.TRUNCATE_ACTION_DELETE.equals(truncateActionText) )
		{
			inf.setTruncateAction(DB2LoadMeta.TRUNCATE_ACTION_DELETE);
		}
		else  if ( DB2LoadMeta.TRUNCATE_ACTION_CUSTOM.equals(truncateActionText) )
		{
			inf.setTruncateAction(DB2LoadMeta.TRUNCATE_ACTION_CUSTOM);
		}
		else
		{
			inf.setTruncateAction(DB2LoadMeta.TRUNCATE_ACTION_DEFAULT);
		}

		inf.setTruncateSQL(wTruncateSQL.getText());

		stepname = wStepname.getText(); // return value
	}

	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		// Get the information for the dialog into the input structure.
		getInfo(input);
		if(input.getDatabaseMeta() != null){
			if(input.getDatabaseMeta().getDatabaseInterface() instanceof DB2DatabaseMeta){
				//ok 
			}else{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				mb.setMessage("Please choose DB2 connection !");
				mb.setText(BaseMessages.getString(PKG, "DB2LoadDialog.SQLError.DialogTitle"));
				mb.open();
				return;
			}
		}
		dispose();
	}

	private void getUpdate()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r != null)
			{
               TableItemInsertListener listener = new TableItemInsertListener()
               {
                   public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                   {
                   	if ( v.getType() == ValueMetaInterface.TYPE_DATE )
                   	{
                   		// The default is date mask.
                   		tableItem.setText(3, BaseMessages.getString(PKG, "DB2LoadDialog.DateMask.Label"));	
                   	}
                   	else
                   	{
                           tableItem.setText(3, "");
                   	}
                       return true;
                   }
               };
               BaseStepDialog.getFieldsFromPrevious(r, wReturn, 1, new int[] { 1, 2}, new int[] {}, -1, -1, listener);
			}else{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				mb.setMessage("Prev step fields is empty !");
				mb.setText(BaseMessages.getString(PKG, "DB2LoadDialog.SQLError.DialogTitle")); //$NON-NLS-1$
				mb.open();
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "DB2LoadDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
					BaseMessages.getString(PKG, "DB2LoadDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
		}
	}

	// Generate code for create table...
	// Conversions done by Database
	private void create()
	{
		try
		{
			DB2LoadMeta info = new DB2LoadMeta();
			getInfo(info);

			String name = stepname; // new name might not yet be linked to other steps!
			StepMeta stepMeta = new StepMeta(BaseMessages.getString(PKG, "DB2LoadDialog.StepMeta.Title"), name, info); //$NON-NLS-1$
			RowMetaInterface prev = transMeta.getPrevStepFields(stepname);

			SQLStatement sql = info.getSQLStatements(transMeta, stepMeta, prev);
			if (!sql.hasError())
			{
				if (sql.hasSQL())
				{
					SQLEditor sqledit = new SQLEditor(transMeta, shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(),
							sql.getSQL());
					sqledit.open();
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					mb.setMessage(BaseMessages.getString(PKG, "DB2LoadDialog.NoSQLNeeds.DialogMessage")); //$NON-NLS-1$
					mb.setText(BaseMessages.getString(PKG, "DB2LoadDialog.NoSQLNeeds.DialogTitle")); //$NON-NLS-1$
					mb.open();
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				mb.setMessage(sql.getError());
				mb.setText(BaseMessages.getString(PKG, "DB2LoadDialog.SQLError.DialogTitle")); //$NON-NLS-1$
				mb.open();
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "DB2LoadDialog.CouldNotBuildSQL.DialogTitle"), //$NON-NLS-1$
					BaseMessages.getString(PKG, "DB2LoadDialog.CouldNotBuildSQL.DialogMessage"), ke); //$NON-NLS-1$
		}

	}
	
	private void getSchemaNames()
	{
		DatabaseMeta databaseMeta = transMeta.findDatabase(wConnection.getText());
		if (databaseMeta!=null)
		{
			Database database = new Database(loggingObject, databaseMeta);
			try
			{
				database.connect();
				String schemas[] = database.getSchemas();
				
				if (null != schemas && schemas.length>0) {
					schemas=Const.sortStrings(schemas);	
					EnterSelectionDialog dialog = new EnterSelectionDialog(shell, schemas, 
							BaseMessages.getString(PKG,"DB2LoadDialog.AvailableSchemas.Title",wConnection.getText()), 
							BaseMessages.getString(PKG,"DB2LoadDialog.AvailableSchemas.Message",wConnection.getText()));
					String d=dialog.open();
					if (d!=null) 
					{
						wSchema.setText(Const.NVL(d.toString(), ""));
						setTableFieldCombo();
					}

				}else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage(BaseMessages.getString(PKG,"DB2LoadDialog.NoSchema.Error"));
					mb.setText(BaseMessages.getString(PKG,"DB2LoadDialog.GetSchemas.Error"));
					mb.open(); 
				}
			}
			catch(Exception e)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.Error.Title"), 
						BaseMessages.getString(PKG,"DB2LoadDialog.ErrorGettingSchemas"), e);
			}
			finally
			{
				if(database!=null) 
				{
					database.disconnect();
					database=null;
				}
			}
		}
	}
	private void getTableName()
	{
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr>=0)
		{
			DatabaseMeta inf = transMeta.getDatabase(connr);
						
			if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "DB2LoadDialog.Log.LookingAtConnection", inf.toString()));
		
			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
			std.setSelectedSchemaAndTable(wSchema.getText(), wTable.getText());
			if (std.open())
			{
                wSchema.setText(Const.NVL(std.getSchemaName(), ""));
                wTable.setText(Const.NVL(std.getTableName(), ""));
                setTableFieldCombo();
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "DB2LoadDialog.ConnectionError2.DialogMessage"));
			mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
			mb.open(); 
		}
					
	}

	protected void setFlagsTruncateSQLOption() {
		String action = wLoadAction.getText();
		boolean truncateTable = BaseMessages.getString(PKG, "DB2LoadDialog.TruncateLoadAction.Label").equals(action);
		boolean custom = DB2LoadMeta.TRUNCATE_ACTION_CUSTOM.equals(wTruncateAction.getText());
		wTruncateSQL.setEnabled(truncateTable && custom);
	}

	protected void setFlagsTruncateOption() {
		String action = wLoadAction.getText();
		boolean truncateTable = BaseMessages.getString(PKG, "DB2LoadDialog.TruncateLoadAction.Label").equals(action);
		wTruncateAction.setEnabled(truncateTable);
	}


	private void setTableFieldCombo(){
		  // define a reusable Runnable to clear out the table fields
		  final Runnable clearTableFields = new Runnable() {
	      public void run() {
	        for (int i = 0; i < tableFieldColumns.size(); i++) {
	          ColumnInfo colInfo = tableFieldColumns.get(i);
	          colInfo.setComboValues(new String[] {});
	        }
	      }
		  };

		  // clear out the fields
		  shell.getDisplay().asyncExec(clearTableFields);

		  if (!Const.isEmpty(wTable.getText())) {
	      final DatabaseMeta ci = transMeta.findDatabase(wConnection.getText());
	      
	      if (ci != null) {
	        final Database db = new Database(loggingObject, ci);
	        
	        // RCF 04.21.2010 - create a new thread to get the database connection in, this way it does not block the UI thread if the DB is unreachable
	        new Thread(new Runnable(){
	          public void run() {
	            try {
	              db.connect();
	              
	              // now that we have the db, we need to modify stuff in the UI. we do this in the UI thread with asyncExec.
	              shell.getDisplay().asyncExec(new Runnable() {                
	                public void run() {
	                  String schemaTable = ci.getQuotedSchemaTableCombination(transMeta.environmentSubstitute(wSchema
	                      .getText()), transMeta.environmentSubstitute(wTable.getText()));
	                  RowMetaInterface r;
	                  try {
	                    r = db.getTableFields(schemaTable);
	                    if (null != r) {
	                      final String[] fieldNames = r.getFieldNames();
	                      if (null != fieldNames) {
	                        for (int i = 0; i < tableFieldColumns.size(); i++) {
	                          ColumnInfo colInfo = tableFieldColumns.get(i);
	                          colInfo.setComboValues(fieldNames);
	                        }
	                      }
	                    }
	                  } catch (KettleDatabaseException e) {
	                    shell.getDisplay().asyncExec(clearTableFields);
	                  }
	                }
	              });
	            } catch (Exception e) {
	              shell.getDisplay().asyncExec(clearTableFields);
	            }
	          }
	        }).start();        
	      }
		  }
		}
}