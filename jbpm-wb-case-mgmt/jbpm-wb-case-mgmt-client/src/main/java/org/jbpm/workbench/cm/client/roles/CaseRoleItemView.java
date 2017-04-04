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
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.TakesValue;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.UnorderedList;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.workbench.cm.client.events.CaseRoleAssignmentListLoadEvent;
import org.jbpm.workbench.cm.client.events.CaseRoleAssignmentListOpenEvent;
import org.jbpm.workbench.cm.client.roles.util.ItemsLine;
import org.jbpm.workbench.cm.client.util.AbstractView;
import org.jbpm.workbench.cm.client.util.ConfirmPopup;
import org.jbpm.workbench.cm.model.CaseRoleAssignmentSummary;

import static org.jboss.errai.common.client.dom.DOMUtil.addCSSClass;
import static org.jboss.errai.common.client.dom.DOMUtil.removeCSSClass;
import static org.jboss.errai.common.client.dom.Window.getDocument;
import static org.jbpm.workbench.cm.client.resources.i18n.Constants.*;

@Dependent
@Templated
public class CaseRoleItemView extends AbstractView<CaseRolesPresenter> implements TakesValue<CaseRoleAssignmentSummary>, IsElement {
    public static String USERS_LINE_ID="_users";
    public static String GROUP_LINE_ID = "_groups";

    @Inject
    @DataField("list-group-item")
    Div listGroupItem;

    @Inject
    @DataField("actions-dropdown")
    Div actions;

    @Inject
    @DataField("actions-button")
    Button actionsButton;

    @Inject
    @DataField("actions-items")
    UnorderedList actionsItems;

    @Inject
    @DataField("role-name")
    @Bound
    Span name;

    @Inject
    @DataField("line-users")
    ItemsLine usersItemsLine;

    @Inject
    @DataField("div-users")
    Div usersDiv;

    @Inject
    @DataField("line-groups")
    ItemsLine groupsItemsLine;

    @Inject
    @DataField("div-groups")
    Div groupsDiv;

    @Inject
    ConfirmPopup confirmPopup;

    @Inject
    protected TranslationService translationService;

    @Inject
    @AutoBound
    private DataBinder<CaseRoleAssignmentSummary> caseRoleAssignmentSummary;

    boolean listenLoadLineEvent=true;

    @Override
    public HTMLElement getElement() {
        return listGroupItem;
    }

    @Override
    public CaseRoleAssignmentSummary getValue() {
        return caseRoleAssignmentSummary.getModel();
    }

    @Override
    public void setValue(final CaseRoleAssignmentSummary model) {
        this.caseRoleAssignmentSummary.setModel(model);
        removeCSSClass(actions, "dropup");
        if (CaseRolesPresenter.CASE_OWNER_ROLE.equals(model.getName())) {
            setOwnerAssignment();
        } else {
            if(actionsItems.getChildNodes().getLength()==0) {
                boolean hasAssignment = model.getUsers().size() + model.getGroups().size() > 0;
                addAction(new CaseRolesPresenter.CaseRoleAction() {
                    @Override
                    public String label() {
                        if (hasAssignment) {
                            return translationService.format(EDIT);
                        }
                        return translationService.format(ASSIGN);
                    }

                    @Override
                    public boolean isEnabled() {
                        return true;
                    }

                    @Override
                    public void execute() {
                        presenter.editAction(model, model.getUsers(), model.getGroups());
                    }
                });
                addAction(new CaseRolesPresenter.CaseRoleAction() {
                    @Override
                    public String label() {
                        return translationService.format(REMOVE_ALL_ASSIGNMENTS);
                    }

                    @Override
                    public boolean isEnabled() {
                        return hasAssignment;
                    }

                    @Override
                    public void execute() {
                        confirmPopup.show(translationService.format(REMOVE_ASSIGNMENT),
                                translationService.format(REMOVE),
                                translationService.format(REMOVE_ALL_USERS_GROUPS_FROM_ROLE, model.getName()),
                                () -> presenter.storeRoleAssignments(model.getName(), Collections.emptyList(),
                                        Collections.emptyList(), model.getUsers(), model.getGroups()));
                    }
                });
            }
        }
    }

    public void setCaseRoleAssignments( int maxWidth) {
        if(!CaseRolesPresenter.CASE_OWNER_ROLE.equals(caseRoleAssignmentSummary.getModel().getName())) {
            if (caseRoleAssignmentSummary.getModel().getUsers().size() == 0) {
                displayUnassignedUser();
            } else {
                List<CaseRolesPresenter.CaseAssignmentItem> itemsList = new ArrayList<>();
                caseRoleAssignmentSummary.getModel().getUsers().forEach(user ->
                        itemsList.add(new CaseRolesPresenter.CaseAssignmentItem() {
                            @Override
                            public String label() {
                                return user;
                            }

                            @Override
                            public void execute() {
                                confirmPopup.show(translationService.format(REMOVE_ASSIGNMENT),
                                        translationService.format(REMOVE),
                                        translationService.format(REMOVE_USER_ASSIGNMENT_FROM_ROLE, user, caseRoleAssignmentSummary.getModel().getName()),
                                        () -> presenter.removeUserFromRole(user, caseRoleAssignmentSummary.getModel().getName()));
                            }
                        }));
                usersItemsLine.initWithItemsLine(maxWidth, name.getTextContent() + USERS_LINE_ID, itemsList);
            }
            if (caseRoleAssignmentSummary.getModel().getGroups().size() == 0) {
                displayUnassignedGroup();
            } else {
                List<CaseRolesPresenter.CaseAssignmentItem> itemsList = new ArrayList<>();
                caseRoleAssignmentSummary.getModel().getGroups().forEach(group ->
                        itemsList.add((new CaseRolesPresenter.CaseAssignmentItem() {
                            @Override
                            public String label() {
                                return group;
                            }

                            @Override
                            public void execute() {
                                confirmPopup.show(translationService.format(REMOVE_ASSIGNMENT),
                                        translationService.format(REMOVE),
                                        translationService.format(REMOVE_GROUP_ASSIGNMENT_FROM_ROLE, group, caseRoleAssignmentSummary.getModel().getName()),
                                        () -> presenter.removeGroupFromRole(group, caseRoleAssignmentSummary.getModel().getName()));

                            }
                        })));
                groupsItemsLine.initWithItemsLine(maxWidth, name.getTextContent() + GROUP_LINE_ID, itemsList);
            }
        }
    }

    public void setOwnerAssignment() {
        usersItemsLine.initWithSingleItem(name.getTextContent(), caseRoleAssignmentSummary.getModel().getUsers().get(0));

        addCSSClass(usersDiv, "kie-owner-role-height");
        addCSSClass(groupsDiv, "hidden");
        removeCSSClass(actions, "hidden");
        addCSSClass(actionsItems, "hidden");
        addCSSClass(actionsButton, "disabled");
    }

    public void displayUnassignedUser() {
        addCSSClass(usersDiv, "kie-unassigned-color");
        usersItemsLine.displayUnassignedItem();
    }

    public void displayUnassignedGroup() {
        addCSSClass(groupsDiv, "kie-unassigned-color");
        groupsItemsLine.displayUnassignedItem();
    }

    public void addAction(final CaseRolesPresenter.CaseRoleAction action) {
        removeCSSClass(actions, "hidden");

        final HTMLElement li = getDocument().createElement("li");
        final HTMLElement a = getDocument().createElement("a");
        a.setTextContent(action.label());
        if (!action.isEnabled()) {
            addCSSClass(li, "disabled");

       } else {
            addCSSClass(a, "kie-more-info-link");
            a.setOnclick(e -> action.execute());
            li.appendChild(a);
        }

        li.appendChild(a);
        actionsItems.appendChild(li);
    }


    public void setLastElementStyle() {
        addCSSClass(actions, "dropup");
    }

    public void onCaseRoleAssignmentLineOpenEvent(@Observes CaseRoleAssignmentListOpenEvent event) {
        String lineId = event.getAssignmentLineId();
        if(lineId.equals(name.getTextContent() + USERS_LINE_ID)){
            groupsItemsLine.hideAllItems();
        } else if(lineId.equals(name.getTextContent() + GROUP_LINE_ID)){
            usersItemsLine.hideAllItems();
        } else {
            usersItemsLine.hideAllItems();
            groupsItemsLine.hideAllItems();
        }
    }

    public void onCaseRoleAssignmentLineLoadEvent(@Observes CaseRoleAssignmentListLoadEvent event) {
        if(listenLoadLineEvent) {
            setCaseRoleAssignments(event.getMaxWidth());
        }
    }
    public void unLinkEvent(){
        listenLoadLineEvent = false;
    }

}
