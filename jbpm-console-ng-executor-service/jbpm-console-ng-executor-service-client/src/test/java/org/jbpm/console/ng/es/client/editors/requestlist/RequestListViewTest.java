/*
 * Copyright 2015 JBoss by Red Hat.
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
package org.jbpm.console.ng.es.client.editors.requestlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jbpm.console.ng.es.model.RequestSummary;
import org.jbpm.console.ng.gc.client.experimental.grid.base.ExtendedPagedTable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uberfire.ext.services.shared.preferences.GridColumnPreference;
import org.uberfire.ext.services.shared.preferences.GridPreferencesStore;
import org.uberfire.ext.services.shared.preferences.MultiGridPreferencesStore;
import org.uberfire.ext.widgets.common.client.tables.ColumnMeta;
import org.uberfire.ext.widgets.common.client.tables.FilterPagedTable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class )
public class RequestListViewTest {

    @Mock
    protected ExtendedPagedTable<RequestSummary> currentListGrid;

    @Mock
    FilterPagedTable filterPagedTableMock;

    @Mock
    MultiGridPreferencesStore multiGridPreferencesStoreMock;

    @Mock
    protected GridPreferencesStore gridPreferencesStoreMock;

    @InjectMocks
    private RequestListViewImpl view;

    @Test
    public void testDataStoreNameIsSet() {
        doAnswer( new Answer() {
            @Override
            public Void answer( InvocationOnMock invocationOnMock ) throws Throwable {
                final List<ColumnMeta> columns = (List<ColumnMeta>) invocationOnMock.getArguments()[ 0 ];
                for ( ColumnMeta columnMeta : columns ) {
                    assertNotNull( columnMeta.getColumn().getDataStoreName() );
                }
                return null;
            }
        } ).when( currentListGrid ).addColumns( anyList() );

        view.initColumns( currentListGrid );

        verify( currentListGrid ).addColumns( anyList() );
    }

    @Test
    public void setDefaultFilterTitleAndDescriptionTest() {
        when(filterPagedTableMock.getMultiGridPreferencesStore()).thenReturn(multiGridPreferencesStoreMock);
        view.resetDefaultFilterTitleAndDescription();

        verify(filterPagedTableMock, times(7)).getMultiGridPreferencesStore();

        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_0"), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_1"), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_2"), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_3"), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_4"), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_5"), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_6"), any(HashMap.class));

    }

    @Test
    public void initColumnsTest() {
        doAnswer( new Answer() {
            @Override
            public Void answer( InvocationOnMock invocationOnMock ) throws Throwable {
                final List<ColumnMeta> columns = (List<ColumnMeta>) invocationOnMock.getArguments()[ 0 ];
                assertTrue(columns.size()==6);
                return null;
            }
        } ).when( currentListGrid ).addColumns(anyList());

        ArrayList<GridColumnPreference> columnPreferences = new ArrayList<GridColumnPreference>();
        when(currentListGrid.getGridPreferencesStore()).thenReturn(gridPreferencesStoreMock);
        when(gridPreferencesStoreMock.getColumnPreferences()).thenReturn(columnPreferences);

        view.initColumns(currentListGrid);

        verify( currentListGrid ).addColumns(anyList());
    }

}
