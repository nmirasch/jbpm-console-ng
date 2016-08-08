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
package org.jbpm.console.ng.cm.client.details;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import org.jboss.errai.common.client.api.Caller;
import org.jbpm.console.ng.cm.client.perspectives.CaseListPerspective;
import org.jbpm.console.ng.cm.client.resources.i18n.Constants;
import org.jbpm.console.ng.cm.model.CaseSummary;
import org.jbpm.console.ng.cm.model.events.CaseRefreshedEvent;
import org.jbpm.console.ng.cm.model.events.CaseSelectedEvent;
import org.jbpm.console.ng.cm.model.events.CaseUpdatedEvent;
import org.jbpm.console.ng.cm.service.CaseInstanceService;
import org.uberfire.client.annotations.DefaultPosition;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.menu.RefreshMenuBuilder;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.Position;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

@Dependent
@WorkbenchScreen(identifier = CaseDetailsPresenter.SCREEN_ID, preferredWidth = 500)
public class CaseDetailsPresenter implements RefreshMenuBuilder.SupportsRefresh {

    public static final String SCREEN_ID = "Case Details Screen";

    private Constants constants = Constants.INSTANCE;

    @Inject
    private CaseDetailsView view;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private Caller<CaseInstanceService> casesService;

    @Inject
    private Event<CaseSelectedEvent> caseSelected;

    private String currentCaseId = "";

    @PostConstruct
    public void init() {
        view.init(this);
    }

    @OnStartup
    public void onStartup(final PlaceRequest place) {
        currentCaseId = place.getParameter("caseId", null);
        refreshCase();
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return Constants.INSTANCE.Case_Details();
    }

    @WorkbenchPartView
    public UberView<CaseDetailsPresenter> getView() {
        return view;
    }

    @DefaultPosition
    public Position getPosition() {
        return CompassPosition.WEST;
    }

    protected void refreshCase() {
        view.setCaseId("");
        view.setCaseStatus("");
        view.setCaseDescription("");
        if (currentCaseId == null) {
            return;
        }
        casesService.call((CaseSummary summary) -> {
            view.setCaseId(summary.getCaseId());
            view.setCaseStatus(summary.getStatus().name());
            view.setCaseDescription(summary.getDescription());
        }, new DefaultErrorCallback()).getCaseInstance(null, null, currentCaseId);
    }

    public void onCaseSelectionEvent(@Observes final CaseSelectedEvent event) {
        this.currentCaseId = event.getCaseId();
        refreshCase();
    }

    public void onCaseRefreshedEvent(@Observes final CaseRefreshedEvent event) {
        if (currentCaseId == event.getCaseId()) {
            refreshCase();
        }
    }

    public void onCaseUpdateEvent(@Observes final CaseUpdatedEvent event){
        if (currentCaseId == event.getCaseId()) {
            refreshCase();
        }
    }

    @WorkbenchMenu
    public Menus buildMenu() {
        return MenuFactory
                .newTopLevelCustomMenu(new RefreshMenuBuilder(this))
                .endMenu()
                .newTopLevelMenu(constants.Close())
                .respondsWith(() -> placeManager.goTo(CaseListPerspective.PERSPECTIVE_ID))
                .endMenu()
                .build();
    }

    @Override
    public void onRefresh() {
        caseSelected.fire(new CaseSelectedEvent(currentCaseId));
    }

    public interface CaseDetailsView extends UberView<CaseDetailsPresenter> {

        void setCaseDescription(String text);

        void setCaseStatus(String status);

        void setCaseId(String caseId);

    }

}