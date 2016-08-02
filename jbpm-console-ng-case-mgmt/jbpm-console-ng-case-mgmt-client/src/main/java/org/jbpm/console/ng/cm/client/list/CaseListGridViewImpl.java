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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.NoSelectionModel;
import org.jbpm.console.ng.cm.client.resources.i18n.Constants;
import org.jbpm.console.ng.cm.model.CaseStatus;
import org.jbpm.console.ng.cm.model.CaseSummary;
import org.jbpm.console.ng.cm.model.events.CaseCreatedEvent;
import org.jbpm.console.ng.gc.client.experimental.grid.base.ExtendedPagedTable;
import org.jbpm.console.ng.gc.client.list.base.AbstractListView;
import org.jbpm.console.ng.gc.client.util.ButtonActionCell;
import org.uberfire.ext.services.shared.preferences.GridGlobalPreferences;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

@Dependent
public class CaseListGridViewImpl extends AbstractListView<CaseSummary, CaseListGridPresenter>
        implements CaseListGridPresenter.CaseListView {

    public static final String COL_ID_CASE_ID = "caseId";
    public static final String COL_ID_DESCRIPTION = "description";
    public static final String COL_ID_STATUS = "status";
    public static final String COL_ID_ACTIONS = "Actions";

    private final Constants constants = Constants.INSTANCE;

//    @Inject
//    private Event<CaseSelectedEvent> caseSelected;

//    @Inject
//    private QuickNewCasePopup newCasePopup;

//    @Inject
//    private QuickNewTaskPopup quickNewTaskPopup;

//    @Inject
//    private QuickNewProcessInstancePopup quickNewProcessInstancePopup;

    @Override
    public void init(final CaseListGridPresenter presenter) {
        List<String> bannedColumns = new ArrayList<String>();
        bannedColumns.add(COL_ID_CASE_ID);
        bannedColumns.add(COL_ID_ACTIONS);
        List<String> initColumns = new ArrayList<String>();
        initColumns.add(COL_ID_CASE_ID);
        initColumns.add(COL_ID_DESCRIPTION);
        initColumns.add(COL_ID_STATUS);
        initColumns.add(COL_ID_ACTIONS);
        super.init(presenter, new GridGlobalPreferences("CaseListGrid", initColumns, bannedColumns));

        selectionModel = new NoSelectionModel<CaseSummary>();
        selectionModel.addSelectionChangeHandler(e -> {
            if (selectedRow == -1) {
                selectedRow = listGrid.getKeyboardSelectedRow();
                listGrid.setRowStyles(selectedStyles);
                listGrid.redraw();
            } else if (listGrid.getKeyboardSelectedRow() != selectedRow) {
                listGrid.setRowStyles(selectedStyles);
                selectedRow = listGrid.getKeyboardSelectedRow();
                listGrid.redraw();
            }

            selectedItem = selectionModel.getLastSelectedObject();

            presenter.caseInstanceSelected(selectedItem.getCaseId());
        });

        noActionColumnManager = DefaultSelectionEventManager
                .createCustomManager(new DefaultSelectionEventManager.EventTranslator<CaseSummary>() {

                    @Override
                    public boolean clearCurrentSelection(CellPreviewEvent<CaseSummary> event) {
                        return false;
                    }

                    @Override
                    public DefaultSelectionEventManager.SelectAction translateSelectionEvent(CellPreviewEvent<CaseSummary> event) {
                        NativeEvent nativeEvent = event.getNativeEvent();
                        if (BrowserEvents.CLICK.equals(nativeEvent.getType()) &&
                                // Ignore if the event didn't occur in the correct column.
                                listGrid.getColumnIndex(actionsColumn) == event.getColumn()) {
                            return DefaultSelectionEventManager.SelectAction.IGNORE;
                        }
                        return DefaultSelectionEventManager.SelectAction.DEFAULT;
                    }
                });
        listGrid.setSelectionModel(selectionModel, noActionColumnManager);
        listGrid.setEmptyTableCaption(constants.No_Cases_Found());
        listGrid.setRowStyles(selectedStyles);

        listGrid.getElement().getStyle().setPaddingRight(20, Style.Unit.PX);
        listGrid.getElement().getStyle().setPaddingLeft(20, Style.Unit.PX);
    }

    @Override
    public void initColumns(final ExtendedPagedTable<CaseSummary> table) {
        initCellPreview();
        Column idColumn = initIdColumn();
        Column descriptionColumn = initDescriptionColumn();
        Column statusColumn = initStatusColumn();
        actionsColumn = initActionsColumn();

        List<ColumnMeta<CaseSummary>> columnMetas = new ArrayList<ColumnMeta<CaseSummary>>();
        columnMetas.add(new ColumnMeta<CaseSummary>(idColumn, constants.Id()));
        columnMetas.add(new ColumnMeta<CaseSummary>(descriptionColumn, constants.Description()));
        columnMetas.add(new ColumnMeta<CaseSummary>(statusColumn, constants.Status()));
        columnMetas.add(new ColumnMeta<CaseSummary>(actionsColumn, constants.Actions()));

        table.addColumns(columnMetas);
    }

    private void initCellPreview() {
        listGrid.addCellPreviewHandler(new CellPreviewEvent.Handler<CaseSummary>() {
            @Override
            public void onCellPreview(final CellPreviewEvent<CaseSummary> event) {
                if (BrowserEvents.MOUSEOVER.equalsIgnoreCase(event.getNativeEvent().getType())) {
                    onMouseOverGrid(event);
                }
            }
        });
    }

    private void onMouseOverGrid(final CellPreviewEvent<CaseSummary> event) {
        CaseSummary caseInstance = event.getValue();
        if (caseInstance.getDescription() != null) {
            listGrid.setTooltip(listGrid.getKeyboardSelectedRow(), event.getColumn(), caseInstance.getDescription());
        }
    }

    private Column initIdColumn() {
        Column<CaseSummary, String> caseIdColumn = new Column<CaseSummary, String>(new TextCell()) {
            @Override
            public String getValue(CaseSummary object) {
                return object.getCaseId();
            }
        };
        caseIdColumn.setSortable(true);
        caseIdColumn.setDataStoreName(COL_ID_CASE_ID);
        return caseIdColumn;
    }

    private Column initDescriptionColumn() {
        Column<CaseSummary, String> descriptionColumn = new Column<CaseSummary, String>(new TextCell()) {
            @Override
            public String getValue(CaseSummary object) {
                return object.getDescription();
            }
        };
        descriptionColumn.setSortable(true);
        descriptionColumn.setDataStoreName(COL_ID_DESCRIPTION);
        return descriptionColumn;
    }

    private Column initStatusColumn() {
        Column<CaseSummary, String> statusColumn = new Column<CaseSummary, String>(new TextCell()) {
            @Override
            public String getValue(CaseSummary object) {
                return object.getStatus().name();
            }
        };
        statusColumn.setSortable(true);
        statusColumn.setDataStoreName(COL_ID_STATUS);
        return statusColumn;
    }

//    public void onCaseRefreshedEvent(@Observes TaskRefreshedEvent event) {
//        presenter.refreshGrid();
//    }

    private Column initActionsColumn() {
        List<HasCell<CaseSummary, ?>> cells = new LinkedList<HasCell<CaseSummary, ?>>();

//        cells.add(new CreateTaskActionHasCell(constants.Create_Task(), new ActionCell.Delegate<CaseSummary>() {
//            @Override
//            public void execute(CaseSummary caseDefinition) {
//                quickNewTaskPopup.show(caseDefinition.getCaseId());
//            }
//        }));

//        cells.add(new CreateProcessActionHasCell(constants.Create_Process(), new ActionCell.Delegate<CaseSummary>() {
//            @Override
//            public void execute(CaseSummary caseDefinition) {
//                quickNewProcessInstancePopup.show(caseDefinition.getCaseId());
//            }
//        }));

//        cells.add(new CreateSubCaseActionHasCell(constants.Create_SubCase(), new ActionCell.Delegate<CaseSummary>() {
//            @Override
//            public void execute(CaseSummary caseDefinition) {
//                newCasePopup.show();
//            }
//        }));

        cells.add(new ActivateActionHasCell(constants.Activate(), (CaseSummary caseSummary) ->
                presenter.activateCaseInstance(caseSummary.getCaseId())
        ));

        cells.add(new CloseActionHasCell(constants.Close(), (CaseSummary caseSummary) ->
                presenter.closeCaseInstance(caseSummary.getCaseId())
        ));

        cells.add(new TerminateActionHasCell(constants.Terminate(), (CaseSummary caseSummary) ->
                presenter.terminateCaseInstance(caseSummary.getCaseId())
        ));

        cells.add(new CompleteActionHasCell(constants.Complete(), (CaseSummary caseSummary) ->
                presenter.completeCaseInstance(caseSummary.getCaseId())
        ));

        CompositeCell<CaseSummary> cell = new CompositeCell<CaseSummary>(cells);
        Column<CaseSummary, CaseSummary> actionsColumn = new Column<CaseSummary, CaseSummary>(cell) {
            @Override
            public CaseSummary getValue(CaseSummary object) {
                return object;
            }
        };

        actionsColumn.setDataStoreName(COL_ID_ACTIONS);
        return actionsColumn;
    }

    public void onCaseCreatedEvent(@Observes CaseCreatedEvent newCase) {
        presenter.refreshGrid();
//        final PlaceStatus status = placeManager.getStatus(new DefaultPlaceRequest(CaseDetailsPresenter.SCREEN_ID));
//        if (status != PlaceStatus.OPEN) {
//            placeManager.goTo(CaseDetailsPresenter.SCREEN_ID);
//        }
//        caseSelected.fire(new CaseSelectedEvent(newCase.getCaseId()));
//        selectionModel.setSelected(new CaseSummary(newCase.getCaseId()), true);
    }

    interface Binder extends UiBinder<Widget, CaseListGridViewImpl> {
    }

    protected class CompleteActionHasCell extends ButtonActionCell<CaseSummary> {

        public CompleteActionHasCell(final String text, final ActionCell.Delegate<CaseSummary> delegate) {
            super(text, delegate);
        }

        @Override
        public void render(final Cell.Context context, final CaseSummary value, final SafeHtmlBuilder sb) {
            if (CaseStatus.ACTIVE == value.getStatus()) {
                super.render(context, value, sb);
            }
        }
    }

    protected class CloseActionHasCell extends ButtonActionCell<CaseSummary> {

        public CloseActionHasCell(final String text, final ActionCell.Delegate<CaseSummary> delegate) {
            super(text, delegate);
        }

        @Override
        public void render(final Cell.Context context, final CaseSummary value, final SafeHtmlBuilder sb) {
            if (CaseStatus.COMPLETED == value.getStatus() || CaseStatus.TERMINATED == value.getStatus()) {
                super.render(context, value, sb);
            }
        }
    }

    protected class TerminateActionHasCell extends ButtonActionCell<CaseSummary> {

        public TerminateActionHasCell(final String text, final ActionCell.Delegate<CaseSummary> delegate) {
            super(text, delegate);
        }

        @Override
        public void render(final Cell.Context context, final CaseSummary value, final SafeHtmlBuilder sb) {
            if (CaseStatus.ACTIVE == value.getStatus()) {
                super.render(context, value, sb);
            }
        }
    }

//    private PlaceStatus getPlaceStatus(String place) {
//        DefaultPlaceRequest defaultPlaceRequest = new DefaultPlaceRequest(place);
//        PlaceStatus status = placeManager.getStatus(defaultPlaceRequest);
//        return status;
//    }

//    private void closePlace(String place) {
//        if (getPlaceStatus(place) == PlaceStatus.OPEN) {
//            placeManager.closePlace(place);
//        }
//    }

    protected class ActivateActionHasCell extends ButtonActionCell<CaseSummary> {

        public ActivateActionHasCell(final String text, final ActionCell.Delegate<CaseSummary> delegate) {
            super(text, delegate);
        }

        @Override
        public void render(final Cell.Context context, final CaseSummary value, final SafeHtmlBuilder sb) {
            if (CaseStatus.COMPLETED == value.getStatus() || CaseStatus.TERMINATED == value.getStatus()) {
                super.render(context, value, sb);
            }
        }
    }

//    protected class CreateProcessActionHasCell implements HasCell<CaseSummary, CaseSummary> {
//
//        private ActionCell<CaseSummary> cell;
//
//        public CreateProcessActionHasCell(String text, ActionCell.Delegate<CaseSummary> delegate) {
//            cell = new ActionCell<CaseSummary>(text, delegate) {
//                @Override
//                public void render(Cell.Context context, CaseSummary value, SafeHtmlBuilder sb) {
//                    AbstractImagePrototype imageProto = AbstractImagePrototype.create(images.createCaseGridIcon());
//                    SafeHtmlBuilder mysb = new SafeHtmlBuilder();
//                    mysb.appendHtmlConstant("<span title='" + constants.Create_Process() + "' style='margin-right:5px;'>");
//                    mysb.append(imageProto.getSafeHtml());
//                    mysb.appendHtmlConstant("</span>");
//                    sb.append(mysb.toSafeHtml());
//                }
//            };
//        }
//
//        @Override
//        public Cell<CaseSummary> getCell() {
//            return cell;
//        }
//
//        @Override
//        public FieldUpdater<CaseSummary, CaseSummary> getFieldUpdater() {
//            return null;
//        }
//
//        @Override
//        public CaseSummary getValue(CaseSummary object) {
//            return object;
//        }
//    }

//    protected class CreateTaskActionHasCell implements HasCell<CaseSummary, CaseSummary> {
//
//        private ActionCell<CaseSummary> cell;
//
//        public CreateTaskActionHasCell(String text, ActionCell.Delegate<CaseSummary> delegate) {
//            cell = new ActionCell<CaseSummary>(text, delegate) {
//                @Override
//                public void render(Cell.Context context, CaseSummary value, SafeHtmlBuilder sb) {
//
//                    AbstractImagePrototype imageProto = AbstractImagePrototype.create(images.createCaseGridIcon());
//                    SafeHtmlBuilder mysb = new SafeHtmlBuilder();
//                    mysb.appendHtmlConstant("<span title='" + constants.Create_Task() + "' style='margin-right:5px;'>");
//                    mysb.append(imageProto.getSafeHtml());
//                    mysb.appendHtmlConstant("</span>");
//                    sb.append(mysb.toSafeHtml());
//
//                }
//            };
//        }
//
//        @Override
//        public Cell<CaseSummary> getCell() {
//            return cell;
//        }
//
//        @Override
//        public FieldUpdater<CaseSummary, CaseSummary> getFieldUpdater() {
//            return null;
//        }
//
//        @Override
//        public CaseSummary getValue(CaseSummary object) {
//            return object;
//        }
//
//    }

//    protected class CreateSubCaseActionHasCell implements HasCell<CaseSummary, CaseSummary> {
//
//        private ActionCell<CaseSummary> cell;
//
//        public CreateSubCaseActionHasCell(String text, ActionCell.Delegate<CaseSummary> delegate) {
//            cell = new ActionCell<CaseSummary>(text, delegate) {
//                @Override
//                public void render(Cell.Context context, CaseSummary value, SafeHtmlBuilder sb) {
//                    AbstractImagePrototype imageProto = AbstractImagePrototype.create(images.createCaseGridIcon());
//                    SafeHtmlBuilder mysb = new SafeHtmlBuilder();
//                    mysb.appendHtmlConstant("<span title='" + constants.Create_SubCase() + "' style='margin-right:5px;'>");
//                    mysb.append(imageProto.getSafeHtml());
//                    mysb.appendHtmlConstant("</span>");
//                    sb.append(mysb.toSafeHtml());
//
//                }
//            };
//        }
//
//        @Override
//        public Cell<CaseSummary> getCell() {
//            return cell;
//        }
//
//        @Override
//        public FieldUpdater<CaseSummary, CaseSummary> getFieldUpdater() {
//            return null;
//        }
//
//        @Override
//        public CaseSummary getValue(CaseSummary object) {
//            return object;
//        }
//    }

}