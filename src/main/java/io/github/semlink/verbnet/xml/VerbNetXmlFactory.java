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
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import io.github.semlink.verbnet.VnClass;
import io.github.semlink.verbnet.xml.VnFrameXml.Syntax;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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
public final class VerbNetXmlFactory {

    private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /**
     * Reads a single VerbNetXml XML file, a collection of VerbNetXml classes.
     *
     * @param inputStream VerbNetXml XML file input stream
     * @return VerbNetXml classes
     */
    public static List<VnClass> readVerbNet(InputStream inputStream) {
        try {
            SAXSource source = new SAXSource(xmlReader(), new InputSource(inputStream));
            VerbNetXml verbNet = (VerbNetXml) unmarshaller(VerbNetXml.class).unmarshal(source);
            verbNet.classes().forEach(VerbNetXmlFactory::setPointers);
            return verbNet.verbClasses();
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while reading VerbNetXml XML files", e);
        }
    }

    /**
     * Read all VerbNet XML files at a given directory.
     *
     * @param path path to VerbNet directory containing only valid VerbNet XML files
     * @return list with one root {@link VnClass} per XML file in directory
     */
    public static List<VnClass> readFromDirectory(@NonNull Path path) {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Was expecting a directory, got a file at " + path.toString());
        }
        XMLReader xmlReader = xmlReader();
        Unmarshaller unmarshaller = unmarshaller(VnClassXml.class);
        try (Stream<Path> xmls = Files.list(path)
                .filter(s -> s.toString().endsWith(".xml"))
                .sorted(Comparator.comparing(Path::toString))) {
            return xmls.map(xml -> readFromXml(xml, unmarshaller, xmlReader)).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read a single VerbNet XML file as a {@link VnClass} from a given {@link InputStream}.
     *
     * @param inputStream VerbNet XML input stream
     * @return root VerbNet class
     */
    public static VnClass readFromXml(@NonNull InputStream inputStream) {
        return readFromXml(inputStream, unmarshaller(VnClassXml.class), xmlReader());
    }

    /**
     * Read a single VerbNet XML file as a {@link VnClass} at a given {@link Path}.
     *
     * @param path VerbNet XML path
     * @return root VerbNet class
     */
    public static VnClass readFromXml(@NonNull Path path) {
        return readFromXml(path, unmarshaller(VnClassXml.class), xmlReader());
    }

    private static VnClass readFromXml(Path xmlPath, Unmarshaller unmarshaller, XMLReader xmlReader) {
        try (InputStream inputStream = Files.newInputStream(xmlPath)) {
            return readFromXml(inputStream, unmarshaller, xmlReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException("Error reading XML at " + xmlPath.toString(), e);
        }
    }

    private static VnClass readFromXml(InputStream inputStream, Unmarshaller unmarshaller, XMLReader xmlReader) {
        try {
            VnClassXml vnClass = (VnClassXml) unmarshaller.unmarshal(new SAXSource(xmlReader, new InputSource(inputStream)));
            setPointers(vnClass);
            return vnClass;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static Unmarshaller unmarshaller(Class clazz) {
        try {
            return JAXBContext.newInstance(clazz, AdjectiveXml.class, AdverbXml.class, NounPhraseXml.class, PrepXml.class,
                    LexXml.class, VerbXml.class).createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static XMLReader xmlReader() {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setFeature(LOAD_EXTERNAL_DTD, false);
            return parserFactory.newSAXParser().getXMLReader();
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
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
