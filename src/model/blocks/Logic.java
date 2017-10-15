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

import java.util.HashMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.entities.Xact;
import utils.Constants;

/**
 * A class representing the LOGIC block.
 *
 * @author Pau Fonseca i Casas
 * @author Ezequiel Andujar Montes
 * @version 1
 * @see     <a href="http://www-eio.upc.es/~Pau/index.php?q=node/28">Project
 * website</a>
 * @serialData
 */
@NoArgsConstructor
@SuppressWarnings("FieldMayBeFinal")
public class Logic extends Bloc {

    @Getter
    @Setter
    private String x;

    @Getter
    @Setter
    private String A;

    /**
     * Logic SET.
     */
    public static final String S = "S";
    /**
     * Logis RESET.
     */
    public static final String R = "R";
    /**
     * Logic INVERT.
     */
    public static final String I = "I";

    /**
     * Creates a new instance of Logic.
     *
     * @param comentari the comment of the block.
     * @param label the labelofthe block.
     * @param x the type of logic R, S, I.
     * @param A the name of the logic.
     */
    public Logic(String comentari, String label, String x, String A) {
        super(Constants.idLogic, label, comentari);
        this.A = A;
        this.x = x;
    }

    @Override
    public Bloc execute(Xact tr) {
       
        incTrans(tr);
       
        HashMap<String, Facility> facilities = this.getModel().getFacilities();
        if (facilities.get(A) == null) {

            Facility fs = new Facility(getModel());
            facilities.put(A, fs);
        }
        if (this.x.equals(S)) {
            facilities.get(A).capture(tr);
        } else if (x.equals(R)) {
            facilities.get(A).release(tr);
        } else if (!facilities.get(A).isAvailable()) {
            facilities.get(A).capture(tr);
        } //facilities.put(this.A, (facilities.get(this.A) == 1) ? 0 : 1);
        return nextBloc(tr);
    }   

    @Override
    public String name() {
        return "Logic";
    }
}
