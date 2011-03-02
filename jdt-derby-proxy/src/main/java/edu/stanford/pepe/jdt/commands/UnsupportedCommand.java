package edu.stanford.pepe.jdt.commands;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class UnsupportedCommand extends Command {

    private final String command;

    public UnsupportedCommand(String command) {
        this.command = command;
    }

    @Override
    public void apply(PepePreparedStatement ps) {
        throw new RuntimeException("Unsupported command: " + command);
    }

}
