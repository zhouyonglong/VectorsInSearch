package org.dice.solrenhancements.queryparsers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.payloads.PayloadDecoder;
import org.apache.lucene.queries.payloads.PayloadFunction;
import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.queries.payloads.AveragePayloadFunction;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.ExtendedDismaxQParser;
import org.apache.solr.search.SyntaxError;

import java.util.HashMap;

/**
 * Created by simon.hughes on 3/29/14.
 */

public class PayloadAwareExtendDismaxQParser extends ExtendedDismaxQParser {

    public PayloadAwareExtendDismaxQParser(
            java.lang.String qstr,
            org.apache.solr.common.params.SolrParams localParams,
            org.apache.solr.common.params.SolrParams params,
            org.apache.solr.request.SolrQueryRequest req)
    {
        super(qstr,localParams, params, req);
    }

    @Override
    protected org.apache.solr.search.ExtendedDismaxQParser.ExtendedSolrQueryParser createEdismaxQueryParser(org.apache.solr.search.QParser qParser, java.lang.String field)
    {
        return new PayloadAwareExtendedSolrQueryParser(qParser, field);
    }

    public static class PayloadAwareExtendedSolrQueryParser extends ExtendedDismaxQParser.ExtendedSolrQueryParser {

        public PayloadAwareExtendedSolrQueryParser(org.apache.solr.search.QParser parser, java.lang.String defaultField) {
            super(parser, defaultField);
        }

        @Override
        protected Query getFieldQuery(String field, String queryText, boolean quoted) throws SyntaxError {
            SchemaField sf = this.schema.getFieldOrNull(field);

            //TODO cache this check
            if (sf != null) {

                final String fieldTypeName = sf.getType().getTypeName().toLowerCase();
                if(fieldTypeName.contains("payload") || fieldTypeName.contains("vector")) {
                    // We need includeSpanScore to include the boost value also
                    return new PayloadScoreQuery(new SpanTermQuery(new Term(field, queryText)), new AveragePayloadFunction(), null, true);
                }
            }
            return super.getFieldQuery(field, queryText, quoted);
        }

        @Override
        protected Query newFieldQuery(Analyzer analyzer, String field, String queryText,
                                      boolean quoted, boolean fieldAutoGenPhraseQueries, boolean enableGraphQueries,
                                      SynonymQueryStyle synonymQueryStyle)
                throws SyntaxError {
            Analyzer actualAnalyzer = parser.getReq().getSchema().getFieldType(field).getQueryAnalyzer();
            SchemaField sf = this.schema.getFieldOrNull(field);
            if (sf != null) {

                final String fieldTypeName = sf.getType().getTypeName().toLowerCase();
                if(fieldTypeName.contains("payload") || fieldTypeName.contains("vector")) {
                    // We need includeSpanScore to include the boost value also
                    return new PayloadScoreQuery(new SpanTermQuery(new Term(field, queryText)), new AveragePayloadFunction(), null, true);
                }
            }

            return super.newFieldQuery(actualAnalyzer, field, queryText, quoted, fieldAutoGenPhraseQueries, enableGraphQueries, synonymQueryStyle);
        }
    }

}
