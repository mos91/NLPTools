package org.mos91.nlptools.writer;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ConllFieldWriter {

  public static final String FORM_FIELD_NAME = "form";

  public static final String LEMMA_FIELD_NAME = "lemma";

  public static final String PLEMMA_FIELD_NAME = "plemma";

  public static final String POS_FIELD_NAME = "pos";

  public static final String PPOS_FIELD_NAME = "ppos";

  public static final String FEAT_FIELD_NAME = "feat";

  public static final String PFEAT_FIELD_NAME = "pfeat";

  public static final String HEAD_FIELD_NAME = "head";

  public static final String PHEAD_FIELD_NAME = "phead";

  public static final String DEPREL_FIELD_NAME = "deprel";

  public static final String PDEPREL_FIELD_NAME = "pdeprel";

  public static final String FILLPRED_FIELD_NAME = "fillpred";

  public static final String PRED_FIELD_NAME = "pred";

  public static final String APREDS_FIELD_NAME = "apreds";

  private int id;

  private int fieldCount = 0;

  private String format;

  private String[] strings;

  private String fieldSeparator = "\t";

  private Map<String, Integer> fieldIdxs = new HashMap();

  public ConllFieldWriter(String format, Map<String, Integer> fieldIdxs) {
    this.format = format;
    initIdxs(fieldIdxs);
  }

  private void initIdxs(Map<String, Integer> fieldIdxs) {
    this.fieldIdxs = fieldIdxs;
    fieldCount = fieldIdxs.keySet().size();
    strings = new String[fieldCount];
  }

  public void setFieldSeparator(String fieldSeparator) {
    this.fieldSeparator = fieldSeparator;
  }

  public ConllFieldWriter putId(int id) {
    this.id = id;

    return this;
  }

  public ConllFieldWriter putForm(String form) {
    return putField(FORM_FIELD_NAME, form);
  }

  public boolean hasForm() {
    return fieldIdxs.containsKey(FORM_FIELD_NAME);
  }

  public ConllFieldWriter putLemma(String lemma) {
    return putField(LEMMA_FIELD_NAME, lemma);
  }

  public boolean hasLemma() {
    return fieldIdxs.containsKey(LEMMA_FIELD_NAME);
  }

  public ConllFieldWriter putPlemma(String plemma) {
    return putField(PLEMMA_FIELD_NAME, plemma);
  }

  public boolean hasPLemma() {
    return fieldIdxs.containsKey(PLEMMA_FIELD_NAME);
  }

  public ConllFieldWriter putPos(String pos) {
    return putField(POS_FIELD_NAME, pos);
  }

  public boolean hasPos() {
    return fieldIdxs.containsKey(POS_FIELD_NAME);
  }

  public ConllFieldWriter putPPos(String ppos) {
    return putField(PPOS_FIELD_NAME, ppos);
  }

  public boolean hasPPos() {
    return fieldIdxs.containsKey(PPOS_FIELD_NAME);
  }

  public ConllFieldWriter putFeat(String feat) {
    return putField(FEAT_FIELD_NAME, feat);
  }

  public boolean hasFeat() {
    return fieldIdxs.containsKey(FEAT_FIELD_NAME);
  }

  public ConllFieldWriter putPFeat(String pfeat) {
    return putField(PFEAT_FIELD_NAME, pfeat);
  }

  public boolean hasPFeat() {
    return fieldIdxs.containsKey(PFEAT_FIELD_NAME);
  }

  public ConllFieldWriter putHead(String head) {
    return putField(HEAD_FIELD_NAME, head);
  }

  public boolean hasHead() {
    return fieldIdxs.containsKey(HEAD_FIELD_NAME);
  }

  public ConllFieldWriter putPHead(String phead) {
    return putField(PHEAD_FIELD_NAME, phead);
  }

  public boolean hasPHead() {
    return fieldIdxs.containsKey(PHEAD_FIELD_NAME);
  }

  public ConllFieldWriter putDeprel(String deprel) {
    return putField(DEPREL_FIELD_NAME, deprel);
  }

  public boolean hasDeprel() {
    return fieldIdxs.containsKey(DEPREL_FIELD_NAME);
  }

  public ConllFieldWriter putPDeprel(String pdeprel) {
    return putField(PDEPREL_FIELD_NAME, pdeprel);
  }

  public boolean hasPDeprel() {
    return fieldIdxs.containsKey(PDEPREL_FIELD_NAME);
  }

  public ConllFieldWriter putFillpred(String fillPred) {
    return putField(FILLPRED_FIELD_NAME, fillPred);
  }

  public boolean hasFillpred() {
    return fieldIdxs.containsKey(FILLPRED_FIELD_NAME);
  }

  public ConllFieldWriter putPred(String pred) {
    return putField(PRED_FIELD_NAME, pred);
  }

  public boolean hasPred() {
    return fieldIdxs.containsKey(PRED_FIELD_NAME);
  }

  public ConllFieldWriter putApreds(String apreds) {
    return putField(APREDS_FIELD_NAME, apreds);
  }

  public boolean hasApreds() {
    return fieldIdxs.containsKey(APREDS_FIELD_NAME);
  }

  public ConllFieldWriter putField(String fieldName, String fieldValue) {
    if (fieldIdxs.containsKey(fieldName)) {
      strings[fieldIdxs.get(fieldName)] = fieldValue;
    }

    return this;
  }

  public void print(PrintWriter out) {
    out.printf("%d\t%s\n", id, Arrays.stream(strings).collect(Collectors.joining(fieldSeparator)));
    for (int i = 0;i < strings.length;i++) {
      strings[i] = null;
    }
  }

}
