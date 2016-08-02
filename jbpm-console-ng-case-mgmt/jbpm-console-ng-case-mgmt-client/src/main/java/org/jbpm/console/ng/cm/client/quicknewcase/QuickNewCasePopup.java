/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.console.ng.cm.client.quicknewcase;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.security.shared.api.identity.User;
import org.jbpm.console.ng.cm.client.resources.i18n.Constants;
import org.jbpm.console.ng.cm.model.events.CaseCreatedEvent;
import org.jbpm.console.ng.cm.service.CaseInstanceService;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.footers.GenericModalFooter;
import org.uberfire.workbench.events.NotificationEvent;

@Dependent
public class QuickNewCasePopup extends BaseModal {

    private static Binder uiBinder = GWT.create(Binder.class);

//    @UiField
//    TextBox deploymentIdText;
//
//    @UiField
//    FormGroup deploymentIdControlGroup;

//    @UiField
//    HelpBlock deploymentIdHelpLabel;

    @UiField
    TextBox caseNameText;

    @UiField
    FormGroup caseNameControlGroup;

    @UiField
    HelpBlock caseNameHelpLabel;

    @UiField
    HelpBlock errorMessages;

//    @UiField
//    ListBox caseTemplatesListBox;

    @UiField
    FormGroup errorMessagesGroup;

    @Inject
    User identity;

    @Inject
    Caller<CaseInstanceService> caseService;

    @Inject
    private Event<NotificationEvent> notification;

    @Inject
    private Event<CaseCreatedEvent> newCaseEvent;

    private HandlerRegistration textKeyPressHandler;

    private String serverTemplateId;

    public QuickNewCasePopup() {
        setTitle(Constants.INSTANCE.New_Case_Instance());
        final ModalBody modalBody = GWT.create(ModalBody.class);
        modalBody.add(uiBinder.createAndBindUi(this));
        add(modalBody);
        final GenericModalFooter footer = new GenericModalFooter();
        footer.addButton(
                Constants.INSTANCE.Create(),
                () -> okButton(),
                IconType.PLUS,
                ButtonType.PRIMARY
        );
        add(footer);
        init();
    }

    public void show(final String serverTemplateId) {
        this.serverTemplateId = serverTemplateId;
        cleanForm();
        super.show();
    }

    private void okButton() {
        if (validateForm()) {
            createCaseInstance();
        }
    }

    public void init() {
        KeyPressHandler keyPressHandlerText = new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                clearErrorMessages();
                if (event.getNativeEvent().getKeyCode() == 13) {
                    createCaseInstance();
                }
            }
        };
        textKeyPressHandler = caseNameText.addKeyPressHandler(keyPressHandlerText);

        caseNameText.setFocus(true);

//        caseTemplatesListBox.addItem("Select Case Template ...", "");
//        caseTemplatesListBox.addItem("Ad Hoc Template", "org.jbpm.empty.adhoc");
    }

    public void cleanForm() {
        caseNameText.setValue("");
        caseNameText.setFocus(true);
        clearErrorMessages();
    }

    public void closePopup() {
        cleanForm();
        hide();
        super.hide();
    }

    private boolean validateForm() {
        boolean valid = true;
        clearErrorMessages();

        if (caseNameText.getText() != null && caseNameText.getText().trim().length() == 0) {
            caseNameText.setFocus(true);
            errorMessages.setText(Constants.INSTANCE.Case_Must_Have_A_Name());
            errorMessagesGroup.setValidationState(ValidationState.ERROR);
            caseNameHelpLabel.setText(Constants.INSTANCE.Case_Must_Have_A_Name());
            caseNameControlGroup.setValidationState(ValidationState.ERROR);
            valid = false;
        } else {
            caseNameControlGroup.setValidationState(ValidationState.SUCCESS);
        }
        return valid;
    }

    public void displayNotification(final String text) {
        notification.fire(new NotificationEvent(text));
    }

    private void createCaseInstance() {
        textKeyPressHandler.removeHandler();

        if ("".equals(caseNameText.getText())) {
            errorMessages.setText(Constants.INSTANCE.Provide_Case_Name());
            errorMessagesGroup.setValidationState(ValidationState.ERROR);
        } else {
            createCase(caseNameText.getText());
        }

    }

    public void createCase(final String name) {
        caseService.call((String caseId) -> {
            cleanForm();
            refreshCaseTask(caseId, Constants.INSTANCE.CaseCreatedWithId(caseId));
        }, new ErrorCallback<Message>() {
            @Override
            public boolean error(Message message, Throwable throwable) {
                errorMessages.setText(throwable.getMessage());
                errorMessagesGroup.setValidationState(ValidationState.ERROR);
                return true;
            }
        }).createCaseInstance(serverTemplateId, null, name);
    }

    private void refreshCaseTask(String caseId, String message) {
        displayNotification(message);
        newCaseEvent.fire(new CaseCreatedEvent(caseId));
        closePopup();
    }

    private void clearErrorMessages() {
        errorMessages.setText("");
        caseNameHelpLabel.setText("");
//        deploymentIdHelpLabel.setText("");
        caseNameControlGroup.setValidationState(ValidationState.NONE);
    }

    interface Binder extends UiBinder<Widget, QuickNewCasePopup> {
    }

}