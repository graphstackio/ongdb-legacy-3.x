/*
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of ONgDB Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3
 * (http://www.fsf.org/licensing/licenses/agpl-3.0.html) with the
 * Commons Clause, as found
 * in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 */
package org.neo4j.server.security.enterprise.auth.plugin;

import org.apache.shiro.authc.AuthenticationToken;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import org.neo4j.server.security.enterprise.auth.ShiroAuthToken;

/**
 * Version of ShiroAuthToken that returns credentials as a char array
 * so that it is compatible for credentials matching with the
 * cacheable auth info results returned by the plugin API
 */
public class PluginShiroAuthToken extends ShiroAuthToken
{
    private final char[] credentials;

    private PluginShiroAuthToken( Map<String,Object> authTokenMap )
    {
        super( authTokenMap );
        // Convert credentials UTF8 byte[] to char[] (this should not create any intermediate copies)
        byte[] credentialsBytes = (byte[]) super.getCredentials();
        credentials = credentialsBytes != null ? StandardCharsets.UTF_8.decode( ByteBuffer.wrap( credentialsBytes ) ).array() : null;
    }

    @Override
    public Object getCredentials()
    {
        return credentials;
    }

    void clearCredentials()
    {
        if ( credentials != null )
        {
            Arrays.fill( credentials, (char) 0 );
        }
    }

    public static PluginShiroAuthToken of( ShiroAuthToken shiroAuthToken )
    {
        return new PluginShiroAuthToken( shiroAuthToken.getAuthTokenMap() );
    }

    public static PluginShiroAuthToken of( AuthenticationToken authenticationToken )
    {
        ShiroAuthToken shiroAuthToken = (ShiroAuthToken) authenticationToken;
        return PluginShiroAuthToken.of( shiroAuthToken );
    }
}
