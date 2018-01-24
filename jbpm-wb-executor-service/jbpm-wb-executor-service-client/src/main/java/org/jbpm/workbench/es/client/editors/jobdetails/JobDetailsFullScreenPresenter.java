/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.workbench.es.client.editors.jobdetails;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jbpm.workbench.common.client.PerspectiveIds;

import org.jbpm.workbench.common.client.list.BreadcrumbAction;
import org.jbpm.workbench.common.client.list.FullScreenView;
import org.jbpm.workbench.es.client.i18n.Constants;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.events.ChangeTitleWidgetEvent;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.mvp.PlaceRequest;

@Dependent
@WorkbenchScreen(identifier = PerspectiveIds.JOB_DETAILS_FULLSCREEN)
public class JobDetailsFullScreenPresenter  {

    @Inject
    public FullScreenView view;

    @Inject
    protected PlaceManager placeManager;

    @WorkbenchPartTitle
    public String getTitle() {
        return Constants.INSTANCE.Job_Details();
    }

    @WorkbenchPartView
    public FullScreenView getView() {
        return view;
    }

    @OnOpen
    public void onOpen() {
        view.addScreenview(PerspectiveIds.JOB_DETAILS_SCREEN,null);

        view.addElementToPath(new BreadcrumbAction() {
            @Override
            public String label() {
                return Constants.INSTANCE.Jobs();
            }

            @Override
            public void execute() {
                placeManager.closePlace(PerspectiveIds.JOB_DETAILS_FULLSCREEN);
            }
        });

    }

    @SuppressWarnings("unused")
    private void onChangeTitleWidgetEvent(@Observes ChangeTitleWidgetEvent event) {
        final PlaceRequest place = event.getPlaceRequest();
        if(place.getIdentifier().equals(PerspectiveIds.JOB_DETAILS_SCREEN)){
            view.setBreadcrubTitle(event.getTitle());
        }
    }

}