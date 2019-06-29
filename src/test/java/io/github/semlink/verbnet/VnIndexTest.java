package io.github.semlink.verbnet;

import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.github.semlink.verbnet.restrictions.VnRestrictions;
import io.github.semlink.verbnet.semantics.VnPredicatePolarity;
import io.github.semlink.verbnet.semantics.VnSemanticArgument;
import io.github.semlink.verbnet.semantics.VnSemanticPredicate;
import io.github.semlink.verbnet.syntax.VnNounPhrase;
import io.github.semlink.verbnet.syntax.VnSyntaxType;
import io.github.semlink.verbnet.xml.VnClassXml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * VerbNet reader unit tests.
 *
 * @author jgung
 */
public class VnIndexTest {

    private static VnIndex verbNet;

    @BeforeClass
    public static void init() {
        verbNet = DefaultVnIndex.fromDirectory(Paths.get("src/test/resources/test-verbnet"));
    }

    @Test
    public void testLoad$FromSingleXml() {
        VnIndex index = DefaultVnIndex.fromInputStream(VnClassXml.class.getClassLoader()
                .getResourceAsStream("test-verbnet.xml"));
        assertEquals(2, index.roots().size());
    }

    @Test
    public void testGet$ByLemma() {
        Set<VnClass> cls = verbNet.getByLemma("appreciate");
        assertEquals(1, cls.size());
        assertEquals("45.6.1-1", cls.iterator().next().verbNetId().classId());
    }

    @Test
    public void testGet$ByBaseIdAndLemma() {
        Set<VnClass> cls = verbNet.getByBaseIdAndLemma("45.6.1", "appreciate");
        assertEquals(1, cls.size());
        assertEquals("45.6.1-1", cls.iterator().next().verbNetId().classId());
    }

    @Test
    public void testGetMembers$ByWnKey() {
        Set<VnMember> members = verbNet.getMembersByWordNetKey(WnKey.parseWordNetKey("climb%2:38:01")
                .orElseThrow(IllegalArgumentException::new));
        assertEquals(1, members.size());
        assertEquals("climb", members.iterator().next().name());
    }

    @Test
    public void testGetMembers$ByLemma() {
        Set<VnMember> members = verbNet.getMembersByLemma("climb");
        assertEquals(1, members.size());
        assertEquals("climb", members.iterator().next().name());
    }

    @Test
    public void testGetWordNetKeys$ByLemma() {
        Set<WnKey> climb = verbNet.getWordNetKeysByLemma("climb");
        assertTrue(climb.contains(WnKey.parseWordNetKey("climb%2:38:01").orElseThrow(IllegalArgumentException::new)));
        assertTrue(climb.contains(WnKey.parseWordNetKey("climb%2:38:00").orElseThrow(IllegalArgumentException::new)));
        assertTrue(climb.contains(WnKey.parseWordNetKey("climb%2:30:01").orElseThrow(IllegalArgumentException::new)));
    }

    @Test
    public void testMembers() {
        VnClass verbNetClass = verbNet.getById("calibratable_cos-45.6.1");
        assertEquals(2, verbNetClass.members().size());
        VnMember build = verbNetClass.members().get(0);
        VnMember die = verbNetClass.members().get(1);
        assertTrue(build.features().contains("increase"));
        assertTrue(die.features().contains("decrease"));
        assertTrue(build.groupings().contains("build.02"));
        assertTrue(die.groupings().contains("die.02"));
        assertEquals("build", build.name());
        assertEquals("die", die.name());
        assertEquals("build#3", build.verbnetKey());
        assertEquals("die#2", die.verbnetKey());
        assertEquals(0, build.wn().size());
        assertEquals(0, die.wn().size());
    }

    @Test
    public void testThematicRoles() {
        VnClass beginClass = verbNet.getById("begin-55.1");
        assertEquals(3, beginClass.roles().size());
        VnThematicRole agent = beginClass.roles().get(0);
        VnThematicRole theme = beginClass.roles().get(1);
        VnThematicRole instrument = beginClass.roles().get(2);
        assertEquals("Agent", agent.type());
        assertEquals("Theme", theme.type());
        assertEquals("Instrument", instrument.type());
        List<VnRestrictions<String>> selRels = agent.restrictions();
        assertTrue(selRels.get(0).include().containsAll(Arrays.asList("animate", "location")));
        assertTrue(selRels.get(1).include().containsAll(Arrays.asList("organization", "location")));
        assertTrue(selRels.get(0).exclude().contains("region"));
        assertTrue(selRels.get(1).exclude().contains("region"));
    }

    @Test
    public void testFrames() {
        VnClass verbNetClass = verbNet.getById("calibratable_cos-45.6.1");
        assertEquals(3, verbNetClass.frames().size());

        VnFrame frame = verbNetClass.frames().get(0);
        assertEquals("The price of oil soared.", frame.examples().get(0));
        assertEquals("2.13.5", frame.description().descriptionNumber());
        assertEquals("NP.attribute V", frame.description().primary());
        assertEquals("Intransitive; Attribute Subject", frame.description().secondary());
        assertEquals("", frame.description().xtag());

        assertEquals(4, frame.syntax().size());
        VnNounPhrase patient = (VnNounPhrase) frame.syntax().get(2);
        assertEquals(VnSyntaxType.NP, patient.type());
        assertEquals(2, patient.index());
        assertEquals("Patient", patient.thematicRole());

        assertEquals(5, frame.predicates().size());
        VnSemanticPredicate semanticPredicate = frame.predicates().get(1);
        assertEquals(VnPredicatePolarity.TRUE, semanticPredicate.polarity());
        assertEquals("change_value", semanticPredicate.type());

        assertEquals(5, semanticPredicate.semanticArguments().size());
        VnSemanticArgument verbSpecific = semanticPredicate.semanticArguments().get(2);
        assertEquals("VerbSpecific", verbSpecific.type());
        assertEquals("V_Direction", verbSpecific.value());
    }

}
