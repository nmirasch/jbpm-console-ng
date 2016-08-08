/*
 * Copyright 2012 JBoss Inc
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
package org.jbpm.console.ng.cm.client.detailsmulti;

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jbpm.console.ng.cm.client.actions.CaseActionsPresenter;
import org.jbpm.console.ng.cm.client.activity.CaseActivitiesPresenter;
import org.jbpm.console.ng.cm.client.comments.CaseCommentsPresenter;
import org.jbpm.console.ng.cm.client.details.CaseDetailsPresenter;
import org.jbpm.console.ng.cm.client.file.CaseFilesPresenter;
import org.jbpm.console.ng.cm.client.milestones.CaseMilestonesPresenter;
import org.jbpm.console.ng.cm.client.resources.i18n.Constants;
import org.jbpm.console.ng.cm.client.roles.CaseRolesPresenter;
import org.jbpm.console.ng.cm.client.stages.CaseStagesPresenter;
import org.jbpm.console.ng.cm.model.CaseSummary;
import org.jbpm.console.ng.cm.model.events.CaseUpdatedEvent;
import org.jbpm.console.ng.cm.service.CaseInstanceService;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

@Dependent
@WorkbenchScreen(identifier = CaseDetailsMultiPresenter.SCREEN_ID, preferredWidth = 500)
public class CaseDetailsMultiPresenter {

    public static final String SCREEN_ID = "Case Details Overview";

    @Inject
    private Caller<CaseInstanceService> casesService;

    @Inject
    private CaseDetailsMultiView view;

    @Inject
    private Event<CaseUpdatedEvent> caseUpdatedEvent;

    private PlaceRequest place;

    private String currentCaseId = "";

    @PostConstruct
    public void init() {
        view.init(this);
    }

    @WorkbenchPartView
    public UberView<CaseDetailsMultiPresenter> getView() {
        return view;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return Constants.INSTANCE.Case_Details();
    }

    @OnStartup
    public void onStartup(final PlaceRequest place) {
        this.place = place;
        currentCaseId = place.getParameter("caseId", null);
        refreshCase();
    }

    @OnOpen
    public void onOpen() {
        view.addCaseDetails(CaseDetailsPresenter.SCREEN_ID, place.getParameters());
        view.addCaseActions(CaseActionsPresenter.SCREEN_ID, place.getParameters());
        view.addCaseStages(CaseStagesPresenter.SCREEN_ID, place.getParameters());
        view.addCaseComments(CaseCommentsPresenter.SCREEN_ID, place.getParameters());
        view.addCaseFiles(CaseFilesPresenter.SCREEN_ID, place.getParameters());
        view.addCaseRoles(CaseRolesPresenter.SCREEN_ID, place.getParameters());
        view.addCaseMilestones(CaseMilestonesPresenter.SCREEN_ID, place.getParameters());
        view.addCaseActivities(CaseActivitiesPresenter.SCREEN_ID, place.getParameters());
    }

    protected void refreshCase() {
        view.setCaseTitle("");
        view.setCaseDescription("");
        if (currentCaseId == null) {
            return;
        }
        casesService.call((CaseSummary summary) -> {
            view.setCaseTitle(summary.getDescription());
            view.setCaseDescription(summary.getCaseId());
        }, new DefaultErrorCallback()).getCaseInstance(null, null, currentCaseId);
    }

    protected void terminateCase() {
        casesService.call((Void aVoid) -> {
            caseUpdatedEvent.fire(new CaseUpdatedEvent(currentCaseId));
            refreshCase();
        }, new DefaultErrorCallback()).terminateCaseInstance(null, null, currentCaseId);
    }

    protected void completeCase() {
        casesService.call((Void aVoid) -> {
            caseUpdatedEvent.fire(new CaseUpdatedEvent(currentCaseId));
            refreshCase();
        }, new DefaultErrorCallback()).completeCaseInstance(null, null, currentCaseId);
    }

    public interface CaseDetailsMultiView extends UberView<CaseDetailsMultiPresenter> {

        void setCaseTitle(String title);

        void setCaseDescription(String description);

        void addCaseDetails(String placeId, Map<String, String> properties);

        void addCaseStages(String placeId, Map<String, String> properties);

        void addCaseActions(String placeId, Map<String, String> properties);

        void addCaseComments(String placeId, Map<String, String> properties);

        void addCaseFiles(String placeId, Map<String, String> properties);

        void addCaseRoles(String placeId, Map<String, String> properties);

        void addCaseMilestones(String placeId, Map<String, String> properties);

        void addCaseActivities(String placeId, Map<String, String> properties);

    }

}