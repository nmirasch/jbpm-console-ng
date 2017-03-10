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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.UnorderedList;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.jboss.errai.common.client.dom.DOMUtil.addCSSClass;
import static org.jboss.errai.common.client.dom.DOMUtil.removeCSSClass;
import static org.jboss.errai.common.client.dom.Window.getDocument;

@Dependent
@Templated(stylesheet = "CaseRoleItemView.css")
public class CaseRoleItemView implements IsElement {

    @Inject
    @DataField("list-group-item")
    Div listGroupItem;

    @Inject
    @DataField("actions-dropdown")
    Div actions;

    @Inject
    @DataField("actions-items")
    UnorderedList actionsItems;

    @Inject
    @DataField("role-name")
    Span roleName;

    @Inject
    @DataField("role-users")
    UnorderedList roleUsers;

    @Inject
    @DataField("div-users")
    Div usersDiv;

    @Inject
    @DataField("users-line")
    Div usersLineDiv;

    @Inject
    @DataField("more-users-link")
    Div moreUsersLinkDiv;

    @Inject
    @DataField("less-users-link")
    Div lessUsersLinkDiv;

    @Inject
    @DataField("role-groups")
    UnorderedList roleGroups;

    @Inject
    @DataField("div-groups")
    Div groupsDiv;

    @Inject
    @DataField("groups-line")
    Div groupsLineDiv;

    @Inject
    @DataField("more-groups-link")
    Div moreGroupsLinkDiv;

    @Inject
    @DataField("less-groups-link")
    Div lessGroupsLinkDiv;

    @Inject
    @DataField("div-unassigned")
    Div unassignedDiv;

    @Override
    public HTMLElement getElement() {
        return listGroupItem;
    }

    public void setRoleName(final String roleName) {
        this.roleName.setInnerHTML(roleName);
    }

    public void setRoleUsers(final String roleUsers) {
        if(!isNullOrEmpty(roleUsers)) {
            removeCSSClass(usersDiv, "hidden");
            final HTMLElement itemText = getDocument().createElement("span");
            itemText.setTextContent(roleUsers);

            final HTMLElement li = getDocument().createElement("li");
            li.appendChild(itemText);

            this.roleUsers.appendChild(li);
           ;
        } else {
            addCSSClass(usersDiv, "hidden");
        }
        addCSSClass(moreUsersLinkDiv,"hidden");
    }

    public void setRoleGroups(final String roleGroups) {
        if(!isNullOrEmpty(roleGroups) ){
            removeCSSClass(groupsDiv, "hidden");
            this.roleGroups.setInnerHTML(roleGroups);
        } else {
            addCSSClass(groupsDiv, "hidden");
        }
    }

    public void displayUnassigned() {
        addCSSClass(usersDiv, "hidden");
        addCSSClass(groupsDiv, "hidden");
        removeCSSClass(unassignedDiv, "hidden");
    }

    public void addAction(final CaseRolesPresenter.CaseRoleAction action) {
        removeCSSClass(actions, "hidden");

        final HTMLElement a = getDocument().createElement("a");
        a.setTextContent(action.label());
        a.setOnclick(e -> action.execute());

        final HTMLElement li = getDocument().createElement("li");
        li.appendChild(a);
        actionsItems.appendChild(li);
    }

    public void addUser(final CaseRolesPresenter.CaseAssignmentItem user) {
        removeCSSClass(usersDiv, "hidden");
        addItem(user,roleUsers);

    }

    public void addGroup(final CaseRolesPresenter.CaseAssignmentItem group) {
        removeCSSClass(groupsDiv, "hidden");
        addItem(group,roleGroups);
    }

    public void addItem(final CaseRolesPresenter.CaseAssignmentItem item, UnorderedList unorderedList) {

        final HTMLElement closeIcon = getDocument().createElement("span");
        addCSSClass(closeIcon, "pficon");
        addCSSClass(closeIcon, "pficon-close");
        addCSSClass(closeIcon, "kie-remove-role-item");

        final HTMLElement itemText = getDocument().createElement("span");
        itemText.setTextContent(item.label());

        final HTMLElement a = getDocument().createElement("a");
        a.appendChild(closeIcon);
        a.setOnclick(e -> item.execute());

        itemText.appendChild(a);
        final HTMLElement li = getDocument().createElement("li");
        li.appendChild(itemText);

        unorderedList.appendChild(li);
    }

    public void setLastElementStyle(boolean last){
        if(last) {
            addCSSClass(actions, "dropup");
        }else{
            removeCSSClass(actions,"dropup");
        }
    }

    @EventHandler("more-users")
    @SuppressWarnings("unsued")
    public void onMoreUsers(@ForEvent("click") final Event event) {
        removeCSSClass(usersLineDiv,"kie-roles-item-line");
        addCSSClass(usersLineDiv,"kie-roles-item-line-expanded");
        addCSSClass(moreUsersLinkDiv,"hidden");
        removeCSSClass(lessUsersLinkDiv,"hidden");
    }

    @EventHandler("less-users")
    @SuppressWarnings("unsued")
    public void onLessUsers(@ForEvent("click") final Event event) {
        addCSSClass(usersLineDiv,"kie-roles-item-line");
        removeCSSClass(usersLineDiv,"kie-roles-item-line-expanded");
        removeCSSClass(moreUsersLinkDiv,"hidden");
        addCSSClass(lessUsersLinkDiv,"hidden");
    }

    @EventHandler("more-groups")
    @SuppressWarnings("unsued")
    public void onMoreGroups(@ForEvent("click") final Event event) {
        removeCSSClass(groupsLineDiv,"kie-roles-item-line");
        addCSSClass(groupsLineDiv,"kie-roles-item-line-expanded");
        addCSSClass(moreGroupsLinkDiv,"hidden");
        removeCSSClass(lessGroupsLinkDiv,"hidden");
    }

    @EventHandler("less-groups")
    @SuppressWarnings("unsued")
    public void onLessGroups(@ForEvent("click") final Event event) {
        addCSSClass(groupsLineDiv,"kie-roles-item-line");
        removeCSSClass(groupsLineDiv,"kie-roles-item-line-expanded");
        removeCSSClass(moreGroupsLinkDiv,"hidden");
        addCSSClass(lessGroupsLinkDiv,"hidden");
    }


}
