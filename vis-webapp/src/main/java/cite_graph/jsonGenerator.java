/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Chris Bellis, Chris Perry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package cite_graph;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Chris on 7/16/15.
 * <p>
 * Static class that can generate a random JSONObject through its getRandomJSON() method, in the form of
 * <p>
 * {
 * "Nodes":[
 * {"name":"NAME","group":1234},
 * {"name":"NAME","group":1234}
 * ],
 * "Links":[
 * {"source":1234,"target":1234,"value":1234},
 * {"source":1234,"target":1234,"value":1234}
 * ]
 * }
 */
public class jsonGenerator {
    /**
     * Weighting for the random assignments of groups
     * <p>
     * A random number is generated between 0 and GROUP_10, and that number is compared against the various GROUP
     * thresholds.  The greater the difference between a group and its predecessor, the greater the chance of that group
     * getting selected for any given node (GROUP_0 is calculated against 0).
     * <p>
     * NOTE: putting these vars out of order will ruin the weighting.  Use increasing numbers only.
     * <p>
     * GROUP_10 is the upper bound of the Random object, so make others relative to GROUP_10 when configuring.
     */
    private final static int GROUP_0 = 50,
            GROUP_1 = 80,
            GROUP_2 = 400,
            GROUP_3 = 450,
            GROUP_4 = 500,
            GROUP_5 = 550,
            GROUP_6 = 700,
            GROUP_7 = 800,
            GROUP_8 = 875,
            GROUP_9 = 950,
            GROUP_10 = 1000;
    /**
     * The RNG to be used throughout this class.
     */
    private final static Random random = new Random();
    /**
     * The maximum amount of nodes to create
     */
    private static int MAX_NODES = 100;
    /**
     * The maximum value of a connection
     */
    private static int MAX_VALUE = 4;
    /**
     * Them maximum number of targets a source can have
     */
    private static int MAX_CONNECTIONS = 5;
    /**
     * The set of names to chose from in generating the nodes
     */
    private static List names;

    /**
     * Creates a randomized 'testData' jsonObject
     *
     * @return a JSONObject in the following format:
     * {
     * "Nodes":[
     * {"name":"NAME","group":1},
     * {"name":"OTHER NAME","group":2}
     * ],
     * "Links":[
     * {"source":1,"target":2,"value":1},
     * {"source":2,"target":3,"value":4}
     * ]
     * }
     */
    public static JSONObject getRandomJSON() {
        JSONObject constructedJson = new JSONObject();
        JSONArray nodes;
        JSONArray links;

        // Grab a shuffled List of names to use in the nodes
        names = populateNames();
        // If the number of max nodes exceeds the number of names available, shrink it to the size of the array.
        if (MAX_NODES > names.size()) {
            MAX_NODES = names.size();
        }

        // Swallow any malformed JSONObject Exceptions
        try {
            // Insert the nodes and links arrays into the JSONObject
            links = generateLinks();
            constructedJson.put("links", links);
            nodes = generateNodes(names);
            constructedJson.put("nodes", nodes);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return constructedJson;
    }

    /**
     * Create and return a shuffled arrayList of random names
     *
     * @return a List of names
     */
    private static List<String> populateNames() {
        // Some famous people
        List<String> famousPeople = new ArrayList<>(
                Arrays.asList(
                        "Jesus",
                        "Napoleon",
                        "Muhammad",
                        "William Shakespeare",
                        "Abraham Lincoln",
                        "George Washington",
                        "Adolf Hitler",
                        "Aristotle",
                        "Alexander the Great",
                        "Thomas Jefferson",
                        "Henry VIII of England",
                        "Charles Darwin",
                        "Elizabeth I of England",
                        "Karl Marx",
                        "Julius Caesar",
                        "Queen Victoria",
                        "Martin Luther",
                        "Joseph Stalin",
                        "Albert Einstein",
                        "Christopher Columbus",
                        "Isaac Newton",
                        "Charlemagne",
                        "Theodore Roosevelt",
                        "Wolfgang Amadeus Mozart",
                        "Plato",
                        "Louis XIV of France",
                        "Ludwig van Beethoven",
                        "Ulysses S. Grant",
                        "Leonardo da Vinci",
                        "Augustus",
                        "Carl Linnaeus",
                        "Ronald Reagan",
                        "Charles Dickens",
                        "Paul the Apostle",
                        "Benjamin Franklin",
                        "George W. Bush",
                        "Winston Churchill",
                        "Genghis Khan",
                        "Charles I of England",
                        "Thomas Edison",
                        "James I of England",
                        "Friedrich Nietzsche",
                        "Franklin D. Roosevelt",
                        "Sigmund Freud",
                        "Alexander Hamilton",
                        "Mohandas Karamchand Gandhi",
                        "Woodrow Wilson",
                        "Johann Sebastian Bach",
                        "Galileo Galilei",
                        "Oliver Cromwell",
                        "James Madison",
                        "Gautama Buddha",
                        "Mark Twain",
                        "Edgar Allan Poe",
                        "Joseph Smith, Jr.",
                        "Adam Smith",
                        "David, King of Israel",
                        "George III of the United Kingdom",
                        "Immanuel Kant",
                        "James Cook",
                        "John Adams",
                        "Richard Wagner",
                        "Pyotr Ilyich Tchaikovsky",
                        "Voltaire",
                        "Saint Peter",
                        "Andrew Jackson",
                        "Constantine the Great",
                        "Socrates",
                        "Elvis Presley",
                        "William the Conqueror",
                        "John F. Kennedy",
                        "Augustine of Hippo",
                        "Vincent van Gogh",
                        "Nicolaus Copernicus",
                        "Vladimir Lenin",
                        "Robert E. Lee",
                        "Oscar Wilde",
                        "Charles II of England",
                        "Cicero",
                        "Jean-Jacques Rousseau",
                        "Francis Bacon",
                        "Richard Nixon",
                        "Louis XVI of France",
                        "Charles V, Holy Roman Emperor",
                        "King Arthur",
                        "Michelangelo",
                        "Philip II of Spain",
                        "Johann Wolfgang von Goethe",
                        "Thomas Aquinas",
                        "Pope John Paul II",
                        "René Descartes",
                        "Nikola Tesla",
                        "Harry S. Truman",
                        "Joan of Arc",
                        "Dante Alighieri",
                        "Otto von Bismarck",
                        "Grover Cleveland",
                        "John Calvin",
                        "John Locke",
                        "Chris Bellis",
                        "Chris Perry",
                        "Max Fowler"
                ));
        // Randomize it so the order is not consistent
        Collections.shuffle(famousPeople);
        return famousPeople;
    }

    /**
     * Creates and returns a JSONArray containing randomized nodes
     *
     * @return a JSONArray consisting of
     * [
     * {"Name":"NAME", "Group":1},
     * {"Name":"OTHER NAME", "Group":2}
     * ]
     * @JSONException If there is an attempt to create a malformed node JSONObject.
     * -Shouldn't happen.
     */
    private static JSONArray generateNodes(List<String> names) throws JSONException {
        JSONArray nodes = new JSONArray();
        // Grab a name and assign a group for the first MAX_NODE elements in the names list
        for (int i = 0; i < MAX_NODES; i++) {
            String name = names.get(i);
            JSONObject node = new JSONObject();
            node.put("group", determineGroup());
            node.put("name", name);

            nodes.put(node);
        }

        return nodes;
    }

    /**
     * Creates and returns a JSONArray containing randomized links
     *
     * @return a JSONArray consisting of
     * [
     * {"source":1,"target":2,"value":1},
     * {"source":2,"target":3,"value":1}
     * <p>
     * ]
     * @JSONException If there is an attempt to create a malformed link JSONObject.
     * -Shouldn't happen.
     */
    private static JSONArray generateLinks() throws JSONException {
        JSONArray links = new JSONArray();
        JSONObject link;

        // Generate the connections
        // For each node, generate between 1 & MAX_CONNECTIONS connections
        for (int source = 0; source < MAX_NODES; source++) {
            int connections = random.nextInt(MAX_CONNECTIONS) + 1;

            // For each connection, grab a random target
            for (int i = 0; i < connections; i++) {
                int target;

                // Assign it a random target (trying again if it's the same value as the source)
                do {
                    target = random.nextInt(MAX_NODES);
                } while (source == target);

                // Assign it a random 'value'
                int value = random.nextInt(MAX_VALUE - 1) + 1;

                // Add this link into links
                link = new JSONObject();
                link.put("source", source).put("target", target).put("value", value);
                links.put(link);
            }
        }

        return links;
    }

    /**
     * A helper method that figures out which group to stick a node in
     *
     * @return a weighted group number
     */
    private static int determineGroup() {
        int unweightedGroup = random.nextInt(GROUP_10);
        if (unweightedGroup < GROUP_0) {
            return 0;
        } else if (unweightedGroup < GROUP_1) {
            return 1;
        } else if (unweightedGroup < GROUP_2) {
            return 2;
        } else if (unweightedGroup < GROUP_3) {
            return 3;
        } else if (unweightedGroup < GROUP_4) {
            return 4;
        } else if (unweightedGroup < GROUP_5) {
            return 5;
        } else if (unweightedGroup < GROUP_6) {
            return 6;
        } else if (unweightedGroup < GROUP_7) {
            return 7;
        } else if (unweightedGroup < GROUP_8) {
            return 8;
        } else if (unweightedGroup < GROUP_9) {
            return 9;
        } else {
            return 10;
        }
    }

}