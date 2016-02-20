package com.coding4people.mosquitoreport.api.indexers;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;

import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomain;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomainClient;
import com.amazonaws.services.cloudsearchdomain.model.SearchRequest;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsRequest;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearch;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsRequest;
import com.amazonaws.services.cloudsearchv2.model.DomainStatus;
import com.amazonaws.services.cloudsearchv2.model.ServiceEndpoint;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.coding4people.mosquitoreport.api.Env;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

abstract public class Indexer<T> {
    @Inject AmazonCloudSearch amazonCloudSearch;
    
    @Inject Env env;

    AmazonCloudSearchDomain domain;

    abstract protected Class<T> getType();
    
    public void index(T item) {
        // TODO send to SQS in order to handle it asynchronously
        try {
            domain.uploadDocuments(new UploadDocumentsRequest().withDocuments(new ByteArrayInputStream(
                    new ObjectMapper().writeValueAsString(item).getBytes(StandardCharsets.UTF_8))));
        } catch (JsonProcessingException e) {
            // TODO log indexer errors
            e.printStackTrace();
        }
    }

    public Object search(String query) {
        return domain.search(new SearchRequest().withQuery(query));
    }

    @PostConstruct
    protected void postConstruct() {
        DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest().withDomainNames(getDomainName());
        List<DomainStatus> list = amazonCloudSearch.describeDomains(describeDomainsRequest).getDomainStatusList();

        if (list.isEmpty()) {
            throw new InternalServerErrorException("Could not find CloudSearch domain: " + getDomainName());
        }
        
        ServiceEndpoint searchService = list.get(0).getSearchService();
        
        if (searchService.getEndpoint() == null) {
            throw new InternalServerErrorException("Could not find CloudSearch domain: " + getDomainName());
        }
        
        domain = new AmazonCloudSearchDomainClient();
        domain.setEndpoint(searchService.getEndpoint());
    }

    protected String getDomainName() {
        return getPrefix() + getType().getAnnotation(DynamoDBTable.class).tableName();
    }
    
    protected String getPrefix() {
        return Optional.ofNullable(env.get("MOSQUITO_REPORT_CLOUDSEARCH_DOMAIN_PREFIX")).orElse("localhost") + "-";
    }
}
