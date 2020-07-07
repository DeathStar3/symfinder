/*
 * This file is part of symfinder.
 *
 * symfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * symfinder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with symfinder. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2018-2019 Johann Mortara <johann.mortara@univ-cotedazur.fr>
 * Copyright 2018-2019 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
 * Copyright 2018-2019 Philippe Collet <philippe.collet@univ-cotedazur.fr>
 */

package configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Configuration {
    private static Configuration ourInstance = new Configuration();

    public static Configuration getInstance() {
        return ourInstance;
    }

    private static ParametersObject properties;

    private Configuration() {
        this("symfinder.yaml");
    }

    private Configuration(String propertiesFile) {
        try (FileInputStream fis = new FileInputStream(propertiesFile)) {
            Yaml yaml = new Yaml();
            properties = yaml.loadAs(fis, ParametersObject.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // TODO: 11/28/18 create exception
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getNeo4JBoltAddress() {
        return properties.getNeo4j().boltAddress;
    }

    public static String getNeo4JUser() {
        return properties.getNeo4j().user;
    }

    public static String getNeo4JPassword() {
        return properties.getNeo4j().password;
    }

}
