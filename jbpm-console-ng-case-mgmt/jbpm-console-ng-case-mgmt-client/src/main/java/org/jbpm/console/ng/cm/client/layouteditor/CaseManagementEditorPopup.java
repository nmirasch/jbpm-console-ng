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

package org.jbpm.console.ng.cm.client.layouteditor;

import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;
import org.uberfire.ext.widgets.common.client.common.popups.footers.ModalFooterOKCancelButtons;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

@Dependent
public class CaseManagementEditorPopup extends BaseModal {

    private static Binder uiBinder = GWT.create(Binder.class);
    @UiField
    Select displayerSelector;
    @UiField
    Select buildingBlockSelector;
    @UiField
    Select dataSetColumnSelector;
    private Command saveCommand;
    private Command cancelCommand;
    private ParameterizedCommand<String> displayerSelectedCommand;

    @PostConstruct
    public void init() {
        setTitle("jBPM Component settings editor");
        setBody(uiBinder.createAndBindUi(CaseManagementEditorPopup.this));
        add(new ModalFooterOKCancelButtons(
                () -> {
                    if (saveCommand != null) {
                        saveCommand.execute();
                    }
                    hide();
                },
                () -> {
                    if (cancelCommand != null) {
                        cancelCommand.execute();
                    }
                    hide();
                }
        ));
        displayerSelector.setTitle("Select a Displayer");
        displayerSelector.addValueChangeHandler(e -> {
            if (displayerSelectedCommand != null) {
                displayerSelectedCommand.execute(e.getValue());
            }
        });
    }

    public void addDisplayerOptions(final Map<String, String> displayers) {
        displayerSelector.clear();
        for (Map.Entry<String, String> displayer : displayers.entrySet()) {
            final Option option = GWT.create(Option.class);
            option.setValue(displayer.getKey());
            option.setText(displayer.getValue());
            displayerSelector.add(option);
        }
        displayerSelector.refresh();
    }

    public void addDataSetColumnOptions(final Set<String> dataSetColumns) {
        dataSetColumnSelector.clear();
        for (String column : dataSetColumns) {
            final Option option = GWT.create(Option.class);
            option.setText(column);
            dataSetColumnSelector.add(option);
        }
        dataSetColumnSelector.refresh();
    }

    public void addBuildingBlockOptions(final Set<String> buildingBlocks) {
        buildingBlockSelector.clear();
        for (String buildingBlock : buildingBlocks) {
            final Option option = GWT.create(Option.class);
            option.setText(buildingBlock);
            buildingBlockSelector.add(option);
        }
        buildingBlockSelector.refresh();
    }

    public String getSelectedDisplayer() {
        return displayerSelector.getValue();
    }

    public String getSelectedBuildingBlock() {
        return buildingBlockSelector.getValue();
    }

    public String getSelectedDataSetColumn() {
        return dataSetColumnSelector.getValue();
    }

    public void setSaveCommand(final Command saveCommand) {
        this.saveCommand = saveCommand;
    }

    public void setCancelCommand(final Command cancelCommand) {
        this.cancelCommand = cancelCommand;
    }

    public void setDisplayerSelectedCommand(ParameterizedCommand<String> displayerSelectedCommand) {
        this.displayerSelectedCommand = displayerSelectedCommand;
    }

    interface Binder extends UiBinder<Widget, CaseManagementEditorPopup> {
    }

}