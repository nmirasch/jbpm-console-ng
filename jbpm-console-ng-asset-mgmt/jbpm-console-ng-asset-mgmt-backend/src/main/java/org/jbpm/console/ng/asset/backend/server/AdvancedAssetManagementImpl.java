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
package org.jbpm.console.ng.asset.backend.server;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.guvnor.asset.management.model.BuildProjectStructureEvent;
import org.guvnor.asset.management.model.ConfigureRepositoryEvent;
import org.guvnor.asset.management.model.ExecuteOperationEvent;
import org.guvnor.asset.management.model.PromoteChangesEvent;
import org.guvnor.asset.management.model.ReleaseProjectEvent;
import org.guvnor.rest.client.JobRequest;
import org.guvnor.rest.client.JobStatus;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutorService;

@ApplicationScoped
public class AdvancedAssetManagementImpl {

    @Inject
    private Instance<ExecutorService> executorService;

    public AdvancedAssetManagementImpl() {
    }

    public void configureRepository(@Observes ConfigureRepositoryEvent event) {

    }

    public void buildProject(@Observes BuildProjectStructureEvent event) {

    }

    public void promoteChanges(@Observes PromoteChangesEvent event) {

    }

    public void releaseProject(@Observes ReleaseProjectEvent event) {

    }

    public void executeOperation(@Observes ExecuteOperationEvent event) {

        Map<String, Object> parameters = event.getParams();

        String command = (String) parameters.remove("CommandClass");
        JobRequest request = (JobRequest) parameters.get("JobRequest");
        request.setStatus(JobStatus.APPROVED);
        CommandContext ctx = new CommandContext();
        ctx.setData(parameters);
        executorService.get().scheduleRequest(command, ctx);

    }
}
