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

package org.jbpm.console.ng.pr.backend.server.integration;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.bd.integration.KieServerIntegration;
import org.jbpm.console.ng.pr.backend.server.integration.model.RemoteCorrelationKey;
import org.jbpm.console.ng.pr.service.integration.RemoteProcessService;
import org.kie.internal.process.CorrelationKey;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ApplicationScoped
public class RemoteProcessServiceImpl implements RemoteProcessService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteProcessServiceImpl.class);

    @Inject
    private KieServerIntegration kieServerIntegration;

    @Override
    public void abortProcessInstance(String serverTemplateId, String containerId, Long processInstanceId) {
        ProcessServicesClient client = getClient(serverTemplateId, containerId);

        client.abortProcessInstance(containerId, processInstanceId);
    }

    @Override
    public void abortProcessInstances(String serverTemplateId, List<String> containers, List<Long> processInstanceId) {

        if (new HashSet<String>(containers).size() == 1) {
            ProcessServicesClient client = getClient(serverTemplateId, containers.get(0));
            client.abortProcessInstances(containers.get(0), processInstanceId);
        } else {
            for (int i = 0; i < processInstanceId.size(); i++) {
                ProcessServicesClient client = getClient(serverTemplateId, containers.get(i));
                client.abortProcessInstance(containers.get(i), processInstanceId.get(i));
            }
        }
    }

    @Override
    public Long startProcess(String serverTemplateId, String containerId, String processId, String correlationKey, Map<String, Object> params) {
        ProcessServicesClient client = getClient(serverTemplateId, containerId);

        if (correlationKey != null && !correlationKey.isEmpty()) {

            CorrelationKey actualCorrelationKey = new RemoteCorrelationKey(correlationKey);

            return client.startProcess(containerId, processId, actualCorrelationKey, params);

        }

        return client.startProcess(containerId, processId, params);
    }

    @Override
    public List<String> getAvailableSignals(String serverTemplateId, String containerId, Long processInstanceId) {
        ProcessServicesClient client = getClient(serverTemplateId, containerId);

        return client.getAvailableSignals(containerId, processInstanceId);
    }

    @Override
    public void signalProcessInstance(String serverTemplateId, String containerId, Long processInstanceId, String signal, Object event) {
        ProcessServicesClient client = getClient(serverTemplateId, containerId);

        client.signalProcessInstance(containerId, processInstanceId, signal, event);
    }

    @Override
    public void signalProcessInstances(String serverTemplateId, List<String> containers, List<Long> processInstanceId, String signal, Object event) {
        if (new HashSet<String>(containers).size() == 1) {
            ProcessServicesClient client = getClient(serverTemplateId, containers.get(0));
            client.signalProcessInstances(containers.get(0), processInstanceId, signal, event);
        } else {
            for (int i = 0; i < processInstanceId.size(); i++) {
                ProcessServicesClient client = getClient(serverTemplateId, containers.get(i));
                client.signalProcessInstance(containers.get(i), processInstanceId.get(i), signal, event);
            }
        }
    }

    @Override
    public void setProcessVariable(String serverTemplateId, String containerId, long processInstanceId, String variableName, String value) {
        ProcessServicesClient client = getClient(serverTemplateId, containerId);

        client.setProcessVariable(containerId, processInstanceId, variableName, value);
    }

    protected ProcessServicesClient getClient(String serverTemplateId, String containerId) {
        KieServicesClient client = kieServerIntegration.getServerClient(serverTemplateId, containerId);
        if (client == null) {
            throw new RuntimeException("No client to interact with server " + serverTemplateId);
        }

        return client.getServicesClient(ProcessServicesClient.class);
    }
}
