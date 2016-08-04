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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.displayer.client.PerspectiveCoordinator;
import org.dashbuilder.renderer.client.table.TableDisplayer;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

@Dependent
public class CaseManagementViewComponent extends Composite {

    public static final String PARAM_PLACE_ID = "placeId";
    public static final String PARAM_CASE_ID_COLUMN = "caseIdColumn";
    public static final String PARAM_DISPLAYER = "displayerUUID";
    @Inject
    PerspectiveCoordinator perspectiveCoordinator;
    private FlowPanel panel = GWT.create(FlowPanel.class);
    @Inject
    private PlaceManager placeManager;

    @PostConstruct
    public void init() {
        initWidget(panel);
    }

    public void init(final Map<String, String> properties) {
        final String displayerUUID = properties.get(PARAM_DISPLAYER);
        final String caseIdColumn = properties.get(PARAM_CASE_ID_COLUMN);
        final String place = properties.get(PARAM_PLACE_ID);
        this.addAttachHandler(e -> {
            final List<Displayer> displayers = perspectiveCoordinator.getDisplayerList();
            for (Displayer displayer : displayers) {
                if (displayerUUID.equals(displayer.getDisplayerSettings().getUUID())) {
                    final TableDisplayer tableDisplayer = (TableDisplayer) displayer;
                    tableDisplayer.addOnCellSelectedCommand(() -> {
                        panel.clear();
                        final DataSet ds = tableDisplayer.getDataSetHandler().getLastDataSet();
                        final String caseId = ds.getValueAt(tableDisplayer.getSelectedCellRow(), caseIdColumn).toString();
                        final Map<String, String> p = new HashMap<>(properties);
                        p.put("caseId", caseId);
//                        tableDisplayer.getView().asWidget().addStyleName("hidden");
                        placeManager.goTo(new DefaultPlaceRequest(place, p), panel);
                    });
                }
            }
        });
    }

}