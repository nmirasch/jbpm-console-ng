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

package org.jbpm.workbench.cm.server;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Specializes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.jbpm.workbench.cm.backend.server.RemoteCaseManagementServiceImpl;
import org.jbpm.workbench.cm.model.CaseActionSummary;
import org.jbpm.workbench.cm.model.CaseCommentSummary;
import org.jbpm.workbench.cm.model.CaseDefinitionSummary;
import org.jbpm.workbench.cm.model.CaseInstanceSummary;
import org.jbpm.workbench.cm.model.CaseMilestoneSummary;
import org.jbpm.workbench.cm.model.CaseRoleAssignmentSummary;
import org.jbpm.workbench.cm.model.CaseStageSummary;
import org.jbpm.workbench.cm.model.ProcessDefinitionSummary;
import org.jbpm.workbench.cm.util.CaseActionStatus;
import org.jbpm.workbench.cm.util.CaseActionType;
import org.jbpm.workbench.cm.util.CaseInstanceSearchRequest;
import org.jbpm.workbench.cm.util.CaseMilestoneSearchRequest;
import org.jbpm.workbench.cm.util.CaseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Specializes
public class MockCaseManagementService extends RemoteCaseManagementServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockCaseManagementService.class);
    private static final String CASE_DEFINITIONS_JSON = "case_definitions.json";
    private static final String CASE_MILESTONES_JSON = "case_milestones.json";
    private static final String CASE_STAGES_JSON = "case_stages.json";
    private static final String CASE_ACTIONS_JSON = "case_actions.json";
    private static final String PROCESS_DEFINITION_JSON = "process_definitions.json";


    private static int commentIdGenerator = 0;
    private static long actionIdLongenerator = 9;

    private List<CaseDefinitionSummary> caseDefinitionList = emptyList();
    private List<CaseInstanceSummary> caseInstanceList = new ArrayList<>();
    private List<CaseStageSummary> caseStageList = new ArrayList<>();
    private Map<String, List<CaseCommentSummary>> caseCommentMap = new HashMap<>();
    private List<CaseMilestoneSummary> caseMilestoneList = new ArrayList<>();
    private Map<String, List<CaseActionSummary>> caseActionMap = new HashMap<>();
    private List<CaseActionSummary> caseActionList = new ArrayList<>();
    private List<ProcessDefinitionSummary> processDefinitionList = emptyList();

    @PostConstruct
    public void init() {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(CASE_DEFINITIONS_JSON);
            caseDefinitionList = Arrays.asList(mapper.readValue(inputStream, CaseDefinitionSummary[].class));
            caseMilestoneList = Arrays.asList(mapper.readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream(CASE_MILESTONES_JSON), CaseMilestoneSummary[].class));
            caseStageList = Arrays.asList(mapper.readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream(CASE_STAGES_JSON), CaseStageSummary[].class));
            caseActionList = Arrays.asList(mapper.readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream(CASE_ACTIONS_JSON), CaseActionSummary[].class));
            processDefinitionList = Arrays.asList(mapper.readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream(PROCESS_DEFINITION_JSON), ProcessDefinitionSummary[].class));

            LOGGER.info("Loaded {} case definitions", caseDefinitionList.size());
        } catch (Exception e) {
            LOGGER.error("Failed to load json data file", e);
        }
    }

    @Override
    public CaseDefinitionSummary getCaseDefinition(final String serverTemplateId, final String containerId, final String caseDefinitionId) {
        return caseDefinitionList.stream().filter(c -> c.getId().equals(caseDefinitionId)).findFirst().get();
    }

    @Override
    public List<CaseDefinitionSummary> getCaseDefinitions() {
        return caseDefinitionList;
    }

    @Override
    public String startCaseInstance(final String serverTemplateId,
                                    final String containerId,
                                    final String caseDefinitionId,
                                    final String owner,
                                    final List<CaseRoleAssignmentSummary> roleAssignments) {
        final CaseInstanceSummary ci = CaseInstanceSummary
                .builder()
                .caseId("CASE-" + Strings.padStart(String.valueOf(caseInstanceList.size() + 1), 5, '0'))
                .owner(owner)
                .startedAt(new Date())
                .caseDefinitionId(caseDefinitionId)
                .status(CaseStatus.OPEN)
                .description("New case instance for development")
                .containerId(containerId)
                .stages(caseStageList)
                .roleAssignments(roleAssignments)
                .build();
        caseInstanceList.add(ci);

        List<CaseActionSummary> actions = new ArrayList<>(caseActionList);
        caseActionMap.putIfAbsent(ci.getCaseId(), actions.stream().map(s -> {
            s.setCreatedOn(new Date());
            return s;
        }).collect(toList()));

        return ci.getCaseId();
    }

    @Override
    public List<CaseInstanceSummary> getCaseInstances(final CaseInstanceSearchRequest request) {
        return caseInstanceList.stream()
                .filter(c -> c.getStatus().equals(request.getStatus()))
                .sorted(getCaseInstanceSummaryComparator(request))
                .collect(toList());
    }

    @Override
    public CaseInstanceSummary getCaseInstance(final String serverTemplateId, final String containerId, final String caseId) {
        return caseInstanceList.stream().filter(c -> c.getCaseId().equals(caseId)).findFirst().get();
    }

    @Override
    public void cancelCaseInstance(final String serverTemplateId, final String containerId, final String caseId) {
        executeOnCaseInstance(caseId, c -> c.setStatus(CaseStatus.CANCELLED));
    }

    @Override
    public void destroyCaseInstance(final String serverTemplateId, final String containerId, final String caseId) {
        executeOnCaseInstance(caseId, c -> c.setStatus(CaseStatus.CANCELLED));
    }

    @Override
    public List<CaseCommentSummary> getComments(final String serverTemplateId, final String containerId, final String caseId) {
        return ofNullable(caseCommentMap.get(caseId)).orElse(emptyList());
    }

    @Override
    public void addComment(final String serverTemplateId, final String containerId, final String caseId, final String author, final String text) {
        final List<CaseCommentSummary> commentSummaryList = caseCommentMap.getOrDefault(caseId, new ArrayList<>());

        final String newId = String.valueOf(commentIdGenerator++);

        final CaseCommentSummary caseCommentSummary = CaseCommentSummary.builder().id(newId).author(author).text(text).addedAt(new Date()).build();
        commentSummaryList.add(caseCommentSummary);
        caseCommentMap.putIfAbsent(caseId, commentSummaryList);
    }

    @Override
    public void updateComment(final String serverTemplateId, final String containerId, final String caseId, final String commentId, final String author, final String text) {
        ofNullable(caseCommentMap.get(caseId)).ifPresent(l -> l.stream().filter(c -> c.getId().equals(commentId)).findFirst().ifPresent(c -> c.setText(text)));
    }

    @Override
    public void removeComment(final String serverTemplateId, final String containerId, final String caseId, final String commentId) {
        ofNullable(caseCommentMap.get(caseId)).ifPresent(l -> l.stream().filter(c -> c.getId().equals(commentId)).findFirst().ifPresent(c -> l.remove(c)));
    }

    @Override
    public void assignUserToRole(final String serverTemplateId, final String containerId, final String caseId, final String roleName, final String user) {
        executeOnCaseRole(caseId, roleName, r -> r.getUsers().add(user));
    }

    @Override
    public void assignGroupToRole(final String serverTemplateId, final String containerId, final String caseId, final String roleName, final String group) {
        executeOnCaseRole(caseId, roleName, r -> r.getGroups().add(group));
    }

    @Override
    public void removeUserFromRole(final String serverTemplateId, final String containerId, final String caseId, final String roleName, final String user) {
        executeOnCaseRole(caseId, roleName, r -> r.getUsers().remove(user));
    }

    @Override
    public void removeGroupFromRole(final String serverTemplateId, final String containerId, final String caseId, final String roleName, final String group) {
        executeOnCaseRole(caseId, roleName, r -> r.getGroups().remove(group));
    }

    private void executeOnCaseInstance(final String caseId, final Consumer<CaseInstanceSummary> consumer) {
        caseInstanceList.stream().filter(c -> c.getCaseId().equals(caseId)).findFirst().ifPresent(consumer);
    }

    private void executeOnCaseRole(final String caseId, final String roleName, final Consumer<CaseRoleAssignmentSummary> consumer) {
        executeOnCaseInstance(caseId,
                c -> {
                    final CaseRoleAssignmentSummary role = c.getRoleAssignments().stream()
                            .filter(r -> r.getName().equals(roleName)).findFirst()
                            .orElseGet(() -> {
                                final CaseRoleAssignmentSummary newRole = CaseRoleAssignmentSummary.builder().name(roleName).build();
                                c.getRoleAssignments().add(newRole);
                                return newRole;
                            });
                    consumer.accept(role);
                }
        );
    }

    @Override
    public List<CaseMilestoneSummary> getCaseMilestones(final String containerId, final String caseId , final CaseMilestoneSearchRequest request) {
        return caseMilestoneList.stream()
                    .sorted(getCaseMilestoneSummaryComparator(request))
                    .collect(toList());
    }

    public List<CaseActionSummary> getAdHocFragments(String containerId, String caseId) {
        return ofNullable(caseActionMap.get(caseId)).orElse(emptyList()).stream()
                .filter(c -> CaseActionType.AD_HOC_TASK == c.getActionType()).collect(toList());
    }

    public List<CaseActionSummary> getInProgressActions(String containerId, String caseId) {
        return ofNullable(caseActionMap.get(caseId)).orElse(emptyList()).stream()
                .filter(c -> CaseActionStatus.IN_PROGRESS == c.getActionStatus()).collect(toList());
    }

    public List<CaseActionSummary> getCompletedActions(String containerId, String caseId) {
        return ofNullable(caseActionMap.get(caseId)).orElse(emptyList()).stream()
                .filter(c -> CaseActionStatus.COMPLETED == c.getActionStatus()).collect(toList());
    }

    @Override
    public void addDynamicUserTask(String containerId, String caseId, String name, String description, String actors, String groups, Map<String, Object> data) {
        final List<CaseActionSummary> actionSummaryList = caseActionMap.getOrDefault(caseId, new ArrayList<>());
        final CaseActionSummary action = CaseActionSummary.builder()
                .id(actionIdLongenerator++)
                .name(name)
                .actualOwner(actors)
                .type("Human Task")
                .actionStatus(CaseActionStatus.IN_PROGRESS)
                .createdOn(new Date())
                .build();
        actionSummaryList.add(action);
        caseActionMap.putIfAbsent(caseId, actionSummaryList);
    }

    public void addDynamicUserTaskToStage(String containerId, String caseId, String stageId, String name, String description, String actors, String groups, Map<String, Object> data) {
        final List<CaseActionSummary> actionSummaryList = caseActionMap.getOrDefault(caseId, new ArrayList<>());
        final CaseActionSummary action = CaseActionSummary.builder()
                .id(actionIdLongenerator++)
                .name(name)
                .actualOwner(actors)
                .type("Human Task")
                .actionStatus(CaseActionStatus.IN_PROGRESS)
                .createdOn(new Date())
                .build();
        actionSummaryList.add(action);
        caseActionMap.putIfAbsent(caseId, actionSummaryList);
    }

    @Override
    public void triggerAdHocActionInStage(String containerId, String caseId, String stageId, String adHocName, Map<String, Object> data) {
        final List<CaseActionSummary> actionSummaryList = caseActionMap.getOrDefault(caseId, new ArrayList<>());
        final CaseActionSummary action = CaseActionSummary.builder()
                .id(actionIdLongenerator++)
                .name(adHocName)
                .actionStatus(CaseActionStatus.IN_PROGRESS)
                .createdOn(new Date())
                .build();
        actionSummaryList.add(action);
        caseActionMap.putIfAbsent(caseId, actionSummaryList);
    }

    @Override
    public void triggerAdHocAction(String containerId, String caseId, String adHocName, Map<String, Object> data) {
        final List<CaseActionSummary> actionSummaryList = caseActionMap.getOrDefault(caseId, new ArrayList<>());
        final CaseActionSummary action = CaseActionSummary.builder()
                .id(actionIdLongenerator++)
                .name(adHocName)
                .actionStatus(CaseActionStatus.IN_PROGRESS)
                .createdOn(new Date())
                .build();
        actionSummaryList.add(action);
        caseActionMap.putIfAbsent(caseId, actionSummaryList);
    }

    @Override
    public void addDynamicSubProcess(String containerId, String caseId, String processId, Map<String, Object> data) {
        final List<CaseActionSummary> actionSummaryList = caseActionMap.getOrDefault(caseId, new ArrayList<>());
        final CaseActionSummary action = CaseActionSummary.builder()
                .id(actionIdLongenerator++)
                .name("subprocess: " + processId)
                .actionStatus(CaseActionStatus.IN_PROGRESS)
                .createdOn(new Date())
                .build();
        actionSummaryList.add(action);
        caseActionMap.putIfAbsent(caseId, actionSummaryList);
    }

    @Override
    public void addDynamicSubProcessToStage(String containerId, String caseId, String stageId, String processId, Map<String, Object> data) {
        final List<CaseActionSummary> actionSummaryList = caseActionMap.getOrDefault(caseId, new ArrayList<>());
        final CaseActionSummary action = CaseActionSummary.builder()
                .id(actionIdLongenerator++)
                .name("subprocess: " + processId + " inStage:" + stageId)
                .actionStatus(CaseActionStatus.IN_PROGRESS)
                .createdOn(new Date())
                .build();
        actionSummaryList.add(action);
        caseActionMap.putIfAbsent(caseId, actionSummaryList);
    }

    @Override
    public List<ProcessDefinitionSummary> getProcessDefinitions(String containerId){
        return processDefinitionList;
    }
}