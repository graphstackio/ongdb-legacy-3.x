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
package org.neo4j.server.security.enterprise.auth;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.server.security.auth.FileRepository;
import org.neo4j.server.security.auth.ListSnapshot;
import org.neo4j.server.security.auth.exception.FormatException;

import static org.neo4j.server.security.auth.ListSnapshot.FROM_MEMORY;
import static org.neo4j.server.security.auth.ListSnapshot.FROM_PERSISTED;

/**
 * Stores role data. In memory, but backed by persistent storage so changes to this repository will survive
 * JVM restarts and crashes.
 */
public class FileRoleRepository extends AbstractRoleRepository implements FileRepository
{
    private final File roleFile;
    private final Log log;
    private final RoleSerialization serialization = new RoleSerialization();
    private final FileSystemAbstraction fileSystem;

    public FileRoleRepository( FileSystemAbstraction fileSystem, File file, LogProvider logProvider )
    {
        this.roleFile = file;
        this.log = logProvider.getLog( getClass() );
        this.fileSystem = fileSystem;
    }

    @Override
    public void start() throws Throwable
    {
        clear();

        FileRepository.assertNotMigrated( roleFile, fileSystem, log );

        ListSnapshot<RoleRecord> onDiskRoles = readPersistedRoles();
        if ( onDiskRoles != null )
        {
            setRoles( onDiskRoles );
        }
    }

    @Override
    protected ListSnapshot<RoleRecord> readPersistedRoles() throws IOException
    {
        if ( fileSystem.fileExists( roleFile ) )
        {
            long readTime;
            List<RoleRecord> readRoles;
            try
            {
                readTime = fileSystem.lastModifiedTime( roleFile );
                readRoles = serialization.loadRecordsFromFile( fileSystem, roleFile );
            }
            catch ( FormatException e )
            {
                log.error( "Failed to read role file \"%s\" (%s)", roleFile.getAbsolutePath(), e.getMessage() );
                throw new IllegalStateException( "Failed to read role file '" + roleFile + "'." );
            }

            return new ListSnapshot<>( readTime, readRoles, FROM_PERSISTED );
        }
        return null;
    }

    @Override
    protected void persistRoles() throws IOException
    {
        serialization.saveRecordsToFile( fileSystem, roleFile, roles );
    }

    @Override
    public ListSnapshot<RoleRecord> getPersistedSnapshot() throws IOException
    {
        if ( lastLoaded.get() < fileSystem.lastModifiedTime( roleFile ) )
        {
            return readPersistedRoles();
        }
        synchronized ( this )
        {
            return new ListSnapshot<>( lastLoaded.get(), new ArrayList<>( roles ), FROM_MEMORY );
        }
    }

    @Override
    public void purge() throws IOException
    {
        super.purge(); // Clears all cached data

        // Delete the file
        if ( !fileSystem.deleteFile( roleFile ) )
        {
            throw new IOException( "Failed to delete file '" + roleFile.getAbsolutePath() + "'" );
        }
    }

    @Override
    public void markAsMigrated() throws IOException
    {
        super.markAsMigrated(); // Clears all cached data

        // Rename the file
        File destinationFile = FileRepository.getMigratedFile( roleFile );
        fileSystem.renameFile( roleFile, destinationFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES );
    }
}
