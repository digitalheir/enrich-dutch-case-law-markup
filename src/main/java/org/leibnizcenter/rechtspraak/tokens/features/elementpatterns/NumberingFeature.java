package org.leibnizcenter.rechtspraak.tokens.features.elementpatterns;

import cc.mallet.types.Token;
import com.google.common.base.Strings;
import org.leibnizcenter.rechtspraak.tokens.features.Features;
import org.leibnizcenter.rechtspraak.tokens.features.elementpatterns.interfaces.ElementFeatureFunction;
import org.leibnizcenter.rechtspraak.tokens.features.elementpatterns.interfaces.NamedElementFeatureFunction;
import org.leibnizcenter.rechtspraak.tokens.features.textpatterns.GeneralTextFeature;
import org.leibnizcenter.rechtspraak.tokens.features.textpatterns.KnownSurnamesNl;
import org.leibnizcenter.rechtspraak.tokens.numbering.interfaces.FullNumber;
import org.leibnizcenter.rechtspraak.tokens.text.TokenTreeLeaf;
import org.leibnizcenter.rechtspraak.tokens.numbering.*;
import org.leibnizcenter.rechtspraak.tokens.numbering.interfaces.AlphabeticNumbering;
import org.leibnizcenter.rechtspraak.tokens.numbering.interfaces.NumberingNumber;
import org.leibnizcenter.rechtspraak.tokens.tokentree.SameKindOfNumbering;
import org.leibnizcenter.rechtspraak.util.Collections3;
import org.leibnizcenter.rechtspraak.util.Regex;
import org.leibnizcenter.rechtspraak.util.Strings2;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by maarten on 1-4-16.
 */
public enum NumberingFeature implements NamedElementFeatureFunction {
    FOLLOWED_BY_SAME_PROFILE_NUMBERING((tokens, i) -> tokens.get(i) instanceof Numbering && ((Numbering) tokens.get(i)).getSameProfileSuccessors().size() > 0),
    PRECEDED_BY_SAME_PROFILE_NUMBERING((tokens, i) -> tokens.get(i) instanceof Numbering && ((Numbering) tokens.get(i)).getSameProfilePredecessors().size() > 0),

    NUM_LARGER_THAN_5(numLargerThan(5)),
    NUM_LARGER_THAN_10(numLargerThan(10)),
    NUM_LARGER_THAN_15(numLargerThan(15)),
    NUM_LARGER_THAN_20(numLargerThan(20)),
    NUM_LARGER_THAN_50(numLargerThan(50)),
    NUM_LARGER_THAN_75(numLargerThan(75)),
    NUM_LARGER_THAN_100(numLargerThan(100)),


    HAS_PLAUSIBLE_SUCCEDENT((tokens, i) -> tokens.get(i) instanceof Numbering && ((Numbering) tokens.get(i)).getPlausibleSuccessors().size() > 0),
    HAS_PLAUSIBLE_PRECEDENT((tokens, i) -> tokens.get(i) instanceof Numbering && ((Numbering) tokens.get(i)).getPlausiblePredecessors().size() > 0),

    ODD_TERMINAL_CHAR((tokens, i) -> {
        if (tokens.get(i) instanceof Numbering) {
            String t = ((Numbering) tokens.get(i)).getTerminal();
            return !(Strings.isNullOrEmpty(t) || Regex.DOTDOT_.matcher(t.trim()).matches());
        } else {
            return false;
        }
    }),

    isFirstNumbering((tokens, i) -> tokens.get(i) instanceof Numbering && ((Numbering) tokens.get(i)).getNumbering().isFirstNumbering()),

    //looksLikeNumberingButProbablyIsnt(NumberingFeature::looksLikeNumberingButProbablyIsnt),
    PART_OF_WG(NumberingFeature::partOfWasGetekend),
    FOLLOWS_WG(NumberingFeature::followsWasGetekend),
    probableAbbrFlorijnen(NumberingFeature::probableAbbrFlorijnen),
    probableAbbrEuro(NumberingFeature::probableAbbrEuro),
    probablyJustU(NumberingFeature::probablyJustU),
    _S(NumberingFeature::_s),
    MORE_THAN_2_NUMBERS_IN_SEQUENCE((elements, ix) ->
            !Collections3.isNullOrEmpty(SameKindOfNumbering.ANY_NUMBERING.getList(elements, ix))
                    && SameKindOfNumbering.ANY_NUMBERING.getList(elements, ix).size() > 2),
    IS_SPELLED_OUT(NumberingFeature::isSpelledOut),// 2 (twee)
    isPossiblyJustStartOfSentence(NumberingFeature::isPossiblyJustStartOfSentence),// 7 patronen
    isPartOfSpacedLetters(NumberingFeature::isJustSpacedLetters),// u i t s p r a a k
    IS_PROBABLY_NAME(NumberingFeature::isProbablyName),

    hasAlphabetic(((tokens, ix) -> Numbering.isAlphabetic(tokens.get(ix)))),
    hasArabicNumber(((tokens, ix) -> Numbering.isArabic(tokens.get(ix)))),
    hasNonNumeric(((tokens, ix) -> Numbering.isNonNumeric(tokens.get(ix)))),
    hasRomanNumber(((tokens, ix) -> Numbering.isRoman(tokens.get(ix)))),
    hasCompositeNumber(((tokens, ix) -> Numbering.isCompositeNumbering(tokens.get(ix)))),

    PREFIXED_WITH_0(((tokens, ix) -> Strings2.firstCharIs(tokens.get(ix).getTextContent(), '0'))),

    hasAlphabeticNumberWithNoTerminal((tokens, ix) -> Numbering.isAlphabetic(tokens.get(ix)) && noTerminal(tokens, ix)),
    hasArabicNumberWithNoTerminal(((tokens, ix) -> Numbering.isArabic(tokens.get(ix)) && noTerminal(tokens, ix))),
    hasNonNumericNumberWithNoTerminal((tokens, ix) -> Numbering.isNonNumeric(tokens.get(ix)) && noTerminal(tokens, ix)),
    hasRomanNumberWithNoTerminal((tokens, ix) -> Numbering.isRoman(tokens.get(ix)) && noTerminal(tokens, ix)),

    PART_OF_NUMBER_CLUSTER_W_ODD_TERMINAL((tokens, ix) -> ODD_TERMINAL_CHAR.apply(tokens, ix)
            && !Collections3.isNullOrEmpty(((Numbering) tokens.get(ix)).getSequence(SameKindOfNumbering.ANY_NUMBERING))),
    SUSPECT_NUMBERING((tokens, ix) -> ODD_TERMINAL_CHAR.apply(tokens, ix) || PREFIXED_WITH_0.apply(tokens, ix)),
    IS_TAINTED_BY_IMPLAUSIBLE_SIBLING_NR((tokens, ix) -> {
        for (SameKindOfNumbering p : SameKindOfNumbering.values()) {
            if (!Collections3.isNullOrEmpty(((Numbering) tokens.get(ix)).getSequence(p))
                    && ((Numbering) tokens.get(ix)).getSequence(p).isTaintedByImplausibleNumbering()) return true;
        }
        return false;
    }
    ), HAS_NO_PLAUSIBLE_PREDECESSORS_OR_SUCCESSORS(((tokens, ix) -> Collections3.isNullOrEmpty(Numbering.getPlausiblePredecessors(tokens.get(ix)))
            && Collections3.isNullOrEmpty(Numbering.getPlausibleSuccessors(tokens.get(ix)))));


    private static ElementFeatureFunction numLargerThan(int checkfor) {
        return (tokens, ix) -> tokens.get(ix) instanceof Numbering
                && (
                isArabicNumberLargerThan(checkfor, (Numbering) tokens.get(ix))
                        || isPartOFSectionNumberLargerThan(checkfor, (Numbering) tokens.get(ix))
        );
    }

    private static boolean isPartOFSectionNumberLargerThan(final int checkfor, Numbering numbering) {
        return numbering.getNumbering() instanceof SubSectionNumber
                // Whether there is at least one subsection part that is larger than checkfor
                && ((SubSectionNumber) numbering.getNumbering()).stream()
                .filter((n) -> isArabicNumberLargerThan(checkfor, n))
                .limit(1)
                .collect(Collectors.toSet()).size() > 0;
    }

    private static boolean isArabicNumberLargerThan(int checkfor, NumberingNumber numbering) {
        return numbering instanceof ArabicNumbering
                && ((FullNumber) numbering).mainNum() > checkfor;
    }

    private static boolean isArabicNumberLargerThan(int checkfor, Numbering numbering) {
        return isArabicNumberLargerThan(checkfor, numbering.getNumbering());
    }

    private static boolean noTerminal(List<TokenTreeLeaf> tokens, int ix) {
        return Strings.isNullOrEmpty(((Numbering) tokens.get(ix)).getTerminal());
    }


//    private static boolean numberingInSequence(List<AbstractRechtspraakElement> tokens, int i) {
//        if (tokens.get(i) instanceof Numbering) {
//            Numbering numbering1 = (Numbering) tokens.get(i);
//            NumberingNumber numbering = numbering1.getNumbering();
//            if (numbering.isFirstNumbering()) return true;
//
//            numbering1.getPlausibleSuccessors();
//
//            else if (numbering instanceof SingleCharNumbering) {
//                return isFirstPlausibleAlphabeticNumWalkingBackFrom(((SingleCharNumbering) numbering).prevChar(), tokens, i);
//            } else if (numbering instanceof SubSectionNumber) {
//                df
//            } else if (numbering instanceof FullNumber) {
//                return isFirstPlausibleNumberingWalkingBackFrom(((FullNumber) numbering).mainNum(), tokens, i);
//            } else {
//                throw new IllegalStateException(numbering.getClass().getSimpleName() + "?");
//            }
//        }
//    }


    public final ElementFeatureFunction function;

    NumberingFeature(ElementFeatureFunction function) {
        this.function = function;
    }


    public static boolean isSpelledOut(List<TokenTreeLeaf> untagged, int ix) {
        if (untagged.size() > ix + 1 && NumberingFeature.hasArabicNumberWithNoTerminal.apply(untagged, ix)) {
            NumberingNumber numbering = ((Numbering) untagged.get(ix)).getNumbering();
            TokenTreeLeaf next = untagged.get(ix + 1);
            String text = next.getNormalizedText();
            switch (((ArabicNumbering) numbering).mainNum()) {
                case 1:
                    return text.startsWith("een") || text.startsWith("één");
                case 2:
                    return text.startsWith("twee");
                case 3:
                    return text.startsWith("drie");
                case 4:
                    return text.startsWith("vier");
                case 5:
                    return text.startsWith("vijf");
                case 6:
                    return text.startsWith("zes");
                case 7:
                    return text.startsWith("zeven");
                case 8:
                    return text.startsWith("acht");
                case 9:
                    return text.startsWith("negen");
                case 10:
                    return text.startsWith("tien");
                case 11:
                    return text.startsWith("elf");
                case 12:
                    return text.startsWith("twaalf");
                case 13:
                    return text.startsWith("dertien");
                case 14:
                    return text.startsWith("veertien");
                case 15:
                    return text.startsWith("vijftien");
                case 16:
                    return text.startsWith("zestien");
                case 17:
                    return text.startsWith("zeventien");
                case 18:
                    return text.startsWith("achttien");
                case 19:
                    return text.startsWith("negentien");
                case 20:
                    return text.startsWith("twintig");
            }
        }
        return false;
    }

    public static void setFeatures(Token t, List<TokenTreeLeaf> tokens, int ix) {
        TokenTreeLeaf token = tokens.get(ix);
        if (token instanceof Numbering) {
            NumberingNumber numbering = ((Numbering) token).getNumbering();
            String terminal = numbering.getTerminal();

            t.setFeatureValue("NUM_TERMINAL_" + terminal, 1.0);

            // Run through enum
            Features.setFeatures(t, tokens, ix, (NamedElementFeatureFunction[]) NumberingFeature.values());

            for (SameKindOfNumbering p : SameKindOfNumbering.values()) {
                if (!Collections3.isNullOrEmpty(((Numbering) token).getSequence(p))) t.setFeatureValue("PART_OF_" + p.name(), 1.0);
            }
        }
    }

    private static boolean isPossiblyJustStartOfSentence(List<TokenTreeLeaf> untagged, int ix) {
        if (untagged.size() > ix + 1) {
            NumberingNumber numbering = ((Numbering) untagged.get(ix)).getNumbering();
            if (Strings.isNullOrEmpty(numbering.getTerminal())
                    && (numbering instanceof ArabicNumbering || numbering instanceof AlphabeticNumbering)
                    ) {
                return true;
            }
        }
        return false;
    }

    private static boolean isProbablyName(List<TokenTreeLeaf> untagged, int ix) {
        char num = getUppercaseCharNumbering(untagged.get(ix));

        // If this element is an uppercase char, it might be part of a name
        if (num != '\0') {

            //noinspection UnnecessaryLocalVariable
            int startToken = ix, lastToken = ix;
            if (!GeneralTextFeature.followsLineBreak(untagged, ix)) {
                for (int i = ix - 1; num != '\0' && i >= 0; i--) {
                    TokenTreeLeaf token = untagged.get(ix);
                    num = getUppercaseCharNumbering(token);
                    if (num != '\0') {
                        // Having multiple letters after each other
                        // doesn't make sense for a list
                        // ex.
                        //    A. [T] S Eliot said some thing
                        // ex.
                        //    C [B] de Mill said otherwise
                        // startToken = i;
                        //if (!token.followsLineBreak) {
                        //num = '\0';
                        //}
                        return true;
                    } else num = '\0';
                }
            }

            for (int i = ix + 1; num != '\0' && i < untagged.size(); i++) {
                TokenTreeLeaf token = untagged.get(ix);
                if (!GeneralTextFeature.followsLineBreak(untagged, ix)) {
                    num = getUppercaseCharNumbering(token);
                    if (num != '\0') {
                        // Having multiple letters after each other
                        // doesn't make sense for a list
                        // NOTE: this fails on the case
                        //    [A.] T S Eliot said some thing
                        //    [B.] C B de Mill said otherwise
                        // startToken = i;
                        return true;
                    }
                } else num = '\0';
            }

            if (lastToken - (startToken - 1) > 1) {
                // Having multiple letters after each other doesn't make sense for a list
                // NOTE: this fails on the case
                //    [A.] T S Eliot said some thing
                //    [B.] C B de Mill said otherwise
                return true;
            } else {
                // startToken == lastToken

                // Single char.... if this is an A., only return
                // true if it's followed by a known last name
                TokenTreeLeaf token = untagged.get(startToken);
                return getUppercaseCharNumbering(token) == 'A'
                        && startToken < untagged.size() - 1
                        && !GeneralTextFeature.followsLineBreak(untagged, ix)
                        && KnownSurnamesNl.startsWithName.apply(untagged.get(startToken + 1).getTextContent());
            }
            //List<Character> stringOfChars = new ArrayList<>(lastToken-(startToken-1));
        }

        return false;
    }

    private static boolean _s(List<TokenTreeLeaf> elements, int ix) {
        return (ix > 0
                && (Numbering.isCharNumbering('s', elements.get(ix)) ||
                Numbering.isCharNumbering('S', elements.get(ix)))
                && elements.get(ix - 1).getTextContent().endsWith("'")
                && !walkingBackFirstSameProfileIsPredecessor(elements, ix)
        );
    }


    private static boolean walkingBackFirstSameProfileIsPredecessor(
            List<TokenTreeLeaf> untagged,
            int ix) {
        Numbering n2 = (Numbering) untagged.get(ix);
        for (int i = ix - 1; i >= 0; i--) {
            TokenTreeLeaf n1 = untagged.get(i);
            if (n1 instanceof Numbering) {
                if (SameKindOfNumbering.isSameProfile((Numbering) n1, n2))
                    return SameKindOfNumbering.isSameProfileSuccession((Numbering) n1, n2);
            }
        }
        return false;
    }

    public static boolean isFirstPlausibleNumberingWalkingBackFrom(int num, List<TokenTreeLeaf> untagged, int ix) {
        for (int i = ix - 1; i >= 0; i--) {
            if (untagged.get(i) instanceof Numbering) {
                Numbering n = (Numbering) untagged.get(i);
                if (n.isPlausibleNumbering
                        && n.getNumbering() != null
                        && n.getNumbering() instanceof ArabicNumbering) {
                    return ((ArabicNumbering) n.getNumbering()).mainNum() == num;
                }
            }
        }
        return false;
    }


    private static boolean hasCharNumberingAndDoesntFollowsPreviousChar(List<TokenTreeLeaf> elements, int ix, char f) {
        TokenTreeLeaf e = elements.get(ix);
        return Numbering.isCharNumbering(f, e) && !walkingBackFirstSameProfileIsPredecessor(elements, ix);
    }

    /**
     * Used for checking names
     *
     * @param token
     * @return
     */
    private static char getUppercaseCharNumbering(TokenTreeLeaf token) {
        char ch = SingleCharNumbering.getChar(token);
        return (ch != '\0' && Character.isUpperCase(ch)) ? ch : '\0';
    }


    private static boolean wasGetekend(TokenTreeLeaf first, TokenTreeLeaf second) {
        return Numbering.isCharNumbering('w', first)
                && Numbering.isCharNumbering('g', second);
    }

    private static boolean followsWasGetekend(List<TokenTreeLeaf> elements, int ix) {
        return ix > 1 && wasGetekend(elements.get(ix - 2), elements.get(ix - 1));
    }

    private static boolean partOfWasGetekend(List<TokenTreeLeaf> elements, int ix) {
        TokenTreeLeaf e = elements.get(ix);
        return ((ix + 1) < elements.size() && wasGetekend(e, elements.get(ix + 1)))
                || (ix > 0 && wasGetekend(elements.get(ix - 1), e));
    }


    private static boolean probablyJustU(List<TokenTreeLeaf> elements, int ix) {
        return Strings.isNullOrEmpty(((Numbering) elements.get(ix)).getNumbering().getTerminal())
                && hasCharNumberingAndDoesntFollowsPreviousChar(elements, ix, 'u')
                ;
    }

    private static boolean probableAbbrEuro(List<TokenTreeLeaf> elements, int ix) {
        return hasCharNumberingAndDoesntFollowsPreviousChar(elements, ix, 'e');
    }

    private static boolean probableAbbrFlorijnen(List<TokenTreeLeaf> elements, int ix) {
        return hasCharNumberingAndDoesntFollowsPreviousChar(elements, ix, 'f');
    }

    private static boolean isJustSpacedLetters(List<TokenTreeLeaf> tokens, int ix) {
        if (tokens.get(ix) instanceof Numbering
                && ((Numbering) tokens.get(ix)).getSequence(SameKindOfNumbering.ALPHABETIC_SAME_TERMINAL) != null) {
            SameKindOfNumbering.List seq = ((Numbering) tokens.get(ix)).getSequence(SameKindOfNumbering.ALPHABETIC_SAME_TERMINAL);
            return seq.size() > 2 && (seq.size() > 3 || Strings.isNullOrEmpty(((Numbering) tokens.get(ix)).getTerminal()));
        } else {
            return false;
        }
    }

    @Override
    public boolean apply(List<org.leibnizcenter.rechtspraak.tokens.text.TokenTreeLeaf> tokens, int ix) {
        return function.apply(tokens, ix);
    }

    public static boolean any(List<TokenTreeLeaf> tokens, int ix, NumberingFeature... features) {
        for (NumberingFeature f : features) {
            if (f.apply(tokens, ix))
                return true;
        }
        return false;
    }
}
