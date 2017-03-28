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

package org.jbpm.workbench.cm.client.roles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.workbench.cm.client.util.AbstractCaseInstancePresenterTest;
import org.jbpm.workbench.cm.client.util.CaseRolesAssignmentFilterBy;
import org.jbpm.workbench.cm.client.util.CaseRolesValidations;
import org.jbpm.workbench.cm.model.CaseDefinitionSummary;
import org.jbpm.workbench.cm.model.CaseInstanceSummary;
import org.jbpm.workbench.cm.model.CaseRoleAssignmentSummary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Collections.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseRolesPresenterTest extends AbstractCaseInstancePresenterTest {

    private static final String USER = "User";
    private static final String GROUP = "Group";
    private static final String CASE_ROLE = "Role";
    private static final String CASE_DEFINITION_ID = "org.jbpm.case";

    @Mock
    CaseRolesPresenter.CaseRolesView view;

    @Mock
    CaseRolesValidations caseRolesValidations;

    @Mock
    CaseRolesPresenter.EditRoleAssignmentView assignmentView;

    @InjectMocks
    CaseRolesPresenter presenter;

    @Override
    public CaseRolesPresenter getPresenter() {
        return presenter;
    }


    final String serverTemplateId = "serverTemplateId";
    final CaseInstanceSummary caseInstance = newCaseInstanceSummary();
    CaseDefinitionSummary caseDefinition;

    @Before
    public void setUp() {
        when(view.getFilterValue()).thenReturn(CaseRolesAssignmentFilterBy.ALL.getLabel());
    }

    @Test
    public void testClearCaseInstance() {
        presenter.clearCaseInstance();

        verifyClearCaseInstance();
    }

    private void verifyClearCaseInstance() {
        verify(view).removeAllRoles();
    }

    @Test
    public void testLoadCaseInstance() {
        caseDefinition = CaseDefinitionSummary.builder()
                .roles(singletonMap(CASE_ROLE, 3))
                .build();
        caseInstance.setRoleAssignments
                (singletonList(CaseRoleAssignmentSummary.builder().name(CASE_ROLE).groups(singletonList(GROUP)).users(singletonList(USER)).build()));
        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);
        when(caseManagementService.getCaseDefinition(serverTemplateId, caseInstance.getContainerId(), caseInstance.getCaseDefinitionId()))
                .thenReturn(caseDefinition);

        setupCaseInstance(caseInstance, serverTemplateId);

        verifyClearCaseInstance();

        final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        verify(view).setRolesAssignmentList(captor.capture());

        assertEquals(1, captor.getValue().size());
        assertEquals(CASE_ROLE, ((CaseRoleAssignmentSummary) captor.getValue().get(0)).getName());
        assertEquals(USER, ((CaseRoleAssignmentSummary) captor.getValue().get(0)).getUsers().get(0));
        assertEquals(GROUP, ((CaseRoleAssignmentSummary) captor.getValue().get(0)).getGroups().get(0));
    }

    @Test
    public void testFiltering() {
        final String caseRole_1 = "Role_1";
        final String caseRole_2 = "Role_2";
        final String caseRole_3 = "Role_3";
        final Map<String, Integer> roles = new HashMap<>();
        roles.put(caseRole_1, -1);
        roles.put(caseRole_2, 2);
        roles.put(caseRole_3, 1);
        final CaseDefinitionSummary caseDefinition = CaseDefinitionSummary.builder().
                roles(roles).
                build();
        final CaseInstanceSummary caseInstance = newCaseInstanceSummary();
        caseInstance.setRoleAssignments(
                Arrays.asList(
                        CaseRoleAssignmentSummary.builder().name(caseRole_1).users(Arrays.asList(USER)).groups(Arrays.asList(GROUP)).build(),
                        CaseRoleAssignmentSummary.builder().name(caseRole_2).build(),
                        CaseRoleAssignmentSummary.builder().name(caseRole_3).groups(Arrays.asList(GROUP)).build()
                ));

        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);
        when(caseManagementService.getCaseDefinition(serverTemplateId, caseInstance.getContainerId(), caseInstance.getCaseDefinitionId()))
                .thenReturn(caseDefinition);
        setupCaseInstance(caseInstance, serverTemplateId);

        final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(view).setRolesAssignmentList(captor.capture());
        assertEquals(3, captor.getValue().size());
        assertEquals(caseRole_1, ((CaseRoleAssignmentSummary) captor.getValue().get(0)).getName());
        assertEquals(caseRole_2, ((CaseRoleAssignmentSummary) captor.getValue().get(1)).getName());
        assertEquals(caseRole_3, ((CaseRoleAssignmentSummary) captor.getValue().get(2)).getName());
        assertEquals(USER, ((CaseRoleAssignmentSummary) captor.getValue().get(0)).getUsers().get(0));
        assertEquals(GROUP, ((CaseRoleAssignmentSummary) captor.getValue().get(0)).getGroups().get(0));
        assertEquals(GROUP, ((CaseRoleAssignmentSummary) captor.getValue().get(2)).getGroups().get(0));

        when(view.getFilterValue()).thenReturn(CaseRolesAssignmentFilterBy.ASSIGNED.getLabel());
        presenter.filterElements();

        final ArgumentCaptor<List> captor2 = ArgumentCaptor.forClass(List.class);
        verify(view,times(2)).setRolesAssignmentList(captor2.capture());
        assertEquals(2, captor2.getValue().size());
        assertEquals(caseRole_1, ((CaseRoleAssignmentSummary) captor2.getValue().get(0)).getName());
        assertEquals(caseRole_3, ((CaseRoleAssignmentSummary) captor2.getValue().get(1)).getName());
        assertEquals(USER, ((CaseRoleAssignmentSummary) captor2.getValue().get(0)).getUsers().get(0));
        assertEquals(GROUP, ((CaseRoleAssignmentSummary) captor2.getValue().get(0)).getGroups().get(0));
        assertEquals(GROUP, ((CaseRoleAssignmentSummary) captor2.getValue().get(1)).getGroups().get(0));

        when(view.getFilterValue()).thenReturn(CaseRolesAssignmentFilterBy.UNASSIGNED.getLabel());
        presenter.filterElements();

        final ArgumentCaptor<List> captor3 = ArgumentCaptor.forClass(List.class);
        verify(view,times(3)).setRolesAssignmentList(captor3.capture());
        assertEquals(1, captor3.getValue().size());
        assertEquals(caseRole_2, ((CaseRoleAssignmentSummary) captor3.getValue().get(0)).getName());

    }


    @Test
    public void testSetupNewRoleAssignments_rolesNotDefined() {
        final CaseDefinitionSummary caseDefinition = CaseDefinitionSummary.builder().build();
        final CaseInstanceSummary caseInstance = newCaseInstanceSummary();
        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);
        when(caseManagementService.getCaseDefinition(anyString(), anyString(), eq(CASE_DEFINITION_ID))).thenReturn(caseDefinition);

        presenter.setupExistingAssignments(caseInstance);

        verify(view,never()).setRolesAssignmentList(anyList());
    }


    @Test
    public void testAssignToRole_rolesAvailableForAssignment() {
        final CaseDefinitionSummary caseDefinition = CaseDefinitionSummary.builder()
                .roles(singletonMap(CASE_ROLE, 2))
                .build();
        final CaseInstanceSummary caseInstance = newCaseInstanceSummary();
        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);
        when(caseManagementService.getCaseDefinition(anyString(), anyString(), eq(CASE_DEFINITION_ID))).thenReturn(caseDefinition);

        CaseRoleAssignmentSummary editedRoleAssignmentSummary =
                CaseRoleAssignmentSummary.builder().name(CASE_ROLE).users(Arrays.asList("user1", "user2","user3")).build();
        when(assignmentView.getValue()).thenReturn(editedRoleAssignmentSummary);
        when(caseRolesValidations.validateRolesAssignments(any(CaseDefinitionSummary.class),anyList())).thenReturn(Arrays.asList("error"));

        presenter.setupExistingAssignments(caseInstance);
        presenter.assignToRole(EMPTY_LIST,EMPTY_LIST);

        verify(assignmentView).showValidationError(anyList());
        verify(caseManagementService,never()).assignToRole(anyString(),anyString(),anyString(),anyString(),anyList(),anyList(),anyList(),anyList());

        when(caseRolesValidations.validateRolesAssignments(any(CaseDefinitionSummary.class),anyList())).thenReturn(EMPTY_LIST);
        CaseRoleAssignmentSummary editedRoleAssignmentSummary2 =
                CaseRoleAssignmentSummary.builder().name(CASE_ROLE).users(Arrays.asList("user1", "user2")).build();
        when(assignmentView.getValue()).thenReturn(editedRoleAssignmentSummary2);
        presenter.assignToRole(EMPTY_LIST,EMPTY_LIST);

        verify(caseManagementService).assignToRole(anyString(),anyString(),anyString(),anyString(),anyList(),anyList(),anyList(),anyList());

    }

    private void setCaseDefinitionID(String caseDefinitionID, CaseDefinitionSummary caseDefinition, CaseInstanceSummary caseInstance) {
        caseDefinition.setId(caseDefinitionID);
        caseInstance.setCaseDefinitionId(caseDefinitionID);
    }
}