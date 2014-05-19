/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cyclop.service.completion.impl.parser.insert;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSortedSet;
import org.cyclop.model.CqlColumnName;
import org.cyclop.model.CqlCompletion;
import org.cyclop.model.CqlKeyword;
import org.cyclop.model.CqlPart;
import org.cyclop.model.CqlQuery;
import org.cyclop.model.CqlTable;
import org.cyclop.service.cassandra.QueryService;
import org.cyclop.service.completion.impl.parser.MarkerBasedCompletion;

import javax.inject.Inject;
import javax.inject.Named;

import static org.cyclop.common.QueryHelper.extractTableName;

/** @author Maciej Miklas */
@Named("insert.ColumnsCompletion")
class ColumnsCompletion extends MarkerBasedCompletion {

	@Inject
	private QueryService queryService;

	public ColumnsCompletion() {
		super(new CqlPart("("));
	}

	@Override
	public CqlCompletion getCompletion(CqlQuery query) {

		CqlTable table = extractTableName(CqlKeyword.Def.INSERT_INTO.value, query);
		ImmutableSortedSet<CqlColumnName> columnNames = queryService.findColumnNames(table);

		CqlCompletion cmp = CqlCompletion.Builder.naturalOrder().all(columnNames).build();
		return cmp;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).toString();
	}

}