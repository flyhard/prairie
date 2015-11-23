package com.intropro.prairie.unit.cmd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by presidentio on 11/14/15.
 */
public class CommandProcessor extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(CommandProcessor.class);
    private static final Pattern ARGS_PATTERN = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
    private static final String FIRST_LINE_MARKER = "---this is the first line marker for prairie---";

    private Socket socket;

    private CommandProvider commandProvider;

    public CommandProcessor(Socket socket, CommandProvider commandProvider) {
        this.socket = socket;
        this.commandProvider = commandProvider;
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            List<String> args = parseArgs(bufferedReader.readLine());
            if (args.size() < 1) {
                throw new IllegalArgumentException("Argument line must contains at least command alias");
            }
            String alias = args.get(0);
            LOGGER.info("Command started: " + args);
            Command command = commandProvider.getCommand(alias);
            try {
                printWriter.write(FIRST_LINE_MARKER);
                int status = command.exec(args.subList(1, args.size()), bufferedReader, printWriter);
                printWriter.write("\n" + status);
                printWriter.flush();
            } catch (InterruptedException e) {
                LOGGER.warn("Command " + command + " interrupted");
            }
            bufferedReader.close();
            printWriter.close();
            socket.close();
            LOGGER.info("Command finished: " + alias);
        } catch (IOException e) {
            LOGGER.error("Client socket error", e);
        }
    }

    private List<String> parseArgs(String argLine) {
        List<String> matchList = new ArrayList<>();
        Matcher regexMatcher = ARGS_PATTERN.matcher(argLine);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }
        return matchList;
    }
}