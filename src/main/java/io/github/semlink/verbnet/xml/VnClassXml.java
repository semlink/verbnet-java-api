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
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.github.semlink.verbnet.VnClass;
import io.github.semlink.verbnet.VnClassId;
import io.github.semlink.verbnet.VnFrame;
import io.github.semlink.verbnet.VnMember;
import io.github.semlink.verbnet.VnThematicRole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * XML binding implementation of {@link VnClass}.
 *
 * @author jgung
 */
@Data
@ToString(of = "verbNetId")
@EqualsAndHashCode(of = "verbNetId")
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = VnClassXml.ROOT_NAME)
public class VnClassXml implements VnClass {

    static final String ROOT_NAME = "VNCLASS";

    @XmlAttribute(name = "ID", required = true)
    @XmlJavaTypeAdapter(VerbNetIdXmlAdapter.class)
    private VnClassId verbNetId;

    @XmlElementWrapper(name = "MEMBERS")
    @XmlElement(name = VnMemberXml.ROOT_NAME, required = true)
    private List<VnMemberXml> memberElements = new ArrayList<>();

    @XmlElementWrapper(name = "THEMROLES")
    @XmlElement(name = VnThematicRoleXml.ROOT_NAME, required = true)
    private List<VnThematicRoleXml> thematicRoles = new ArrayList<>();

    @XmlElementWrapper(name = "FRAMES")
    @XmlElement(name = VnFrameXml.ROOT_NAME, required = true)
    private List<VnFrameXml> frameElements = new ArrayList<>();

    @XmlElementWrapper(name = "SUBCLASSES")
    @XmlElement(name = "VNSUBCLASS", required = true)
    private List<VnClassXml> children = new ArrayList<>();

    private transient VnClass parentClass;

    @Override
    public List<VnMember> members() {
        return memberElements.stream()
            .map(member -> (VnMember) member)
            .collect(Collectors.toList());
    }

    @Override
    public List<VnThematicRole> roles() {
        return thematicRoles.stream()
            .map(role -> (VnThematicRole) role)
            .collect(Collectors.toList());
    }

    @Override
    public List<VnFrame> frames() {
        return frameElements.stream()
            .map(frame -> (VnFrame) frame)
            .collect(Collectors.toList());
    }

    @Override
    public List<VnClass> subclasses() {
        return children.stream()
            .map(cls -> (VnClass) cls)
            .collect(Collectors.toList());
    }

    public Optional<VnClass> parentClass() {
        return Optional.ofNullable(parentClass);
    }

    public static class VerbNetIdXmlAdapter extends XmlAdapter<String, VnClassId> {

        @Override
        public VnClassId unmarshal(String value) {
            if (null == value) {
                return null;
            }
            return VnClassId.parse(value);
        }

        @Override
        public String marshal(VnClassId value) {
            if (null == value) {
                return null;
            }
            return value.toString();
        }
    }

}
