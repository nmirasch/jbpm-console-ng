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
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.DataSetReadyCallback;

import org.dashbuilder.dataset.sort.SortOrder;
import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jbpm.workbench.df.client.filter.FilterSettings;
import org.jbpm.workbench.df.client.list.DataSetQueryHelper;

import org.jbpm.workbench.pr.model.ProcessInstanceLogSummary;
import org.jbpm.workbench.pr.client.util.LogUtils.LogOrder;
import org.jbpm.workbench.pr.events.ProcessInstanceSelectionEvent;

import org.uberfire.client.mvp.UberElement;
import org.uberfire.client.workbench.widgets.common.ErrorPopupPresenter;

import static org.jbpm.workbench.pr.model.ProcessInstanceLogDataSetConstants.*;

@Dependent
public class ProcessInstanceLogPresenter {

    public static String NODE_HUMAN_TASK = "HumanTaskNode";
    public static String NODE_START = "StartNode";
    public static String NODE_END = "EndNode";

    private String serverTemplateId;

    @Inject
    private ProcessInstanceLogView view;

    int currentPage = 0;
    public static final int PAGE_SIZE = 5;

    List<ProcessInstanceLogSummary> visibleLogs = new ArrayList<ProcessInstanceLogSummary>();

    public int getPageSize() {
        return PAGE_SIZE;
    }

    public void setCurrentPage(int i) {
        this.currentPage  = i;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    protected DataSetQueryHelper dataSetQueryHelper;

    protected ProcessInstanceLogFilterSettingsManager filterSettingsManager;

    @Inject
    protected ErrorPopupPresenter errorPopup;

    @Inject
    public void setDataSetQueryHelper(final DataSetQueryHelper dataSetQueryHelper) {
        this.dataSetQueryHelper = dataSetQueryHelper;
    }

    @Inject
    public void setFilterSettingsManager(final ProcessInstanceLogFilterSettingsManager filterSettingsManager) {
        this.filterSettingsManager = filterSettingsManager;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public IsWidget getWidget() {
        return ElementWrapperWidget.getWidget(view.getElement());
    }

    public void setServerTemplateId(String serverTemplateId) {
        this.serverTemplateId = serverTemplateId;
    }

    public void loadProcessInstanceLogs(final LogOrder logOrder) {
        try {
            final FilterSettings currentTableSettings = dataSetQueryHelper.getCurrentTableSettings();
            currentTableSettings.setServerTemplateId(this.serverTemplateId);
            currentTableSettings.setTablePageSize(PAGE_SIZE);
            dataSetQueryHelper.setLastOrderedColumn(COLUMN_LOG_DATE);
            dataSetQueryHelper.setLastSortOrder(logOrder.equals(LogOrder.ASC) ? SortOrder.ASCENDING : SortOrder.DESCENDING);

            dataSetQueryHelper.setCurrentTableSettings(currentTableSettings);
            dataSetQueryHelper.setDataSetHandler(currentTableSettings);
            dataSetQueryHelper.lookupDataSet(
                    currentPage * getPageSize(),
                    new DataSetReadyCallback() {
                        @Override
                        public void callback(DataSet dataSet) {
                            if (dataSet != null && dataSetQueryHelper.getCurrentTableSettings().getKey().equals(currentTableSettings.getKey())) {
                                List<ProcessInstanceLogSummary> logs = new ArrayList<ProcessInstanceLogSummary>();
                                for (int i = 0; i < dataSet.getRowCount(); i++) {
                                    logs.add(new ProcessInstanceLogSummaryDataSetMapper().apply(dataSet,
                                                                                                i));
                                }
                                visibleLogs.addAll(logs);
                                view.hideLoadButton(logs.size() < PAGE_SIZE);
                                view.setLogsList(visibleLogs);
                            }
                        }

                        @Override
                        public void notFound() {
                            errorPopup.showMessage(org.jbpm.workbench.common.client.resources.i18n.Constants.INSTANCE.DataSetNotFound(currentTableSettings.getDataSet().getUUID()));
                        }

                        @Override
                        public boolean onError(ClientRuntimeError error) {
                            errorPopup.showMessage(org.jbpm.workbench.common.client.resources.i18n.Constants.INSTANCE.DataSetError(currentTableSettings.getDataSet().getUUID(),
                                                                                                                                   error.getMessage()));
                            return false;
                        }
                    });
        } catch (Exception e) {
            errorPopup.showMessage(org.jbpm.workbench.common.client.resources.i18n.Constants.INSTANCE.UnexpectedError(e.getMessage()));
        }
    }

    public void loadMoreProcessInstanceLogs(LogOrder logOrder) {
        setCurrentPage(currentPage + 1);
        loadProcessInstanceLogs(logOrder);
    }

    public void resetLogsList(){
        currentPage = 0;
        visibleLogs = new ArrayList<>();
    }

    public void onProcessInstanceSelectionEvent(@Observes final ProcessInstanceSelectionEvent event) {
        setServerTemplateId(event.getServerTemplateId());
        view.setActiveLogOrderButton(LogOrder.ASC);
        resetLogsList();
        dataSetQueryHelper.setCurrentTableSettings(filterSettingsManager.createDefaultFilterSettingsPrototype(event.getProcessInstanceId()));
        loadProcessInstanceLogs(LogOrder.ASC);
    }



    public interface ProcessInstanceLogView extends UberElement<ProcessInstanceLogPresenter> {

        void init(final ProcessInstanceLogPresenter presenter);

        void setActiveLogOrderButton(LogOrder logOrder);

        void setLogsList(final List<ProcessInstanceLogSummary> processInstanceLogSummaries);

        void hideLoadButton(boolean hidden);
    }

}