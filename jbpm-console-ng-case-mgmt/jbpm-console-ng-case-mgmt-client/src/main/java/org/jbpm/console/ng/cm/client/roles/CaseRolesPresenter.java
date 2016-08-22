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

package org.jbpm.console.ng.cm.client.roles;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jbpm.console.ng.cm.client.resources.i18n.Constants;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;

@Dependent
@WorkbenchScreen(identifier = CaseRolesPresenter.SCREEN_ID)
public class CaseRolesPresenter {

    public static final String SCREEN_ID = "Case Roles";

    @Inject
    private View view;

    @WorkbenchPartView
    public UberView<CaseRolesPresenter> getView() {
        return view;
    }

    @WorkbenchPartTitle
    public String getTittle() {
        return Constants.INSTANCE.Actions();
    }


    public interface View extends UberView<CaseRolesPresenter> {
    }

}