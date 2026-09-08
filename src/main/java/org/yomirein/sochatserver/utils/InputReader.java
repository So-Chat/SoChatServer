package org.yomirein.sochatserver.utils;

import java.io.BufferedReader;
import java.io.IOException;

public class InputReader {
    // BufferedReader readLine for easier use
    // (I won't use Scanner because I think BufferedReader more compatible for just typing a few words in console)
    // (and maybe commands in future))
    public static String readLine(BufferedReader in, String prompt) {
        try {
            String input = in.readLine();
            if (!input.isEmpty()) { return input; }
            else { return prompt; }
        }
        catch (IOException _) { return prompt; }
    }
}
