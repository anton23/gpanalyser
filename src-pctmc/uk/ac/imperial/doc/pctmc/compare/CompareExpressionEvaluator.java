package uk.ac.imperial.doc.pctmc.compare;

import uk.ac.imperial.doc.jexpressions.javaoutput.statements.AbstractExpressionEvaluator;

public class CompareExpressionEvaluator extends AbstractExpressionEvaluator{
        
        private AbstractExpressionEvaluator evaluator1,evaluator2;
        
        public AbstractExpressionEvaluator getEvaluator1() {
                return evaluator1;
        }

        public AbstractExpressionEvaluator getEvaluator2() {
                return evaluator2;
        }

        public CompareExpressionEvaluator(AbstractExpressionEvaluator evaluator1,
                        AbstractExpressionEvaluator evaluator2) {
                super();
                this.evaluator1 = evaluator1;
                this.evaluator2 = evaluator2;
        }

        @Override
        public void setRates(double[] r) {
                super.setRates(r);
        }

        @Override
        public int getNumberOfExpressions() {
                return 0;
        }

        @Override
        public double[] update(double[] values, double t) {
                return null;
        }
}
