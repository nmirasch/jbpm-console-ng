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

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.Button;

import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.MouseEvent;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.workbench.pr.client.resources.i18n.Constants;
import org.jbpm.workbench.pr.client.util.LogUtils.LogOrder;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.jbpm.workbench.pr.model.ProcessInstanceLogSummary;

import static org.jboss.errai.common.client.dom.DOMUtil.addCSSClass;
import static org.jboss.errai.common.client.dom.DOMUtil.removeCSSClass;

@Dependent
@Templated(value = "ProcessInstanceLogViewImpl.html", stylesheet = "/org/jbpm/workbench/common/client/resources/css/kie-manage.less")
public class ProcessInstanceLogViewImpl
        implements ProcessInstanceLogPresenter.ProcessInstanceLogView {

    // Can't inject this because many uberfire widgets extend it.

    @Inject
    @DataField("logs_container")
    private Div logsContainer;

    @Inject
    @DataField
    public Button showAscLogButton;

    @Inject
    @DataField
    public Button showDescLogButton;

    @Inject
    @DataField("load-div")
    Div loadDiv;

    @Inject
    @DataField("load-more-logs")
    @SuppressWarnings("PMD.UnusedPrivateField")
    private Button loadMoreLogs;

    @Inject
    @Bound
    @DataField("logs-list")
    @SuppressWarnings("unused")
    private ListComponent<ProcessInstanceLogSummary, ProcessInstanceLogItemView> logs;

    @Inject
    @AutoBound
    private DataBinder<List<ProcessInstanceLogSummary>> logsList;

    @Inject
    @DataField("empty-list-item")
    private Div emptyContainer;

    private ProcessInstanceLogPresenter presenter;

    private LogOrder logOrder = LogOrder.ASC;

    private Constants constants = Constants.INSTANCE;

    @Override
    public void init(final ProcessInstanceLogPresenter presenter) {
        this.presenter = presenter;

        this.setOrder(showAscLogButton,
                      constants.Asc_Log_Order(),
                      LogOrder.ASC);
        this.setOrder(showDescLogButton,
                      constants.Desc_Log_Order(),
                      LogOrder.DESC);
    }

    private void setOrder(Button button,
                          String description,
                          final LogOrder logOrder) {
        button.setText(description);
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setActiveLogOrderButton(logOrder);
                getInstanceData();
            }
        });
    }

    public void getInstanceData() {
        presenter.resetLogsList();
        presenter.loadProcessInstanceLogs(logOrder);
    }

    @Override
    public void setLogsList(final List<ProcessInstanceLogSummary> processInstanceLogSummaries) {
        logsList.setModel(new ArrayList<>());
        logsList.setModel(processInstanceLogSummaries);
        if (processInstanceLogSummaries.isEmpty()) {
            removeCSSClass(emptyContainer,
                           "hidden");
        } else {
            addCSSClass(emptyContainer,
                        "hidden");
        }
    }

    public void setActiveLogOrderButton(LogOrder logOrder) {
        this.logOrder = logOrder;
        switch (logOrder) {
            case ASC:
                showAscLogButton.setActive(true);
                showDescLogButton.setActive(false);
                break;
            case DESC:
                showDescLogButton.setActive(true);
                showAscLogButton.setActive(false);
                break;
        }
    }

    @Override
    public void hideLoadButton(boolean hidden) {
        loadDiv.setHidden(hidden);
    }

    @EventHandler("load-more-logs")
    public void loadMoreComments(final @ForEvent("click") MouseEvent event) {
        presenter.loadMoreProcessInstanceLogs(logOrder);;
    }

    @Override
    public HTMLElement getElement() {
        return logsContainer;
    }
}