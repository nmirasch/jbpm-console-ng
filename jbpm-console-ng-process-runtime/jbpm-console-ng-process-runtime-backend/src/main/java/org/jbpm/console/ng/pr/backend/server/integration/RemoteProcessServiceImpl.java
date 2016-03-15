package org.jbpm.console.ng.pr.backend.server.integration;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.bd.integration.KieServerIntegration;
import org.jbpm.console.ng.pr.service.integration.RemoteProcessService;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ApplicationScoped
public class RemoteProcessServiceImpl implements RemoteProcessService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteProcessServiceImpl.class);

    private CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

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

            CorrelationKey actualCorrelationKey = correlationKeyFactory.newCorrelationKey(correlationKey);

            return client.startProcess(containerId, processId, actualCorrelationKey, params);

        }

        return client.startProcess(containerId, processId, params);
    }


    protected ProcessServicesClient getClient(String serverTemplateId, String containerId) {
        KieServicesClient client = kieServerIntegration.getServerClient(serverTemplateId, containerId);
        if (client == null) {
            throw new RuntimeException("No client to interact with server " + serverTemplateId);
        }

        return client.getServicesClient(ProcessServicesClient.class);
    }
}
