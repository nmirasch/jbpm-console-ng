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

package org.jbpm.dashboard.dataset.integration;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dashbuilder.dataprovider.DataSetProvider;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetFactory;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.DataColumnDef;
import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.impl.DataColumnImpl;
import org.dashbuilder.dataset.impl.DataSetMetadataImpl;
import org.dashbuilder.dataset.sort.ColumnSort;
import org.dashbuilder.dataset.sort.DataSetSort;
import org.dashbuilder.dataset.sort.SortOrder;
import org.jbpm.console.ng.bd.integration.KieServerIntegration;
import org.jbpm.console.ng.ga.model.dataset.ConsoleDataSetLookup;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.definition.QueryParam;
import org.kie.server.client.QueryServicesClient;

@ApplicationScoped
public class KieServerDataSetProvider implements DataSetProvider {

    @Inject
    private KieServerIntegration kieServerIntegration;



    @Override
    public DataSetProviderType getType() {
        return DataSetProviderType.REMOTE;
    }

    @Override
    public DataSetMetadata getDataSetMetadata(DataSetDef def) throws Exception {
        return new DataSetMetadataImpl(def, def.getUUID(), -1, def.getColumns().size(), new ArrayList<>(), new ArrayList<>(), -1);
    }

    @Override
    public DataSet lookupDataSet(DataSetDef def, DataSetLookup lookup) throws Exception {
        if (!(lookup instanceof ConsoleDataSetLookup)) {
            throw new IllegalArgumentException("DataSetLookup is of incorrect type " + lookup.getClass().getName());
        }
        ConsoleDataSetLookup dataSetLookup = (ConsoleDataSetLookup) lookup;
        if (dataSetLookup.getServerTemplateId() == null || dataSetLookup.getServerTemplateId().isEmpty()) {
            DataSet result = buildDataSet(def, new ArrayList<>());
            return result;
        }

        QueryServicesClient queryClient = kieServerIntegration.getServerClient(dataSetLookup.getServerTemplateId()).getServicesClient(QueryServicesClient.class);

        QueryFilterSpec filterSpec = new QueryFilterSpec();
        // apply filtering
        DataSetFilter filter = dataSetLookup.getFirstFilterOp();
        if (filter != null) {
            QueryParam[] filterParams = new QueryParam[filter.getColumnFilterList().size()];
            int index = 0;
            for (ColumnFilter cFilter : filter.getColumnFilterList()) {
                if (cFilter instanceof CoreFunctionFilter) {

                    CoreFunctionFilter coreFunctionFilter = (CoreFunctionFilter) cFilter;

                    filterParams[index] = new QueryParam(coreFunctionFilter.getColumnId(), coreFunctionFilter.getType().toString(), coreFunctionFilter.getParameters());
                    index++;
                }
            }

            filterSpec.setParameters(filterParams);
        }
        // apply sorting
        DataSetSort sort = dataSetLookup.getFirstSortOp();
        if (sort != null) {
            SortOrder sortOrder = SortOrder.UNSPECIFIED;
            StringBuilder orderBy = new StringBuilder();
            for (ColumnSort cSort : sort.getColumnSortList()) {
                orderBy.append(cSort.getColumnId()).append(",");
                sortOrder = cSort.getOrder();
            }
            // remove last ,
            orderBy.deleteCharAt(orderBy.length()-1);

            filterSpec.setOrderBy(orderBy.toString());
            filterSpec.setAscending(sortOrder.equals(SortOrder.ASCENDING));
        }

        final List<List> instances = queryClient.query(dataSetLookup.getDataSetUUID(), QueryServicesClient.QUERY_MAP_RAW, filterSpec, dataSetLookup.getRowOffset()/dataSetLookup.getNumberOfRows(), dataSetLookup.getNumberOfRows(), List.class);

        DataSet result = buildDataSet(def, instances);

        return result;
    }

    @Override
    public boolean isDataSetOutdated(DataSetDef def) {
        return false;
    }

    protected DataSet buildDataSet(DataSetDef def, List<List> instances) throws Exception {
        DataSet dataSet = DataSetFactory.newEmptyDataSet();
        dataSet.setUUID(def.getUUID());
        dataSet.setDefinition(def);


        for (DataColumnDef column : def.getColumns()) {
            DataColumn numRows = new DataColumnImpl(column.getId(), column.getColumnType());
            dataSet.addColumn(numRows);
        }


        for(List<Object> row : instances ) {

            int columnIndex = 0;
            for (Object value : row) {
                DataColumn intervalBuilder = dataSet.getColumnByIndex(columnIndex);
                intervalBuilder.getValues().add(value);

                columnIndex++;
            }
        }
        // set size of the results to allow paging to be more then the actual size
        dataSet.setRowCountNonTrimmed(instances.size() == 0 ? 0 : instances.size() + 1);

        return dataSet;
    }

}
