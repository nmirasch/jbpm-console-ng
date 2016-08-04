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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerType;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.PerspectiveCoordinator;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Text;
import org.jbpm.console.ng.cm.client.actions.CaseActionsPresenter;
import org.jbpm.console.ng.cm.client.activity.CaseActivitiesPresenter;
import org.jbpm.console.ng.cm.client.comments.CaseCommentsPresenter;
import org.jbpm.console.ng.cm.client.details.CaseDetailsPresenter;
import org.jbpm.console.ng.cm.client.file.CaseFilesPresenter;
import org.jbpm.console.ng.cm.client.milestones.CaseMilestonesPresenter;
import org.jbpm.console.ng.cm.client.roles.CaseRolesPresenter;
import org.jbpm.console.ng.cm.client.stages.CaseStagesPresenter;
import org.uberfire.ext.layout.editor.client.api.HasModalConfiguration;
import org.uberfire.ext.layout.editor.client.api.ModalConfigurationContext;
import org.uberfire.ext.layout.editor.client.api.RenderingContext;
import org.uberfire.ext.plugin.client.perspective.editor.api.PerspectiveEditorDragComponent;

import static org.jbpm.console.ng.cm.client.layouteditor.CaseManagementViewComponent.*;

@Dependent
public class CaseManagementDragComponent implements PerspectiveEditorDragComponent, HasModalConfiguration {

    @Inject
    CaseManagementEditorPopup editorPopup;

    @Inject
    PerspectiveCoordinator perspectiveCoordinator;

    @Inject
    CaseManagementViewComponent viewComponent;

//    @Inject
//    SyncBeanManager beanManager;

    @Override
    public Modal getConfigurationModal(final ModalConfigurationContext context) {
        editorPopup.setSaveCommand(() -> {
            context.setComponentProperty(PARAM_CASE_ID_COLUMN, editorPopup.getSelectedDataSetColumn());
            context.setComponentProperty(PARAM_DISPLAYER, editorPopup.getSelectedDisplayer());
            context.setComponentProperty(PARAM_PLACE_ID, editorPopup.getSelectedBuildingBlock());
            context.configurationFinished();
        });
        editorPopup.setCancelCommand(() -> context.configurationCancelled());
        editorPopup.setDisplayerSelectedCommand(displayerUUID -> editorPopup.addDataSetColumnOptions(getColumnsFromDisplayer(displayerUUID)));
        editorPopup.addDisplayerOptions(getAvailableDisplayers());
        editorPopup.addBuildingBlockOptions(getAvailableBuildingBlocks());
        return editorPopup;
    }

    private Set<String> getColumnsFromDisplayer(final String displayerUUID) {
        final List<Displayer> displayerList = perspectiveCoordinator.getDisplayerList();
        for (Displayer displayer : displayerList) {
            if (displayer.getDisplayerSettings().getUUID().equals(displayerUUID)) {
                return FluentIterable
                        .from(displayer.getDataSetHandler().getLastDataSet().getColumns())
                        .transform(c -> c.getId())
                        .toSet();
            }
        }
        return Collections.emptySet();
    }

    private Map<String, String> getAvailableDisplayers() {
        final List<Displayer> displayerList = perspectiveCoordinator.getDisplayerList();
        Map<String, String> displayers = new HashMap<>();
        for (Displayer displayer : displayerList) {
            final DisplayerSettings settings = displayer.getDisplayerSettings();
            if (settings.getType() == DisplayerType.TABLE) {
                final String value = settings.getTitle() == null ? settings.getUUID() : settings.getTitle();
                displayers.put(settings.getUUID(), value);
            }
        }
        return displayers;
    }

    private Set<String> getAvailableBuildingBlocks() {
        return Sets.newHashSet(
                CaseDetailsPresenter.SCREEN_ID,
                CaseMilestonesPresenter.SCREEN_ID,
                CaseRolesPresenter.SCREEN_ID,
                CaseActivitiesPresenter.SCREEN_ID,
                CaseActionsPresenter.SCREEN_ID,
                CaseCommentsPresenter.SCREEN_ID,
                CaseFilesPresenter.SCREEN_ID,
                CaseStagesPresenter.SCREEN_ID
        );
    }

    @Override
    public IsWidget getDragWidget() {
        final TextBox textBox = GWT.create(TextBox.class);
        textBox.setPlaceholder("jBPM Component");
        textBox.setReadOnly(true);
        return textBox;
    }

    @Override
    public IsWidget getPreviewWidget(RenderingContext renderingContext) {
        return new Text("Preview not available");
    }

    @Override
    public IsWidget getShowWidget(RenderingContext renderingContext) {
//        final CaseManagementViewComponent view = beanManager.lookupBean(CaseManagementViewComponent.class).newInstance();
        viewComponent.init(renderingContext.getComponent().getProperties());
        return viewComponent;
    }
}
