/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.wildfly.extension.messaging.activemq;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;

/**
 * The add operation for the messaging subsystem's http-acceptor resource.
 *
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2013 Red Hat inc.
 */
public class HTTPAcceptorAdd extends ActiveMQReloadRequiredHandlers.AddStepHandler {

    public static final HTTPAcceptorAdd INSTANCE = new HTTPAcceptorAdd();

    private HTTPAcceptorAdd() {
        super(HTTPAcceptorDefinition.ATTRIBUTES);
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        super.performRuntime(context, operation, model);

        PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
        String activemqServerName = address.getElement(address.size() - 2).getValue();
        String acceptorName = address.getLastElement().getValue();
        final ModelNode fullModel = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS));

        launchServices(context, activemqServerName, acceptorName, fullModel);
    }

    void launchServices(OperationContext context, String activemqServerName, String acceptorName, ModelNode model) throws OperationFailedException {
        String httpConnectorName = HTTPAcceptorDefinition.HTTP_LISTENER.resolveModelAttribute(context, model).asString();

        HTTPUpgradeService.installService(context.getServiceTarget(),
                activemqServerName,
                acceptorName,
                httpConnectorName);
    }
}