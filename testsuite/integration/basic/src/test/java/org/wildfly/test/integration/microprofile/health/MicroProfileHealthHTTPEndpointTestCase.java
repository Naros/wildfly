/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.test.integration.microprofile.health;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.as.arquillian.container.ManagementClient;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2018 Red Hat inc.
 */
public class MicroProfileHealthHTTPEndpointTestCase extends MicroProfileHealthTestBase{

    void checkGlobalOutcome(ManagementClient managementClient, boolean mustBeUP, String probeName) throws IOException {

        final String healthURL = "http://" + managementClient.getMgmtAddress() + ":" + managementClient.getMgmtPort() + "/health";

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            CloseableHttpResponse resp = client.execute(new HttpGet(healthURL));
            assertEquals(mustBeUP ? 200 : 503, resp.getStatusLine().getStatusCode());

            String content = EntityUtils.toString(resp.getEntity());
            resp.close();

            try (
                    JsonReader jsonReader = Json.createReader(new StringReader(content))
            ) {
                JsonObject payload = jsonReader.readObject();
                String outcome = payload.getString("outcome");
                assertEquals(mustBeUP ? "UP": "DOWN", outcome);

                if (probeName != null) {
                    for (JsonValue check : payload.getJsonArray("checks")) {
                        if (probeName.equals(check.asJsonObject().getString("name"))) {
                            // probe name found
                            assertEquals(mustBeUP ? "UP" : "DOWN", check.asJsonObject().getString("state"));
                            return;
                        }
                    }
                    fail("Probe named " + probeName + " not found in " + content);
                }
            }
        }
    }
}
