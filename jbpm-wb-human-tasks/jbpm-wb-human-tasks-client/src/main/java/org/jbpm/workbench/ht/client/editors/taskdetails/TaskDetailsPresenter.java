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
package org.jbpm.workbench.ht.client.editors.taskdetails;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jbpm.workbench.common.client.util.UTCDateBox;
import org.jbpm.workbench.ht.client.resources.i18n.Constants;
import org.jbpm.workbench.ht.model.TaskSummary;
import org.jbpm.workbench.ht.model.events.TaskRefreshedEvent;
import org.jbpm.workbench.ht.model.events.TaskSelectionEvent;
import org.jbpm.workbench.ht.service.TaskService;

import static org.jbpm.workbench.common.client.util.TaskUtils.*;

@Dependent
public class TaskDetailsPresenter {

    TaskDetailsView view;

    private Constants constants = Constants.INSTANCE;

    @Inject
    private Caller<TaskService> taskService;

    private Event<TaskRefreshedEvent> taskRefreshed;

    private long currentTaskId = 0;

    private String currentServerTemplateId;

    private String currentContainerId;

    private TaskSummary currentTaskSummary;

    @Inject
    public TaskDetailsPresenter(
            TaskDetailsView view,
            Caller<TaskService> taskService) {
        this.view = view;
        this.taskService = taskService;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public IsWidget getView() {
        return view;
    }

    public void updateTask(final String taskDescription,
                           final String userId,
                           final Date dueDate,
                           final int priority) {

        if (currentTaskId > 0) {

            taskService.call(new RemoteCallback<Void>() {
                @Override
                public void callback(Void nothing) {
                    view.displayNotification(constants.TaskDetailsUpdatedForTaskId(currentTaskId));
                    taskRefreshed.fire(new TaskRefreshedEvent(currentTaskId));
                }
            }).updateTask(currentServerTemplateId,
                          currentContainerId,
                          currentTaskId,
                          priority,
                          taskDescription,
                          dueDate);
        }
    }

    protected TaskSummary getCurrentTaskSummary() {
        return currentTaskSummary;
    }

    public void refreshTask() {
        taskService.call((TaskSummary details) -> {
            if (details != null) {
                currentTaskSummary = details;
            }
            setTaskDetails(currentTaskSummary);
        }).getTask(currentServerTemplateId,
                   currentContainerId,
                   currentTaskId);

    }

    protected void setTaskDetails(TaskSummary details) {
        if (details == null) {
            setReadOnlyTaskDetail();
            return;
        }
        if (details.getStatus().equals(TASK_STATUS_COMPLETED)) {
            setReadOnlyTaskDetail();
        }

        view.setTaskDescription(details.getDescription());
        final Long date = UTCDateBox.date2utc(details.getExpirationTime());
        if (date != null) {
            view.setDueDate(date);
            view.setDueDateTime(date);
        }
        view.setUser(details.getActualOwner());
        view.setTaskStatus(details.getStatus());
        view.setTaskPriority(String.valueOf(details.getPriority()));
    }

    public void setReadOnlyTaskDetail() {
        view.setTaskDescriptionEnabled(false);
        view.setDueDateEnabled(false);
        view.setDueDateTimeEnabled(false);
        view.setTaskPriorityEnabled(false);
        view.setUpdateTaskVisible(false);
    }

    public void onTaskSelectionEvent(@Observes final TaskSelectionEvent event) {
        this.currentTaskId = event.getTaskId();
        this.currentServerTemplateId = event.getServerTemplateId();
        this.currentContainerId = event.getContainerId();
        this.currentTaskSummary =
                TaskSummary.builder()
                        .id(event.getTaskId())
                        .name(event.getTaskName())
                        .description(event.getDescription())
                        .expirationTime(event.getExpirationTime())
                        .actualOwner(event.getActualOwner())
                        .priority(event.getPriority())
                        .status(event.getStatus())
                        .build();
        refreshTask();
    }

    public void onTaskRefreshedEvent(@Observes final TaskRefreshedEvent event) {
        if (currentTaskId == event.getTaskId()) {
            refreshTask();
        }
    }

    public interface TaskDetailsView extends IsWidget {

        void init(final TaskDetailsPresenter presenter);

        void setTaskDescription(String text);

        void setTaskDescriptionEnabled(Boolean enabled);

        void setDueDate(Long date);

        void setDueDateEnabled(Boolean enabled);

        void setDueDateTime(Long time);

        void setDueDateTimeEnabled(Boolean enabled);

        void setUser(String user);

        void setTaskStatus(String status);

        void setTaskPriority(String priority);

        void setTaskPriorityEnabled(Boolean enabled);

        void setUpdateTaskVisible(Boolean enabled);

        void displayNotification(final String text);
    }
}
