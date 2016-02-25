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
package org.jbpm.console.ng.ht.client.editors.taskdetailsmulti;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class )
public class TaskDetailsMultiViewImplTest {

    @Mock
    private TaskDetailsMultiPresenter presenterMock;

    @InjectMocks
    private TaskDetailsMultiViewImpl taskDetailsMultiView;

    @Test
    public void displayOnlyLogTabTest() {
        taskDetailsMultiView.init( presenterMock );

        taskDetailsMultiView.displayOnlyLogTab();
        verify( presenterMock ).disableTaskDetailsEdition();
    }

    @Test
    public void initTabsTest() {
        taskDetailsMultiView.init( presenterMock );

        verify( presenterMock ).getGenericFormView();
        verify( presenterMock ).getTaskDetailsView();
        verify( presenterMock ).getProcessContextView();
        verify( presenterMock ).getTaskAssignmentsView();
        verify( presenterMock ).getTaskCommentsView();
        verify( presenterMock ).getTaskAdminView();
        verify( presenterMock ).getTaskLogsView();
    }

}
