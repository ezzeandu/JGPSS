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
package model.blocks;

import java.util.PriorityQueue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.entities.Xact;
import utils.Constants;

/**
 * A class representing the FUNAVAIL block.
 *
 * @author Pau Fonseca i Casas
 * @version 1
 * @see     <a href="http://www-eio.upc.es/~Pau/index.php?q=node/28">Project
 * website</a>
 * @serialData
 *
 * FUNAVAIL Blocks are used to make a Facility Entity unavailable for ownership
 * by Transactions
 */
@NoArgsConstructor
@SuppressWarnings("FieldMayBeFinal")
public class Funavail extends Bloc {

    //Intergeneration time.
    @Getter
    @Setter
    private String A;
    //Halfrange or Function Modifier.
    @Getter
    @Setter
    private String B;
    //Start delay time.
    @Getter
    @Setter
    private String C;
    //Creation limit.
    @Getter
    @Setter
    private String D;
    //Priority.
    @Getter
    @Setter
    private String E;
    //Not yet used.
    @Getter
    @Setter
    private String F;

    /**
     * Creates a new instance of Funavail
     *
     * @param comentari the comment of the block.
     * @param label the label of the block.
     * @param A the instalation that become unavailable (SEIZE).
     * @param B modality (RE) remover or (CO) continue.
     * @param C label of the block to send the XACT that are in the instalation.
     * @param D parameter that receives the residual time of the XACT.
     * @param E modality (RE) remover of (CO) continue for the PREEMPT XACT's.
     * @param F label to send the PREEMP XACT's.
     */
    public Funavail(String comentari, String label, String A, String B, String C, String D, String E, String F) {

        super(Constants.idFunavail, label, comentari);

        this.A = A;
        this.B = B;
        this.C = C;
        this.D = D;
        this.E = E;
        this.F = F;
        this.setLabel(label);
        this.setComentari(comentari);
    }

    /**
     * The complexity of the FUNAVAIL Block is due to the three classes of
     * Transactions which must be dealt with:
     *
     * 1. the owning Transaction (operands B-D),
     *
     * 2. preempted Transactions on the Interrupt Chain (operands E-F), and
     *
     * 3. delayed Transactions on the Delay Chain or Pending Chain (operands
     * G-H).
     *
     * A FUNAVAIL Block allows you to put a Facility "out of action" and to
     * control the fate of Transactions waiting for, using, or preempted at the
     * Facility Entity. Transactions arriving during the unavailable period will
     * be delayed and not be granted ownership. A FUNAVAIL Block has no effect
     * if the Facility is already unavailable.
     *
     * When the REmove option is used, the Transactions are removed from
     * contention for the Facility If the REmove option is used for pending and
     * delayed Transactions, i. e. if G is RE, then H must be used to redirect
     * the Transactions. When the COntinue option is used, the Transactions on
     * the specific Facility chain may continue to own the Facility, even during
     * the unavailable period. In this case, Facility utilization statistics are
     * adjusted to include this time.
     *
     * When an alternate destination Block is used, the Transactions are
     * displaced from their current context and redirected to the new Block.
     * Delayed and pending Transactions, which are controlled by operands G and
     * H, cannot be redirected without also using the REmove option.
     *
     * The owning Transaction, controlled by operands B through D, and preempted
     * Transactions, controlled by operands E and F, can remain in contention
     * for the Facility and yet be displaced to a new destination. This is done
     * by specifying an alternate destination without using the corresponding RE
     * option.
     *
     * When RE is not used in Operand B, any owning Transaction becomes
     * preempted at this Facility. Such Transactions cannot leave ASSEMBLE,
     * GATHER, or MATCH Blocks or enter (nonzero) ADVANCE Blocks until all
     * preemptions are cleared.
     *
     * @param tr
     * @return
     * @throws java.lang.Exception
     */
    @Override
    public Bloc execute(Xact tr) throws Exception {

        incTrans(tr);
        String facilityName = getModel().evaluateExpression(A, tr);
        Facility facilityState = getModel().getFacilities().get(facilityName);
        facilityState.setAvailable(false);

        Xact owningXact = facilityState.getOwningXact();

        Bloc destinationB = getProces().findBloc(getModel().evaluateExpression(C, tr));
        Bloc destinationF = getProces().findBloc(getModel().evaluateExpression(F, tr));

        /**
         * The transactions are removed from contention for the facility
         */
        if (B.equals("RE")) {

            if (destinationB != null && destinationB instanceof Release) {
                throw new Exception("In Block FUNAVAIL " + getLabel() + " at Process " + getProces().getDescpro() + "Operand C refers to a Release Block and must not be used with RE option");
            }

            facilityState.setOwningXact(null);

        } /**
         * The owning transaction continues
         */
        else if (B.equals("CO")) {

        } /**
         * The owning Transaction is preempted and placed on the Interrupt Chain
         * of the Facility. If it was taken from the FEC and the C Operand is
         * not used, it will be automatically restored to the FEC using the
         * automatically saved residual time when the Facility again becomes
         * available.
         */
        else if (B.isEmpty()) {

            if (getModel().getPreemptedXacts().get(facilityName) == null) {                
                
                getModel().getPreemptedXacts().put(facilityName, new PriorityQueue<>(1000, getModel().getPriorityComparator()));
            }
            getModel().getPreemptedXacts().get(facilityName).add(tr);
        }
        /**
         * The C Operand may be used regardless of Operand B. It causes the
         * owning Transaction to be displaced, and gives it a new destination
         * Block. If you choose to return the Transaction to the FEC, having
         * used the C Operand, you must explicitly route the Transaction to an
         * ADVANCE Block. The D Operand causes the residual time to be saved in
         * a Parameter of the owning Transaction. The residual time value is
         * then available for explicit rescheduling when you use the Parameter
         * value as Operand A of an ADVANCE Block.
         */
        if (!C.isEmpty() && destinationB != null) {

            if (getModel().getFEC().contains(owningXact)) {
                for (Object b : getProces().getBlocs()) {

                    if (b instanceof Advance) {
                        owningXact.setBloc((Bloc) b);
                        break;
                    }
                }
                owningXact.setBlockRoute(destinationB);
            } else {
                owningXact.setBloc(destinationB);
            }
        } else {
            throw new Exception("In Block FUNAVAIL " + getLabel() + " at Process " + getProces().getDescpro() + "Missing operand C or block not found");
        }

        /**
         * The owning transaction in is the FEC
         */
        if (getModel().getFEC().contains(owningXact)) {

            String residualTimeName = getModel().evaluateExpression(D, tr);

            if (C.isEmpty()) {
                tr.restore(true);
            }

            float residualTimeValue = Math.abs(getModel().getRelativeClock() - owningXact.getMoveTime());

            if (!residualTimeName.isEmpty()) {
                owningXact.getTransactionParameters().put(residualTimeName, residualTimeValue);
            }

            owningXact.getTransactionParameters().put("residual-time", residualTimeValue);
            getModel().getFEC().remove(owningXact);
            owningXact.setMoveTime(owningXact.getMoveTime() - residualTimeValue);
            getModel().getBEC().get(A).add(owningXact);
        }

        /**
         * If E is CO, preempted Transactions are not removed from contention
         * for the Facility, and may own the Facility during any unavailable
         * period. Preempted Transactions may be given a new destination with
         * the F Operand.
         */
        if (E.equals("CO")) {

            if (getModel().getPreemptedXacts().get(facilityName) != null) {
                while (getModel().getPreemptedXacts().get(facilityName).iterator().hasNext()) {

                    Xact xact = getModel().getPreemptedXacts().get(facilityName).iterator().next();
                    xact.setOwnershipGranted(true);
                }
            }
        } /**
         * if E is RE, preempted Transactions are removed from contention for
         * the Facility. This means that the Transaction may continue to
         * circulate in the simulation without restrictions due to a preemption
         * at this Facility (there may be outstanding preemptions at other
         * Facilities, however). It also means that preempted Transactions must
         * not attempt to RETURN or RELEASE the Facility. Optionally, the F
         * Operand is available to redirect the course of such a Transaction.
         */
        else if (E.equals("RE") && getModel().getPreemptedXacts().get(facilityName) != null) {

            if (destinationF != null && destinationF instanceof Release) {
                throw new Exception("In Block FUNAVAIL " + getLabel() + " at Process " + getProces().getDescpro() + "Operand C refers to a Release Block and must not be used with RE option");
            }

            while (!getModel().getBEC().get(facilityName).isEmpty()) {
                Xact preemptedXact = getModel().getBEC().get(facilityName).poll();
                preemptedXact.setOwnershipGranted(true);
                getModel().getCEC().add(preemptedXact);
            }

        } /**
         * If E is Null, preempted Transactions are left on the Interrupt Chain
         * of the Facility, and cannot be granted ownership of the Facility
         * during the unavailable period.
         */
        else if (E.isEmpty() && getModel().getPreemptedXacts().get(facilityName) != null) {
            while (getModel().getBEC().get(facilityName).iterator().hasNext()) {

                Xact bloquedXsct = getModel().getBEC().get(facilityName).iterator().next();
                bloquedXsct.setOwnershipGranted(false);
            }
        }

        if (!F.isEmpty() && destinationF != null) {

            if (getModel().getPreemptedXacts().get(facilityName) != null) {
                while (getModel().getPreemptedXacts().get(facilityName).iterator().hasNext()) {
                    Xact bloquedXact = getModel().getPreemptedXacts().get(facilityName).iterator().next();
                    bloquedXact.setBloc(destinationF);
                }
            }
        } else {
            throw new Exception("In Block FUNAVAIL " + getLabel() + " at Process " + getProces().getDescpro() + "Missing operand F");
        }
        return nextBloc(tr);
    }

    @Override
    public String name() {
        return "Funavail";
    }
}
