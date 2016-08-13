package org.mos91.nlptools.writer;

/**
 *
 * This class is not thread-safe.
 *
 * @author OMeleshin.
 * @version 03.07.2016
 */
public class ConllFormatBuilder {

  private final StringBuilder formatBuilder = new StringBuilder();

  private int fieldCount = 0;

  public ConllFormatBuilder withForm() {
    appendField(ConllFieldWriter.FORM_FIELD_NAME);

    return this;
  }

  public ConllFormatBuilder withPlemma() {
    appendField(ConllFieldWriter.PLEMMA_FIELD_NAME);

    return this;
  }

  public ConllFormatBuilder withPos() {
    appendField(ConllFieldWriter.POS_FIELD_NAME);

    return this;
  }

  public ConllFormatBuilder withPPos() {
    appendField(ConllFieldWriter.PPOS_FIELD_NAME);

    return this;
  }

  public ConllFormatBuilder withFeat() {
    appendField(ConllFieldWriter.FEAT_FIELD_NAME);

    return this;
  }

  public ConllFormatBuilder withPFeat() {
    appendField(ConllFieldWriter.PFEAT_FIELD_NAME);

    return this;
  }

  public ConllFormatBuilder withHead() {
    appendField(ConllFieldWriter.HEAD_FIELD_NAME);

    return this;
  }

  public ConllFormatBuilder withPHead() {
    appendField(ConllFieldWriter.PHEAD_FIELD_NAME);

    return this;
  }

  public ConllFormatBuilder withDeprel() {
    appendField(ConllFieldWriter.DEPREL_FIELD_NAME);

    return this;
  }

  public ConllFormatBuilder withPDeprel() {
    appendField(ConllFieldWriter.PDEPREL_FIELD_NAME);

    return this;
  }

  public ConllFormatBuilder withFillPred() {
    appendField(ConllFieldWriter.FILLPRED_FIELD_NAME);

    return this;
  }

  public ConllFormatBuilder withPred() {
    appendField(ConllFieldWriter.PRED_FIELD_NAME);

    return this;
  }

  public ConllFormatBuilder withApreds() {
    appendField(ConllFieldWriter.APREDS_FIELD_NAME);

    return this;
  }

  private void appendField(String name) {
    if (fieldCount != 0) {
      formatBuilder.append(",");
    }

    formatBuilder.append("${");
    formatBuilder.append(name);
    formatBuilder.append("}");
    fieldCount++;
  }

  public String build() {
    return formatBuilder.toString();
  }
}
