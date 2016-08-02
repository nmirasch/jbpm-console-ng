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

package org.jbpm.console.ng.cm.client.perspectives;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import org.jbpm.console.ng.cm.client.actions.CaseActionsPresenter;
import org.jbpm.console.ng.cm.client.activity.CaseActivitiesPresenter;
import org.jbpm.console.ng.cm.client.comments.CaseCommentsPresenter;
import org.jbpm.console.ng.cm.client.details.CaseDetailsPresenter;
import org.jbpm.console.ng.cm.client.file.CaseFilesPresenter;
import org.jbpm.console.ng.cm.client.milestones.CaseMilestonesPresenter;
import org.jbpm.console.ng.cm.client.roles.CaseRolesPresenter;
import org.jbpm.console.ng.cm.client.stages.CaseStagesPresenter;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.workbench.docks.UberfireDock;
import org.uberfire.client.workbench.docks.UberfireDockPosition;
import org.uberfire.client.workbench.docks.UberfireDocks;
import org.uberfire.client.workbench.panels.impl.SimpleWorkbenchPanelPresenter;
import org.uberfire.client.workbench.panels.impl.StaticWorkbenchPanelPresenter;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

@ApplicationScoped
@WorkbenchPerspective(identifier = CaseDetailsPerspective.PERSPECTIVE_ID)
public class CaseDetailsPerspective {

    public static final String PERSPECTIVE_ID = "Case Details";

    private final DefaultPlaceRequest milestones = new DefaultPlaceRequest(CaseMilestonesPresenter.SCREEN_ID);
    private final DefaultPlaceRequest activities = new DefaultPlaceRequest(CaseActivitiesPresenter.SCREEN_ID);
    private final DefaultPlaceRequest details = new DefaultPlaceRequest(CaseDetailsPresenter.SCREEN_ID);
    private final DefaultPlaceRequest actions = new DefaultPlaceRequest(CaseActionsPresenter.SCREEN_ID);
    private final DefaultPlaceRequest roles = new DefaultPlaceRequest(CaseRolesPresenter.SCREEN_ID);
    private final DefaultPlaceRequest stages = new DefaultPlaceRequest(CaseStagesPresenter.SCREEN_ID);
    private final DefaultPlaceRequest comments = new DefaultPlaceRequest(CaseCommentsPresenter.SCREEN_ID);
    private final DefaultPlaceRequest files = new DefaultPlaceRequest(CaseFilesPresenter.SCREEN_ID);

    @Inject
    private UberfireDocks uberfireDocks;

    @PostConstruct
    protected void init() {
        GWT.log("init");
        final UberfireDock milestonesDock = new UberfireDock(
                UberfireDockPosition.EAST,
                "FLAG_O",
                milestones,
                PERSPECTIVE_ID)
                .withSize(300).withLabel("Milestones");

        final UberfireDock caseActivitiesDock = new UberfireDock(
                UberfireDockPosition.EAST,
                "RSS",
                activities,
                PERSPECTIVE_ID)
                .withSize(300).withLabel("Activity");

        final UberfireDock caseCommentsDock = new UberfireDock(
                UberfireDockPosition.EAST,
                "COMMENTS",
                comments,
                PERSPECTIVE_ID)
                .withSize(300).withLabel("Comments");

        final UberfireDock caseFilesDock = new UberfireDock(
                UberfireDockPosition.EAST,
                "PAPERCLIP",
                files,
                PERSPECTIVE_ID)
                .withSize(300).withLabel("Files");

        uberfireDocks.add(milestonesDock, caseActivitiesDock, caseCommentsDock, caseFilesDock);
    }

    @Perspective
    public PerspectiveDefinition getPerspective() {
        GWT.log("getPerspective");
        final PerspectiveDefinition perspectiveDefinition = new PerspectiveDefinitionImpl(SimpleWorkbenchPanelPresenter.class.getName());
        perspectiveDefinition.setName("Case Details");

        perspectiveDefinition.getRoot().addPart(new PartDefinitionImpl(details));

//        final PanelDefinition details = new PanelDefinitionImpl(StaticWorkbenchPanelPresenter.class.getName());
//        details.setWidth(300);
//        details.setMinWidth(300);
//        details.addPart(new PartDefinitionImpl(this.details));
//        perspectiveDefinition.getRoot().insertChild(CompassPosition.NORTH, details);

        final PanelDefinition actions = new PanelDefinitionImpl(StaticWorkbenchPanelPresenter.class.getName());
        actions.setWidth(300);
        actions.setMinWidth(300);
        actions.addPart(new PartDefinitionImpl(this.actions));
        perspectiveDefinition.getRoot().insertChild(CompassPosition.WEST, actions);

        final PanelDefinition roles = new PanelDefinitionImpl(StaticWorkbenchPanelPresenter.class.getName());
        roles.setWidth(400);
        roles.setMinWidth(400);
        roles.addPart(new PartDefinitionImpl(this.roles));
        perspectiveDefinition.getRoot().insertChild(CompassPosition.EAST, roles);

        final PanelDefinition stages = new PanelDefinitionImpl(StaticWorkbenchPanelPresenter.class.getName());
        stages.setHeight(160);
        stages.setMinHeight(160);
        stages.addPart(new PartDefinitionImpl(this.stages));
        perspectiveDefinition.getRoot().insertChild(CompassPosition.SOUTH, stages);

        return perspectiveDefinition;
    }

    @OnStartup
    public void onStartup(final PlaceRequest place) {
        //TODO review OnStartup called twice
        GWT.log("CaseDetailsPerspective params1: " + place.getParameters());
        GWT.log("CaseDetailsPerspective params2: " + place.getParameterNames());

        addParameters(place, activities, milestones, details, actions, roles, stages, comments, files);
    }

    private void addParameters(final PlaceRequest startupPlace, final PlaceRequest... places) {
        for (PlaceRequest place : places) {
            place.getParameters().clear();
            for (String param : startupPlace.getParameterNames()) {
                place.addParameter(param, startupPlace.getParameter(param, null));
            }
        }
    }

}