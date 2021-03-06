/**
 *
 * Copyright (c) 2006-2016, Speedment, Inc. All Rights Reserved.
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
package com.speedment.internal.core.code.entity;

import com.speedment.codegen.model.Field;
import com.speedment.codegen.model.File;
import com.speedment.codegen.model.Generic;
import com.speedment.codegen.model.Import;
import com.speedment.codegen.model.Javadoc;
import com.speedment.codegen.model.Method;
import com.speedment.codegen.model.Type;
import com.speedment.config.db.Column;
import com.speedment.config.db.ForeignKey;
import com.speedment.config.db.ForeignKeyColumn;
import com.speedment.config.db.Project;
import com.speedment.config.db.Table;
import com.speedment.config.db.trait.HasEnabled;
import com.speedment.db.MetaResult;
import com.speedment.encoder.JsonEncoder;
import com.speedment.exception.SpeedmentException;
import com.speedment.field.ComparableField;
import com.speedment.field.ComparableForeignKeyField;
import com.speedment.field.ReferenceField;
import com.speedment.field.ReferenceForeignKeyField;
import com.speedment.field.StringField;
import com.speedment.field.StringForeignKeyField;
import static com.speedment.internal.codegen.model.constant.DefaultJavadocTag.PARAM;
import static com.speedment.internal.codegen.model.constant.DefaultJavadocTag.RETURN;
import static com.speedment.internal.codegen.model.constant.DefaultJavadocTag.SEE;
import static com.speedment.internal.codegen.model.constant.DefaultType.STRING;
import static com.speedment.internal.codegen.util.Formatting.DOT;
import com.speedment.internal.core.field.ComparableFieldImpl;
import com.speedment.internal.core.field.ComparableForeignKeyFieldImpl;
import com.speedment.internal.core.field.ReferenceFieldImpl;
import com.speedment.internal.core.field.ReferenceForeignKeyFieldImpl;
import com.speedment.internal.core.field.StringFieldImpl;
import com.speedment.internal.core.field.StringForeignKeyFieldImpl;
import com.speedment.internal.util.document.DocumentDbUtil;
import static com.speedment.internal.util.document.DocumentUtil.Name.JAVA_NAME;
import static com.speedment.internal.util.document.DocumentUtil.ancestor;
import com.speedment.util.JavaLanguageNamer;
import com.speedment.util.Pluralis;
import static com.speedment.util.StaticClassUtil.instanceNotAllowed;
import java.util.Optional;
import java.util.function.Consumer;
import static com.speedment.internal.util.document.DocumentUtil.relativeName;
import static com.speedment.util.NullUtil.requireNonNulls;
import static java.util.Objects.requireNonNull;

/**
 *
 * @author pemi
 */
public final class EntityTranslatorSupport {

    public static final String CONSUMER_NAME = "consumer";
    public static final String FIND = "find";

    public EntityTranslatorSupport() {
        instanceNotAllowed(getClass());
    }

    public static Type getEntityType(Table table, JavaLanguageNamer javaLanguageNamer) {
        requireNonNull(table);
        requireNonNull(javaLanguageNamer);
        final Project project = ancestor(table, Project.class).get();

        return Type.of(project.findPackageName(javaLanguageNamer) + DOT
            + relativeName(table, Project.class, JAVA_NAME, javaLanguageNamer::javaPackageName) + DOT
            + javaLanguageNamer.javaTypeName(table.getJavaName())
        );
    }

    public static class ReferenceFieldType {

        public final Type type, implType;

        public ReferenceFieldType(Type type, Type implType) {
            this.type = type;
            this.implType = implType;
        }
    }

    public static ReferenceFieldType getReferenceFieldType(
        File file,
        Table table,
        Column column,
        Type entityType,
        JavaLanguageNamer javaLanguageNamer
    ) {
        requireNonNulls(file, table, column, entityType, javaLanguageNamer);

        final Class<?> mapping = column.findTypeMapper().getJavaType();
        final Type databaseType = Type.of(column.findTypeMapper().getDatabaseType());

        return EntityTranslatorSupport.getForeignKey(table, column)
            // If this is a foreign key.
            .map(fkc -> {
                final Type type, implType;
                final Type fkType = getEntityType(
                    fkc.findForeignTable().orElseThrow(
                        () -> new SpeedmentException(
                            "Could not find referenced foreign table '"
                            + fkc.getForeignTableName() + "'."
                        )),
                    javaLanguageNamer
                );

                file.add(Import.of(fkType));

                if (String.class.equals(mapping)) {
                    type = Type.of(StringForeignKeyField.class)
                        .add(Generic.of().add(entityType))
                        .add(Generic.of().add(databaseType))
                        .add(Generic.of().add(fkType));

                    implType = Type.of(StringForeignKeyFieldImpl.class)
                        .add(Generic.of().add(entityType))
                        .add(Generic.of().add(databaseType))
                        .add(Generic.of().add(fkType));
                } else if (Comparable.class.isAssignableFrom(mapping)) {
                    type = Type.of(ComparableForeignKeyField.class)
                        .add(Generic.of().add(entityType))
                        .add(Generic.of().add(databaseType))
                        .add(Generic.of().add(Type.of(mapping)))
                        .add(Generic.of().add(fkType));

                    implType = Type.of(ComparableForeignKeyFieldImpl.class)
                        .add(Generic.of().add(entityType))
                        .add(Generic.of().add(databaseType))
                        .add(Generic.of().add(Type.of(mapping)))
                        .add(Generic.of().add(fkType));
                } else {
                    type = Type.of(ReferenceForeignKeyField.class)
                        .add(Generic.of().add(entityType))
                        .add(Generic.of().add(databaseType))
                        .add(Generic.of().add(Type.of(mapping)))
                        .add(Generic.of().add(fkType));

                    implType = Type.of(ReferenceForeignKeyFieldImpl.class)
                        .add(Generic.of().add(entityType))
                        .add(Generic.of().add(databaseType))
                        .add(Generic.of().add(Type.of(mapping)))
                        .add(Generic.of().add(fkType));
                }

                return new ReferenceFieldType(type, implType);

                // If it is not a foreign key
            }).orElseGet(() -> {
            final Type type, implType;

            if (String.class.equals(mapping)) {
                type = Type.of(StringField.class)
                    .add(Generic.of().add(entityType))
                    .add(Generic.of().add(databaseType));

                implType = Type.of(StringFieldImpl.class)
                    .add(Generic.of().add(entityType))
                    .add(Generic.of().add(databaseType));

            } else if (Comparable.class.isAssignableFrom(mapping)) {
                type = Type.of(ComparableField.class)
                    .add(Generic.of().add(entityType))
                    .add(Generic.of().add(databaseType))
                    .add(Generic.of().add(Type.of(mapping)));

                implType = Type.of(ComparableFieldImpl.class)
                    .add(Generic.of().add(entityType))
                    .add(Generic.of().add(databaseType))
                    .add(Generic.of().add(Type.of(mapping)));
            } else {
                type = Type.of(ReferenceField.class)
                    .add(Generic.of().add(entityType))
                    .add(Generic.of().add(databaseType))
                    .add(Generic.of().add(Type.of(mapping)));

                implType = Type.of(ReferenceFieldImpl.class)
                    .add(Generic.of().add(entityType))
                    .add(Generic.of().add(databaseType))
                    .add(Generic.of().add(Type.of(mapping)));
            }

            return new ReferenceFieldType(type, implType);
        });
    }

    public static String pluralis(Table table, JavaLanguageNamer javaLanguageNamer) {
        requireNonNull(table);
        return Pluralis.INSTANCE.pluralizeJavaIdentifier(javaLanguageNamer.javaTypeName(table.getJavaName()), javaLanguageNamer);
    }

    public static Method toJson() {
        return Method.of("toJson", STRING)
            .set(Javadoc.of(
                "Returns a JSON representation of this Entity using the default {@link " + JsonEncoder.class.getSimpleName() + "}. "
                + "All of the fields in this Entity will appear in the returned JSON String."
            )
                .add(RETURN.setText("Returns a JSON representation of this Entity using the default {@link " + JsonEncoder.class.getSimpleName() + "}"))
            );
    }

    public static Method toJsonExtended(Type entityType) {
        requireNonNull(entityType);
        final String paramName = "jsonEncoder";
        return Method.of("toJson", STRING)
            .add(Field.of(paramName, Type.of(JsonEncoder.class)
                .add(Generic.of().add(entityType))))
            .set(Javadoc.of(
                "Returns a JSON representation of this Entity using the provided {@link "
                + JsonEncoder.class.getSimpleName() + "}."
            )
                .add(PARAM.setValue(paramName).setText("to use as encoder"))
                .add(RETURN.setText("Returns a JSON representation of this Entity using the provided {@link " + JsonEncoder.class.getSimpleName() + "}"))
                .add(SEE.setText(JsonEncoder.class.getSimpleName()))
            );

    }

    public static Optional<ForeignKeyColumn> getForeignKey(Table table, Column column) {
        requireNonNull(table);
        requireNonNull(column);
        return table.foreignKeys()
            .filter(HasEnabled::test)
            .flatMap(ForeignKey::foreignKeyColumns)
            .filter(fkc -> DocumentDbUtil.isSame(column, fkc.findColumn().orElse(null)))
            .findFirst();
    }

    public static Method dbMethod(String name, Type entityType) {
        requireNonNull(name);
        requireNonNull(entityType);
        return Method.of(name, entityType).add(Type.of(SpeedmentException.class));
    }

    public static Method dbMethodWithListener(String name, Type entityType) {
        requireNonNull(name);
        requireNonNull(entityType);
        return Method.of(name, entityType).add(Type.of(SpeedmentException.class))
            .add(Field.of(CONSUMER_NAME, Type.of(Consumer.class)
                .add(Generic.of().add(Type.of(MetaResult.class).add(Generic.of().add(entityType))))
            ));
    }

    public static Method persist(Type entityType) {
        return EntityTranslatorSupport.dbMethod("persist", requireNonNull(entityType));
    }

    public static Method update(Type entityType) {
        return EntityTranslatorSupport.dbMethod("update", requireNonNull(entityType));
    }

    public static Method remove(Type entityType) {
        return EntityTranslatorSupport.dbMethod("remove", requireNonNull(entityType));
    }

    public static Method persistWithListener(Type entityType) {
        return EntityTranslatorSupport.dbMethodWithListener("persist", requireNonNull(entityType));
    }

    public static Method updateWithListener(Type entityType) {
        return EntityTranslatorSupport.dbMethodWithListener("update", requireNonNull(entityType));
    }

    public static Method removeWithListener(Type entityType) {
        return EntityTranslatorSupport.dbMethodWithListener("remove", requireNonNull(entityType));
    }
}
