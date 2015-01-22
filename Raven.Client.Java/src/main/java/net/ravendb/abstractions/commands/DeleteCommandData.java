package net.ravendb.abstractions.commands;

import net.ravendb.abstractions.data.Etag;
import net.ravendb.abstractions.data.HttpMethods;
import net.ravendb.abstractions.json.linq.RavenJObject;
import net.ravendb.abstractions.json.linq.RavenJValue;

/**
 * A single batch operation for a document DELETE
 */
public class DeleteCommandData implements ICommandData {
  private String key;
  private Etag etag;
  private RavenJObject additionalData;

  public DeleteCommandData(String key, Etag etag) {
    super();
    this.key = key;
    this.etag = etag;
  }

  public DeleteCommandData() {
    super();
  }

  /**
   * Additional command data. For internal use only.
   */
  @Override
  public RavenJObject getAdditionalData() {
    return additionalData;
  }

  /**
   * Current document etag, used for concurrency checks (null to skip check)
   */
  @Override
  public Etag getEtag() {
    return etag;
  }

  /**
   * Key of a document to delete.
   */
  @Override
  public String getKey() {
    return key;
  }

  /**
   * RavenJObject representing document's metadata. In this case null.
   */
  @Override
  public RavenJObject getMetadata() {
    return null;
  }

  /**
   * Returns operation method. In this case DELETE.
   */
  @Override
  public HttpMethods getMethod() {
    return HttpMethods.DELETE;
  }

  /**
   * Additional command data. For internal use only.
   * @param additionalData
   */
  @Override
  public void setAdditionalData(RavenJObject additionalData) {
    this.additionalData = additionalData;
  }

  /**
   * Current document etag, used for concurrency checks (null to skip check)
   * @param etag
   */
  public void setEtag(Etag etag) {
    this.etag = etag;
  }

  /**
   * Key of a document to delete.
   * @param key
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Translates this instance to a Json object.
   * @return RavenJObject representing the command.
   */
  @Override
  public RavenJObject toJson() {
    RavenJObject object = new RavenJObject();
    object.add("Key", new RavenJValue(key));
    object.add("Etag", new RavenJValue(etag != null ? etag.toString() : null));
    object.add("Method", new RavenJValue(getMethod().name()));
    object.add("AdditionalData", additionalData);
    return object;
  }

}
