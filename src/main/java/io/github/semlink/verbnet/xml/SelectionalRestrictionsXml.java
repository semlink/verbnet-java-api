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

package io.github.semlink.verbnet.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.github.semlink.verbnet.restrictions.DefaultVnRestrictions;
import io.github.semlink.verbnet.restrictions.VnRestrictions;
import io.github.semlink.verbnet.xml.util.LogicAdapterXmlAdapter;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * XML bindings for VerbNet selectional restrictions.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SelectionalRestrictionsXml.ROOT_NAME)
public class SelectionalRestrictionsXml {

    static final String ROOT_NAME = "SELRESTRS";

    @XmlAttribute(name = "logic")
    @XmlJavaTypeAdapter(LogicAdapterXmlAdapter.class)
    private LogicalRelation logic;

    @XmlElement(name = SelectionalRestrictionXml.ROOT_NAME)
    private List<SelectionalRestrictionXml> resAtomic = new ArrayList<>();

    @XmlElement(name = ROOT_NAME)
    private List<SelectionalRestrictionsXml> resHierarchies = new ArrayList<>();

    public List<VnRestrictions<String>> restrictions() {
        return restrictions(this);
    }

    private static List<VnRestrictions<String>> restrictions(SelectionalRestrictionsXml restrictions) {
        List<VnRestrictions<String>> result = new ArrayList<>();
        if (restrictions.logic == LogicalRelation.OR) {
            for (SelectionalRestrictionXml xml : restrictions.resAtomic) {
                result.add(xml.include()
                    ? DefaultVnRestrictions.including(xml.type())
                    : DefaultVnRestrictions.excluding(xml.type()));
            }
            // recursively add additional restrictions for each restrictions hierarchy
            for (SelectionalRestrictionsXml res : restrictions.resHierarchies) {
                result.addAll(restrictions(res));
            }
        } else {
            // AND relation -- combine restrictions
            DefaultVnRestrictions<String> rest = new DefaultVnRestrictions<>();
            for (SelectionalRestrictionXml xml : restrictions.resAtomic) {
                (xml.include() ? rest.include() : rest.exclude()).add(xml.type());
            }
            // combine with hierarchical restrictions
            List<DefaultVnRestrictions<String>> paths = new ArrayList<>();
            paths.add(rest);
            for (SelectionalRestrictionsXml res : restrictions.resHierarchies) {
                // recursively find all restrictions for a given hierarchy
                List<VnRestrictions<String>> andRes = restrictions(res);

                List<DefaultVnRestrictions<String>> newPaths = new ArrayList<>();
                for (VnRestrictions<String> path : paths) {
                    for (VnRestrictions<String> andRe : andRes) {
                        DefaultVnRestrictions<String> combined = DefaultVnRestrictions
                            .includingExcluding(path.include(), path.exclude());
                        combined.include().addAll(andRe.include());
                        combined.exclude().addAll(andRe.exclude());
                        newPaths.add(combined);
                    }
                }
                paths = newPaths;
            }
            result = paths.stream().map(r -> (VnRestrictions<String>) r).collect(Collectors.toList());
        }

        return result;
    }
}
