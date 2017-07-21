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

package org.jbpm.workbench.ht.backend.server;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.workbench.ht.model.CommentSummary;
import org.jbpm.workbench.ht.model.TaskAssignmentSummary;
import org.jbpm.workbench.ht.model.TaskEventSummary;
import org.jbpm.workbench.ht.model.TaskSummary;
import org.jbpm.workbench.ht.service.TaskService;
import org.jbpm.workbench.ks.integration.AbstractKieServerService;
import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.client.UserTaskServicesClient;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Service
@ApplicationScoped
public class RemoteTaskServiceImpl extends AbstractKieServerService implements TaskService {

    @Inject
    private IdentityProvider identityProvider;

    @Override
    public TaskSummary getTask(String serverTemplateId,
                               String containerId,
                               Long taskId) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return null;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);
        try {
            TaskInstance task = client.getTaskInstance(containerId,
                                                       taskId);
            return new TaskSummaryMapper().apply(task);
        } catch (KieServicesException e) {
            // task not found
            return null;
        }
    }

    @Override
    public void updateTask(String serverTemplateId,
                           String containerId,
                           Long taskId,
                           Integer priority,
                           String description,
                           Date dueDate) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);
        // TODO update only when it actually changed
        client.setTaskDescription(containerId,
                                  taskId,
                                  description);
        client.setTaskPriority(containerId,
                               taskId,
                               priority);
        client.setTaskExpirationDate(containerId,
                                     taskId,
                                     dueDate);
    }

    @Override
    public void claimTask(String serverTemplateId,
                          String containerId,
                          Long taskId) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);

        client.claimTask(containerId,
                         taskId,
                         identityProvider.getName());
    }

    @Override
    public void releaseTask(String serverTemplateId,
                            String containerId,
                            Long taskId) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);

        client.releaseTask(containerId,
                           taskId,
                           identityProvider.getName());
    }

    @Override
    public void startTask(String serverTemplateId,
                          String containerId,
                          Long taskId) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);

        client.startTask(containerId,
                         taskId,
                         identityProvider.getName());
    }

    @Override
    public void completeTask(String serverTemplateId,
                             String containerId,
                             Long taskId,
                             Map<String, Object> output) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);

        client.completeTask(containerId,
                            taskId,
                            identityProvider.getName(),
                            output);
    }

    @Override
    public void resumeTask(String serverTemplateId,
                           String containerId,
                           Long taskId) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);

        client.resumeTask(containerId,
                          taskId,
                          identityProvider.getName());
    }

    @Override
    public void suspendTask(String serverTemplateId,
                            String containerId,
                            Long taskId) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);

        client.suspendTask(containerId,
                           taskId,
                           identityProvider.getName());
    }

    @Override
    public void saveTaskContent(String serverTemplateId,
                                String containerId,
                                Long taskId,
                                Map<String, Object> output) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);

        client.saveTaskContent(containerId,
                               taskId,
                               output);
    }

    @Override
    public void addTaskComment(String serverTemplateId,
                               String containerId,
                               Long taskId,
                               String text,
                               Date addedOn) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);

        client.addTaskComment(containerId,
                              taskId,
                              text,
                              identityProvider.getName(),
                              addedOn);
    }

    @Override
    public void deleteTaskComment(String serverTemplateId,
                                  String containerId,
                                  Long taskId,
                                  Long commentId) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);

        client.deleteTaskComment(containerId,
                                 taskId,
                                 commentId);
    }

    @Override
    public List<CommentSummary> getTaskComments(String serverTemplateId,
                                                String containerId,
                                                Long taskId) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return emptyList();
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);

        try {
            List<TaskComment> comments = client.getTaskCommentsByTaskId(containerId,
                                                                        taskId);
            return comments.stream().map(c -> build(c)).collect(toList());
        } catch (KieServicesException e) {
            // task not found
        }
        return emptyList();
    }

    @Override
    public List<TaskEventSummary> getTaskEvents(String serverTemplateId,
                                                String containerId,
                                                Long taskId) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return emptyList();
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);

        List<TaskEventInstance> events = client.findTaskEvents(containerId,
                                                               taskId,
                                                               0,
                                                               1000);

        return events.stream().map(e -> build(e)).collect(toList());
    }

    @Override
    public void delegate(String serverTemplateId,
                         String containerId,
                         Long taskId,
                         String entity) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);

        client.delegateTask(containerId,
                            taskId,
                            identityProvider.getName(),
                            entity);
    }

    @Override
    public TaskAssignmentSummary getTaskAssignmentDetails(String serverTemplateId,
                                                          String containerId,
                                                          Long taskId) {
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return null;
        }

        UserTaskServicesClient client = getClient(serverTemplateId,
                                                  UserTaskServicesClient.class);
        try {
            TaskInstance task = client.getTaskInstance(containerId,
                                                       taskId,
                                                       false,
                                                       false,
                                                       true);
            TaskAssignmentSummary summary = new TaskAssignmentSummary();
            summary.setTaskId(task.getId());
            summary.setActualOwner(task.getActualOwner());
            summary.setTaskName(task.getName());
            summary.setPotOwnersString(task.getPotentialOwners());
            summary.setCreatedBy(task.getCreatedBy());
            summary.setBusinessAdmins(task.getBusinessAdmins());
            summary.setStatus(task.getStatus());
            summary.setDelegationAllowed(isDelegationAllowed(task));
            return summary;
        } catch (KieServicesException e) {
            // task not found
            return null;
        }
    }

    protected Boolean isDelegationAllowed(final TaskInstance task) {

        if (task == null) {
            return false;
        }

        if ("Completed".equals(task.getStatus())) {
            return false;
        }

        final String actualOwner = task.getActualOwner();
        if (actualOwner != null && actualOwner.equals(identityProvider.getName())) {
            return true;
        }

        final String initiator = task.getCreatedBy();
        if (initiator != null && initiator.equals(identityProvider.getName())) {
            return true;
        }

        List<String> roles = identityProvider.getRoles();

        //TODO Needs to check if po or ba string is a group or a user
        final List<String> potentialOwners = task.getPotentialOwners();
        if (potentialOwners != null && Collections.disjoint(potentialOwners,
                                                            roles) == false) {
            return true;
        }

        final List<String> businessAdministrators = task.getBusinessAdmins();
        if (businessAdministrators != null && Collections.disjoint(businessAdministrators,
                                                                   roles) == false) {
            return true;
        }

        return false;
    }

    @Override
    public void executeReminderForTask(String serverTemplateId,
                                       String containerId,
                                       Long taskId,
                                       String fromUser) {

    }

    protected CommentSummary build(TaskComment comment) {
        CommentSummary summary = new CommentSummary(
                comment.getId(),
                comment.getText(),
                comment.getAddedBy(),
                comment.getAddedAt()
        );

        return summary;
    }

    protected TaskEventSummary build(TaskEventInstance event) {

        TaskEventSummary summary = new TaskEventSummary(
                event.getId(),
                event.getTaskId(),
                event.getType(),
                event.getUserId(),
                event.getWorkItemId(),
                event.getLogTime(),
                event.getMessage()
        );

        return summary;
    }
}