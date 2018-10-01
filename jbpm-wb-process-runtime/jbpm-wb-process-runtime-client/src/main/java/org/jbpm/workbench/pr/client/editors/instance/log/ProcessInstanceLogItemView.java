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
package org.jbpm.workbench.pr.client.editors.instance.log;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.TakesValue;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import org.jbpm.workbench.common.client.util.DateUtils;
import org.jbpm.workbench.pr.client.resources.i18n.Constants;
import org.jbpm.workbench.pr.model.ProcessInstanceLogSummary;


@Dependent
@Templated
public class ProcessInstanceLogItemView implements TakesValue<ProcessInstanceLogSummary>,
                                                   IsElement {
    private Constants constants = Constants.INSTANCE;

    public static String NODE_HUMAN_TASK = "HumanTaskNode";
    public static String NODE_START = "StartNode";
    public static String NODE_END = "EndNode";

    @Inject
    @DataField("list-group-item")
    Div listGroupItem;

    @Inject
    @DataField("detailsPanelDiv")
    Div detailsPanelDiv;

    @Inject
    @DataField("logIcon")
    Span logIcon;

    @Inject
    @DataField("logTime")
    Span logTime;

    @Inject
    @DataField("nodeTypeDesc")
    Span nodeTypeDesc;

    @Inject
    @DataField("logCompleted")
    Span logCompleted;

    @Inject
    @AutoBound
    private DataBinder<ProcessInstanceLogSummary> logSummary;

    @Override
    public HTMLElement getElement() {
        return listGroupItem;
    }

    @Override
    public ProcessInstanceLogSummary getValue() {
        return this.logSummary.getModel();
    }

    @Override
    public void setValue(final ProcessInstanceLogSummary model) {
        String iconClass ="list-view-pf-icon-sm kie-timeline-list-view-pf-icon-sm";
        this.logSummary.setModel(model);
        logTime.setTextContent(DateUtils.getPrettyTime(model.getDate()));

        String agent = constants.System();

        if (!NODE_HUMAN_TASK.equals(model.getNodeType())){
            detailsPanelDiv.setHidden(true);
        }

        if (NODE_HUMAN_TASK.equals(model.getNodeType()) ||
                (NODE_START.equals(model.getNodeType()) && !model.isCompleted())) {
            iconClass += " fa fa-user";
            agent = constants.Human();
        } else {
            iconClass += " fa fa-cogs";
        }
        if(model.isCompleted()) {
            iconClass += " kie-timeline-icon--completed";
            logCompleted.setTextContent( constants.NodeWasLeft(agent));
        } else {
            logCompleted.setTextContent( constants.NodeWasEntered(agent));
        }
        logIcon.setClassName(iconClass);
        nodeTypeDesc.setTextContent(getLogTitle(model));
    }

    private String getLogTitle(ProcessInstanceLogSummary logsum){
        if (NODE_HUMAN_TASK.equals(logsum.getNodeType())){
            return constants.Task(logsum.getName());
        }
        return logsum.getNodeType();
    }

}
