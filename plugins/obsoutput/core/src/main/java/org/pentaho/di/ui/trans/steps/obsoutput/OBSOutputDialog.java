/*******************************************************************************
 *
 *
 *
 * Copyright (C) 2011-2019 by Sun : http://www.kingbase.com.cn
 *
 *******************************************************************************
 *
 *
 *    Email : snj1314@163.com
 *
 *
 ******************************************************************************/

package org.pentaho.di.ui.trans.steps.obsoutput;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.obsoutput.OBSObjectsProvider;
import org.pentaho.di.trans.steps.obsoutput.OBSOutputMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboValuesSelectionListener;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

/**
 * 
 * 
 * @author Sun
 * @since 2019年2月25日
 * @version
 * 
 */
public class OBSOutputDialog extends BaseStepDialog implements StepDialogInterface {

  private static Class<?> PKG = OBSOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private CTabFolder wTabFolder;
  private FormData fdTabFolder;

  private CTabItem wFileTab, wContentTab, wFieldsTab;
  private FormData fdFileComp, fdContentComp, fdFieldsComp;

  // FileTab

  private Label wlEndPoint;
  private TextVar wEndPoint;
  private FormData fdlEndPoint, fdEndPoint;

  private Label wlAccessKey;
  private TextVar wAccessKey;
  private FormData fdlAccessKey, fdAccessKey;

  private Label wlSecretKey;
  private TextVar wSecretKey;
  private FormData fdlSecretKey, fdSecretKey;

  private Label wlBucket;
  private Button wbBucket;
  private TextVar wBucket;
  private FormData fdlBucket, fdbBucket, fdBucket;

  private Label wlObjectKey;
  private TextVar wObjectKey;
  private FormData fdlObjectKey, fdObjectKey;

  private Label wlFilename;
  private Button wbFilename;
  private TextVar wFilename;
  private FormData fdlFilename, fdbFilename, fdFilename;

  private Label wlDoNotOpenNewFileInit;
  private Button wDoNotOpenNewFileInit;
  private FormData fdlDoNotOpenNewFileInit, fdDoNotOpenNewFileInit;

  /* Additional fields */
  private Label wlFileNameInField;
  private Button wFileNameInField;
  private FormData fdlFileNameInField, fdFileNameInField;

  private Label wlFileNameField;
  private ComboVar wFileNameField;
  private FormData fdlFileNameField, fdFileNameField;
  /* END */

  private Label wlExtension;
  private TextVar wExtension;
  private FormData fdlExtension, fdExtension;

  private Label wlAddStepnr;
  private Button wAddStepnr;
  private FormData fdlAddStepnr, fdAddStepnr;

  private Label wlAddPartnr;
  private Button wAddPartnr;
  private FormData fdlAddPartnr, fdAddPartnr;

  private Label wlAddDate;
  private Button wAddDate;
  private FormData fdlAddDate, fdAddDate;

  private Label wlAddTime;
  private Button wAddTime;
  private FormData fdlAddTime, fdAddTime;

  private Label wlSpecifyFormat;
  private Button wSpecifyFormat;
  private FormData fdlSpecifyFormat, fdSpecifyFormat;

  private Label wlDateTimeFormat;
  private CCombo wDateTimeFormat;
  private FormData fdlDateTimeFormat, fdDateTimeFormat;

  private Button wbShowFiles;
  private FormData fdbShowFiles;

  private Label wlAddToResult;
  private Button wAddToResult;
  private FormData fdlAddToResult, fdAddToResult;

  // ContentTab

  private Label wlAppend;
  private Button wAppend;
  private FormData fdlAppend, fdAppend;

  private Label wlSeparator;
  private Button wbSeparator;
  private TextVar wSeparator;
  private FormData fdlSeparator, fdbSeparator, fdSeparator;

  private Label wlEnclosure;
  private TextVar wEnclosure;
  private FormData fdlEnclosure, fdEnclosure;

  private Label wlEnclForced;
  private Button wEnclForced;
  private FormData fdlEnclForced, fdEnclForced;

  private Label wlHeader;
  private Button wHeader;
  private FormData fdlHeader, fdHeader;

  private Label wlFooter;
  private Button wFooter;
  private FormData fdlFooter, fdFooter;

  private Label wlFormat;
  private CCombo wFormat;
  private FormData fdlFormat, fdFormat;

  private Label wlCompression;
  private CCombo wCompression;
  private FormData fdlCompression, fdCompression;

  private Label wlEncoding;
  private CCombo wEncoding;
  private FormData fdlEncoding, fdEncoding;

  private Label wlPad;
  private Button wPad;
  private FormData fdlPad, fdPad;

  private Label wlFastDump;
  private Button wFastDump;
  private FormData fdlFastDump, fdFastDump;

  private Label wlSplitEvery;
  private Text wSplitEvery;
  private FormData fdlSplitEvery, fdSplitEvery;

  private Label wlEndedLine;
  private Text wEndedLine;
  private FormData fdlEndedLine, fdEndedLine;

  // FieldsTab

  private TableView wFields;
  private FormData fdFields;

  private OBSOutputMeta input;

  private boolean gotEncodings = false;

  private ColumnInfo[] colinf;

  private Map<String, Integer> inputFields;

  private boolean gotPreviousFields = false;

  public OBSOutputDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
    super(parent, (BaseStepMeta) in, transMeta, sname);
    this.input = (OBSOutputMeta) in;
    inputFields = new HashMap<String, Integer>();
  }

  @Override
  public String open() {

    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    props.setLook(shell);
    setShellImage(shell, input);

    ModifyListener lsMod = new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        input.setChanged();
      }
    };
    SelectionAdapter lsSel = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label(shell, SWT.RIGHT);
    wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
    props.setLook(wlStepname);
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment(0, 0);
    fdlStepname.top = new FormAttachment(0, margin);
    fdlStepname.right = new FormAttachment(middle, -margin);
    wlStepname.setLayoutData(fdlStepname);

    wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wStepname.setText(stepname);
    props.setLook(wStepname);
    wStepname.addModifyListener(lsMod);
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment(middle, 0);
    fdStepname.top = new FormAttachment(0, margin);
    fdStepname.right = new FormAttachment(100, 0);
    wStepname.setLayoutData(fdStepname);

    // TabFolder line
    wTabFolder = new CTabFolder(shell, SWT.BORDER);
    props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
    wTabFolder.setSimple(false);

    // ////////////////////////
    // START OF FILE TAB///
    // /
    wFileTab = new CTabItem(wTabFolder, SWT.NONE);
    wFileTab.setText(BaseMessages.getString(PKG, "OBSOutputDialog.FileTab.Title"));

    Composite wFileComp = new Composite(wTabFolder, SWT.NONE);
    props.setLook(wFileComp);

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wFileComp.setLayout(fileLayout);

    // EndPoint line
    wlEndPoint = new Label(wFileComp, SWT.RIGHT);
    wlEndPoint.setText(BaseMessages.getString(PKG, "OBSOutputDialog.EndPoint.Label"));
    props.setLook(wlEndPoint);
    fdlEndPoint = new FormData();
    fdlEndPoint.left = new FormAttachment(0, 0);
    fdlEndPoint.top = new FormAttachment(wStepname, 2 * margin);
    fdlEndPoint.right = new FormAttachment(middle, -margin);
    wlEndPoint.setLayoutData(fdlEndPoint);

    wEndPoint = new TextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wEndPoint);
    wEndPoint.addModifyListener(lsMod);
    fdEndPoint = new FormData();
    fdEndPoint.left = new FormAttachment(middle, 0);
    fdEndPoint.top = new FormAttachment(0, 2 * margin);
    fdEndPoint.right = new FormAttachment(100, 0);
    wEndPoint.setLayoutData(fdEndPoint);

    // AccessKey line
    wlAccessKey = new Label(wFileComp, SWT.RIGHT);
    wlAccessKey.setText(BaseMessages.getString(PKG, "OBSOutputDialog.AccessKey.Label"));
    props.setLook(wlAccessKey);
    fdlAccessKey = new FormData();
    fdlAccessKey.left = new FormAttachment(0, 0);
    fdlAccessKey.top = new FormAttachment(wEndPoint, 2 * margin);
    fdlAccessKey.right = new FormAttachment(middle, -margin);
    wlAccessKey.setLayoutData(fdlAccessKey);

    // wAccessKey = new TextVar(transMeta, wFileComp, SWT.PASSWORD | SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wAccessKey = new PasswordTextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wAccessKey);
    wAccessKey.addModifyListener(lsMod);
    fdAccessKey = new FormData();
    fdAccessKey.left = new FormAttachment(middle, 0);
    fdAccessKey.top = new FormAttachment(wEndPoint, 2 * margin);
    fdAccessKey.right = new FormAttachment(100, 0);
    wAccessKey.setLayoutData(fdAccessKey);

    // SecretKey line
    wlSecretKey = new Label(wFileComp, SWT.RIGHT);
    wlSecretKey.setText(BaseMessages.getString(PKG, "OBSOutputDialog.SecretKey.Label"));
    props.setLook(wlSecretKey);
    fdlSecretKey = new FormData();
    fdlSecretKey.left = new FormAttachment(0, 0);
    fdlSecretKey.top = new FormAttachment(wAccessKey, 2 * margin);
    fdlSecretKey.right = new FormAttachment(middle, -margin);
    wlSecretKey.setLayoutData(fdlSecretKey);

    // wSecretKey = new TextVar(transMeta, wFileComp, SWT.PASSWORD | SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wSecretKey = new PasswordTextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wSecretKey);
    wSecretKey.addModifyListener(lsMod);
    fdSecretKey = new FormData();
    fdSecretKey.left = new FormAttachment(middle, 0);
    fdSecretKey.top = new FormAttachment(wAccessKey, 2 * margin);
    fdSecretKey.right = new FormAttachment(100, 0);
    wSecretKey.setLayoutData(fdSecretKey);

    // Bucket line
    wlBucket = new Label(wFileComp, SWT.RIGHT);
    wlBucket.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Bucket.Label"));
    props.setLook(wlBucket);
    fdlBucket = new FormData();
    fdlBucket.left = new FormAttachment(0, 0);
    fdlBucket.top = new FormAttachment(wSecretKey, 2 * margin);
    fdlBucket.right = new FormAttachment(middle, -margin);
    wlBucket.setLayoutData(fdlBucket);

    wbBucket = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
    props.setLook(wbBucket);
    wbBucket.setText(BaseMessages.getString("System.Button.Browse"));
    fdbBucket = new FormData();
    fdbBucket.top = new FormAttachment(wSecretKey, margin);
    fdbBucket.right = new FormAttachment(100, 0);
    wbBucket.setLayoutData(fdbBucket);

    wbBucket.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {

        // List the buckets...
        //
        try {
          OBSOutputMeta meta = new OBSOutputMeta();
          getInfo(meta);

          OBSObjectsProvider provider = new OBSObjectsProvider(meta.getObsClient(transMeta));

          // ObsClient client = meta.getObsClient(transMeta);
          // ListBucketsRequest request = new ListBucketsRequest();
          // request.setQueryLocation(true);
          // List<ObsBucket> result = client.listBuckets(request);
          // String[] buckets = result.stream().map(b -> b.getBucketName()).toArray(String[]::new);

          EnterSelectionDialog dialog = new EnterSelectionDialog(//
              shell, //
              provider.getBucketsNames(), //
              BaseMessages.getString(PKG, "OBSOutputDialog.Exception.SelectBucket.Title"), //
              BaseMessages.getString(PKG, "OBSOutputDialog.Exception.SelectBucket.Message")//
          );
          dialog.setMulti(false);
          String bucketName = dialog.open();
          if (bucketName != null) {
            wBucket.setText(bucketName);
          }
        } catch (Exception e) {
          new ErrorDialog(//
              shell, //
              BaseMessages.getString(PKG, "OBSInputDialog.Exception.UnableToGetBuckets.Title"), //
              BaseMessages.getString(PKG, "OBSInputDialog.Exception.UnableToGetBuckets.Message"), //
              e//
          );
        }

      }
    });

    wBucket = new TextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wBucket);
    wBucket.addModifyListener(lsMod);
    fdBucket = new FormData();
    fdBucket.left = new FormAttachment(middle, 0);
    fdBucket.top = new FormAttachment(wSecretKey, 2 * margin);
    fdBucket.right = new FormAttachment(wbBucket, -margin);
    wBucket.setLayoutData(fdBucket);

    // ObjectKey line
    //
    wlObjectKey = new Label(wFileComp, SWT.RIGHT);
    wlObjectKey.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Filename.Label")+" 或目录");
    props.setLook(wlObjectKey);
    fdlObjectKey = new FormData();
    fdlObjectKey.left = new FormAttachment(0, 0);
    fdlObjectKey.top = new FormAttachment(wBucket, 2 * margin);
    fdlObjectKey.right = new FormAttachment(middle, -margin);
    wlObjectKey.setLayoutData(fdlObjectKey);

    wObjectKey = new TextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wObjectKey);
    wObjectKey.addModifyListener(lsMod);
    fdObjectKey = new FormData();
    fdObjectKey.left = new FormAttachment(middle, 0);
    fdObjectKey.top = new FormAttachment(wBucket, 2 * margin);
    fdObjectKey.right = new FormAttachment(100, 0);
    wObjectKey.setLayoutData(fdObjectKey);

    // Filename line
    //
    wlFilename = new Label(wFileComp, SWT.RIGHT);
    wlFilename.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Filename.Label"));
    props.setLook(wlFilename);
    fdlFilename = new FormData();
    fdlFilename.left = new FormAttachment(0, 0);
    fdlFilename.top = new FormAttachment(wObjectKey, 2 * margin);
    fdlFilename.right = new FormAttachment(middle, -margin);
    wlFilename.setLayoutData(fdlFilename);

    wbFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
    wbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
    props.setLook(wbFilename);
    fdbFilename = new FormData();
    fdbFilename.top = new FormAttachment(wObjectKey, margin);
    fdbFilename.right = new FormAttachment(100, 0);
    wbFilename.setLayoutData(fdbFilename);

    wFilename = new TextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wFilename);
    wFilename.addModifyListener(lsMod);
    fdFilename = new FormData();
    fdFilename.left = new FormAttachment(middle, 0);
    fdFilename.top = new FormAttachment(wObjectKey, 2 * margin);
    fdFilename.right = new FormAttachment(wbFilename, -margin);
    wFilename.setLayoutData(fdFilename);

    wlFilename.setVisible(false);
    wbFilename.setVisible(false);
    wFilename.setVisible(false);

    // Open new File at Init
    wlDoNotOpenNewFileInit = new Label(wFileComp, SWT.RIGHT);
    wlDoNotOpenNewFileInit.setText(BaseMessages.getString(PKG, "OBSOutputDialog.DoNotOpenNewFileInit.Label"));
    props.setLook(wlDoNotOpenNewFileInit);
    fdlDoNotOpenNewFileInit = new FormData();
    fdlDoNotOpenNewFileInit.left = new FormAttachment(0, 0);
    fdlDoNotOpenNewFileInit.top = new FormAttachment(wFilename, 2 * margin);
    fdlDoNotOpenNewFileInit.right = new FormAttachment(middle, -margin);
    wlDoNotOpenNewFileInit.setLayoutData(fdlDoNotOpenNewFileInit);

    wDoNotOpenNewFileInit = new Button(wFileComp, SWT.CHECK);
    props.setLook(wDoNotOpenNewFileInit);
    wDoNotOpenNewFileInit.addSelectionListener(lsSel);
    fdDoNotOpenNewFileInit = new FormData();
    fdDoNotOpenNewFileInit.left = new FormAttachment(middle, 0);
    fdDoNotOpenNewFileInit.top = new FormAttachment(wFilename, 2 * margin);
    fdDoNotOpenNewFileInit.right = new FormAttachment(100, 0);
    wDoNotOpenNewFileInit.setLayoutData(fdDoNotOpenNewFileInit);

    /* next Lines */
    // FileNameInField line
    wlFileNameInField = new Label(wFileComp, SWT.RIGHT);
    wlFileNameInField.setText(BaseMessages.getString(PKG, "OBSOutputDialog.FileNameInField.Label"));
    props.setLook(wlFileNameInField);
    fdlFileNameInField = new FormData();
    fdlFileNameInField.left = new FormAttachment(0, 0);
    fdlFileNameInField.top = new FormAttachment(wDoNotOpenNewFileInit, 2 * margin);
    fdlFileNameInField.right = new FormAttachment(middle, -margin);
    wlFileNameInField.setLayoutData(fdlFileNameInField);

    wFileNameInField = new Button(wFileComp, SWT.CHECK);
    props.setLook(wFileNameInField);
    fdFileNameInField = new FormData();
    fdFileNameInField.left = new FormAttachment(middle, 0);
    fdFileNameInField.top = new FormAttachment(wDoNotOpenNewFileInit, 2 * margin);
    fdFileNameInField.right = new FormAttachment(100, 0);
    wFileNameInField.setLayoutData(fdFileNameInField);

    wFileNameInField.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        input.setChanged();
        activeFileNameField();
      }
    });

    // FileNameField Line
    wlFileNameField = new Label(wFileComp, SWT.RIGHT);
    wlFileNameField.setText(BaseMessages.getString(PKG, "OBSOutputDialog.FileNameField.Label")); //$NON-NLS-1$
    props.setLook(wlFileNameField);
    fdlFileNameField = new FormData();
    fdlFileNameField.left = new FormAttachment(0, 0);
    fdlFileNameField.top = new FormAttachment(wFileNameInField, 2 * margin);
    fdlFileNameField.right = new FormAttachment(middle, -margin);
    wlFileNameField.setLayoutData(fdlFileNameField);

    wFileNameField = new ComboVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wFileNameField);
    wFileNameField.addModifyListener(lsMod);
    fdFileNameField = new FormData();
    fdFileNameField.left = new FormAttachment(middle, 0);
    fdFileNameField.top = new FormAttachment(wFileNameInField, 2 * margin);
    fdFileNameField.right = new FormAttachment(100, 0);
    wFileNameField.setLayoutData(fdFileNameField);
    wFileNameField.setEnabled(false);

    wFileNameField.addFocusListener(new FocusListener() {
      @Override
      public void focusLost(org.eclipse.swt.events.FocusEvent e) {
      }

      @Override
      public void focusGained(org.eclipse.swt.events.FocusEvent e) {
        Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        shell.setCursor(busy);
        getFields();
        shell.setCursor(null);
        busy.dispose();
      }
    });
    /* End */

    // Extension line
    wlExtension = new Label(wFileComp, SWT.RIGHT);
    wlExtension.setText(BaseMessages.getString(PKG, "System.Label.Extension"));
    props.setLook(wlExtension);
    fdlExtension = new FormData();
    fdlExtension.left = new FormAttachment(0, 0);
    fdlExtension.top = new FormAttachment(wFileNameField, 2 * margin);
    fdlExtension.right = new FormAttachment(middle, -margin);
    wlExtension.setLayoutData(fdlExtension);

    wExtension = new TextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wExtension.setText("");
    props.setLook(wExtension);
    wExtension.addModifyListener(lsMod);
    fdExtension = new FormData();
    fdExtension.left = new FormAttachment(middle, 0);
    fdExtension.top = new FormAttachment(wFileNameField, 2 * margin);
    fdExtension.right = new FormAttachment(100, 0);
    wExtension.setLayoutData(fdExtension);

    // Create multi-part file?
    wlAddStepnr = new Label(wFileComp, SWT.RIGHT);
    wlAddStepnr.setText(BaseMessages.getString(PKG, "OBSOutputDialog.AddStepnr.Label"));
    props.setLook(wlAddStepnr);
    fdlAddStepnr = new FormData();
    fdlAddStepnr.left = new FormAttachment(0, 0);
    fdlAddStepnr.top = new FormAttachment(wExtension, 2 * margin);
    fdlAddStepnr.right = new FormAttachment(middle, -margin);
    wlAddStepnr.setLayoutData(fdlAddStepnr);

    wAddStepnr = new Button(wFileComp, SWT.CHECK);
    props.setLook(wAddStepnr);
    wAddStepnr.addSelectionListener(lsSel);
    fdAddStepnr = new FormData();
    fdAddStepnr.left = new FormAttachment(middle, 0);
    fdAddStepnr.top = new FormAttachment(wExtension, 2 * margin);
    fdAddStepnr.right = new FormAttachment(100, 0);
    wAddStepnr.setLayoutData(fdAddStepnr);

    // Create multi-part file?
    wlAddPartnr = new Label(wFileComp, SWT.RIGHT);
    wlAddPartnr.setText(BaseMessages.getString(PKG, "OBSOutputDialog.AddPartnr.Label"));
    props.setLook(wlAddPartnr);
    fdlAddPartnr = new FormData();
    fdlAddPartnr.left = new FormAttachment(0, 0);
    fdlAddPartnr.top = new FormAttachment(wAddStepnr, 2 * margin);
    fdlAddPartnr.right = new FormAttachment(middle, -margin);
    wlAddPartnr.setLayoutData(fdlAddPartnr);

    wAddPartnr = new Button(wFileComp, SWT.CHECK);
    props.setLook(wAddPartnr);
    wAddPartnr.addSelectionListener(lsSel);
    fdAddPartnr = new FormData();
    fdAddPartnr.left = new FormAttachment(middle, 0);
    fdAddPartnr.top = new FormAttachment(wAddStepnr, 2 * margin);
    fdAddPartnr.right = new FormAttachment(100, 0);
    wAddPartnr.setLayoutData(fdAddPartnr);

    // Create multi-part file?
    wlAddDate = new Label(wFileComp, SWT.RIGHT);
    wlAddDate.setText(BaseMessages.getString(PKG, "OBSOutputDialog.AddDate.Label"));
    props.setLook(wlAddDate);
    fdlAddDate = new FormData();
    fdlAddDate.left = new FormAttachment(0, 0);
    fdlAddDate.top = new FormAttachment(wAddPartnr, 2 * margin);
    fdlAddDate.right = new FormAttachment(middle, -margin);
    wlAddDate.setLayoutData(fdlAddDate);

    wAddDate = new Button(wFileComp, SWT.CHECK);
    props.setLook(wAddDate);
    wAddDate.addSelectionListener(lsSel);
    fdAddDate = new FormData();
    fdAddDate.left = new FormAttachment(middle, 0);
    fdAddDate.top = new FormAttachment(wAddPartnr, 2 * margin);
    fdAddDate.right = new FormAttachment(100, 0);
    wAddDate.setLayoutData(fdAddDate);

    // Create multi-part file?
    wlAddTime = new Label(wFileComp, SWT.RIGHT);
    wlAddTime.setText(BaseMessages.getString(PKG, "OBSOutputDialog.AddTime.Label"));
    props.setLook(wlAddTime);
    fdlAddTime = new FormData();
    fdlAddTime.left = new FormAttachment(0, 0);
    fdlAddTime.top = new FormAttachment(wAddDate, 2 * margin);
    fdlAddTime.right = new FormAttachment(middle, -margin);
    wlAddTime.setLayoutData(fdlAddTime);

    wAddTime = new Button(wFileComp, SWT.CHECK);
    props.setLook(wAddTime);
    wAddTime.addSelectionListener(lsSel);
    fdAddTime = new FormData();
    fdAddTime.left = new FormAttachment(middle, 0);
    fdAddTime.top = new FormAttachment(wAddDate, 2 * margin);
    fdAddTime.right = new FormAttachment(100, 0);
    wAddTime.setLayoutData(fdAddTime);

    // Specify date time format?
    wlSpecifyFormat = new Label(wFileComp, SWT.RIGHT);
    wlSpecifyFormat.setText(BaseMessages.getString(PKG, "OBSOutputDialog.SpecifyFormat.Label"));
    props.setLook(wlSpecifyFormat);
    fdlSpecifyFormat = new FormData();
    fdlSpecifyFormat.left = new FormAttachment(0, 0);
    fdlSpecifyFormat.top = new FormAttachment(wAddTime, 2 * margin);
    fdlSpecifyFormat.right = new FormAttachment(middle, -margin);
    wlSpecifyFormat.setLayoutData(fdlSpecifyFormat);

    wSpecifyFormat = new Button(wFileComp, SWT.CHECK);
    props.setLook(wSpecifyFormat);
    fdSpecifyFormat = new FormData();
    fdSpecifyFormat.left = new FormAttachment(middle, 0);
    fdSpecifyFormat.top = new FormAttachment(wAddTime, 2 * margin);
    fdSpecifyFormat.right = new FormAttachment(100, 0);
    wSpecifyFormat.setLayoutData(fdSpecifyFormat);

    wSpecifyFormat.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        input.setChanged();
        setDateTimeFormat();
      }
    });

    // DateTimeFormat
    wlDateTimeFormat = new Label(wFileComp, SWT.RIGHT);
    wlDateTimeFormat.setText(BaseMessages.getString(PKG, "OBSOutputDialog.DateTimeFormat.Label"));
    props.setLook(wlDateTimeFormat);
    fdlDateTimeFormat = new FormData();
    fdlDateTimeFormat.left = new FormAttachment(0, 0);
    fdlDateTimeFormat.top = new FormAttachment(wSpecifyFormat, 2 * margin);
    fdlDateTimeFormat.right = new FormAttachment(middle, -margin);
    wlDateTimeFormat.setLayoutData(fdlDateTimeFormat);

    wDateTimeFormat = new CCombo(wFileComp, SWT.BORDER | SWT.READ_ONLY);
    wDateTimeFormat.setEditable(true);
    props.setLook(wDateTimeFormat);
    wDateTimeFormat.addModifyListener(lsMod);
    fdDateTimeFormat = new FormData();
    fdDateTimeFormat.left = new FormAttachment(middle, 0);
    fdDateTimeFormat.top = new FormAttachment(wSpecifyFormat, 2 * margin);
    fdDateTimeFormat.right = new FormAttachment(100, 0);
    wDateTimeFormat.setLayoutData(fdDateTimeFormat);

    String[] dats = Const.getDateFormats();
    for (int x = 0; x < dats.length; x++) {
      wDateTimeFormat.add(dats[x]);
    }

    wbShowFiles = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
    props.setLook(wbShowFiles);
    wbShowFiles.setText(BaseMessages.getString(PKG, "OBSOutputDialog.ShowFiles.Button"));
    fdbShowFiles = new FormData();
    fdbShowFiles.left = new FormAttachment(middle, 0);
    fdbShowFiles.top = new FormAttachment(wDateTimeFormat, margin * 2);
    wbShowFiles.setLayoutData(fdbShowFiles);

    wbShowFiles.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {

      }
    });
    wbShowFiles.setVisible(false);

    // Add File to the result files name
    wlAddToResult = new Label(wFileComp, SWT.RIGHT);
    wlAddToResult.setText(BaseMessages.getString(PKG, "OBSOutputDialog.AddFileToResult.Label"));
    props.setLook(wlAddToResult);
    fdlAddToResult = new FormData();
    fdlAddToResult.left = new FormAttachment(0, 0);
    fdlAddToResult.top = new FormAttachment(wbShowFiles, 2 * margin);
    fdlAddToResult.right = new FormAttachment(middle, -margin);
    wlAddToResult.setLayoutData(fdlAddToResult);

    wAddToResult = new Button(wFileComp, SWT.CHECK);
    props.setLook(wAddToResult);
    wAddToResult.addSelectionListener(lsSel);
    fdAddToResult = new FormData();
    fdAddToResult.left = new FormAttachment(middle, 0);
    fdAddToResult.top = new FormAttachment(wbShowFiles, 2 * margin);
    fdAddToResult.right = new FormAttachment(100, 0);
    wAddToResult.setLayoutData(fdAddToResult);

    fdFileComp = new FormData();
    fdFileComp.left = new FormAttachment(0, 0);
    fdFileComp.top = new FormAttachment(0, 0);
    fdFileComp.right = new FormAttachment(100, 0);
    fdFileComp.bottom = new FormAttachment(100, 0);
    wFileComp.setLayoutData(fdFileComp);

    wFileComp.layout();
    wFileTab.setControl(wFileComp);

    // ///////////////////////////////////////////////////////////
    // / END OF FILE TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF CONTENT TAB///
    // /
    wContentTab = new CTabItem(wTabFolder, SWT.NONE);
    wContentTab.setText(BaseMessages.getString(PKG, "OBSOutputDialog.ContentTab.Title"));

    FormLayout contentLayout = new FormLayout();
    contentLayout.marginWidth = 3;
    contentLayout.marginHeight = 3;

    Composite wContentComp = new Composite(wTabFolder, SWT.NONE);
    props.setLook(wContentComp);
    wContentComp.setLayout(contentLayout);

    // Append to end of file?
    wlAppend = new Label(wContentComp, SWT.RIGHT);
    wlAppend.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Append.Label"));
    props.setLook(wlAppend);
    fdlAppend = new FormData();
    fdlAppend.left = new FormAttachment(0, 0);
    fdlAppend.top = new FormAttachment(0, 0);
    fdlAppend.right = new FormAttachment(middle, -margin);
    wlAppend.setLayoutData(fdlAppend);

    wAppend = new Button(wContentComp, SWT.CHECK);
    props.setLook(wAppend);
    wAppend.addSelectionListener(lsSel);
    fdAppend = new FormData();
    fdAppend.left = new FormAttachment(middle, 0);
    fdAppend.top = new FormAttachment(0, 0);
    fdAppend.right = new FormAttachment(100, 0);
    wAppend.setLayoutData(fdAppend);

    // Separator line
    wlSeparator = new Label(wContentComp, SWT.RIGHT);
    wlSeparator.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Separator.Label"));
    props.setLook(wlSeparator);
    fdlSeparator = new FormData();
    fdlSeparator.left = new FormAttachment(0, 0);
    fdlSeparator.top = new FormAttachment(wAppend, 2 * margin);
    fdlSeparator.right = new FormAttachment(middle, -margin);
    wlSeparator.setLayoutData(fdlSeparator);

    wbSeparator = new Button(wContentComp, SWT.PUSH | SWT.CENTER);
    props.setLook(wbSeparator);
    wbSeparator.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Separator.Button.Tab"));
    fdbSeparator = new FormData();
    fdbSeparator.top = new FormAttachment(wAppend, margin);
    fdbSeparator.right = new FormAttachment(100, 0);
    wbSeparator.setLayoutData(fdbSeparator);

    // Allow the insertion of tabs as delimiter...
    wbSeparator.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent se) {
        Text t = wSeparator.getTextWidget();
        if (t != null) {
          t.insert("\t");
        }
      }
    });

    wSeparator = new TextVar(transMeta, wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wSeparator);
    wSeparator.addModifyListener(lsMod);
    fdSeparator = new FormData();
    fdSeparator.left = new FormAttachment(middle, 0);
    fdSeparator.top = new FormAttachment(wAppend, 2 * margin);
    fdSeparator.right = new FormAttachment(wbSeparator, -margin);
    wSeparator.setLayoutData(fdSeparator);

    // Enclosure line
    wlEnclosure = new Label(wContentComp, SWT.RIGHT);
    wlEnclosure.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Enclosure.Label"));
    props.setLook(wlEnclosure);
    fdlEnclosure = new FormData();
    fdlEnclosure.left = new FormAttachment(0, 0);
    fdlEnclosure.top = new FormAttachment(wSeparator, 2 * margin);
    fdlEnclosure.right = new FormAttachment(middle, -margin);
    wlEnclosure.setLayoutData(fdlEnclosure);

    wEnclosure = new TextVar(transMeta, wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wEnclosure);
    wEnclosure.addModifyListener(lsMod);
    fdEnclosure = new FormData();
    fdEnclosure.left = new FormAttachment(middle, 0);
    fdEnclosure.top = new FormAttachment(wSeparator, 2 * margin);
    fdEnclosure.right = new FormAttachment(100, 0);
    wEnclosure.setLayoutData(fdEnclosure);

    // Enclosure Forced line
    wlEnclForced = new Label(wContentComp, SWT.RIGHT);
    wlEnclForced.setText(BaseMessages.getString(PKG, "OBSOutputDialog.EnclForced.Label"));
    props.setLook(wlEnclForced);
    fdlEnclForced = new FormData();
    fdlEnclForced.left = new FormAttachment(0, 0);
    fdlEnclForced.top = new FormAttachment(wEnclosure, 2 * margin);
    fdlEnclForced.right = new FormAttachment(middle, -margin);
    wlEnclForced.setLayoutData(fdlEnclForced);

    wEnclForced = new Button(wContentComp, SWT.CHECK);
    props.setLook(wEnclForced);
    wEnclForced.addSelectionListener(lsSel);
    fdEnclForced = new FormData();
    fdEnclForced.left = new FormAttachment(middle, 0);
    fdEnclForced.top = new FormAttachment(wEnclosure, 2 * margin);
    fdEnclForced.right = new FormAttachment(100, 0);
    wEnclForced.setLayoutData(fdEnclForced);

    // header line
    wlHeader = new Label(wContentComp, SWT.RIGHT);
    wlHeader.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Header.Label"));
    props.setLook(wlHeader);
    fdlHeader = new FormData();
    fdlHeader.left = new FormAttachment(0, 0);
    fdlHeader.top = new FormAttachment(wEnclForced, 2 * margin);
    fdlHeader.right = new FormAttachment(middle, -margin);
    wlHeader.setLayoutData(fdlHeader);

    wHeader = new Button(wContentComp, SWT.CHECK);
    props.setLook(wHeader);
    wHeader.addSelectionListener(lsSel);
    fdHeader = new FormData();
    fdHeader.left = new FormAttachment(middle, 0);
    fdHeader.top = new FormAttachment(wEnclForced, 2 * margin);
    fdHeader.right = new FormAttachment(100, 0);
    wHeader.setLayoutData(fdHeader);

    // Footer line
    wlFooter = new Label(wContentComp, SWT.RIGHT);
    wlFooter.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Footer.Label"));
    props.setLook(wlFooter);
    fdlFooter = new FormData();
    fdlFooter.left = new FormAttachment(0, 0);
    fdlFooter.top = new FormAttachment(wHeader, 2 * margin);
    fdlFooter.right = new FormAttachment(middle, -margin);
    wlFooter.setLayoutData(fdlFooter);

    wFooter = new Button(wContentComp, SWT.CHECK);
    props.setLook(wFooter);
    wFooter.addSelectionListener(lsSel);
    fdFooter = new FormData();
    fdFooter.left = new FormAttachment(middle, 0);
    fdFooter.top = new FormAttachment(wHeader, 2 * margin);
    fdFooter.right = new FormAttachment(100, 0);
    wFooter.setLayoutData(fdFooter);

    // Format line
    wlFormat = new Label(wContentComp, SWT.RIGHT);
    wlFormat.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Format.Label"));
    props.setLook(wlFormat);
    fdlFormat = new FormData();
    fdlFormat.left = new FormAttachment(0, 0);
    fdlFormat.top = new FormAttachment(wFooter, 2 * margin);
    fdlFormat.right = new FormAttachment(middle, -margin);
    wlFormat.setLayoutData(fdlFormat);

    wFormat = new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
    props.setLook(wFormat);
    wFormat.addModifyListener(lsMod);
    fdFormat = new FormData();
    fdFormat.left = new FormAttachment(middle, 0);
    fdFormat.top = new FormAttachment(wFooter, 2 * margin);
    fdFormat.right = new FormAttachment(100, 0);
    wFormat.setLayoutData(fdFormat);

    for (int i = 0; i < TextFileOutputMeta.formatMapperLineTerminator.length; i++) {
      wFormat.add(BaseMessages.getString(PKG, "OBSOutputDialog.Format." + TextFileOutputMeta.formatMapperLineTerminator[i]));
    }
    wFormat.select(0);

    // Compression line
    wlCompression = new Label(wContentComp, SWT.RIGHT);
    wlCompression.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Compression.Label"));
    props.setLook(wlCompression);
    fdlCompression = new FormData();
    fdlCompression.left = new FormAttachment(0, 0);
    fdlCompression.top = new FormAttachment(wFormat, 2 * margin);
    fdlCompression.right = new FormAttachment(middle, -margin);
    wlCompression.setLayoutData(fdlCompression);

    wCompression = new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
    props.setLook(wCompression);
    wCompression.addModifyListener(lsMod);
    fdCompression = new FormData();
    fdCompression.left = new FormAttachment(middle, 0);
    fdCompression.top = new FormAttachment(wFormat, 2 * margin);
    fdCompression.right = new FormAttachment(100, 0);
    wCompression.setLayoutData(fdCompression);

    wCompression.setItems(CompressionProviderFactory.getInstance().getCompressionProviderNames());

    // Encoding line
    wlEncoding = new Label(wContentComp, SWT.RIGHT);
    wlEncoding.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Encoding.Label"));
    props.setLook(wlEncoding);
    fdlEncoding = new FormData();
    fdlEncoding.left = new FormAttachment(0, 0);
    fdlEncoding.top = new FormAttachment(wCompression, 2 * margin);
    fdlEncoding.right = new FormAttachment(middle, -margin);
    wlEncoding.setLayoutData(fdlEncoding);

    wEncoding = new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
    wEncoding.setEditable(true);
    props.setLook(wEncoding);
    wEncoding.addModifyListener(lsMod);
    fdEncoding = new FormData();
    fdEncoding.left = new FormAttachment(middle, 0);
    fdEncoding.top = new FormAttachment(wCompression, 2 * margin);
    fdEncoding.right = new FormAttachment(100, 0);
    wEncoding.setLayoutData(fdEncoding);

    wEncoding.addFocusListener(new FocusListener() {
      @Override
      public void focusLost(org.eclipse.swt.events.FocusEvent e) {
      }

      @Override
      public void focusGained(org.eclipse.swt.events.FocusEvent e) {
        Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        shell.setCursor(busy);
        setEncodings();
        shell.setCursor(null);
        busy.dispose();
      }
    });

    // Pad line
    wlPad = new Label(wContentComp, SWT.RIGHT);
    wlPad.setText(BaseMessages.getString(PKG, "OBSOutputDialog.Pad.Label"));
    props.setLook(wlPad);
    fdlPad = new FormData();
    fdlPad.left = new FormAttachment(0, 0);
    fdlPad.top = new FormAttachment(wEncoding, 2 * margin);
    fdlPad.right = new FormAttachment(middle, -margin);
    wlPad.setLayoutData(fdlPad);

    wPad = new Button(wContentComp, SWT.CHECK);
    props.setLook(wPad);
    wPad.addSelectionListener(lsSel);
    fdPad = new FormData();
    fdPad.left = new FormAttachment(middle, 0);
    fdPad.top = new FormAttachment(wEncoding, 2 * margin);
    fdPad.right = new FormAttachment(100, 0);
    wPad.setLayoutData(fdPad);

    // FastDump line
    wlFastDump = new Label(wContentComp, SWT.RIGHT);
    wlFastDump.setText(BaseMessages.getString(PKG, "OBSOutputDialog.FastDump.Label"));
    props.setLook(wlFastDump);
    fdlFastDump = new FormData();
    fdlFastDump.left = new FormAttachment(0, 0);
    fdlFastDump.top = new FormAttachment(wPad, 2 * margin);
    fdlFastDump.right = new FormAttachment(middle, -margin);
    wlFastDump.setLayoutData(fdlFastDump);

    wFastDump = new Button(wContentComp, SWT.CHECK);
    props.setLook(wFastDump);
    wFastDump.addSelectionListener(lsSel);
    fdFastDump = new FormData();
    fdFastDump.left = new FormAttachment(middle, 0);
    fdFastDump.top = new FormAttachment(wPad, 2 * margin);
    fdFastDump.right = new FormAttachment(100, 0);
    wFastDump.setLayoutData(fdFastDump);

    // SplitEvery line
    wlSplitEvery = new Label(wContentComp, SWT.RIGHT);
    wlSplitEvery.setText(BaseMessages.getString(PKG, "OBSOutputDialog.SplitEvery.Label"));
    props.setLook(wlSplitEvery);
    fdlSplitEvery = new FormData();
    fdlSplitEvery.left = new FormAttachment(0, 0);
    fdlSplitEvery.top = new FormAttachment(wFastDump, 2 * margin);
    fdlSplitEvery.right = new FormAttachment(middle, -margin);
    wlSplitEvery.setLayoutData(fdlSplitEvery);

    wSplitEvery = new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wSplitEvery);
    wSplitEvery.addModifyListener(lsMod);
    fdSplitEvery = new FormData();
    fdSplitEvery.left = new FormAttachment(middle, 0);
    fdSplitEvery.top = new FormAttachment(wFastDump, 2 * margin);
    fdSplitEvery.right = new FormAttachment(100, 0);
    wSplitEvery.setLayoutData(fdSplitEvery);

    // Bruise:
    wlEndedLine = new Label(wContentComp, SWT.RIGHT);
    wlEndedLine.setText(BaseMessages.getString(PKG, "OBSOutputDialog.EndedLine.Label"));
    props.setLook(wlEndedLine);
    fdlEndedLine = new FormData();
    fdlEndedLine.left = new FormAttachment(0, 0);
    fdlEndedLine.top = new FormAttachment(wSplitEvery, 2 * margin);
    fdlEndedLine.right = new FormAttachment(middle, -margin);
    wlEndedLine.setLayoutData(fdlEndedLine);

    wEndedLine = new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wEndedLine);
    wEndedLine.addModifyListener(lsMod);
    fdEndedLine = new FormData();
    fdEndedLine.left = new FormAttachment(middle, 0);
    fdEndedLine.top = new FormAttachment(wSplitEvery, 2 * margin);
    fdEndedLine.right = new FormAttachment(100, 0);
    wEndedLine.setLayoutData(fdEndedLine);

    fdContentComp = new FormData();
    fdContentComp.left = new FormAttachment(0, 0);
    fdContentComp.top = new FormAttachment(0, 0);
    fdContentComp.right = new FormAttachment(100, 0);
    fdContentComp.bottom = new FormAttachment(100, 0);
    wContentComp.setLayoutData(fdContentComp);

    wContentComp.layout();
    wContentTab.setControl(wContentComp);

    // ///////////////////////////////////////////////////////////
    // / END OF CONTENT TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF CONTENT TAB///
    // /

    wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
    wFieldsTab.setText(BaseMessages.getString(PKG, "OBSOutputDialog.FieldsTab.Title"));

    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginWidth = Const.FORM_MARGIN;
    fieldsLayout.marginHeight = Const.FORM_MARGIN;

    Composite wFieldsComp = new Composite(wTabFolder, SWT.NONE);
    wFieldsComp.setLayout(fieldsLayout);
    props.setLook(wFieldsComp);

    wGet = new Button(wFieldsComp, SWT.PUSH);
    wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));

    setButtonPositions(new Button[] { wGet }, margin, null);

    final int fieldsCols = 10;
    final int fieldsRows = input.getOutputFields().length;

    colinf = new ColumnInfo[fieldsCols];
    colinf[0] = new ColumnInfo(BaseMessages.getString(PKG, "OBSOutputDialog.FieldsTable.Column.Name"), ColumnInfo.COLUMN_TYPE_CCOMBO, true);
    colinf[1] = new ColumnInfo(BaseMessages.getString(PKG, "OBSOutputDialog.FieldsTable.Column.Type"), ColumnInfo.COLUMN_TYPE_CCOMBO,
        ValueMetaFactory.getValueMetaNames(), true);
    colinf[2] = new ColumnInfo(BaseMessages.getString(PKG, "OBSOutputDialog.FieldsTable.Column.Format"), ColumnInfo.COLUMN_TYPE_FORMAT, 2);
    colinf[3] = new ColumnInfo(BaseMessages.getString(PKG, "OBSOutputDialog.FieldsTable.Column.Length"), ColumnInfo.COLUMN_TYPE_TEXT, false);
    colinf[4] = new ColumnInfo(BaseMessages.getString(PKG, "OBSOutputDialog.FieldsTable.Column.Precision"), ColumnInfo.COLUMN_TYPE_TEXT, false);
    colinf[5] = new ColumnInfo(BaseMessages.getString(PKG, "OBSOutputDialog.FieldsTable.Column.Currency"), ColumnInfo.COLUMN_TYPE_TEXT, false);
    colinf[6] = new ColumnInfo(BaseMessages.getString(PKG, "OBSOutputDialog.FieldsTable.Column.Decimal"), ColumnInfo.COLUMN_TYPE_TEXT, false);
    colinf[7] = new ColumnInfo(BaseMessages.getString(PKG, "OBSOutputDialog.FieldsTable.Column.Group"), ColumnInfo.COLUMN_TYPE_TEXT, false);
    colinf[8] = new ColumnInfo(BaseMessages.getString(PKG, "OBSOutputDialog.FieldsTable.Column.TrimType"), ColumnInfo.COLUMN_TYPE_CCOMBO,
        ValueMetaString.trimTypeDesc, true);
    colinf[9] = new ColumnInfo(BaseMessages.getString(PKG, "OBSOutputDialog.FieldsTable.Column.Null"), ColumnInfo.COLUMN_TYPE_TEXT, false);

    colinf[2].setComboValuesSelectionListener(new ComboValuesSelectionListener() {

      @Override
      public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
        String[] comboValues = new String[] {};
        int type = ValueMetaFactory.getIdForValueMeta(tableItem.getText(colNr - 1));
        switch (type) {
        case ValueMetaInterface.TYPE_DATE:
          comboValues = Const.getDateFormats();
          break;
        case ValueMetaInterface.TYPE_INTEGER:
        case ValueMetaInterface.TYPE_BIGNUMBER:
        case ValueMetaInterface.TYPE_NUMBER:
          comboValues = Const.getNumberFormats();
          break;
        default:
          break;
        }
        return comboValues;
      }
    });

    wFields = new TableView(transMeta, wFieldsComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, fieldsRows, lsMod, props);

    fdFields = new FormData();
    fdFields.left = new FormAttachment(0, 0);
    fdFields.top = new FormAttachment(0, 0);
    fdFields.right = new FormAttachment(100, 0);
    fdFields.bottom = new FormAttachment(wGet, -margin);
    wFields.setLayoutData(fdFields);

    //
    // Search the fields in the background
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        StepMeta stepMeta = transMeta.findStep(stepname);
        if (stepMeta != null) {
          try {
            RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);

            // Remember these fields...
            for (int i = 0; i < row.size(); i++) {
              inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
            }
            setComboBoxes();
          } catch (KettleException e) {
            logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
          }
        }
      }
    };
    new Thread(runnable).start();

    fdFieldsComp = new FormData();
    fdFieldsComp.left = new FormAttachment(0, 0);
    fdFieldsComp.top = new FormAttachment(0, 0);
    fdFieldsComp.right = new FormAttachment(100, 0);
    fdFieldsComp.bottom = new FormAttachment(100, 0);
    wFieldsComp.setLayoutData(fdFieldsComp);

    wFieldsComp.layout();
    wFieldsTab.setControl(wFieldsComp);

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment(0, 0);
    fdTabFolder.top = new FormAttachment(wStepname, margin);
    fdTabFolder.right = new FormAttachment(100, 0);
    fdTabFolder.bottom = new FormAttachment(100, -50);
    wTabFolder.setLayoutData(fdTabFolder);

    wTabFolder.setSelection(0);

    //
    //
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));

    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);

    // Add listeners
    lsGet = new Listener() {
      @Override
      public void handleEvent(Event e) {
        get();
      }
    };

    lsOK = new Listener() {
      @Override
      public void handleEvent(Event e) {
        ok();
      }
    };

    lsCancel = new Listener() {
      @Override
      public void handleEvent(Event e) {
        cancel();
      }
    };

    wGet.addListener(SWT.Selection, lsGet);
    wOK.addListener(SWT.Selection, lsOK);
    wCancel.addListener(SWT.Selection, lsCancel);

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        ok();
      }
    };

    wStepname.addSelectionListener(lsDef);
    wFilename.addSelectionListener(lsDef);
    wSeparator.addSelectionListener(lsDef);

    // Whenever something changes, set the tooltip to the expanded version:
    wFilename.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        wFilename.setToolTipText(transMeta.environmentSubstitute(wFilename.getText()));
      }
    });

    // Listen to the Browse... button
    wbFilename.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {

      }
    });

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      @Override
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    activeFileNameField();
    input.setChanged(changed);

    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void activeFileNameField() {
    wlFileNameField.setEnabled(wFileNameInField.getSelection());
    wFileNameField.setEnabled(wFileNameInField.getSelection());
    wlExtension.setEnabled(!wFileNameInField.getSelection());
    wExtension.setEnabled(!wFileNameInField.getSelection());
    wlFilename.setEnabled(!wFileNameInField.getSelection());
    wFilename.setEnabled(!wFileNameInField.getSelection());

    if (wFileNameInField.getSelection()) {
      if (!wDoNotOpenNewFileInit.getSelection()) {
        wDoNotOpenNewFileInit.setSelection(true);
      }
      wAddDate.setSelection(false);
      wAddTime.setSelection(false);
      wSpecifyFormat.setSelection(false);
      wAddStepnr.setSelection(false);
      wAddPartnr.setSelection(false);
    }

    wlDoNotOpenNewFileInit.setEnabled(!wFileNameInField.getSelection());
    wDoNotOpenNewFileInit.setEnabled(!wFileNameInField.getSelection());
    wlSpecifyFormat.setEnabled(!wFileNameInField.getSelection());
    wSpecifyFormat.setEnabled(!wFileNameInField.getSelection());

    wAddStepnr.setEnabled(!wFileNameInField.getSelection());
    wlAddStepnr.setEnabled(!wFileNameInField.getSelection());
    wAddPartnr.setEnabled(!wFileNameInField.getSelection());
    wlAddPartnr.setEnabled(!wFileNameInField.getSelection());
    if (wFileNameInField.getSelection()) {
      wSplitEvery.setText("0");
    }
    wSplitEvery.setEnabled(!wFileNameInField.getSelection());
    wlSplitEvery.setEnabled(!wFileNameInField.getSelection());
    if (wFileNameInField.getSelection()) {
      wEndedLine.setText("");
    }
    wEndedLine.setEnabled(!wFileNameInField.getSelection());
    wbShowFiles.setEnabled(!wFileNameInField.getSelection());
    wbFilename.setEnabled(!wFileNameInField.getSelection());

    setDateTimeFormat();
  }

  protected void setComboBoxes() {
    // Something was changed in the row.
    //
    final Map<String, Integer> fields = new HashMap<String, Integer>();

    // Add the currentMeta fields...
    fields.putAll(inputFields);

    Set<String> keySet = fields.keySet();
    List<String> entries = new ArrayList<String>(keySet);

    String[] fieldNames = entries.toArray(new String[entries.size()]);

    Const.sortStrings(fieldNames);
    colinf[0].setComboValues(fieldNames);
  }

  private void setDateTimeFormat() {

    // boolean specifyFormat = wSpecifyFormat.getSelection();
    // boolean fileNameInField = wFileNameInField.getSelection();

    if (wSpecifyFormat.getSelection()) {
      wAddDate.setSelection(false);
      wAddTime.setSelection(false);
    }

    wDateTimeFormat.setEnabled(wSpecifyFormat.getSelection() && !wFileNameInField.getSelection());
    wlDateTimeFormat.setEnabled(wSpecifyFormat.getSelection() && !wFileNameInField.getSelection());
    wAddDate.setEnabled(!(wFileNameInField.getSelection() || wSpecifyFormat.getSelection()));
    wlAddDate.setEnabled(!(wSpecifyFormat.getSelection() || wFileNameInField.getSelection()));
    wAddTime.setEnabled(!(wSpecifyFormat.getSelection() || wFileNameInField.getSelection()));
    wlAddTime.setEnabled(!(wSpecifyFormat.getSelection() || wFileNameInField.getSelection()));
  }

  private void setEncodings() {
    // Encoding of the text file:
    if (!gotEncodings) {
      gotEncodings = true;

      wEncoding.removeAll();
      List<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
      for (int i = 0; i < values.size(); i++) {
        Charset charSet = values.get(i);
        wEncoding.add(charSet.displayName());
      }

      // Now select the default!
      String defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
      int idx = Const.indexOfString(defEncoding, wEncoding.getItems());
      if (idx >= 0) {
        wEncoding.select(idx);
      }
    }
  }

  private void getFields() {
    if (!gotPreviousFields) {
      try {
        String field = wFileNameField.getText();
        RowMetaInterface r = transMeta.getPrevStepFields(stepname);
        if (r != null) {
          wFileNameField.setItems(r.getFieldNames());
        }
        if (field != null) {
          wFileNameField.setText(field);
        }
      } catch (KettleException ke) {
        new ErrorDialog(//
            shell, //
            BaseMessages.getString(PKG, "OBSOutputDialog.FailedToGetFields.DialogTitle"), //
            BaseMessages.getString(PKG, "OBSOutputDialog.FailedToGetFields.DialogMessage"), //
            ke//
        );
      }
      gotPreviousFields = true;
    }
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {

    wStepname.setText(stepname);

    wEndPoint.setText(Const.NVL(input.getEndPoint(), ""));
    wAccessKey.setText(Const.NVL(input.getAccessKey(), ""));
    wSecretKey.setText(Const.NVL(input.getSecretKey(), ""));
    wBucket.setText(Const.NVL(input.getBucket(), ""));
    wObjectKey.setText(Const.NVL(input.getObjectKey(), ""));

    wFilename.setText(Const.NVL(input.getFileName(), ""));

    wDoNotOpenNewFileInit.setSelection(input.isDoNotOpenNewFileInit());
    wExtension.setText(Const.NVL(input.getExtension(), ""));
    wSeparator.setText(Const.NVL(input.getSeparator(), ""));

    if (input.getFileFormat() != null) {
      wFormat.select(0); // default if not found: CR+LF
      for (int i = 0; i < TextFileOutputMeta.formatMapperLineTerminator.length; i++) {
        if (input.getFileFormat().equalsIgnoreCase(TextFileOutputMeta.formatMapperLineTerminator[i])) {
          wFormat.select(i);
        }
      }
    }
    wCompression.setText(Const.NVL(input.getFileCompression(), ""));
    wEncoding.setText(Const.NVL(input.getEncoding(), ""));
    wEndedLine.setText(Const.NVL(input.getEndedLine(), ""));
    wFileNameInField.setSelection(input.isFileNameInField());
    wFileNameField.setText(Const.NVL(input.getFileNameField(), ""));
    wSplitEvery.setText("" + input.getSplitEvery());
    wEnclForced.setSelection(input.isEnclosureForced());
    wHeader.setSelection(input.isHeaderEnabled());
    wFooter.setSelection(input.isFooterEnabled());
    wAddDate.setSelection(input.isDateInFilename());
    wAddTime.setSelection(input.isTimeInFilename());
    wDateTimeFormat.setText(Const.NVL(input.getDateTimeFormat(), ""));
    wSpecifyFormat.setSelection(input.isSpecifyingFormat());
    wAppend.setSelection(input.isFileAppended());
    wAddStepnr.setSelection(input.isStepNrInFilename());
    wAddPartnr.setSelection(input.isPartNrInFilename());
    wPad.setSelection(input.isPadded());
    wFastDump.setSelection(input.isFastDump());
    wAddToResult.setSelection(input.isAddToResultFiles());

    logDebug("getting fields info...");

    for (int i = 0; i < input.getOutputFields().length; i++) {
      TextFileField field = input.getOutputFields()[i];

      TableItem item = wFields.table.getItem(i);
      int colnr = 1;
      item.setText(colnr++, Const.NVL(field.getName(), ""));
      item.setText(colnr++, ValueMetaFactory.getValueMetaName(field.getType()));
      item.setText(colnr++, Const.NVL(field.getFormat(), ""));
      item.setText(colnr++, field.getLength() >= 0 ? Integer.toString(field.getLength()) : "");
      item.setText(colnr++, field.getPrecision() >= 0 ? Integer.toString(field.getPrecision()) : "");
      item.setText(colnr++, Const.NVL(field.getCurrencySymbol(), ""));
      item.setText(colnr++, Const.NVL(field.getDecimalSymbol(), ""));
      item.setText(colnr++, Const.NVL(field.getGroupingSymbol(), ""));
      item.setText(colnr++, Const.NVL(field.getTrimTypeDesc(), ""));
      item.setText(colnr++, Const.NVL(field.getNullString(), ""));
    }

    wFields.optWidth(true);
    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth(true);

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged(changed);
    input.setChanged(backupChanged);
    dispose();
  }

  private void getInfo(OBSOutputMeta meta) {

    meta.setEndPoint(wEndPoint.getText());
    meta.setAccessKey(wAccessKey.getText());
    meta.setSecretKey(wSecretKey.getText());
    meta.setBucket(wBucket.getText());
    meta.setObjectKey(wObjectKey.getText());

    meta.setFileName(wFilename.getText());
    meta.setFileName("obs://" + wAccessKey.getText() + ":" + wSecretKey.getText() + "@" + wEndPoint.getText() + "/" + wBucket.getText() + "/"
        + (wObjectKey.getText().indexOf('/') == 0 ? wObjectKey.getText().substring(1) : wObjectKey.getText()));

    meta.setFileAsCommand(false);
    meta.setDoNotOpenNewFileInit(wDoNotOpenNewFileInit.getSelection());
    meta.setFileFormat(TextFileOutputMeta.formatMapperLineTerminator[wFormat.getSelectionIndex()]);
    meta.setFileCompression(wCompression.getText());
    meta.setEncoding(wEncoding.getText());
    meta.setSeparator(wSeparator.getText());
    meta.setEnclosure(wEnclosure.getText());
    meta.setExtension(wExtension.getText());
    meta.setSplitEvery(Const.toInt(wSplitEvery.getText(), 0));
    meta.setEndedLine(wEndedLine.getText());

    meta.setFileNameField(wFileNameField.getText());
    meta.setFileNameInField(wFileNameInField.getSelection());

    meta.setEnclosureForced(wEnclForced.getSelection());
    meta.setHeaderEnabled(wHeader.getSelection());
    meta.setFooterEnabled(wFooter.getSelection());
    meta.setFileAppended(wAppend.getSelection());
    meta.setStepNrInFilename(wAddStepnr.getSelection());
    meta.setPartNrInFilename(wAddPartnr.getSelection());
    meta.setDateInFilename(wAddDate.getSelection());
    meta.setTimeInFilename(wAddTime.getSelection());
    meta.setDateTimeFormat(wDateTimeFormat.getText());
    meta.setSpecifyingFormat(wSpecifyFormat.getSelection());
    meta.setPadded(wPad.getSelection());
    meta.setAddToResultFiles(wAddToResult.getSelection());
    meta.setFastDump(wFastDump.getSelection());

    int nrfields = wFields.nrNonEmpty();
    meta.allocate(nrfields);

    for (int i = 0; i < nrfields; i++) {
      TextFileField field = new TextFileField();

      TableItem item = wFields.getNonEmpty(i);

      int colnr = 1;
      field.setName(item.getText(colnr++));
      field.setType(ValueMetaFactory.getIdForValueMeta(item.getText(colnr++)));
      field.setFormat(item.getText(colnr++));
      field.setLength(Const.toInt(item.getText(colnr++), -1));
      field.setPrecision(Const.toInt(item.getText(colnr++), -1));
      field.setCurrencySymbol(item.getText(colnr++));
      field.setDecimalSymbol(item.getText(colnr++));
      field.setGroupingSymbol(item.getText(colnr++));
      field.setTrimType(ValueMetaString.getTrimTypeByDesc(item.getText(colnr++)));

      (meta.getOutputFields())[i] = field;
    }
    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth(true);

    meta.setChanged();
  }

  private void ok() {
    if (Utils.isEmpty(wStepname.getText())) {
      return;
    }

    stepname = wStepname.getText(); // return value

    // copy info to OBSInputMeta class (input)
    getInfo(input);

    dispose();
  }

  private void get() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields(stepname);
      if (r != null) {
        TableItemInsertListener listener = new TableItemInsertListener() {
          @Override
          public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v) {
            if (v.isNumber()) {
              if (v.getLength() > 0) {
                int le = v.getLength();
                int pr = v.getPrecision();

                if (v.getPrecision() <= 0) {
                  pr = 0;
                }

                String mask = "";
                for (int m = 0; m < le - pr; m++) {
                  mask += "0";
                }
                if (pr > 0) {
                  mask += ".";
                }
                for (int m = 0; m < pr; m++) {
                  mask += "0";
                }
                tableItem.setText(3, mask);
              }
            }
            return true;
          }
        };
        BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] { 2 }, 4, 5, listener);
      }
    } catch (KettleException e) {
      new ErrorDialog(//
          shell, //
          BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), //
          BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), //
          e//
      );
    }
  }

  protected VariableSpace getVariableSpace() {
    return transMeta;
  }

  protected FileSystemOptions getFileSystemOptions() throws FileSystemException {
    FileSystemOptions opts = new FileSystemOptions();
    if (!Utils.isEmpty(wAccessKey.getText()) || !Utils.isEmpty(wSecretKey.getText())) {
      // create a FileSystemOptions with user & password
      StaticUserAuthenticator userAuthenticator = new StaticUserAuthenticator(//
          null, //
          getVariableSpace().environmentSubstitute(wAccessKey.getText()), //
          getVariableSpace().environmentSubstitute(wSecretKey.getText())//
      );
      DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, userAuthenticator);
    }
    return opts;
  }

}