package redbayesiana.modelo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.inference.variableElimination.VariableElimination;
import org.openmarkov.io.probmodel.PGMXReader;

public class RedBayesianaGrado {

    final private String bayesNetworkName = "BN-two-diseases.pgmx";
    final public static List<ProbNode> nodos = new ArrayList<>();
    final public static List<String> utility = new ArrayList<>();
    final public static List<String> rfinal = new ArrayList<>();

    public RedBayesianaGrado() {
    }

    public void ObtenerDatos() {
        try {

            InputStream file = new FileInputStream(new File("C:/Users/Ronald/Documents/NetBeansProjects"
                    + "/RedesBayesianas/src/Archivos/grado.pgmx"));
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet = pgmxReader.loadProbNet(file, bayesNetworkName).getProbNet();

            List<ProbNode> listPro = probNet.getProbNodes();
            for (int i = 0; i < listPro.size(); i++) {
                nodos.add(listPro.get(i));

            }
            EvidenceCase evidence = new EvidenceCase();

            evidence.addFinding(probNet, "Grado", "si");

            InferenceAlgorithm variableElimination = new VariableElimination(probNet);

            variableElimination.setPreResolutionEvidence(evidence);

            Variable caso1 = probNet.getVariable("Grado");

            ArrayList<Variable> variablesOfInterest = new ArrayList<Variable>();
            variablesOfInterest.add(caso1);

            HashMap<Variable, TablePotential> posteriorProbabilities
                    = variableElimination.getProbsAndUtilities();

            printResults(evidence, variablesOfInterest, posteriorProbabilities);

            evidence.addFinding(probNet, "Grado", "si");
            posteriorProbabilities = variableElimination.getProbsAndUtilities(variablesOfInterest);
            printResults(evidence, variablesOfInterest, posteriorProbabilities);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> obtenerResultadosUtility() {
        try {
            utility.clear();
            InputStream file = new FileInputStream(new File("C:/Users/Ronald/Documents/NetBeansProjects/"
                    + "RedesBayesianas/src/Archivos/grado.pgmx"));
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet = pgmxReader.loadProbNet(file, bayesNetworkName).getProbNet();
            List<ProbNode> listPro = probNet.getProbNodes();
            listPro.stream().forEach((probNode) -> {
                try {
                    utility.add("\n" + probNode.getUtilityFunction());
                } catch (NonProjectablePotentialException | WrongCriterionException ex) {
                    Logger.getLogger(RedBayesianaGrado.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

        } catch (FileNotFoundException | ParserException e) {
            System.err.println(e.getMessage());
        }
        return utility;

    }

    /**
     * Print the posterior probabilities of the variables of interest on the
     * standard output
     *
     * @param evidence. <code>EvidenceCase</code> The set of observed findings
     * @param variablesOfInterest. <code>ArrayList</code> of
     * <code>Variable</code> The variables whoseposterior probability we are
     * interested in
     * @param posteriorProbabilities. <code>HashMap</code>. Each
     * <code>Variable</code> is mapped onto a <code>TablePotential</code>
     * containing its posterior probability
     */
    public void printResults(EvidenceCase evidence, ArrayList<Variable> variablesOfInterest,
            HashMap<Variable, TablePotential> posteriorProbabilities) {

  //      System.out.println("Evidence:");
        for (Finding finding : evidence.getFindings()) {
            rfinal.add("" + finding.getVariable());
            rfinal.add("" + finding.getState());

        }
     //   System.out.println("Posterior probabilities: ");
        for (Variable variable : variablesOfInterest) {
            double value;
            TablePotential posteriorProbabilitiesPotential = posteriorProbabilities.get(variable);
            System.out.print("  " + variable + ": ");
            int stateIndex = -1;
            try {
                stateIndex = variable.getStateIndex("si");
                value = posteriorProbabilitiesPotential.values[stateIndex];
                rfinal.add(Util.roundedString(value, "0.001"));
            } catch (InvalidStateException e) {
                System.err.println("State \"present\" not found for variable \""
                        + variable.getName() + "\".");
                e.printStackTrace();
            }
        }
        System.out.println();
    }
}
