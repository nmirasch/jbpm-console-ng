/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.console.ng.pr.backend.server.listener;

import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.internal.identity.IdentityProvider;

// TODO move this out from console and include in jbpm-services so it can be used in other places such as kie server
public class InitiatorProviderProcessListener extends DefaultProcessEventListener {
    
    private KieSession ksession;
    private IdentityProvider identityProvider;
    
    public InitiatorProviderProcessListener(KieSession ksession) {
        this.ksession = ksession;
    }
    
    private void resolveIdentityProvider() {
        if (identityProvider != null) {
            return;
        }
        Object identityProvider = ksession.getEnvironment().get("IdentityProvider");
        Environment env = ksession.getEnvironment();
        if (identityProvider instanceof IdentityProvider) {
            this.identityProvider = (IdentityProvider) identityProvider;
        }
    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        resolveIdentityProvider();
        if (identityProvider != null) {
            // TODO expose setVariable on kie-api level - ProcessInstance
//            WorkflowProcessInstance wpi = (WorkflowProcessInstance)event.getProcessInstance();
//            wpi.setVariable( "initiator", identityProvider.getName() );
        }
        
    }
}
