package net.ravendb.client.document.sessionoperations;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import net.ravendb.abstractions.data.Constants;
import net.ravendb.abstractions.data.JsonDocument;
import net.ravendb.abstractions.data.MultiLoadResult;
import net.ravendb.abstractions.json.linq.RavenJArray;
import net.ravendb.abstractions.json.linq.RavenJObject;
import net.ravendb.abstractions.json.linq.RavenJToken;
import net.ravendb.client.connection.SerializationHelper;
import net.ravendb.client.document.InMemoryDocumentSessionOperations;



public class LoadTransformerOperation {
  private InMemoryDocumentSessionOperations documentSession;
  private final String transformer;
  private String[] ids;

  public LoadTransformerOperation(InMemoryDocumentSessionOperations documentSession, String transformer, String[] ids) {
    this.documentSession = documentSession;
    this.transformer = transformer;
    this.ids = ids;
  }

  @SuppressWarnings("unchecked")
  public <T> T[] complete(Class<T> clazz, MultiLoadResult multiLoadResult) {

    for (JsonDocument include : SerializationHelper.ravenJObjectsToJsonDocuments(multiLoadResult.getIncludes())) {
      documentSession.trackIncludedDocument(include);
    }

    if (clazz.isArray()) {

      // Returns array of arrays, public APIs don't surface that yet though as we only support Transform
      // With a single Id
      List<RavenJObject> results = multiLoadResult.getResults();
      List<T> items = new ArrayList<>();

      Class<?> innerType = clazz.getComponentType();

      for (RavenJObject result : results) {
        List<RavenJObject> values = result.value(RavenJArray.class, "$values").values(RavenJObject.class);
        List<Object> innerTypes = new ArrayList<>();
        for (RavenJObject value: values) {
          ensureNotReadVetoed(value);
          innerTypes.add(documentSession.projectionToInstance(value, innerType));
        }
        Object[] innerArray = (Object[]) Array.newInstance(innerType, innerTypes.size());
        for (int i = 0; i < innerTypes.size(); i++) {
          innerArray[i] = innerTypes.get(i);
        }
        items.add((T) innerArray);
      }

      return (T[]) items.toArray();

    } else {
      List<T> items = parseResults(clazz, multiLoadResult.getResults());

      if (items.size() > ids.length) {
        throw new IllegalStateException(String.format("A load was attempted with transformer %s, and more " +
            "than one item was returned per entity - please use %s[] as the projection type instead of %s",
            transformer, clazz.getSimpleName(), clazz.getSimpleName()));
      }
      return (T[]) items.toArray();
    }
  }

  private <T> List<T> parseResults(Class<T> clazz, List<RavenJObject> results) {


    List<T> items = new ArrayList<>();
    for (RavenJObject object : results) {
      ensureNotReadVetoed(object);
      for (RavenJToken token : object.value(RavenJArray.class, "$values")) {
        items.add(documentSession.getConventions().createSerializer().deserialize(token, clazz));
      }
    }

    return items;
  }

  private boolean ensureNotReadVetoed(RavenJObject result) {
    RavenJObject metadata = result.value(RavenJObject.class, Constants.METADATA);
    if (metadata != null) {
      documentSession.ensureNotReadVetoed(metadata); //this will throw on read veto
    }
    return true;
  }
}
