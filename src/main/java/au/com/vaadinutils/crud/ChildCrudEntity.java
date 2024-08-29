package au.com.vaadinutils.crud;

/**
 * This is so a crud can select the same record after saving one or more new
 * records.<br>
 * 
 * As the id doesn't exist before the entity is persisted and the child crud has
 * possibly multiple records uncommitted it needs a "GUID" that is created when
 * the entity is instantiated so that it can locate the record again after being
 * persisted.<br>
 * 
 * Recommended implementation looks like this...<br>
 * 
 * <pre>
 * <code>
 * 
* &#64;Override
* public String getGuid() {
*     return guid;
* }
*
* @Column(updatable = false)
* private String guid = UUID.randomUUID().toString();
 * </code>
 * </pre>
 * 
 */
public interface ChildCrudEntity extends CrudEntity {

    String getGuid();
}
