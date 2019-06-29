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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.github.semlink.verbnet.VnThematicRole;
import io.github.semlink.verbnet.restrictions.VnRestrictions;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * XML binding implementation of {@link VnThematicRole}.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = VnThematicRoleXml.ROOT_NAME)
public class VnThematicRoleXml implements VnThematicRole {

    static final String ROOT_NAME = "THEMROLE";

    @XmlAttribute(name = "type", required = true)
    private String type;

    @XmlElement(name = SelectionalRestrictionsXml.ROOT_NAME, required = true)
    private SelectionalRestrictionsXml selectionalRestrictions;

    @Override
    public List<VnRestrictions<String>> restrictions() {
        return selectionalRestrictions.restrictions();
    }
}
