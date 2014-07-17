/**
 * 
 */
package org.msh.tb.sync;

import org.msh.tb.sync.EntityKey.EntityKeyAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ricardo Memoria
 *
 */
public class EntityKeyList {

	private Map<Class, List<EntityKey>> entityKeys = new HashMap<Class, List<EntityKey>>();

	/**
	 * Register the entity key for the given class
	 * @param clazz
	 * @param clientId
	 * @param serverId
	 * @return instance of the {@link EntityKey} containing the keys
	 */
	protected EntityKey registerUpdatedKey(Class clazz, Integer clientId, Integer serverId) {
		// return the list of keys for the given class
		List<EntityKey> keys = findKeysFromClass(clazz);

		EntityKey key = new EntityKey(clazz, clientId, serverId);
		key.setAction(EntityKeyAction.UPDATED);
		keys.add(key);

		return key;
	}

	
	/**
	 * Register a key that was deleted from the client
	 * @param clazz
	 * @param clientId
	 * @param serverId
	 * @return
	 */
	protected EntityKey registerClientDeletedKey(Class clazz, Integer clientId) {
		// return the list of keys for the given class
		List<EntityKey> keys = findKeysFromClass(clazz);

		EntityKey key = new EntityKey(clazz, clientId, clientId);
		// just because constructor doesn't accept null arguments
		key.setServerId(null);
		key.setAction(EntityKeyAction.CLI_DELETED);
		keys.add(key);

		return key;
	}
	
	/**
	 * Register a deleted key from the server
	 * @param clazz
	 * @param serverId
	 * @return
	 */
	protected EntityKey registerDeletedKey(Class clazz, Integer serverId) {
		List<EntityKey> keys = findKeysFromClass(clazz);
		
		EntityKey key = new EntityKey(clazz, serverId);
		key.setAction(EntityKeyAction.SRV_DELETED);
		keys.add(key);
		return key;
	}
	
	
	/**
	 * Return the list of keys
	 * @param clazz
	 * @return
	 */
	private List<EntityKey> findKeysFromClass(Class clazz) {
		// return the list of keys for the given class
		List<EntityKey> keys = entityKeys.get(clazz);

		// is there any key list for the class?
		if (keys == null) {
			keys = new ArrayList<EntityKey>();
			entityKeys.put(clazz, keys);
		}
		
		return keys;
	}
	
	
	/**
	 * Search for an instance of {@link EntityKey} by its class and client ID
	 * @param clazz the entity class
	 * @param clientId the client ID
	 * @return instance of {@link EntityKey} class, or null if it was not found
	 */
	public EntityKey findEntityKey(Class clazz, int clientId) {
		// return the list of keys for the given class
		List<EntityKey> keys = entityKeys.get(clazz);
		
		if (keys == null)
			return null;
		
		for (EntityKey key: keys) {
			if (key.getClientId() == clientId) {
				return key;
			}
		}
		return null;
	}
	
	/**
	 * Update the server key 
	 * @param clazz
	 * @param clientId
	 * @param serverId
	 */
	protected void updateServerKey(Class clazz, int clientId, int serverId) {
		EntityKey key = findEntityKey(clazz, clientId);
		
		if (key == null)
			throw new RuntimeException("client key " + clientId + " was not found for class " + clazz);

		key.setServerId(serverId);
		key.setNewServerId(true);
	}
	
	/**
	 * Return the list of keys used for the given class
	 * @param clazz entity class to retrieve the keys
	 * @return instance of {@link List} containing instances of {@link EntityKey}
	 */
	public List<EntityKey> getKeys(Class clazz) {
		if (entityKeys == null)
			return null;

		return entityKeys.get(clazz);
	}
	
	
	/**
	 * Return information about client and server keys of all entities that
	 * generated new server key during synchronization.<br/>
	 * This list will be sent back to the client 
	 * @return List of {@link EntityKey} objects
	 */
	public List<EntityKey> getAllKeys() {
		if (entityKeys == null)
			return null;

		List<EntityKey> lst = new ArrayList<EntityKey>();
		for (Class clazz: entityKeys.keySet()) {
			for (EntityKey key: entityKeys.get(clazz)) {
					lst.add(key);
			}
		}
		return lst;
	}
}
