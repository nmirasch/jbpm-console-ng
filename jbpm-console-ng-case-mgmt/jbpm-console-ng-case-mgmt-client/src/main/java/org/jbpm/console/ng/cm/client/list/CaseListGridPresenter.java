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

package org.jbpm.console.ng.cm.client.list;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import org.jboss.errai.common.client.api.Caller;
import org.jbpm.console.ng.cm.client.resources.i18n.Constants;
import org.jbpm.console.ng.cm.client.perspectives.CaseDetailsPerspective;
import org.jbpm.console.ng.cm.client.quicknewcase.QuickNewCasePopup;
import org.jbpm.console.ng.cm.model.CaseSummary;
import org.jbpm.console.ng.cm.service.CaseInstanceService;
import org.jbpm.console.ng.ga.model.PortableQueryFilter;
import org.jbpm.console.ng.gc.client.list.base.AbstractListView.ListView;
import org.jbpm.console.ng.gc.client.list.base.AbstractScreenListPresenter;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.menu.RefreshMenuBuilder;
import org.uberfire.ext.widgets.common.client.menu.RefreshSelectorMenuBuilder;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.paging.PageResponse;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

@Dependent
@WorkbenchScreen(identifier = CaseListGridPresenter.SCREEN_ID)
public class CaseListGridPresenter extends AbstractScreenListPresenter<CaseSummary> {

    public static final String SCREEN_ID = "Case List";

    private Constants constants = Constants.INSTANCE;

    @Inject
    private CaseListView view;

    @Inject
    private Caller<CaseInstanceService> casesService;

    @Inject
    private QuickNewCasePopup newCasePopup;

    public CaseListGridPresenter() {
        dataProvider = new AsyncDataProvider<CaseSummary>() {
            @Override
            protected void onRangeChanged(HasData<CaseSummary> display) {
                view.showBusyIndicator(constants.Loading());
                final Range visibleRange = display.getVisibleRange();
                getData(visibleRange);
            }
        };
    }

    @Override
    protected ListView getListView() {
        return view;
    }

    @Override
    public void getData(Range visibleRange) {
        final ColumnSortList columnSortList = view.getListGrid().getColumnSortList();
        if ( currentFilter == null ) {
            currentFilter = new PortableQueryFilter( visibleRange.getStart(),
                    visibleRange.getLength(),
                    false, "",
                    columnSortList.size() > 0 ? columnSortList.get( 0 )
                            .getColumn().getDataStoreName() : "",
                    columnSortList.size() == 0 || columnSortList.get( 0 ).isAscending() );
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

        currentFilter.setOrderBy( columnSortList.size() > 0 ? columnSortList.get( 0 )
                .getColumn().getDataStoreName() : "" );
        currentFilter.setIsAscending(columnSortList.size() == 0 || columnSortList.get(0).isAscending());

        casesService.call((List<CaseSummary> cases) -> {
            final PageResponse<CaseSummary> response = new PageResponse<CaseSummary>();

            response.setStartRowIndex(currentFilter.getOffset());
            response.setTotalRowSize(cases.size());
            response.setPageRowList(cases);
            response.setTotalRowSizeExact(cases.isEmpty());
            if (cases.size() < visibleRange.getLength()) {
                response.setLastPage(true);
            } else {
                response.setLastPage(false);
            }

            updateDataOnCallback(response);
        }, new DefaultErrorCallback()).getCaseInstances(selectedServerTemplate);
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return constants.Cases_List();
    }

    @WorkbenchPartView
    public UberView<CaseListGridPresenter> getView() {
        return view;
    }

    @WorkbenchMenu
    public Menus buildMenu() {
        return MenuFactory
                .newTopLevelMenu(constants.New_Case_Instance())
                .respondsWith(() -> {
//                    if (selectedServerTemplate != null && !selectedServerTemplate.isEmpty()) {
                        newCasePopup.show(selectedServerTemplate);
//                    } else {
//                        view.displayNotification(constants.Select_Server_Template());
//                    }
                })
                .endMenu()
                .newTopLevelCustomMenu(serverTemplateSelectorMenuBuilder)
                .endMenu()
                .newTopLevelCustomMenu(new RefreshMenuBuilder(this))
                .endMenu()
                .newTopLevelCustomMenu(new RefreshSelectorMenuBuilder(this))
                .endMenu()
                .build();
    }

    protected void caseInstanceSelected(final String caseId){
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("caseId", caseId);
        final DefaultPlaceRequest caseDetails = new DefaultPlaceRequest(CaseDetailsPerspective.PERSPECTIVE_ID, parameters);
        GWT.log("status: " + placeManager.getStatus(CaseDetailsPerspective.PERSPECTIVE_ID));
//        placeManager.closePlace(CaseDetailsPerspective.PERSPECTIVE_ID);
        placeManager.goTo(caseDetails);
    }

    protected void closeCaseInstance(final String caseId){
        casesService.call(
                e -> refreshGrid(),
                new DefaultErrorCallback()
        ).closeCaseInstance(selectedServerTemplate, null, caseId);
    }

    protected void completeCaseInstance(final String caseId){
        casesService.call(
                e -> refreshGrid(),
                new DefaultErrorCallback()
        ).completeCaseInstance(selectedServerTemplate, null, caseId);
    }

    protected void terminateCaseInstance(final String caseId){
        casesService.call(
                e -> refreshGrid(),
                new DefaultErrorCallback()
        ).terminateCaseInstance(selectedServerTemplate, null, caseId);
    }

    protected void activateCaseInstance(final String caseId){
        casesService.call(
                e -> refreshGrid(),
                new DefaultErrorCallback()
        ).activateCaseInstance(selectedServerTemplate, null, caseId);
    }

    public interface CaseListView extends ListView<CaseSummary, CaseListGridPresenter> {

    }

}