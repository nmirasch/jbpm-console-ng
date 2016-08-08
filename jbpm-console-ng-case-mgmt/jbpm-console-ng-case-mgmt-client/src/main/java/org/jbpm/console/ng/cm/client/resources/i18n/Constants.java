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

package org.jbpm.console.ng.cm.client.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface Constants extends Messages {

    Constants INSTANCE = GWT.create(Constants.class);

    String New_Case();

    String Filters();

    String Id();

    String Case();

    String Description();

    String Status();

    String Actions();

    String Create_Case();

    String Name();

    String No_Cases_Found();

    String Cases_List();
   
    String Loading();

    String Create();

    String Case_Must_Have_A_Name();

    String New_Case_Instance();

    String Provide_Case_Name();
    
    String CaseCreatedWithId(String caseId);
    
    String Basic();
    
    String Advanced();
    
    String Case_Name();
    
    String Case_Template();
    
    String DeploymentId();

    String Create_Task();

    String Create_Process();

    String Create_SubCase();
    
    String Case_Details();

    String Close();

    String Details();

    String Milestones();

    String Terminate();

    String Select_Server_Template();

    String Complete();

    String Activate();

    String Comments();

}