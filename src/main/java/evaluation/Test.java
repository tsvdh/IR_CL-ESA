package evaluation;

import edu.kit.aifb.concept.ConceptVectorSimilarity;
import edu.kit.aifb.concept.IConceptExtractor;
import edu.kit.aifb.concept.IConceptIndex;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.scorer.CosineScorer;
import edu.kit.aifb.document.TextDocument;
import edu.kit.aifb.nlp.Language;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        List<String> sentencesA = new ArrayList<>();
        sentencesA.add("I often ride on my bike");
        sentencesA.add("Pancakes are my favorite food");
        sentencesA.add("Doing homework is boring");
        sentencesA.add("I will try to go to bed early");
        sentencesA.add("My bank account is empty");

        List<String> sentencesB = new ArrayList<>();
        sentencesB.add("Riding on the bicycle is something I enjoy");
        sentencesB.add("I like pancakes with syrup");
        sentencesB.add("I fell asleep working for school");
        sentencesB.add("My sleeping schedule is messed up");
        sentencesB.add("I have no money left");

        String lang_key = "en";

        Language lang = Language.getLanguage(lang_key);

        ApplicationContext context = new FileSystemXmlApplicationContext("config/evaluation_context.xml");

        IConceptIndex conceptIndex = (IConceptIndex) context.getBean("concept_index_" + lang_key);

        IConceptExtractor conceptExtractor = conceptIndex.getConceptExtractor();

        ConceptVectorSimilarity vectorSimilarity = new ConceptVectorSimilarity(new CosineScorer());

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                TextDocument docA = new TextDocument("a");
                docA.setText("content", lang, sentencesA.get(i));
                TextDocument docB = new TextDocument("b");
                docB.setText("content", lang, sentencesB.get(j));

                IConceptVector vectorA = conceptExtractor.extract(docA);
                IConceptVector vectorB = conceptExtractor.extract(docB);

                double similarity = vectorSimilarity.calcSimilarity(vectorA, vectorB);
                System.out.printf("%s %s - %s\n", i, j, similarity);
            }
        }
    }
}
