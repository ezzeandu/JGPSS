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
package model.entities;

import exceptions.MalformedFunctionDistribution;
import java.util.ArrayList;
import javafx.util.Pair;
import lombok.Data;

/**
 *
 * @author Ezequiel Andujar Montes
 */
@Data
public class Function {

    private String name;
    private Float A;
    private String B;
    private ArrayList<Pair<Float, Float>> distribution;
    private int distributionSize;

    public final static String C = "C";
    public final static String D = "D";
    public final static String E = "E";
    public final static String L = "L";
    public final static String M = "M";

    public Function(String name, String A, String B, String d) throws MalformedFunctionDistribution {

        this.name = name;
        this.A = evaluateParameter(A);
        Pair<String, Integer> type = evaluateFunctionType(B);
        this.B = type.getKey();
        this.distributionSize = type.getValue();
        this.distribution = evaluateDistribution(d);
    }
    
    public Float evaluate() {
        
        Float result = 0f;
        
        switch(B) {
            case C:
                break;
            case D:
                break;
            case E:
                break;
            case L:
                break;
            case M:
                break;
        }
     
        return result;
    }

    private ArrayList<Pair<Float, Float>> evaluateDistribution(String d) throws MalformedFunctionDistribution {

        ArrayList<Pair<Float, Float>> dist = new ArrayList<>();

        String[] pairs = d.split("/");

        if (pairs.length == 0 || pairs.length != distributionSize) {
            throw new MalformedFunctionDistribution();
        }

        for (String pair : pairs) {
            String[] values = pair.split(",");
            if (values.length != 2) {
                throw new MalformedFunctionDistribution();
            }
            try {

                Float x = Float.valueOf(values[0]);
                Float y = Float.valueOf(values[1]);
                dist.add(new Pair<>(x, y));

            } catch (NumberFormatException e) {
                throw new MalformedFunctionDistribution();
            }
        }
        return dist;
    }

    private Pair<String, Integer> evaluateFunctionType(String t) throws MalformedFunctionDistribution {

        Integer size;
        String type;

        if (!t.matches("[CDELM0-9]+")) {
            throw new MalformedFunctionDistribution();
        }

        try {
            size = Integer.valueOf(t.split("^[A-Z]+")[1]);
        } catch (NumberFormatException e) {
            throw new MalformedFunctionDistribution();
        }
        type = t.substring(0, 1);    
        return new Pair<>(type, size);
    }

    private Float evaluateParameter(String A) throws MalformedFunctionDistribution {
        
        Float f = new Float(0);
        try {
            f = Float.parseFloat(A);
        }
        catch(NumberFormatException e) {
            throw new MalformedFunctionDistribution();
        }        
        return f;
    }
}
