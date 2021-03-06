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
package com.speedment.internal.core.code.model.java;

import com.speedment.code.StandardTranslatorKey;
import com.speedment.code.Translator;
import com.speedment.code.TranslatorManager;
import com.speedment.codegen.Meta;
import com.speedment.codegen.model.File;
import com.speedment.codegen.model.Interface;
import com.speedment.config.db.Project;
import com.speedment.config.db.Table;
import com.speedment.internal.core.code.TranslatorManagerImpl;
import com.speedment.util.JavaLanguageNamer;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author pemi
 */
public class TranslatorManagerTest extends SimpleModel {

    @Test
    public void testAccept() {
        final TranslatorManager instance = new TranslatorManagerImpl(speedment) {

            @Override
            public void writeToFile(Project project, Meta<File, String> meta, boolean overwriteExisting) {
                String name = meta.getModel().getName();
                //System.out.println("Processing " + name);
                // Do nothing on file...
            }

        };
        instance.accept(project);
    }

    @Test
    public void testPreview() {

        final Translator<Table, Interface> translator = speedment.getCodeGenerationComponent()
            .findTranslator(table, StandardTranslatorKey.GENERATED_ENTITY);

        final String code = translator.toCode();
        //System.out.println(code);

        final JavaLanguageNamer javaLanguageNamer = speedment.getCodeGenerationComponent().javaLanguageNamer();

        assertTrue(code.contains(javaLanguageNamer.javaVariableName(table.getName())));
        assertTrue(code.contains(javaLanguageNamer.javaTypeName(table.getName())));
        assertTrue(code.contains(javaLanguageNamer.javaVariableName(column.getName())));
        assertTrue(code.contains(javaLanguageNamer.javaTypeName(column.getName())));
    }
}
