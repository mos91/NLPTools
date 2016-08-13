package org.mos91.nlptools;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.mos91.nlptools.writer.ConllUniWriter;

public class Pipeline {

  public static void main(String[] args) throws Exception {
    CollectionReaderDescription reader = createReaderDescription(
      TextReader.class,
      TextReader.PARAM_SOURCE_LOCATION, "src/test/resources",
      TextReader.PARAM_PATTERNS, "*.txt",
      TextReader.PARAM_LANGUAGE, "en");

    AnalysisEngineDescription segmenter = createEngineDescription(StanfordSegmenter.class);
    AnalysisEngineDescription parser = createEngineDescription(StanfordParser.class);
    AnalysisEngineDescription writer = createEngineDescription(ConllUniWriter.class,
      ConllUniWriter.FIELD_SEPARATOR, "|",
      ConllUniWriter.PARAM_TARGET_LOCATION, ".");

    runPipeline(
      reader, segmenter,
      createEngineDescription(OpenNlpPosTagger.class),
      createEngineDescription(LanguageToolLemmatizer.class),
      parser, writer);

  }

}
