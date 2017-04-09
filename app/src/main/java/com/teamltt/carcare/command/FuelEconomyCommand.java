/*
 * Copyright 2017, Team LTT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teamltt.carcare.command;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.SystemOfUnits;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class FuelEconomyCommand extends ConsumptionRateCommand implements SystemOfUnits {

    private SpeedCommand speedCommand = new SpeedCommand();

    private float fuelEconomy = -1.0f; // in L/100km

    @Override
    public void run(InputStream in, OutputStream out) throws IOException, InterruptedException {
        // speed first so it is available in performCalculations
        speedCommand.run(in, out);
        super.run(in, out);
        setStart(speedCommand.getStart());
    }

    @Override
    protected void performCalculations() {
        super.performCalculations();
        float speed = speedCommand.getMetricSpeed();
        float rate = getLitersPerHour();
        if (speed != -1.0f && rate != -1.0f) {
            fuelEconomy = rate / speed / 100;
        }
    }

    @Override
    public String getFormattedResult() {
        return useImperialUnits ? String.format(Locale.getDefault(), "%.1f%s", getImperialEconomy(), getResultUnit())
                : String.format(Locale.getDefault(), "%.1f%s", getMetricEconomy(), getResultUnit());
    }

    @Override
    public String getCalculatedResult() {
        return useImperialUnits ? String.valueOf(getImperialEconomy()) : String.valueOf(getMetricEconomy());
    }

    @Override
    public String getResultUnit() {
        return useImperialUnits ? "mi/gal" : "L/100km";
    }

    @Override
    public String getName() {
        return "Fuel Economy";
    }

    private float getMetricEconomy() {
        return fuelEconomy;
    }

    private float getImperialEconomy() {
        return getImperialUnit();
    }

    /**
     * calculation from https://en.wikipedia.org/wiki/Fuel_economy_in_automobiles#Units_of_measure
     *
     * @return float, the economy in the imperial units
     */
    @Override
    public float getImperialUnit() {
        return 235.0f / fuelEconomy;
    }
}
