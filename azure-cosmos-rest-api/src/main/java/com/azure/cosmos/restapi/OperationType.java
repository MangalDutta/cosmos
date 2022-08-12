package com.azure.cosmos.restapi;

import com.azure.cosmos.implementation.RequestVerb;
import com.azure.cosmos.implementation.ResourceType;

public enum OperationType {

    LIST_ALL_DATABASES(ResourceType.Database, RequestVerb.GET),
    LIST_ALL_PKRANGES(ResourceType.PartitionKeyRange, RequestVerb.GET),
    LIST_ALL_COLLECTIONS(ResourceType.DocumentCollection, RequestVerb.GET);

    OperationType(final ResourceType resourceType, final RequestVerb requestVerb) {
        this.resourceType = resourceType;
        this.requestVerb = requestVerb;
    }

    private ResourceType resourceType;
    private RequestVerb requestVerb;

    public RequestVerb getRequestVerb() {
        return requestVerb;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }
}
