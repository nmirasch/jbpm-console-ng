/*
 * Copyright 2012 JBoss by Red Hat.
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

package org.jbpm.console.ng.ht.forms.backend.server;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.bd.integration.KieServerIntegration;
import org.jbpm.console.ng.ga.forms.service.FormServiceEntryPoint;
import org.jbpm.formModeler.kie.services.form.FormManagerService;
import org.jbpm.formModeler.kie.services.form.FormProvider;
import org.jbpm.formModeler.kie.services.form.TaskDefinition;
import org.kie.internal.task.api.ContentMarshallerContext;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UIServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ApplicationScoped
public class FormServiceEntryPointImpl implements FormServiceEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(FormServiceEntryPointImpl.class);

    @Inject
    private KieServerIntegration kieServerIntegration;

    @Inject
    private FormManagerService formManagerService;

    private Set<FormProvider> providers;

    @Inject
    @Any
    private Instance<FormProvider> providersInjected;

    @PostConstruct
    public void prepare() {
        Set<FormProvider> providers = new TreeSet<FormProvider>(new Comparator<FormProvider>() {

            @Override
            public int compare(FormProvider o1, FormProvider o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });
        for (FormProvider p : providersInjected) {
            providers.add(p);
        }

        this.providers = providers;
    }

    @Override
    public String getFormDisplayTask(String serverTemplateId, String domainId, long taskId) {
        String registrationKey = serverTemplateId + "@" + domainId + "@" + System.currentTimeMillis();
        KieServicesClient client = kieServerIntegration.getServerClient(serverTemplateId, domainId);

        if (client == null) {
            throw new RuntimeException("No client to interact with server " + serverTemplateId);
        }
        // get form content
        UIServicesClient uiServicesClient = client.getServicesClient(UIServicesClient.class);
        String formContent = uiServicesClient.getTaskForm(domainId, taskId);

        // get task with inputs and outputs
        UserTaskServicesClient taskClient = client.getServicesClient(UserTaskServicesClient.class);
        TaskInstance task = taskClient.getTaskInstance(domainId, taskId, true, true, false);
        if (task == null) {
            throw new RuntimeException("No task found for id " + taskId);
        }

        TaskDefinition taskInstance = new TaskDefinition();
        taskInstance.setId(task.getId());
        taskInstance.setName(task.getName());
        taskInstance.setDescription(task.getDescription());
        taskInstance.setFormName(task.getFormName());
        taskInstance.setDeploymentId(registrationKey);
        taskInstance.setProcessId(task.getProcessId());

        taskInstance.setStatus(task.getStatus());

        // prepare render context
        Map<String, Object> renderContext = new HashMap<String, Object>();
        renderContext.put("task", taskInstance);
        renderContext.put("marshallerContext", new ContentMarshallerContext(null, client.getClassLoader()));

        if (task.getInputData() != null && !task.getInputData().isEmpty()) {
            renderContext.put("inputs", task.getInputData());
            renderContext.putAll(task.getInputData());
        }

        if (task.getOutputData() != null && !task.getOutputData().isEmpty()) {
            renderContext.put("outputs", task.getOutputData());
            renderContext.putAll(task.getOutputData());

            taskInstance.setOutputIncluded(true);
        }

        if (formContent != null) {


            formManagerService.registerForm(registrationKey, task.getFormName() +"-taskform.form", formContent);
        }

        try {
            for (FormProvider provider : providers) {
                String template = provider.render(task.getName(), taskInstance, null, renderContext);
                if (template != null && !template.trim().isEmpty()) {
                    return template;
                }
            }
        } finally {
            formManagerService.unRegisterForms(registrationKey);
        }

        return "";
    }

    @Override
    public String getFormDisplayProcess(String serverTemplateId, String domainId, String processId) {

        KieServicesClient client = kieServerIntegration.getServerClient(serverTemplateId, domainId);

        if (client == null) {
            throw new RuntimeException("No client to interact with server " + serverTemplateId);
        }

        ProcessServicesClient processClient = client.getServicesClient(ProcessServicesClient.class);

        ProcessDefinition processDefinition = processClient.getProcessDefinition(domainId, processId);

        org.jbpm.formModeler.kie.services.form.ProcessDefinition processDesc = new org.jbpm.formModeler.kie.services.form.ProcessDefinition();
        processDesc.setId(processDefinition.getId());
        processDesc.setName(processDefinition.getName());
        processDesc.setPackageName(processDefinition.getPackageName());
        processDesc.setDeploymentId(serverTemplateId + "@" + processDefinition.getContainerId() + "@" + System.currentTimeMillis());

        Map<String, String> processData = processDefinition.getProcessVariables();

        if (processData == null) {
            processData = new HashMap<String, String>();
        }

        Map<String, Object> renderContext = new HashMap<String, Object>();
        renderContext.put("process", processDesc);
        renderContext.put("outputs", processData);
        renderContext.put("marshallerContext", new ContentMarshallerContext(null, client.getClassLoader()));

        UIServicesClient uiServicesClient = client.getServicesClient(UIServicesClient.class);

        String formContent = uiServicesClient.getProcessForm(domainId, processId);
        if (formContent != null) {

            formManagerService.registerForm(processDesc.getDeploymentId(), processDesc.getId() +"-taskform.form", formContent);

        }

        try {

            for (FormProvider provider : providers) {
                String template = provider.render(processDesc.getName(), processDesc, renderContext);
                if (template != null && !template.trim().isEmpty()) {
                    return template;
                }
            }
        } finally {
            formManagerService.unRegisterForms(processDesc.getDeploymentId());
        }
        logger.warn("Unable to find form to render for process '{}'", processDesc.getName());
        return "";

    }

}
