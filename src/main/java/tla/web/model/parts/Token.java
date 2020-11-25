package tla.web.model.parts;
    
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import tla.domain.model.Language;
import tla.domain.model.SentenceToken;

import java.util.List;
import java.util.SortedMap;

import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    private String id;

    private String label;

    private String type;

    private Transcription transcription;

    private Glyphs glyphs;

    private SentenceToken.Flexion flexion;

    private SentenceToken.Lemmatization lemma;

    @Singular
    private SortedMap<Language, List<String>> translations;

}