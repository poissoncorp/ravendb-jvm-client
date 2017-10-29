package net.ravendb.client.documents.commands;

import net.ravendb.client.documents.conventions.DocumentConventions;
import net.ravendb.client.documents.queries.IndexQuery;
import net.ravendb.client.documents.queries.QueryResult;
import net.ravendb.client.http.RavenCommand;
import net.ravendb.client.http.ServerNode;
import net.ravendb.client.json.ContentProviderHttpEntity;
import net.ravendb.client.primitives.Reference;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;

import java.io.IOException;

public class QueryCommand extends RavenCommand<QueryResult> {
    private final DocumentConventions _conventions;
    private final IndexQuery _indexQuery;
    private final boolean _metadataOnly;
    private final boolean _indexEntriesOnly;

    public QueryCommand(DocumentConventions conventions, IndexQuery indexQuery, boolean metadataOnly, boolean indexEntiresOnly) {
        super(QueryResult.class);

        if (conventions == null) {
            throw new IllegalArgumentException("conventions cannot be null");
        }

        if (indexQuery == null) {
            throw new IllegalArgumentException("indexQuery cannot be null");
        }

        _conventions = conventions;
        _indexQuery = indexQuery;
        _metadataOnly = metadataOnly;
        _indexEntriesOnly = indexEntiresOnly;

/* TODO

            if (indexQuery.WaitForNonStaleResultsTimeout.HasValue && indexQuery.WaitForNonStaleResultsTimeout != TimeSpan.MaxValue)
                Timeout = indexQuery.WaitForNonStaleResultsTimeout.Value.Add(TimeSpan.FromSeconds(10)); // giving the server an opportunity to finish the response
 */

    }

    @Override
    public HttpRequestBase createRequest(ServerNode node, Reference<String> url) {
        canCache = !_indexQuery.isDisableCaching();

        // we won't allow aggresive caching of queries with WaitForNonStaleResults
        canCacheAggressively = canCache && !_indexQuery.isWaitForNonStaleResults();

        StringBuilder path = new StringBuilder(node.getUrl())
                .append("/databases/")
                .append(node.getDatabase())
                .append("/queries?query-hash=")
                // we need to add a query hash because we are using POST queries
                // so we need to unique parameter per query so the query cache will
                // work properly
                .append(_indexQuery.getQueryHash());

        if (_metadataOnly) {
            path.append("&metadata-only=true");
        }

        if (_indexEntriesOnly) {
            path.append("&debug=entries");
        }

        HttpPost request = new HttpPost();
        request.setEntity(new ContentProviderHttpEntity(outputStream -> {
            //TODO: writer.WriteIndexQuery(_conventions, ctx, _indexQuery);
        }, ContentType.APPLICATION_JSON));

        url.value = path.toString();
        return request;
    }

    @Override
    public void setResponse(String response, boolean fromCache) throws IOException {
        if (response == null) {
            result = null;
            return;
        }

        //TODO: JsonDeserializationClient.QueryResult(response);
        if (fromCache) {
            result.setDurationInMs(-1);
        }
    }

    @Override
    public boolean isReadRequest() {
        return true;
    }
}
