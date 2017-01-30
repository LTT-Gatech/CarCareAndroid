/*
 ** Copyright 2017, Team LTT
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package com.teamltt.carcare.model;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.PendingTroubleCodesCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.util.HashMap;
import java.util.Map;

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
        translator.put("Reset", new ObdResetCommand());
        translator.put("Echo Off", new EchoOffCommand());
        translator.put("Line Feed Off", new LineFeedOffCommand());
        translator.put("Timeout", new TimeoutCommand(125));
        translator.put("Set Protocol", new SelectProtocolCommand(ObdProtocols.AUTO));
        translator.put("RPM", new RPMCommand());
        translator.put("Pending Trouble Codes", new PendingTroubleCodesCommand());
        translator.put("Engine Coolant Temp", new EngineCoolantTemperatureCommand());
        translator.put("Vehicle Speed", new SpeedCommand());
        translator.put("Intake Air Temp", new AirIntakeTemperatureCommand());
        translator.put("Current Engine Runtime", new RuntimeCommand());
    }


}
