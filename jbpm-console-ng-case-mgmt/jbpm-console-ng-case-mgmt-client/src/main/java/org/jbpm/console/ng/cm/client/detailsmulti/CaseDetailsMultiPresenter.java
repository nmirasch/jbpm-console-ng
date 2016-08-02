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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jbpm.console.ng.cm.client.resources.i18n.Constants;
import org.jbpm.console.ng.cm.model.events.CaseSelectedEvent;
import org.uberfire.client.annotations.DefaultPosition;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;
import org.uberfire.client.workbench.events.ChangeTitleWidgetEvent;
import org.uberfire.ext.widgets.common.client.menu.RefreshMenuBuilder;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.Position;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

@Dependent
@WorkbenchScreen(identifier = "Case Details Multi", preferredWidth = 500)
public class CaseDetailsMultiPresenter implements RefreshMenuBuilder.SupportsRefresh {

    @Inject
    private Event<ChangeTitleWidgetEvent> changeTitleWidgetEvent;

    @Inject
    private Event<CaseSelectedEvent> caseSelected;

    @Inject
    private CaseDetailsMultiView view;

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

    @DefaultPosition
    public Position getPosition() {
        return CompassPosition.EAST;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return Constants.INSTANCE.Case_Details();
    }

    @OnStartup
    public void onStartup(final PlaceRequest place) {
        this.place = place;
    }

    public void onCaseSelectedEvent(@Observes final CaseSelectedEvent event) {
        currentCaseId = event.getCaseId();

        changeTitleWidgetEvent.fire(new ChangeTitleWidgetEvent(this.place, currentCaseId));
    }

    @WorkbenchMenu
    public Menus buildMenu() {
        return MenuFactory
                .newTopLevelCustomMenu(new RefreshMenuBuilder(this)).endMenu()
                .build();
    }

    @Override
    public void onRefresh() {
        caseSelected.fire(new CaseSelectedEvent(currentCaseId));
    }

    public interface CaseDetailsMultiView extends UberView<CaseDetailsMultiPresenter> {
    }
}
