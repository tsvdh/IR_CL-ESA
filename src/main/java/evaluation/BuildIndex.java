package evaluation;

import edu.kit.aifb.document.ICollection;
import edu.kit.aifb.nlp.Language;
import edu.kit.aifb.terrier.MTTerrierIndexFactory;
import edu.kit.aifb.terrier.TerrierIndexFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BuildIndex {

    public static void main(String[] args) throws IOException {
        String lang_key = "de";

        ApplicationContext context = new FileSystemXmlApplicationContext("config/evaluation_context.xml");

        ICollection collection = (ICollection) context.getBean("wikipedia_collection_" + lang_key);
        String indexId = "concept_index";
        Language language = Language.getLanguage(lang_key);

        TerrierIndexFactory factory = context.getBean(TerrierIndexFactory.class);
        factory.buildIndex(indexId, language, collection);

        String fileName = indexId + "_" + lang_key + ".inverted.bf";
        Files.move(Paths.get("index/" + fileName),
                   Paths.get(System.getProperty("terrier.home") + "/var/index/" + fileName));
    }
}
