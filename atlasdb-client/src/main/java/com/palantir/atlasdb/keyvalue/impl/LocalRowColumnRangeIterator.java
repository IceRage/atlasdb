/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.keyvalue.impl;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.palantir.atlasdb.keyvalue.api.Cell;
import com.palantir.atlasdb.keyvalue.api.RowColumnRangeIterator;
import com.palantir.atlasdb.keyvalue.api.Value;

@JsonTypeName("RowColumnRangeIterator.class")
public class LocalRowColumnRangeIterator implements RowColumnRangeIterator {
    private final Iterator<Map.Entry<Cell, Value>> it;

    public LocalRowColumnRangeIterator(Iterator<Map.Entry<Cell, Value>> it) {
        this.it = it;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public Map.Entry<Cell, Value> next() {
        return it.next();
    }
}