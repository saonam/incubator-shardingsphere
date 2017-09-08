/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api.fixture;

import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.keygen.fixture.IncrementKeyGenerator;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.none.NoneShardingStrategy;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ShardingRuleMockBuilder {
    
    private final List<TableRule> tableRules = new LinkedList<>();
    
    private final List<String> shardingColumns = new LinkedList<>();
    
    private final Multimap<String, String> generateKeyColumnsMap = LinkedHashMultimap.create();
    
    private final List<String> bindTables = new ArrayList<>();
    
    public ShardingRuleMockBuilder addTableRules(final TableRule tableRule) {
        tableRules.add(tableRule);
        return this;
    }
    
    public ShardingRuleMockBuilder addShardingColumns(final String shardingColumnName) {
        shardingColumns.add(shardingColumnName);
        return this;
    }
    
    public ShardingRuleMockBuilder addGenerateKeyColumn(final String tableName, final String columnName) {
        generateKeyColumnsMap.put(tableName, columnName);
        return this;
    }
    
    public ShardingRuleMockBuilder addBindingTable(final String bindingTableName) {
        bindTables.add(bindingTableName);
        return this;
    }
    
    public ShardingRule build() {
        final DataSourceRule dataSourceRule = new DataSourceRule(ImmutableMap.of("db0", Mockito.mock(DataSource.class), "db1", Mockito.mock(DataSource.class)));
        Collection<TableRule> tableRules = Lists.newArrayList(Iterators.transform(generateKeyColumnsMap.keySet().iterator(), new Function<String, TableRule>() {
            
            @Override
            public TableRule apply(final String input) {
                TableRule.TableRuleBuilder builder =  TableRule.builder(input).actualTables(Collections.singletonList(input)).dataSourceRule(dataSourceRule);
                for (String each : generateKeyColumnsMap.get(input)) {
                    builder.generateKeyColumn(each);
                }
                return builder.build();
            }
        }));
        tableRules.addAll(this.tableRules);
        if (tableRules.isEmpty()) {
            tableRules.add(new TableRule.TableRuleBuilder("mock").actualTables(Collections.singletonList("mock")).dataSourceRule(dataSourceRule).build());
        }
        List<TableRule> bindingTableRules = new ArrayList<>(bindTables.size());
        for (String each : bindTables) {
            bindingTableRules.add(new TableRule.TableRuleBuilder(each).actualTables(Collections.singletonList(each)).dataSourceRule(dataSourceRule).build());
        }
        for (TableRule each : tableRules) {
            bindingTableRules.add(each);
        }
        return new ShardingRule.ShardingRuleBuilder(dataSourceRule).keyGenerator(IncrementKeyGenerator.class)
                .tableRules(tableRules.toArray(new TableRule[tableRules.size()])).bindingTableRules(new BindingTableRule(bindingTableRules.toArray(new TableRule[bindingTableRules.size()])))
                .databaseShardingStrategy(new NoneShardingStrategy()).build();
    }
}
