package tla.web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import tla.domain.dto.LemmaDto;
import tla.domain.model.Language;
import tla.domain.model.Script;
import tla.domain.model.extern.AttestedTimespan;
import tla.domain.model.meta.BTSeClass;
import tla.domain.model.meta.TLADTO;

@Data
@Slf4j
@SuperBuilder
@NoArgsConstructor
@BackendPath("lemma")
@TLADTO(LemmaDto.class)
@BTSeClass("BTSLemmaEntry")
@EqualsAndHashCode(callSuper = true)
public class Lemma extends TLAObject {

    /**
     * The passport locator where bibliographical information should be stored.
     */
    public static final String PASSPORT_PROP_BIBL = "bibliography.bibliographical_text_field";

    @Setter(AccessLevel.NONE)
    private List<String> bibliography;

    @Singular
    private SortedMap<Language, List<String>> translations;

    @Singular
    private List<Word> words;

    @Singular
    private List<AttestedTimespan> attestations;

    /**
     * first and last year
     */
    private AttestedTimespan.Period timespan;

    public Long getAttestationCount() {
        return this.attestations.stream().mapToLong(
            timespan -> timespan.getAttestations().getCount()
        ).sum();
    }

    /**
    * Determines the language phase this lemma belongs to
    */
    public Script getDictionaryName() {
        return Script.ofLemmaId(this.getId());
    }

    /**
     * Extract hieroglyphs from lemma words.
     * Return null if only empty hieroglyphs can be found.
     *
     * @return List of all lemma word hieroglyphs, or null if there are no hieroglyphs at all
     */
    public List<Glyphs> getHieroglyphs() {
        if (this.getDictionaryName().equals(Script.HIERATIC)) {
            List<Glyphs> hieroglyphs = this.getWords().stream().map(
                Word::getGlyphs
            ).collect(
                Collectors.toList()
            );
            return (hieroglyphs.stream().allMatch(
                glyphs -> glyphs == null || glyphs.isEmpty()
            )) ? null : hieroglyphs;
        }
        return null;
    }

    /**
     * Returns a list of bibliographic references extracted from this lemma's
     * <code>bibliography.bibliographical_text_field</code> passport field.
     *
     * @see {@link #extractBibliography(Lemma)}
     */
    public List<String> getBibliography() {
        if (this.bibliography == null) {
            this.bibliography = extractBibliography(this);
        }
        return this.bibliography;
    }

    /**
     * Extract bibliographic information from lemma passport.
     *
     * Bibliography is being copied from the <code>bibliography.bibliographical_text_field</code>
     * passport field. The value(s) found under that locator are split at semicolons <code>";"</code>.
     *
     * @param lemma The Lemma instance from whose passport the bibliography is to be extracted.
     * @return List of textual bibliographic references or an empty list
     */
    private static List<String> extractBibliography(Lemma lemma) {
        List<String> bibliography = new ArrayList<>();
        try {
            lemma.getPassport().extractProperty(
                PASSPORT_PROP_BIBL
            ).forEach(
                node -> bibliography.addAll(
                    Arrays.asList(
                        node.getLeafNodeValue().split(";")
                    ).stream().map(
                        bibref -> bibref.strip()
                    ).collect(
                        Collectors.toList()
                    )
                )
            );
        } catch (Exception e) {
            log.debug("could not extract bibliography from lemma {}", lemma.getId());
        }
        return bibliography;
    }

}
