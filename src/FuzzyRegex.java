/*
 * File:    FuzzyRegex.java
 * Package:
 * Author:  Zachary Gill
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides fuzzy regex pattern matching functionality.
 */
public final class FuzzyRegex {
    
    //Constants
    
    /**
     * The character used to specify a variable in a string compare.
     */
    public static final char VARCHAR = '¿';
    
    /**
     * The character used to specify a variable in a string compare, represented as a string.
     */
    public static final String VARCHAR_STRING = String.valueOf(VARCHAR);
    
    
    //Static Fields
    
    /**
     * The current index in the vars and tokens list to store extracted vars and tokens for extractVariables().
     *
     * @see #extractVariables(String, String, int[][], int, int, UUID, List, List, boolean, boolean)
     */
    private static final Map<UUID, Integer> extractionDepths = new ConcurrentHashMap<>();
    
    
    //Main Method
    
    /**
     * The main method.
     *
     * @param args Arguments to the main method.
     */
    public static void main(String[] args) {
        String source = "What the hell are lobsters";
        String target = "What ¿ ¿s";
        
        List<List<String>> vars = new ArrayList<>();
        List<List<String>> tokens = new ArrayList<>();
        
        double score = stringCompare(target, source, vars, tokens, false);
        System.out.println("Score: " + score);
        System.out.println();
        
        System.out.println("---");
        for (int i = 0; i < vars.size(); i++) {
            System.out.println("Extraction #" + i);
            
            System.out.println("  Variables:");
            for (String var : vars.get(i)) {
                System.out.println("    '" + var + "'");
            }
            System.out.println("  Tokens:");
            for (String token : tokens.get(i)) {
                System.out.println("    '" + token + "'");
            }
            
            System.out.println("---");
        }
    }
    
    
    //Functions
    
    /**
     * Compares the closeness of two strings and extracts variables and tokens if applicable.<br>
     * The text string is compared against the pattern string and the value returned will be equal to the length of the pattern minus the edit distance between the two strings, divided by the length of the pattern.<br>
     * If non-null vars and tokens lists are supplied as arguments, then following the comparison, the variables and tokens from the text as compared to the pattern will be extracted and stored in these lists.<br>
     * There may be multiple top-level extractions of variables and tokens from the string, if so, each top-level extraction will be stored in the lists.<br>
     * If the ignoreCase flag is set to true, then the case of the characters between the text and pattern strings will not be considered for scoring, however the cases of the text string will remain unchanged in the extractions.<br>
     * If the ignorePunctuation flag is set to true, then punctuation in the text string will not affect the score of the comparison, however the punctuation of the text string will remain in the extractions.<br>
     * If the ignorePunctuation flag is set to true, punctuation will most likely be assumed to be part of the variable or token to its left, even if the punctuation is specified in the pattern string.<br>
     * In addition, if the ignorePunctuation flag is set to true, all punctuation in the pattern string will be stripped, which may change the length of the pattern string and the resulting comparison score.<br>
     * If you wish to compare strings including punctuation, set the ignorePunctuation flag to false.
     *
     * @param pattern           The string to compare against.
     * @param text              The string to compare.
     * @param vars              The list to store extracted variables in. This will contain a list of all optimal variable extraction sets.
     * @param tokens            The list to store extracted non-variable tokens in. This will contain a list of all optimal non-variable token extraction sets.
     * @param ignoreCase        Whether to ignore case of not.
     * @param ignorePunctuation Whether to ignore punctuation of not.
     * @return The closeness of the two strings, between 0 and 1.
     */
    public static double stringCompare(String pattern, String text, List<List<String>> vars, List<List<String>> tokens, boolean ignoreCase, boolean ignorePunctuation) {
        if (pattern.isEmpty() || text.isEmpty()) {
            return 0.0;
        }
        
        pattern = pattern.replaceAll("[" + VARCHAR_STRING + "]+", VARCHAR_STRING); //remove double vars
        pattern = ignorePunctuation ? removePunctuationSoft(pattern, Collections.singletonList(VARCHAR)) : pattern;
        
        if (pattern.isEmpty()) {
            return 0.0;
        }
        
        int length = Math.max(pattern.length(), ignorePunctuation ? removePunctuation(text).length() : text.length());
        return truncateNum((double) (length - stringEditDistance(pattern, text, vars, tokens, ignoreCase, ignorePunctuation)) / length, 0.0, 1.0).doubleValue();
    }
    
    /**
     * Compares the closeness of two strings and extracts variables and non-variables is applicable.
     *
     * @param pattern    The string to compare against.
     * @param text       The string to compare.
     * @param vars       The list to store extracted variables in. This will contain a list of all optimal variable extraction sets.
     * @param tokens     The list to store extracted non-variable tokens in. This will contain a list of all optimal non-variable token extraction sets.
     * @param ignoreCase Whether to ignore case of not.
     * @return The closeness of the two strings, between 0 and 1.
     *
     * @see #stringCompare(String, String, List, List, boolean, boolean)
     */
    public static double stringCompare(String pattern, String text, List<List<String>> vars, List<List<String>> tokens, boolean ignoreCase) {
        return stringCompare(pattern, text, vars, tokens, false, false);
    }
    
    /**
     * Compares the closeness of two strings and extracts variables and non-variables is applicable.
     *
     * @param pattern The string to compare against.
     * @param text    The string to compare.
     * @param vars    The list to store extracted variables in. This will contain a list of all optimal variable extraction sets.
     * @param tokens  The list to store extracted non-variable tokens in. This will contain a list of all optimal non-variable token extraction sets.
     * @return The closeness of the two strings, between 0 and 1.
     *
     * @see #stringCompare(String, String, List, List, boolean)
     */
    public static double stringCompare(String pattern, String text, List<List<String>> vars, List<List<String>> tokens) {
        return stringCompare(pattern, text, vars, tokens, false);
    }
    
    /**
     * Compares the closeness of two strings and extracts variables if applicable.
     *
     * @param pattern The string to compare against.
     * @param text    The string to compare.
     * @param vars    The list to store extracted variables in. This will contain a list of all optimal variable extraction sets.
     * @return The closeness of the two strings, between 0 and 1.
     *
     * @see #stringCompare(String, String, List, List)
     */
    public static double stringCompare(String pattern, String text, List<List<String>> vars) {
        return stringCompare(pattern, text, vars, null);
    }
    
    /**
     * Compares the closeness of two strings.
     *
     * @param pattern           The string to compare against.
     * @param text              The string to compare.
     * @param ignoreCase        Whether to ignore case of not.
     * @param ignorePunctuation Whether to ignore punctuation of not.
     * @return The closeness of the two strings, between 0 and 1.
     *
     * @see #stringCompare(String, String, List, List, boolean, boolean)
     */
    public static double stringCompare(String pattern, String text, boolean ignoreCase, boolean ignorePunctuation) {
        return stringCompare(pattern, text, null, null, ignoreCase, ignorePunctuation);
    }
    
    /**
     * Compares the closeness of two strings.
     *
     * @param pattern    The string to compare against.
     * @param text       The string to compare.
     * @param ignoreCase Whether to ignore case of not.
     * @return The closeness of the two strings, between 0 and 1.
     *
     * @see #stringCompare(String, String, boolean, boolean)
     */
    public static double stringCompare(String pattern, String text, boolean ignoreCase) {
        return stringCompare(pattern, text, ignoreCase, false);
    }
    
    /**
     * Compares the closeness of two strings.
     *
     * @param pattern The string to compare against.
     * @param text    The string to compare.
     * @return The closeness of the two strings, between 0 and 1.
     *
     * @see #stringCompare(String, String, boolean)
     */
    public static double stringCompare(String pattern, String text) {
        return stringCompare(pattern, text, false);
    }
    
    /**
     * Calculates the distance between two strings with variable collection.<br>
     * The text string is compared against the pattern string and the value returned will be equal to the edit distance between the two strings.<br>
     * If non-null vars and tokens lists are supplied as arguments, then following the comparison, the variables and tokens from the text as compared to the pattern will be extracted and stored in these lists.<br>
     * There may be multiple top-level extractions of variables and tokens from the string, if so, each top-level extraction will be stored in the lists.<br>
     * If the ignoreCase flag is set to true, then the case of the characters between the text and pattern strings will not be considered for scoring, however the cases of the text string will remain unchanged in the extractions.<br>
     * If the ignorePunctuation flag is set to true, then punctuation in the text string will not affect the score of the comparison, however the punctuation of the text string will remain in the extractions.<br>
     * If the ignorePunctuation flag is set to true, punctuation will most likely be assumed to be part of the variable or token to its left, even if the punctuation is specified in the pattern string.<br>
     * In addition, if the ignorePunctuation flag is set to true, all punctuation in the pattern string will be stripped, which may change the length of the pattern string and the resulting comparison score.<br>
     * If you wish to compare strings including punctuation, set the ignorePunctuation flag to false.
     *
     * @param pattern           The pattern to compare the string against.
     * @param text              The string to compare.
     * @param vars              The list to store extracted variables in. This will contain a list of all optimal variable extraction sets.
     * @param tokens            The list to store extracted non-variable tokens in. This will contain a list of all optimal non-variable token extraction sets.
     * @param ignoreCase        Whether to ignore case or not.
     * @param ignorePunctuation Whether to ignore punctuation or not.
     * @return The edit distance between the two strings.
     */
    @SuppressWarnings({"AssignmentToMethodParameter", "SillyAssignment"})
    public static int stringEditDistance(String pattern, String text, List<List<String>> vars, List<List<String>> tokens, boolean ignoreCase, boolean ignorePunctuation) {
        pattern = ignorePunctuation ? removePunctuationSoft(pattern, Collections.singletonList(VARCHAR)) : pattern;
        pattern = pattern.replaceAll("[" + VARCHAR_STRING + "]+", VARCHAR_STRING); //remove double vars
        
        int m = pattern.length(); //i
        int n = text.length(); //j
        
        int g = 0; //number of variables
        int v = -1; //index of first variable
        for (int pi = 0; pi < m; pi++) {
            if (VARCHAR == pattern.charAt(pi)) {
                g++;
                if (v == -1) {
                    v = pi;
                }
            }
        }
        if (g == 0) {
            v = m;
        }
        
        int[][] D = new int[m + 1][n + 1];
        
        for (int j = 0; j <= n; j++) {
            D[0][j] = j;
        }
        for (int i = 0; i <= v; i++) {
            D[i][0] = i;
        }
        for (int i = v + 1; i <= m; i++) {
            D[i][0] = Integer.MAX_VALUE;
        }
        
        boolean init = false;
        for (int i = 1; i <= m; i++) {
            
            if (VARCHAR == pattern.charAt(i - 1)) {
                boolean hitZero = false;
                for (int j = 0; j <= n; j++) { //assuming don't care set could span from 0 chars to the rest of the string
                    if (D[i - 1][j] == 0) {
                        D[i][j] = 1;
                        hitZero = true;
                    } else if (hitZero) {
                        D[i][j - 1] = D[i - 1][j - 1];
                        for (j = j; j <= n; j++) {
                            D[i][j] = 0;
                        }
                        hitZero = false;
                        break; //no need to continue this loop
                    } else {
                        D[i][j] = (j > 0) ? Math.min(D[i - 1][j], D[i][j - 1]) : D[i - 1][j];
                    }
                }
                if (hitZero) {
                    D[i][n] = D[i - 1][n];
                }
                init = true;
                
            } else {
                char patternChar = pattern.charAt(i - 1);
                for (int j = 1; j <= n; j++) {
                    char textChar = text.charAt(j - 1);
                    if (ignorePunctuation && isSymbol(textChar)) {
                        for (j = j; j <= n; j++) {
                            if (isSymbol(text.charAt(j - 1))) {
                                if (!init) {
                                    D[i][j] = D[i][j - 1] - ((j == 1) ? 1 : 0);
                                    D[i - 1][j] = D[i - 1][j - 1];
                                } else {
                                    D[i][j] = D[i][j - 1];
                                }
                            } else {
                                j--;
                                break;
                            }
                        }
                    } else {
                        int matchChar;
                        if (ignoreCase) {
                            matchChar = (Character.toUpperCase(patternChar) == Character.toUpperCase(textChar)) ? 1 : 0;
                        } else {
                            matchChar = (patternChar == textChar) ? 1 : 0;
                        }
                        D[i][j] = 1 + Math.min(Math.min(D[i - 1][j], D[i][j - 1]),
                                D[i - 1][j - 1] - matchChar);
                    }
                    init = true;
                }
            }
        }
        
        if ((vars != null) && (g > 0)) { //if there are variables
            if (tokens == null) {
                tokens = new ArrayList<>();
            }
            extractVariables(pattern, text, D, vars, tokens, ignorePunctuation);
        }
        
        return D[m][n];
    }
    
    /**
     * Calculates the distance between two strings with variable collection.
     *
     * @param pattern    The pattern to compare the string against.
     * @param text       The string to compare.
     * @param vars       The list to store extracted variables in. This will contain a list of all optimal variable extraction sets.
     * @param tokens     The list to store extracted non-variable tokens in. This will contain a list of all optimal non-variable token extraction sets.
     * @param ignoreCase Whether to ignore case or not.
     * @return The edit distance between the two strings.
     *
     * @see #stringEditDistance(String, String, List, List, boolean, boolean)
     */
    public static int stringEditDistance(String pattern, String text, List<List<String>> vars, List<List<String>> tokens, boolean ignoreCase) {
        return stringEditDistance(pattern, text, vars, tokens, ignoreCase, false);
    }
    
    /**
     * Calculates the distance between two strings with variable collection.
     *
     * @param pattern The pattern to compare the string against.
     * @param text    The string to compare.
     * @param vars    The list to store extracted variables in. This will contain a list of all optimal variable extraction sets.
     * @param tokens  The list to store extracted non-variable tokens in. This will contain a list of all optimal non-variable token extraction sets.
     * @return The edit distance between the two strings.
     *
     * @see #stringEditDistance(String, String, List, List, boolean)
     */
    public static int stringEditDistance(String pattern, String text, List<List<String>> vars, List<List<String>> tokens) {
        return stringEditDistance(pattern, text, vars, tokens, false);
    }
    
    /**
     * Calculates the distance between two strings.
     *
     * @param pattern           The pattern to compare the string against.
     * @param text              The string to compare.
     * @param ignoreCase        Whether to ignore case or not.
     * @param ignorePunctuation Whether to ignore punctuation or not.
     * @return The edit distance between the two strings.
     *
     * @see #stringEditDistance(String, String, List, List, boolean, boolean)
     */
    public static int stringEditDistance(String pattern, String text, boolean ignoreCase, boolean ignorePunctuation) {
        return stringEditDistance(pattern, text, null, null, ignoreCase, ignorePunctuation);
    }
    
    /**
     * Calculates the distance between two strings.
     *
     * @param pattern    The pattern to compare the string against.
     * @param text       The string to compare.
     * @param ignoreCase Whether to ignore case or not.
     * @return The edit distance between the two strings.
     *
     * @see #stringEditDistance(String, String, boolean, boolean)
     */
    public static int stringEditDistance(String pattern, String text, boolean ignoreCase) {
        return stringEditDistance(pattern, text, ignoreCase, false);
    }
    
    /**
     * Calculates the distance between two strings.
     *
     * @param pattern The pattern to compare the string against.
     * @param text    The string to compare.
     * @return The edit distance between the two strings.
     *
     * @see #stringEditDistance(String, String, boolean)
     */
    public static int stringEditDistance(String pattern, String text) {
        return stringEditDistance(pattern, text, false);
    }
    
    /**
     * Extracts variables from the array produced by stringEditDistance().
     *
     * @param pattern           The pattern to compare the string against.
     * @param text              The string to compare.
     * @param D                 The array produced by stringEditDistance().
     * @param r                 The row to start extraction from.
     * @param c                 The column to start extraction from.
     * @param e                 The key associated with the algorithm's extraction depth in the extractionDepth map, null initially.
     * @param vars              The list to store extracted variables in. This will contain a list of all optimal variable extraction sets.
     * @param tokens            The list to store extracted non-variable tokens in. This will contain a list of all optimal non-variable token extraction sets.
     * @param varNext           A flag indicating whether the next extracted chunk will be a variable or not.
     * @param ignorePunctuation Whether to ignore punctuation or not.
     */
    @SuppressWarnings({"ConstantConditions"})
    private static void extractVariables(String pattern, String text, int[][] D, int r, int c, UUID e, List<List<String>> vars, List<List<String>> tokens, boolean varNext, boolean ignorePunctuation) {
        int row = r;
        int col = c;
        boolean endOfLine = (r == pattern.length()) && (c == text.length());
        
        if ((e == null) || endOfLine) {
            e = UUID.randomUUID();
            extractionDepths.put(e, 0);
        }
        int i = extractionDepths.get(e);
        
        if (vars.size() < (i + 1)) {
            vars.add(new ArrayList<>());
        }
        if (tokens.size() < (i + 1)) {
            tokens.add(new ArrayList<>());
        }
        
        StringBuilder thisVar = new StringBuilder();
        StringBuilder thisToken = new StringBuilder();
        while ((row >= 0) && (col > 0)) { //start at bottom right corner, move to upper left corner
            if ((row > 0) && VARCHAR == pattern.charAt(row - 1)) { //the current row is a var char
                
                while (col >= 1) { //move left to digest variable
                    if (D[row][col - 1] == D[row][col]) {
                        thisVar.insert(0, text.charAt(col - 1));
                        col--; //move left
                    }
                    
                    if ((row > 1) && (col >= 1)) {
                        if (D[row][col - 1] != D[row][col]) { //stop at the last col in the variable sequence
                            int min = Math.min(D[row - 1][col], D[row - 1][col - 1]); //move out of variable row
                            if (D[row - 1][col] == min) {
                                row--; //move up
                            } else { //D[row - 1][col - 1] == min
                                row--; //move diagonally up and left
                                col--;
                            }
                            break; //out of variable
                            
                        } else if (D[row][col - 1] == D[row - 1][col]) { //detect alternate optimal variable path
                            List<String> popVars = new ArrayList<>(vars.get(i));
                            List<String> popTokens = new ArrayList<>(tokens.get(i));
                            popVars.add(0, thisVar.toString());
                            vars.add(popVars);
                            tokens.add(popTokens);
                            int currentExtractionDepth = extractionDepths.get(e);
                            extractionDepths.replace(e, ++currentExtractionDepth);
                            extractVariables(pattern, text, D, row - 1, col, e, vars, tokens, false, ignorePunctuation);
                        }
                    }
                }
                
                vars.get(i).add(0, thisVar.toString());
                varNext = false;
                thisVar = new StringBuilder();
                
            } else if ((row > 1) && (VARCHAR == pattern.charAt(row - 2))) { //if the next row is a var char
                
                if (ignorePunctuation && isSymbol(text.charAt(col - 1))) {
                    while ((col > 1) && (D[row][col - 1] == D[row][col])) {
                        thisToken.insert(0, text.charAt(col - 1));
                        col--; //move left
                    }
                }
                
                if ((col > 1) && (D[row - 1][col - 1] == D[row - 1][col]) && (D[row][col] != D[row - 1][col - 1])) {
                    List<String> popVars = new ArrayList<>(vars.get(i));
                    List<String> popTokens = new ArrayList<>(tokens.get(i));
                    popTokens.add(0, text.charAt(col - 1) + thisToken.toString());
                    vars.add(popVars);
                    tokens.add(popTokens);
                    int currentExtractionDepth = extractionDepths.get(e);
                    extractionDepths.replace(e, ++currentExtractionDepth);
                    extractVariables(pattern, text, D, row - 1, col - 1, e, vars, tokens, true, ignorePunctuation);
                    
                    row--; //move up
                } else {
                    thisToken.insert(0, text.charAt(col - 1));
                    row--; //move diagonally up and left
                    col--;
                }
                
                tokens.get(i).add(0, thisToken.toString());
                varNext = true;
                thisToken = new StringBuilder();
                
            } else {
                if (row > 0) {
                    int min = Math.min(Math.min(D[row - 1][col], D[row - 1][col - 1]), D[row][col - 1]); //move
                    if (col > 0) {
                        if ((D[row - 1][col] == min) && (D[row - 1][col] == D[row - 1][col - 1]) && (D[row][col] == D[row][col - 1])) {
                            row--; //move up;
                        } else if (D[row - 1][col - 1] == min) {
                            thisToken.insert(0, text.charAt(col - 1));
                            row--; //move diagonally up and left
                            col--;
                        } else if (D[row - 1][col] == min) {
                            row--; //move up;
                        } else { //D[row][col - 1] == min
                            thisToken.insert(0, text.charAt(col - 1));
                            col--; //move left
                        }
                    }
                } else {
                    thisToken.insert(0, text.charAt(col - 1));
                    col--; //move left
                }
                
                if (col == 0) { //reached beginning of string
                    tokens.get(i).add(0, thisToken.toString());
                    varNext = true;
                    thisToken = new StringBuilder();
                }
            }
        }
        
        //digest unreached chunks in the pattern
        if ((col == 0) && (row > 0) && (pattern.length() >= row)) {
            while (row > 0) {
                boolean isVar = (pattern.charAt(row - 1) == VARCHAR);
                if (!isVar && !varNext) {
                    tokens.get(i).add(0, "");
                    varNext = true;
                } else if (isVar && varNext) {
                    vars.get(i).add(0, "");
                    varNext = false;
                }
                row--; //move up
            }
        }
        
        if (endOfLine) {
            extractionDepths.remove(e);
        }
    }
    
    /**
     * Extracts variables from the array produced by stringEditDistance().
     *
     * @param pattern           The pattern to compare the string against.
     * @param text              The string to compare.
     * @param D                 The array produced by stringEditDistance().
     * @param vars              The list to store extracted variables in. This will contain a list of all optimal variable extraction sets.
     * @param tokens            The list to store extracted non-variable tokens in. This will contain a list of all optimal non-variable token extraction sets.
     * @param ignorePunctuation Whether to ignore punctuation or not.
     */
    private static void extractVariables(String pattern, String text, int[][] D, List<List<String>> vars, List<List<String>> tokens, boolean ignorePunctuation) {
        extractVariables(pattern, text, D, pattern.length(), text.length(), null, vars, tokens, false, ignorePunctuation);
    }
    
    /**
     * Prints out the D matrix used in the stringEditDistance method.
     *
     * @param pattern The pattern to compare the string against.
     * @param text    The string to compare.
     * @param D       The matrix to print.
     * @return The D matrix as a string.
     *
     * @see #printDmatch(String, String, int[][], int, int)
     * @see #stringEditDistance(String, String, List, List, boolean, boolean)
     */
    private static String printDmatch(String pattern, String text, int[][] D) {
        return printDmatch(pattern, text, D, -1, -1);
    }
    
    /**
     * Prints out the D matrix used in the stringEditDistance method with an indicator at a location.
     *
     * @param pattern The pattern to compare the string against.
     * @param text    The string to compare.
     * @param D       The matrix to print.
     * @param row     The current row location of the algorithm.
     * @param col     The current column location of the algorithm.
     * @return The D matrix as a string.
     *
     * @see #stringEditDistance(String, String, List, List, boolean, boolean)
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static String printDmatch(String pattern, String text, int[][] D, int row, int col) {
        StringBuilder dMatch = new StringBuilder();
        int m = pattern.length(); //i
        int n = text.length(); //j
        
        int spaces = Math.max(String.valueOf(n).length(), String.valueOf(m).length()) + 2;
        
        for (int i = 0; i <= text.length(); i++) {
            if (i > 0) {
                dMatch.append(spaces(spaces - 1)).append(text.charAt(i - 1));
            } else {
                dMatch.append(spaces(spaces + 1));
            }
        }
        dMatch.append(System.lineSeparator());
        
        for (int i = 0; i <= m; i++) {
            if (i > 0) {
                dMatch.append(pattern.charAt(i - 1));
            } else {
                dMatch.append(" ");
            }
            
            for (int j = 0; j <= n; j++) {
                String element = (((i == row) && (j == col)) ? "*" : "") + ((D[i][j] > (Integer.MAX_VALUE / 2)) ? "^" : D[i][j]);
                dMatch.append(spaces(spaces - element.length())).append(element);
            }
            dMatch.append(System.lineSeparator());
        }
        
        System.out.println(dMatch);
        return dMatch.toString();
    }
    
    
    //String Functions
    
    /**
     * Determines if a character is a symbol or not.
     *
     * @param c The character.
     * @return Whether the character is a symbol or not.
     */
    public static boolean isSymbol(char c) {
        return !(Character.isLetterOrDigit(c) || isWhitespace(c));
    }
    
    /**
     * Determines if a character is whitespace or not.
     *
     * @param c The character.
     * @return Whether the character is whitespace or not.
     */
    public static boolean isWhitespace(char c) {
        return Character.isWhitespace(c) || (c == '\0');
    }
    
    /**
     * Removes the punctuation from a string.
     *
     * @param string The string to operate on.
     * @return The string with punctuation removed.
     */
    public static String removePunctuation(String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char current = string.charAt(i);
            if (Character.isLetterOrDigit(current) || Character.isWhitespace(current)) {
                sb.append(string.charAt(i));
            }
        }
        return sb.toString();
    }
    
    /**
     * Gently removes the punctuation from a string.
     *
     * @param string The string to operate on.
     * @param save   A list of punctuation characters to ignore.
     * @return The string with punctuation gently removed.
     */
    public static String removePunctuationSoft(String string, List<Character> save) {
        StringBuilder depuncted = new StringBuilder();
        
        for (char c : string.toCharArray()) {
            if (!isSymbol(c) || save.contains(c)) {
                depuncted.append(c);
            }
        }
        
        return depuncted.toString();
    }
    
    /**
     * Creates a string of the length specified filled with spaces.
     *
     * @param num The length to make the string.
     * @return A new string filled with spaces to the length specified.
     */
    public static String spaces(int num) {
        return fillStringOfLength(' ', num);
    }
    
    /**
     * Creates a string of the length specified filled with the character specified.
     *
     * @param fill The character to fill the string with.
     * @param size The length to make the string.
     * @return A new string filled with the specified character to the length specified.
     */
    public static String fillStringOfLength(char fill, int size) {
        return padRight("", size, fill);
    }
    
    /**
     * Pads a string on the right to a specified length.
     *
     * @param str     The string to pad.
     * @param size    The target size of the string.
     * @param padding The character to pad with.
     * @return The padded string.
     */
    public static String padRight(String str, int size, char padding) {
        if (str.length() >= size) {
            return str;
        }
        
        int numPad = size - str.length();
        char[] chars = new char[numPad];
        Arrays.fill(chars, padding);
        String pad = new String(chars);
        return str + pad;
    }
    
    
    //Bound Functions
    
    /**
     * Forces a number within defined bounds.
     *
     * @param num The number value.
     * @param min The minimum value allowed.
     * @param max The maximum value allowed.
     * @return The truncated number.
     */
    public static Number truncateNum(Number num, Number min, Number max) {
        Number n = num;
        if (num.doubleValue() < min.doubleValue()) {
            n = min;
        }
        if (num.doubleValue() > max.doubleValue()) {
            n = max;
        }
        return n;
    }
    
}
