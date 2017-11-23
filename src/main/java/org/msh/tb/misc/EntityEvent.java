package org.msh.tb.misc;

/**
 * Created by Mauricio on 12/05/2015.
 */
public class EntityEvent {

    public enum EventType {
        NEW,
        EDIT,
        DELETE;
    }

    private EventType type;
    private Object entity;

    public EntityEvent(EventType type, Object entity) {
        this.type = type;
        this.entity = entity;
    }

    public EntityEvent() {

    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }
}
