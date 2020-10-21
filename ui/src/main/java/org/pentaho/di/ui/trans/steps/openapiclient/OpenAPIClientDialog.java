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

package org.pentaho.di.ui.trans.steps.openapiclient;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.openapiclient.OpenAPIClientMeta;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class OpenAPIClientDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = OpenAPIClientMeta.class; // for i18n purposes, needed by Translator2!!

  private Label wlAPIServer;
  private Text wAPIServer;

  private Label wlMethodName;
  private ComboVar wMethodName;

  private Label wlAccessKey;
  private TextVar wAccessKey;

  private Label wlSecret;
  private TextVar wSecret;

  private Label wluid;
  private TextVar wuid;

  private Label wlBindId;
  private TextVar wBindId;

  private Label wlBoName;
  private TextVar wBoName;

  private OpenAPIClientMeta input;

  public OpenAPIClientDialog(Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (OpenAPIClientMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "OpenAPIClientDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "OpenAPIClientDialog.StepName.Label" ) );
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

    // Valuename line
    wlAPIServer = new Label( shell, SWT.RIGHT );
    wlAPIServer.setText( BaseMessages.getString( PKG, "OpenAPIClientDialog.APIServer.Label" ) );
    props.setLook( wlAPIServer );
    FormData fdlValuename = new FormData();
    fdlValuename.left = new FormAttachment( 0, 0 );
    fdlValuename.top = new FormAttachment( wStepname, margin );
    fdlValuename.right = new FormAttachment( middle, -margin );
    wlAPIServer.setLayoutData( fdlValuename );
    wAPIServer = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wAPIServer.setText( "" );
    props.setLook( wAPIServer );
    wAPIServer.addModifyListener( lsMod );
    FormData fdValuename = new FormData();
    fdValuename.left = new FormAttachment( middle, 0 );
    fdValuename.top = new FormAttachment( wStepname, margin );
    fdValuename.right = new FormAttachment( 100, 0 );
    wAPIServer.setLayoutData( fdValuename );

    // Connection line
    //
    wlMethodName = new Label( shell, SWT.RIGHT );
    wlMethodName.setText( BaseMessages.getString( PKG, "OpenAPIClientDialog.Method.Label" ) );
    props.setLook( wlMethodName );
    FormData fdlSlaveServer = new FormData();
    fdlSlaveServer.left = new FormAttachment( 0, 0 );
    fdlSlaveServer.top = new FormAttachment( wAPIServer, margin );
    fdlSlaveServer.right = new FormAttachment( middle, -margin );
    wlMethodName.setLayoutData( fdlSlaveServer );
    wMethodName = new ComboVar( transMeta, shell, SWT.LEFT | SWT.SINGLE | SWT.BORDER );
    wMethodName.setItems( new String[]{"bo.creates","bo.datas.create"} );
    FormData fdSlaveServer = new FormData();
    fdSlaveServer.left = new FormAttachment( middle, 0 );
    fdSlaveServer.top = new FormAttachment( wAPIServer, margin );
    fdSlaveServer.right = new FormAttachment( 100, 0 );
    wMethodName.setLayoutData( fdSlaveServer );

    // Seqname line
    wlAccessKey = new Label( shell, SWT.RIGHT );
    wlAccessKey.setText( BaseMessages.getString( PKG, "OpenAPIClientDialog.AccessKey.Label" ) );
    props.setLook( wlAccessKey );
    FormData fdlAccessKey = new FormData();
    fdlAccessKey.left = new FormAttachment( 0, 0 );
    fdlAccessKey.right = new FormAttachment( middle, -margin );
    fdlAccessKey.top = new FormAttachment( wMethodName, margin );
    wlAccessKey.setLayoutData( fdlAccessKey );

    wAccessKey = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wAccessKey.setText( "" );
    props.setLook( wAccessKey );
    wAccessKey.addModifyListener( lsMod );
    FormData fdSeqname = new FormData();
    fdSeqname.left = new FormAttachment( middle, 0 );
    fdSeqname.top = new FormAttachment( wMethodName, margin );
    fdSeqname.right = new FormAttachment( 100, 0 );
    wAccessKey.setLayoutData( fdSeqname );

    // Secret
    wlSecret = new Label( shell, SWT.RIGHT );
    wlSecret.setText( BaseMessages.getString( PKG, "OpenAPIClientDialog.Secret.Label" ) );
    props.setLook( wlSecret );
    FormData fdlSecret = new FormData();
    fdlSecret.left = new FormAttachment( 0, 0 );
    fdlSecret.right = new FormAttachment( middle, -margin );
    fdlSecret.top = new FormAttachment( wAccessKey, margin );
    wlSecret.setLayoutData( fdlSecret );

    wSecret = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wSecret.setText( "" );
    props.setLook( wSecret );
    wSecret.addModifyListener( lsMod );
    FormData fdIncrement = new FormData();
    fdIncrement.left = new FormAttachment( middle, 0 );
    fdIncrement.top = new FormAttachment( wAccessKey, margin );
    fdIncrement.right = new FormAttachment( 100, 0 );
    wSecret.setLayoutData( fdIncrement );

    // uid
    wluid = new Label( shell, SWT.RIGHT );
    wluid.setText( BaseMessages.getString( PKG, "OpenAPIClientDialog.uid.Label" ) );
    props.setLook( wluid );
    FormData fdluid = new FormData();
    fdluid.left = new FormAttachment( 0, 0 );
    fdluid.right = new FormAttachment( middle, -margin );
    fdluid.top = new FormAttachment( wSecret, margin );
    wluid.setLayoutData( fdluid );

    wuid = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wuid.setText( "" );
    props.setLook( wuid );
    wuid.addModifyListener( lsMod );
    FormData fduid = new FormData();
    fduid.left = new FormAttachment( middle, 0 );
    fduid.top = new FormAttachment( wSecret, margin );
    fduid.right = new FormAttachment( 100, 0 );
    wuid.setLayoutData( fduid );

    // bindid
    wlBindId = new Label( shell, SWT.RIGHT );
    wlBindId.setText( BaseMessages.getString( PKG, "OpenAPIClientDialog.bindId.Label" ) );
    props.setLook( wlBindId );
    FormData fdlBindid = new FormData();
    fdlBindid.left = new FormAttachment( 0, 0 );
    fdlBindid.right = new FormAttachment( middle, -margin );
    fdlBindid.top = new FormAttachment( wuid, margin );
    wlBindId.setLayoutData( fdlBindid );

    wBindId = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wBindId.setText( "" );
    props.setLook( wBindId );
    wBindId.addModifyListener( lsMod );
    FormData fdBindId = new FormData();
    fdBindId.left = new FormAttachment( middle, 0 );
    fdBindId.top = new FormAttachment( wuid, margin );
    fdBindId.right = new FormAttachment( 100, 0 );
    wBindId.setLayoutData( fdBindId );


    // bindid
    wlBoName = new Label( shell, SWT.RIGHT );
    wlBoName.setText( BaseMessages.getString( PKG, "OpenAPIClientDialog.boName.Label" ) );
    props.setLook( wlBoName );
    FormData fdlBoName = new FormData();
    fdlBoName.left = new FormAttachment( 0, 0 );
    fdlBoName.right = new FormAttachment( middle, -margin );
    fdlBoName.top = new FormAttachment( wBindId, margin );
    wlBoName.setLayoutData( fdlBoName );

    wBoName = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wBoName.setText( "" );
    props.setLook( wBoName );
    wBoName.addModifyListener( lsMod );
    FormData fdBoName = new FormData();
    fdBoName.left = new FormAttachment( middle, 0 );
    fdBoName.top = new FormAttachment( wBindId, margin );
    fdBoName.right = new FormAttachment( 100, 0 );
    wBoName.setLayoutData( fdBoName );


    // THE BUTTONS
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, wBoName );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wAPIServer.addSelectionListener( lsDef );
    wAccessKey.addSelectionListener( lsDef );
    wSecret.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    logDebug( BaseMessages.getString( PKG, "OpenAPIClientDialog.Log.GettingKeyInfo" ) );

    wAPIServer.setText( Const.NVL( input.getAPIServer(), "" ) );
    wMethodName.setText( Const.NVL( input.getMethodName(), "" ) );
    wAccessKey.setText( Const.NVL( input.getAccessKey(), "" ) );
    wSecret.setText( Const.NVL( input.getSecret(), "" ) );
    wuid.setText( Const.NVL( input.getuid(), "" ) );
    wBindId.setText( Const.NVL( input.getbindId(), "" ) );
    wBoName.setText( Const.NVL( input.getBoName(), "" ) );
    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    stepname = wStepname.getText(); // return value

    input.setMethodName( wMethodName.getText() );
    input.setAPIServer( wAPIServer.getText() );
    input.setAccessKey( wAccessKey.getText() );
    input.setSecret( wSecret.getText() );
    input.setuid( wuid.getText() );
    input.setbindId( wBindId.getText() );
    input.setBoName(wBoName.getText());
    dispose();
  }

}
