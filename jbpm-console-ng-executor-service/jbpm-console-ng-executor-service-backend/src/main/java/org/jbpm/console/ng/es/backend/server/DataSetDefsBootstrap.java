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
package org.jbpm.console.ng.es.backend.server;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dashbuilder.dataset.def.DataColumnDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefFactory;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.jbpm.console.ng.bd.integration.KieServerIntegration;
import org.jbpm.dashboard.dataset.integration.KieServerDataSetProvider;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.client.KieServicesException;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.controller.api.model.events.ServerInstanceConnected;
import org.kie.server.controller.api.model.runtime.ServerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.async.DisposableExecutor;
import org.uberfire.commons.async.SimpleAsyncExecutorService;
import org.uberfire.commons.services.cdi.Startup;

import static org.jbpm.console.ng.es.model.RequestDataSetConstants.*;

@Startup
@ApplicationScoped
public class DataSetDefsBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(DataSetDefsBootstrap.class);

    @Inject
    protected DataSetDefRegistry dataSetDefRegistry;

    @Inject
    private KieServerIntegration kieServerIntegration;

    private DisposableExecutor executor;

    private DataSetDef requestListDef;

    @PostConstruct
    protected void registerDataSetDefinitions() {
        executor = SimpleAsyncExecutorService.getDefaultInstance();

        String jbpmDataSource = "${"+ KieServerConstants.CFG_PERSISTANCE_DS + "}";

        requestListDef = DataSetDefFactory.newSQLDataSetDef()
                .uuid(REQUEST_LIST_DATASET)
                .name("Request List")
                .dataSource(jbpmDataSource)
                .dbSQL("select id, timestamp, status, commandName, message, businessKey from RequestInfo", false)
                .number(COLUMN_ID)
                .date(COLUMN_TIMESTAMP)
                .label(COLUMN_STATUS)
                .label(COLUMN_COMMANDNAME)
                .label(COLUMN_MESSAGE)
                .label(COLUMN_BUSINESSKEY)
                .buildDef();

        // Hide all these internal data set from end user view
        requestListDef.setPublic(false);
        requestListDef.setProvider(KieServerDataSetProvider.TYPE);

        // Register the data set definitions
        dataSetDefRegistry.registerDataSetDef(requestListDef);
        logger.info("Executor service datasets registered");
    }

    public void registerInKieServer(@Observes final ServerInstanceConnected serverInstanceConnected) {
        final ServerInstance serverInstance = serverInstanceConnected.getServerInstance();
        final String serverInstanceId = serverInstance.getServerInstanceId();
        logger.debug("Server instance '{}' connected, registering job related data sets", serverInstanceId);

        executor.execute(new Runnable() {
            @Override
            public void run() {

                final String serverTemplateId = serverInstanceConnected.getServerInstance().getServerTemplateId();
                try {
                    long waitLimit = 5 * 60 * 1000;   // default 5 min
                    long elapsed = 0;

                    logger.info("Registering executor data set definition '{}' on connected server instance '{}'", requestListDef.getUUID(), serverInstanceId);
                    QueryServicesClient queryClient = kieServerIntegration.getAdminServerClient(serverTemplateId).getServicesClient(QueryServicesClient.class);
                    QueryDefinition definition = QueryDefinition.builder()
                            .name(requestListDef.getUUID())
                            .expression(((SQLDataSetDef) requestListDef).getDbSQL())
                            .source(((SQLDataSetDef) requestListDef).getDataSource())
                            .target("CUSTOM")
                            .build();
                    while (elapsed < waitLimit) {
                        try {

                            queryClient.replaceQuery(definition);
                            logger.info("Query {} definition successfully registered on kie server '{}'", REQUEST_LIST_DATASET, serverInstanceId);
                            return;
                        } catch (KieServicesException e) {
                            // unable to register, might still be booting
                            Thread.sleep(500);
                            elapsed += 500;
                            logger.debug("Cannot reach KIE Server, elapsed time while waiting '{}', max time '{}'", elapsed, waitLimit);

                        }
                    }
                    logger.warn("Timeout while trying to register query definition '{}' on '{}'", REQUEST_LIST_DATASET, serverInstanceId);
                } catch (Exception e) {
                    logger.warn("Unable to register query '{}' on '{}' due to {}", REQUEST_LIST_DATASET, serverInstanceId, e.getMessage(), e);
                }
            }
        });

    }

}
