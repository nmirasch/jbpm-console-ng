/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.console.ng.pr.client.editors.definition.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.view.client.Range;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jbpm.console.ng.ga.model.PortableQueryFilter;
import org.jbpm.console.ng.gc.client.list.base.AbstractListView.ListView;
import org.jbpm.console.ng.gc.client.list.base.AbstractScreenListPresenter;
import org.jbpm.console.ng.pr.client.i18n.Constants;
import org.jbpm.console.ng.pr.forms.client.display.providers.StartProcessFormDisplayProviderImpl;
import org.jbpm.console.ng.pr.forms.client.display.views.PopupFormDisplayerView;
import org.jbpm.console.ng.pr.forms.display.process.api.ProcessDisplayerConfig;
import org.jbpm.console.ng.bd.model.ProcessDefinitionKey;
import org.jbpm.console.ng.bd.model.ProcessSummary;
import org.jbpm.console.ng.pr.service.ProcessDefinitionService;
import org.jbpm.console.ng.pr.service.integration.RemoteRuntimeDataService;
import org.kie.server.controller.api.model.events.ServerTemplateDeleted;
import org.kie.server.controller.api.model.events.ServerTemplateUpdated;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.workbench.common.screens.server.management.service.SpecManagementService;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;
import org.uberfire.ext.widgets.common.client.menu.RefreshMenuBuilder;
import org.uberfire.paging.PageResponse;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

@Dependent
@WorkbenchScreen(identifier = "Process Definition List")
public class ProcessDefinitionListPresenter extends AbstractScreenListPresenter<ProcessSummary> {

    @Inject
    PopupFormDisplayerView formDisplayPopUp;

    @Inject
    StartProcessFormDisplayProviderImpl startProcessDisplayProvider;



    public interface ProcessDefinitionListView extends ListView<ProcessSummary, ProcessDefinitionListPresenter> {
        String getSelectedServer();

        void setSelectedServer(String selected);

        void addServerTemplate(AnchorListItem serverTemplateNavLink);

        void removeServerTemplate(String serverTemplateId);
    }

    @Inject
    private ProcessDefinitionListView view;

    @Inject
    private Caller<ProcessDefinitionService> processDefinitionService;

    @Inject
    private Caller<SpecManagementService> specManagementService;
    @Inject
    private Caller<RemoteRuntimeDataService> remoteRuntimeDataService;

    private Constants constants = GWT.create( Constants.class );

    public ProcessDefinitionListPresenter() {
        super();
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return constants.Process_Definitions();
    }

    @WorkbenchPartView
    public UberView<ProcessDefinitionListPresenter> getView() {
        return view;
    }

    public void openGenericForm( final String processDefId,
                                 final String deploymentId,
                                 final String processDefName ) {

        ProcessDisplayerConfig config = new ProcessDisplayerConfig(new ProcessDefinitionKey(view.getSelectedServer(), deploymentId, processDefId), processDefName);

        formDisplayPopUp.setTitle(processDefName);

        startProcessDisplayProvider.setup(config, formDisplayPopUp);
    }

    @Override
    protected ListView getListView() {
        return view;
    }

    @Override
    public void getData(Range visibleRange) {
        ColumnSortList columnSortList = view.getListGrid().getColumnSortList();
        if ( currentFilter == null ) {
            currentFilter = new PortableQueryFilter( visibleRange.getStart(),
                    visibleRange.getLength(),
                    false, "",
                    ( columnSortList.size() > 0 ) ? columnSortList.get( 0 )
                            .getColumn().getDataStoreName() : "",
                    ( columnSortList.size() > 0 ) ? columnSortList.get( 0 )
                            .isAscending() : true );
        }
        // If we are refreshing after a search action, we need to go back to offset 0
        if ( currentFilter.getParams() == null || currentFilter.getParams().isEmpty()
                || currentFilter.getParams().get( "textSearch" ) == null || currentFilter.getParams().get( "textSearch" ).equals( "" ) ) {
            currentFilter.setOffset( visibleRange.getStart() );
            currentFilter.setCount( visibleRange.getLength() );
        } else {
            currentFilter.setOffset( 0 );
            currentFilter.setCount( view.getListGrid().getPageSize() );
        }

        currentFilter.setOrderBy( ( columnSortList.size() > 0 ) ? columnSortList.get( 0 )
                .getColumn().getDataStoreName() : "" );
        currentFilter.setIsAscending((columnSortList.size() > 0) ? columnSortList.get(0)
                .isAscending() : true);

        remoteRuntimeDataService.call( new RemoteCallback<List<ProcessSummary>>() {
            @Override
            public void callback( List<ProcessSummary> processDefsSums ) {

                PageResponse<ProcessSummary> response = new PageResponse<ProcessSummary>();

                response.setStartRowIndex(currentFilter.getOffset());
                response.setTotalRowSize(processDefsSums.size());
                response.setPageRowList(processDefsSums);
                response.setTotalRowSizeExact( processDefsSums.isEmpty() );
                if ( processDefsSums.size() < visibleRange.getLength() ) {
                    response.setLastPage( true );
                } else {
                    response.setLastPage( false );
                }

                updateDataOnCallback(response);
            }
        }, new ErrorCallback<Message>() {
            @Override
            public boolean error( Message message,
                                  Throwable throwable ) {
                view.hideBusyIndicator();
                view.displayNotification(constants.ErrorRetrievingProcessDefinitions(throwable.getMessage()));
                GWT.log( throwable.toString() );
                return true;
            }
        } ).getProcesses(view.getSelectedServer(), currentFilter.getOffset() / currentFilter.getCount(), currentFilter.getCount());
    }

    @WorkbenchMenu
    public Menus buildMenu() {
        return MenuFactory
                .newTopLevelCustomMenu(new RefreshMenuBuilder(this))
                .endMenu()
                .build();
    }

    public void loadServerTemplates() {
        specManagementService.call( new RemoteCallback<Collection<ServerTemplate>>() {
            @Override
            public void callback( final Collection<ServerTemplate> serverTemplates ) {

                for (ServerTemplate serverTemplate : serverTemplates) {
                    if (serverTemplate.getServerInstanceKeys() != null && !serverTemplate.getServerInstanceKeys().isEmpty()) {
                        AnchorListItem serverTemplateNavLink = new AnchorListItem(serverTemplate.getId());
                        serverTemplateNavLink.setIcon(IconType.BAN);
                        serverTemplateNavLink.setIconFixedWidth(true);
                        serverTemplateNavLink.addClickHandler(new SelectServerTemplateClickHandler(serverTemplate.getId()));

                        view.addServerTemplate(serverTemplateNavLink);
                    }
                }
            }
        } ).listServerTemplates();
    }

    private class SelectServerTemplateClickHandler implements ClickHandler {

        private String selected;

        public SelectServerTemplateClickHandler(String selected) {
            this.selected = selected;
        }

        @Override
        public void onClick( ClickEvent event ) {
            view.setSelectedServer(selected);
            refreshGrid();
        }
    }

    public void onServerTemplateDeleted(@Observes ServerTemplateDeleted serverTemplateDeleted) {
        view.removeServerTemplate(serverTemplateDeleted.getServerTemplateId());
    }

    public void onServerTemplateUpdated(@Observes ServerTemplateUpdated serverTemplateUpdated) {
        ServerTemplate serverTemplate = serverTemplateUpdated.getServerTemplate();
        if (serverTemplate.getServerInstanceKeys() == null || serverTemplate.getServerInstanceKeys().isEmpty()) {
            view.removeServerTemplate(serverTemplate.getId());
        }
    }

}