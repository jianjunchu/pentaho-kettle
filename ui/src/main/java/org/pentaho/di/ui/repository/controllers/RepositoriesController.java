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

package org.pentaho.di.ui.repository.controllers;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.repository.ILoginCallback;
import org.pentaho.di.ui.repository.RepositoriesHelper;
import org.pentaho.di.ui.repository.dialog.RepositoryDialogInterface;
import org.pentaho.di.ui.repository.model.RepositoriesModel;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.xul.KettleWaitBox;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.*;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class RepositoriesController extends AbstractXulEventHandler {

  private static Class<?> PKG = RepositoryDialogInterface.class; // for i18n purposes, needed by Translator2!!

  private ResourceBundle messages;

  private BindingFactory bf;

  private XulDialog loginDialog;

  private XulTextbox username;

  private XulTextbox userPassword;

  private XulListbox availableRepositories;

  //auphi
  private XulLabel   usernameLabel;
  private XulLabel   userPasswordLabel;
  private XulLabel   connectTipLabel;
  private XulTextbox repositoryUrl;
  private XulLabel   repLabel;
  // auphi end

  private XulButton repositoryEditButton;

  private XulButton repositoryRemoveButton;

  private XulCheckbox showAtStartup;

  private RepositoriesModel loginModel;

  private XulButton okButton;

  private XulButton cancelButton;

  private XulMessageBox messageBox;

  protected XulConfirmBox confirmBox;

  private RepositoriesHelper helper;
  private String preferredRepositoryName;
  private ILoginCallback callback;

  private Shell shell;

  public RepositoriesController() {
    super();
    loginModel = new RepositoriesModel();
  }

  public void init() throws ControllerInitializationException {
    // TODO Initialize the Repository Login Dialog
    try {
      messageBox = (XulMessageBox) document.createElement( "messagebox" );
      confirmBox = (XulConfirmBox) document.createElement( "confirmbox" );
    } catch ( Exception e ) {
      throw new ControllerInitializationException( e );
    }
    if ( bf != null ) {
      createBindings();
    }
  }


  private void createBindings() {
    loginDialog = (XulDialog) document.getElementById("repository-login-dialog");//$NON-NLS-1$

    repositoryUrl = (XulTextbox) document.getElementById("repository-url");
    repLabel=(XulLabel) document.getElementById("repository-url-lab");

    username = (XulTextbox) document.getElementById("user-name");
    usernameLabel = (XulLabel) document.getElementById("user-name-lab");//$NON-NLS-1$

    userPassword = (XulTextbox) document.getElementById("user-password");//$NON-NLS-1$
    userPasswordLabel = (XulLabel) document.getElementById("user-password-lab");

    connectTipLabel =(XulLabel) document.getElementById("connect-tip");

    availableRepositories = (XulListbox) document.getElementById("available-repository-list");//$NON-NLS-1$

    okButton = (XulButton) document.getElementById("repository-login-dialog_accept"); //$NON-NLS-1$
    cancelButton = (XulButton) document.getElementById("repository-login-dialog_cancel"); //$NON-NLS-1$
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(loginModel, "repositoryUrl", repositoryUrl, "value");
    bf.createBinding(loginModel, "username", username, "value");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(loginModel, "password", userPassword, "value");//$NON-NLS-1$ //$NON-NLS-2$

    bf.createBinding(loginModel, "availableRepositories", availableRepositories, "elements");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(loginModel, "selectedRepository", availableRepositories, "selectedItem");//$NON-NLS-1$ //$NON-NLS-2$
//    bf.createBinding(loginModel, "showDialogAtStartup", showAtStartup, "checked");//$NON-NLS-1$ //$NON-NLS-2$
    bf.setBindingType(Binding.Type.ONE_WAY);
//   bf.createBinding(loginModel, "valid", okButton, "disabled");//$NON-NLS-1$ //$NON-NLS-2$
//   availableRepositories.setVisible(false);
//   connectTipLabel.setVisible(false);
    BindingConvertor<RepositoryMeta, Boolean> buttonConverter = new BindingConvertor<RepositoryMeta, Boolean>() {
      @Override
      public Boolean sourceToTarget(RepositoryMeta value) {
        return (value == null);
      }
      @Override
      public RepositoryMeta targetToSource(Boolean value) {
        return null;
      }
    };

    BindingConvertor<RepositoryMeta, Boolean> userpassConverter = new BindingConvertor<RepositoryMeta, Boolean>() {
      @Override
      public Boolean sourceToTarget(RepositoryMeta value) {
        return (value == null) || !value.getRepositoryCapabilities().supportsUsers();
      }
      @Override
      public RepositoryMeta targetToSource(Boolean value) {
        return null;
      }
    };

    bf.createBinding(loginModel, "selectedRepository", repositoryUrl, "disabled", userpassConverter);
    bf.createBinding(loginModel, "selectedRepository", username, "disabled", userpassConverter);//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(loginModel, "selectedRepository", userPassword, "disabled", userpassConverter);//$NON-NLS-1$ //$NON-NLS-2$
  }

  public void setBindingFactory( BindingFactory bf ) {
    this.bf = bf;
  }

  public BindingFactory getBindingFactory() {
    return this.bf;
  }

  public String getName() {
    return "repositoryLoginController";
  }

  /**
   * Executed when the user clicks the new repository image from the Repository Login Dialog It present a new dialog
   * where the user can selected what type of repository to create
   */
  public void newRepository() {
    helper.newRepository();
  }

  /**
   * Executed when the user clicks the edit repository image from the Repository Login Dialog It presents an edit dialog
   * where the user can edit information about the currently selected repository
   */

  public void editRepository() {
    helper.editRepository();
  }

  /**
   * Executed when the user clicks the delete repository image from the Repository Login Dialog It prompts the user with
   * a warning about the action to be performed and upon the approval of this action from the user, the selected
   * repository is deleted
   */

  public void deleteRepository() {
    helper.deleteRepository();
  }

  /**
   * Executed when user clicks cancel button on the Repository Login Dialog
   */
  public void closeRepositoryLoginDialog() {
    loginDialog.hide();
    getCallback().onCancel();
  }

  /**
   * Executed when the user checks or uncheck the "show this dialog at startup checkbox" It saves the current selection.
   */
  public void updateShowDialogAtStartup() {
    helper.updateShowDialogOnStartup( showAtStartup.isChecked() );
  }

  public XulMessageBox getMessageBox() {
    return messageBox;
  }

  public void setMessageBox( XulMessageBox messageBox ) {
    this.messageBox = messageBox;
  }

  public void setMessages( ResourceBundle messages ) {
    this.messages = messages;
  }

  public ResourceBundle getMessages() {
    return messages;
  }

  public String getPreferredRepositoryName() {
    return preferredRepositoryName;
  }

  public void setPreferredRepositoryName( String preferredRepositoryName ) {
    this.preferredRepositoryName = preferredRepositoryName;
  }

  public void setCallback( ILoginCallback callback ) {
    this.callback = callback;
  }

  public ILoginCallback getCallback() {
    return callback;
  }

  public void setShell( final Shell shell ) {
    this.shell = shell;
  }

  public Shell getShell() {
    return shell;
  }

  public void show(){
    if(loginModel.getUsername() != null){
      userPassword.setFocus();
    } else {
      username.setFocus();
    }
    //xnren start set server_url
    String server_url = "http://localhost:8080/etl_platform";
    try{
      Properties p = new Properties();
      String propFile = Const.getKettleDirectory()+"/"+Const.KETTLE_PROPERTIES;
      p.load(new FileInputStream(propFile));
      if(p.getProperty("SERVER_URL") != null){
        server_url = p.getProperty("SERVER_URL");
      }
    }catch (Exception e){
      e.printStackTrace();
    }
   repositoryUrl.setValue("http://localhost:8080/etl_platform");
    repositoryUrl.setValue(server_url);
    //xnren end


    // PDI-7443: The repo list does not show the selected repo
    // make the layout play nice, this is necessary to have the selection box scroll reliably
//    if (availableRepositories.getRows() < 4){
//    	availableRepositories.setRows(4);
//    }
//
//    int idx = loginModel.getRepositoryIndex(loginModel.getSelectedRepository());
//    if (idx >= 0){
//    	availableRepositories.setSelectedIndex(idx);
//    }
    // END OF PDI-7443

    loginDialog.show();
  }

  /**
   * login and show all available repositories in platform
   */
  public void showRepository(){
    final Shell loginShell = (Shell) loginDialog.getRootObject();
    helper = new RepositoriesHelper(loginModel, document, loginShell);
    if("OK".equals(helper.getInitMessages())){
      if(helper.getRepositoryCount()==0){
        MessageBox box = new MessageBox(shell, SWT.YES| SWT.ICON_INFORMATION);
        box.setMessage(BaseMessages.getString(PKG,"Repository.Show.NoRepository"));
        box.setText(BaseMessages.getString(PKG,"Repository.Show.NoRepository.Dialog.Title"));
        box.open();
        return;
      }
      if (availableRepositories.getRows() < 4){
        availableRepositories.setRows(4);
      }

      int idx = loginModel.getRepositoryIndex(loginModel.getSelectedRepository());
      if (idx >= 0){
        availableRepositories.setSelectedIndex(idx);
      }
      helper.setPreferedReopsitory();
      availableRepositories.setVisible(true);
      connectTipLabel.setVisible(true);
      //username.setVisible(false);
      //userPassword.setVisible(false);
      //repositoryUrl.setVisible(false);
      //repLabel.setVisible(false);
      //usernameLabel.setVisible(false);
      //userPasswordLabel.setVisible(false);
      //okButton.setVisible(false);
      //cancelButton.setVisible(false);
    }else{
      MessageBox box = new MessageBox(shell, SWT.YES| SWT.ICON_ERROR);
      box.setMessage(BaseMessages.getString(PKG,"Dialog.Error.Message"));
      box.setText(BaseMessages.getString(PKG,"Dialog.Error"));
      box.open();
    }
  }

  public void login() {
    if(loginModel.isValid() == false){
      return;
    }
    XulWaitBox box;
    try {
      box = (XulWaitBox) document.createElement("waitbox");
      box.setIndeterminate(true);
      box.setCanCancel(false);
      box.setTitle(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Wait.Title"));
      box.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Wait.Message"));
      final Shell loginShell = (Shell) loginDialog.getRootObject();
      final Display display = loginShell.getDisplay();
      box.setDialogParent(loginShell);
      box.setRunnable(new WaitBoxRunnable(box){
        @Override
        public void run() {
          try {
            helper.loginToRepository();

            waitBox.stop();
            display.syncExec(new Runnable(){
              public void run() {
                loginDialog.hide();
                okButton.setDisabled(false);
                cancelButton.setDisabled(false);

                if (helper.getConnectedRepository().getConnectMessage() != null) {
                  getMessageBox().setTitle(BaseMessages.getString(PKG, "ConnectMessageTitle")); //$NON-NLS-1$
                  getMessageBox().setMessage(helper.getConnectedRepository().getConnectMessage());
                  getMessageBox().open();
                }

                getCallback().onSuccess(helper.getConnectedRepository());
              }
            });

          } catch (final Throwable th) {

            waitBox.stop();

            try {
              display.syncExec(new Runnable(){
                public void run() {

                  getCallback().onError(th);
                  okButton.setDisabled(false);
                  cancelButton.setDisabled(false);
                }
              });
            } catch (Exception e) {
              e.printStackTrace();
            }

          }
        }

        @Override
        public void cancel() {
        }

      });
      okButton.setDisabled(true);
      cancelButton.setDisabled(true);
      box.start();
    } catch (XulException e1) {
      getCallback().onError(e1);
    }
  }
}
