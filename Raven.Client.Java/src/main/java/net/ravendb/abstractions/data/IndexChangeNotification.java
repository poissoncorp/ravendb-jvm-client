package net.ravendb.abstractions.data;

import net.ravendb.abstractions.basic.EventArgs;


public class IndexChangeNotification extends EventArgs {
  private IndexChangeTypes type;
  private String name;
  private Etag etag;

  @Override
  public String toString() {
    return String.format("%s on %s", type, name);
  }

  /**
   * Type of change that occured on index.
   */
  public IndexChangeTypes getType() {
    return type;
  }

  /**
   * Type of change that occured on index.
   * @param type
   */
  public void setType(IndexChangeTypes type) {
    this.type = type;
  }

  /**
   * Name of index for which notification was created
   */
  public String getName() {
    return name;
  }

  /**
   * Name of index for which notification was created
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * TODO [ppekrol]
   */
  public Etag getEtag() {
    return etag;
  }

  /**
   * TODO [ppekrol]
   * @param etag
   */
  public void setEtag(Etag etag) {
    this.etag = etag;
  }
}
