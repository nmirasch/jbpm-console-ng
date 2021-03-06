/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.workbench.wi.workitems.service;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jbpm.workbench.wi.workitems.model.ServiceTaskSummary;
import org.jbpm.workbench.wi.workitems.model.ServiceTasksConfiguration;

@Remote
public interface ServiceTaskService {

    List<ServiceTaskSummary> getServiceTasks();
    
    List<ServiceTaskSummary> getEnabledServiceTasks();
    
    void enableServiceTask(String id);
    
    void disableServiceTask(String id);
    
    void installServiceTask(String id, String target, List<String> parameters);
    
    void uninstallServiceTask(String id, String target);
    
    ServiceTasksConfiguration getConfiguration();

    void saveConfiguration(ServiceTasksConfiguration configuration);

    List<String> addServiceTasks(String location);
}
