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
 * XML bindings for Verbnet syntactic restrictions.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SyntacticRestrictionsXml.ROOT_NAME)
public class SyntacticRestrictionsXml {

    static final String ROOT_NAME = "SYNRESTRS";

    @XmlAttribute(name = "logic")
    @XmlJavaTypeAdapter(LogicAdapterXmlAdapter.class)
    private LogicalRelation logic;

    @XmlElement(name = SyntacticRestrictionXml.ROOT_NAME)
    private List<SyntacticRestrictionXml> syntacticRestrictions = new ArrayList<>();

    public List<VnRestrictions<String>> restrictions() {
        List<VnRestrictions<String>> result = new ArrayList<>();
        if (logic == LogicalRelation.OR) {
            for (SyntacticRestrictionXml xml : syntacticRestrictions) {
                VnRestrictions<String> single = xml.include()
                    ? DefaultVnRestrictions.including(xml.type())
                    : DefaultVnRestrictions.excluding(xml.type());
                result.add(single);
            }
        } else {
            VnRestrictions<String> rest = new DefaultVnRestrictions<>();
            for (SyntacticRestrictionXml xml : syntacticRestrictions) {
                (xml.include() ? rest.include() : rest.exclude()).add(xml.type());
            }
            result.add(rest);
        }
        return result;
    }
}
