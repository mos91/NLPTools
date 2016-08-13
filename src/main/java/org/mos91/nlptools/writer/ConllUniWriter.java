package org.mos91.nlptools.writer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

/**
 * @author OMeleshin.
 * @version 03.07.2016
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
  "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
  "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
  "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures",
  "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
  "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
  "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency",
  "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate",
  "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument"})
public class ConllUniWriter extends JCasFileWriter_ImplBase {

  private static final String UNUSED = "_";
  private static final int UNUSED_INT = -1;

  public static final String DEFAULT_CONLL2009_ROW_FORMAT = "${form}, ${lemma}, ${plemma}, ${pos}, ${ppos}, "
    + "${feat}, ${pfeat}, ${head}, ${phead}, ${deprel}, ${pdeprel}, ${fillpred}, ${pred}, ${apreds}";

  public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
  @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
  private String encoding;

  public static final String PARAM_FILENAME_SUFFIX = "filenameSuffix";
  @ConfigurationParameter(name = PARAM_FILENAME_SUFFIX, mandatory = true, defaultValue = ".conll")
  private String filenameSuffix;

  public static final String PARAM_INCLUDE_HEADER = "includeHeader";
  @ConfigurationParameter(name = PARAM_INCLUDE_HEADER, mandatory = true, defaultValue = "")
  private boolean includeHeader;

  public static final String PARAM_ROW_FORMAT = "rowFormat";
  @ConfigurationParameter(name = PARAM_ROW_FORMAT, mandatory = true, defaultValue = DEFAULT_CONLL2009_ROW_FORMAT)
  private String rowFormat;

  public static final String FIELD_SEPARATOR = "fieldSeparator";
  @ConfigurationParameter(name = FIELD_SEPARATOR, mandatory = true, defaultValue = "\t")
  private String fieldSeparator;

  private CFormatInfo cFormatInfo;

  public String defaultConll2009Header;

  private static final Map<String, String> FIELD_SPECIFICATORS = Collections.unmodifiableMap(
    new LinkedHashMap<String, String>() {
    {
      put(ConllFieldWriter.FORM_FIELD_NAME, "%s");
      put(ConllFieldWriter.LEMMA_FIELD_NAME, "%s");
      put(ConllFieldWriter.PLEMMA_FIELD_NAME, "%s");
      put(ConllFieldWriter.POS_FIELD_NAME, "%s");
      put(ConllFieldWriter.PPOS_FIELD_NAME, "%s");
      put(ConllFieldWriter.FEAT_FIELD_NAME, "%s");
      put(ConllFieldWriter.PFEAT_FIELD_NAME, "%s");
      put(ConllFieldWriter.HEAD_FIELD_NAME, "%s");
      put(ConllFieldWriter.PHEAD_FIELD_NAME, "%s");
      put(ConllFieldWriter.DEPREL_FIELD_NAME, "%s");
      put(ConllFieldWriter.FILLPRED_FIELD_NAME, "%s");
      put(ConllFieldWriter.PRED_FIELD_NAME, "%s");
      put(ConllFieldWriter.APREDS_FIELD_NAME, "%s");
    }
  });

  private static final Map<String, Integer> DEFAULT_FIELD_IDX = ImmutableMap.<String, Integer> builder()
      .put(ConllFieldWriter.FORM_FIELD_NAME, 0)
      .put(ConllFieldWriter.LEMMA_FIELD_NAME, 1)
      .put(ConllFieldWriter.PLEMMA_FIELD_NAME, 2)
      .put(ConllFieldWriter.POS_FIELD_NAME, 3)
      .put(ConllFieldWriter.PPOS_FIELD_NAME, 4)
      .put(ConllFieldWriter.FEAT_FIELD_NAME, 5)
      .put(ConllFieldWriter.HEAD_FIELD_NAME, 6)
      .put(ConllFieldWriter.PHEAD_FIELD_NAME, 7)
      .put(ConllFieldWriter.DEPREL_FIELD_NAME, 8)
      .put(ConllFieldWriter.FILLPRED_FIELD_NAME, 9)
      .put(ConllFieldWriter.PRED_FIELD_NAME, 10)
      .put(ConllFieldWriter.APREDS_FIELD_NAME, 11).build();

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    if (rowFormat == null || rowFormat.isEmpty()) {
      rowFormat = DEFAULT_CONLL2009_ROW_FORMAT;
    }

    if (DEFAULT_CONLL2009_ROW_FORMAT.equals(rowFormat)) {
      cFormatInfo = new CFormatInfo();
      cFormatInfo.formatValue =
        FIELD_SPECIFICATORS.values().stream().collect(Collectors.joining("\t")).concat("\n");
      cFormatInfo.fieldIdxs = DEFAULT_FIELD_IDX;

      Set<String> headerFields = Sets.newLinkedHashSet();
      headerFields.add("ID");
      headerFields.addAll(FIELD_SPECIFICATORS.keySet());
      defaultConll2009Header =
        headerFields.stream().map(header -> header.toUpperCase()).collect(Collectors.joining(fieldSeparator));
      cFormatInfo.headerValue = defaultConll2009Header + "\n";
    } else {
      try {
        cFormatInfo = new CFormatCompiler().call();
      } catch (Exception e) {
        throw new ResourceInitializationException(e);
      }
    }
  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    PrintWriter out = null;
    try {
      out = new PrintWriter(new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix),
        encoding));
      convert(aJCas, out);
    }
    catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
    finally {
      closeQuietly(out);
    }
  }

  private void convert(JCas jCas, PrintWriter out) {
    Map<Token, Collection<SemanticPredicate>> predIdx = indexCovered(jCas, Token.class,
      SemanticPredicate.class);
    Map<SemanticArgument, Collection<Token>> argIdx = indexCovered(jCas,
      SemanticArgument.class, Token.class);

    ConllFieldWriter conllFieldWriter = new ConllFieldWriter(cFormatInfo.formatValue, cFormatInfo.fieldIdxs);
    conllFieldWriter.setFieldSeparator(fieldSeparator);

    select(jCas, Sentence.class).stream().forEachOrdered(sentence -> {
      HashMap<Token, Row> ctokens = new LinkedHashMap<>();

      // Tokens
      List<Token> tokens = selectCovered(Token.class, sentence);

      // Check if we should try to include the FEATS in output
      List<MorphologicalFeatures> morphology = selectCovered(MorphologicalFeatures.class, sentence);
      boolean useFeats = tokens.size() == morphology.size();

      List<SemanticPredicate> preds = selectCovered(SemanticPredicate.class, sentence);

      for (int i = 0; i < tokens.size(); i++) {
        Row row = new Row();
        row.id = i+1;
        row.token = tokens.get(i);
        row.args = new SemanticArgument[preds.size()];
        if (useFeats) {
          row.feats = morphology.get(i);
        }

        // If there are multiple semantic predicates for the current token, then
        // we keep only the first
        Collection<SemanticPredicate> predsForToken = predIdx.get(row.token);
        if (predsForToken != null && !predsForToken.isEmpty()) {
          row.pred = predsForToken.iterator().next();
        }
        ctokens.put(row.token, row);
      }

      // Dependencies
      selectCovered(Dependency.class, sentence).stream().forEachOrdered(rel -> ctokens.get(rel.getDependent()).deprel = rel);

      // Semantic arguments
      for (int p = 0; p < preds.size(); p++) {
        FSArray args = preds.get(p).getArguments();
        for (SemanticArgument arg : select(args, SemanticArgument.class)) {
          for (Token t : argIdx.get(arg)) {
            Row row = ctokens.get(t);
            row.args[p] = arg;
          }
        }
      }

      out.print(cFormatInfo.headerValue);
      // Write sentence in CONLL 2009 format
      ctokens.values().stream().forEachOrdered(row -> {
        int id = row.id;
        conllFieldWriter.putId(id);

        String form;
        if (conllFieldWriter.hasForm()) {
          form = row.token.getCoveredText();
          conllFieldWriter.putForm(form);
        }

        String lemma = UNUSED;
        if ((row.token.getLemma() != null)) {
          lemma = row.token.getLemma().getValue();
        }
        conllFieldWriter.putLemma(lemma);
        conllFieldWriter.putPlemma(lemma);

        String pos = UNUSED;
        if ((row.token.getPos() != null)) {
          POS posAnno = row.token.getPos();
          pos = posAnno.getPosValue();
        }
        conllFieldWriter.putPos(pos);
        conllFieldWriter.putPPos(pos);

        String feat = UNUSED;
        if ((row.feats != null)) {
          feat = row.feats.getValue();
        }
        conllFieldWriter.putFeat(feat);
        conllFieldWriter.putPFeat(feat);

        int headId = UNUSED_INT;
        String deprel = UNUSED;
        if ((row.deprel != null)) {
          deprel = row.deprel.getDependencyType();
          headId = ctokens.get(row.deprel.getGovernor()).id;
          if (headId == row.id) {
            // ROOT dependencies may be modeled as a loop, ignore these.
            headId = 0;
          }
        }
        conllFieldWriter.putDeprel(deprel);

        String head = UNUSED;
        if (headId != UNUSED_INT) {
          head = Integer.toString(headId);
        }

        conllFieldWriter.putHead(head);
        conllFieldWriter.putPHead(head);
        conllFieldWriter.putPDeprel(deprel);

        String fillpred = UNUSED;
        String pred = UNUSED;
        StringBuilder apreds = new StringBuilder();

        if (row.pred != null) {
          fillpred = "Y";
          pred = row.pred.getCategory();
        }

        conllFieldWriter.putFillpred(fillpred);
        conllFieldWriter.putPred(pred);

        for (SemanticArgument arg : row.args) {
          if (apreds.length() > 0) {
            apreds.append('\t');
          }
          apreds.append(arg != null ? arg.getRole() : UNUSED);
        }
        conllFieldWriter.putApreds(apreds.toString());

        conllFieldWriter.print(out);
      });

      out.println();
    });
  }

  private class CFormatInfo {

    private String formatValue;

    private String headerValue;

    private Map<String, Integer> fieldIdxs;
  }

  private class CFormatCompiler implements Callable<CFormatInfo> {

    private final StringBuilder cformatBuilder = new StringBuilder();

    private final StringBuilder headerBuilder = new StringBuilder();

    private int fieldCount;

    @Override
    public CFormatInfo call() throws Exception {
      Map<String, Integer> fieldIdxs = new HashMap<String, Integer>();
      headerBuilder.append("ID\t");
      Arrays.stream(rowFormat.split(",")).forEachOrdered((String t) ->{
        String fieldName = t.trim();
        fieldName = fieldName.substring(2, fieldName.indexOf('}'));

        String cformatSpec = FIELD_SPECIFICATORS.get(fieldName);

        if (cformatSpec != null) {
          if (fieldCount != 0) {
            cformatBuilder.append(fieldSeparator);
            headerBuilder.append(fieldSeparator);
          }
          cformatBuilder.append(cformatSpec);
          headerBuilder.append(fieldName.toUpperCase());
          fieldIdxs.put(fieldName, fieldCount++);
        }
      });
      headerBuilder.append("\n");

      CFormatInfo formatInfo = new CFormatInfo();
      formatInfo.formatValue = cformatBuilder.toString();
      formatInfo.headerValue = headerBuilder.toString();
      formatInfo.fieldIdxs = fieldIdxs;
      return formatInfo;
    }
  }

  private static final class Row {
    int id;
    Token token;
    MorphologicalFeatures feats;
    Dependency deprel;
    SemanticPredicate pred;
    SemanticArgument[] args;
  }
}
