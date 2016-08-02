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

package org.jbpm.console.ng.cm.client.stages;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.console.ng.cm.client.resources.CaseManagementResources;

@Dependent
@Templated
public class CaseStagesViewImpl extends Composite implements CaseStagesPresenter.View {

    @DataField("stages")
    Div stages = GWT.create(Div.class);

    @PostConstruct
    public void init() {
        stages.addStyleName(CaseManagementResources.INSTANCE.css().stages());
        addStage("Apply for Refinance", true);
        addStage("Collect background data", true);
        addStage("Signatures", false);
        addStage("Submit claim", false);
    }

    private void addStage(String name, boolean done) {
        final Anchor stage = GWT.create(Anchor.class);
        stage.setText(name);
        if (done) {
            stage.addStyleName(CaseManagementResources.INSTANCE.css().stageCompleted());
        }
        stages.add(stage);
    }

    @Override
    public void init(final CaseStagesPresenter presenter) {
    }

}