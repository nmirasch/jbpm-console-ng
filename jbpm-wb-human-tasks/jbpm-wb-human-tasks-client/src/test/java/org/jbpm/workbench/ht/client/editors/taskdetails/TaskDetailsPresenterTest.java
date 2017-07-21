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

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jbpm.workbench.ht.model.TaskSummary;
import org.jbpm.workbench.ht.model.events.TaskSelectionEvent;
import org.jbpm.workbench.ht.service.TaskService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.uberfire.mocks.CallerMock;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(GwtMockitoTestRunner.class)
public class TaskDetailsPresenterTest {

    private CallerMock<TaskService> callerMock;

    @Mock
    private TaskService taskService;

    @Mock
    private TaskDetailsPresenter.TaskDetailsView viewMock;

    private TaskDetailsPresenter presenter;

    @Before
    public void setup() {
        callerMock = new CallerMock<TaskService>(taskService);
        presenter = new TaskDetailsPresenter(viewMock,
                                             callerMock);
    }

    @Test
    public void disableTaskDetailEditionTest() {
        presenter.setReadOnlyTaskDetail();
        verifyReadOnlyMode(1,
                           false);
    }

    @Test
    public void testSetTaskDetails() {
        TaskSummary taskSummary = TaskSummary.builder()
                .expirationTime(new Date())
                .actualOwner("actualOwner")
                .status("ready")
                .priority(2)
                .build();
        presenter.setTaskDetails(taskSummary);
        verifySetTaskDetails(taskSummary);
        verifyReadOnlyMode(0,
                           false);
    }

    @Test
    public void testSetTaskCompletedDetailsReadOnly() {
        TaskSummary taskSummary = TaskSummary.builder()
                .expirationTime(new Date())
                .actualOwner("actualOwner")
                .status("Completed")
                .priority(2)
                .build();
        presenter.setTaskDetails(taskSummary);
        verifySetTaskDetails(taskSummary);
        verifyReadOnlyMode(1,
                           false);
    }

    @Test
    public void testSetTaskNullDetailsReadOnly() {
        presenter.setTaskDetails(null);
        verifyReadOnlyMode(1,
                           false);
        verifyNoMoreInteractions(viewMock);
    }

    @Test
    public void testTaskSelectionWithNoPersistedTask() {
        String serverTemplateId = "serverTemplateId";
        String containerId = "containerId";
        Long taskId = 1L;
        boolean isForLog = false;
        TaskSelectionEvent event = new TaskSelectionEvent(serverTemplateId,
                                                          containerId,
                                                          taskId,
                                                          "task",
                                                          true,
                                                          isForLog,
                                                          "description",
                                                          new Date(),
                                                          "Completed",
                                                          "actualOwner",
                                                          2,
                                                          1L,
                                                          "processId");
        when(taskService.getTask(serverTemplateId,
                                 containerId,
                                 taskId)).thenReturn(null);
        presenter.onTaskSelectionEvent(event);

        verifyCurrentTaskSummaryFieldsMatch(event,
                                            presenter.getCurrentTaskSummary());
        verify(taskService).getTask(serverTemplateId,
                                    containerId,
                                    taskId);
        verifySetTaskDetails(presenter.getCurrentTaskSummary());
    }

    @Test
    public void testTaskSelectionWithPersistedTask() {
        String serverTemplateId = "serverTemplateId";
        String containerId = "containerId";
        Long taskId = 1L;
        boolean isForLog = false;
        TaskSelectionEvent event = new TaskSelectionEvent(serverTemplateId,
                                                          containerId,
                                                          taskId,
                                                          "task",
                                                          true,
                                                          isForLog,
                                                          "description",
                                                          new Date(),
                                                          "Completed",
                                                          "actualOwner",
                                                          2,
                                                          1L,
                                                          "processId");
        TaskSummary taskSummary = TaskSummary.builder()
                .id(1L)
                .expirationTime(new Date())
                .actualOwner("actualOwner2")
                .status("Ready")
                .priority(1)
                .build();
        when(taskService.getTask(serverTemplateId,
                                 containerId,
                                 taskId)).thenReturn(taskSummary);
        presenter.onTaskSelectionEvent(event);

        assertEquals(taskSummary.getId(),
                     presenter.getCurrentTaskSummary().getId());
        assertEquals(taskSummary.getName(),
                     presenter.getCurrentTaskSummary().getName());
        assertEquals(taskSummary.getDescription(),
                     presenter.getCurrentTaskSummary().getDescription());
        assertEquals(taskSummary.getExpirationTime(),
                     presenter.getCurrentTaskSummary().getExpirationTime());
        assertEquals(taskSummary.getActualOwner(),
                     presenter.getCurrentTaskSummary().getActualOwner());
        assertEquals(taskSummary.getPriority(),
                     presenter.getCurrentTaskSummary().getPriority());
        assertEquals(taskSummary.getStatus(),
                     presenter.getCurrentTaskSummary().getStatus());

        verify(taskService).getTask(serverTemplateId,
                                    containerId,
                                    taskId);
        verifySetTaskDetails(presenter.getCurrentTaskSummary());
    }

    private void verifyCurrentTaskSummaryFieldsMatch(TaskSelectionEvent event,
                                                     TaskSummary taskSummary) {
        assertEquals(event.getTaskId(),
                     taskSummary.getId());
        assertEquals(event.getTaskName(),
                     taskSummary.getName());
        assertEquals(event.getDescription(),
                     taskSummary.getDescription());
        assertEquals(event.getExpirationTime(),
                     taskSummary.getExpirationTime());
        assertEquals(event.getActualOwner(),
                     taskSummary.getActualOwner());
        assertEquals(event.getPriority(),
                     taskSummary.getPriority());
        assertEquals(event.getStatus(),
                     taskSummary.getStatus());
    }

    private void verifySetTaskDetails(TaskSummary taskSummary) {
        verify(viewMock).setDueDate(any());
        verify(viewMock).setDueDateTime(any());
        verify(viewMock).setUser(taskSummary.getActualOwner());
        verify(viewMock).setTaskStatus(taskSummary.getStatus());
        verify(viewMock).setTaskPriority(String.valueOf(taskSummary.getPriority()));
    }

    private void verifyReadOnlyMode(int i,
                                    boolean readOnly) {
        verify(viewMock,
               times(i)).setTaskDescriptionEnabled(readOnly);
        verify(viewMock,
               times(i)).setDueDateEnabled(readOnly);
        verify(viewMock,
               times(i)).setDueDateTimeEnabled(readOnly);
        verify(viewMock,
               times(i)).setTaskPriorityEnabled(readOnly);
        verify(viewMock,
               times(i)).setUpdateTaskVisible(readOnly);
    }
}