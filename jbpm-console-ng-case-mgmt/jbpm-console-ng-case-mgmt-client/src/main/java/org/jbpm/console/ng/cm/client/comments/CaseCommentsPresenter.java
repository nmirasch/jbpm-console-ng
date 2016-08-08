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

package org.jbpm.console.ng.cm.client.comments;

import java.util.Date;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.security.shared.api.identity.User;
import org.jbpm.console.ng.cm.client.resources.i18n.Constants;
import org.jbpm.console.ng.cm.service.CaseInstanceService;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

@Dependent
@WorkbenchScreen(identifier = CaseCommentsPresenter.SCREEN_ID)
public class CaseCommentsPresenter {

    public static final String SCREEN_ID = "Case Comments";

    @Inject
    private View view;

    @Inject
    private Caller<CaseInstanceService> caseService;

    @Inject
    private User user;

    private String currentCaseId = "";

    @WorkbenchPartView
    public UberView<CaseCommentsPresenter> getView() {
        return view;
    }

    @WorkbenchPartTitle
    public String getTittle() {
        return Constants.INSTANCE.Comments();
    }

    @OnStartup
    public void onStartup(final PlaceRequest place) {
        currentCaseId = place.getParameter("caseId", null);
        refreshCase();
    }

    protected void refreshCase() {
        view.removeAllComments();
        if (currentCaseId == null) {
            return;
        }
        caseService.call((List<String> comments) -> {
            for (String comment : comments) {
                view.addComment(comment, user.getIdentifier(), null);
            }
        }, new DefaultErrorCallback()).getComments(null, null, currentCaseId);
    }

    public void addComment(final String comment) {
        caseService.call(
                (Void nothing) -> view.addComment(comment, user.getIdentifier(), null),
                new DefaultErrorCallback())
                .addComment(null, null, currentCaseId, comment, user.getIdentifier());
    }

    public interface View extends UberView<CaseCommentsPresenter> {

        void removeAllComments();

        void addComment(String comment, String user, Date time);

    }

}