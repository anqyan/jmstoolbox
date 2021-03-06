/*
 * Copyright (C) 2015 Denis Forveille titou10.titou10@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.titou10.jtb.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.config.gen.Properties.Property;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.sessiontype.SessionType;
import org.titou10.jtb.sessiontype.SessionTypeManager;
import org.titou10.jtb.ui.UIProperty;
import org.titou10.jtb.util.Utils;

/**
 * Dialog for creating or updating a new JTBSession
 * 
 * @author Denis Forveille
 *
 */
public class SessionAddOrEditDialog extends Dialog {

   private ConfigManager          cm;
   private SessionTypeManager     sessionTypeManager;
   private List<QManager>         queueManagers;
   private List<SessionType>      sessionTypes;
   private JTBSession             jtbSession;

   private QManager               queueManagerSelected;
   private SessionType            sessionTypeSelected;

   // Session data
   private String                 name;
   private String                 folder;

   private String                 host;
   private Integer                port;
   private String                 host2;
   private Integer                port2;
   private String                 host3;
   private Integer                port3;
   private String                 userId;
   private String                 password;
   private boolean                promptForCredentials;

   final private List<UIProperty> properties = new ArrayList<>();

   // Widgets
   private Text                   txtName;
   private Text                   txtFolder;

   private Text                   txtHost;
   private Text                   txtPort;

   private Label                  lblHost2;
   private Text                   txtHost2;
   private Text                   txtPort2;

   private Label                  lblHost3;
   private Text                   txtHost3;
   private Text                   txtPort3;

   private Text                   txtUserId;
   private Text                   txtPassword;
   private Button                 btnPromptForCredentials;

   // JFace objects
   private TabFolder              tabFolder;
   private TabItem                tabSession;
   private TabItem                tabProperties;

   private Table                  propertyTable;
   private TableColumn            propertyNameColumn;
   private TableColumn            propertyValueColumn;
   private Label                  lblSessionType;

   /**
    * @wbp.parser.constructor
    */
   // @Inject
   // public SessionAddOrEditDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell parentShell, ConfigManager cm) {
   public SessionAddOrEditDialog(Shell parentShell, ConfigManager cm, SessionTypeManager sessionTypeManager) {
      this(parentShell, cm, sessionTypeManager, null);
   }

   // Editing a JTBSession
   public SessionAddOrEditDialog(Shell parentShell,
                                 ConfigManager cm,
                                 SessionTypeManager sessionTypeManager,
                                 JTBSession jtbSession) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.cm = cm;
      this.sessionTypeManager = sessionTypeManager;
      this.queueManagers = cm.getRunningQManagers();
      this.sessionTypes = sessionTypeManager.getSessionTypes();
      this.jtbSession = jtbSession;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      if (jtbSession == null) {
         newShell.setText("Add Session");
      } else {
         newShell.setText("Update Session");
      }
   }

   @Override
   protected Point getInitialSize() {
      Point p = super.getInitialSize();
      return new Point(700, p.y + 20);// +20 to accomodate the text warning
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new FillLayout(SWT.HORIZONTAL));

      tabFolder = new TabFolder(container, SWT.NONE);

      // -----------------------------
      // Session Basic Information Tab
      // -----------------------------
      tabSession = new TabItem(tabFolder, SWT.NONE);
      tabSession.setText("Connection");

      Composite composite = new Composite(tabFolder, SWT.NONE);
      tabSession.setControl(composite);
      composite.setLayout(new GridLayout(1, false));

      // ----------
      // Definition
      // ----------

      Group gDefinition = new Group(composite, SWT.SHADOW_ETCHED_IN);
      gDefinition.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      gDefinition.setText("Definition");
      gDefinition.setLayout(new GridLayout(2, false));

      Label lblNewLabel6 = new Label(gDefinition, SWT.NONE);
      lblNewLabel6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
      lblNewLabel6.setText("Session Name");

      txtName = new Text(gDefinition, SWT.BORDER);
      txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
      txtName.setFocus();

      Label lblNewLabel3 = new Label(gDefinition, SWT.NONE);
      lblNewLabel3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
      lblNewLabel3.setText("Queue Manager");

      // http://stackoverflow.com/questions/34603707/have-a-way-to-set-the-length-of-jfaces-comboviewer
      GridData gdCvQueueManagers = new GridData(SWT.LEFT, SWT.CENTER, false, false);
      gdCvQueueManagers.verticalSpan = 1;
      gdCvQueueManagers.widthHint = 200;
      ComboViewer cvQueueManagers = new ComboViewer(gDefinition, SWT.READ_ONLY);
      Combo combo = cvQueueManagers.getCombo();
      combo.setLayoutData(gdCvQueueManagers);
      cvQueueManagers.setContentProvider(ArrayContentProvider.getInstance());
      cvQueueManagers.setLabelProvider(new QueueManagerLabelProvider());

      Label lblNewLabel16 = new Label(gDefinition, SWT.NONE);
      lblNewLabel16.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
      lblNewLabel16.setText("Folder");

      txtFolder = new Text(gDefinition, SWT.BORDER);
      txtFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

      // Session Type

      lblSessionType = new Label(gDefinition, SWT.NONE);
      lblSessionType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
      lblSessionType.setText("Session Type");

      GridLayout gl = new GridLayout(2, false);
      gl.marginLeft = -5; // DF: magic?
      gl.marginTop = -5;

      Composite c = new Composite(gDefinition, SWT.NONE);
      c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
      c.setLayout(gl);

      // http://stackoverflow.com/questions/34603707/have-a-way-to-set-the-length-of-jfaces-comboviewer
      GridData gdCvSessionType = new GridData(SWT.LEFT, SWT.CENTER, false, false);
      gdCvSessionType.verticalSpan = 1;
      gdCvSessionType.widthHint = 100;
      ComboViewer cvSessionType = new ComboViewer(c, SWT.READ_ONLY);
      Combo comboSessionType = cvSessionType.getCombo();
      comboSessionType.setLayoutData(gdCvSessionType);
      cvSessionType.setContentProvider(ArrayContentProvider.getInstance());
      cvSessionType.setLabelProvider(new SessionTypeLabelProvider());

      Button resetSessionType = new Button(c, SWT.NONE);
      resetSessionType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
      resetSessionType.setText("Clear");
      resetSessionType.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         sessionTypeSelected = null;
         comboSessionType.deselectAll();
         cvSessionType.refresh();
      }));

      // ----------
      // Server
      // ----------

      Group gServer = new Group(composite, SWT.SHADOW_ETCHED_IN);
      gServer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      gServer.setText("Servers");
      gServer.setLayout(new GridLayout(3, false));

      // Host / Port

      Label lblNewLabel8 = new Label(gServer, SWT.NONE);
      lblNewLabel8.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
      lblNewLabel8.setText("Host / Port");

      txtHost = new Text(gServer, SWT.BORDER);
      txtHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

      GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
      gd.widthHint = 35;
      txtPort = new Text(gServer, SWT.BORDER);
      txtPort.setLayoutData(gd);
      txtPort.setTextLimit(5);
      final Text txtPortFinal = txtPort;
      txtPort.addVerifyListener(new VerifyListener() {
         @Override
         public void verifyText(VerifyEvent e) {
            final String oldS = txtPortFinal.getText();
            final String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
            if (!newS.isEmpty()) {
               try {
                  Long.valueOf(newS);
               } catch (final NumberFormatException nfe) {
                  e.doit = false;
               }
            }
         }
      });

      // HA Group

      lblHost2 = new Label(gServer, SWT.NONE);
      lblHost2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
      lblHost2.setText("Host / Port (2)");

      txtHost2 = new Text(gServer, SWT.BORDER);
      txtHost2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

      GridData gd2 = new GridData(SWT.LEFT, SWT.CENTER, false, false);
      gd2.widthHint = 35;
      txtPort2 = new Text(gServer, SWT.BORDER);
      txtPort2.setLayoutData(gd2);
      txtPort2.setTextLimit(5);
      final Text txtPort2Final = txtPort2;
      txtPort2.addVerifyListener(new VerifyListener() {
         @Override
         public void verifyText(VerifyEvent e) {
            final String oldS = txtPort2Final.getText();
            final String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
            if (!newS.isEmpty()) {
               try {
                  Long.valueOf(newS);
               } catch (final NumberFormatException nfe) {
                  e.doit = false;
               }
            }
         }
      });

      lblHost3 = new Label(gServer, SWT.NONE);
      lblHost3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
      lblHost3.setText("Host / Port (3)");

      txtHost3 = new Text(gServer, SWT.BORDER);
      txtHost3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

      GridData gd3 = new GridData(SWT.LEFT, SWT.CENTER, false, false);
      gd3.widthHint = 35;
      txtPort3 = new Text(gServer, SWT.BORDER);
      txtPort3.setLayoutData(gd3);
      txtPort3.setTextLimit(5);
      final Text txtPort3Final = txtPort3;
      txtPort3.addVerifyListener(new VerifyListener() {
         @Override
         public void verifyText(VerifyEvent e) {
            final String oldS = txtPort3Final.getText();
            final String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
            if (!newS.isEmpty()) {
               try {
                  Long.valueOf(newS);
               } catch (final NumberFormatException nfe) {
                  e.doit = false;
               }
            }
         }
      });

      // ----------
      // Security
      // ----------

      Group gCredentials = new Group(composite, SWT.SHADOW_ETCHED_IN);
      gCredentials.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      gCredentials.setText("Security");
      gCredentials.setLayout(new GridLayout(2, false));

      // Userid /Password

      Label lblNewLabel5 = new Label(gCredentials, SWT.NONE);
      lblNewLabel5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
      lblNewLabel5.setText("User id");

      txtUserId = new Text(gCredentials, SWT.BORDER);
      txtUserId.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

      Label lblNewLabel2 = new Label(gCredentials, SWT.NONE);
      lblNewLabel2.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
      lblNewLabel2.setText("Password");

      txtPassword = new Text(gCredentials, SWT.BORDER | SWT.PASSWORD);
      txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

      new Label(gCredentials, SWT.NONE);
      btnPromptForCredentials = new Button(gCredentials, SWT.CHECK);
      btnPromptForCredentials.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
      btnPromptForCredentials.setText("Prompt for userid/password");

      // Warning
      new Label(gCredentials, SWT.NONE);
      GridData gd5 = new GridData(SWT.FILL, SWT.CENTER, true, false);
      gd5.horizontalIndent = -8; // DF: ???
      Composite cWarning = new Composite(gCredentials, SWT.NONE);
      cWarning.setLayoutData(gd5);
      cWarning.setLayout(new GridLayout(2, false));

      Label aaa = new Label(cWarning, SWT.NONE);
      aaa.setImage(SWTResourceManager.getImage(this.getClass(), "icons/error.png"));
      aaa.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, true, 1, 1));

      Label lblWarning = new Label(cWarning, SWT.WRAP);
      lblWarning
               .setText("If the userid/password are NOT saved in the session, the 'REST' and 'scripts' features of JMSToolBox will not work");
      lblWarning.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, true, 1, 1));

      cWarning.pack();
      cWarning.layout(true);

      // --------------
      // Properties Tab
      // --------------

      tabProperties = new TabItem(tabFolder, SWT.NONE);
      tabProperties.setText("Properties");

      final Composite composite1 = new Composite(tabFolder, SWT.NONE);
      tabProperties.setControl(composite1);
      composite1.setLayout(new GridLayout(1, false));

      // Properties TableViewer
      Composite composite4 = new Composite(composite1, SWT.NONE);
      composite4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

      TableColumnLayout tclComposite4 = new TableColumnLayout();
      composite4.setLayout(tclComposite4);

      final TableViewer tableViewer = new TableViewer(composite4, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
      propertyTable = tableViewer.getTable();
      propertyTable.setHeaderVisible(true);
      propertyTable.setLinesVisible(true);
      ColumnViewerToolTipSupport.enableFor(tableViewer);

      TableViewerColumn propertyRequiredColumnViewer = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn propertyRequiredColumn = propertyRequiredColumnViewer.getColumn();
      propertyRequiredColumn.setAlignment(SWT.CENTER);
      tclComposite4.setColumnData(propertyRequiredColumn, new ColumnPixelData(8, false, true));
      propertyRequiredColumnViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            UIProperty u = (UIProperty) element;
            return u.isRequired() ? " *" : null;
         }
      });

      TableViewerColumn propertyNameColumnViewer = new TableViewerColumn(tableViewer, SWT.NONE);
      propertyNameColumn = propertyNameColumnViewer.getColumn();
      propertyNameColumn.setAlignment(SWT.LEFT);
      tclComposite4.setColumnData(propertyNameColumn, new ColumnWeightData(3, true));
      propertyNameColumn.setText("Name");
      propertyNameColumnViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            UIProperty u = (UIProperty) element;
            return u.getName();
         }

         @Override
         public String getToolTipText(Object element) {
            UIProperty u = (UIProperty) element;
            return u.getToolTip();
         }
      });

      TableViewerColumn propertyKindColumnViewer = new TableViewerColumn(tableViewer, SWT.NONE);
      TableColumn propertyKindColumn = propertyKindColumnViewer.getColumn();
      propertyKindColumn.setAlignment(SWT.LEFT);
      tclComposite4.setColumnData(propertyKindColumn, new ColumnWeightData(1, true));
      propertyKindColumn.setText("Kind");
      // propertyKindColumnViewer.setEditingSupport(new NameValueDeleteSupport(tableViewer, properties));
      propertyKindColumnViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            UIProperty u = (UIProperty) element;
            return u.getKind().name();
         }
      });

      TableViewerColumn propertyValueColumnViewer = new TableViewerColumn(tableViewer, SWT.NONE);
      propertyValueColumn = propertyValueColumnViewer.getColumn();
      propertyValueColumn.setAlignment(SWT.LEFT);
      tclComposite4.setColumnData(propertyValueColumn, new ColumnWeightData(4, true));
      propertyValueColumn.setText("Value");
      propertyValueColumnViewer.setEditingSupport(new ValueEditingSupport(tableViewer));
      propertyValueColumnViewer.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            UIProperty u = (UIProperty) element;
            return u.getValue();
         }

         @Override
         public String getToolTipText(Object element) {
            UIProperty u = (UIProperty) element;
            return u.getToolTip();
         }
      });

      tableViewer.setContentProvider(ArrayContentProvider.getInstance());
      // newPropertyKindCombo.setItems(JMSPropertyKind.NAMES);

      if (jtbSession == null) {
         queueManagerSelected = queueManagers.get(0);
      } else {
         queueManagerSelected = jtbSession.getQm();

         SessionDef sessionDef = jtbSession.getSessionDef();

         txtName.setText(sessionDef.getName());
         if (sessionDef.getFolder() != null) {
            txtFolder.setText(sessionDef.getFolder());
         }

         txtHost.setText(sessionDef.getHost());
         txtPort.setText(String.valueOf(sessionDef.getPort()));

         if (sessionDef.getHost2() != null) {
            txtHost2.setText(sessionDef.getHost2());
         }
         if (sessionDef.getPort2() != null) {
            txtPort2.setText(String.valueOf(sessionDef.getPort2()));
         }
         if (sessionDef.getHost3() != null) {
            txtHost3.setText(sessionDef.getHost3());
         }
         if (sessionDef.getPort3() != null) {
            txtPort3.setText(String.valueOf(sessionDef.getPort3()));
         }

         if (sessionDef.getUserid() != null) {
            txtUserId.setText(sessionDef.getUserid());
         }
         if (sessionDef.getPassword() != null) {
            txtPassword.setText(sessionDef.getPassword());
         }

         sessionTypeSelected = sessionTypeManager.getSessionTypeFromSessionTypeName(sessionDef.getSessionType());

         btnPromptForCredentials.setSelection(Utils.isTrue(sessionDef.isPromptForCredentials()));
      }

      // ----------
      // Set values
      // ----------

      showMultipleHosts();
      populateProperties();

      tableViewer.setInput(properties);

      cvQueueManagers.setInput(queueManagers);
      ISelection qmSelected = new StructuredSelection(queueManagerSelected);
      cvQueueManagers.setSelection(qmSelected);

      cvSessionType.setInput(sessionTypes);
      if (sessionTypeSelected != null) {
         ISelection stSelected = new StructuredSelection(sessionTypeSelected);
         cvSessionType.setSelection(stSelected);
      }
      // --------
      // Behavior
      // --------

      // Save the selected QueueManager
      cvQueueManagers.addSelectionChangedListener((event) -> {
         IStructuredSelection sel = (IStructuredSelection) event.getSelection();
         queueManagerSelected = (QManager) sel.getFirstElement();
         // grpHA.setVisible(queueManagerSelected.supportsMultipleHosts());

         showMultipleHosts();
         populateProperties();
         tableViewer.setInput(properties);

         tableViewer.refresh();

         Utils.resizeTableViewer(tableViewer);
      });

      // Save the selected SessionType
      cvSessionType.addSelectionChangedListener((event) -> {
         IStructuredSelection sel = (IStructuredSelection) event.getSelection();
         if (!sel.isEmpty()) {
            sessionTypeSelected = (SessionType) sel.getFirstElement();
         }
      });

      // Delete key on Session Type Combo deselects the selection
      comboSessionType.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
         if (e.keyCode == SWT.DEL) {
            sessionTypeSelected = null;
            comboSessionType.deselectAll();
            cvSessionType.refresh();
         }
      }));

      Utils.resizeTableViewer(tableViewer);

      return container;
   }

   private void showMultipleHosts() {
      boolean enabled = queueManagerSelected.supportsMultipleHosts();

      lblHost2.setEnabled(enabled);
      txtHost2.setEnabled(enabled);
      txtPort2.setEnabled(enabled);
      lblHost3.setEnabled(enabled);
      txtHost3.setEnabled(enabled);
      txtPort3.setEnabled(enabled);

      if (!enabled) {
         host2 = null;
         txtHost2.setText("");
         port2 = null;
         txtPort2.setText("");
         host3 = null;
         txtHost3.setText("");
         port3 = null;
         txtPort3.setText("");
      }
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      if (jtbSession == null) {
         Button btnCancel = createButton(parent, IDialogConstants.OK_ID, "Create", true);
         btnCancel.setText("Create");
      } else {
         Button btnCancel = createButton(parent, IDialogConstants.OK_ID, "Update", true);
         btnCancel.setText("Update");
      }
      Button button = createButton(parent, IDialogConstants.CANCEL_ID, "Done", false);
      button.setText("Cancel");
   }

   @Override
   protected Control createButtonBar(final Composite parent) {
      Composite buttonBar = new Composite(parent, SWT.NONE);

      GridLayout layout = new GridLayout(3, false);
      layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
      layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
      layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
      layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
      buttonBar.setLayout(layout);

      GridData data = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
      buttonBar.setLayoutData(data);
      buttonBar.setFont(parent.getFont());

      // Help Button
      Button help = new Button(buttonBar, SWT.PUSH);
      help.setImage(SWTResourceManager.getImage(this.getClass(), "icons/help.png"));
      help.setToolTipText("Help");
      help.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         QManagerHelpDialog helpDialog = new QManagerHelpDialog(getShell(), queueManagerSelected.getHelpText());
         helpDialog.open();
      }));

      final GridData leftButtonData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
      leftButtonData.grabExcessHorizontalSpace = true;
      leftButtonData.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
      help.setLayoutData(leftButtonData);
      if (queueManagerSelected.getHelpText() == null) {
         help.setEnabled(false);
      }

      // Other buttons on the right
      final Control buttonControl = super.createButtonBar(buttonBar);
      buttonControl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

      return buttonBar;
   }

   @Override
   protected void okPressed() {

      // Session Name
      if (Utils.isEmpty(txtName.getText())) {
         tabFolder.setSelection(tabSession);
         txtName.setFocus();
         MessageDialog.openError(getShell(), "Validation error", "The session name is mandatory");
         return;
      } else {
         name = txtName.getText().trim();
      }

      // Check duplicate name when adding
      boolean duplicate = false;
      if (jtbSession == null) {
         // Adding a session
         if (cm.getSessionDefByName(name) != null) {
            duplicate = true;
         }
      } else {
         // Updating a session
         if (!(jtbSession.getName().equals(name))) {
            if (cm.getSessionDefByName(name) != null) {
               duplicate = true;
            }
         }
      }

      if (duplicate) {
         txtName.setFocus();
         MessageDialog.openError(getShell(), "Validation error", "A session with this name already exists");
         return;
      }

      // Folder
      if (Utils.isNotEmpty(txtFolder.getText())) {
         folder = txtFolder.getText().trim();
      }

      // Host Name
      if (Utils.isEmpty(txtHost.getText())) {
         tabFolder.setSelection(tabSession);
         txtHost.setFocus();
         MessageDialog.openError(getShell(), "Validation error", "The host name is mandatory");
         return;
      } else {
         host = txtHost.getText().trim();
      }

      // Port
      if (Utils.isEmpty(txtPort.getText())) {
         tabFolder.setSelection(tabSession);
         txtPort.setFocus();
         MessageDialog.openError(getShell(), "Validation error", "The port is mandatory");
         return;
      } else {
         port = Integer.valueOf(txtPort.getText());
      }

      // Host / Port 2
      if (Utils.isEmpty(txtHost2.getText()) && Utils.isNotEmpty(txtPort2.getText())) {
         tabFolder.setSelection(tabSession);
         txtHost2.setFocus();
         MessageDialog.openError(getShell(), "Validation error", "A port can not be specified without a host name");
         return;
      }

      if (Utils.isNotEmpty(txtHost2.getText())) {
         host2 = txtHost2.getText().trim();
      }

      if (Utils.isNotEmpty(txtPort2.getText())) {
         port2 = Integer.valueOf(txtPort2.getText());
      }

      // Host / Port 3
      if (Utils.isEmpty(txtHost3.getText()) && Utils.isNotEmpty(txtPort3.getText())) {
         tabFolder.setSelection(tabSession);
         txtHost3.setFocus();
         MessageDialog.openError(getShell(), "Validation error", "A port can not be specified without a host name");
         return;
      }
      if (Utils.isNotEmpty(txtHost3.getText())) {
         host3 = txtHost3.getText().trim();
      }
      if (Utils.isNotEmpty(txtPort3.getText())) {
         port3 = Integer.valueOf(txtPort3.getText());
      }

      // UserId
      if (!(txtUserId.getText().trim().isEmpty())) {
         userId = txtUserId.getText().trim();
      }

      // Password
      if (!(txtPassword.getText().trim().isEmpty())) {
         password = txtPassword.getText().trim();
      }

      // Prompt For Credentials
      promptForCredentials = btnPromptForCredentials.getSelection();

      // Validate properties
      for (UIProperty property : properties) {

         String name = property.getName().trim();
         String value = property.getValue();

         // Mandatory parameters
         if ((property.isRequired()) && (Utils.isEmpty(value))) {
            tabFolder.setSelection(tabProperties);
            // newPropertyName.setFocus();
            MessageDialog.openError(getShell(),
                                    "Validation error",
                                    "Property '" + property.getName() + "' is mandatory for this Queue manager");
            return;
         }

         // Check kind of parameter
         boolean ok = JMSPropertyKind.validateValue(property.getKind(), value);
         if (!ok) {
            tabFolder.setSelection(tabProperties);
            // newPropertyName.setFocus();
            MessageDialog.openError(getShell(),
                                    "Validation error",
                                    "Property '" + name + "' must be of kind '" + property.getKind() + "'");
            return;
         }
      }

      super.okPressed();
   }

   // -------
   // Helpers
   // -------

   private void populateProperties() {

      properties.clear();

      if (queueManagerSelected.getQManagerProperties() == null) {
         return;
      }

      for (QManagerProperty qmProperty : queueManagerSelected.getQManagerProperties()) {
         properties.add(new UIProperty(qmProperty));
      }

      if (jtbSession == null) {
         return;
      }
      SessionDef sessionDef = jtbSession.getSessionDef();
      if (sessionDef.getProperties() != null) {
         if (sessionDef.getProperties().getProperty() != null) {
            // For each property of the session
            for (Property property : sessionDef.getProperties().getProperty()) {
               // if it exist in the QM, add the value, drop the other
               for (UIProperty uiProperty : properties) {
                  if (uiProperty.getName().equals(property.getName())) {
                     uiProperty.setValue(property.getValue());
                     break;
                  }
               }
            }
         }
      }
      // Sort properties: required first, then populated
      Collections.sort(properties);
   }

   public class ValueEditingSupport extends EditingSupport {

      private final TableViewer viewer;
      private final CellEditor  editor;

      public ValueEditingSupport(TableViewer viewer) {
         super(viewer);
         this.viewer = viewer;
         this.editor = new TextCellEditor(viewer.getTable());
      }

      @Override
      protected CellEditor getCellEditor(Object element) {
         return editor;
      }

      @Override
      protected boolean canEdit(Object element) {
         return true;
      }

      @Override
      protected Object getValue(Object element) {
         String s = ((UIProperty) element).getValue();
         return (s == null) ? "" : s;
      }

      @Override
      protected void setValue(Object element, Object userInputValue) {
         ((UIProperty) element).setValue(String.valueOf(userInputValue));
         viewer.update(element, null);
      }
   }

   private final class QueueManagerLabelProvider extends LabelProvider {
      @Override
      public String getText(Object element) {
         return ((QManager) element).getName();
      }
   }

   private final class SessionTypeLabelProvider extends LabelProvider {
      @Override
      public String getText(Object element) {
         return ((SessionType) element).getName();
      }
   }

   // ----------------
   // Standard Getters
   // ----------------

   public String getName() {
      return name;
   }

   public String getFolder() {
      return folder;
   }

   public Integer getPort() {
      return port;
   }

   public String getHost() {
      return host;
   }

   public String getUserId() {
      return userId;
   }

   public String getPassword() {
      return password;
   }

   public QManager getQueueManagerSelected() {
      return queueManagerSelected;
   }

   public List<UIProperty> getProperties() {
      return properties;
   }

   public String getHost2() {
      return host2;
   }

   public Integer getPort2() {
      return port2;
   }

   public String getHost3() {
      return host3;
   }

   public Integer getPort3() {
      return port3;
   }

   public boolean isPromptForCredentials() {
      return promptForCredentials;
   }

   public SessionType getSessionTypeSelected() {
      return sessionTypeSelected;
   }

}
