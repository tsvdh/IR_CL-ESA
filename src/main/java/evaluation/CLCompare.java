package evaluation;

import edu.kit.aifb.concept.ConceptVectorSimilarity;
import edu.kit.aifb.concept.IConceptDescription;
import edu.kit.aifb.concept.IConceptExtractor;
import edu.kit.aifb.concept.IConceptIndex;
import edu.kit.aifb.concept.IConceptIterator;
import edu.kit.aifb.concept.IConceptVector;
import edu.kit.aifb.concept.IConceptVectorMapper;
import edu.kit.aifb.concept.scorer.CosineScorer;
import edu.kit.aifb.document.TextDocument;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.wikipedia.sql.ILanglinksApi;
import edu.kit.aifb.wikipedia.sql.LanglinksConceptVectorMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class CLCompare {

    static Log logger = LogFactory.getLog( CLCompare.class );

    public static void main(String[] args) {
        String lang_key_from = "en";
        String text_from = "I ride my bike";

        String lang_key_to = "de";
        String text_to = "Ich fahre mein Fahrrad";

        Language lang_from = Language.getLanguage(lang_key_from);
        Language lang_to = Language.getLanguage(lang_key_to);

        ApplicationContext context = new FileSystemXmlApplicationContext("config/evaluation_context.xml");

        IConceptIndex conceptIndexFrom = (IConceptIndex) context.getBean("concept_index_" + lang_key_from);
        IConceptIndex conceptIndexTo = (IConceptIndex) context.getBean("concept_index_" + lang_key_to);

        IConceptExtractor conceptExtractorFrom = conceptIndexFrom.getConceptExtractor();
        IConceptExtractor conceptExtractorTo = conceptIndexTo.getConceptExtractor();

        TextDocument docFrom = new TextDocument("text_from");
        docFrom.setText("content", lang_from, text_from);

        TextDocument docTo = new TextDocument("text_to");
        docTo.setText("content", lang_to, text_to);

        logger.info("Extracting concepts");
        IConceptVector vectorFrom = conceptExtractorFrom.extract(docFrom);
        IConceptVector vectorTo = conceptExtractorTo.extract(docTo);

        System.out.println(vectorFrom.size());
        System.out.println(vectorTo.size());

        ILanglinksApi langlinksApi = (ILanglinksApi) context.getBean(String.format("langlinks_api_%s_%s", lang_key_from, lang_key_to));

        logger.info("Initializing concept mapper");
        long start = System.currentTimeMillis();
        IConceptVectorMapper conceptVectorMapper = new LanglinksConceptVectorMapper(null,
                conceptIndexFrom, lang_from,
                conceptIndexTo, lang_to,
                langlinksApi);
        System.out.println("Took " + (System.currentTimeMillis() - start) / 1000 + "s");

        logger.info("Mapping vector");
        IConceptVector vectorFromMapped = conceptVectorMapper.map(vectorFrom);

        logger.info("Resulting vectors:");
        printVector(context, vectorFrom, conceptIndexFrom);
        System.out.println();
        printVector(context, vectorTo, conceptIndexTo);
        System.out.println();
        printVector(context, vectorFromMapped, conceptIndexTo);

        ConceptVectorSimilarity vectorSimilarity = new ConceptVectorSimilarity(new CosineScorer());

        double similarity = vectorSimilarity.calcSimilarity(vectorFromMapped, vectorTo);

        System.out.println("ESA similarity: " + similarity);
    }

    private static void printVector(ApplicationContext context, IConceptVector vector, IConceptIndex index) {
        IConceptDescription description = (IConceptDescription) context.getBean("concept_description_" + index.getLanguage().toString());

        IConceptIterator iterator = vector.orderedIterator();

        while (iterator.next()) {
            String specificDescription;
            try {
                specificDescription = description.getDescription(index.getConceptName(iterator.getId()), index.getLanguage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            System.out.println(iterator.getValue() + "\t" + specificDescription
                    + " (" + index.getConceptName( iterator.getId() ) + ")");
        }
    }
}
