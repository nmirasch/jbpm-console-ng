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

package org.jbpm.workbench.es.client.editors.requestlist;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jbpm.workbench.common.client.PerspectiveIds;
import org.jbpm.workbench.common.client.list.BreadcrumbAction;


import org.jbpm.workbench.common.client.list.FullScreenView;
import org.jbpm.workbench.es.client.i18n.Constants;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnOpen;

import static org.jbpm.workbench.es.client.editors.requestlist.RequestListPresenter.JOB_DETAIL_SCREEN_ID;

@Dependent
@WorkbenchScreen(identifier = PerspectiveIds.JOB_LIST_FULLSCREEN)
public class RequestListFullScreenPresenter {

    @Inject
    public FullScreenView view;

    @Inject
    RequestListPresenter jobListPresenter;

    @WorkbenchPartTitle
    public String getTitle() {
        return Constants.INSTANCE.Jobs();
    }

    @WorkbenchPartView
    public FullScreenView getView() {
        return view;
    }

    @OnOpen
    public void onOpen() {

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(JOB_DETAIL_SCREEN_ID,
                       PerspectiveIds.JOB_DETAILS_FULLSCREEN);
        view.addScreenview(PerspectiveIds.JOB_LIST_SCREEN,parameters);

        view.setBreadcrubTitle(Constants.INSTANCE.Jobs());
        view.addPrimaryAction(new BreadcrumbAction() {
            @Override
            public String label() {
                return Constants.INSTANCE.New_Job();
            }

            @Override
            public void execute() {
                jobListPresenter.getAddNewJobCommand().execute();
            }
        });

        view.addSubAction(new BreadcrumbAction() {
                                        @Override
                                        public String label() {
                                            return Constants.INSTANCE.New_Job();
                                        }

                                        @Override
                                        public void execute() {
                                            jobListPresenter.getAddNewJobCommand().execute();
                                        }
                                    }
        );
    }

}