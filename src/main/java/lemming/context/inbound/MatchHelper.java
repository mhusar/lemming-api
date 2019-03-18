package lemming.context.inbound;

import lemming.context.BaseContext;
import name.fraser.neil.plaintext.diff_match_patch;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

/**
 * A helper class for context matching.
 */
public abstract class MatchHelper {
    /**
     * Returns the Levenshtein Distance between two texts.
     *
     * @param text1 text 1
     * @param text2 text 2
     * @return The Levenshtein distance.
     */
    private static Integer getLeventstheinDistance(String text1, String text2) {
        diff_match_patch diffMatchPatch = new diff_match_patch();
        LinkedList<diff_match_patch.Diff> diffs = diffMatchPatch.diff_main(text1, text2, false);
        return diffMatchPatch.diff_levenshtein(diffs);
    }

    /**
     * Returns the Levenshtein distance between two contexts.
     *
     * @param context1 context 1
     * @param context2 context 2
     * @return The Levenshtein distance.
     */
    private static Integer getDistance(BaseContext context1, BaseContext context2) {
        String context1String = context1.toString("#");
        String context2String = context2.toString("#");
        return getLeventstheinDistance(context1String, context2String);
    }

    /**
     * Computes distances between contexts.
     *
     * @param contexts1 first list of contexts
     * @param contexts2 second list of contexts
     * @return A map of triples.
     */
    public static MultivaluedMap<Integer, Triple> getTriples(List<? extends BaseContext> contexts1,
                                                                    List<? extends BaseContext> contexts2) {
        MultivaluedMap<Integer, Triple> distanceMap = new MultivaluedHashMap<>();

        for (int i = 0; i < contexts1.size(); i++) {
            for (int j = 0; j < contexts2.size(); j++) {
                Integer distance = getDistance(contexts1.get(i), contexts2.get(j));
                distanceMap.add(distance, new Triple(contexts1.get(i), i, distance, contexts2.get(j), j));
            }
        }

        return distanceMap;
    }

    /**
     * Sorts a list of triples by number attribute of a triple’s first context.
     *
     * @param triples list of triples
     * @return A list of triples.
     */
    public static List<Triple> sortTriples(List<Triple> triples) {
        triples.sort((triple1, triple2) -> {
            int number1 = triple1.getContext1().getNumber();
            int number2 = triple2.getContext1().getNumber();

            if (number1 == number2) {
                return 0;
            } else {
                return (number1 < number2) ? -1 : 1;
            }
        });

        return triples;
    }

    /**
     * Checks if a triple has no intersection with triples in a list.
     *
     * @param triple a triple
     * @param triples list of triples
     * @return True if there is no intersection; false otherwise.
     */
    private static boolean hasNoIntersectionWith(Triple triple, List<Triple> triples) {
        for (Triple t : triples) {
            if (t.getContext1Index().equals(triple.getContext1Index())) {
                return false;
            } else if (t.getContext2Index().equals(triple.getContext2Index())) {
                return false;
            } else if (t.getContext1Index() < triple.getContext1Index() &&
                    t.getContext2Index() > triple.getContext2Index()) {
                return false;
            } else if (t.getContext1Index() > triple.getContext1Index() &&
                    t.getContext2Index() < triple.getContext2Index()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Applies triples in order of lowest distance if they don’t have an intersection with each other.
     *
     * @return A list of triples.
     */
    public static List<Triple> computeMatchingTriples(MultivaluedMap<Integer, Triple> distanceMap) {
        TreeSet<Integer> distanceSet = new TreeSet<>(distanceMap.keySet());
        ArrayList<Triple> appliedTriples = new ArrayList<>();

        for (Integer distance : distanceSet) {
            List<Triple> triples = distanceMap.get(distance);

            for (Triple triple : triples) {
                if (appliedTriples.isEmpty()) {
                    appliedTriples.add(triple);
                } else if (hasNoIntersectionWith(triple, appliedTriples)) {
                    appliedTriples.add(triple);
                }
            }
        }

        return appliedTriples;
    }

    /**
     * Checks if a list of triples have consistent distances.
     *
     * @param triples list of triples
     * @return True if distance is consistent; false otherwise.
     */
    public static boolean haveConsistentDistance(List<Triple> triples) {
        int distance = -1;

        for (Triple triple : triples) {
            if (triple.getDistance() != distance) {
                if (distance == -1) {
                    distance = triple.getDistance();
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if contexts of a triple have identical keywords.
     *
     * @param triple a triple
     * @return True if keywords are identical; false otherwise.
     */
    public static boolean hasIdenticalKeywords(Triple triple) {
        return triple.getContext1().getKeyword().equals(triple.getContext2().getKeyword());
    }
}
