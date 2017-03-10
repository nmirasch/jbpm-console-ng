/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.workbench.cm.client.roles;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ioc.client.api.ManagedInstance;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.workbench.cm.client.pagination.PaginationViewImpl;
import org.jbpm.workbench.cm.client.util.CaseRolesAssignmentFilterBy;
import org.jbpm.workbench.cm.client.util.Select;
import org.uberfire.mvp.Command;

import static java.util.stream.Collectors.toMap;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.stream;
import static org.jboss.errai.common.client.dom.DOMUtil.*;

@Dependent
@Templated
public class CaseRolesViewImpl implements CaseRolesPresenter.CaseRolesView,PaginationViewImpl.PageList {
    public static int PAGE_SIZE = 3;

    @Inject
    @DataField("roles")
    private Div rolesContainer;

    @Inject
    @DataField("roles-badge")
    Span rolesBadge;

    @Inject
    @DataField("role-list")
    private Div roles;

    @Inject
    @DataField("filter-select")
    private Select filterSelect;

    @Inject
    @DataField("scrollbox")
    private Div scrollbox;

    @Inject
    @DataField("pagination")
    private PaginationViewImpl pagination;

    List allElementsList = new ArrayList();

    private Command filterCommand;

    @Inject
    private ManagedInstance<CaseRoleItemView> provider;

    @Inject
    private TranslationService translationService;

    @Override
    public void init(final CaseRolesPresenter presenter) {
    }

    @PostConstruct
    public void init() {
        stream(CaseRolesAssignmentFilterBy.values())
                .collect(toMap(s -> s.getLabel(),
                        s -> translationService.format(s.getLabel())))
                .entrySet()
                .forEach(s -> filterSelect.addOption(s.getValue(), s.getKey()));

       filterSelect.refresh();
    }

    @Override
    public void removeAllRoles() {
        removeAllChildren(roles);
        allElementsList = new ArrayList();
    }

    @Override
    public void setFilterCommand(Command command){
        this.filterCommand = command;
    }

    public void addAssignment(String roleName, String user){
        final CaseRoleItemView roleItemView = provider.get();
        roleItemView.setRoleName(roleName);
        roleItemView.setRoleUsers(user);
        allElementsList.add(roleItemView);
    }

    public String getFilterValue (){
        if(isNullOrEmpty(filterSelect.getValue())){
            return "All";
        }
        return filterSelect.getValue();
    }

    public void addAssignment(String roleName, List<CaseRolesPresenter.CaseAssignmentItem> users, List <CaseRolesPresenter.CaseAssignmentItem> groups,
                  CaseRolesPresenter.CaseRoleAction... actions){
        final CaseRoleItemView roleItemView = provider.get();
        roleItemView.setRoleName(roleName);
        if( users.size() == 0 && groups.size() == 0){
            roleItemView.displayUnassigned();
        } else {
            for (CaseRolesPresenter.CaseAssignmentItem user : users) {
                roleItemView.addUser(user);
            }
            for (CaseRolesPresenter.CaseAssignmentItem group : groups) {
                roleItemView.addGroup(group);
            }
        }

        for (CaseRolesPresenter.CaseRoleAction action : actions) {
            roleItemView.addAction(action);
        }
        allElementsList.add(roleItemView);
    }

    @Override
    public void setupPagination() {
        rolesBadge.setTextContent(String.valueOf(allElementsList.size()));
        pagination.init(allElementsList, this, PAGE_SIZE);
    }

    @Override
    public void setVisibleItems(List visibleItems) {
        removeAllChildren(roles);
        int visibleItemsSize = visibleItems.size();
        if(visibleItemsSize>0){
            visibleItems.forEach(e -> ((CaseRoleItemView)e).setLastElementStyle(false));
            ((CaseRoleItemView)visibleItems.get(visibleItemsSize-1)).setLastElementStyle(true);
        }
        visibleItems.forEach(e -> roles.appendChild(((CaseRoleItemView)e).getElement()) );
    }

    @Override
    public Div getScrollBox() {
        return scrollbox;
    }

    @EventHandler("filter-select")
    public void onRolesAssignmentFilterChange(@ForEvent("change") Event e) {
        filterCommand.execute();
    }

    @Override
    public HTMLElement getElement() {
        return rolesContainer;
    }
}