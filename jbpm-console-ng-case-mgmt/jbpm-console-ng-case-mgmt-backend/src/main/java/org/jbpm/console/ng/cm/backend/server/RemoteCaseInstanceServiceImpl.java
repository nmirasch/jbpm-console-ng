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

package org.jbpm.console.ng.cm.backend.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.bd.integration.AbstractKieServerService;
import org.jbpm.console.ng.cm.model.CaseMilestoneSummary;
import org.jbpm.console.ng.cm.model.CaseStatus;
import org.jbpm.console.ng.cm.model.CaseSummary;
import org.jbpm.console.ng.cm.service.CaseInstanceService;

@Service
@ApplicationScoped
public class RemoteCaseInstanceServiceImpl extends AbstractKieServerService implements CaseInstanceService {

    private Map<String, CaseSummary> cases = new HashMap<>();
    private Map<String, List<String>> comments = new HashMap<>();
    private AtomicLong counter = new AtomicLong();

    @Override
    public List<CaseMilestoneSummary> getCaseMilestones(final String serverTemplateId,
                                                        final String containerId,
                                                        final String caseId) {
//        final CaseServicesClient client = getClient(serverTemplateId, containerId, CaseServicesClient.class);
//        final List<CaseMilestone> milestones = client.getMilestones(containerId, caseId, false);
//        return milestones.stream()
//                .map(m -> new CaseMilestoneSummary(m.getIdentifier(), m.getName(), m.isAchieved(), m.getAchievedAt()))
//                .collect(Collectors.toList());
        return Arrays.asList(
                new CaseMilestoneSummary("id1", "milestone 1", true, new Date()),
                new CaseMilestoneSummary("id2", "milestone 2", false, null)
        );
    }

    @Override
    public String createCaseInstance(final String serverTemplateId,
                                     final String containerId,
                                     final String description) {
        final CaseSummary summary = new CaseSummary();
        summary.setCaseId("CASE-" + counter.incrementAndGet());
        summary.setDescription(description);
        summary.setStatus(CaseStatus.ACTIVE);
        cases.put(summary.getCaseId(), summary);
        return summary.getCaseId();
    }

    @Override
    public List<CaseSummary> getCaseInstances(final String serverTemplateId) {
        return new ArrayList<>(cases.values());
    }

    @Override
    public CaseSummary getCaseInstance(final String serverTemplateId,
                                       final String containerId,
                                       final String caseId) {
        return cases.get(caseId);
    }

    @Override
    public void closeCaseInstance(final String serverTemplateId,
                                  final String containerId,
                                  final String caseId) {
        updateCaseStatus(caseId, CaseStatus.CLOSED);
    }

    @Override
    public void completeCaseInstance(final String serverTemplateId,
                                     final String containerId,
                                     final String caseId) {
        updateCaseStatus(caseId, CaseStatus.COMPLETED);
    }

    @Override
    public void terminateCaseInstance(final String serverTemplateId,
                                      final String containerId,
                                      final String caseId) {
        updateCaseStatus(caseId, CaseStatus.TERMINATED);
    }

    @Override
    public void activateCaseInstance(final String serverTemplateId,
                                     final String containerId,
                                     final String caseId) {
        updateCaseStatus(caseId, CaseStatus.ACTIVE);
    }

    private void updateCaseStatus(final String caseId, final CaseStatus status) {
        CaseSummary summary = cases.get(caseId);
        if (summary != null) {
            summary.setStatus(status);
        }
    }

    @Override
    public void addComment(final String serverTemplateId,
                           final String containerId,
                           final String caseId,
                           final String comment,
                           final String user) {
        List<String> caseComments = this.comments.get(caseId);
        if (caseComments == null) {
            caseComments = new ArrayList<>();
            this.comments.put(caseId, caseComments);
        }
        caseComments.add(comment);
    }

    @Override
    public List<String> getComments(final String serverTemplateId,
                                    final String containerId,
                                    final String caseId) {
        final List<String> comments = this.comments.get(caseId);
        return comments == null ? Collections.emptyList() : comments;
    }
}