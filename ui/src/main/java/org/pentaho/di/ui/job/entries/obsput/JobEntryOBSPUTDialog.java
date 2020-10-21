/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.job.entries.obsput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.obsput.JobEntryOBSPUT;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the OBS Put job entry settings.
 *
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryOBSPUTDialog extends JobEntryDialog implements JobEntryDialogInterface {
  private static Class<?> PKG = JobEntryOBSPUT.class; // for i18n purposes, needed by Translator2!!
  private static final String[] FILETYPES = new String[] {
    BaseMessages.getString( PKG, "JobOBSPUT.Filetype.Pem" ),
    BaseMessages.getString( PKG, "JobOBSPUT.Filetype.All" ) };

  private Label wlName;
  private Text wName;
  private FormData fdlName, fdName;

  private Label wlEndPoint;
  private TextVar wEndPoint;
  private FormData fdlEndPoint, fdEndPoint;

  private Label wlBucketName;
  private TextVar wBucketName;
  private FormData fdlBucketName, fdBucketName;

  private Label wlak;
  private TextVar wak;
  private FormData fdlak, fdak;

  private Label wlsk;
  private TextVar wsk;
  private FormData fdlsk, fdsk;

//  private Label wlScpDirectory;
//  private TextVar wScpDirectory;
//  private FormData fdlScpDirectory, fdScpDirectory;
//
//  private Label wlLocalDirectory;
//  private TextVar wLocalDirectory;
//  private FormData fdlLocalDirectory, fdLocalDirectory;
//
//  private Label wlWildcard;
//  private TextVar wWildcard;
//  private FormData fdlWildcard, fdWildcard;

  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;

  private JobEntryOBSPUT jobEntry;
  private Shell shell;

  private Label wlCreateRemoteFolder;
  private Button wCreateRemoteFolder;
  private FormData fdlCreateRemoteFolder, fdCreateRemoteFolder;

  private SelectionAdapter lsDef;

  private Group wServerSettings;

  private FormData fdServerSettings;

  private Group wSourceFiles;

  private FormData fdSourceFiles;

  private Group wTargetFiles;

  private FormData fdTargetFiles;

  private Button wbLocalDirectory;

  private FormData fdbLocalDirectory;

  private boolean changed;

  private Button wTest;

  private FormData fdTest;

  private Listener lsTest;

  private Listener lsCheckChangeFolder;

  private Button wbTestChangeFolderExists;

  private FormData fdbTestChangeFolderExists;

  private Label wlgetPrevious;

  private Button wgetPrevious;

  private FormData fdlgetPrevious, fdgetPrevious;

  private Label wlgetPreviousFiles;

  private Button wgetPreviousFiles;

  private FormData fdlgetPreviousFiles, fdgetPreviousFiles;

  private Label wlSuccessWhenNoFile;

  private Button wSuccessWhenNoFile;

  private FormData fdlSuccessWhenNoFile, fdSuccessWhenNoFile;

  private Label wlAddFilenameToResult;

  private Button wAddFilenameToResult;

  private FormData fdlAddFilenameToResult, fdAddFilenameToResult;

  private LabelTextVar wkeyfilePass;

  private FormData fdkeyfilePass;

  private Label wlusePublicKey;

  private Button wusePublicKey;

  private FormData fdlusePublicKey, fdusePublicKey;

  private Label wlKeyFilename;

  private Button wbKeyFilename;

  private TextVar wKeyFilename;

  private FormData fdlKeyFilename, fdbKeyFilename, fdKeyFilename;

  private CTabFolder wTabFolder;
  private Composite wGeneralComp, wFilesComp;
  private CTabItem wGeneralTab, wFilesTab;
  private FormData fdGeneralComp, fdFilesComp;
  private FormData fdTabFolder;

  //
  private Label wlOBSFilePrefix;
  private FormData fdlOBSFilePrefix;
  private CCombo wOBSFilePrefix;//set flag
  private FormData fdOBSFilePrefix;

  private LabelTextVar wFileDir;
  private FormData fdFileDir;

  private LabelTextVar wCounter;
  private FormData fdCounter;

  private LabelTextVar wStartValue;
  private FormData fdStartValue;

  private LabelTextVar wMaxValue;
  private FormData fdMaxValue;

  public JobEntryOBSPUTDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta ) {
    super( parent, jobEntryInt, rep, jobMeta );
    jobEntry = (JobEntryOBSPUT) jobEntryInt;
    if ( this.jobEntry.getName() == null ) {
      this.jobEntry.setName( BaseMessages.getString( PKG, "JobOBSPUT.Title" ) );
    }
  }

  public JobEntryInterface open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    JobDialog.setShellImage( shell, jobEntry );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
       // sftpclient = null;
        jobEntry.setChanged();
      }
    };
    changed = jobEntry.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "JobOBSPUT.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Filename line
    wlName = new Label( shell, SWT.RIGHT );
    wlName.setText( BaseMessages.getString( PKG, "JobOBSPUT.Name.Label" ) );
    props.setLook( wlName );
    fdlName = new FormData();
    fdlName.left = new FormAttachment( 0, 0 );
    fdlName.right = new FormAttachment( middle, -margin );
    fdlName.top = new FormAttachment( 0, margin );
    wlName.setLayoutData( fdlName );
    wName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    wName.addModifyListener( lsMod );
    fdName = new FormData();
    fdName.left = new FormAttachment( middle, 0 );
    fdName.top = new FormAttachment( 0, margin );
    fdName.right = new FormAttachment( 100, 0 );
    wName.setLayoutData( fdName );

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF GENERAL TAB ///
    // ////////////////////////

    wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "JobOBSPUT.Tab.General.Label" ) );

    wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );

    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;
    wGeneralComp.setLayout( generalLayout );

    // ////////////////////////
    // START OF SERVER SETTINGS GROUP///
    // /
    wServerSettings = new Group( wGeneralComp, SWT.SHADOW_NONE );
    props.setLook( wServerSettings );
    wServerSettings.setText( BaseMessages.getString( PKG, "JobOBSPUT.ServerSettings.Group.Label" ) );
    FormLayout ServerSettingsgroupLayout = new FormLayout();
    ServerSettingsgroupLayout.marginWidth = 10;
    ServerSettingsgroupLayout.marginHeight = 10;
    wServerSettings.setLayout( ServerSettingsgroupLayout );

    // ServerName line
    wlEndPoint = new Label( wServerSettings, SWT.RIGHT );
    wlEndPoint.setText( BaseMessages.getString( PKG, "JobOBSPUT.Server.Label" ) );
    props.setLook(wlEndPoint);
    fdlEndPoint = new FormData();
    fdlEndPoint.left = new FormAttachment( 0, 0 );
    fdlEndPoint.top = new FormAttachment( wName, margin );
    fdlEndPoint.right = new FormAttachment( middle, -margin );
    wlEndPoint.setLayoutData( fdlEndPoint );
    wEndPoint = new TextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wEndPoint );
    wEndPoint.addModifyListener( lsMod );
    fdEndPoint = new FormData();
    fdEndPoint.left = new FormAttachment( middle, 0 );
    fdEndPoint.top = new FormAttachment( wName, margin );
    fdEndPoint.right = new FormAttachment( 100, 0 );
    wEndPoint.setLayoutData(fdEndPoint);

    // ServerPort line
    wlBucketName = new Label( wServerSettings, SWT.RIGHT );
    wlBucketName.setText( BaseMessages.getString( PKG, "JobOBSPUT.BucketName.Label" ) );
    props.setLook(wlBucketName);
    fdlBucketName = new FormData();
    fdlBucketName.left = new FormAttachment( 0, 0 );
    fdlBucketName.top = new FormAttachment( wEndPoint, margin );
    fdlBucketName.right = new FormAttachment( middle, -margin );
    wlBucketName.setLayoutData(fdlBucketName);
    wBucketName = new TextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook(wBucketName);
    wBucketName.setToolTipText( BaseMessages.getString( PKG, "JobOBSPUT.Port.Tooltip" ) );
    wBucketName.addModifyListener( lsMod );
    fdBucketName = new FormData();
    fdBucketName.left = new FormAttachment( middle, 0 );
    fdBucketName.top = new FormAttachment( wEndPoint, margin );
    fdBucketName.right = new FormAttachment( 100, 0 );
    wBucketName.setLayoutData(fdBucketName);

    // UserName line
    wlak = new Label( wServerSettings, SWT.RIGHT );
    wlak.setText( BaseMessages.getString( PKG, "JobOBSPUT.ak.Label" ) );
    props.setLook(wlak);
    fdlak = new FormData();
    fdlak.left = new FormAttachment( 0, 0 );
    fdlak.top = new FormAttachment(wBucketName, margin );
    fdlak.right = new FormAttachment( middle, -margin );
    wlak.setLayoutData(fdlak);
    wak = new TextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook(wak);
    wak.setToolTipText( BaseMessages.getString( PKG, "JobOBSPUT.ak.Tooltip" ) );
    wak.addModifyListener( lsMod );
    fdak = new FormData();
    fdak.left = new FormAttachment( middle, 0 );
    fdak.top = new FormAttachment(wBucketName, margin );
    fdak.right = new FormAttachment( 100, 0 );
    wak.setLayoutData(fdak);

    // Password line
    wlsk = new Label( wServerSettings, SWT.RIGHT );
    wlsk.setText( BaseMessages.getString( PKG, "JobOBSPUT.sk.Label" ) );
    props.setLook(wlsk);
    fdlsk = new FormData();
    fdlsk.left = new FormAttachment( 0, 0 );
    fdlsk.top = new FormAttachment(wak, margin );
    fdlsk.right = new FormAttachment( middle, -margin );
    wlsk.setLayoutData(fdlsk);
    wsk = new PasswordTextVar( jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook(wsk);
    wsk.addModifyListener( lsMod );
    fdsk = new FormData();
    fdsk.left = new FormAttachment( middle, 0 );
    fdsk.top = new FormAttachment(wak, margin );
    fdsk.right = new FormAttachment( 100, 0 );
    wsk.setLayoutData(fdsk);

    // local file dir
    wFileDir = new LabelTextVar( jobMeta, wServerSettings,
      BaseMessages.getString( PKG, "JobOBSPUT.FileDir.Label" ),
      BaseMessages.getString( PKG, "JobOBSPUT.FileDir.Tooltip" ) );
    props.setLook(wFileDir);
    wFileDir.addModifyListener( lsMod );
    fdFileDir = new FormData();
    fdFileDir.left = new FormAttachment( 0, -2 * margin );
    fdFileDir.top = new FormAttachment( wsk, margin );
    fdFileDir.right = new FormAttachment( 100, 0 );
    wFileDir.setLayoutData(fdFileDir);

    // start index
    wCounter =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobOBSPUT.Counter.Label" ), BaseMessages
          .getString( PKG, "JobOBSPUT.ProxyPort.Tooltip" ) );
    props.setLook(wCounter);
    wCounter.addModifyListener( lsMod );
    fdCounter = new FormData();
    fdCounter.left = new FormAttachment( 0, -2 * margin );
    fdCounter.top = new FormAttachment(wFileDir, margin );
    fdCounter.right = new FormAttachment( 100, 0 );
    wCounter.setLayoutData( fdCounter );

    // max index
    wStartValue =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobOBSPUT.StartValue.Label" ),
        BaseMessages.getString( PKG, "JobOBSPUT.StartValue.Tooltip" ) );
    props.setLook(wStartValue);
    wStartValue.addModifyListener( lsMod );
    fdStartValue = new FormData();
    fdStartValue.left = new FormAttachment( 0, -2 * margin );
    fdStartValue.top = new FormAttachment(wCounter, margin );
    fdStartValue.right = new FormAttachment( 100, 0 );
    wStartValue.setLayoutData(fdStartValue);

    // Proxy password line
    wMaxValue =
      new LabelTextVar(
        jobMeta, wServerSettings, BaseMessages.getString( PKG, "JobOBSPUT.MaxValue.Label" ),
        BaseMessages.getString( PKG, "JobOBSPUT.MaxValue.Tooltip" ), false );
    props.setLook(wMaxValue);
    wMaxValue.addModifyListener( lsMod );
    fdMaxValue = new FormData();
    fdMaxValue.left = new FormAttachment( 0, -2 * margin );
    fdMaxValue.top = new FormAttachment(wStartValue, margin );
    fdMaxValue.right = new FormAttachment( 100, 0 );
    wMaxValue.setLayoutData(fdMaxValue);

    fdServerSettings = new FormData();
    fdServerSettings.left = new FormAttachment( 0, margin );
    fdServerSettings.top = new FormAttachment( wName, margin );
    fdServerSettings.right = new FormAttachment( 100, -margin );
    wServerSettings.setLayoutData( fdServerSettings );
    // ///////////////////////////////////////////////////////////
    // / END OF SERVER SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    wlOBSFilePrefix = new Label( wGeneralComp, SWT.RIGHT );
    wlOBSFilePrefix.setText( BaseMessages.getString( PKG, "JobOBSPUT.OBSFilePrefix.Label" ) );
    props.setLook(wlOBSFilePrefix);
    fdlOBSFilePrefix = new FormData();
    fdlOBSFilePrefix.left = new FormAttachment( 0, -margin );
    fdlOBSFilePrefix.right = new FormAttachment( middle, 0 );
    fdlOBSFilePrefix.top = new FormAttachment( wServerSettings, margin );
    wlOBSFilePrefix.setLayoutData(fdlOBSFilePrefix);

    wOBSFilePrefix = new CCombo( wGeneralComp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wOBSFilePrefix.add( "0" );
    wOBSFilePrefix.add( "1" );
    wOBSFilePrefix.select( 0 ); // +1: starts at -1

    props.setLook(wOBSFilePrefix);
    fdOBSFilePrefix = new FormData();
    fdOBSFilePrefix.left = new FormAttachment( middle, margin );
    fdOBSFilePrefix.top = new FormAttachment( wServerSettings, margin );
    fdOBSFilePrefix.right = new FormAttachment( 100, 0 );
    wOBSFilePrefix.setLayoutData(fdOBSFilePrefix);

    fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment( 0, 0 );
    fdGeneralComp.top = new FormAttachment( 0, 0 );
    fdGeneralComp.right = new FormAttachment( 100, 0 );
    fdGeneralComp.bottom = new FormAttachment( 100, 0 );
    wGeneralComp.setLayoutData( fdGeneralComp );

    wGeneralComp.layout();
    wGeneralTab.setControl( wGeneralComp );
    props.setLook( wGeneralComp );

    // ///////////////////////////////////////////////////////////
    // / END OF GENERAL TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF Files TAB ///
    // ////////////////////////

//    wFilesTab = new CTabItem( wTabFolder, SWT.NONE );
//    wFilesTab.setText( BaseMessages.getString( PKG, "JobOBSPUT.Tab.Files.Label" ) );
//
//    wFilesComp = new Composite( wTabFolder, SWT.NONE );
//    props.setLook( wFilesComp );
//
//    FormLayout FilesLayout = new FormLayout();
//    FilesLayout.marginWidth = 3;
//    FilesLayout.marginHeight = 3;
//    wFilesComp.setLayout( FilesLayout );
//
//    // ////////////////////////
//    // START OF Source files GROUP///
//    // /
//    wSourceFiles = new Group( wFilesComp, SWT.SHADOW_NONE );
//    props.setLook( wSourceFiles );
//    wSourceFiles.setText( BaseMessages.getString( PKG, "JobOBSPUT.SourceFiles.Group.Label" ) );
//    FormLayout SourceFilesgroupLayout = new FormLayout();
//    SourceFilesgroupLayout.marginWidth = 10;
//    SourceFilesgroupLayout.marginHeight = 10;
//    wSourceFiles.setLayout( SourceFilesgroupLayout );
//
//    // Get arguments from previous result...
//    wlgetPrevious = new Label( wSourceFiles, SWT.RIGHT );
//    wlgetPrevious.setText( BaseMessages.getString( PKG, "JobOBSPUT.getPrevious.Label" ) );
//    props.setLook( wlgetPrevious );
//    fdlgetPrevious = new FormData();
//    fdlgetPrevious.left = new FormAttachment( 0, 0 );
//    fdlgetPrevious.top = new FormAttachment( wServerSettings, 2 * margin );
//    fdlgetPrevious.right = new FormAttachment( middle, -margin );
//    wlgetPrevious.setLayoutData( fdlgetPrevious );
//    wgetPrevious = new Button( wSourceFiles, SWT.CHECK );
//    props.setLook( wgetPrevious );
//    wgetPrevious.setToolTipText( BaseMessages.getString( PKG, "JobOBSPUT.getPrevious.Tooltip" ) );
//    fdgetPrevious = new FormData();
//    fdgetPrevious.left = new FormAttachment( middle, 0 );
//    fdgetPrevious.top = new FormAttachment( wServerSettings, 2 * margin );
//    fdgetPrevious.right = new FormAttachment( 100, 0 );
//    wgetPrevious.setLayoutData( fdgetPrevious );
//    wgetPrevious.addSelectionListener( new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent e ) {
//        if ( wgetPrevious.getSelection() ) {
//          wgetPreviousFiles.setSelection( false ); // only one is allowed
//        }
//        activeCopyFromPrevious();
//        jobEntry.setChanged();
//      }
//    } );
//
//    // Get arguments from previous files result...
//    wlgetPreviousFiles = new Label( wSourceFiles, SWT.RIGHT );
//    wlgetPreviousFiles.setText( BaseMessages.getString( PKG, "JobOBSPUT.getPreviousFiles.Label" ) );
//    props.setLook( wlgetPreviousFiles );
//    fdlgetPreviousFiles = new FormData();
//    fdlgetPreviousFiles.left = new FormAttachment( 0, 0 );
//    fdlgetPreviousFiles.top = new FormAttachment( wgetPrevious, 2 * margin );
//    fdlgetPreviousFiles.right = new FormAttachment( middle, -margin );
//    wlgetPreviousFiles.setLayoutData( fdlgetPreviousFiles );
//    wgetPreviousFiles = new Button( wSourceFiles, SWT.CHECK );
//    props.setLook( wgetPreviousFiles );
//    wgetPreviousFiles.setToolTipText( BaseMessages.getString( PKG, "JobOBSPUT.getPreviousFiles.Tooltip" ) );
//    fdgetPreviousFiles = new FormData();
//    fdgetPreviousFiles.left = new FormAttachment( middle, 0 );
//    fdgetPreviousFiles.top = new FormAttachment( wgetPrevious, 2 * margin );
//    fdgetPreviousFiles.right = new FormAttachment( 100, 0 );
//    wgetPreviousFiles.setLayoutData( fdgetPreviousFiles );
//    wgetPreviousFiles.addSelectionListener( new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent e ) {
//        if ( wgetPreviousFiles.getSelection() ) {
//          wgetPrevious.setSelection( false ); // only one is allowed
//        }
//        activeCopyFromPrevious();
//        jobEntry.setChanged();
//      }
//    } );
//
//    // Local Directory line
//    wlLocalDirectory = new Label( wSourceFiles, SWT.RIGHT );
//    wlLocalDirectory.setText( BaseMessages.getString( PKG, "JobOBSPUT.LocalDir.Label" ) );
//    props.setLook( wlLocalDirectory );
//    fdlLocalDirectory = new FormData();
//    fdlLocalDirectory.left = new FormAttachment( 0, 0 );
//    fdlLocalDirectory.top = new FormAttachment( wgetPreviousFiles, margin );
//    fdlLocalDirectory.right = new FormAttachment( middle, -margin );
//    wlLocalDirectory.setLayoutData( fdlLocalDirectory );
//
//    // Browse folders button ...
//    wbLocalDirectory = new Button( wSourceFiles, SWT.PUSH | SWT.CENTER );
//    props.setLook( wbLocalDirectory );
//    wbLocalDirectory.setText( BaseMessages.getString( PKG, "JobOBSPUT.BrowseFolders.Label" ) );
//    fdbLocalDirectory = new FormData();
//    fdbLocalDirectory.right = new FormAttachment( 100, 0 );
//    fdbLocalDirectory.top = new FormAttachment( wgetPreviousFiles, margin );
//    wbLocalDirectory.setLayoutData( fdbLocalDirectory );
//    wbLocalDirectory.addSelectionListener( new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent e ) {
//        DirectoryDialog ddialog = new DirectoryDialog( shell, SWT.OPEN );
//        if ( wLocalDirectory.getText() != null ) {
//          ddialog.setFilterPath( jobMeta.environmentSubstitute( wLocalDirectory.getText() ) );
//        }
//
//        // Calling open() will open and run the dialog.
//        // It will return the selected directory, or
//        // null if user cancels
//        String dir = ddialog.open();
//        if ( dir != null ) {
//          // Set the text box to the new selection
//          wLocalDirectory.setText( dir );
//        }
//
//      }
//    } );
//
//    wLocalDirectory = new TextVar( jobMeta, wSourceFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
//    props.setLook( wLocalDirectory );
//    wLocalDirectory.setToolTipText( BaseMessages.getString( PKG, "JobOBSPUT.LocalDir.Tooltip" ) );
//    wLocalDirectory.addModifyListener( lsMod );
//    fdLocalDirectory = new FormData();
//    fdLocalDirectory.left = new FormAttachment( middle, 0 );
//    fdLocalDirectory.top = new FormAttachment( wgetPreviousFiles, margin );
//    fdLocalDirectory.right = new FormAttachment( wbLocalDirectory, -margin );
//    wLocalDirectory.setLayoutData( fdLocalDirectory );
//
//    // Wildcard line
//    wlWildcard = new Label( wSourceFiles, SWT.RIGHT );
//    wlWildcard.setText( BaseMessages.getString( PKG, "JobOBSPUT.Wildcard.Label" ) );
//    props.setLook( wlWildcard );
//    fdlWildcard = new FormData();
//    fdlWildcard.left = new FormAttachment( 0, 0 );
//    fdlWildcard.top = new FormAttachment( wbLocalDirectory, margin );
//    fdlWildcard.right = new FormAttachment( middle, -margin );
//    wlWildcard.setLayoutData( fdlWildcard );
//    wWildcard = new TextVar( jobMeta, wSourceFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
//    props.setLook( wWildcard );
//    wWildcard.setToolTipText( BaseMessages.getString( PKG, "JobOBSPUT.Wildcard.Tooltip" ) );
//    wWildcard.addModifyListener( lsMod );
//    fdWildcard = new FormData();
//    fdWildcard.left = new FormAttachment( middle, 0 );
//    fdWildcard.top = new FormAttachment( wbLocalDirectory, margin );
//    fdWildcard.right = new FormAttachment( 100, 0 );
//    wWildcard.setLayoutData( fdWildcard );
//
//    // Success when there is no file...
//    wlSuccessWhenNoFile = new Label( wSourceFiles, SWT.RIGHT );
//    wlSuccessWhenNoFile.setText( BaseMessages.getString( PKG, "JobOBSPUT.SuccessWhenNoFile.Label" ) );
//    props.setLook( wlSuccessWhenNoFile );
//    fdlSuccessWhenNoFile = new FormData();
//    fdlSuccessWhenNoFile.left = new FormAttachment( 0, 0 );
//    fdlSuccessWhenNoFile.top = new FormAttachment( wWildcard, margin );
//    fdlSuccessWhenNoFile.right = new FormAttachment( middle, -margin );
//    wlSuccessWhenNoFile.setLayoutData( fdlSuccessWhenNoFile );
//    wSuccessWhenNoFile = new Button( wSourceFiles, SWT.CHECK );
//    props.setLook( wSuccessWhenNoFile );
//    wSuccessWhenNoFile.setToolTipText( BaseMessages.getString( PKG, "JobOBSPUT.SuccessWhenNoFile.Tooltip" ) );
//    fdSuccessWhenNoFile = new FormData();
//    fdSuccessWhenNoFile.left = new FormAttachment( middle, 0 );
//    fdSuccessWhenNoFile.top = new FormAttachment( wWildcard, margin );
//    fdSuccessWhenNoFile.right = new FormAttachment( 100, 0 );
//    wSuccessWhenNoFile.setLayoutData( fdSuccessWhenNoFile );
//    wSuccessWhenNoFile.addSelectionListener( new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent e ) {
//        jobEntry.setChanged();
//      }
//    } );


    // ////////////////////////
    // START OF Target files GROUP///
    // /
//    wTargetFiles = new Group( wFilesComp, SWT.SHADOW_NONE );
//    props.setLook( wTargetFiles );
//    wTargetFiles.setText( BaseMessages.getString( PKG, "JobOBSPUT.TargetFiles.Group.Label" ) );
//    FormLayout TargetFilesgroupLayout = new FormLayout();
//    TargetFilesgroupLayout.marginWidth = 10;
//    TargetFilesgroupLayout.marginHeight = 10;
//    wTargetFiles.setLayout( TargetFilesgroupLayout );
//
//    // FtpDirectory line
//    wlScpDirectory = new Label( wTargetFiles, SWT.RIGHT );
//    wlScpDirectory.setText( BaseMessages.getString( PKG, "JobOBSPUT.RemoteDir.Label" ) );
//    props.setLook( wlScpDirectory );
//    fdlScpDirectory = new FormData();
//    fdlScpDirectory.left = new FormAttachment( 0, 0 );
//    fdlScpDirectory.top = new FormAttachment( wSourceFiles, margin );
//    fdlScpDirectory.right = new FormAttachment( middle, -margin );
//    wlScpDirectory.setLayoutData( fdlScpDirectory );
//
//    // Test remote folder button ...
//    wbTestChangeFolderExists = new Button( wTargetFiles, SWT.PUSH | SWT.CENTER );
//    props.setLook( wbTestChangeFolderExists );
//    wbTestChangeFolderExists.setText( BaseMessages.getString( PKG, "JobOBSPUT.TestFolderExists.Label" ) );
//    fdbTestChangeFolderExists = new FormData();
//    fdbTestChangeFolderExists.right = new FormAttachment( 100, 0 );
//    fdbTestChangeFolderExists.top = new FormAttachment( wSourceFiles, margin );
//    wbTestChangeFolderExists.setLayoutData( fdbTestChangeFolderExists );
//
//    // Target (remote) folder
//    wScpDirectory = new TextVar( jobMeta, wTargetFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
//    props.setLook( wScpDirectory );
//    wScpDirectory.setToolTipText( BaseMessages.getString( PKG, "JobOBSPUT.RemoteDir.Tooltip" ) );
//    wScpDirectory.addModifyListener( lsMod );
//    fdScpDirectory = new FormData();
//    fdScpDirectory.left = new FormAttachment( middle, 0 );
//    fdScpDirectory.top = new FormAttachment( wSourceFiles, margin );
//    fdScpDirectory.right = new FormAttachment( wbTestChangeFolderExists, -margin );
//    wScpDirectory.setLayoutData( fdScpDirectory );
//
//    // CreateRemoteFolder files after retrieval...
//    wlCreateRemoteFolder = new Label( wTargetFiles, SWT.RIGHT );
//    wlCreateRemoteFolder.setText( BaseMessages.getString( PKG, "JobOBSPUT.CreateRemoteFolderFiles.Label" ) );
//    props.setLook( wlCreateRemoteFolder );
//    fdlCreateRemoteFolder = new FormData();
//    fdlCreateRemoteFolder.left = new FormAttachment( 0, 0 );
//    fdlCreateRemoteFolder.top = new FormAttachment( wScpDirectory, margin );
//    fdlCreateRemoteFolder.right = new FormAttachment( middle, -margin );
//    wlCreateRemoteFolder.setLayoutData( fdlCreateRemoteFolder );
//    wCreateRemoteFolder = new Button( wTargetFiles, SWT.CHECK );
//    props.setLook( wCreateRemoteFolder );
//    fdCreateRemoteFolder = new FormData();
//    wCreateRemoteFolder
//      .setToolTipText( BaseMessages.getString( PKG, "JobOBSPUT.CreateRemoteFolderFiles.Tooltip" ) );
//    fdCreateRemoteFolder.left = new FormAttachment( middle, 0 );
//    fdCreateRemoteFolder.top = new FormAttachment( wScpDirectory, margin );
//    fdCreateRemoteFolder.right = new FormAttachment( 100, 0 );
//    wCreateRemoteFolder.setLayoutData( fdCreateRemoteFolder );
//    wCreateRemoteFolder.addSelectionListener( new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent e ) {
//        jobEntry.setChanged();
//      }
//    } );
//
//    fdTargetFiles = new FormData();
//    fdTargetFiles.left = new FormAttachment( 0, margin );
//    fdTargetFiles.top = new FormAttachment( wSourceFiles, margin );
//    fdTargetFiles.right = new FormAttachment( 100, -margin );
//    wTargetFiles.setLayoutData( fdTargetFiles );
    // ///////////////////////////////////////////////////////////
    // / END OF Target files GROUP
    // ///////////////////////////////////////////////////////////

//    fdFilesComp = new FormData();
//    fdFilesComp.left = new FormAttachment( 0, 0 );
//    fdFilesComp.top = new FormAttachment( 0, 0 );
//    fdFilesComp.right = new FormAttachment( 100, 0 );
//    fdFilesComp.bottom = new FormAttachment( 100, 0 );
//    wFilesComp.setLayoutData( fdFilesComp );
//
//    wFilesComp.layout();
//    wFilesTab.setControl( wFilesComp );
//    props.setLook( wFilesComp );

    // ///////////////////////////////////////////////////////////
    // / END OF Files TAB
    // ///////////////////////////////////////////////////////////

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wName, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wCancel }, margin, wTabFolder );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
//    lsTest = new Listener() {
//      public void handleEvent( Event e ) {
//        test();
//      }
//    };
//    lsCheckChangeFolder = new Listener() {
//      public void handleEvent( Event e ) {
//        checkRemoteFolder();
//      }
//    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );
    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wName.addSelectionListener( lsDef );
    wEndPoint.addSelectionListener( lsDef );
    wak.addSelectionListener( lsDef );
    wsk.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );
    wTabFolder.setSelection( 0 );

    getData();
    //activeCopyFromPrevious();
    //activeUseKey();
    //AfterFTPPutActivate();
    BaseStepDialog.setSize( shell );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return jobEntry;
  }

//  private void activeCopyFromPrevious() {
//    boolean enabled = !wgetPrevious.getSelection() && !wgetPreviousFiles.getSelection();
//    wbLocalDirectory.setEnabled( enabled );
//    wbTestChangeFolderExists.setEnabled( enabled );
//  }


  public void dispose() {
    // Close open connections
    //closeFTPConnections();
    WindowProperty winprop = new WindowProperty( shell );
    props.setScreen( winprop );
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wName.setText( Const.nullToEmpty( jobEntry.getName() ) );
    wEndPoint.setText( Const.NVL( jobEntry.getEndPoint(), "" ) );
    wBucketName.setText(JobEntryOBSPUT.getBucketName() );
    wak.setText( Const.NVL( jobEntry.getAk(), "" ) );
    wsk.setText( Const.NVL( jobEntry.getSk(), "" ) );
    wOBSFilePrefix.setText( Const.NVL( jobEntry.getSexFlag(), "none" ) );
    wFileDir.setText( Const.NVL( jobEntry.getFileDir(), "" ) );
    wCounter.setText( Const.NVL( new Integer(jobEntry.getCounter()).toString(), "" ) );
    wStartValue.setText( Const.NVL( new Integer(jobEntry.getStartValue()).toString(), "" ) );
    wMaxValue.setText( Const.NVL( new Integer(jobEntry.getMaxValue()).toString(), "" ) );
    wName.selectAll();
    wName.setFocus();
  }

  private void cancel() {
    jobEntry.setChanged( changed );
    jobEntry = null;
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wName.getText() ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "System.StepJobEntryNameMissing.Title" ) );
      mb.setMessage( BaseMessages.getString( PKG, "System.JobEntryNameMissing.Msg" ) );
      mb.open();
      return;
    }
    jobEntry.setName( wName.getText() );
    jobEntry.setEndPoint( wEndPoint.getText() );
    jobEntry.setBucketName( wBucketName.getText() );
    jobEntry.setAk( wak.getText() );
    jobEntry.setSk( wsk.getText() );
    jobEntry.setSexFlag( wOBSFilePrefix.getText() );
    jobEntry.setFileDir( wFileDir.getText() );
    jobEntry.setCounter( new Integer(wCounter.getText()).intValue() );
    jobEntry.setStartValue( new Integer(wStartValue.getText()).intValue() );
    jobEntry.setMaxValue( new Integer(wMaxValue.getText()).intValue()  );
    dispose();
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return false;
  }

//  private void activeUseKey() {
//    wlKeyFilename.setEnabled( wusePublicKey.getSelection() );
//    wKeyFilename.setEnabled( wusePublicKey.getSelection() );
//    wbKeyFilename.setEnabled( wusePublicKey.getSelection() );
//    wkeyfilePass.setEnabled( wusePublicKey.getSelection() );
//  }
}
