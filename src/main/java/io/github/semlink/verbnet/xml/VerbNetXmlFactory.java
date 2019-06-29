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

import org.xml.sax.InputSource;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import io.github.semlink.verbnet.VnClass;
import io.github.semlink.verbnet.xml.VnFrameXml.Syntax;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static io.github.semlink.verbnet.xml.VnFrameXml.AdjectiveXml;
import static io.github.semlink.verbnet.xml.VnFrameXml.AdverbXml;
import static io.github.semlink.verbnet.xml.VnFrameXml.LexXml;
import static io.github.semlink.verbnet.xml.VnFrameXml.NounPhraseXml;
import static io.github.semlink.verbnet.xml.VnFrameXml.PrepXml;
import static io.github.semlink.verbnet.xml.VnFrameXml.VerbXml;

/**
 * VerbNetXml factory.
 *
 * @author jgung
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VerbNetXmlFactory {

    private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /**
     * Reads a single VerbNetXml XML file, a collection of VerbNetXml classes.
     *
     * @param inputStream VerbNetXml XML file input stream
     * @return VerbNetXml classes
     */
    public static List<VnClass> readVerbNet(InputStream inputStream) {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setFeature(LOAD_EXTERNAL_DTD, false);
            SAXSource source = new SAXSource(parserFactory.newSAXParser().getXMLReader(), new InputSource(inputStream));
            VerbNetXml verbNet = (VerbNetXml) JAXBContext.newInstance(VerbNetXml.class,
                AdjectiveXml.class, AdverbXml.class, NounPhraseXml.class, PrepXml.class, LexXml.class, VerbXml.class)
                .createUnmarshaller().unmarshal(source);
            verbNet.classes().forEach(VerbNetXmlFactory::setPointers);
            return verbNet.verbClasses();
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while reading VerbNetXml XML files", e);
        }
    }

    private static void setPointers(VnClassXml parent) {
        for (VnMemberXml member : parent.memberElements()) {
            member.verbClass(parent);
        }
        for (VnFrameXml frameXml : parent.frameElements()) {
            frameXml.verbClass(parent);
            int index = 0;
            for (Syntax syntax : frameXml.syntaxElements()) {
                syntax.index(index++);
            }
        }
        for (VnClassXml verbNetClass : parent.children()) {
            verbNetClass.parentClass(parent);
            setPointers(verbNetClass);
        }
    }

}
