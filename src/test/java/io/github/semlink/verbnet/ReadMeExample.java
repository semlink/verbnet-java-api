package io.github.semlink.verbnet;

import org.junit.Ignore;

import java.util.List;
import java.util.Optional;

import io.github.semlink.verbnet.semantics.VnSemanticPredicate;
import io.github.semlink.verbnet.syntax.VnSyntax;

/**
 * Ensure that the README.MD code at least compiles properly.
 *
 * @author jgung
 */
@Ignore
@SuppressWarnings("unused")
public class ReadMeExample {

    public static void main(String[] args) {
        VnIndex vnIndex = DefaultVnIndex.fromDirectory("/path/to/verbnet/xmls/");

        VnClass comprehend = vnIndex.getById("87.2-1"); // same as `vnIndex.getById("comprehend-87.2-1")`

        // members for specific to this subclass
        List<VnMember> members = comprehend.members();

        // all members, including those inherited from parent classes (ancestors)
        List<VnMember> allMembers = comprehend.membersIncludeInherited();
        for (VnMember member : allMembers) {
            // name of this member, e.g. "comprehend" or "realize"
            String name = member.name();
            // list of mapped WordNet synset keys
            List<WnKey> wn = member.wn();
            // PropBank rolesets mapped to this member
            List<String> groupings = member.groupings();
            // list of verb-specific features for this member
            List<String> features = member.features();
        }

        // all frames for this class, including those inherited from parent classes
        List<VnFrame> frames = comprehend.framesIncludeInherited();

        for (VnFrame frame : frames) {
            // list of syntactic elements for this frame
            List<VnSyntax> syntax = frame.syntax();
            // list of semantic predicates for this frame
            List<VnSemanticPredicate> predicates = frame.predicates();
        }

        // parent class (empty if root)
        Optional<VnClass> parent = comprehend.parentClass();

        // ancestors (parent, parent of parent, etc.)
        List<VnClass> ancestors = comprehend.ancestors();

        // all descendant subclasses (children, children of children, etc.)
        List<VnClass> descendants = comprehend.descendants();

        // only children
        List<VnClass> subclasses = comprehend.subclasses();

        // all related subclasses, including self
        List<VnClass> related = comprehend.related();
    }

}
