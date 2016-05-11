/*
 * Copyright 2013 JBoss by Red Hat.
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

package org.jbpm.console.ng.es.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jbpm.console.ng.es.model.ErrorSummary;
import org.jbpm.console.ng.es.model.RequestDetails;
import org.jbpm.console.ng.es.model.RequestSummary;
import org.jbpm.console.ng.ga.model.QueryFilter;
import org.uberfire.paging.PageResponse;

@Remote
public interface ExecutorServiceEntryPoint {

    public RequestDetails getRequestDetails(String serverTemplateId, Long requestId);

    public Long scheduleRequest(String serverTemplateId, String commandName, Map<String, String> ctx);

    public Long scheduleRequest(String serverTemplateId, String commandId, Date date, Map<String, String> ctx);

    public void cancelRequest(String serverTemplateId, Long requestId);

    public void requeueRequest(String serverTemplateId, Long requestId);

    public void init();

    public void destroy();

    public Boolean isActive();

    public Boolean startStopService(int waitTime, int nroOfThreads);

    public int getInterval();

    public void setInterval(int waitTime);

    public int getRetries();

    public void setRetries(int defaultNroOfRetries);

    public int getThreadPoolSize();

    public void setThreadPoolSize(int nroOfThreads);
    
    public PageResponse<RequestSummary> getData(QueryFilter filter);

    public boolean isExecutorDisabled();

}
