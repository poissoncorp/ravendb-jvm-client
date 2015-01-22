package net.ravendb.imports.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ravendb.abstractions.exceptions.JsonReaderException;
import net.ravendb.abstractions.extensions.JsonExtensions;
import net.ravendb.abstractions.json.linq.RavenJArray;
import net.ravendb.abstractions.json.linq.RavenJObject;
import net.ravendb.abstractions.json.linq.RavenJToken;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


public class JsonConvert {

  private static ObjectMapper objectMapper;

  public static String serializeObject(Object obj) {
    initObjectMapper();
    try {
      return JsonExtensions.createDefaultJsonSerializer().writer().writeValueAsString(obj);
    } catch (IOException e) {
      throw new RuntimeException("Unable to serialize object.", e);
    }
  }

  private static void initObjectMapper() {
    if (objectMapper != null) {
      return ;
    }
    synchronized (JsonConvert.class) {
      if (objectMapper == null) {
        objectMapper = JsonExtensions.createDefaultJsonSerializer();
      }
    }
  }

  /**
   * This method gets RavenJArray, extracts propertyName from each object and maps to targetClass
   * @param array
   * @param targetClass
   * @param propertyName
   * @return list of deserialized objects
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonParseException
   */
  public static <T> List<T> deserializeObject(RavenJArray array, Class<T> targetClass, String propertyName) {
    initObjectMapper();
    List<T> result = new ArrayList<>();

    try {
      for (RavenJToken token: array) {
        RavenJObject object = (RavenJObject) token;
        RavenJToken ravenJToken = object.get(propertyName);
        result.add(objectMapper.readValue(ravenJToken.toString(), targetClass));
      }
      return result;
    } catch (IOException e) {
      throw new JsonReaderException(e.getMessage(), e);
    }
  }

  public static <T> T deserializeObject(Class<T> targetClass, String input) {
    initObjectMapper();
    try {
      return objectMapper.readValue(input, targetClass);
    } catch (IOException e) {
      throw new JsonReaderException(e.getMessage(), e);
    }
  }


}
