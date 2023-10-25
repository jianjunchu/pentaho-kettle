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

package org.pentaho.di.ui.trans.steps.excelcontentinput;

import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.excelcontentinput.ExcelContentInputField;
import org.pentaho.di.trans.steps.excelcontentinput.ExcelContentInputMeta;
import org.pentaho.di.trans.steps.excelinput.SpreadSheetType;
import org.pentaho.di.ui.core.dialog.*;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class ExcelContentInputDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = ExcelContentInputMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String[] YES_NO_COMBO = new String[] {
    BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG, "System.Combo.Yes" ) };


  private Label wlFilename;
  private Button wbTemplateFilename; // Browse: add template file
  private FormData fdbTemplateFilename;

  private TextVar wFilename;
  private FormData fdlFilename, fdFilename;


  private Label wlFilenamePart;
  private Button wbFileDirectory; // Browse: add template file
  private FormData fdbFileDirectory;
  private TextVar wFilenamePart;
  private FormData fdlFilenamePart, fdFilenamePart;

  private Button wbShowFiles;

  private FormData fdlFilenameField, fdlFilenameInField;
  private FormData fdFilenameField, fdFileNameInField;
  private FormData fdOutputField, fdAdditionalFields, fdAddFileResult, fdXmlConf;
  private Label wlFilenameField, wlFilenameInField;
  private CCombo wFilenameField;
  private Button wFilenameInField;

  private Label wlIgnoreEmptyFile;
  private Button wIgnoreEmptyFile;
  private FormData fdlIgnoreEmptyFile, fdIgnoreEmptyFile;


  private Label wlIncludeSubFolder;
  private Button wIncludeSubFolder;
  private FormData fdlIncludeSubFolder, fdIncludeSubFolder;


  private Label wlFileType;
  //private Button wFileTypeTxt,wFileTypeDoc,wFileTypePDF,wFileTypeAll;
  //private FormData fdlFileType, fdFileType;

  private Label wlTemplateFile;
  private Text wTemplateFile;
  private FormData fdlTemplateFile, fdTemplateFile;

  private Label wlEncoding;
  private CCombo wEncoding;
  private FormData fdlEncoding, fdEncoding;

  private Group wFromFieldGroup;
  private Group wAdditionalFields;

  private ExcelContentInputMeta input;

  private boolean gotEncodings = false;

  private boolean gotPreviousFields = false;

  public static final int[] dateLengths = new int[] { 23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6 };
  private int middle;
  private int margin;

  private ModifyListener lsMod;

  public ExcelContentInputDialog(Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (ExcelContentInputMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "FileContentInputDialog.DialogTitle" ) );

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );
    // ///////////////////////////////
    // START OF Output Field GROUP //
    // ///////////////////////////////
//    wFromFieldGroup = new Group( shell, SWT.SHADOW_NONE );
//    props.setLook(wFromFieldGroup);
//    wFromFieldGroup.setText( BaseMessages.getString( PKG, "FileContentInputDialog.wOutputField.Label" ) );
//
//    FormLayout fromFieldgroupLayout = new FormLayout();
//    fromFieldgroupLayout.marginWidth = 10;
//    fromFieldgroupLayout.marginHeight = 10;
//    wFromFieldGroup.setLayout( fromFieldgroupLayout );

    // Is filename defined in a Field
    wlFilenameInField = new Label(shell, SWT.RIGHT );
    wlFilenameInField.setText( BaseMessages.getString( PKG, "FileContentInputDialog.FilenameInField.Label" ) );
    props.setLook( wlFilenameInField );
    fdlFilenameInField = new FormData();
    fdlFilenameInField.left = new FormAttachment( 0, 0 );
    fdlFilenameInField.top = new FormAttachment( wStepname, margin );
    fdlFilenameInField.right = new FormAttachment( middle, -margin );
    wlFilenameInField.setLayoutData( fdlFilenameInField );

    wFilenameInField = new Button(shell, SWT.CHECK );
    props.setLook( wFilenameInField );
    wFilenameInField.setToolTipText( BaseMessages.getString( PKG, "FileContentInputDialog.FilenameInField.Tooltip" ) );
    fdFileNameInField = new FormData();
    fdFileNameInField.left = new FormAttachment( middle, 0 );
    fdFileNameInField.top = new FormAttachment( wStepname, margin );
    wFilenameInField.setLayoutData( fdFileNameInField );
    SelectionAdapter selectFromFieldAdapter = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        ActiveFromField();
        input.setChanged();
      }
    };
    wFilenameInField.addSelectionListener( selectFromFieldAdapter );

    // If Filename defined in a Field
    wlFilenameField = new Label(shell, SWT.RIGHT );
    wlFilenameField.setText( BaseMessages.getString( PKG, "FileContentInputDialog.FilenameField.Label" ) );
    props.setLook( wlFilenameField );
    fdlFilenameField = new FormData();
    fdlFilenameField.left = new FormAttachment( 0, 0 );
    fdlFilenameField.top = new FormAttachment( wFilenameInField, margin );
    fdlFilenameField.right = new FormAttachment( middle, -margin );
    wlFilenameField.setLayoutData( fdlFilenameField );

    wFilenameField = new CCombo(shell, SWT.BORDER | SWT.READ_ONLY );
    wFilenameField.setEditable( true );
    props.setLook( wFilenameField );
    wFilenameField.addModifyListener( lsMod );
    fdFilenameField = new FormData();
    fdFilenameField.left = new FormAttachment( middle, 0 );
    fdFilenameField.top = new FormAttachment( wFilenameInField, margin );
    fdFilenameField.right = new FormAttachment( 100, 0 );

    wFilenameField.setLayoutData(fdFilenameField);
    wFilenameField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        setDynamicFilenameField();
      }
    } );

    // ///////////////////////////////////////////////////////////
    // / END OF  GROUP
    // ///////////////////////////////////////////////////////////


    // Filename line
    wlFilename = new Label( shell, SWT.RIGHT );
    wlFilename.setText( BaseMessages.getString( PKG, "FileContentInputDialog.Filename.Label" ) );
    props.setLook( wlFilename );
    fdlFilename = new FormData();
    fdlFilename.left = new FormAttachment( 0, 0 );
    fdlFilename.top = new FormAttachment(wFilenameField, margin );
    fdlFilename.right = new FormAttachment( middle, -margin );
    wlFilename.setLayoutData( fdlFilename );

    wbFileDirectory = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook(wbFileDirectory);
    wbFileDirectory.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    wbFileDirectory.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForFileOrDirAndAdd" ) );
    fdbFileDirectory = new FormData();
    fdbFileDirectory.top = new FormAttachment( wFilenameField, margin );
    fdbFileDirectory.right = new FormAttachment( 100, 0 );
    wbFileDirectory.setLayoutData(fdbFileDirectory);

    wFilename = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFilename );
    wFilename.addModifyListener( lsMod );
    fdFilename = new FormData();
    fdFilename.left = new FormAttachment( middle, 0 );
    fdFilename.top = new FormAttachment(wFilenameField, margin );
    fdFilename.right = new FormAttachment( wbFileDirectory, -margin );
    wFilename.setLayoutData( fdFilename );


    wlFilenamePart = new Label( shell, SWT.RIGHT );
    wlFilenamePart.setText( BaseMessages.getString( PKG, "FileContentInputDialog.FileNamePart.Label" ) );
    props.setLook( wlFilenamePart );
    fdlFilenamePart = new FormData();
    fdlFilenamePart.left = new FormAttachment( 0, 0 );
    fdlFilenamePart.top = new FormAttachment( wFilename, margin );
    fdlFilenamePart.right = new FormAttachment( middle, -margin );
    wlFilenamePart.setLayoutData( fdlFilenamePart );
    wFilenamePart = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wFilenamePart.setToolTipText(BaseMessages.getString( PKG, "FileContentInputDialog.FileNamePart.Tooltip" ));
    wFilenamePart.setText("");
    wFilenamePart.setEditable( true );
    props.setLook( wFilenamePart );
    wFilenamePart.addModifyListener( lsMod );
    fdFilenamePart = new FormData();
    fdFilenamePart.left = new FormAttachment( middle, 0 );
    fdFilenamePart.top = new FormAttachment( wFilename, margin );
    fdFilenamePart.right = new FormAttachment( 80, 0 );
    wFilenamePart.setLayoutData( fdFilenamePart );
    wFilenamePart.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        setEncodings();
      }
    } );
//
//
//    //File Type
//    wlFileType = new Label( shell, SWT.RIGHT );
//    wlFileType.setText( BaseMessages.getString( PKG, "FileContentInputDialog.FileType.Label" ) );
//    props.setLook( wlFileType );
//    fdlFileType = new FormData();
//    fdlFileType.left = new FormAttachment( 0, 0 );
//    fdlFileType.top = new FormAttachment( wFilenamePart, margin );
//    fdlFileType.right = new FormAttachment( middle, -margin );
//    wlFileType.setLayoutData( fdlFileType );
//
//    wFileTypeTxt = new Button( shell, SWT.CHECK );
//    props.setLook(wFileTypeTxt);
//    wFileTypeTxt.setText( "txt/csv" );
//    wFileTypeTxt.setToolTipText( BaseMessages.getString( PKG, "FileContentInputDialog.TxtFile.Tooltip" ) );
//    fdFileType = new FormData();
//    fdFileType.left = new FormAttachment( middle, 0 );
//    fdFileType.top = new FormAttachment( wFilenamePart, margin );
//    wFileTypeTxt.setLayoutData( fdFileType );
//    wFileTypeTxt.addSelectionListener(new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent e ) {
//        input.setChanged();
//      }
//    } );
//
//    wFileTypeDoc = new Button( shell, SWT.CHECK );
//    props.setLook(wFileTypeDoc);
//    wFileTypeDoc.setText( "doc/docx" );
//    wFileTypeDoc.setToolTipText( BaseMessages.getString( PKG, "FileContentInputDialog.docFile.Tooltip" ) );
//    fdFileType = new FormData();
//    fdFileType.left = new FormAttachment( wFileTypeTxt, 0 );
//    fdFileType.top = new FormAttachment( wFilenamePart, margin );
//    wFileTypeDoc.setLayoutData( fdFileType );
//    wFileTypeDoc.addSelectionListener(new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent e ) {
//        input.setChanged();
//      }
//    } );
//    wFileTypePDF = new Button( shell, SWT.CHECK );
//    props.setLook(wFileTypePDF);
//    wFileTypePDF.setText( "pdf" );
//    wFileTypePDF.setToolTipText( BaseMessages.getString( PKG, "FileContentInputDialog.pdfFile.Tooltip" ) );
//    fdFileType = new FormData();
//    fdFileType.left = new FormAttachment( wFileTypeDoc, 0 );
//    fdFileType.top = new FormAttachment( wFilenamePart, margin );
//    wFileTypePDF.setLayoutData( fdFileType );
//    wFileTypePDF.addSelectionListener(new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent e ) {
//        input.setChanged();
//      }
//    } );
//


    wbShowFiles = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbShowFiles );
    wbShowFiles.setText( BaseMessages.getString( PKG, "FileContentInputDialog.ShowFiles.Button" ) );
    FormData fdbShowFiles = new FormData();
    fdbShowFiles.top = new FormAttachment( wFilename, 0 );
    fdbShowFiles.left = new FormAttachment( wFilenamePart, 0 );
    fdbShowFiles.right = new FormAttachment( 100, 0 );
    wbShowFiles.setLayoutData( fdbShowFiles );

    wlEncoding = new Label( shell, SWT.RIGHT );
    wlEncoding.setText( BaseMessages.getString( PKG, "FileContentInputDialog.Encoding.Label" ) );
    props.setLook( wlEncoding );
    fdlEncoding = new FormData();
    fdlEncoding.left = new FormAttachment( 0, 0 );
    fdlEncoding.top = new FormAttachment( wbShowFiles, margin );
    fdlEncoding.right = new FormAttachment( middle, -margin );
    wlEncoding.setLayoutData( fdlEncoding );
    wEncoding = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
    wEncoding.setEditable( true );
    props.setLook( wEncoding );
    wEncoding.addModifyListener( lsMod );
    fdEncoding = new FormData();
    fdEncoding.left = new FormAttachment( middle, 0 );
    fdEncoding.top = new FormAttachment( wbShowFiles, margin );
    fdEncoding.right = new FormAttachment( 100, 0 );
    wEncoding.setLayoutData( fdEncoding );
    wEncoding.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        setEncodings();
      }
    } );

    // Ignore Empty File
    wlIgnoreEmptyFile = new Label( shell, SWT.RIGHT );
    wlIgnoreEmptyFile.setText( BaseMessages.getString( PKG, "FileContentInputDialog.IgnoreEmptyFile.Label" ) );
    props.setLook( wlIgnoreEmptyFile );
    fdlIgnoreEmptyFile = new FormData();
    fdlIgnoreEmptyFile.left = new FormAttachment( 0, 0 );
    fdlIgnoreEmptyFile.top = new FormAttachment( wEncoding, margin );
    fdlIgnoreEmptyFile.right = new FormAttachment( middle, -margin );
    wlIgnoreEmptyFile.setLayoutData( fdlIgnoreEmptyFile );
    wIgnoreEmptyFile = new Button( shell, SWT.CHECK );
    props.setLook( wIgnoreEmptyFile );
    wIgnoreEmptyFile.setToolTipText( BaseMessages.getString( PKG, "FileContentInputDialog.IgnoreEmptyFile.Tooltip" ) );
    fdIgnoreEmptyFile = new FormData();
    fdIgnoreEmptyFile.left = new FormAttachment( middle, 0 );
    fdIgnoreEmptyFile.top = new FormAttachment( wEncoding, margin );
    wIgnoreEmptyFile.setLayoutData( fdIgnoreEmptyFile );
    wIgnoreEmptyFile.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

      // Include sub folder
      wlIncludeSubFolder = new Label( shell, SWT.RIGHT );
      wlIncludeSubFolder.setText( BaseMessages.getString( PKG, "FileContentInputDialog.IncludeSubFolder.Label" ) );
      props.setLook( wlIncludeSubFolder );
      fdlIncludeSubFolder = new FormData();
      fdlIncludeSubFolder.left = new FormAttachment( 0, 0 );
      fdlIncludeSubFolder.top = new FormAttachment( wIgnoreEmptyFile, margin );
      fdlIncludeSubFolder.right = new FormAttachment( middle, -margin );
      wlIncludeSubFolder.setLayoutData( fdlIncludeSubFolder );
      wIncludeSubFolder = new Button( shell, SWT.CHECK );
      props.setLook( wIncludeSubFolder );
      wIncludeSubFolder.setToolTipText( BaseMessages.getString( PKG, "FileContentInputDialog.IncludeSubFolder.Tooltip" ) );
      fdIncludeSubFolder = new FormData();
      fdIncludeSubFolder.left = new FormAttachment( middle, 0 );
      fdIncludeSubFolder.top = new FormAttachment( wIgnoreEmptyFile, margin );
      wIncludeSubFolder.setLayoutData( fdIncludeSubFolder );
      wIncludeSubFolder.addSelectionListener( new SelectionAdapter() {
          public void widgetSelected( SelectionEvent e ) {
              input.setChanged();
          }
      } );

    // template file
    wlTemplateFile = new Label( shell, SWT.RIGHT );
    wlTemplateFile.setText( BaseMessages.getString( PKG, "FileContentInputDialog.Template.Label" ) );
    props.setLook(wlTemplateFile);
    fdlTemplateFile = new FormData();
    fdlTemplateFile.left = new FormAttachment( 0, 0 );
    fdlTemplateFile.top = new FormAttachment( wIncludeSubFolder, margin );
    fdlTemplateFile.right = new FormAttachment( middle, -margin );
    wlTemplateFile.setLayoutData(fdlTemplateFile);


    wbTemplateFilename = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook(wbTemplateFilename);
    wbTemplateFilename.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    wbTemplateFilename.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForFileOrDirAndAdd" ) );
    fdbTemplateFilename = new FormData();
    fdbTemplateFilename.top = new FormAttachment( wIncludeSubFolder, margin );
    fdbTemplateFilename.right = new FormAttachment( 100, 0 );
    wbTemplateFilename.setLayoutData(fdbTemplateFilename);

    wTemplateFile = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook(wTemplateFile);
    wTemplateFile.addModifyListener( lsMod );
    fdTemplateFile = new FormData();
    fdTemplateFile.left = new FormAttachment( middle, 0 );
    fdTemplateFile.top = new FormAttachment( wIncludeSubFolder, margin );
    fdTemplateFile.right = new FormAttachment(wbTemplateFilename, 0 );
    wTemplateFile.setLayoutData(fdTemplateFile);




    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "FileContentInputDialog.Button.PreviewRows" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wPreview, wCancel }, margin, wTemplateFile);

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsGet = new Listener() {
      public void handleEvent( Event e ) {
        get();
      }
    };
    lsPreview = new Listener() {
      public void handleEvent( Event e ) {
        preview();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    //wGet.addListener( SWT.Selection, lsGet );
    wPreview.addListener( SWT.Selection, lsPreview );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wTemplateFile.addSelectionListener( lsDef );
 //   wInclRownumField.addSelectionListener( lsDef );
    //wInclFilenameField.addSelectionListener( lsDef );

    // Add the file to the list of files...
//    SelectionAdapter selA = new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent arg0 ) {
//        wFilenameList.add( new String[] {
//          wFilename.getText(), wFilemask.getText(), wExcludeFilemask.getText(),
//          FileContentInputMeta.RequiredFilesCode[0], FileContentInputMeta.RequiredFilesCode[0] } );
//        wFilename.setText( "" );
////        wFilemask.setText( "" );
////        wExcludeFilemask.setText( "" );
//        wFilenameList.removeEmptyRows();
//        wFilenameList.setRowNums();
//        wFilenameList.optWidth( true );
//      }
//    };
//    wbaFilename.addSelectionListener( selA );
//    wFilename.addSelectionListener( selA );

    // Delete files from the list of files...
//    wbdFilename.addSelectionListener( new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent arg0 ) {
//        int[] idx = wFilenameList.getSelectionIndices();
//        wFilenameList.remove( idx );
//        wFilenameList.removeEmptyRows();
//        wFilenameList.setRowNums();
//      }
//    } );

    // Edit the selected file & remove from the list...
//    wbeFilename.addSelectionListener( new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent arg0 ) {
//        int idx = wFilenameList.getSelectionIndex();
//        if ( idx >= 0 ) {
//          String[] string = wFilenameList.getItem( idx );
//          wFilename.setText( string[0] );
//          wFilemask.setText( string[1] );
//          wExcludeFilemask.setText( string[2] );
//          wFilenameList.remove( idx );
//        }
//        wFilenameList.removeEmptyRows();
//        wFilenameList.setRowNums();
//      }
//    } );

    // Show the files that are selected at this time...
    wbShowFiles.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        try {
          ExcelContentInputMeta tfii = new ExcelContentInputMeta();
          getInfo( tfii );
          FileInputList fileInputList = tfii.getFiles( transMeta );
          String[] files = fileInputList.getFileStrings();
          if ( files != null && files.length > 0 ) {
            EnterSelectionDialog esd = new EnterSelectionDialog( shell, files,
              BaseMessages.getString( PKG, "FileContentInputDialog.FilesReadSelection.DialogTitle" ),
              BaseMessages.getString( PKG, "FileContentInputDialog.FilesReadSelection.DialogMessage" ) );
            esd.setViewOnly();
            esd.open();
          } else {
            MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage( BaseMessages.getString( PKG, "FileContentInputDialog.NoFileFound.DialogMessage" ) );
            mb.setText( BaseMessages.getString( PKG, "System.Dialog.Error.Title" ) );
            mb.open();
          }
        }
        catch ( FileSystemException ex ) {
          new ErrorDialog(
                  shell, BaseMessages.getString( PKG, "FileContentInputDialog.ErrorParsingData.DialogTitle" ),
                  BaseMessages.getString( PKG, "FileContentInputDialog.ErrorParsingData.DialogMessage" ), ex );
        }
        catch ( Exception ex ) {
          new ErrorDialog(
            shell, BaseMessages.getString( PKG, "FileContentInputDialog.ErrorParsingData.DialogTitle" ),
            BaseMessages.getString( PKG, "FileContentInputDialog.ErrorParsingData.DialogMessage" ), ex );
        }
      }
    } );


    // Listen to the Browse... button
    wbTemplateFilename.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
          FileDialog dialog = new FileDialog( shell, SWT.OPEN );
          String[] extentions = new String[] { "*.xls;*.XLS;*.xlsx;*.XLSX", "*" };
          dialog.setFilterExtensions( extentions );
          if ( wTemplateFile.getText() != null ) {
            String fname = transMeta.environmentSubstitute( wFilename.getText() );
            dialog.setFileName( fname );
          }
          dialog.setFilterNames( new String[] {
                  BaseMessages.getString( PKG, "ExcelInputDialog.FilterNames.ExcelFiles" ),
                  BaseMessages.getString( PKG, "System.FileType.AllFiles" ) } );

          if ( dialog.open() != null ) {
            String str = dialog.getFilterPath() + System.getProperty( "file.separator" ) + dialog.getFileName();
            wTemplateFile.setText( str );
          }
      }
    } );


    // Listen to the Browse button to browse directory
    wbFileDirectory.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        DirectoryDialog dialog = new DirectoryDialog( shell, SWT.OPEN );
        if ( wbFileDirectory.getText() != null ) {
          String fpath = transMeta.environmentSubstitute( wFilename.getText() );
          dialog.setFilterPath( fpath );
        }
        if ( dialog.open() != null ) {
          String str = dialog.getFilterPath();
          wFilename.setText( str );
        }
      }

    } );


    // Enable/disable the right fields to allow a filename to be added to each row...
//    wInclFilename.addSelectionListener( new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent e ) {
//        //setIncludeFilename();
//        input.setChanged();
//      }
//    } );

    // Enable/disable the right fields to allow a row number to be added to each row...
//    wInclRownum.addSelectionListener( new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent e ) {
//       // setIncludeRownum();
//        input.setChanged();
//      }
//    } );

    // Whenever something changes, set the tooltip to the expanded version of the filename:
    wFilename.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        wFilename.setToolTipText( wFilename.getText() );
      }
    } );

    // Listen to the Browse... button
//    wbbFilename.addSelectionListener( new SelectionAdapter() {
//      public void widgetSelected( SelectionEvent e ) {
//        if ( !Utils.isEmpty( wFilemask.getText() ) || !Utils.isEmpty( wExcludeFilemask.getText() ) ) { // A mask: a directory!
//          DirectoryDialog dialog = new DirectoryDialog( shell, SWT.OPEN );
//          if ( wFilename.getText() != null ) {
//            String fpath = transMeta.environmentSubstitute( wFilename.getText() );
//            dialog.setFilterPath( fpath );
//          }
//
//          if ( dialog.open() != null ) {
//            String str = dialog.getFilterPath();
//            wFilename.setText( str );
//          }
//        } else {
//          FileDialog dialog = new FileDialog( shell, SWT.OPEN );
//
//          dialog.setFilterExtensions( new String[] { "*.txt;", "*.csv", "*.TRT", "*" } );
//
//          if ( wFilename.getText() != null ) {
//            String fname = transMeta.environmentSubstitute( wFilename.getText() );
//            dialog.setFileName( fname );
//          }
//
//          dialog.setFilterNames( new String[] {
//            BaseMessages.getString( PKG, "System.FileType.TextFiles" ),
//            BaseMessages.getString( PKG, "FileContentInputDialog.FileType.TextAndCSVFiles" ),
//            BaseMessages.getString( PKG, "LoadFileInput.FileType.TRTFiles" ),
//            BaseMessages.getString( PKG, "System.FileType.AllFiles" ) } );
//
//          if ( dialog.open() != null ) {
//            String str = dialog.getFilterPath() + System.getProperty( "file.separator" ) + dialog.getFileName();
//            wFilename.setText( str );
//          }
//        }
//      }
//    } );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    //wTabFolder.setSelection( 0 );

    // Set the shell size, based upon previous time...
    setSize();
    getData( input );
    ActiveFromField();
    input.setChanged( changed );
    //wFields.optWidth( true );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void setDynamicFilenameField() {
    if ( !gotPreviousFields ) {
      try {
        String field = wFilenameField.getText();
        wFilenameField.removeAll();
        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
        if ( r != null ) {
          wFilenameField.setItems( r.getFieldNames() );
        }
        if ( field != null ) {
          wFilenameField.setText( field );
        }
      } catch ( KettleException ke ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "FileContentInputDialog.FailedToGetFields.DialogTitle" ),
          BaseMessages.getString( PKG, "FileContentInputDialog.FailedToGetFields.DialogMessage" ), ke );
      }
      gotPreviousFields = true;
    }
  }

  private void ActiveFromField() {
    wlFilenameField.setEnabled( wFilenameInField.getSelection() );
    wFilenameField.setEnabled( wFilenameInField.getSelection() );
    wlFilename.setEnabled( !wFilenameInField.getSelection() );
    wFilename.setEnabled( !wFilenameInField.getSelection() );

//    wbbFilename.setEnabled( !wFilenameInField.getSelection() );
//    wbaFilename.setEnabled( !wFilenameInField.getSelection() );
//    wbShowFiles.setEnabled( !wFilenameInField.getSelection() );
//    wlFilenameList.setEnabled( !wFilenameInField.getSelection() );
//   wFilenameList.setEnabled( !wFilenameInField.getSelection() );
//    wInclFilename.setEnabled( !wFilenameInField.getSelection() );
//    wlInclFilename.setEnabled( !wFilenameInField.getSelection() );

//    if ( wFilenameInField.getSelection() ) {
//      wInclFilename.setSelection( false );
//      wlInclFilenameField.setEnabled( false );
//      wInclFilenameField.setEnabled( false );
//    } else {
//      wlInclFilenameField.setEnabled( wInclFilename.getSelection() );
//      wInclFilenameField.setEnabled( wInclFilename.getSelection() );
//    }
    //wAddResult.setEnabled( !wFilenameInField.getSelection() );
    //wlAddResult.setEnabled( !wFilenameInField.getSelection() );
  //  wLimit.setEnabled( !wFilenameInField.getSelection() );
  //  wPreview.setEnabled( !wFilenameInField.getSelection() );
  }

  private void get() {
    int clearFields = SWT.NO;
  //  int nrInputFields = wFields.nrNonEmpty();
//
//    if ( nrInputFields > 0 ) {
//      MessageBox mb = new MessageBox( shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION );
//      mb.setMessage( BaseMessages.getString( PKG, "FileContentInputDialog.ClearFieldList.DialogMessage" ) );
//      mb.setText( BaseMessages.getString( PKG, "FileContentInputDialog.ClearFieldList.DialogTitle" ) );
//      clearFields = mb.open();
//    }

//    if ( clearFields == SWT.YES ) {
//      // Clear Fields Grid
//      wFields.table.removeAll();
//    }

//    TableItem item = new TableItem( wFields.table, SWT.NONE );
//    item.setText( 1, ExcelContentInputField.ElementTypeDesc[0] );
//    item.setText( 2, ExcelContentInputField.ElementTypeDesc[0] );
//    item.setText( 3, "String" );
//    // file size
//    item = new TableItem( wFields.table, SWT.NONE );
//    item.setText( 1, ExcelContentInputField.ElementTypeDesc[1] );
//    item.setText( 2, ExcelContentInputField.ElementTypeDesc[1] );
//    item.setText( 3, "Integer" );

//    wFields.removeEmptyRows();
//    wFields.setRowNums();
//    wFields.optWidth( true );
  }

  private void setEncodings() {
    // Encoding of the text file:
    if ( !gotEncodings ) {
      gotEncodings = true;
      String encoding = wEncoding.getText();
      wEncoding.removeAll();
      ArrayList<Charset> values = new ArrayList<Charset>( Charset.availableCharsets().values() );
      for ( int i = 0; i < values.size(); i++ ) {
        Charset charSet = values.get( i );
        wEncoding.add( charSet.displayName() );
      }

      if ( !Utils.isEmpty( encoding ) ) {
        wEncoding.setText( encoding );
      } /*
         * else { // Now select the default! String defEncoding = Const.getEnvironmentVariable("file.encoding",
         * "UTF-8"); int idx = Const.indexOfString(defEncoding, wEncoding.getItems() ); if (idx>=0) wEncoding.select(
         * idx ); }
         */
    }
  }

//  public void setIncludeFilename() {
//    wlInclFilenameField.setEnabled( wInclFilename.getSelection() );
//    wInclFilenameField.setEnabled( wInclFilename.getSelection() );
//  }

//  public void setIncludeRownum() {
//    //wlInclRownumField.setEnabled( wInclRownum.getSelection() );
//    //wInclRownumField.setEnabled( wInclRownum.getSelection() );
//  }

  /**
   * Read the data from the TextFileInputMeta object and show it in this dialog.
   *
   * @param in
   *          The TextFileInputMeta object to obtain the data from.
   */
  public void getData( ExcelContentInputMeta in ) {
      if ( in.getFileName() != null ) {
        //wFilenameList.removeAll();

    //      for ( int i = 0; i < in.getFileName().length; i++ ) {
    //        wFilenameList.add( new String[] {
    //          in.getFileName()[i], in.getFileMask()[i], in.getExludeFileMask()[i],
    //          in.getRequiredFilesDesc( in.getFileRequired()[i] ),
    //          in.getRequiredFilesDesc( in.getIncludeSubFolders()[i] ) } );
    //      }

    //      wFilenameList.removeEmptyRows();
    //      wFilenameList.setRowNums();
    //      wFilenameList.optWidth( true );
      }
      //wInclFilename.setSelection( in.includeFilename() );
    //    wInclRownum.setSelection( in.includeRowNumber() );
    //    wAddResult.setSelection( in.addResultFile() );
      wFilenameInField.setSelection( in.getFileInFields() );
      wFilenameField.setText( Utils.isEmpty(in.getFilenameField())?"":in.getFilenameField() );
      wFilename.setText(Utils.isEmpty(in.getFileName())?"":in.getFileName());
      wFilenamePart.setText(Utils.isEmpty(in.getFilenamePart())?"":in.getFilenamePart());
      //SpreadSheetType fileTypes = in.getFileType();
//      if(fileTypes!=null && fileTypes.name().indexOf("poi")>-1)
//        this.wFileTypeDoc.setSelection(true);
//      if(fileTypes!=null && fileTypes.name().indexOf("jxl")>-1)
//        this.wFileTypeTxt.setSelection(true);
//      if(fileTypes!=null && fileTypes.name().indexOf("sax")>-1)
//        this.wFileTypePDF.setSelection(true);
      wEncoding.setText(Utils.isEmpty(in.getEncoding())?"UTF-8":in.getEncoding());
      wIgnoreEmptyFile.setSelection( in.isIgnoreEmptyFile() );
      wIncludeSubFolder.setSelection(in.getIncludeSubFolders());
      wTemplateFile.setText( "" + in.getTemplateFile() );
      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "FileContentInputDialog.Log.GettingFieldsInfo" ) );
      }
      wStepname.selectAll();
      wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void ok() {
    try {
      getInfo( input );
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "FileContentInputDialog.ErrorParsingData.DialogTitle" ), BaseMessages
          .getString( PKG, "FileContentInputDialog.ErrorParsingData.DialogMessage" ), e );
    }
    dispose();
  }

  private void getInfo( ExcelContentInputMeta in ) throws KettleException {
    stepname = wStepname.getText(); // return value

    // copy info to TextFileInputMeta class (input)
    in.setTemplateFile( wTemplateFile.getText() );
    in.setEncoding( wEncoding.getText() );
    in.setIgnoreEmptyFile( wIgnoreEmptyFile.getSelection() );
    in.setFileInFields( this.wFilenameInField.getSelection() );
    in.setFilenameField( this.wFilenameField.getText() );
    in.setFilenamePart( this.wFilenamePart.getText());
    in.setIncludeSubFolders(wIncludeSubFolder.getSelection());
    //in.setFileType(SpreadSheetType.valueOf(getFileTypeSelection()));
    in.setFileInFields(wFilenameInField.getSelection());
//    in.setIsInFields( wFilenameInField.getSelection() );
//    in.setDynamicFilenameField( wFilenameField.getText() );
    int nrFields = 4;
    if ( wFilenameInField.getSelection() ) {
   //   in.allocate( 0, nrFields );
      //in.setFileInFields();
      in.setFileName( wFilenameField.getText() );
    } else {
        in.setFileName( wFilename.getText() );
//      in.setExcludeFileMask( wFilenameList.getItems( 2 ) );
    }
    //for ( int i = 0; i < nrFields; i++ ) {
    ExcelContentInputField contentField = new ExcelContentInputField();
    contentField.setName( "content" );
    contentField.setType( ValueMetaFactory.getIdForValueMeta( "String" ) );
    in.getInputFields()[0] = contentField;

    ExcelContentInputField sizeField = new ExcelContentInputField();
    contentField.setName( "size" );
    contentField.setType( ValueMetaFactory.getIdForValueMeta( "Integer" ) );
    in.getInputFields()[1] = sizeField;

    ExcelContentInputField filenameField = new ExcelContentInputField();
    contentField.setName( "filename" );
    contentField.setType( ValueMetaFactory.getIdForValueMeta( "String" ) );
    in.getInputFields()[2] = filenameField;

    ExcelContentInputField shortFilenameField = new ExcelContentInputField();
    contentField.setName( "shortfilename" );
    contentField.setType( ValueMetaFactory.getIdForValueMeta( "String" ) );
    in.getInputFields()[3] = shortFilenameField;

//      field.setElementType( ExcelContentInputField.getElementTypeByDesc( item.getText( 2 ) ) );
//      field.setFormat( item.getText( 4 ) );
//      field.setLength( Const.toInt( item.getText( 5 ), -1 ) );
//      field.setPrecision( Const.toInt( item.getText( 6 ), -1 ) );
//      field.setCurrencySymbol( item.getText( 7 ) );
//      field.setDecimalSymbol( item.getText( 8 ) );
//      field.setGroupSymbol( item.getText( 9 ) );
//      field.setTrimType( ExcelContentInputField.getTrimTypeByDesc( item.getText( 10 ) ) );
//      field.setRepeated( BaseMessages.getString( PKG, "System.Combo.Yes" ).equalsIgnoreCase( item.getText( 11 ) ) );
//      //CHECKSTYLE:Indentation:OFF
    //}
//    in.setShortFileNameField( wShortFileFieldName.getText() );
//    in.setPathField( wPathFieldName.getText() );
//    in.setIsHiddenField( wIsHiddenName.getText() );
//    in.setLastModificationDateField( wLastModificationTimeName.getText() );
//    in.setUriField( wUriName.getText() );
//    in.setRootUriField( wRootUriName.getText() );
//    in.setExtensionField( wExtensionFieldName.getText() );
  }

//    private String getFileTypeSelection() {
//      String result="";
//        if(wFileTypeTxt.getSelection())
//            result+=wFileTypeTxt.getText()+";";
//        if(this.wFileTypeDoc.getSelection())
//            result+=wFileTypeDoc.getText()+";";
//        if(this.wFileTypePDF.getSelection())
//            result+=wFileTypePDF.getText();
//        return result;
//    }

    // Preview the data
  private void preview() {
    try {
      // Create the XML input step
      ExcelContentInputMeta oneMeta = new ExcelContentInputMeta();
      getInfo( oneMeta );

      TransMeta previewMeta =
        TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );

      EnterNumberDialog numberDialog = new EnterNumberDialog( shell, props.getDefaultPreviewSize(),
        BaseMessages.getString( PKG, "FileContentInputDialog.NumberRows.DialogTitle" ),
        BaseMessages.getString( PKG, "FileContentInputDialog.NumberRows.DialogMessage" ) );

      int previewSize = numberDialog.open();
      if ( previewSize > 0 ) {
        TransPreviewProgressDialog progressDialog =
          new TransPreviewProgressDialog(
            shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
        progressDialog.open();

        if ( !progressDialog.isCancelled() ) {
          Trans trans = progressDialog.getTrans();
          String loggingText = progressDialog.getLoggingText();

          if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
            EnterTextDialog etd =
              new EnterTextDialog(
                shell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ), BaseMessages
                  .getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
            etd.setReadOnly();
            etd.open();
          }

          PreviewRowsDialog prd =
            new PreviewRowsDialog(
              shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta( wStepname
                .getText() ), progressDialog.getPreviewRows( wStepname.getText() ), loggingText );
          prd.open();

        }
      }
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "FileContentInputDialog.ErrorPreviewingData.DialogTitle" ),
        BaseMessages.getString( PKG, "FileContentInputDialog.ErrorPreviewingData.DialogMessage" ), e );
    }
  }
}
