package com.salesforce.tests.dependency;

import org.junit.Test;

import java.io.IOException;

/**
 * Place holder for your unit tests
 */
public class YourUnitTest extends BaseTest {
    @Test
    public void badInputTest() throws IOException {
        String[] input = {
                "DEPEND A B C\n",
                "INSTALL B\n",
                "INSTALL A\n",
                "BAD A\n",
                "INPUT A\n",
                "KEYWORDS A\n",
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
    public void emptyInputTest() throws IOException {
        String[] input = {
                "END\n"
        };

        String expectedOutput =
                "END\n";

        runTest(expectedOutput, input);
    }

    @Test
    public void noDependencyTest() throws IOException {
        String[] input = {
                "INSTALL B\n",
                "INSTALL A\n",
                "BAD A\n",
                "INPUT A\n",
                "KEYWORDS A\n",
                "REMOVE B\n",
                "REMOVE A\n",
                "END\n"
        };

        String expectedOutput =
                "INSTALL B\n" +
                "Installing B\n" +
                "INSTALL A\n" +
                "Installing A\n" +
                "REMOVE B\n" +
                "Removing B\n" +
                "REMOVE A\n" +
                "Removing A\n" +
                "END\n";

        runTest(expectedOutput, input);
    }

    @Test
    public void removingThoseNotInstalled() throws IOException {
        String[] input = {
                "INSTALL B\n",
                "INSTALL A\n",
                "BAD A\n",
                "INPUT A\n",
                "KEYWORDS A\n",
                "REMOVE B\n",
                "REMOVE C\n",
                "END\n"
        };

        String expectedOutput =
                "INSTALL B\n" +
                        "Installing B\n" +
                        "INSTALL A\n" +
                        "Installing A\n" +
                        "REMOVE B\n" +
                        "Removing B\n" +
                        "REMOVE C\n" +
                        "C is not installed\n" +
                        "END\n";

        runTest(expectedOutput, input);
    }
}
