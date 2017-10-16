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
import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Ezequiel Andujar Montes
 */
public class FunctionTest extends TestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public FunctionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void test1() throws Exception {
        Function f = new Function("test", "1", "D4", "1,3/2,5/3,8/4,12");
        assertEquals(4, f.getDistributionSize());
    }

    @Test
    public void test2() throws Exception {
        Function f = new Function("test", "1", "C3", "1,3/2,5/3,8");
        assertEquals(3, f.getDistributionSize());
    }

    @Test
    public void test3() throws MalformedFunctionDistribution {

        try {
            Function f = new Function("test", "1", "C6", "1,3/2,5/3,8");
        } catch (MalformedFunctionDistribution e) {
            assertEquals(e.getMessage(), "Malformed function distribution");
        }
    }

    @Test
    public void test4() throws MalformedFunctionDistribution {

        try {
            Function f = new Function("test", "1", "C3", "1,3/2,/3,8");
        } catch (MalformedFunctionDistribution e) {
            assertEquals(e.getMessage(), "Malformed function distribution");
        }
    }

    @Test
    public void test5() throws MalformedFunctionDistribution {

        try {
            Function f = new Function("test", "1", "C3", "1,3/2,r/3,8");
        } catch (MalformedFunctionDistribution e) {
            assertEquals(e.getMessage(), "Malformed function distribution");
        }
    }

    @Test
    public void test6() throws MalformedFunctionDistribution {

        try {
            Function f = new Function("test", "1", "X3", "1,3/2,5/3,8");
        } catch (MalformedFunctionDistribution e) {
            assertEquals(e.getMessage(), "Malformed function distribution");
        }
    }

    @Test
    public void test7() throws Exception {
        Function f = new Function("test", "1", "C3", "1,3/2,5/3,8");
        assertEquals(Function.C, f.getB());
    }
}
