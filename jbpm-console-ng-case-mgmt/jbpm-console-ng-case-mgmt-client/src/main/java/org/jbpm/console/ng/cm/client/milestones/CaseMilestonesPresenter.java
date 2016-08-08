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

package org.jbpm.console.ng.cm.client.milestones;

import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.common.client.api.Caller;
import org.jbpm.console.ng.cm.client.resources.i18n.Constants;
import org.jbpm.console.ng.cm.model.CaseMilestoneSummary;
import org.jbpm.console.ng.cm.service.CaseInstanceService;
import org.uberfire.client.annotations.DefaultPosition;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.Position;

@Dependent
@WorkbenchScreen(identifier = CaseMilestonesPresenter.SCREEN_ID)
public class CaseMilestonesPresenter {

    public static final String SCREEN_ID = "Case Milestones";

    @Inject
    private View view;

    @Inject
    private Caller<CaseInstanceService> caseService;

    private String containerId;

    private String serverTemplateId;

    @PostConstruct
    public void init() {
        view.init(this);
    }

    @OnStartup
    public void onStartup(final PlaceRequest place) {
        containerId = place.getParameter("containerId", "");
        serverTemplateId = place.getParameter("serverTemplateId", "");
        view.clearAllMilestones();
        caseService.call((List<CaseMilestoneSummary> milestones) -> {
            for (CaseMilestoneSummary milestone : milestones) {
                if (milestone.isAchieved()) {
                    view.addArchivedMilestone(milestone.getName(), milestone.getAchievedAt());
                } else {
                    view.addMilestone(milestone.getName());
                }
            }
        }, new DefaultErrorCallback()).getCaseMilestones(serverTemplateId, containerId, "CASE-12345");
    }

    @WorkbenchPartView
    public UberView<CaseMilestonesPresenter> getView() {
        return view;
    }

    @WorkbenchPartTitle
    public String getTittle() {
        return Constants.INSTANCE.Milestones();
    }

    @DefaultPosition
    public Position getPosition() {
        return CompassPosition.WEST;
    }

    public interface View extends UberView<CaseMilestonesPresenter> {

        void clearAllMilestones();

        void addMilestone(String name);

        void addArchivedMilestone(String name, Date achievedAt);
    }
}