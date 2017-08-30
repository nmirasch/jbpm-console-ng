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

package org.jbpm.workbench.pr.backend.server;

import java.util.Arrays;

import java.util.Date;
import java.util.List;

import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.workbench.common.model.QueryFilter;
import org.jbpm.workbench.pr.model.DocumentSummary;
import org.jbpm.workbench.pr.model.ProcessVariableSummary;
import org.jbpm.workbench.pr.service.ProcessVariablesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.paging.PageResponse;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RemoteProcessDocumentServiceImplTest {

    @Mock
    PageResponse<ProcessVariableSummary> pageResponse;
    @Mock
    private ProcessVariablesService processVariablesService;
    @InjectMocks
    private RemoteProcessDocumentsServiceImpl service;

    @Test
    public void testGetDocuments() throws Exception {
        DocumentImpl doc = new DocumentImpl("Id",
                                            "fileName",
                                            100,
                                            new Date(),
                                            "link");

        ProcessVariableSummary document1 = new ProcessVariableSummary();
        document1.setType("org.jbpm.document.Document");
        document1.setNewValue(doc.toString());

        when(processVariablesService.getData(any(QueryFilter.class))).thenReturn(pageResponse);
        when(pageResponse.getPageRowList()).thenReturn(Arrays.asList(document1));

        List<DocumentSummary> documentSummaries = service.getDocuments(mock(QueryFilter.class));
        String documentLink = service.getDocumentLink(null,
                                                      doc.getIdentifier());

        assertEquals(1,
                     documentSummaries.size());

        assertNotNull(documentSummaries);
        assertEquals(doc.getName(),
                     documentSummaries.get(0).getName());
        assertEquals(doc.getIdentifier(),
                     documentSummaries.get(0).getDocumentId());
        assertEquals(documentLink,
                     documentSummaries.get(0).getDocumentLink());
    }
}