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
package org.jbpm.console.ng.cm.model;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jbpm.console.ng.ga.model.GenericSummary;

@Portable
public class CaseSummary extends GenericSummary {

    private String description;
    private CaseStatus status;
    private String recipient;
    private String humanTasksDetails;
    private String processesDetails;
    private String subCasesDetails;

    public CaseSummary() {
    }

    public CaseSummary(String caseId) {
        super(caseId, null);
    }

    public String getCaseId() {
        return (String)getId();
    }

    public void setCaseId(String caseId) {
        setId(caseId);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getHumanTasksDetails() {
        return humanTasksDetails;
    }

    public void setHumanTasksDetails(String humanTasksDetails) {
        this.humanTasksDetails = humanTasksDetails;
    }

    public String getProcessesDetails() {
        return processesDetails;
    }

    public void setProcessesDetails(String processesDetails) {
        this.processesDetails = processesDetails;
    }

    public String getSubCasesDetails() {
        return subCasesDetails;
    }

    public void setSubCasesDetails(String subCasesDetails) {
        this.subCasesDetails = subCasesDetails;
    }

    @Override
    public String toString() {
        return "CaseSummary{" +
                "description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", recipient='" + recipient + '\'' +
                ", humanTasksDetails='" + humanTasksDetails + '\'' +
                ", processesDetails='" + processesDetails + '\'' +
                ", subCasesDetails='" + subCasesDetails + '\'' +
                "} " + super.toString();
    }
}