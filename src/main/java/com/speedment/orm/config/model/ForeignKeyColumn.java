/**
 *
 * Copyright (c) 2006-2015, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.orm.config.model;

import com.speedment.orm.config.model.aspects.Ordinable;
import com.speedment.orm.annotations.Api;
import com.speedment.orm.config.model.aspects.Child;
import java.util.Optional;

/**
 *
 * @author pemi
 */
@Api(version = 0)
public interface ForeignKeyColumn extends ConfigEntity, Ordinable, Child<ForeignKey> {

    @Override
    default Class<ForeignKeyColumn> getInterfaceMainClass() {
        return ForeignKeyColumn.class;
    }

    @Override
    default Class<ForeignKey> getParentInterfaceMainClass() {
        return ForeignKey.class;
    }

    default Column getColumn() {
        return ConfigEntityUtil.findColumnByName(this, ancestor(Table.class), getName());
    }

    @External
    String getForeignColumnName();

    @External
    void setForeignColumnName(String foreignColumnName);

    @External
    String getForeignTableName();

    @External
    void setForeignTableName(String foreignTableName);

    default Column getForeignColumn() {
        return ConfigEntityUtil.findColumnByName(this, Optional.of(getForeignTable()), getForeignColumnName());
    }

    default Table getForeignTable() {
        return ConfigEntityUtil.findTableByName(this, ancestor(Schema.class), getForeignTableName());
    }
}