/*
 * Copyright 2019 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.semlink.verbnet;

import java.util.List;

import io.github.semlink.verbnet.restrictions.VnRestrictions;

/**
 * VerbNet thematic role, which defines the semantic relationship between a predicate and an argument.
 *
 * @author jgung
 */
public interface VnThematicRole {

    /**
     * Type of thematic role, e.g. "Agent" or "Patient".
     */
    String type();

    /**
     * Selectional restrictions further refining the nature of this thematic role.
     */
    List<VnRestrictions<String>> restrictions();

}
