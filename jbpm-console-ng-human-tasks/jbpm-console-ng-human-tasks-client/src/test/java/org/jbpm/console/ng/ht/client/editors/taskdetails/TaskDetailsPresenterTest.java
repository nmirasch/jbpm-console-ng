/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.console.ng.ht.client.editors.taskdetails;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.jbpm.console.ng.gc.client.util.UTCDateBox;
import org.jbpm.console.ng.gc.client.util.UTCTimeBox;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class )
public class TaskDetailsPresenterTest {

    @Mock
    private TaskDetailsViewImpl view;

    @Mock
    TextArea taskDescriptionTextAreaMock;

    @Mock
    Select taskPriorityListBoxMock;

    @Mock
    UTCDateBox dueDateMock;

    @Mock
    UTCTimeBox dueDateTimeMock;

    @Mock
    TextBox userTextMock;

    @Mock
    TextBox taskStatusTextMock;

    @Mock
    Button updateTaskButtonMock;

    @InjectMocks
    private TaskDetailsPresenter presenter;

    @Before
    public void setupMocks() {
        when( view.getTaskDescriptionTextArea() ).thenReturn( taskDescriptionTextAreaMock );
        when( view.getTaskPriorityListBox() ).thenReturn( taskPriorityListBoxMock );
        when( view.getDueDate() ).thenReturn( dueDateMock );
        when( view.getDueDateTime() ).thenReturn( dueDateTimeMock );
        when( view.getUserText() ).thenReturn( userTextMock );
        when( view.getTaskStatusText() ).thenReturn( taskStatusTextMock );
        when( view.getUpdateTaskButton() ).thenReturn( updateTaskButtonMock );
    }

    @Test
    public void disableTaskDetailEditionTest() {
        presenter.setReadOnlyTaskDetail();

        verify( view ).getTaskDescriptionTextArea();
        verify( view ).getDueDate();
        verify( view ).getUserText();
        verify( view ).getTaskStatusText();
        verify( view ).getDueDateTime();
        verify( view ).getTaskPriorityListBox();
        verify( view ).getUpdateTaskButton();

        verify( taskDescriptionTextAreaMock ).setEnabled( false );
        verify( dueDateMock ).setEnabled( false );
        verify( userTextMock ).setEnabled( false );
        verify( taskStatusTextMock ).setEnabled( false );
        verify( dueDateTimeMock ).setEnabled( false );
        verify( taskPriorityListBoxMock ).setEnabled( false );
        verify( updateTaskButtonMock ).setVisible( false );

    }

}
