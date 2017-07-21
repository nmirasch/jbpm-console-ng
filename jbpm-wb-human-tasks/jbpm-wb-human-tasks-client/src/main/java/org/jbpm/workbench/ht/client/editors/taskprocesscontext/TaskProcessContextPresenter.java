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
package org.jbpm.workbench.ht.client.editors.taskprocesscontext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.shared.api.identity.User;
import org.jbpm.workbench.common.client.PerspectiveIds;
import org.jbpm.workbench.ht.model.TaskSummary;
import org.jbpm.workbench.ht.model.events.TaskRefreshedEvent;
import org.jbpm.workbench.ht.model.events.TaskSelectionEvent;
import org.jbpm.workbench.ht.service.TaskService;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.security.ResourceRef;
import org.uberfire.security.authz.AuthorizationManager;
import org.uberfire.workbench.model.ActivityResourceType;

@Dependent
public class TaskProcessContextPresenter {

    public User identity;

    private PlaceManager placeManager;

    private ActivityManager activityManager;

    private TaskProcessContextView view;

    private AuthorizationManager authorizationManager;

    private Caller<TaskService> taskService;

    private long currentTaskId = 0;

    private Long currentProcessInstanceId = -1L;

    private String currentProcessId = "";

    private String serverTemplateId;

    private String containerId;

    @Inject
    public TaskProcessContextPresenter(TaskProcessContextView view,
                                       PlaceManager placeManager,
                                       Caller<TaskService> taskService,
                                       ActivityManager activityManager,
                                       AuthorizationManager authorizationManager,
                                       User identity) {
        this.view = view;
        this.taskService = taskService;
        this.placeManager = placeManager;
        this.activityManager = activityManager;
        this.authorizationManager = authorizationManager;
        this.identity = identity;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        boolean enableProcessInstanceLink = false;
        if (hasAccessToPerspective(PerspectiveIds.PROCESS_INSTANCES) &&
                !activityManager.getActivities(new DefaultPlaceRequest(PerspectiveIds.PROCESS_INSTANCES)).isEmpty()) {
            enableProcessInstanceLink = true;
        }
        view.enablePIDetailsButton(enableProcessInstanceLink);
    }

    public IsWidget getView() {
        return view;
    }

    public void goToProcessInstanceDetails() {
        final PlaceRequest request = new DefaultPlaceRequest(PerspectiveIds.PROCESS_INSTANCES);
        request.addParameter(PerspectiveIds.SEARCH_PARAMETER_PROCESS_INSTANCE_ID,
                             currentProcessInstanceId.toString());
        placeManager.goTo(request);
    }

    public void refreshProcessContextOfTask() {
        taskService.call(new RemoteCallback<TaskSummary>() {
                             @Override
                             public void callback(TaskSummary details) {
                                 if (details != null) {
                                     currentProcessInstanceId = details.getProcessInstanceId();
                                     currentProcessId = details.getProcessId();
                                 }

                                 if (currentProcessInstanceId == null) {
                                     view.setProcessInstanceId("None");
                                     view.setProcessId("None");
                                     view.enablePIDetailsButton(false);
                                     return;
                                 }

                                 view.setProcessInstanceId(String.valueOf(currentProcessInstanceId));
                                 view.setProcessId(currentProcessId);
                             }
                         }
        ).getTask(serverTemplateId,
                  containerId,
                  currentTaskId);
    }

    boolean hasAccessToPerspective(String perspectiveId) {
        ResourceRef resourceRef = new ResourceRef(perspectiveId,
                                                  ActivityResourceType.PERSPECTIVE);
        return authorizationManager.authorize(resourceRef,
                                              identity);
    }

    public void onTaskSelectionEvent(@Observes final TaskSelectionEvent event) {
        this.currentTaskId = event.getTaskId();
        this.serverTemplateId = event.getServerTemplateId();
        this.containerId = event.getContainerId();
        this.currentProcessInstanceId = event.getProcessInstanceId();
        this.currentProcessId = event.getProcessId();
        refreshProcessContextOfTask();
    }

    public void onTaskRefreshedEvent(@Observes final TaskRefreshedEvent event) {
        if (currentTaskId == event.getTaskId()) {
            refreshProcessContextOfTask();
        }
    }

    public interface TaskProcessContextView extends UberView<TaskProcessContextPresenter> {

        void displayNotification(String text);

        void setProcessInstanceId(String none);

        void setProcessId(String none);

        void enablePIDetailsButton(boolean enable);
    }
}
