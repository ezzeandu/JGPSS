/**
 * Software end-user license agreement.
 *
 * The LICENSE.TXT containing the license is located in the JGPSS project.
 * License.txt can be downloaded here:
 * href="http://www-eio.upc.es/~Pau/index.php?q=node/28
 *
 * NOTICE TO THE USER: BY COPYING, INSTALLING OR USING THIS SOFTWARE OR PART OF
 * THIS SOFTWARE, YOU AGREE TO THE   TERMS AND CONDITIONS OF THE LICENSE AGREEMENT
 * AS IF IT WERE A WRITTEN AGREEMENT NEGOTIATED AND SIGNED BY YOU. THE LICENSE
 * AGREEMENT IS ENFORCEABLE AGAINST YOU AND ANY OTHER LEGAL PERSON ACTING ON YOUR
 * BEHALF.
 * IF, AFTER READING THE TERMS AND CONDITIONS HEREIN, YOU DO NOT AGREE TO THEM,
 * YOU MAY NOT INSTALL THIS SOFTWARE ON YOUR COMPUTER.
 * UPC IS THE OWNER OF ALL THE INTELLECTUAL PROPERTY OF THE SOFTWARE AND ONLY
 * AUTHORIZES YOU TO USE THE SOFTWARE IN ACCORDANCE WITH THE TERMS SET OUT IN
 * THE LICENSE AGREEMENT.
 */
package model;

import model.entities.QueueReport;
import model.entities.Storage;
import model.entities.Xact;
import model.blocks.Generate;
import model.blocks.Bloc;
import model.blocks.Facility;
import java.io.Serializable;
import java.util.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import model.entities.AmperVariable;
import model.entities.SaveValue;
import utils.Constants;

/**
 * A class representing the model elements.
 *
 * @author Pau Fonseca i Casas
 * @author Ezequiel Andujar Montes
 * @version 1
 * @see     <a href="http://www-eio.upc.es/~Pau/index.php?q=node/28">Project
 * website</a>
 * @serialData
 */
@Data
@SuppressWarnings("FieldMayBeFinal")
public final class Model implements Serializable {

    static final long serialVersionUID = 42L;

    @Getter @Setter
    private String name;
    @Getter @Setter
    private String description;
    private ArrayList<Proces> proces;
    private ArrayList<Storage> storages;
    private ArrayList<SaveValue> saveValues;
    @Getter @Setter
    private ArrayList<AmperVariable<?>> amperVariables;
    private ArrayList<Matrix<Float>> matrix;

    /**
     * Current Event Chain
     */
    private PriorityQueue<Xact> CEC;

    /**
     * Future Event Chain
     */
    private PriorityQueue<Xact> FEC;

    /**
     * Blocked Event Chain
     */
    private HashMap<String, PriorityQueue<Xact>> BEC;

    /**
     * Preempted Xacts
     */
    private HashMap<String, PriorityQueue<Xact>> preemptedXacts;

    private HashMap<String, Facility> facilities;
    private HashMap<String, QueueReport> queues;

    /**
     * The transaction counter.
     */
    private int TC;
    /**
     * Identifier for the XACT created until the moment.
     */

    private int idxact;
    /**
     * Absolute simulation clock.
     */

    private float absoluteClock;
    /**
     * Relative simulation clock.
     */

    private float relativeClock;

    public Model() {

        proces = new ArrayList<>();
        storages = new ArrayList<>();
        saveValues = new ArrayList<>();
        matrix = new ArrayList<>();
        amperVariables = new ArrayList<>();

        CEC = new PriorityQueue<>(1000, this.getPriorityComparator());
        FEC = new PriorityQueue<>(1000, this.getTimeComparator());
        BEC = new HashMap<>();

        preemptedXacts = new HashMap<>();
        facilities = new HashMap<>();

        queues = new HashMap<>();
    }

    public Comparator<Xact> getPriorityComparator() {
        return (Xact o1, Xact o2) -> {
            if (o1.getPriority() > o2.getPriority()) {
                return -1;
            } else if (o1.getPriority() == o2.getPriority()) {
                return 0;
            } else {
                return 1;
            }
        };
    }

    public Comparator<Xact> getTimeComparator() {
        return (Xact o1, Xact o2) -> {
            if (o1.getMoveTime() < o2.getMoveTime()) {
                return -1;
            } else if (o1.getMoveTime() == o2.getMoveTime()) {
                return 0;
            } else {
                return 1;
            }
        };
    }

    public void incIdXact() {
        this.idxact++;
    }

    /**
     * Returns the saveValue entity named name
     *
     * @param name
     * @return
     */
    public SaveValue getSaveValue(String name) {

        return saveValues.stream()//
                .filter(sv -> sv.getName().equals(name))//
                .findFirst()//
                .orElse(null);
    }

    /**
     * Returns true if the relatedXact is currently preempted at any facility
     *
     * @param relatedXact
     * @return
     */
    public boolean preempted(Xact relatedXact) {

        while (preemptedXacts.entrySet().iterator().hasNext()) {

            Map.Entry<String, PriorityQueue<Xact>> pair;
            pair = preemptedXacts.entrySet().iterator().next();
            PriorityQueue<Xact> preempted = pair.getValue();

            if (preempted.contains(relatedXact)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the maximum capacity storage with titol name
     *
     * @param name the name of the storage
     * @return a Storage instance
     */
    public int getStorageMaxCapacity(String name) {
        Storage st;
        for (int i = 0; i < storages.size(); i++) {
            st = storages.get(i);
            if (st.getNom().equals(name)) {
                return storages.get(i).getValor();
            }
        }
        return 1;
    }

    /**
     * Evaluates the Expression A associated with a String, a Number or an SNA
     *
     * @param A
     * @param tr
     * @return
     */
    public String evaluateExpression(String A, Xact tr) {

        String _A = "";

        // Value of the relative Clock
        if (A.equals("C1")) {
            _A = String.valueOf(relativeClock);
        } // The Xact Assembly Set
        else if (A.equals("A1")) {
            _A = String.valueOf(tr.getAssemblySet());
        } // Value of the absolute Clock
        else if (A.equals("AC1")) {
            _A = String.valueOf(absoluteClock);
        } // Remaining termination count
        else if (A.equals("TG1")) {
            _A = String.valueOf(TC);
        } // Active Transaction number
        else if (A.equals("XN1")) {
            _A = String.valueOf(tr.getID());
        } // Transit time returns the absolute system clock minus the "Mark Time" of the Transaction
        else if (A.startsWith("M1")) {
            return String.valueOf(absoluteClock - tr.getMoveTime());
        } // Transactions priority
        else if (A.startsWith("PR")) {
            return String.valueOf(tr.getPriority());
        } // Block entry count
        else if (A.startsWith("N$")) {
            _A = String.valueOf(findBloc(tr.getBloc().getLabel()).getEntryCount());
        } // Transaction parameter
        else if (A.startsWith("P$")) {
            String parameterName = A.split("P$")[1];
            _A = String.valueOf(tr.getParameter(parameterName));
        } // Facility Bussy
        else if (A.startsWith("F$")) {
            String facilityName = A.split("F$")[1];
            boolean available = facilities.get(facilityName).isAvailable();
            _A = available ? "1" : "0";
        } // Facility Capture Count
        else if (A.startsWith("FC$")) {
            String facilityName = A.split("F$")[1];
            _A = String.valueOf(facilities.get(facilityName).getCaptureCount());
        } // Facility Capture Count
        else if (A.startsWith("FN$")) {
            String facilityName = A.split("F$")[1];
            // Not implemented yet
        } // Transit Time. Current absolute system clock value minus value in Parameter Parameter
        else if (A.startsWith("MP$")) {
            float parameter = Float.parseFloat(A.split("MP$")[1]);
            _A = String.valueOf(absoluteClock - parameter);
        } // Current count value of the queue
        else if (A.startsWith("Q$")) {
            String queue = A.split("Q$")[1];
            _A = String.valueOf(queues.get(queue).getCurrentCount());
        } // Average queue content 
        else if (A.startsWith("QA$")) {
            String queue = A.split("QA$")[1];
            _A = String.valueOf(queues.get(queue).getAvgContent());
        } // Queue total entries
        else if (A.startsWith("QC$")) {
            String queue = A.split("QC$")[1];
            _A = String.valueOf(queues.get(queue).getTotalEntries());
        } // Queue max length
        else if (A.startsWith("QM$")) {
            String queue = A.split("QM$")[1];
            _A = String.valueOf(queues.get(queue).getMaxCount());
        } // Average Queue residence time
        else if (A.startsWith("QT$")) {
            String queue = A.split("QT$")[1];
            _A = String.valueOf(queues.get(queue).getAvgTime());
        } // Average Queue residence time excluding zero entries
        else if (A.startsWith("QX$")) {
            String queue = A.split("QX$")[1];
            _A = String.valueOf(queues.get(queue).getAvgTime(true));
        } // Queue zero entry count
        else if (A.startsWith("QZ$")) {
            String queue = A.split("QZ$")[1];
            _A = String.valueOf(queues.get(queue).getZeroEntries());
        } // Available storage capacity
        else if (A.startsWith("R$")) {
            String storage = A.split("R$")[1];
            _A = String.valueOf(facilities.get(storage).getAvailableCapacity());

            // Returns a random value between 0-999
        } else if (A.startsWith("RN")) {
            java.util.Random rnd = new java.util.Random();
            _A = String.valueOf(rnd.nextFloat() * 999);
        } // Float value of the string
        // Storage in use
        else if (A.startsWith("S$")) {

            String storage = A.split("S$")[1];
            _A = String.valueOf(facilities.get(storage).getCaptureCount());

        } // Average storage in use
        else if (A.startsWith("SA$")) {

            String storage = A.split("SA$")[1];
            _A = String.valueOf(facilities.get(storage).avgHoldingTime());

        } // Storage empty
        else if (A.startsWith("SE$")) {
            String storage = A.split("SE$")[1];
            _A = facilities.get(storage).getCapturingTransactions() == 0 ? "1" : "0";
        } // Storage full
        else if (A.startsWith("SM$")) {
            String storage = A.split("SM$")[1];
            _A = !facilities.get(storage).isAvailable() ? "1" : "0";
        } // Value of SaveValue entity
        else if (A.startsWith("X$")) {

            _A = saveValues.stream()//
                    .filter(sv -> sv.getName().equals(A.split("X$")[1]))//
                    .map(sv -> String.valueOf(sv.getValue()))//
                    .findFirst()//
                    .orElse("0.0");

        } else {
            _A = A;
        }
        return _A;
    }

    /**
     * To initialize the GENERATE block. This method can be used as a template
     * for other initialization procedures.
     *
     * @throws java.lang.Exception
     */
    public void InitializeGenerateBocs() throws Exception {

        for (int j = 0; j < proces.size(); j++) {
            Proces p = proces.get(j);
            for (int k = 0; k < p.getBlocs().size(); k++) {
                Bloc b = p.getBlocs().get(k);
                if (b.getId() == Constants.idGenerate) {
                    ((Generate) b).execute(null);
                }
            }
        }
    }

    public int blocIndex(String bloc, Proces proces) {

        for (int k = 0; k < proces.getBlocs().size(); k++) {
            Bloc b = proces.getBlocs().get(k);
            if (b.getLabel().equals(bloc)) {
                return k;
            }
        }
        return -1;
    }

    /**
     * Returns the Bloc with the label label
     *
     * @param label
     * @return
     */
    public Bloc findBloc(String label) {

        for (Proces p : proces) {
            Bloc b = p.findBloc(label);
            if (b != null) {
                return b;
            }
        }
        return null;
    }

    public Storage getStorage(String sn) {

        return storages.stream()//
                .filter(s -> s.getNom().equals(sn))//
                .findFirst()//
                .orElse(null);
    }

    /**
     * To execute the simulation model.
     *
     * @param b if true we execute the simulation step by step.
     * @throws java.lang.Exception
     */
    public void execute(boolean b) throws Exception {
        relativeClock = 0;
        absoluteClock = 0;
        InitializeGenerateBocs();
        if (!b) {
            executeAll();
        } else {
            executeStep();
        }
    }

    /**
     * To execute the simulation model.
     *
     * @throws java.lang.Exception
     */
    public void executeAll() throws Exception {
        Xact xact;
        //Simulation engine loop.

        while (TC > 0) {

            scanPhase();
            clockUpdatedPhase();
            updateBEC();

        }
        updateCurrentCount();
        System.out.println("Simulation terminated");
    }

    /**
     * To execute a single step of the simulation model. Executes untin a new
     * CLOCK UPDATE PHASE.
     *
     * @throws java.lang.Exception
     */
    public void executeStep() throws Exception {
        Xact xact;
        //Motor central de simulaci�.
        if (TC > 0) {
            scanPhase();
            clockUpdatedPhase();
            updateBEC();
        }
        updateCurrentCount();
        System.out.println("Simulation terminated");
    }

    private void scanPhase() throws Exception {
        Xact xact = CEC.poll();
        while (xact != null) {
            Bloc b = xact.getBloc();
            do {
                b = b.execute(xact);
            } while (b != null);
            xact = CEC.poll();
        }
    }

    private void clockUpdatedPhase() {

        Xact xact = FEC.poll();
        if (xact != null) {

            relativeClock = xact.getMoveTime();

            do {
                CEC.add(xact);
                xact = FEC.poll();

            } while (xact != null && xact.getMoveTime() == relativeClock);

            if (xact != null) {
                FEC.add(xact);
            }
        }
    }

    private void updateBEC() {
        BEC.forEach((name, bloquedXacts) -> {

            Xact xactB = bloquedXacts.poll();

            if (xactB != null) {
                do {

                    if (xactB.restoreToFEC()) {
                        FEC.add(xactB);
                    } else {
                        CEC.add(xactB);
                    }
                    xactB = bloquedXacts.poll();

                } while (xactB != null);
            }
        });
    }  

    /**
     * Updates the current count of the Blocs that has xact left
     */
    private void updateCurrentCount() {

        CEC.stream().forEach(xact -> {
            xact.getBloc().incCurrentCount();
        });

        FEC.stream().forEach(xact -> {
            xact.getBloc().incCurrentCount();
        });

        BEC.forEach((name, bloquedXacts) -> {
            bloquedXacts.forEach(xact -> {
                xact.getBloc().incCurrentCount();
            });
        });
    }
}
