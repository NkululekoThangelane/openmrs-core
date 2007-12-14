/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.synchronization.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.server.RemoteServer;
import org.openmrs.synchronization.server.RemoteServerType;
import org.openmrs.synchronization.server.SyncServerRecord;

/**
 * sync strategy that implements sync-ing via disconnected push/pull.
 */
public class SyncStrategyFile {
    
    //fields
    private final Log log = LogFactory.getLog(getClass());
    
    //constructor
    public SyncStrategyFile() {}
    
    /** 
     * Using the sourceChild sync source, create a sync transmission using JournalManager   
     */
    public SyncTransmission createSyncTransmission(SyncSource source) {

        SyncTransmission tx = new SyncTransmission();
        List<SyncRecord> changeset = null;
        
        //retrieve value of the last sync timestamps
        SyncPoint lastSyncLocal = source.getLastSyncLocal();
        
        //establish the 'new' sync point; this will be new sync local after transmission was 'exported'
        SyncPoint lastSyncLocalNew = source.moveSyncPoint();

        //get changeset for sourceA
        changeset = this.getChangeset(source, lastSyncLocal,lastSyncLocalNew);
        
        String sourceGuid = source.getSyncSourceGuid();
        
        //pack it into transmission
        SyncTransmission syncTx = new SyncTransmission(sourceGuid,changeset);
        syncTx.createFile();
        
        //set new SyncPoint
        source.setLastSyncLocal(lastSyncLocalNew);
        
        return syncTx;
    }

    public SyncTransmission createStateBasedSyncTransmission(SyncSource source, boolean writeFileToo) {

        SyncTransmission ret =  null;
        RemoteServer parent = Context.getSynchronizationService().getParentServer();
        
        if ( parent != null ) {
            ret = createStateBasedSyncTransmission(source, writeFileToo, parent);
        }
        
        return ret;
    }

    public SyncTransmission createStateBasedSyncTransmission(SyncSource source, boolean writeFileToo, RemoteServer server) {

        SyncTransmission syncTx = null;
        
        if ( server != null ) {
            List<SyncRecord> changeset = null;
            List<SyncRecord> filteredChangeset = null;
            
            //get changeset for sourceA
            changeset = this.getStateBasedChangeset(source, server);
            
            // need to check each SyncRecord to see if it's eligible for sync'ing
            if ( changeset != null ) {
                for ( SyncRecord record : changeset ) {
                    Set<String> containedClasses = record.getContainedClassSet();
                    if ( server.getClassesSent().containsAll(containedClasses) ) {
                        //log.warn("ADDING RECORD TO TRANSMISSION BECAUSE SERVER IS SET TO SEND ALL " + containedClasses + " TO SERVER " + server.getNickname() + " with SENT: " + server.getClassesSent());
                        if ( filteredChangeset == null ) filteredChangeset = new ArrayList<SyncRecord>();
                        filteredChangeset.add(record);
                    } else {
                        if ( server.getServerType().equals(RemoteServerType.PARENT)) {
                            record.setState(SyncRecordState.NOT_SUPPOSED_TO_SYNC);
                            Context.getSynchronizationService().updateSyncRecord(record);
                        } else {
                            SyncServerRecord serverRecord = record.getServerRecord(server);
                            if ( serverRecord != null ) {
                                serverRecord.setState(SyncRecordState.NOT_SUPPOSED_TO_SYNC);
                                Context.getSynchronizationService().updateSyncRecord(record);
                            }
                        }
                        log.warn("NOT ADDING RECORD TO TRANSMISSION, SERVER IS NOT SET TO SEND ALL OF " + containedClasses + " TO SERVER " + server.getNickname());
                    }
                }
            }
            
            String sourceGuid = source.getSyncSourceGuid();
            
            //pack it into transmission
            syncTx = new SyncTransmission(sourceGuid,filteredChangeset);
            syncTx.createFile(writeFileToo);
            syncTx.setSyncTargetGuid(server.getGuid());
        }
                
        return syncTx;
    }

    /** 
     * Update status of a given sync transmission
     */
    public void updateSyncTransmission(SyncTransmission Tx) {
        
        //TODO
        
        return;

    }
    
    /**
     * 
     * TODO: Review the 'exported' transmissions and return the list of the ones that did not receive 
     * a confirmation from the server; these are in the 'pending' state.
     *
     */
    public List<String> getPendingTransmissions() {
        //TODO
        List<String> pending = new ArrayList<String>();
        
        return pending;
    }

    /**
     * Apply given sync tx to source. 
     */
    public void applySyncTransmission(SyncSource source, SyncTransmission tx) {

        //TODO
        
        return;
    }
    
    
    private List<SyncRecord> getChangeset(SyncSource source, SyncPoint from,SyncPoint to) {
        List<SyncRecord> deleted = null;
        List<SyncRecord> changed = null;
        List<SyncRecord> changeset = null;
                
        //get all local deletes, inserts and updates
        deleted = source.getDeleted(from,to);
        changed = source.getChanged(from,to);
        
        //merge
        changeset = deleted;
        changeset.addAll(changed);
 
        return changeset;
    }

    private List<SyncRecord> getStateBasedChangeset(SyncSource source) {
        List<SyncRecord> deleted = null;
        List<SyncRecord> changed = null;
        List<SyncRecord> changeset = null;
                
        //get all local deletes, inserts and updates
        deleted = source.getDeleted();
        changed = source.getChanged();
        
        //merge
        changeset = deleted;
        changeset.addAll(changed);
 
        return changeset;
    }

    private List<SyncRecord> getStateBasedChangeset(SyncSource source, RemoteServer server) {
        List<SyncRecord> deleted = null;
        List<SyncRecord> changed = null;
        List<SyncRecord> changeset = null;
                
        //get all local deletes, inserts and updates
        deleted = source.getDeleted();
        changed = source.getChanged(server);
        
        //merge
        changeset = deleted;
        changeset.addAll(changed);
 
        return changeset;
    }

    //apply items to source
    private void applyChangeset(SyncSource source, List<SyncRecord> items  ) {
        
        //TODO
        return;
    }

}
