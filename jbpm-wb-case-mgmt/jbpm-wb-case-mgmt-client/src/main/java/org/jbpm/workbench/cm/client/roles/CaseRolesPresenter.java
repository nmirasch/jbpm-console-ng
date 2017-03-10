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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jbpm.workbench.cm.client.util.AbstractCaseInstancePresenter;
import org.jbpm.workbench.cm.client.util.CaseRolesAssignmentFilterBy;
import org.jbpm.workbench.cm.client.util.ConfirmPopup;
import org.jbpm.workbench.cm.model.CaseDefinitionSummary;
import org.jbpm.workbench.cm.model.CaseInstanceSummary;
import org.jbpm.workbench.cm.model.CaseRoleAssignmentSummary;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.mvp.Command;

import static org.jbpm.workbench.cm.client.resources.i18n.Constants.*;

@Dependent
@WorkbenchScreen(identifier = CaseRolesPresenter.SCREEN_ID)
public class CaseRolesPresenter extends AbstractCaseInstancePresenter<CaseRolesPresenter.CaseRolesView> {

    public static final String SCREEN_ID = "Case Roles";

    @Inject
    private EditRoleAssignmentView editRoleAssignmentView;

    @Inject
    ConfirmPopup confirmPopup;

    @WorkbenchPartTitle
    public String getTittle() {
        return translationService.format(ROLES);
    }

    @Override
    protected void clearCaseInstance() {
        view.removeAllRoles();
    }

    @Override
    protected void loadCaseInstance(final CaseInstanceSummary cis) {
        boolean unassigned = false;
        if (passFilter(unassigned)) {
            view.addAssignment(translationService.format(OWNER), cis.getOwner());
        }
        view.setFilterCommand(() -> findCaseInstance());
        setupExistingAssignments(cis);
    }

    protected void setupExistingAssignments(final CaseInstanceSummary cis) {
        caseService.call(
                (CaseDefinitionSummary cds) -> {
                    if (cds == null || cds.getRoles() == null || cds.getRoles().isEmpty()) {
                        return;
                    }
                    cds.getRoles().keySet().stream().forEach(
                            roleName -> {
                                CaseRoleAssignmentSummary caseRoleAssignmentSummary = getRoleAssignment(cis, roleName);
                                if (!passFilter(isAssignmentAvailable(cds, cis, roleName))) {
                                    return;
                                } else {

                                    List<CaseAssignmentItem> usersList = new ArrayList<CaseAssignmentItem>();
                                    caseRoleAssignmentSummary.getUsers()
                                            .forEach(u -> usersList.add(new CaseAssignmentItem() {
                                                @Override
                                                public String label() {
                                                    return u;
                                                }

                                                @Override
                                                public void execute() {
                                                    removeUserFromRole(u, roleName);
                                                }
                                            }));
                                    List<CaseAssignmentItem> groupsList = new ArrayList<CaseAssignmentItem>();
                                    caseRoleAssignmentSummary.getGroups()
                                            .forEach(g -> groupsList.add(new CaseAssignmentItem() {
                                                @Override
                                                public String label() {
                                                    return g;
                                                }

                                                @Override
                                                public void execute() {
                                                    removeGroupFromRole(g, roleName);
                                                }
                                            }));

                                    String currentUsersStr = getStringFromList(caseRoleAssignmentSummary.getUsers());
                                    String currentGroupsStr = getStringFromList(caseRoleAssignmentSummary.getGroups());
                                    view.addAssignment(roleName, usersList, groupsList,
                                            new CaseRoleAction() {
                                                @Override
                                                public String label() {
                                                    if (isAssignmentAvailable(cds, cis, roleName)) {
                                                        return translationService.format(ASSIGN);
                                                    }
                                                    return translationService.format(EDIT);
                                                }

                                                @Override
                                                public void execute() {
                                                    editRoleAssignmentView.show(roleName, currentUsersStr, currentGroupsStr,
                                                            () -> saveAssignment(editRoleAssignmentView.getUsersNames(),
                                                                    editRoleAssignmentView.getGroupsNames(),
                                                                    roleName));
                                                }
                                            },
                                            new CaseRoleAction() {
                                                @Override
                                                public String label() {
                                                    return translationService.format(DELETE);
                                                }

                                                @Override
                                                public void execute() {
                                                    confirmPopup.show(translationService.format(DELETE_ASSIGNMENT),
                                                            translationService.format(DELETE),
                                                            translationService.format(DELETE_THE_ASSIGNMENT_FOR_ROLE,roleName),
                                                            () -> saveAssignment("",
                                                                    "",
                                                                    roleName));
                                                }
                                            });
                                }
                            });
                    view.setupPagination();
                }
        ).getCaseDefinition(serverTemplateId, containerId, cis.getCaseDefinitionId());
    }

    private boolean passFilter(boolean unassigned) {
        String filterBy = view.getFilterValue();
        if (filterBy != null && filterBy.equals(CaseRolesAssignmentFilterBy.ASSIGNED.getLabel())) {
            return !unassigned;
        }
        if (filterBy != null && filterBy.equals(CaseRolesAssignmentFilterBy.UNASSIGNED.getLabel())) {
            return unassigned;
        }
        return true;
    }

    private boolean isAssignmentAvailable(CaseDefinitionSummary cds, CaseInstanceSummary cis, String roleName) {
        final Integer roleCardinality = cds.getRoles().get(roleName);
        if (roleCardinality == -1) {
            return true;
        }
        final Integer roleInstanceCardinality =
                cis.getRoleAssignments().stream()
                        .filter(ra -> roleName.equals(ra.getName()))
                        .findFirst()
                        .map(ra -> ra.getGroups().size() + ra.getUsers().size()).orElse(0);
        return roleInstanceCardinality < roleCardinality;
    }

    protected CaseRoleAssignmentSummary getRoleAssignment(final CaseInstanceSummary cis, final String roleName) {
        if (cis.getRoleAssignments() != null && cis.getRoleAssignments().size() > 0) {
            return cis.getRoleAssignments()
                    .stream()
                    .filter(ra -> roleName.equals(ra.getName()))
                    .findFirst().orElse(CaseRoleAssignmentSummary.builder().name(roleName).build());
        }
        return CaseRoleAssignmentSummary.builder().name(roleName).build();

    }


    protected Set<String> getRolesAvailableForAssignment(final CaseInstanceSummary cis, final CaseDefinitionSummary cds) {
        return cds.getRoles().keySet().stream().filter(
                role -> {
                    if ("owner".equals(role)) {
                        return false;
                    }
                    return isAssignmentAvailable(cds, cis, role);
                }
        ).collect(Collectors.toSet());
    }

    protected void saveAssignment(final String usersNames, final String groupsNames, final String roleName) {
        caseService.call(
                (Void) -> findCaseInstance()
        ).saveAssignment(serverTemplateId, containerId, caseId, roleName,
                usersNames, groupsNames);
    }

    protected void removeUserFromRole(final String userName, final String roleName) {
        caseService.call(
                (Void) -> findCaseInstance()
        ).removeUserFromRole(serverTemplateId, containerId, caseId, roleName, userName);
    }

    protected void removeGroupFromRole(final String groupName, final String roleName) {
        caseService.call(
                (Void) -> findCaseInstance()
        ).removeGroupFromRole(serverTemplateId, containerId, caseId, roleName, groupName);
    }

    private String getStringFromList(List<String> stringList) {
        if (stringList != null) {
            return stringList.stream().map(Object::toString).collect(Collectors.joining(", "));
        }
        return "";
    }

    public interface CaseRolesView extends UberElement<CaseRolesPresenter> {

        void removeAllRoles();

        void addAssignment(String roleName, String user);

        void addAssignment(String roleName, List<CaseAssignmentItem> users, List<CaseAssignmentItem> groups, CaseRoleAction... actions);

        void setFilterCommand(Command command);

        String getFilterValue();

        void setupPagination();

    }

    public interface EditRoleAssignmentView extends UberElement<CaseRolesPresenter> {

        void show(String roleName, String users, String groups, Command okCommand);

        void hide();

        String getUsersNames();

        String getGroupsNames();

    }

    public interface CaseRoleAction extends Command {

        String label();

    }

    public interface CaseAssignmentItem extends Command {

        String label();

    }
}