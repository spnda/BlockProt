/*
 * Copyright (C) 2021 - 2024 spnda
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.sean.blockprot.bukkit.metrics;

import de.sean.blockprot.bukkit.BlockProt;
import org.bstats.charts.SimpleBarChart;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Simple bar chart that shows how many people are using an integration together with BlockProt.
 */
public final class IntegrationBarChart extends SimpleBarChart {
    public IntegrationBarChart() {
        super("integrations", new IntegrationBarChartData());
    }

    public static class IntegrationBarChartData implements Callable<Map<String, Integer>> {
        @Override
        public Map<String, Integer> call() {
            HashMap<String, Integer> map = new HashMap<>();
            for (var integration : BlockProt.getInstance().getIntegrations()) {
                if (integration.isEnabled()) {
                    map.put(integration.name, 1);
                } else {
                    map.put(integration.name, 0);
                }
            }
            return map;
        }
    }
}
