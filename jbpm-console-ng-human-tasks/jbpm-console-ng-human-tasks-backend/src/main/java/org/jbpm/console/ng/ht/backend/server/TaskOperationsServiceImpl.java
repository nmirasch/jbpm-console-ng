/*
 * Copyright 2014 JBoss by Red Hat.
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

package org.jbpm.console.ng.ht.backend.server;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.ht.model.TaskAssignmentSummary;
import org.jbpm.console.ng.ht.model.TaskSummary;
import org.jbpm.console.ng.ht.service.TaskOperationsService;

@Service
@ApplicationScoped
public class TaskOperationsServiceImpl implements TaskOperationsService {


    @Override
    public long addQuickTask(
                         final String taskName,
                         int priority,
                         Date dueDate, final List<String> users, List<String> groups, String identity, boolean start,
                         boolean claim,String taskformName,String deploymentId, Long processInstanceId){

        
        return -1;
    }

    @Override
    public void updateTask(long taskId, int priority, List<String> taskDescription,
            Date dueDate) {

    }
  
  
    @Override
    public TaskSummary getTaskDetails(long taskId) {

        return null;
    }

    @Override
    public long saveContent(long taskId, Map<String, Object> values) {
        return -1;
    }
    
    @Override
    public boolean existInDatabase(long taskId) {
        return false;
    }
  
    @Override
    public TaskAssignmentSummary getTaskAssignmentDetails(String serverTemplateId, String containerId, long taskId) {

        return null;
    }

    @Override
   	public void executeReminderForTask(long taskId,String fromUser) {

   	}

    /**
     * Checks if the user is allowed to delegate the given task
     *
     * @param taskId
     * @param userId
     * @param groups
     * @return
     */
    @Override
    public Boolean allowDelegate(long taskId, final String userId, final Set<String> groups) {

        return false;
    }

}