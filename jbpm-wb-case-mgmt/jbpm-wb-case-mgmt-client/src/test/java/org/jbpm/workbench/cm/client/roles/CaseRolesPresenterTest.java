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
import java.util.Set;
import java.util.stream.Collectors;

import org.jbpm.workbench.cm.client.util.AbstractCaseInstancePresenterTest;
import org.jbpm.workbench.cm.client.util.CaseRolesAssignmentFilterBy;
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
import org.uberfire.mvp.Command;

import static java.util.Collections.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseRolesPresenterTest extends AbstractCaseInstancePresenterTest {

    private static final String USER = "User";
    private static final String GROUP = "Group";
    private static final String CASE_ROLE = "Role";
    private static final String CASE_DEFINITION_ID = "org.jbpm.case";

    @Mock
    CaseRolesPresenter.CaseRolesView view;

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
    public void setUp(){
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
        verify(view).addAssignment("Owner","admin");

        final ArgumentCaptor<CaseRolesPresenter.CaseRoleAction> actionCaptor = ArgumentCaptor.forClass(CaseRolesPresenter.CaseRoleAction.class);
        final ArgumentCaptor<List> captorUserList = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<List> captorGroupList = ArgumentCaptor.forClass(List.class);

        verify(view).addAssignment(eq(CASE_ROLE),captorUserList.capture(),captorGroupList.capture(), actionCaptor.capture());
        assertEquals(USER, ((CaseRolesPresenter.CaseAssignmentItem)captorUserList.getValue().get(0)).label());
        assertEquals(GROUP, ((CaseRolesPresenter.CaseAssignmentItem)captorGroupList.getValue().get(0)).label());
        assertEquals("Assign", actionCaptor.getAllValues().get(0).label());
        assertEquals("Delete", actionCaptor.getAllValues().get(1).label());
    }


    @Test
    public void testSetupRoleAssignments_whenNoAssignmentsAreMade() {
        presenter.setupExistingAssignments(newCaseInstanceSummary());

        verifyZeroInteractions(view);
    }

    @Test
    public void testSetupRoleAssignments_onlyUsersAssigned() {
        caseDefinition = CaseDefinitionSummary.builder()
                .roles(singletonMap(CASE_ROLE, 3))
                .build();
        caseInstance.setRoleAssignments
                (singletonList(CaseRoleAssignmentSummary.builder().name(CASE_ROLE).users(singletonList(USER)).groups(emptyList()).build()));
        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);
        when(caseManagementService.getCaseDefinition(serverTemplateId, caseInstance.getContainerId(), caseInstance.getCaseDefinitionId()))
                .thenReturn(caseDefinition);
        setupCaseInstance(caseInstance, serverTemplateId);

        presenter.setupExistingAssignments(caseInstance);

        verify(view).addAssignment(eq("Owner"), eq("admin"));

        final ArgumentCaptor<CaseRolesPresenter.CaseRoleAction> actionCaptor = ArgumentCaptor.forClass(CaseRolesPresenter.CaseRoleAction.class);
        final ArgumentCaptor<List> captorUserList = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<List> captorGroupList = ArgumentCaptor.forClass(List.class);

        verify(view,times(2)).addAssignment(eq(CASE_ROLE),captorUserList.capture(),captorGroupList.capture(), actionCaptor.capture());
        assertEquals(USER, ((CaseRolesPresenter.CaseAssignmentItem)captorUserList.getValue().get(0)).label());
        assertEquals(0, captorGroupList.getValue().size());
        assertEquals("Assign", actionCaptor.getAllValues().get(0).label());
        assertEquals("Delete", actionCaptor.getAllValues().get(1).label());

    }

    @Test
    public void testSetupRoleAssignments_onlyGroupsAssigned() {
        caseDefinition = CaseDefinitionSummary.builder()
                .roles(singletonMap(CASE_ROLE, 3))
                .build();
        caseInstance.setRoleAssignments
                (singletonList(CaseRoleAssignmentSummary.builder().name(CASE_ROLE).users(emptyList()).groups(singletonList(GROUP)).build()));
        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);
        when(caseManagementService.getCaseDefinition(serverTemplateId, caseInstance.getContainerId(), caseInstance.getCaseDefinitionId()))
                .thenReturn(caseDefinition);
        setupCaseInstance(caseInstance, serverTemplateId);

        presenter.setupExistingAssignments(caseInstance);

        verify(view).addAssignment(eq("Owner"), eq("admin"));
        final ArgumentCaptor<CaseRolesPresenter.CaseRoleAction> actionCaptor = ArgumentCaptor.forClass(CaseRolesPresenter.CaseRoleAction.class);
        final ArgumentCaptor<List> captorUserList = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<List> captorGroupList = ArgumentCaptor.forClass(List.class);

        verify(view,times(2)).addAssignment(eq(CASE_ROLE),captorUserList.capture(),captorGroupList.capture(), actionCaptor.capture());
        assertEquals(GROUP, ((CaseRolesPresenter.CaseAssignmentItem)captorGroupList.getValue().get(0)).label());
        assertEquals(0, captorUserList.getValue().size());
        assertEquals("Assign", actionCaptor.getAllValues().get(0).label());
        assertEquals("Delete", actionCaptor.getAllValues().get(1).label());
    }

    @Test
    public void testSetupNewRoleAssignments_rolesNotDefined() {
        final CaseDefinitionSummary caseDefinition = CaseDefinitionSummary.builder().build();
        final CaseInstanceSummary caseInstance = newCaseInstanceSummary();
        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);
        when(caseManagementService.getCaseDefinition(anyString(), anyString(), eq(CASE_DEFINITION_ID))).thenReturn(caseDefinition);

        presenter.setupExistingAssignments(caseInstance);

        verifyZeroInteractions(view);
    }

    @Test
    public void testSetupNewRoleAssignments_noRolesAvailableForAssignment() {
        final CaseDefinitionSummary caseDefinition = CaseDefinitionSummary.builder()
                .roles(singletonMap(CASE_ROLE, 1))
                .build();
        final CaseInstanceSummary caseInstance = newCaseInstanceSummary();
        caseInstance.setRoleAssignments
                (singletonList(CaseRoleAssignmentSummary.builder().name(CASE_ROLE).groups(emptyList()).users(singletonList(USER)).build()));
        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);
        when(caseManagementService.getCaseDefinition(anyString(), anyString(), eq(CASE_DEFINITION_ID))).thenReturn(caseDefinition);

        presenter.setupExistingAssignments(caseInstance);

        verify(view,never()).addAssignment(anyString(), anyList(), anyList());
    }

    @Test
    public void testSetupNewRoleAssignments_rolesAvailableForAssignment() {
        final CaseDefinitionSummary caseDefinition = CaseDefinitionSummary.builder()
                .roles(singletonMap(CASE_ROLE, 1))
                .build();
        final CaseInstanceSummary caseInstance = newCaseInstanceSummary();
        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);
        when(caseManagementService.getCaseDefinition(anyString(), anyString(), eq(CASE_DEFINITION_ID))).thenReturn(caseDefinition);

        presenter.setupExistingAssignments(caseInstance);

        final ArgumentCaptor<CaseRolesPresenter.CaseRoleAction> actionCaptor = ArgumentCaptor.forClass(CaseRolesPresenter.CaseRoleAction.class);
        verify(view).addAssignment(eq(CASE_ROLE),anyList(),anyList(), actionCaptor.capture());
        actionCaptor.getAllValues().get(0).execute();
        final ArgumentCaptor<Command> captor = ArgumentCaptor.forClass(Command.class);
        verify(assignmentView).show( eq(CASE_ROLE),eq(""),eq(""), captor.capture());

        when(assignmentView.getUsersNames()).thenReturn("user1");
        when(assignmentView.getGroupsNames()).thenReturn("");
        captor.getValue().execute();
        verify(caseManagementService).saveAssignment(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

        when(assignmentView.getUsersNames()).thenReturn("");
        when(assignmentView.getGroupsNames()).thenReturn("groupName");
        captor.getValue().execute();
        verify(caseManagementService,times(2)).saveAssignment(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());

        when(assignmentView.getUsersNames()).thenReturn("user1");
        when(assignmentView.getGroupsNames()).thenReturn("groupName");
        captor.getValue().execute();
        verify(caseManagementService,times(3)).saveAssignment(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGetRolesAvailableForAssignment_excludeOwnerRole() {
        final String ownerRole = "owner";
        final Map<String, Integer> roles = new HashMap<>();
        roles.put(ownerRole, 1);

        final String[] rolesNames = {"Owner", " owner ", "OWNER"};
        final Integer rolesCardinality = -1;
        Arrays.stream(rolesNames).forEach(role -> roles.put(role, rolesCardinality));

        final CaseDefinitionSummary caseDefinition = CaseDefinitionSummary.builder()
                .roles(roles)
                .build();
        final CaseInstanceSummary caseInstance = newCaseInstanceSummary();
        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);

        final Set<String> availableRoles = presenter.getRolesAvailableForAssignment(caseInstance, caseDefinition);

        assertThat(availableRoles)
                .doesNotContain(ownerRole)
                .containsAll(Arrays.asList(rolesNames));
    }

    @Test
    public void testGetRolesAvailableForAssignment_rolesWithDifferentCardinality() {
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
        caseInstance.setRoleAssignments(caseDefinition.getRoles().keySet().stream().map(
                role -> CaseRoleAssignmentSummary.builder()
                        .name(role)
                        .groups(emptyList())
                        .users(emptyList())
                        .build()
        ).collect(Collectors.toList()));
        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);

        final Set<String> availableRolesFirstPass = presenter.getRolesAvailableForAssignment(caseInstance, caseDefinition);

        assertThat(availableRolesFirstPass)
                .contains(caseRole_1, caseRole_2, caseRole_3);


        caseInstance.getRoleAssignments().stream()
                .filter(roleAssignment -> availableRolesFirstPass.contains(roleAssignment.getName()))
                .forEach(roleAssignment -> roleAssignment.setUsers(singletonList(USER)));

        final Set<String> availableRolesSecondPass = presenter.getRolesAvailableForAssignment(caseInstance, caseDefinition);

        assertThat(availableRolesSecondPass)
                .contains(caseRole_1, caseRole_2)
                .doesNotContain(caseRole_3);

        caseInstance.getRoleAssignments().stream()
                .filter(roleAssignment -> availableRolesSecondPass.contains(roleAssignment.getName()))
                .forEach(roleAssignment -> roleAssignment.setGroups(singletonList(GROUP)));

        final Set<String> availableRolesThirdPass = presenter.getRolesAvailableForAssignment(caseInstance, caseDefinition);

        assertThat(availableRolesThirdPass)
                .contains(caseRole_1)
                .doesNotContain(caseRole_2, caseRole_3);
    }

    @Test
    public void testAssignmentFiltering_ALL() {
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
        caseInstance.setRoleAssignments(caseDefinition.getRoles().keySet().stream().map(
                role -> CaseRoleAssignmentSummary.builder()
                        .name(role)
                        .groups(Arrays.asList(GROUP))
                        .users(Arrays.asList(USER))
                        .build()
        ).collect(Collectors.toList()));

        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);

        when(caseManagementService.getCaseDefinition(serverTemplateId, caseInstance.getContainerId(), caseInstance.getCaseDefinitionId()))
                .thenReturn(caseDefinition);
        when(view.getFilterValue()).thenReturn(CaseRolesAssignmentFilterBy.ALL.getLabel());

        setupCaseInstance(caseInstance, serverTemplateId);

        final ArgumentCaptor<CaseRolesPresenter.CaseRoleAction> actionCaptor = ArgumentCaptor.forClass(CaseRolesPresenter.CaseRoleAction.class);
        final ArgumentCaptor<String> captorRoleName = ArgumentCaptor.forClass(String.class);
        verify(view,times(3)).addAssignment(captorRoleName.capture(), anyList(), anyList(), actionCaptor.capture());
        assertEquals(caseRole_1,captorRoleName.getAllValues().get(0));
        assertEquals(caseRole_2,captorRoleName.getAllValues().get(1));
        assertEquals(caseRole_3,captorRoleName.getAllValues().get(2));

        verify(view,times(3)).addAssignment(anyString(), anyList(), anyList(), actionCaptor.capture());
        verify(view).addAssignment(eq("Owner"), eq("admin"));

    }

    @Test
    public void testAssignmentFiltering_Assigned() {
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
        caseInstance.setRoleAssignments(caseDefinition.getRoles().keySet().stream().map(
                role -> CaseRoleAssignmentSummary.builder()
                        .name(role)
                        .groups(Arrays.asList(GROUP))
                        .users(Arrays.asList(USER))
                        .build()
        ).collect(Collectors.toList()));

        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);

        when(caseManagementService.getCaseDefinition(serverTemplateId, caseInstance.getContainerId(), caseInstance.getCaseDefinitionId()))
                .thenReturn(caseDefinition);
        when(view.getFilterValue()).thenReturn(CaseRolesAssignmentFilterBy.ASSIGNED.getLabel());

        setupCaseInstance(caseInstance, serverTemplateId);

        final ArgumentCaptor<CaseRolesPresenter.CaseRoleAction> actionCaptor = ArgumentCaptor.forClass(CaseRolesPresenter.CaseRoleAction.class);
        final ArgumentCaptor<String> captorRoleName = ArgumentCaptor.forClass(String.class);
        verify(view,times(2)).addAssignment(captorRoleName.capture(), anyList(), anyList(), actionCaptor.capture());
        assertEquals(caseRole_2,captorRoleName.getAllValues().get(0));
        assertEquals(caseRole_3,captorRoleName.getAllValues().get(1));

        verify(view).addAssignment(eq("Owner"), eq("admin"));

    }
    @Test
    public void testAssignmentFiltering_Unassigned() {
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
        caseInstance.setRoleAssignments(caseDefinition.getRoles().keySet().stream().map(
                role -> CaseRoleAssignmentSummary.builder()
                        .name(role)
                        .groups(Arrays.asList(GROUP))
                        .users(Arrays.asList(USER))
                        .build()
        ).collect(Collectors.toList()));

        setCaseDefinitionID(CASE_DEFINITION_ID, caseDefinition, caseInstance);

        when(caseManagementService.getCaseDefinition(serverTemplateId, caseInstance.getContainerId(), caseInstance.getCaseDefinitionId()))
                .thenReturn(caseDefinition);
        when(view.getFilterValue()).thenReturn(CaseRolesAssignmentFilterBy.UNASSIGNED.getLabel());

        setupCaseInstance(caseInstance, serverTemplateId);

        final ArgumentCaptor<CaseRolesPresenter.CaseRoleAction> actionCaptor = ArgumentCaptor.forClass(CaseRolesPresenter.CaseRoleAction.class);
        final ArgumentCaptor<String> captorRoleName = ArgumentCaptor.forClass(String.class);
        verify(view).addAssignment(captorRoleName.capture(), anyList(), anyList(), actionCaptor.capture());
        assertEquals(caseRole_1,captorRoleName.getAllValues().get(0));
        verify(view,never()).addAssignment(eq("Owner"), eq("admin"));

    }

    private void setCaseDefinitionID(String caseDefinitionID, CaseDefinitionSummary caseDefinition, CaseInstanceSummary caseInstance) {
        caseDefinition.setId(caseDefinitionID);
        caseInstance.setCaseDefinitionId(caseDefinitionID);
    }
}