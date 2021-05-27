package com.salesforce.tests.dependency;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * The default test class to be executed always
 */
public class HiddenTest extends BaseTest {

    @Test
    //GENERAL: Properly parse commands with variable # of whitespace characters (space, tab) between tokens.
    public void testGeneral() throws IOException {
        String[] input = {
                "DEPEND      A       B\n",
                "INSTALL A\n",
                "END\n"
        };


        String expectedOutput = "DEPEND      A       B\n" +
                "INSTALL A\n" +
                "Installing B\n" +
                "Installing A\n" +
                "END\n";

        runTest(expectedOutput, input);
    }

    @Test
    //DEPEND: Store the dependencies info properly.
    public void testDepend() throws IOException {

        String[] input = {
                "DEPEND A B\n",
                "INSTALL B\n",
                "END\n"
        };


        String expectedOutput = "DEPEND A B\n" +
                "INSTALL B\n" +
                "Installing B\n" +
                "END\n";

        runTest(expectedOutput, input);
    }


    @Test
    //INSTALL: Install an item together with its dependnecy items if those have not been installed yet.
    public void testInstall1() throws IOException {
        String[] input = {
                "DEPEND A B C\n",
                "DEPEND B D\n",
                "INSTALL A\n",
                "END\n"
        };

        String expectedOutput = "DEPEND A B C\n" +
                "DEPEND B D\n" +
                "INSTALL A\n" +
                "Installing D\n" +
                "Installing B\n" +
                "Installing C\n" +
                "Installing A\n" +
                "END\n";
        runTest(expectedOutput, input);
    }

    @Test
    //INSTALL: Shall not install an item which is already explicitly installed.
    public void testInstall2() throws IOException {
        String[] input = {"DEPEND A B C\n" +
                "DEPEND B D\n",
                "INSTALL A\n",
                "INSTALL A\n",
                "END\n"
        };

        String expectedOutput = "DEPEND A B C\n" +
                "DEPEND B D\n" +
                "INSTALL A\n" +
                "Installing D\n" +
                "Installing B\n" +
                "Installing C\n" +
                "Installing A\n" +
                "INSTALL A\n" +
                "A is already installed\n" +
                "END\n";

        runTest(expectedOutput, input);
    }


    @Test
    //INSTALL: Shall not install an item which is already implicitly installed.
    public void testInstall3() throws IOException {
        String[] input = {"DEPEND A B C\n",
                "DEPEND B D\n",
                "INSTALL A\n",
                "INSTALL B\n",
                "END\n"
        };

        String expectedOutput = "DEPEND A B C\n" +
                "DEPEND B D\n" +
                "INSTALL A\n" +
                "Installing D\n" +
                "Installing B\n" +
                "Installing C\n" +
                "Installing A\n" +
                "INSTALL B\n" +
                "B is already installed\n" +
                "END\n";

        runTest(expectedOutput, input);
    }

    @Test
    //INSTALL: No message shall be printed out while trying to implicitly install an already installed dependency item.
    public void testInstall4() throws IOException {
        String[] input = {
                "DEPEND A B\n" +
                        "DEPEND C B\n",
                "INSTALL A\n",
                "INSTALL C\n",
                "END\n"
        };

        String expectedOutput = "DEPEND A B\n" +
                "DEPEND C B\n" +
                "INSTALL A\n" +
                "Installing B\n" +
                "Installing A\n" +
                "INSTALL C\n" +
                "Installing C\n" +
                "END\n";

        runTest(expectedOutput, input);
    }

    @Test
    //REMOVE: Shall remove the installed item only if the item is no longer depended by any other installed items.
    public void testRemove1() throws IOException {
        String[] input = {
                "DEPEND A B\n",
                "INSTALL A\n",
                "REMOVE A\n",
                "END\n"
        };

        String expectedOutput = "DEPEND A B\n" +
                "INSTALL A\n" +
                "Installing B\n" +
                "Installing A\n" +
                "REMOVE A\n" +
                "Removing A\n" +
                "Removing B\n" +
                "END\n";

        runTest(expectedOutput, input);
    }

    @Test
    //REMOVE: Shall remove the installed item together with its dependency items
    // if the dependency items were installed implicitly and are no longer depended by any other installed items.
    public void testRemove2() throws IOException {
        String[] input = {
                "DEPEND A B C\n",
                "INSTALL A\n",
                "REMOVE A\n",
                "END\n"
        };

        String expectedOutput = "DEPEND A B C\n" +
                "INSTALL A\n" +
                "Installing B\n" +
                "Installing C\n" +
                "Installing A\n" +
                "REMOVE A\n" +
                "Removing A\n" +
                "Removing B\n" +
                "Removing C\n" +
                "END\n";
        runTest(expectedOutput, input);
    }

    @Test
    //REMOVE: Shall not remove an item which has not been installed yet.
    public void testRemove3() throws IOException {
        String[] input = {
                "DEPEND A B\n",
                "REMOVE A\n",
                "END\n"
        };
        String expectedOutput = "DEPEND A B\n" +
                "REMOVE A\n" +
                "A is not installed\n" +
                "END\n";

        runTest(expectedOutput, input);
    }

    @Test
    //REMOVE: Shall not remove dependency items which were installed explicitly while removing a dependent item.
    public void testRemove4() throws IOException {
        String[] input = {
                "DEPEND A B C\n",
                "INSTALL B\n",
                "INSTALL C\n",
                "INSTALL A\n",
                "REMOVE A\n",
                "END\n"
        };

        String expectedOutput = "DEPEND A B C\n" +
                "INSTALL B\n" +
                "Installing B\n" +
                "INSTALL C\n" +
                "Installing C\n" +
                "INSTALL A\n" +
                "Installing A\n" +
                "REMOVE A\n" +
                "Removing A\n" +
                "END\n";

        runTest(expectedOutput, input);
    }


    @Test
    //REMOVE: Shall not remove an item which is still needed.
    public void testRemove5() throws IOException {
        String[] input = {
                "DEPEND A B C\n",
                "INSTALL B\n",
                "INSTALL A\n",
                "REMOVE B\n",
                "REMOVE C\n",
                "END\n"
        };

        String expectedOutput = "DEPEND A B C\n" +
                "INSTALL B\n" +
                "Installing B\n" +
                "INSTALL A\n" +
                "Installing C\n" +
                "Installing A\n" +
                "REMOVE B\n" +
                "B is still needed\n" +
                "REMOVE C\n" +
                "C is still needed\n" +
                "END\n";

        runTest(expectedOutput, input);
    }
    
    @Test
    public void testIfUnitTestsAdded() throws ClassNotFoundException {
        String className = YourUnitTest.class.getName();
        Class c = Class.forName(className);

        for (Method method : c.getDeclaredMethods()) {
            Test testAnno = method.getAnnotation(Test.class);
            if (testAnno != null) {
                return;
            }
        }

        Assert.fail("You did not add any unit tests in " + className);
    }    
    
}
