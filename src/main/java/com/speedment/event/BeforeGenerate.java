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
package com.speedment.event;

import com.speedment.annotation.Api;
import com.speedment.code.TranslatorManager;
import com.speedment.codegen.Generator;
import com.speedment.config.db.Project;
import com.speedment.event.trait.GeneratorEvent;
import com.speedment.event.trait.ProjectEvent;
import static java.util.Objects.requireNonNull;

/**
 *
 * @author Emil Forslund
 * @since 2.3
 */
@Api(version="2.3")
public final class BeforeGenerate implements ProjectEvent, GeneratorEvent {
    
    private final Project project;
    private final Generator generator;
    private final TranslatorManager translatorManager;
    
    public BeforeGenerate(Project project, Generator generator, TranslatorManager translatorManager) {
        this.project           = requireNonNull(project);
        this.generator         = requireNonNull(generator);
        this.translatorManager = translatorManager;
    }

    @Override
    public Project project() {
        return project;
    }

    @Override
    public Generator generator() {
        return generator;
    }

    @Override
    public TranslatorManager translatorManager() {
        return translatorManager;
    }
}