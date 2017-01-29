package com.teamltt.carcare.model;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jack on 1/28/2017.
 */

public final class ObdTranslator {

    private static Map<String, ObdCommand> translator;


    public static ObdCommand translate(String englishCommand) {
        if (translator == null) {
            buildTranslator();
        }
        return translator.get(englishCommand);
    }

    private static void buildTranslator() {
        translator = new HashMap<>();
        translator.put("Echo Off", new EchoOffCommand());
        translator.put("Line Feed Off", new LineFeedOffCommand());
        translator.put("Timeout", new TimeoutCommand(125));
        translator.put("Set Protocol", new SelectProtocolCommand(ObdProtocols.AUTO));
        translator.put("Reset", new ObdResetCommand());
        translator.put("RPM", new RPMCommand());
    }


}
