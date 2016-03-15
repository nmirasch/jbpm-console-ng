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


import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.bd.service.KieSessionEntryPoint;
import org.jbpm.console.ng.ht.forms.modeler.service.FormModelerProcessStarterEntryPoint;
import org.jbpm.console.ng.ht.service.integration.RemoteTaskService;
import org.jbpm.console.ng.pr.service.integration.RemoteProcessService;
import org.jbpm.formModeler.api.client.FormRenderContextManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import org.jbpm.console.ng.ht.service.TaskLifeCycleService;
import org.jbpm.console.ng.ht.service.TaskOperationsService;

@Service
@ApplicationScoped
public class FormModelerProcessStarterEntryPointImpl implements FormModelerProcessStarterEntryPoint {
    @Inject
    private FormRenderContextManager formRenderContextManager;

    @Inject
    private RemoteProcessService remoteProcessService;

    @Inject
    private RemoteTaskService taskService;

    @Override
    public Long startProcessFromRenderContext(String ctxUID, String serverTemplateId, String domainId, String processId, String correlationKey, Long parentProcessInstanceId) {
        Map<String, Object> params = formRenderContextManager.getFormRenderContext(ctxUID).getOutputData();
        formRenderContextManager.removeContext(ctxUID);

        return remoteProcessService.startProcess(serverTemplateId, domainId, processId, correlationKey, params);

    }

    @Override
    public Long saveTaskStateFromRenderContext(String ctxUID, String serverTemplateId, String containerId, Long taskId, boolean clearStatus) {
        Map<String, Object> params = formRenderContextManager.getFormRenderContext(ctxUID).getOutputData();
        if (clearStatus) formRenderContextManager.removeContext(ctxUID);
        taskService.saveTaskContent(serverTemplateId, containerId, taskId, params);

        return -1l;
    }

    @Override
    public Long saveTaskStateFromRenderContext(String ctxUID, String serverTemplateId, String containerId, Long taskId) {
        return saveTaskStateFromRenderContext(ctxUID, serverTemplateId, containerId, taskId, false);
    }

    @Override
    public void completeTaskFromContext(String ctxUID, String serverTemplateId, String containerId, Long taskId) {
        Map<String, Object> params = formRenderContextManager.getFormRenderContext(ctxUID).getOutputData();
        formRenderContextManager.removeContext(ctxUID);
        taskService.completeTask(serverTemplateId, containerId, taskId, params);
    }

    @Override
    public void clearContext(String ctxUID) {
        formRenderContextManager.removeContext(ctxUID);
    }
}
