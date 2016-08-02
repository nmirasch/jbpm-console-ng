/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.console.ng.cm.service;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jbpm.console.ng.cm.model.CaseMilestoneSummary;
import org.jbpm.console.ng.cm.model.CaseSummary;

@Remote
public interface CaseInstanceService {

    String createCaseInstance(String serverTemplateId, String containerId, String description);

    List<CaseMilestoneSummary> getCaseMilestones(String serverTemplateId, String containerId, String caseId);

    List<CaseSummary> getCaseInstances(String serverTemplateId);

    CaseSummary getCaseInstance(String serverTemplateId, String containerId, String caseId);

    void closeCaseInstance(String serverTemplateId, String containerId, String caseId);

    void completeCaseInstance(String serverTemplateId, String containerId, String caseId);

    void terminateCaseInstance(String serverTemplateId, String containerId, String caseId);

    void activateCaseInstance(String serverTemplateId, String containerId, String caseId);

}