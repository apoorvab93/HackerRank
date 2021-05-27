package com.salesforce.tests.dependency;

import java.util.*;

/**
 * The entry point for the Test program
 */
public class Main {

    static final String LIST = "LIST";
    static final String DEPEND = "DEPEND";
    static final String INSTALL = "INSTALL";
    static final String REMOVE = "REMOVE";

    public static void main(String[] args) {

        //read input from stdin
        Scanner scan = new Scanner(System.in);
        HashMap<String, HashSet<String>> graph = new HashMap<String, HashSet<String>>();
        HashMap<String, HashSet<String>> inverseGraph = new HashMap<String, HashSet<String>>();
        LinkedHashSet<String> installedComponents = new LinkedHashSet<String>();
        HashSet<String> explicitlyInstalled = new HashSet<>(); // New set to contain items explicitly installed

        while (true) {
            String line = scan.nextLine();

            //no action for empty input
            if (line == null || line.length() == 0) {
                continue;
            }

            String[] subStrings = line.split("\\s+");
            if (subStrings.length < 1)
            {
                System.out.println("Incorrect input, ignoring");
                continue;
            }

            String command = subStrings[0].trim();
            //TODO: Convert to command pattern and clean up conditional code below
            switch(command)
            {
                case DEPEND:
                    System.out.println(line);
                    HandleDependCommand(subStrings, graph, inverseGraph);
                    break;
                case INSTALL:
                    System.out.println(line);
                    HandleInstallCommand_Recursive(subStrings, graph, installedComponents, explicitlyInstalled);
                    break;
                case LIST:
                    System.out.println(line);
                    HandleListCommand(installedComponents);
                    break;
                case REMOVE:
                    System.out.println(line);
                    HandleRemoveCommand(subStrings, graph, inverseGraph, installedComponents, explicitlyInstalled);
                    break;
            }

            //the END command to stop the program
            if ("END".equals(line)) {
                System.out.println("END");
                break;
            }
        }
    }

    /*

    GRAPH =>
    TELNET -> TCPIP, NETCARD
    TCPIP -> NETCARD
    DNS-> TCPIP, NETCARD
    BROWSER -> TCPIP, HTML

    INVERSEGRAPH =>
    TCPIP -> TELNET, DNS, BROWSER
    NETCARD -> TELNET, TCIP, DNS
    HTML -> BROWSER
    */
    // Takes the command and builds a dependency graph and an inverse dependency graph.
    // The use of two maps makes writing an iterative approach easier.
    private static void HandleDependCommand(
            String[] subStrings,
            HashMap<String, HashSet<String>> graph,
            HashMap<String, HashSet<String>> inverseGraph
    )
    {
        String component = subStrings[1].trim();
        HashSet<String> requirements = new HashSet<>();
        for (int i = 2; i < subStrings.length; i++)
        {
            String currentComponent = subStrings[i].trim();
            requirements.add(currentComponent);
        }

        boolean isCausingCycle = doesCycleExist(component, requirements, graph);
        if (!isCausingCycle)
        {
            graph.put(component, requirements);
            for (String currentComponent : requirements)
            {
                if (inverseGraph.containsKey(currentComponent))
                {
                    inverseGraph.get(currentComponent).add(component);
                }
                else
                {
                    HashSet<String> set = new HashSet<>();
                    set.add(component);
                    inverseGraph.put(currentComponent, set);
                }
            }
        }
    }

    private static void HandleInstallCommand_Recursive(
            String[] subStrings,
            HashMap<String, HashSet<String>> graph,
            HashSet<String> installedSet,
            HashSet<String> explicitlyInstalled
    )
    {
        String component = subStrings[1].trim();
        if(installedSet.contains(component))
        {
            System.out.println(component+" is already installed");
        }
        // explicitly installed components will be tracked separately so that we can prevent their removal in cases
        // where a component dependent on an explicitly installed component is removed
        explicitlyInstalled.add(component);
        recurse(component, graph, installedSet);
    }

    // recursive call to install dependencies
    private static void recurse(String currentComponent,
                        HashMap<String, HashSet<String>> graph,
                        HashSet<String> installedSet)
    {
        if(installedSet.contains(currentComponent)){
            return;
        }
        if(graph.containsKey(currentComponent))
        {
            for(String dependent: graph.get(currentComponent))
            {
                recurse(dependent, graph, installedSet);
            }
        }
        System.out.println("Installing "+currentComponent);
        installedSet.add(currentComponent);
    }

    private static void HandleListCommand(LinkedHashSet<String> installedSet)
    {
        for (String component : installedSet)
        {
            System.out.println(component);
        }
    }

    private static void HandleRemoveCommand(
            String[] subStrings,
            HashMap<String, HashSet<String>> graph,
            HashMap<String, HashSet<String>> inverseGraph,
            HashSet<String> installedSet,
            HashSet<String> explicitlyInstalled)
    {
        String component = subStrings[1].trim();
        if (!installedSet.contains(component))
        {
            System.out.println(component+" is not installed");
            return;
        }

        Stack<String> stack = new Stack<>();
        stack.push(component);

        HashSet<String> itemsRemovable = new HashSet<>();
        while (!stack.isEmpty())
        {
            String current = stack.pop();
            boolean canRemove = canBeRemoved(current, inverseGraph, installedSet, itemsRemovable);
            if (!canRemove) {
                //No need to check its dependencies
                continue;
            }

            // Since the current component can be removed, lets check if its dependent components can also be removed
            itemsRemovable.add(current);
            if (graph.containsKey(current))
            {
                HashSet<String> candidatesForRemoval = graph.get(current);
                for (String each : candidatesForRemoval)
                {
                    if (installedSet.contains(each))
                    {
                        stack.push(each);
                    }
                }
            }
        }

        if (!itemsRemovable.contains(component)) {
            System.out.println(component +" is still needed");
            return;
        }

        // Remove all candidate dependencies
        for (String item : itemsRemovable)
        {
            if (!explicitlyInstalled.contains(item) || item.equals(component)) {
                //Allow removal
                System.out.println("Removing " + item);
                installedSet.remove(item);
                explicitlyInstalled.remove(item);
            }
        }
    }

    // This method tells us if its safe to remove a component from the installed item list
    private static boolean canBeRemoved(
            String current,
            HashMap<String, HashSet<String>> inverseGraph,
            HashSet<String> installedSet,
            HashSet<String> itemsRemovable)
    {
        if (inverseGraph.containsKey(current))
        {
            //Some components rely on current, check if these are installed
            HashSet<String> dependents = inverseGraph.get(current);
            for (String dependent : dependents)
            {
                if (installedSet.contains(dependent) && !itemsRemovable.contains(dependent))
                {
                    // The item is installed and not eligible for removal
                    return false;
                }
            }
        }

        return true;

    }

    // This method checks whether a cycle exists if the component and its requirements are added to the dependency graph
    private static boolean doesCycleExist(
            String component,
            HashSet<String> requirements,
            HashMap<String, HashSet<String>> graph)
    {
        Stack<String> stack = new Stack<>();
        for (String requirement : requirements)
        {
            stack.push(requirement);
        }
        HashSet<String> allRequirements = new HashSet<>();
        while (!stack.isEmpty())
        {
            String currentComponent = stack.pop();
            allRequirements.add(currentComponent);
            if (graph.containsKey(currentComponent))
            {
                for (String requiredComponent : graph.get(currentComponent))
                {
                    if (!allRequirements.contains(requiredComponent))
                    {
                        stack.push(requiredComponent);
                    }
                }
            }
        }

        for (String requirement : allRequirements)
        {
            if (graph.containsKey(requirement))
            {
                boolean exists = graph.get(requirement).contains(component);
                if (exists) // cycle found
                {
                    System.out.println(requirement+" depends on " +component+", ignoring command");
                    return true;
                }
            }
        }

        return false;
    }
}