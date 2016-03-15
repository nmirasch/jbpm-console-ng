package org.jbpm.console.ng.pr.backend.server.integration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.bd.integration.KieServerIntegration;
import org.jbpm.console.ng.pr.service.integration.RemoteImageService;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UIServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ApplicationScoped
public class RemoteImageServiceImpl implements RemoteImageService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteImageServiceImpl.class);


    @Inject
    private KieServerIntegration kieServerIntegration;


    protected UIServicesClient getClient(String serverTemplateId, String containerId) {
        KieServicesClient client = kieServerIntegration.getServerClient(serverTemplateId, containerId);
        if (client == null) {
            throw new RuntimeException("No client to interact with server " + serverTemplateId);
        }

        return client.getServicesClient(UIServicesClient.class);
    }

    @Override
    public String getProcessInstanceDiagram(String serverTemplateId, String containerId, Long processInstanceId) {

        UIServicesClient uiServicesClient = getClient(serverTemplateId, containerId);

        String imageContent = uiServicesClient.getProcessInstanceImage(containerId, processInstanceId);

        return imageContent;
    }

    @Override
    public String getProcessDiagram(String serverTemplateId, String containerId, String processId) {
        UIServicesClient uiServicesClient = getClient(serverTemplateId, containerId);

        String imageContent = uiServicesClient.getProcessImage(containerId, processId);

        return imageContent;
    }
}
