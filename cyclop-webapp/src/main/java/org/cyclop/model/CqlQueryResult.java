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
package org.cyclop.model;

import com.datastax.driver.core.Row;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.cyclop.common.SerializationUtil;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Iterator;

/** @author Maciej Miklas */
@Immutable
public final class CqlQueryResult implements Serializable, Iterable<Row> {

	public final static CqlQueryResult EMPTY = new CqlQueryResult();

	/** could be null, it not found in result, or in case of error while reading meta data info */
	public final transient CqlPartitionKey partitionKey;

	/** all columns returned by db-query, in exact the same order */
	@NotNull
	@Valid
	public final ImmutableList<CqlExtendedColumnName> columns;

	/** List of columns that value is not empty for multiple rows */
	@NotNull
	@Valid
	private final ImmutableList<CqlExtendedColumnName> commonColumns;

	/** List of columns that value is not empty only for single row, or it's empty */
	@NotNull
	@Valid
	private final ImmutableList<CqlExtendedColumnName> dynamicColumns;

	@NotNull
	@Valid
	private final transient ImmutableList<Row> rows;

	private CqlQueryResult() {
		this.commonColumns = ImmutableList.of();
		this.dynamicColumns = ImmutableList.of();
		this.columns = ImmutableList.of();
		this.rows = ImmutableList.of();
		this.partitionKey = null;
	}

	public CqlQueryResult(ImmutableList<CqlExtendedColumnName> commonColumns,
						  ImmutableList<CqlExtendedColumnName> dynamicColumns,
						  ImmutableList<CqlExtendedColumnName> columns, ImmutableList<Row> rows,
						  CqlPartitionKey partitionKey) {
		this.commonColumns = commonColumns;
		this.dynamicColumns = dynamicColumns;
		this.columns = columns;
		this.rows = rows;
		this.partitionKey = partitionKey;
	}

	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		SerializationUtil.setFiled(this, "rows", ImmutableList.of());
	}

	public boolean isEmpty() {
		return rows == null || rows.isEmpty();
	}

	@Override
	public RowIterator iterator() {
		return new RowIterator(rows, new CqlRowMetadata(commonColumns, dynamicColumns, columns, partitionKey));
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("commonColumns", commonColumns).add("dynamicColumns", dynamicColumns)
				.add("rows", rows).add("partitionKey", partitionKey).toString();
	}

	/**
	 * CALL CLOSE ON ITERATOR BECAUSE IT HOLDS READ-LOCK <br> Iterates over history entries from newest to oldest entry
	 * (reversed fifo col)
	 */
	@XmlTransient
	@NotThreadSafe
	public final static class RowIterator implements Iterator<Row> {

		public final static RowIterator EMPTY = new RowIterator();

		@NotNull
		@Valid
		public final CqlRowMetadata rowMetadata;

		private final Iterator<Row> rowsIt;

		RowIterator() {
			rowsIt = EmptyIterator.INSTANCE;
			rowMetadata = CqlRowMetadata.EMPTY;
		}

		RowIterator(ImmutableList<Row> rows, CqlRowMetadata rowMetadata) {
			rowsIt = rows.iterator();
			this.rowMetadata = rowMetadata;
		}

		@Override
		public boolean hasNext() {
			return rowsIt.hasNext();
		}

		@Override
		public Row next() {
			return rowsIt.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove is not supported on RowIterator");
		}
	}

}
